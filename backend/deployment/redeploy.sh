#! /bin/bash

# ensure we are in the correct branch
git checkout working-on-ubuntu

# pull latest
git pull

# restart appgo service
sudo service appgo restart 
