# Convolutional Neural Network for Fruit Classification

What is a CNN? https://www.ibm.com/cloud/learn/convolutional-neural-networks

This directory contains the files for the convolutional neural network to train our fruit classification model (`cnn.ipynb`), training datasets, a pre-trained model (`fruit_model.h5`) and a test prediction script (`predict.py`).

## Dataset

The following dataset was used to train this CNN: https://www.kaggle.com/sshikamaru/fruit-recognition

It contains 33 different classes of fruits and a total of 22495 images.

Download dataset above and put the contents of `archive.zip/train`and `archive.zip/test` inside the `fruits_dataset` folder.

## Running the CNN and training the model

1. Whilst not required, I recommend installing Anaconda Distribution [https://www.anaconda.com/products/distribution] to manage your python environments. Otherwise you can try and run the the notebook locally without Anaconda or using Google Colab.

2. Once Anaconda is installed, create a new Anaconda environment. See https://docs.conda.io/projects/conda/en/latest/user-guide/tasks/manage-environments.html

`conda create -n myenv python=3.7`

The python version can optionally be specified.

3. Install Jupyter Notebook in your Anaconda environment and open the notebook `cnn.ipynb`

Tested on Jupyter Notebook and Anaconda Environment using the following dependency versions:

- Python: 3.7.13
- Tensorflow: 2.3.0 (Keras comes with Tensorflow so no need to install it separately)
- Matplotlib: 3.5.1
- Pandas: 1.3.5
- Seaborn: 0.1.1.2

## Making predictions

`predictions.py` is a script to make predictions using the trained model. It loads the saved trained model from the CNN and using a test image, it will run it through the model and print out a prediction (i.e what kind of fruit it thinks it is)

1. In your Anaconda environment, install CMD.exe Prompt. This allows you to execute python scripts from your Anaconda environment.

2. Run python `predictions.py` to make a prediciton (test image can be changed within the script)
