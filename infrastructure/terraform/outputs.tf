output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "eks_cluster_endpoint" {
  description = "EKS cluster endpoint"
  value       = module.eks.cluster_endpoint
}

output "eks_cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "aurora_cluster_endpoint" {
  description = "Aurora PostgreSQL cluster writer endpoint"
  value       = module.aurora.cluster_endpoint
}

output "aurora_reader_endpoint" {
  description = "Aurora PostgreSQL cluster reader endpoint"
  value       = module.aurora.reader_endpoint
}

output "dynamodb_table_arns" {
  description = "DynamoDB table ARNs"
  value       = module.dynamodb.table_arns
}

output "timestream_database_name" {
  description = "Timestream database name"
  value       = module.timestream.database_name
}

output "neptune_cluster_endpoint" {
  description = "Neptune cluster endpoint"
  value       = module.neptune.cluster_endpoint
}

output "redis_endpoint" {
  description = "ElastiCache Redis primary endpoint"
  value       = module.elasticache.primary_endpoint
}

output "msk_bootstrap_brokers" {
  description = "MSK bootstrap brokers connection string"
  value       = module.msk.bootstrap_brokers_tls
}

output "cloudfront_distribution_domain" {
  description = "CloudFront distribution domain name"
  value       = module.s3_cloudfront.cloudfront_domain_name
}

output "s3_media_bucket" {
  description = "S3 media bucket name"
  value       = module.s3_cloudfront.media_bucket_name
}

output "sagemaker_notebook_url" {
  description = "SageMaker notebook URL"
  value       = module.sagemaker.notebook_url
}

output "redshift_cluster_endpoint" {
  description = "Redshift cluster endpoint"
  value       = module.redshift.cluster_endpoint
}
