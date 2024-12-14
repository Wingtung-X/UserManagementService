resource "aws_security_group" "db_security_group" {
  name        = "db_security_group"
  description = "Security group for RDS database"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port = 5432
    to_port   = 5432
    protocol  = "tcp"
    # The source of the traffic should be the application security group.
    security_groups = [aws_security_group.application_security_group.id]
  }

  # Restrict access to the instance from the internet.
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
