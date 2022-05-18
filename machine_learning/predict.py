import os
import sys
import numpy as np

from tensorflow import keras
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
#from tensorflow.keras.preprocessing.image import img_to_array, load_img

#FILENAME_PREFIX = __file__[:__file__.rindex("/")+1]
FILENAME_PREFIX = "C:\\Users\\camak\\Documents\\Projects\\University\\Team-12-Project-Test\\machine_learning"
IMG_FILENAME_PREFIX = "C:\\Users\\camak\\Documents\\Projects\\University\\Team-12-Project-Test\\backend"

# Get fruit categories based on training dir subdirectories
def get_categories():
	# Training dataset dir
	train_dir = FILENAME_PREFIX+"/fruit_dataset/train/train"
	Name=[]
	for file in os.listdir(train_dir):
		Name+=[file]
	# Create map    
	#fruit_map = dict(zip(Name, [t for t in range(len(Name))]))
	#print(fruit_map)
	r_fruit_map = dict(zip([t for t in range(len(Name))],Name))
	return r_fruit_map
	
# Mapper function
def category_mapper(value):
	global r_fruit_map
	return r_fruit_map[value]

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
	prediction = model.predict(prediction_image)
	value = np.argmax(prediction)
	move_name = category_mapper(value)
	return move_name



#print("Running Fruit Watch Predictor..")
# Load trained model
model = keras.models.load_model(FILENAME_PREFIX+'/fruit_model.h5')

r_fruit_map = get_categories()

#test_image = FILENAME_PREFIX+'/fruit_dataset/test/test/0030.jpg'
img_path = IMG_FILENAME_PREFIX+sys.argv[1]

prediction_image = load_image(img_path)

print(predict(prediction_image))


