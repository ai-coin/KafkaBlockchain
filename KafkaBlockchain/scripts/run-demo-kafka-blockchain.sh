#!/bin/sh
# ****************************************************************************
# * runs the DemoKafkaBlockchain program.
# ****************************************************************************

mvn "-Dexec.args=-classpath %classpath com.ai_blockchain.KafkaBlockchainDemo" -Dexec.executable=java -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:1.5.0:exec


