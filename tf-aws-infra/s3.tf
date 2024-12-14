resource "random_uuid" "bucket_name" {}

resource "aws_s3_bucket" "my_bucket" {
  bucket = random_uuid.bucket_name.result
  # delete bucket even not empty
  # CLI command : aws s3 rm s3://bucket-name --recursive
  force_destroy = true

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        kms_master_key_id = aws_kms_key.s3_key.arn
        sse_algorithm     = "aws:kms"
      }
    }
  }
}

resource "aws_s3_bucket_ownership_controls" "example" {
  bucket = aws_s3_bucket.my_bucket.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}


resource "aws_s3_bucket_acl" "my_bucket_acl" {
  depends_on = [aws_s3_bucket_ownership_controls.example]
  bucket     = aws_s3_bucket.my_bucket.id
  acl        = "private"
}


resource "aws_s3_bucket_server_side_encryption_configuration" "my_bucket_encryption" {
  bucket = aws_s3_bucket.my_bucket.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}


# lifecycle policy
# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_lifecycle_configuration
resource "aws_s3_bucket_lifecycle_configuration" "my_bucket_liftcycle" {
  bucket = aws_s3_bucket.my_bucket.id

  rule {
    id = "transition-to-ia"

    status = "Enabled"

    transition {
      days          = 30
      storage_class = "STANDARD_IA"
    }
  }
}


resource "aws_s3_object" "test_object" {
  bucket  = aws_s3_bucket.my_bucket.id
  key     = "test-file.txt"
  content = "This is a test file for verifying S3 permissions"
}