resource "aws_db_parameter_group" "postgres_parameter_group" {
  name   = "rds-pgsql"
  family = "postgres14"
}