# Convolutional Neural Network for Fruit Classification

Author: Marck Munoz

This directory contains the files for the convolutional neural network used to train our fruit classification model (`cnn.ipynb`), training datasets information, a pre-trained model we have trained ourselves (`fruit_model_30.h5`) and prediction script (`predict.py`) which is being utilized by the backend Golang code.

What is a CNN? https://www.ibm.com/cloud/learn/convolutional-neural-networks

## Dataset

- The following dataset was modified (i.e removed some categories and added more images per category) to train this CNN: https://www.kaggle.com/sshikamaru/fruit-recognition

- Our final pre-trained model consists of 30 different classfications:

```
fruitCategories = {
	'Apple': 0,
	'Apricot' : 1,
	'Avocado' : 2,
	'Banana' : 3,
	'Blueberry' : 4,
	'Cherry' : 5,
	'Coconut' : 6,
	'Corn' : 7,
	'Dragonfruit' : 8,
	'Durian' : 9,
	'Eggplant' : 10,
	'Grapes' : 11,
	'Kiwi' : 12,
	'Kumquats' : 13,
	'Lemon' : 14,
	'Limes' : 15,
	'Lychee' : 16,
	'Mandarin' : 17,
	'Mango' : 18,
	'Orange' : 19,
	'Papaya' : 20,
	'Passion Fruit' : 21,
	'Peach' : 22,
	'Pear' : 23,
	'Pineapple' : 24,
	'Pomegranate' : 25,
	'Raspberry' : 26,
	'Strawberry' : 27,
	'Tomato' : 28,
	'Watermelon' : 29,
	}
```

## Running the CNN and training the model

1. Place training and validation dataset inside a folder.

2. Whilst not required, I recommend installing Anaconda Distribution [https://www.anaconda.com/products/distribution] to manage your python environments. Otherwise you can try and run the the notebook locally without Anaconda or by using Google Colab.

3. Once Anaconda is installed, create a new Anaconda environment. See https://docs.conda.io/projects/conda/en/latest/user-guide/tasks/manage-environments.html

`conda create -n myenv python=3.7`

The python version can optionally be specified.

4. Install Jupyter Notebook in your Anaconda environment and open the notebook `cnn.ipynb`

Tested on Jupyter Notebook and Anaconda Environment using the following dependency versions:

- Python: `3.7.13`
- Tensorflow: `2.3.0` (Keras comes with Tensorflow so no need to install it separately)
- Matplotlib: `3.5.1`
- Pandas: `1.3.5`
- Seaborn: `0.1.1.2`

## Making predictions

`predictions.py` is a script to make predictions using the trained model. It loads the saved pre-trained model (`fruit_model.h5`) and then uses it to predict the fruit image passed in as an argument.

To test in Anacoda:

1. In your Anaconda environment, install CMD.exe Prompt. This allows you to execute python scripts from your Anaconda environment.

2. Run python `predictions.py` to make a prediciton (test image can be changed within the script)

Alternatively, you can also just run the script and pass in a path of the image as an argument:

```
./predictions.py ../machine_learning/corn.jpg
```
