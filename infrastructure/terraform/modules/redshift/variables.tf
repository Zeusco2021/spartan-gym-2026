variable "env" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "private_subnet_ids" {
  type = list(string)
}

variable "node_type" {
  type    = string
  default = "dc2.large"
}

variable "number_of_nodes" {
  type    = number
  default = 2
}

variable "eks_security_group_id" {
  type = string
}
