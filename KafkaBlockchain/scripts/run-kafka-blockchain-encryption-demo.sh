#!/bin/sh
# ****************************************************************************
# * runs the KafkaBlockchainEncryptionDemo program.
# ****************************************************************************

echo describing partitions for the Kafka group demo-encryption-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-encryption-blockchain-consumers --describe

echo reset the partitions for the demo-encryption-blockchain-consumers group
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-encryption-blockchain-consumers --topic kafka-demo-blockchain-2 --reset-offsets --to-earliest --execute

mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.samples.KafkaBlockchainEncryptionDemo" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


