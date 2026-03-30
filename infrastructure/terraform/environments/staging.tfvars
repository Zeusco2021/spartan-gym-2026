aws_region           = "us-east-1"
aws_secondary_region = "us-west-2"
vpc_cidr             = "10.1.0.0/16"
availability_zones   = ["us-east-1a", "us-east-1b"]

# EKS
eks_cluster_version     = "1.29"
eks_node_instance_types = ["t3.large"]
eks_node_desired_size   = 3
eks_node_min_size       = 2
eks_node_max_size       = 6

# Aurora
aurora_instance_class = "db.r6g.large"

# Neptune
neptune_instance_class = "db.r5.large"

# Redis
redis_node_type = "cache.r6g.large"

# MSK
msk_instance_type   = "kafka.m5.large"
msk_kafka_version   = "3.5.1"
msk_ebs_volume_size = 100

# SageMaker
sagemaker_instance_type = "ml.t3.medium"

# Redshift
redshift_node_type       = "dc2.large"
redshift_number_of_nodes = 2
