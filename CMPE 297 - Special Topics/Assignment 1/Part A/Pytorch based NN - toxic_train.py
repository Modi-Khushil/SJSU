from logging.config import valid_ident
from pyexpat import model
from transformers import BertTokenizer
import torch
import torch.nn as nn
import pandas as pd

class BERTDataset:
    def __init__(self, texts, labels, max_len=128):
        self.texts = texts
        self.labels = labels
        self.max_len = max_len
        self.tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
        self.num_examples = len(self.texts)

    def __len__(self):
        return len(self.data)

    def __getitem__(self, idx):
        text = str(self.texts[idx])
        label = self.data[idx][0]
        tokenized_text = self.tokenizer(text, add_special_tokens=True, max_length=self.max_len, truncation=True, padding='max_length')  
        ids =  tokenized_text["input_ids"]
        mask = tokenized_text["attention_mask"]
        token_type_ids = tokenized_text["token_type_ids"]
        return {"ids": torch.tensor(ids, dtype=torch.long), 
        "mask": torch.tensor(mask, dtype=torch.long), 
        "token_type_ids": torch.tensor(token_type_ids, dtype=torch.long), 
        "label": torch.tensor(label, dtype=torch.long)}

class ToxicModel(nn.Module):
    #A simple bert model for training a 2 class classification problem
    def __init__(self, max_len, hidden_dim, num_layers, num_classes):
        super().__init__()
        self.bert = BertModel.from_pretrained('bert-base-uncased', return_dic = False)
        self.dropout = nn.Dropout(0.1)
        self.classifier = nn.Linear(768, 1)
        
    def forward(self, ids, mask, token_type_ids):
        _, pooled_output = self.bert(ids, attention_mask=mask, token_type_ids=token_type_ids)
        dropout_output = self.dropout(pooled_output)
        logits = self.classifier(dropout_output)
        return logits

    def train(model, train_dataset, valid_dataset, epochs = 1):
        #Train a model
        device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        model.to(device)
        optimizer = torch.optim.Adam(model.parameters(), lr=0.001)
        criterion = nn.BCEWithLogitsLoss()
        
        for epoch in range(epochs):
            model.train()
            train_loss = 0
            for step, batch in enumerate(train_dataset):
                batch = tuple(t.to(device) for t in batch)
                ids = batch["ids"].to(device)
                mask = batch["mask"].to(device)
                token_type_ids = batch["token_type_ids"].to(device)
                target = batch["target"].to(device)
                logits = model(ids, mask, token_type_ids)
                loss = criterion(logits, target.view(-1, 1))
                optimizer.zero_grad()
                loss.backward()
                optimizer.step()
                if step % 100 == 0:
                    print(f'Epoch {epoch+1}, Batch {step+1}/{len(train_dataset)} Loss: {loss.item():.4f}')
            train_loss = train_loss/len(train_dataset)
            model.eval()
            valid_loss = 0
            valid_acc = 0
            for batch in valid_dataset:
                ids = batch["ids"].to(device)
                mask = batch["mask"].to(device)
                token_type_ids = batch["token_type_ids"].to(device)
                target = batch["target"].to(device)
                logits = model(ids, mask, token_type_ids)
                loss = criterion(logits, target.view(-1, 1))
                valid_loss += loss.item()
                pred = torch.sigmoid(logits) > 0.5
                valid_acc += torch.mean((pred == target).float()).item()
            print(f'Epoch {epoch+1}, Validation Loss: {valid_loss/len(valid_dataset):.4f}, Validation Accuracy: {valid_acc/len(valid_dataset):.4f}')

if __name__ == "__main__":
    df_train = pd.read_csv('~/datasets/toxic/toxic_train.csv').head(5000)
    df_valid = pd.read_csv('~/datasets/toxic/toxic_valid.csv').head(100)
    train_dataset = BERTDataset(df_train.comment_text.values, df_train.is_toxic.values)
    valid_dataset = BERTDataset(df_valid.comment_text.values, df_valid.is_toxic.values)
    train_data_loader = torch.utils.data.DataLoader(train_dataset, batch_size=32, shuffle=True)
    valid_data_loader = torch.utils.data.DataLoader(valid_dataset, batch_size=32, shuffle=True)
    model = ToxicModel(max_len=128, hidden_dim=768, num_layers=1, num_classes=1)
    train(model, train_data_loader, valid_data_loader, epochs=1)