aws_region           = "us-east-1"
aws_secondary_region = "us-west-2"
vpc_cidr             = "10.2.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b"]

# EKS - production-grade
eks_cluster_version     = "1.29"
eks_node_instance_types = ["m5.xlarge"]
eks_node_desired_size   = 5
eks_node_min_size       = 3
eks_node_max_size       = 20

# Aurora - production-grade
aurora_instance_class = "db.r6g.xlarge"

# Neptune
neptune_instance_class = "db.r5.xlarge"

# Redis
redis_node_type = "cache.r6g.xlarge"

# MSK
msk_instance_type   = "kafka.m5.xlarge"
msk_kafka_version   = "3.5.1"
msk_ebs_volume_size = 500

# SageMaker
sagemaker_instance_type = "ml.m5.large"

# Redshift
redshift_node_type       = "dc2.large"
redshift_number_of_nodes = 4
