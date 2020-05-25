| Connector Version | Source Technology Version | Confluent Platorm Version |   
| --- | --- | --- |  
| 1.0 | Ai-BlockChain 1.0 | Compatible Confluent Platform Version - ≥5.0.X |  
---

# KafkaBlockchain
KafkaBlockchain is a java library for tamper-evidence using Kafka. Messages are optionally encrypted and hashed sequentially. The library methods are called within a Kafka application's message producer code to wrap messages, and called within the application's consumer code to unwrap messages. A sample utility program is provided that consumes and verifies a blockchained topic.

Because blockchains must be strictly sequentially ordered, Kafka blockchain topics must either have a single partition, or consumers for each partition must cooperate to sequence the records. Sample programs demonstrate blockchains with a single partition and with multiple partitions. If multiple producers exist, they must cooperate to serially add records to a Kafka blockchain.

Kafka already implements checksums for message streams to detect data loss. However, an attacker can provide false records that have correct checksums. Cryptographic hashes such as the standard SHA-256 algorithm are very difficult to falsify, which makes them ideal for tamper-evidence despite being a bit more computation than checksums.

To manage Kafka blockchains, the sample programs store the first (genesis) message SHA-256 hash for each blockchain topic in ZooKeeper. In production, secret keeping facilities, for example Vault can be used.

**Dependencies**
This library uses the Bouncy Castle crypto library.
Apache Maven is required to build this library, and to run the quickstart examples.
This library is written using Java 10.
The jar file is shaded such that the dependencies are hidden from any Maven project which includes KafkaBlockchain as a dependency.

**Goodies**
Included are utility classes: ByteUtils, KafkaAccess, ZooKeeperAccess, SHA256Hash and Serialization.

# Quickstart

 These demonstrations perform KafkaBlockchain operations using a test Kafka broker configured per [Kafka Quickstart"](https://kafka.apache.org/quickstart).
 
 Launch ZooKeeper in a terminal session
 > cd ~/kafka_2.12-2.5.0; bin/zookeeper-server-start.sh config/zookeeper.properties
 
 Launch Kafka in a second terminal session after ZooKeeper initializes.
 > cd ~/kafka_2.12-2.5.0; bin/kafka-server-start.sh config/server.properties
 
 Launch the demonstrations in a third terminal session after Kafka initializes.
 
 **Kafka blockchain demonstration**
 
 Navigate to this project's directory, and launch this script in a third terminal session which runs the KafkaBlockchain demo.
 > scripts/run-kafka-blockchain-demo.sh
 
 The first program produces four payloads on the blockchain and then consumes them. A second program verifies the whole blockchain. Before re-running this demonstration, remove the previous blockchain messages by running this script first.
 
 > scripts/run-kafka-blockchain-demo-reset.sh
 
**Kafka blockchain multiple partition demonstration**
 
 Navigate to this project's directory, and launch this script in a third terminal session which runs the KafkaBlockchain demo.
 > scripts/run-kafka-blockchain-multiple-partition-demo.sh
 
 The first program produces four payloads on the blockchain and then consumes them. A second program verifies the whole blockchain. Before re-running this demonstration, remove the previous blockchain messages by running this script first.
 
 > scripts/run-kafka-blockchain-multiple-partition-demo-reset.sh
 
**Kafka encrypted blockchain demonstration**
 
 Navigate to this project's directory, and launch this script in the third terminal session which runs the KafkaBlockchain encryption demo.
 > scripts/run-kafka-blockchain-encryption-demo.sh
 
 The program produces four encrypted payloads on the blockchain and then consumes them with decryption. A second program verifies the whole blockchain without needing to decrypt it. Before re-running this demonstration, remove the previous blockchain messages by running this script first.
 
 > scripts/run-kafka-blockchain-demo-encryption-reset.sh
 
**Kafka blockchain benchmark**
 
 Navigate to this project's directory, and launch this script in the third terminal session which runs the KafkaBlockchain benchmark. The producer creates 10 million messages for the Kafka blockchain with three partitions. A second program reads the 10 million messages and verifies the correctness of the blockchain. The demonstration payload is efficiently serialized using tailored methods that implement Externalizable. The benchmark takes a couple of minutes to create the messages and about five minutes more to verify the integrity of the 10 million messages with a consumer thread for each partition.
 
 > scripts/run-kafka-blockchain-benchmark.sh
 
  Before re-running this benchmark, remove the previous 10 million blockchain messages by running this script first.
 
 > scripts/run-kafka-blockchain-benchmark-reset.sh
 
 **shutdown**
 
 After the demo is complete, shut down the Kafka session with Ctrl-C.
 
 Shut down the ZooKeeper session with Ctrl-C.
