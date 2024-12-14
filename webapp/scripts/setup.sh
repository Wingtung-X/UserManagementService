#!/bin/bash

# status check
echo "Current directory:"
pwd

# uodate and upgrade
sudo apt update
sudo apt upgrade -y

# create croup
sudo groupadd -f csye6225 || echo "Group creation failed!"

# create user put into group
sudo useradd -M -s /usr/sbin/nologin -g csye6225 csye6225 || echo "User creation failed!"
id csye6225 || echo "User csye6225 still does not exist."

#  make opt dir
sudo mkdir -p /opt/app || { echo "Failed to create /opt/app directory!"; exit 1; }
sudo chown -R csye6225:csye6225 /opt/app || { echo "Failed to change ownership of /opt/app!"; exit 1; }
sudo chmod 755 /opt/app
sudo mkdir -p /opt/app/logs ||  { echo "Failed to create /opt/app/logs directory!"; exit 1; }
sudo chmod 755 /opt/app/logs


# move file
sudo mv /tmp/webapp.service /etc/systemd/system/webapp.service || { echo "Failed to move webapp.service!"; exit 1; }
sudo chmod 644 /etc/systemd/system/webapp.service
sudo mv /tmp/CloudComputing-0.0.1-SNAPSHOT.jar /opt/app/CloudComputing-0.0.1-SNAPSHOT.jar || { echo "Failed to move JAR file!"; exit 1; }
sudo mv /tmp/application.properties /opt/app/application.properties || { echo "Failed to move application.properties!"; exit 1; }
sudo mv /tmp/setup.sh /opt/app/setup.sh || { echo "Failed to move setup.sh!"; exit 1; }
#for prod
sudo mv /tmp/amazon-cloudwatch-agent.json /opt/app/amazon-cloudwatch-agent.json || { echo "Failed to move cloudwatch agent!"; exit 1; }
#for local
#sudo mv /tmp/cw-local.json /opt/app/cw-local.json || { echo "Failed to move cloudwatch agent!"; exit 1; }


# 查看 /opt/app 目录的内容
echo "Checking contents of /opt/app:"
ls -l /opt/app  # 列出 /opt/app 目录的详细内容

# 查看 /etc/systemd/system 中的 webapp.service
echo "Checking contents of /etc/systemd/system for webapp.service:"
ls -l /etc/systemd/system/webapp.service


# install Java and Maven
sudo apt install -y openjdk-17-jdk maven

# install cloudwatch agent
sudo apt update
sudo apt install unzip -y
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
curl https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb -o amazon-cloudwatch-agent.deb
sudo dpkg -i -E ./amazon-cloudwatch-agent.deb


# 确保文件和目录路径正确
sudo chown -R csye6225:csye6225 /opt/app/CloudComputing-0.0.1-SNAPSHOT.jar
sudo chown -R csye6225:csye6225 /opt/app/application.properties
sudo chown -R csye6225:csye6225 /opt/app/setup.sh
sudo chown -R csye6225:csye6225 /opt/app/amazon-cloudwatch-agent.json


#stast cloud watch agent
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -c file:/opt/app/amazon-cloudwatch-agent.json -s
#for local test
#sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
#  -a fetch-config -m ec2 -c file:/opt/app/cw-local.json -s


# start service
sudo systemctl daemon-reload
sudo systemctl enable webapp.service
sudo systemctl start webapp.service
sudo systemctl enable amazon-cloudwatch-agent

