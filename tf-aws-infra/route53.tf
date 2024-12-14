resource "aws_route53_record" "A_record" {
  zone_id = var.zone_id
  name    = var.subdomain_name
  type    = "A"

  alias {
    name                   = aws_lb.webapp_load_balancer.dns_name
    zone_id                = aws_lb.webapp_load_balancer.zone_id
    evaluate_target_health = true
  }
}