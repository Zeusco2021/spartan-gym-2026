variable "env" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "instance_class" {
  type    = string
  default = "db.r6g.large"
}

variable "backup_retention_days" {
  description = "Backup retention period in days (Req 17.5: 30 days)"
  type        = number
  default     = 30
}

variable "availability_zones" {
  description = "AZs for Aurora replication (Req 27.2: at least 2 AZs)"
  type        = list(string)
}

variable "eks_security_group_id" {
  description = "Security group ID of EKS nodes for DB access"
  type        = string
}
