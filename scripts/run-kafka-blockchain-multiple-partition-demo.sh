#!/bin/sh
# ***************************************************************************************
# runs the KafkaBlockchainMultiplePartitionDemo program, and preview enabled for Java 14.
# ***************************************************************************************

export _JAVA_OPTIONS='-ea -Xms1G -Xmx5G --enable-preview'
KAFKA_VERSION=kafka_2.12-2.5.0

echo demonstrate putting messages into a Kafka blockchain named kafka-demo-multiple-partition-blockchain
mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.kafka_bc.samples.KafkaBlockchainMultiplePartitionDemo" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec

echo describing blockchain partitions for the Kafka group demo-multiple-partition-blockchain-consumers
${HOME}/${KAFKA_VERSION}/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-multiple-partition-blockchain-consumers --describe

echo demonstrate verifying messages from the Kafka blockchain named kafka-demo-multiple-partition-blockchain
mvn "-Dexec.args=-ea -classpath %classpath com.ai_blockchain.kafka_bc.samples.KafkaBlockchainMultiplePartitionDemoVerification kafka-demo-multiple-partition-blockchain" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


