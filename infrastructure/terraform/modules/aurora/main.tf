# Req 17.5: Automatic daily backups with 30-day retention
# Req 27.2: Replication across 2 AZs with automatic failover

resource "aws_db_subnet_group" "aurora" {
  name       = "spartan-gym-${var.env}-aurora-subnet"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "spartan-gym-${var.env}-aurora-subnet-group"
  }
}

resource "aws_security_group" "aurora" {
  name_prefix = "spartan-gym-${var.env}-aurora-"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
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
    Name = "spartan-gym-${var.env}-aurora-sg"
  }
}

resource "aws_rds_cluster" "main" {
  cluster_identifier = "spartan-gym-${var.env}-aurora"
  engine             = "aurora-postgresql"
  engine_version     = "15.4"
  database_name      = "spartangym"
  master_username    = "spartanadmin"
  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.aurora.name
  vpc_security_group_ids = [aws_security_group.aurora.id]
  availability_zones     = var.availability_zones

  # Req 17.5: Daily automatic backups with 30-day retention
  backup_retention_period      = var.backup_retention_days
  preferred_backup_window      = "03:00-04:00"
  preferred_maintenance_window = "sun:04:00-sun:05:00"

  # Encryption at rest (Req 13.1: AES-256)
  storage_encrypted = true

  # Req 27.2: Automatic failover
  deletion_protection = var.env == "produccion" ? true : false
  skip_final_snapshot = var.env != "produccion"

  # Req 27.6: Backups stored encrypted in S3
  copy_tags_to_snapshot = true

  tags = {
    Name = "spartan-gym-${var.env}-aurora-cluster"
  }
}

# Writer instance in AZ 1
resource "aws_rds_cluster_instance" "writer" {
  identifier         = "spartan-gym-${var.env}-aurora-writer"
  cluster_identifier = aws_rds_cluster.main.id
  instance_class     = var.instance_class
  engine             = aws_rds_cluster.main.engine
  engine_version     = aws_rds_cluster.main.engine_version
  availability_zone  = var.availability_zones[0]

  tags = {
    Name = "spartan-gym-${var.env}-aurora-writer"
  }
}

# Reader instance in AZ 2 (Req 27.2: replication in 2 AZs with failover)
resource "aws_rds_cluster_instance" "reader" {
  identifier         = "spartan-gym-${var.env}-aurora-reader"
  cluster_identifier = aws_rds_cluster.main.id
  instance_class     = var.instance_class
  engine             = aws_rds_cluster.main.engine
  engine_version     = aws_rds_cluster.main.engine_version
  availability_zone  = var.availability_zones[1]

  tags = {
    Name = "spartan-gym-${var.env}-aurora-reader"
  }
}
