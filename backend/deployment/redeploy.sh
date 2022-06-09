#! /bin/bash

# ensure we are in the correct branch
echo "Checking out main branch"
git checkout main

# pull latest
echo "Pulling latest.."
git pull

# restart appgo service
echo "Restarting the appgo service.."
sudo service appgo restart 

echo "Redeploy completed."
