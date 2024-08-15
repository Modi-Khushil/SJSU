#Solving MNIST using tensorflow keras and a CNN

#Import all the modules
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
import numpy as np
import matplotlib.pyplot as plt
import os
import random
import time
import sys
import math
import pandas as pd
import tensorflow.keras.backend as K
from tensorflow.keras.models import Sequential

#Load dataset
(x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()

#Normalize the data
x_train = x_train / 255.0
x_test = x_test / 255.0

#Reshape the data
x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
x_test = x_test.reshape(x_test.shape[0], 28, 28, 1)

#Define the model
model = Sequential()
model.add(layers.Conv2D(32, (3, 3), activation='relu', input_shape=(28, 28, 1)))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Conv2D(64, (3, 3), activation='relu'))
model.add(layers.MaxPooling2D((2, 2)))
model.add(layers.Conv2D(64, (3, 3), activation='relu'))
model.add(layers.Flatten())
model.add(layers.Dense(64, activation='relu'))
model.add(layers.Dense(10, activation='softmax'))

#Compile the model
model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

#Train the model
model.fit(x_train, y_train, epochs=5)

#Evaluate the model
model.evaluate(x_test, y_test)

#Save the model
model.save('mnist.h5')

#Load the model
model = tf.keras.models.load_model('mnist.h5')

#Predict the model
predictions = model.predict(x_test)

#Print the predictions
print(predictions[0])
print(np.argmax(predictions[0]))
print(y_test[0])

#Print the accuracy
print(model.evaluate(x_test, y_test))

#Render the history. Use loss and valiation loss to plot the loss
history = model.fit(x_train, y_train, epochs=5, validation_data=(x_test, y_test))
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('model loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(['train', 'test'], loc='upper left')
plt.show()

