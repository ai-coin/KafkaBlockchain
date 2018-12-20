#!/bin/sh
# ****************************************************************************************
# deletes the existing demo benchmark Kafka blockchain so that the benchmark can be rerun.
# 
# ****************************************************************************************

echo delete previous records for the kafka-benchmark-blockchain 
${HOME}/kafka_2.11-2.1.0/bin/kafka-delete-records.sh --bootstrap-server localhost:9092 --offset-json-file scripts/demo-benchmark-records-to-delete.json


