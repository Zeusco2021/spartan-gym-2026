aws_region           = "us-east-1"
aws_secondary_region = "us-west-2"
vpc_cidr             = "10.0.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b"]

# EKS - smaller for dev
eks_cluster_version     = "1.29"
eks_node_instance_types = ["t3.medium"]
eks_node_desired_size   = 2
eks_node_min_size       = 1
eks_node_max_size       = 4

# Aurora - smaller for dev
aurora_instance_class = "db.t4g.medium"

# Neptune
neptune_instance_class = "db.t3.medium"

# Redis
redis_node_type = "cache.t3.medium"

# MSK
msk_instance_type   = "kafka.t3.small"
msk_kafka_version   = "3.5.1"
msk_ebs_volume_size = 50

# SageMaker
sagemaker_instance_type = "ml.t3.medium"

# Redshift
redshift_node_type       = "dc2.large"
redshift_number_of_nodes = 1
