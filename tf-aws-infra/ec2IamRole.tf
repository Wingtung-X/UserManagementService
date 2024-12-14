resource "aws_iam_role" "ec2_role" {
  name               = "ec2_s3_cloudwatch_access_role"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json
}

data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "s3_cloudwatch_access_policy" {
  name        = "ec2_s3_cloudwatch_access_policy"
  description = "Allows EC2 instance full access to S3, CloudWatch, and CloudWatch Logs"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      # s3 access
      {
        Effect = "Allow",
        Action = [
          "s3:*"
        ],
        Resource = [
          "${aws_s3_bucket.my_bucket.arn}",
          "${aws_s3_bucket.my_bucket.arn}/*"
        ]
      },
      # CloudWatch, CloudWatch Logs access
      {
        Effect = "Allow",
        Action = [
          "cloudwatch:*",
          "logs:*"
        ],
        Resource = "*"
      },
      # SNS publish access
      {
        Effect = "Allow",
        Action = [
          "sns:Publish"
        ],
        Resource = "${aws_sns_topic.email_topic.arn}"
      },
      # Secrets Manager access
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:ListSecrets"
        ],
        Resource = "*"
      },
      # KMS decryption for Secrets Manager
      {
        Effect = "Allow",
        Action = [
          "kms:Encrypt",
          "kms:Decrypt",
          "kms:ReEncrypt*",
          "kms:GenerateDataKey*",
          "kms:DescribeKey"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ec2_role_policy_attachment" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = aws_iam_policy.s3_cloudwatch_access_policy.arn
}

resource "aws_iam_role_policy_attachment" "cloudwatch_agent_policy_attachment" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}