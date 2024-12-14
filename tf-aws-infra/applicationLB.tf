resource "aws_lb" "webapp_load_balancer" {
  name               = "webapp-load-balancer"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.lb_security_group.id]
  subnets            = aws_subnet.public_subnets[*].id
}


resource "aws_lb_target_group" "webapp_target_group" {
  name     = "app-target-group"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  health_check {
    path = "/healthz"
    port = "8080"
  }
}

# forward traffic to 8080 port
resource "aws_lb_listener" "http_listener" {
  load_balancer_arn = aws_lb.webapp_load_balancer.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.webapp_target_group.arn
  }
}

data "aws_acm_certificate" "certificate" {
  domain      = var.subdomain_name
  statuses    = ["ISSUED"]
  most_recent = true
}

resource "aws_lb_listener" "https_listener" {
  load_balancer_arn = aws_lb.webapp_load_balancer.arn
  port              = 443
  protocol          = "HTTPS"

  ssl_policy      = "ELBSecurityPolicy-2016-08"
  certificate_arn = data.aws_acm_certificate.certificate.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.webapp_target_group.arn
  }
}

