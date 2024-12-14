# tf-aws-infra

## Instruction

This is a terraform project to create vpc, create 3 public subnets and 3 private subnets. Create a internet gateway and route tables for both private and public subnets.

## Prerequisites
-[Terraform 1.9.7] (https://developer.hashicorp.com/terraform/install)

## Clone this project
Use SSH to clone this project

```
    git clone github-repo-SSH-link
```

## Run this project
1. Setup AWS CLI
```
    curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
    sudo installer -pkg AWSCLIV2.pkg -target /

    which aws
    aws --version
```

2. Set up account
```
    aws configure --profile=<NAME>
    example: 
    for dev -> aws configure --profile=demo
```

3. Set up credentials
   aws Acess Key ID and AWS Secret Access Key

4. Run
```
    terraform init  
    AWS_PROFILE=demo terraform plan (Can replace AWS_PROFILE=demo to other users like demo as long as it's configured)
    AWS_PROFILE=demo terraform apply  
```
5. Destroy
```
    AWS_PROFILE=demo terraform destroy  
```
6. Replace variable (with .tfvar file in proj doc)
```
    AWS_PROFILE=demo terraform init -var="region=us-west-2"
    AWS_PROFILE=demo terraform plan -var="region=us-west-2"
    AWS_PROFILE=demo terraform apply -var="region=us-west-2"
    AWS_PROFILE=demo terraform destroy -var="region=us-west-2"
```

7. Replace variable (without .tfvar file in proj doc, new .tfvar file is needed)
    1. new file must contains:
   ```
    vp_cidr = "10.0.0.0/16"

    private_subnet_cidrs = ["10.0.4.0/24", "10.0.5.0/24", "10.0.6.0/24"]

    public_subnet_cidrs  = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]

    region = "us-west-2"
    ```
    2. 
    ```
    AWS_PROFILE=demo terraform init -var-file="<path to file>" -var="region=us-west-1"

    AWS_PROFILE=demo terraform plan -var-file="<path to file>" -var="region=us-west-1"

    AWS_PROFILE=demo terraform apply -var-file="<path to file>" -var="region=us-west-1"

    AWS_PROFILE=demo terraform destroy -var-file="<path to file>" -var="region=us-west-1"
    ```

## format the code before raise PR

```
    terraform fmt
```


## terraform.tfvars file should be provided or pass value from command
The following information should be included
```
    vp_cidr = ""

    region = ""

    ami_id = ""

    private_key_path=""

    key_name=""

    zone_id="" -> find this in route53 hosted zone

    subdomain_name=""
```


## RDS db command

1. install PostgreSQL in EC2 instance

```
    sudo apt update
    sudo apt upgrade -y
    sudo apt-get install postgresql-client-common postgresql-client
```
2. connect to the RDS database, password is required

```
    psql -h <RDS endpoint> -U csye6225 -d csye6225
```
3. show all DB
```
    \l
```
4. check current db
```
    SELECT current_database();
```
5. show all table in current database
```
    \dt
```
6. select users table (for this project, table name is users)
```
    SELECT * FROM users;
```
7. exit users table 
```
    :q
```
7. exit pgsql shell 
```
    \q
```

## SSH connect to EC2 instance
```
    ssh -i <path to key> ubuntu@<EC2_Public_IP>
```


# JMeter for Load Teat
```
    cd <path to JMeter bin>
    ./jmeter.sh
```

# Upload SSL certificate to AWS Certificate Manager
```
   aws acm import-certificate \
  --certificate fileb://<path to certificate>/
  --private-key fileb://<path to key> \
  --certificate-chain fileb://<path to ca.bundle> \
  --profile demo 
```