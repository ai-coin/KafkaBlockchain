#!/bin/sh
# *******************************************************************************************
# deletes the existing demo encrypted Kafka blockchain so that the demonstration can be rerun.
# 
# ********************************************************************************************

echo describing existing partitions for the Kafka group demo-encryption-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-encryption-blockchain-consumers --describe

echo delete previous records for the demo encryption consumer group
${HOME}/kafka_2.11-2.1.0/bin/kafka-delete-records.sh --bootstrap-server localhost:9092 --offset-json-file scripts/demo-encryption-records-to-delete.json

echo describing blockchain partitions for the Kafka demo-encryption-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-encryption-blockchain-consumers --describe


