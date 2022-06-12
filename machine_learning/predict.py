import os
import sys
import numpy as np

from tensorflow import keras

# Supress warning logs from tensorflow. Use version 2.8.0
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

# In the current implementation and for Golang to execute this script
# prefix to the machine learning and backend folder are required.
FILENAME_PREFIX = "/home/ubuntu/team-12-project/machine_learning"
IMG_FILENAME_PREFIX = "/home/ubuntu/team-12-project/backend"

# Fruit classifications
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
model = keras.models.load_model(FILENAME_PREFIX+'/fruit_model_30.h5')

# test_image = 'corn.jpg'
# The uploaded image's location is passed as an argument to this script.
img_path = IMG_FILENAME_PREFIX+sys.argv[1]

prediction_image = load_image(img_path)

# Print predicted fruit name to console which is then used by Golang.
print(predict(prediction_image))
