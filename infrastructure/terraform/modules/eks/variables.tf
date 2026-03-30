variable "env" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "cluster_version" {
  type    = string
  default = "1.29"
}

variable "node_instance_types" {
  type    = list(string)
  default = ["t3.large"]
}

variable "node_desired_size" {
  type    = number
  default = 3
}

variable "node_min_size" {
  type    = number
  default = 2
}

variable "node_max_size" {
  type    = number
  default = 10
}

variable "cpu_target_percentage" {
  description = "CPU utilization threshold for autoscaling (Req 17.3)"
  type        = number
  default     = 70
}

variable "memory_target_percentage" {
  description = "Memory utilization threshold for autoscaling (Req 17.3)"
  type        = number
  default     = 80
}
