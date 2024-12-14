resource "aws_lambda_function" "verification_lambda" {
  function_name = "verification_lambda"
  runtime       = "python3.12"
  handler       = "lambdafunction.lambda_handler"
  filename      = var.zip_file_path
  role          = aws_iam_role.lambda_execution_role.arn

  environment {
    variables = {
      #     SENDGRID_API_KEY = var.sendgrid_key
      BASE_URL = var.subdomain_name
    }
  }
}


resource "aws_sns_topic_subscription" "lambda_subscription" {
  topic_arn = aws_sns_topic.email_topic.arn
  protocol  = "lambda"
  endpoint  = aws_lambda_function.verification_lambda.arn
}

resource "aws_lambda_permission" "allow_sns_invoke" {
  function_name = aws_lambda_function.verification_lambda.function_name
  action        = "lambda:InvokeFunction"
  principal     = "sns.amazonaws.com"
  source_arn    = aws_sns_topic.email_topic.arn
}