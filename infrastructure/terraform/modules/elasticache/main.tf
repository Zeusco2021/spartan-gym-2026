resource "aws_elasticache_subnet_group" "main" {
  name       = "spartan-gym-${var.env}-redis"
  subnet_ids = var.private_subnet_ids
}

resource "aws_security_group" "redis" {
  name_prefix = "spartan-gym-${var.env}-redis-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [var.eks_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spartan-gym-${var.env}-redis-sg"
  }
}

resource "aws_elasticache_replication_group" "main" {
  replication_group_id = "spartan-gym-${var.env}-redis"
  description          = "Spartan Golden Gym Redis cluster - ${var.env}"
  node_type            = var.node_type
  num_cache_clusters   = 2
  engine               = "redis"
  engine_version       = "7.0"
  port                 = 6379
  parameter_group_name = "default.redis7"

  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [aws_security_group.redis.id]

  # Encryption (Req 13.1)
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true

  automatic_failover_enabled = true
  multi_az_enabled           = true

  tags = {
    Name = "spartan-gym-${var.env}-redis"
  }
}
