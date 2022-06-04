# Team 12 FruitWatch Project

- `backend` folder contains implementation of the API server in Golang
- `frontend` folder contains the Android frontend application
- `machine_learning` folder contains the neural network training notebook, pre-trained model as well as a link to the dataset used


# Server Setup Steps (AWS EC2 Ubuntu)
- Install Golang 
- Install MySQL server and load database schema located in ```/backend/static/schema/fruitwatchdb.sql```
- Update ```/backend/.env``` file to the correct MySQL connection details
- Install pip: ```sudo apt install python3-pip```
- Install numpy: ```pip install numpy```
- Install tensorflow version 2.8.0: ```pip install tensorflow==2.8.0```
- Set environment variable (required for tensorflow): ```export PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION=python```
- Update paths in the ```/machine-learning/predict.py``` script
- Update paths in ```/backend/controllers/controllers.go``` line 261
- Created systemd service to run the go app in the background: ```sudo nano etc/systemd/system/appgo.service```

appgo.service
```
[Unit]
Description=FruitWatch Go Service
ConditionPathExists=/home/ubuntu/team-12-project/backend
After=network.target
[Service]
Type=simple
User=ubuntu
Group=ubuntu
WorkingDirectory=/home/ubuntu/team-12-project/backend
ExecStart=/usr/local/go/bin/go run .
Restart=on-failure
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=appgoservice
# Required for tensorflow to work
Environment=PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION=python
[Install]
WantedBy=multi-user.target
```
Commands to start/stop and check status of service
```
sudo service appgo start
sudo service appgo status
sudo service appgo stop
```

