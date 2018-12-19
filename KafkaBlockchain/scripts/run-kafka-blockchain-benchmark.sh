#!/bin/sh
# ****************************************************************************
# * runs the KafkaBlockchainDemo program, with assertions disabled
# ****************************************************************************

echo describing partitions for the Kafka group benchmark-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group benchmark-blockchain-consumers --describe

echo reset the partitions for the benchmark-blockchain-consumers group
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group benchmark-blockchain-consumers --topic kafka-benchmark-blockchain --reset-offsets --to-earliest --execute

mvn "-Dexec.args=-da -classpath %classpath com.ai_blockchain.samples.KafkaBlockchainBenchmark" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


