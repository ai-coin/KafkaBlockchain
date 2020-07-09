#!/bin/sh
# **************************************************************************************************
# * runs the KafkaBlockchainDemo program, with assertions disabled, and preview enabled for Java 14.
# **************************************************************************************************

export _JAVA_OPTIONS='-ea -Xms1G -Xmx5G --enable-preview'

echo demonstrate verifying messages from the Kafka blockchain named kafka-benchmark-blockchain
mvn "-Dexec.args=-da -classpath %classpath com.ai_blockchain.kafka_bc.samples.KafkaBlockchainMultiplePartitionDemoVerification kafka-benchmark-blockchain -quiet" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


