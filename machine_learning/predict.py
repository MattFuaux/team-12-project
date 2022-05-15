import os
import numpy as np
from tensorflow import keras
from tensorflow.keras.preprocessing.image import load_img, img_to_array

# Get fruit categories based on training dir subdirectories
def get_categories():
	# Training dataset dir
	train_dir = "fruits_dataset/train"
	Name=[]
	for file in os.listdir(train_dir):
		Name+=[file]
	# Create map    
	fruit_map = dict(zip(Name, [t for t in range(len(Name))]))
	# print(fruit_map)
	r_fruit_map = dict(zip([t for t in range(len(Name))],Name))
	return r_fruit_map
	
# Mapper function
def category_mapper(value):
    return r_fruit_map[value]

# Load image and convert size	
def load_image(image_path):
	image=load_img(image_path,target_size=(100,100))

	image=img_to_array(image) 
	image=image/255.0
	prediction_image=np.array(image)
	prediction_image= np.expand_dims(image, axis=0)

	return prediction_image	

# Predict image
def predict(prediction_image):
	prediction = model.predict(prediction_image)
	value = np.argmax(prediction)
	move_name = category_mapper(value)
	print("Prediction is {}.".format(move_name))

# Load trained model
model = keras.models.load_model('fruit_model.h5')

r_fruit_map = get_categories()

test_image = 'fruits_dataset/test/0030.jpg'
prediction_image = load_image(test_image)

predict(prediction_image)


