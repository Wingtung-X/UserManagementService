resource "aws_db_instance" "csye6225_rds_instance" {
  engine                 = "postgres"
  engine_version         = "14.9"
  instance_class         = "db.t3.micro"
  multi_az               = false
  identifier             = "csye6225"
  username               = "csye6225"
  password               = local.rds_password
  db_subnet_group_name   = aws_db_subnet_group.csye6225_db_subnet_group.name
  publicly_accessible    = false
  db_name                = "csye6225"
  parameter_group_name   = aws_db_parameter_group.postgres_parameter_group.name
  vpc_security_group_ids = [aws_security_group.db_security_group.id]
  allocated_storage      = 20
  skip_final_snapshot    = true
  storage_encrypted      = true
  kms_key_id             = aws_kms_key.rds_key.arn
}


# to fix  creating RDS DB Instance (csye6225): operation error RDS: CreateDBInstance, https response error StatusCode: 404
# may need a subnet group
resource "aws_db_subnet_group" "csye6225_db_subnet_group" {
  name       = "csye6225-db-subnet-group"
  subnet_ids = aws_subnet.private_subnets[*].id

  tags = {
    Name = "csye6225-db-subnet-group"
  }
}