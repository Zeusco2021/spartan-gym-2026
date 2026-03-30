#!/bin/bash
# =============================================================================
# Spartan Golden Gym — Kafka Topic Provisioning Script for Amazon MSK
# Creates all platform topics with partition counts, retention policies,
# and Dead Letter Queue (DLQ) topics.
#
# Usage:
#   ./create-topics.sh <bootstrap-servers> [replication-factor]
#
# Example:
#   ./create-topics.sh b-1.spartan-gym.abc123.kafka.us-east-1.amazonaws.com:9092 3
#
# Validates: Requirements 11.3, 11.4
# =============================================================================

set -euo pipefail

BOOTSTRAP_SERVERS="${1:?Usage: $0 <bootstrap-servers> [replication-factor]}"
REPLICATION_FACTOR="${2:-3}"
DLQ_PARTITIONS=3

KAFKA_TOPICS_CMD="${KAFKA_TOPICS_CMD:-kafka-topics.sh}"

# Retention values
RETENTION_7D="604800000"
RETENTION_24H="86400000"

create_topic() {
    local name="$1"
    local partitions="$2"
    local retention="$3"

    echo "Creating topic: ${name} (partitions=${partitions}, retention=${retention}ms, replicas=${REPLICATION_FACTOR})"
    ${KAFKA_TOPICS_CMD} \
        --bootstrap-server "${BOOTSTRAP_SERVERS}" \
        --create \
        --if-not-exists \
        --topic "${name}" \
        --partitions "${partitions}" \
        --replication-factor "${REPLICATION_FACTOR}" \
        --config "retention.ms=${retention}"
}

create_dlq() {
    local name="${1}.dlq"
    echo "Creating DLQ topic: ${name} (partitions=${DLQ_PARTITIONS}, replicas=${REPLICATION_FACTOR})"
    ${KAFKA_TOPICS_CMD} \
        --bootstrap-server "${BOOTSTRAP_SERVERS}" \
        --create \
        --if-not-exists \
        --topic "${name}" \
        --partitions "${DLQ_PARTITIONS}" \
        --replication-factor "${REPLICATION_FACTOR}"
}

echo "============================================="
echo " Spartan Golden Gym — Kafka Topic Setup"
echo " Bootstrap: ${BOOTSTRAP_SERVERS}"
echo " Replication Factor: ${REPLICATION_FACTOR}"
echo "============================================="

# Main topics
create_topic "workout.completed"          20  "${RETENTION_7D}"
create_topic "user.achievements"          10  "${RETENTION_7D}"
create_topic "real.time.heartrate"        50  "${RETENTION_24H}"
create_topic "ai.recommendations.request" 15  "${RETENTION_7D}"
create_topic "social.interactions"        20  "${RETENTION_7D}"
create_topic "nutrition.logs"             20  "${RETENTION_7D}"
create_topic "gym.occupancy"              10  "${RETENTION_24H}"
create_topic "bookings.events"            10  "${RETENTION_7D}"

# Dead Letter Queues
create_dlq "workout.completed"
create_dlq "user.achievements"
create_dlq "real.time.heartrate"
create_dlq "ai.recommendations.request"
create_dlq "social.interactions"
create_dlq "nutrition.logs"
create_dlq "gym.occupancy"
create_dlq "bookings.events"

echo ""
echo "All topics created successfully."
