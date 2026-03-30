variable "env" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "instance_type" {
  type    = string
  default = "kafka.m5.large"
}

variable "kafka_version" {
  type    = string
  default = "3.5.1"
}

variable "ebs_volume_size" {
  type    = number
  default = 100
}

variable "eks_security_group_id" {
  type = string
}
