packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0, <2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "us-west-2"
}

variable "source_ami" {
  type    = string
  default = "ami-04dd23e62ed049936"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = "subnet-095a6bfeba592ef50"
}

source "amazon-ebs" "my-ami" {
  region          = "${var.aws_region}"
  ami_name        = "csye6225-webapp-local-test-${formatdate("YYYYMMDD-HHMMss", timestamp())}"
  ami_description = "AMI for CSYE 6225 local test"

  ami_regions = [
    "us-west-2",
  ]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  instance_type = "t2.small"
  source_ami    = "${var.source_ami}"
  ssh_username  = "${var.ssh_username}"
  subnet_id     = "${var.subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = "8"
    volume_type           = "gp3"
  }
}


build {
  sources = ["source.amazon-ebs.my-ami"]

  provisioner "file" {
    source      = "../scripts/webapp.service"
    destination = "/tmp/webapp.service"
  }

  provisioner "shell" {
    inline = [
      "echo 'Current directory:'",
      "pwd",
      "echo 'Checking contents of /tmp:'",
      "ls -l /tmp",
    ]
  }


  provisioner "shell" {
    inline = [
      "echo 'Current directory:'",
      "pwd",
      "echo 'Checking contents of /tmp:'",
      "ls -l /tmp",
    ]
  }

  provisioner "file" {
    source      = "../target/CloudComputing-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/CloudComputing-0.0.1-SNAPSHOT.jar"
  }

  provisioner "file" {
    source      = "../src/main/resources/application.properties"
    destination = "/tmp/application.properties"
  }

  provisioner "shell" {
    inline = [
      "echo 'Current directory:'",
      "pwd",
      "echo 'Checking contents of /tmp:'",
      "ls -l /tmp",
    ]
  }

  provisioner "file" {
    source      = "../scripts/amazon-cloudwatch-agent.json"
    destination = "/tmp/amazon-cloudwatch-agent.json"
  }


  provisioner "file" {
    source      = "../scripts/setup.sh"
    destination = "/tmp/setup.sh"
  }

  provisioner "shell" {
    inline = [
      "echo 'Current directory:'",
      "pwd",
      "echo 'Checking contents of /tmp:'",
      "ls -l /tmp",
    ]
  }


  provisioner "shell" {
    inline = [
      "chmod +x /tmp/setup.sh",
      "ls -l /tmp/CloudComputing-0.0.1-SNAPSHOT.jar",
      "/tmp/setup.sh"
    ]
  }
}