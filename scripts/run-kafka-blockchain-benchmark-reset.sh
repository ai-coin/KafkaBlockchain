#!/bin/sh
# ****************************************************************************************
# deletes the existing demo benchmark Kafka blockchain so that the benchmark can be rerun.
# ****************************************************************************************

KAFKA_VERSION=kafka_2.12-2.5.0

echo delete previous records for the kafka-benchmark-blockchain 
${HOME}/$KAFKA_VERSION/bin/kafka-delete-records.sh --bootstrap-server localhost:9092 --offset-json-file scripts/demo-benchmark-records-to-delete.json


