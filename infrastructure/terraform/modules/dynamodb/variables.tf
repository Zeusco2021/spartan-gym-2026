variable "env" {
  type = string
}

variable "replica_region" {
  description = "Secondary region for DynamoDB global tables (Req 27.3)"
  type        = string
  default     = "us-west-2"
}

variable "enable_pitr" {
  description = "Enable Point-in-Time Recovery for on-demand backups (Req 17.5)"
  type        = bool
  default     = true
}
