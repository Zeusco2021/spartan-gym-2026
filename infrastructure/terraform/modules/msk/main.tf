resource "aws_security_group" "msk" {
  name_prefix = "spartan-gym-${var.env}-msk-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 9092
    to_port         = 9098
    protocol        = "tcp"
    security_groups = [var.eks_security_group_id]
  }

  ingress {
    from_port = 9092
    to_port   = 9098
    protocol  = "tcp"
    self      = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spartan-gym-${var.env}-msk-sg"
  }
}

resource "aws_msk_cluster" "main" {
  cluster_name           = "spartan-gym-${var.env}-kafka"
  kafka_version          = var.kafka_version
  number_of_broker_nodes = length(var.private_subnet_ids)

  broker_node_group_info {
    instance_type  = var.instance_type
    client_subnets = var.private_subnet_ids
    security_groups = [aws_security_group.msk.id]

    storage_info {
      ebs_storage_info {
        volume_size = var.ebs_volume_size
      }
    }
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS"
      in_cluster    = true
    }
  }

  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
        log_group = aws_cloudwatch_log_group.msk.name
      }
    }
  }

  tags = {
    Name = "spartan-gym-${var.env}-msk"
  }
}

resource "aws_cloudwatch_log_group" "msk" {
  name              = "/aws/msk/spartan-gym-${var.env}"
  retention_in_days = 30
}
