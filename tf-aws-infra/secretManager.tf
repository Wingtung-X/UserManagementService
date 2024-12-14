# password for rds
# resource "random_password" "password" {
#   length           = 16
#   special          = true
#   override_special = "!#$%&*()-_=+[]{}<>:?"
# }


resource "random_password" "uppercase" {
  length  = 1
  special = false
  upper   = true
  lower   = false
  numeric = false
}


resource "random_password" "lowercase" {
  length  = 1
  special = false
  upper   = false
  lower   = true
  numeric = false
}


resource "random_password" "digit" {
  length  = 1
  special = false
  upper   = false
  lower   = false
  numeric = true
}


resource "random_password" "rest" {
  length           = 13
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}


locals {
  rds_password = "${random_password.uppercase.result}${random_password.lowercase.result}${random_password.digit.result}${random_password.rest.result}"
}


resource "aws_secretsmanager_secret" "rds_password" {
  name       = "rds-db-password-${uuid()}"
  kms_key_id = aws_kms_key.rds_key.id

}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/secretsmanager_secret_version
resource "aws_secretsmanager_secret_version" "rds_password_version" {
  secret_id = aws_secretsmanager_secret.rds_password.id
  secret_string = jsonencode({
    username = "csye6225"
    password = local.rds_password
  })
}


resource "aws_secretsmanager_secret" "email_service_credentials" {
  name       = "email-service-credentials-${uuid()}"
  kms_key_id = aws_kms_key.email_service_key.id

  tags = {
    Service = "Email"
    Name    = "email-service-credentials"
  }
}

resource "aws_secretsmanager_secret_version" "email_service_credentials_version" {
  secret_id = aws_secretsmanager_secret.email_service_credentials.id
  secret_string = jsonencode({
    api_key = "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"
  })
}


