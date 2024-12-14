#!/bin/bash

sudo apt update
sudo apt upgrade -y

sudo apt install -y jq


# echo "Fetching database password from Secrets Manager..."
# # DB_SECRET=$(aws secretsmanager get-secret-value --secret-id "rds-db-password" --query SecretString --output text)
# # DB_PASSWORD=$(echo "$DB_SECRET" | jq -r '.password')
# LATEST_SECRET=$(aws secretsmanager list-secrets \
#     --query "SecretList[?starts_with(Name, 'rds-db-password')].Name | sort(@) | [-1]" \
#     --output text)

# # DB_SECRET=$(aws secretsmanager get-secret-value --secret-id "$LATEST_SECRET" --query SecretString --output text)
# # DB_PASSWORD=$(echo "$DB_SECRET" | jq -r '.password')
# DB_PASSWORD="$${DB_PASSWORD:-placeholder}"

# # Fetch dynamic database password from Secrets Manager
# echo "Fetching database password from Secrets Manager..."
# LATEST_SECRET=$(aws secretsmanager list-secrets \
#     --query "SecretList[?starts_with(Name, 'rds-db-password')].Name | sort(@) | [-1]" \
#     --output text)

# DB_SECRET=$(aws secretsmanager get-secret-value --secret-id "$LATEST_SECRET" --query SecretString --output text)
# DYNAMIC_DB_PASSWORD=$(echo "$DB_SECRET" | jq -r '.password')

# # Use dynamic password if retrieved successfully, otherwise fallback to placeholder
# if [[ -n "$DYNAMIC_DB_PASSWORD" ]]; then
#     DB_PASSWORD="$DYNAMIC_DB_PASSWORD"
#     echo "Dynamic password retrieved: $DB_PASSWORD"
# else
#     echo "Failed to retrieve dynamic DB password, using placeholder."
#     DB_PASSWORD="no-pass-word-passed"
# fi


echo "DB_PASSWORD from user_data: ${DB_PASSWORD}"
DB_PASSWORD=${DB_PASSWORD}
if [[ "$DB_PASSWORD" == "default-placeholder" ]]; then
    echo "Default placeholder detected, retrieving dynamic password..."
    LATEST_SECRET=$(aws secretsmanager list-secrets \
        --query "SecretList[?starts_with(Name, 'rds-db-password')].Name | sort(@) | [-1]" \
        --output text)
    
    if [[ -n "$LATEST_SECRET" ]]; then
        DB_SECRET=$(aws secretsmanager get-secret-value --secret-id "$LATEST_SECRET" --query SecretString --output text)
        DYNAMIC_DB_PASSWORD=$(echo "$DB_SECRET" | jq -r '.password')
        
        if [[ -n "$DYNAMIC_DB_PASSWORD" ]]; then
            DB_PASSWORD="$DYNAMIC_DB_PASSWORD"
            echo "Dynamic password retrieved: $DB_PASSWORD"
        else
            echo "Failed to retrieve dynamic password. Using fallback."
            DB_PASSWORD="no-pass-word-passed"
        fi
    else
        echo "Failed to find latest secret. Using fallback."
        DB_PASSWORD="no-pass-word-passed"
    fi
else
    echo "Using provided DB_PASSWORD: $DB_PASSWORD"
fi

echo "Final DB_PASSWORD: $DB_PASSWORD"

# get user data
DB_HOSTNAME="${DB_HOSTNAME}"
DB_PORT="${DB_PORT}"
DB_NAME="${DB_NAME}"
DB_USER="${DB_USER}"
S3_BUCKET="${S3_BUCKET}"

# rewrite user data
echo "Rewriting db conection"
cat <<EOT > /opt/app/application.properties
          spring.application.name=CloudComputing
                
          # Data source
          spring.datasource.url=jdbc:postgresql://${DB_HOSTNAME}/${DB_NAME}
          spring.datasource.username=${DB_USER}
          spring.datasource.password=$DB_PASSWORD
                
          # JPA config
          spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
          spring.jpa.hibernate.ddl-auto=update

          # S3 config
          aws.s3.bucket.name=${S3_BUCKET}

          # logs path
          logging.file.path=/opt/app/logs
          logging.file.name=csye6225.log

          #aws
          cloud.aws.credentials.profile=dev
          cloud.aws.region.static=us-west-2

          spring.servlet.multipart.enabled=true
          spring.servlet.multipart.max-file-size=10MB
          spring.servlet.multipart.max-request-size=10MB

          management.metrics.export.cloudwatch.namespace=CloudWatchNotTest
          management.metrics.export.cloudwatch.step=5s

          logging.level.io.micrometer=DEBUG
          logging.level.org.springframework.cloud=DEBUG

          # cw
          #CLOUDWATCH_NAMESPACE=Test6
          #METRICS_ENABLED=true
          management.metrics.enable.jvm=false
          management.metrics.enable.system=false
          management.metrics.enable.logback=false
          management.metrics.enable.spring=false
          management.metrics.enable.process=false
          management.metrics.enable.hikaricp=false

          #domain for verification email
          aws.sns.topicArn=${SNS_TOPIC}

EOT

sudo systemctl daemon-reload
sudo systemctl restart webapp.service

# cloud watch agent setup
# changed to local json for testing  remember to change that back when move to demo
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/app/amazon-cloudwatch-agent.json -s
