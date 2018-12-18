#!/bin/sh
# ****************************************************************************
# * runs the KafkaBlockchainDemo program, with assertions disabled
# ****************************************************************************

mvn "-Dexec.args=-da -classpath %classpath com.ai_blockchain.samples.KafkaBlockchainBenchmark" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


