# KafkaBlockchain
A java library for tamper-evidence using Kafka. Messages are optionally encrypted and hashed sequentially.

 This demonstration performs KafkaBlockchain operations using a test Kafka broker configured per "Kafka Quickstart" https://kafka.apache.org/quickstart .
 Launch ZooKeeper in a terminal session
 > cd ~/kafka_2.11-2.1.0; bin/zookeeper-server-start.sh config/zookeeper.properties
 
 Launch Kafka in a second terminal session after ZooKeeper initializes.
 > cd ~/kafka_2.11-2.1.0; bin/kafka-server-start.sh config/server.properties
 
 Navigate to this project's scripts directory, and launch the script in a third terminal session which runs the KafkaBlockchain demo.
 > ./run-kafka-blockchain-demo.sh
 
 After the demo is complete, shut down the Kafka session with Ctrl-C.
 
 Shut down the ZooKeeper session with Ctrl-C.
