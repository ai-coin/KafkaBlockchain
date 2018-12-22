#!/bin/sh
# **********************************************************************************
# deletes the existing demo Kafka blockchain so that the demonstration can be rerun.
# **********************************************************************************

echo describing existing partitions for the Kafka group demo-multiple-partition-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-multiple-partition-blockchain-consumers --describe

echo delete previous records for the demo-multiple-partition-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-delete-records.sh --bootstrap-server localhost:9092 --offset-json-file scripts/demo-multiple-partition-records-to-delete.json

echo describing blockchain partitions for the Kafka group demo-multiple-partition-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-multiple-partition-blockchain-consumers --describe


