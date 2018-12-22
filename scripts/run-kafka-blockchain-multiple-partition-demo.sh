#!/bin/sh
# ****************************************************************************
# runs the KafkaBlockchainMultiplePartitionDemo program.
# ****************************************************************************

echo demonstrate putting messages into a Kafka blockchain named kafka-demo-multiple-partition-blockchain
mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.kafka_bc.samples.KafkaBlockchainMultiplePartitionDemo" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec

echo describing blockchain partitions for the Kafka group demo-blockchain-consumers
${HOME}/kafka_2.11-2.1.0/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-multiple-partition-blockchain-consumers --describe

echo demonstrate verifying messages from the Kafka blockchain named kafka-demo-blockchain
mvn "-Dexec.args=-ea -classpath %classpath com.ai_blockchain.kafka_bc.samples.KafkaBlockchainMultiplePartitionDemoVerification kafka-demo-multiple-partition-blockchain" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


