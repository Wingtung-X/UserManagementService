output "vpc_id" {
  value       = aws_vpc.main.id
  description = "The ID of the VPC"
}

output "internet_gateway_id" {
  value       = aws_internet_gateway.gw.id
  description = "The ID of the Internet Gateway"
}

output "public_subnet_ids" {
  value       = aws_subnet.public_subnets[*].id
  description = "The IDs of the public subnets"
}

output "private_subnet_ids" {
  value       = aws_subnet.private_subnets[*].id
  description = "The IDs of the private subnets"
}

output "public_route_table_id" {
  value       = aws_route_table.public.id
  description = "The ID of the public route table"
}

output "private_route_table_id" {
  value       = aws_route_table.private.id
  description = "The ID of the private route table"
}

output "db_endpoint" {
  value       = aws_db_instance.csye6225_rds_instance.endpoint
  description = "DB endpoint"
}

output "rds_password" {
  value     = local.rds_password
  sensitive = true
}

output "selected_subnent_id" {
  value       = element(aws_subnet.public_subnets.*.id, 0)
  description = "?"
}