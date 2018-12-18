#!/bin/sh
# ****************************************************************************
# * runs the KafkaBlockchainDemo program.
# ****************************************************************************

mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.samples.KafkaBlockchainEncryptionDemo" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


