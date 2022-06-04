#! /bin/bash

# ensure we are in the correct branch
git checkout working-with-ubuntu

# pull latest
git pull

# restart appgo service
sudo service appgo 