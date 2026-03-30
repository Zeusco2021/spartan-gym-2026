resource "aws_neptune_subnet_group" "main" {
  name       = "spartan-gym-${var.env}-neptune"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "spartan-gym-${var.env}-neptune-subnet-group"
  }
}

resource "aws_security_group" "neptune" {
  name_prefix = "spartan-gym-${var.env}-neptune-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 8182
    to_port         = 8182
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
    Name = "spartan-gym-${var.env}-neptune-sg"
  }
}

resource "aws_neptune_cluster" "main" {
  cluster_identifier  = "spartan-gym-${var.env}-neptune"
  engine              = "neptune"
  neptune_subnet_group_name = aws_neptune_subnet_group.main.name
  vpc_security_group_ids    = [aws_security_group.neptune.id]

  storage_encrypted   = true
  skip_final_snapshot = var.env != "produccion"

  backup_retention_period = 7

  tags = {
    Name = "spartan-gym-${var.env}-neptune-cluster"
  }
}

resource "aws_neptune_cluster_instance" "main" {
  identifier         = "spartan-gym-${var.env}-neptune-instance"
  cluster_identifier = aws_neptune_cluster.main.id
  instance_class     = var.instance_class
  engine             = "neptune"

  tags = {
    Name = "spartan-gym-${var.env}-neptune-instance"
  }
}
