resource "aws_redshift_subnet_group" "main" {
  name       = "spartan-gym-${var.env}-redshift"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "spartan-gym-${var.env}-redshift-subnet-group"
  }
}

resource "aws_security_group" "redshift" {
  name_prefix = "spartan-gym-${var.env}-redshift-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 5439
    to_port         = 5439
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
    Name = "spartan-gym-${var.env}-redshift-sg"
  }
}

resource "aws_redshift_cluster" "main" {
  cluster_identifier = "spartan-gym-${var.env}-redshift"
  database_name      = "spartananalytics"
  master_username    = "spartanadmin"
  manage_master_password = true
  node_type          = var.node_type
  number_of_nodes    = var.number_of_nodes
  cluster_type       = var.number_of_nodes > 1 ? "multi-node" : "single-node"

  cluster_subnet_group_name = aws_redshift_subnet_group.main.name
  vpc_security_group_ids    = [aws_security_group.redshift.id]

  encrypted = true

  skip_final_snapshot = var.env != "produccion"

  automated_snapshot_retention_period = 7

  tags = {
    Name = "spartan-gym-${var.env}-redshift"
  }
}
