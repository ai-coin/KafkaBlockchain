#!/bin/sh
# ****************************************************************************
# runs the KafkaBlockchainDemo program, and preview enabled for Java 14.
# If the following error occurs, the Kafka instance is not yet initialized and this script should run OK the second time
#     Error: Executing consumer group command failed due to org.apache.kafka.common.errors.CoordinatorNotAvailableException: The coordinator is not available.
#     java.util.concurrent.ExecutionException: org.apache.kafka.common.errors.CoordinatorNotAvailableException: The coordinator is not available.
# ****************************************************************************

export _JAVA_OPTIONS='-ea -Xms1G -Xmx5G --enable-preview'
KAFKA_VERSION=kafka_2.12-2.5.0

echo demonstrate putting messages into a Kafka blockchain named kafka-demo-blockchain
mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.kafka_bc.samples.KafkaBlockchainDemo" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec

echo describing blockchain partitions for the Kafka group demo-blockchain-consumers
${HOME}/$KAFKA_VERSION/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group demo-blockchain-consumers --describe

echo demonstrate verifying messages from the Kafka blockchain named kafka-demo-blockchain
mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.kafka_bc.samples.KafkaBlockchainDemoVerification kafka-demo-blockchain" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


