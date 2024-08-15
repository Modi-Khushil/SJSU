import React, { useState } from 'react';
import Title from './Title';
import axios from 'axios';
import { motion } from 'framer-motion';

const EmotionDetection = () => {

    const [file, setFile] = useState(null);
    const [error, setError] = useState(null);

    const [detectedEmotion, setDetectedEmotion] = useState(null);
    const [loading, setLoading] = useState(false);

    const types = ['image/png', 'image/jpeg'];
    
    const emotionDetectionAPI = "/emotion-detection";

    const handleChange = (e) => {

        let selected = e.target.files[0];

        if (selected && types.includes(selected.type)) {
            setFile(selected);
            setError('');

            let formData = new FormData();
            formData.append('file', e.target.files[0]);
            formData.append('email', localStorage.getItem('email'));
            formData.append('token', localStorage.getItem('user-token'));

            setLoading(true);

            axios.post(emotionDetectionAPI, formData)
            .then((response) => {

                if(response.status === 200){
                    const data = response.data['emotion detected'];
                    console.log(data);
                    setDetectedEmotion(data[0]);
                    setFile(null);
                    setLoading(false);
                }
            });

        } else {
            setFile(null);
            setError('Please select an image file (png or jpg)');
        }
    };

    return (
        <div className="App">
            <Title title="Upload a Person's Image that you want to detect the emotion"/>
            <form className='uploadImgForm'>
                <label className='uploadImgLabel'>
                    <input type="file" onChange={handleChange}/>
                    <span>+</span>
                </label>
                
            </form>
            <div className="output">
                { error && <div className="error">{ error }</div>}
                { file && <div style = {{textAlign:"center"}}>{ file.name }</div> }
                <br />
                {detectedEmotion && <div style = {{textAlign:"center", marginTop:"40px"}}><h4><b>Detected Emotion:</b></h4></div>}
                {detectedEmotion && <div style = {{textAlign:"center"}}><h4>{detectedEmotion}</h4></div>}
            </div>
            {loading && <motion.div className="loader-container" initial={{ opacity: 0 }} animate={{ opacity: 3 }}>
                    <motion.div className="spinner"></motion.div>
            </motion.div>}
        </div>
      );
}

export default EmotionDetection;