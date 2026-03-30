output "notebook_url" {
  value = aws_sagemaker_notebook_instance.main.url
}

output "role_arn" {
  value = aws_iam_role.sagemaker.arn
}
