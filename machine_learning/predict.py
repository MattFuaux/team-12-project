import os
import sys
import numpy as np

from tensorflow import keras

# Supress warning logs from tensorflow. Use version 2.8.0
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

# In the current implementation and for Golang to execute this script
# prefix to the machine learning and backend folder are required.
FILENAME_PREFIX = "/home/marck/Desktop/team-12-project/machine_learning"
IMG_FILENAME_PREFIX = "/home/marck/Desktop/team-12-project/backend"

fruitCategories = {
	'Apple Braeburn': 0, 
	'Apple Granny Smith': 1, 
	'Apricot': 2, 'Avocado': 3, 
	'Banana': 4, 
	'Blueberry': 5, 
	'Cactus fruit': 6, 
	'Cantaloupe': 7, 
	'Cherry': 8, 
	'Clementine': 9, 
	'Corn': 10, 
	'Cucumber Ripe': 11, 
	'Grape Blue': 12, 
	'Kiwi': 13, 
	'Lemon': 14, 
	'Limes': 15, 
	'Mango': 16, 
	'Onion White': 17, 
	'Orange': 18, 
	'Papaya': 19, 
	'Passion Fruit': 20, 
	'Peach': 21, 
	'Pear': 22, 
	'Pepper Green': 23, 
	'Pepper Red': 24, 
	'Pineapple': 25, 
	'Plum': 26, 
	'Pomegranate': 27, 
	'Potato Red': 28, 
	'Raspberry': 29, 
	'Strawberry': 30, 
	'Tomato': 31, 
	'Watermelon': 32
	}

# Load image and convert size	
def load_image(image_path):
	image=keras.preprocessing.image.load_img(image_path,target_size=(100,100))

	image=keras.preprocessing.image.img_to_array(image) 
	image=image/255.0
	prediction_image=np.array(image)
	prediction_image= np.expand_dims(image, axis=0)

	return prediction_image	

# Predict image
def predict(prediction_image):
	predictionVal = model.predict(prediction_image)
	value = np.argmax(predictionVal)
	predictedFruit = ""
	# Loop through fruit category to find the right key
	for fruit, val in fruitCategories.items():
		if val == value:
			predictedFruit = fruit

	return predictedFruit	


# Load trained model
model = keras.models.load_model(FILENAME_PREFIX+'/fruit_model.h5')

# test_image = 'corn.jpg'
# The uploaded image's location is passed as an argument to this script.
img_path = IMG_FILENAME_PREFIX+sys.argv[1]

prediction_image = load_image(img_path)

# Print predicted fruit name to console which is then used by Golang.
print(predict(prediction_image))
