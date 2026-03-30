variable "aws_region" {
  description = "Primary AWS region"
  type        = string
  default     = "us-east-1"
}

variable "aws_secondary_region" {
  description = "Secondary AWS region for disaster recovery"
  type        = string
  default     = "us-west-2"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of availability zones (minimum 2 for Aurora replication)"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

# EKS
variable "eks_cluster_version" {
  description = "Kubernetes version for EKS"
  type        = string
  default     = "1.29"
}

variable "eks_node_instance_types" {
  description = "Instance types for EKS node group"
  type        = list(string)
  default     = ["t3.large"]
}

variable "eks_node_desired_size" {
  description = "Desired number of EKS nodes"
  type        = number
  default     = 3
}

variable "eks_node_min_size" {
  description = "Minimum number of EKS nodes"
  type        = number
  default     = 2
}

variable "eks_node_max_size" {
  description = "Maximum number of EKS nodes"
  type        = number
  default     = 10
}

# Aurora
variable "aurora_instance_class" {
  description = "Instance class for Aurora PostgreSQL"
  type        = string
  default     = "db.r6g.large"
}

# Neptune
variable "neptune_instance_class" {
  description = "Instance class for Neptune"
  type        = string
  default     = "db.r5.large"
}

# ElastiCache Redis
variable "redis_node_type" {
  description = "Node type for ElastiCache Redis"
  type        = string
  default     = "cache.r6g.large"
}

# MSK
variable "msk_instance_type" {
  description = "Instance type for MSK brokers"
  type        = string
  default     = "kafka.m5.large"
}

variable "msk_kafka_version" {
  description = "Kafka version for MSK"
  type        = string
  default     = "3.5.1"
}

variable "msk_ebs_volume_size" {
  description = "EBS volume size in GB for MSK brokers"
  type        = number
  default     = 100
}

# SageMaker
variable "sagemaker_instance_type" {
  description = "Instance type for SageMaker notebook"
  type        = string
  default     = "ml.t3.medium"
}

# Redshift
variable "redshift_node_type" {
  description = "Node type for Redshift cluster"
  type        = string
  default     = "dc2.large"
}

variable "redshift_number_of_nodes" {
  description = "Number of nodes in Redshift cluster"
  type        = number
  default     = 2
}
