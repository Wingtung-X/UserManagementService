resource "aws_launch_template" "webapp_launch_template" {
  name          = "csye6225_asg"
  image_id      = var.ami_id
  instance_type = "t2.micro"
  key_name      = var.key_name
  iam_instance_profile {
    name = aws_iam_instance_profile.ec2_instance_profile.name
  }

  block_device_mappings {
    device_name = "/dev/xvdb"
    ebs {
      encrypted   = true
      kms_key_id  = aws_kms_key.ec2_key.arn
      volume_size = 5
      volume_type = "gp3"
    }
  }

  network_interfaces {
    security_groups             = [aws_security_group.application_security_group.id]
    associate_public_ip_address = true
  }

  user_data = base64encode(templatefile("${path.module}/setup-userdata.sh", {
    DB_HOSTNAME = aws_db_instance.csye6225_rds_instance.endpoint
    DB_PORT     = "5432"
    DB_NAME     = aws_db_instance.csye6225_rds_instance.db_name
    DB_USER     = aws_db_instance.csye6225_rds_instance.username
    S3_BUCKET   = aws_s3_bucket.my_bucket.bucket
    SNS_TOPIC   = aws_sns_topic.email_topic.arn
    DB_PASSWORD = "default-placeholder"
  }))
}


resource "aws_iam_instance_profile" "ec2_instance_profile" {
  name = "ec2_instance_profile"
  role = aws_iam_role.ec2_role.name
}
