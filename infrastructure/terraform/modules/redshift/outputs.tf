output "cluster_endpoint" {
  value = aws_redshift_cluster.main.endpoint
}

output "cluster_id" {
  value = aws_redshift_cluster.main.id
}
