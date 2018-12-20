#!/bin/sh
# ****************************************************************************
# * runs the KafkaBlockchainDemo program, with assertions disabled
# ****************************************************************************

#mvn "-Dexec.args=-da -classpath %classpath com.ai_blockchain.samples.KafkaBlockchainBenchmark" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


echo demonstrate verifying messages from the Kafka blockchain named kafka-benchmark-blockchain
mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.samples.KafkaBlockchainDemoVerification kafka-benchmark-blockchain -quiet" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


