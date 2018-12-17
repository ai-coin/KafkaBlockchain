#!/bin/sh
# ****************************************************************************
# * runs the TestKafkaBlockchainOnline program.
# ****************************************************************************

mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.TestKafkaBlockchainOnline" -Dexec.executable=java -Dexec.classpathScope=test org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


