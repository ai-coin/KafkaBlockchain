#!/bin/sh
# **********************************************************************************
# deletes the existing demo Kafka blockchain so that the demonstration can be rerun.
# **********************************************************************************

KAFKA_VERSION=kafka_2.12-2.5.0

echo describing existing partitions for the Kafka group demo-multiple-partition-blockchain-consumers
${HOME}/$KAFKA_VERSION/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-multiple-partition-blockchain-consumers --describe

echo delete previous records for the demo-multiple-partition-blockchain-consumers
${HOME}/$KAFKA_VERSION/bin/kafka-delete-records.sh --bootstrap-server localhost:9092 --offset-json-file scripts/demo-multiple-partition-records-to-delete.json

echo describing blockchain partitions for the Kafka group demo-multiple-partition-blockchain-consumers
${HOME}/$KAFKA_VERSION/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-multiple-partition-blockchain-consumers --describe


