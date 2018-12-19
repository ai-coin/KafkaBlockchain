#!/bin/sh
# ****************************************************************************
# * runs the KafkaBlockchainDemo program.
# ****************************************************************************

echo describing existing partitions for the Kafka group demo-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-blockchain-consumers --describe

echo delete previous records for the demo consumer group
${HOME}/kafka_2.11-2.1.0/bin/kafka-delete-records.sh --bootstrap-server localhost:9092 --offset-json-file scripts/demo-records-to-delete.json

echo demonstrate putting messages into a Kafka blockchain named kafka-demo-blockchain
mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.samples.KafkaBlockchainDemo" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec

echo describing blockchain partitions for the Kafka group demo-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-blockchain-consumers --describe

echo demonstrate verifying messages from the Kafka blockchain named kafka-demo-blockchain
mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.samples.KafkaBlockchainDemoVerification kafka-demo-blockchain" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


