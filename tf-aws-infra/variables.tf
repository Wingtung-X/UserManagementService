variable "vp_cidr" {
  description = "The CIDR for the VPC"
  type        = string
}

variable "region" {
  description = "Region"
  type        = string
}

variable "ami_id" {
  description = "IMA ID"
  type        = string
}

variable "key_name" {
  description = "ssh key name"
  type        = string
}

variable "private_key_path" {
  description = "Path to the private key for ssh access"
  type        = string
}

variable "zone_id" {
  description = "The ID of the Route 53 hosted zone"
  type        = string
}

variable "subdomain_name" {
  description = "The subdomain to create or update A record for"
  type        = string
}

variable "max_threshold" {
  description = "The CPU usage we want to increase the number of instance"
  type        = number
}

variable "min_threshold" {
  description = "The CPU usage we want to decrease the number of instance"
  type        = number
}

variable "zip_file_path" {
  description = "Lambda zip file path"
  type        = string
}

variable "sendgrid_key" {
  description = "SENDGRID_API_KEY"
  type        = string
}