import json
from sendgrid import SendGridAPIClient
from sendgrid.helpers.mail import Mail, Email, To
import os
import boto3
import json
from datetime import datetime

# session = boto3.session.Session(profile_name='demo')
# client = session.client('secretsmanager')
client = boto3.client('secretsmanager')

def get_latest_secret_with_prefix(prefix):
    try:
        # List secrets
        response = client.list_secrets()

        secrets = response.get('SecretList', [])

        if not secrets:
            print("No secrets found.")
            return None

        # Filter secrets by prefix
        filtered_secrets = [s for s in secrets if s['Name'].startswith(prefix)]

        if not filtered_secrets:
            print(f"No secrets found with prefix '{prefix}'.")
            return None

        # Sort filtered secrets by CreatedDate in descending order to get the latest one
        latest_secret = max(filtered_secrets, key=lambda s: s.get('CreatedDate', datetime.min))

        # Extract the secret's details
        latest_secret_name = latest_secret['Name']
        latest_secret_creation_date = latest_secret['CreatedDate']

       # Retrieve the secret's value
        secret_value_response = client.get_secret_value(SecretId=latest_secret_name)
        secret_value = secret_value_response.get('SecretString', 'No secret string available')
        data = json.loads(secret_value)
        key = data["api_key"]

        print(f"Secret value: {secret_value}")
        print(f"key: {key}")
        return key

    except Exception as e:
        print(f"An error occurred: {e}")
        return None


def lambda_handler(event, context):
    try:
        # get sns
        sns_message = event['Records'][0]['Sns']['Message']
        parsed_message = json.loads(sns_message)

        # get info
        to_email = parsed_message['email']
        token = parsed_message['verificationToken']

        # send
        result = send_verification_email(to_email, token)
        return {
            "statusCode": 200,
            "body": json.dumps({"success": result})
        }

    except Exception as e:
        print(f"Error: {e}")
        return {
            "statusCode": 500,
            "body": json.dumps({"error": str(e)})
        }

def send_verification_email(to_email, token):
    # verification_link = f"https://dev.yingtongcsye6225.me/v1/user/verify?token={token}"
    base_url = os.environ.get("BASE_URL", "localhost:8080")
    verification_link = f"http://{base_url}/v1/user/verify?token={token}"
    # verification_link = f"http://localhost:8080/v1/user/verify?token={token}"
    sender_email = "noreply@em6962.yingtongcsye6225.me"
    subject = "Verify Your Email Address"
    content = f"""Please verify your email by clicking the following link:
                {verification_link}

                This link will expire in 2 minutes.
                """
    
    try:
        message = Mail(
            from_email=Email(sender_email),
            to_emails=To(to_email),
            subject=subject,
            plain_text_content=content
        )
        sg = SendGridAPIClient(get_latest_secret_with_prefix("email-service-credentials-"))
        response = sg.send(message)
        print(f"Email sent! Status Code: {response.status_code}")
        return response.status_code == 202
    except Exception as e:
        print(f"Error sending email: {e}")
        return False
