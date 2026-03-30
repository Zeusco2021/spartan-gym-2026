terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "spartan-golden-gym-terraform-state"
    key            = "infrastructure/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "spartan-golden-gym"
      Environment = terraform.workspace
      ManagedBy   = "terraform"
    }
  }
}

provider "aws" {
  alias  = "secondary"
  region = var.aws_secondary_region

  default_tags {
    tags = {
      Project     = "spartan-golden-gym"
      Environment = terraform.workspace
      ManagedBy   = "terraform"
    }
  }
}

locals {
  env = terraform.workspace
}

# --- VPC ---
module "vpc" {
  source = "./modules/vpc"

  env                = local.env
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
}

# --- EKS ---
module "eks" {
  source = "./modules/eks"

  env                    = local.env
  vpc_id                 = module.vpc.vpc_id
  private_subnet_ids     = module.vpc.private_subnet_ids
  cluster_version        = var.eks_cluster_version
  node_instance_types    = var.eks_node_instance_types
  node_desired_size      = var.eks_node_desired_size
  node_min_size          = var.eks_node_min_size
  node_max_size          = var.eks_node_max_size
  cpu_target_percentage  = 70
  memory_target_percentage = 80
}

# --- Aurora PostgreSQL ---
module "aurora" {
  source = "./modules/aurora"

  env                    = local.env
  vpc_id                 = module.vpc.vpc_id
  private_subnet_ids     = module.vpc.private_subnet_ids
  instance_class         = var.aurora_instance_class
  backup_retention_days  = 30
  availability_zones     = var.availability_zones
  eks_security_group_id  = module.eks.node_security_group_id
}

# --- DynamoDB ---
module "dynamodb" {
  source = "./modules/dynamodb"

  env              = local.env
  replica_region   = var.aws_secondary_region
  enable_pitr      = true
}

# --- Timestream ---
module "timestream" {
  source = "./modules/timestream"

  env = local.env
}

# --- Neptune ---
module "neptune" {
  source = "./modules/neptune"

  env                   = local.env
  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  instance_class        = var.neptune_instance_class
  eks_security_group_id = module.eks.node_security_group_id
}

# --- ElastiCache Redis ---
module "elasticache" {
  source = "./modules/elasticache"

  env                   = local.env
  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  node_type             = var.redis_node_type
  eks_security_group_id = module.eks.node_security_group_id
}

# --- Amazon MSK (Kafka) ---
module "msk" {
  source = "./modules/msk"

  env                   = local.env
  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  instance_type         = var.msk_instance_type
  kafka_version         = var.msk_kafka_version
  ebs_volume_size       = var.msk_ebs_volume_size
  eks_security_group_id = module.eks.node_security_group_id
}

# --- S3 + CloudFront ---
module "s3_cloudfront" {
  source = "./modules/s3_cloudfront"

  env = local.env
}

# --- SageMaker ---
module "sagemaker" {
  source = "./modules/sagemaker"

  env                = local.env
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  instance_type      = var.sagemaker_instance_type
}

# --- Redshift ---
module "redshift" {
  source = "./modules/redshift"

  env                   = local.env
  vpc_id                = module.vpc.vpc_id
  private_subnet_ids    = module.vpc.private_subnet_ids
  node_type             = var.redshift_node_type
  number_of_nodes       = var.redshift_number_of_nodes
  eks_security_group_id = module.eks.node_security_group_id
}
