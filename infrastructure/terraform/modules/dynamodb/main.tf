# Req 17.5: On-demand backups for DynamoDB (PITR enabled)
# Req 27.3: Global tables replicated in 2 regions

locals {
  tables = {
    workout_sessions = {
      hash_key  = "userId"
      range_key = "sessionId"
    }
    workout_sets = {
      hash_key  = "sessionId"
      range_key = "setId"
    }
    user_achievements = {
      hash_key  = "userId"
      range_key = "achievementId"
    }
    user_preferences = {
      hash_key  = "userId"
      range_key = "preferenceKey"
    }
    messages = {
      hash_key  = "conversationId"
      range_key = "messageId"
    }
    conversations = {
      hash_key  = "userId"
      range_key = "conversationId"
    }
    notification_delivery = {
      hash_key  = "userId"
      range_key = "notificationId"
    }
  }
}

resource "aws_dynamodb_table" "tables" {
  for_each = local.tables

  name         = "spartan-gym-${var.env}-${each.key}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = each.value.hash_key
  range_key    = each.value.range_key

  attribute {
    name = each.value.hash_key
    type = "S"
  }

  attribute {
    name = each.value.range_key
    type = "S"
  }

  # Req 17.5: On-demand backups via Point-in-Time Recovery
  point_in_time_recovery {
    enabled = var.enable_pitr
  }

  # Encryption at rest (Req 13.1: AES-256)
  server_side_encryption {
    enabled = true
  }

  # Req 27.3: Global table replication in 2 regions
  replica {
    region_name = var.replica_region
  }

  tags = {
    Name = "spartan-gym-${var.env}-${each.key}"
  }
}
