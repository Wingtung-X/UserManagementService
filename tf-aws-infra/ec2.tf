# resource "aws_instance" "webapp_server" {
#   ami                    = var.ami_id
#   instance_type          = "t2.micro"
#   subnet_id              = aws_subnet.public_subnets[0].id
#   vpc_security_group_ids = [aws_security_group.application_security_group.id]

#   key_name = var.key_name

#   iam_instance_profile = aws_iam_instance_profile.ec2_instance_profile.name


#   user_data = templatefile("${path.module}/setup-userdata.sh", {
#     DB_HOSTNAME = aws_db_instance.csye6225_rds_instance.endpoint
#     DB_PORT     = "5432"
#     DB_NAME     = aws_db_instance.csye6225_rds_instance.db_name
#     DB_USER     = aws_db_instance.csye6225_rds_instance.username
#     DB_PASSWORD = aws_db_instance.csye6225_rds_instance.password
#     S3_BUCKET   = aws_s3_bucket.my_bucket.bucket
#   })

#   root_block_device {
#     volume_size = 25
#     volume_type = "gp2"
#   }

#   disable_api_termination = false

#   tags = {
#     Name = "WebappEC2Instance"
#   }
# }

# resource "aws_iam_instance_profile" "ec2_instance_profile" {
#   name = "ec2_instance_profile"
#   role = aws_iam_role.ec2_role.name
# }