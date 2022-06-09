# Team 12 FruitWatch Project

## Folder Structure

- `backend` folder contains implementation of the backend API server written in Golang.
- `frontend` folder contains the Android frontend application code.
- `machine_learning` folder contains the neural network training notebook used, pre-trained fruit prediction model and information about the datasets.
- `scraper` folder contains scraping script written in Python to scrape fruit prices (Currently only scrapes Coles).

## Server Setup

The FruitWatch API server is hosted in AWS and is running on an EC2 `t2.medium` instance using an Ubuntu `20.04 LTS` image.

### EC2 instance setup

- Allow ingress and egress http/https traffic by creating a security group and applying it to the instance.
- Create a new SSH key pair and add to GitHub account.

### FruitWatch application setup

#### Install dependancies

- Install Golang `v1.18.3`
- Install MySQL server `v8.0.29-0ubuntu0.20.04.3`
- Install pip: `sudo apt install python3-pip`
- Install numpy: `pip install numpy`
- Install tensorflow v2.8.0: `pip install tensorflow==2.8.0`

#### Configure database

- Load FruitWatch database schema in MySQL located in: `team-12-project/backend/static/schema/fruitwatchdb.sql`
- Update MySQL connection details in `team-12-project/backend/.env`

#### Update application paths

- Update paths in the `team-12-project/machine-learning/predict.py` script
- Update paths in `team-12-project/backend/controllers/controllers.go` line `261`

#### Environment variable(s)

- Set environment variable (required for tensorflow): `export PROTOCOL_BUFFERS_PYTHON_IMPLEMENTATION=python`

#### Create systemd service

- Create systemd service to run the go app in the background: `sudo nano etc/systemd/system/appgo.service`

Add the following to `appgo.service`

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

##### Commands to start/stop and check status of service

```
sudo service appgo start
sudo service appgo status
sudo service appgo stop
```

## Re-deploying service

Redeployment script is located in: `team-12-project/backend/deployment/redeploy.sh`

The script runs the following steps:

1. Checks out the `main` branch and pulls latest.
2. Restarts the `appgo` service.
