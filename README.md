# Team 12 FruitWatch Project

- `backend` folder contains implementation of the API server in Golang
- `frontend` folder contains the Android frontend application
- `machine_learning` folder contains the neural network training notebook, pre-trained model as well as a link to the dataset used


# Deploying to Ubuntu
- Install MySQL server and load database schema located in ```/backend/static/schema/fruitwatchdb.sql```
- Update ```/backend/.env``` file to the correct MySQL connection details
- Install golang
- Install pip: ```sudo apt install python3-pip```
- Install numpy: ```pip install numpy```
- Install tensorflow version 2.8.0: ```pip install tensorflow==2.8.0```
- Update paths in the ```/machine-learning/predict.py``` script
- Update paths in ```/backend/controllers/controllers.go``` line 261
