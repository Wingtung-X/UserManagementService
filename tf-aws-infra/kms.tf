# for ec2
resource "aws_kms_key" "ec2_key" {
  description             = "KMS key for EC2 encryption"
  enable_key_rotation     = true
  rotation_period_in_days = 90
  tags = {
    Name = "ec2-key"
  }
}

resource "aws_kms_alias" "ec2_key_alias" {
  name          = "alias/ec2-key"
  target_key_id = aws_kms_key.ec2_key.id
}

# for rds
resource "aws_kms_key" "rds_key" {
  description             = "KMS key for RDS encryption"
  enable_key_rotation     = true
  rotation_period_in_days = 90
  tags = {
    Name = "rds-key"
  }
}

resource "aws_kms_alias" "rds_key_alias" {
  name          = "alias/rds-key"
  target_key_id = aws_kms_key.rds_key.id
}

# for s3
resource "aws_kms_key" "s3_key" {
  description             = "KMS key for S3 encryption"
  enable_key_rotation     = true
  rotation_period_in_days = 90
  tags = {
    Name = "s3-key"
  }
}

resource "aws_kms_alias" "s3_key_alias" {
  name          = "alias/s3-key"
  target_key_id = aws_kms_key.s3_key.id
}

# for secret manager
resource "aws_kms_key" "secrets_manager_key" {
  description             = "KMS key for Secrets Manager encryption"
  enable_key_rotation     = true
  rotation_period_in_days = 90
  tags = {
    Name = "secrets-manager-key"
  }
}

resource "aws_kms_alias" "secrets_manager_key_alias" {
  name          = "alias/secrets-manager-key"
  target_key_id = aws_kms_key.secrets_manager_key.id
}

# for email
resource "aws_kms_key" "email_service_key" {
  description             = "KMS key for Email Service Credentials"
  enable_key_rotation     = true
  rotation_period_in_days = 90

  tags = {
    Name = "email-service-key"
  }
}

resource "aws_kms_alias" "email_service_key_alias" {
  name          = "alias/email-service-key"
  target_key_id = aws_kms_key.email_service_key.id
}

data "aws_caller_identity" "current" {}


resource "aws_kms_key_policy" "ec2_key_policy" {
  key_id = aws_kms_key.ec2_key.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          AWS = [
            "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root",
            "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/aws-service-role/autoscaling.amazonaws.com/AWSServiceRoleForAutoScaling"
          ]
        }
        Action   = "kms:*"
        Resource = "${aws_kms_key.ec2_key.arn}"
      },
      {
        Effect = "Allow"
        Principal = {
          AWS = "${aws_iam_role.ec2_role.arn}"
        }
        Action = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:DescribeKey"
        ]
        Resource = "${aws_kms_key.ec2_key.arn}"
      }
    ]
  })
}
