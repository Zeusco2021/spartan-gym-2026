resource "aws_timestreamwrite_database" "main" {
  database_name = "spartan_metrics_${var.env}"

  tags = {
    Name = "spartan-gym-${var.env}-timestream"
  }
}

resource "aws_timestreamwrite_table" "heartrate_data" {
  database_name = aws_timestreamwrite_database.main.database_name
  table_name    = "heartrate_data"

  retention_properties {
    memory_store_retention_period_in_hours  = 24
    magnetic_store_retention_period_in_days = 365
  }
}

resource "aws_timestreamwrite_table" "workout_metrics" {
  database_name = aws_timestreamwrite_database.main.database_name
  table_name    = "workout_metrics"

  retention_properties {
    memory_store_retention_period_in_hours  = 24
    magnetic_store_retention_period_in_days = 365
  }
}

resource "aws_timestreamwrite_table" "biometric_data" {
  database_name = aws_timestreamwrite_database.main.database_name
  table_name    = "biometric_data"

  retention_properties {
    memory_store_retention_period_in_hours  = 24
    magnetic_store_retention_period_in_days = 365
  }
}

resource "aws_timestreamwrite_table" "performance_metrics" {
  database_name = aws_timestreamwrite_database.main.database_name
  table_name    = "performance_metrics"

  retention_properties {
    memory_store_retention_period_in_hours  = 24
    magnetic_store_retention_period_in_days = 365
  }
}
