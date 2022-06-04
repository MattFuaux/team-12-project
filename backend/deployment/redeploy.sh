#! /bin/bash

# ensure we are in the correct branch
echo "Checking out working-on-ubuntu-branch.."
git checkout working-on-ubuntu

# pull latest
echo "Pulling latest.."
git pull

# restart appgo service
echo "Restarting the appgo service.."
sudo service appgo restart 

echo "Redeploy completed."
