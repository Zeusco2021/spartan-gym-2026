resource "aws_iam_role" "sagemaker" {
  name = "spartan-gym-${var.env}-sagemaker-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "sagemaker.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "sagemaker_full" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonSageMakerFullAccess"
  role       = aws_iam_role.sagemaker.name
}

resource "aws_security_group" "sagemaker" {
  name_prefix = "spartan-gym-${var.env}-sagemaker-"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "spartan-gym-${var.env}-sagemaker-sg"
  }
}

resource "aws_sagemaker_notebook_instance" "main" {
  name          = "spartan-gym-${var.env}-notebook"
  role_arn      = aws_iam_role.sagemaker.arn
  instance_type = var.instance_type
  subnet_id     = var.private_subnet_ids[0]
  security_groups = [aws_security_group.sagemaker.id]

  direct_internet_access = "Disabled"

  tags = {
    Name = "spartan-gym-${var.env}-sagemaker-notebook"
  }
}
