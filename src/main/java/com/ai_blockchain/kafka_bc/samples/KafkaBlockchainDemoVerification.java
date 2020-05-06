/*
 * KafkaBlockchainDemoVerification.java
 *
 * Created on December 17, 2018.
 *
 * Description:  Verifies a tamper-evident blockchain on a stream of Kafka messages. The name of the blockchain is a Kafka topic and is provided as a command line argument.
 *
 * Copyright (C) 2018 Stephen L. Reed.
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * <code>
 * This demonstration performs KafkaBlockchain operations using a test Kafka broker configured per "Kafka Quickstart" https://kafka.apache.org/quickstart .
 * Launch ZooKeeper in a terminal session
 * > cd ~/kafka_2.12-2.5.0; bin/zookeeper-server-start.sh config/zookeeper.properties
 * 
 * Launch Kafka in a second terminal session after ZooKeeper initializes.
 * > cd ~/kafka_2.12-2.5.0; bin/kafka-server-start.sh config/server.properties
 *
 * Navigate to this project's scripts directory, and launch the script in a third terminal session which runs the KafkaBlockchain demo and this verification program.
 * > ./run-kafka-blockchain-demo.sh
 *
 * After the tests are complete, shut down the Kafka session with Ctrl-C.
 *
 * Shut down the ZooKeeper session with Ctrl-C.
 *
 * </code>
 *
 * Compare with a python blockchain implementation: https://hackernoon.com/a-blockchain-experiment-with-apache-kafka-97ee0ab6aefc
 */
package com.ai_blockchain.kafka_bc.samples;

import com.ai_blockchain.kafka_bc.KafkaAccess;
import com.ai_blockchain.kafka_bc.KafkaUtils;
import com.ai_blockchain.kafka_bc.SHA256Hash;
import com.ai_blockchain.kafka_bc.Serialization;
import com.ai_blockchain.kafka_bc.TEObject;
import com.ai_blockchain.kafka_bc.ZooKeeperAccess;
import static com.ai_blockchain.kafka_bc.samples.KafkaBlockchainDemo.KAFKA_DEMO_BLOCKCHAIN;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public class KafkaBlockchainDemoVerification {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainDemoVerification.class);
  // the list of Kafka broker seed addresses, formed as "host1:port1, host2:port2, ..."
  public static final String KAFKA_HOST_ADDRESSES = "localhost:9092";
  // the Kafka message consumer group id
  private static final String KAFKA_GROUP_ID = "demo-blockchain-consumers";
  // the ZooKeeper access object, which is used to retrieve the genesis block hash
  private final ZooKeeperAccess zooKeeperAccess;
  // the indicator whether the first (genesis) blockchain record is being consumed
  private boolean isBlockchainGenesis = true;
  // the blockchain name, which is a Kafka topic
  private static String blockchainName;
  // the prefix used for ZooKeeper genesis data, the path has the format /KafkaBlockchain/<blockchain name>
  public static final String ZK_GENESIS_PATH_PREFIX = "/KafkaBlockchain/";

  /**
   * Constructs a new KafkaBlockchainDemoVerification instance.
   */
  public KafkaBlockchainDemoVerification() {
    LOGGER.info("connecting to ZooKeeper...");
    zooKeeperAccess = new ZooKeeperAccess();
    zooKeeperAccess.connect(ZooKeeperAccess.ZOOKEEPER_CONNECT_STRING);
  }

  /**
   * Executes this Kafka blockchain demonstration.
   *
   * @param args the command line arguments (unused)
   */
  public static void main(final String[] args) {
    if (args != null && args.length > 0 && args[0] != null) {
      blockchainName = args[0].trim();
      LOGGER.info("Verifying the specified Kafka blockchain named " + blockchainName);
    } else {
      blockchainName = KAFKA_DEMO_BLOCKCHAIN;
      LOGGER.info("Verifying the default Kafka blockchain named " + blockchainName);
    }
    if (args != null && args.length > 1 && "-quiet".equals(args[1])) {
      Logger.getLogger(KafkaBlockchainDemoVerification.class).setLevel(Level.WARN);
    }

    final KafkaBlockchainDemoVerification kafkaBlockchainDemoVerification = new KafkaBlockchainDemoVerification();
    kafkaBlockchainDemoVerification.verifyDemoBlockchain();
    kafkaBlockchainDemoVerification.finalization();
  }

  /**
   * Closes the open resources.
   */
  public void finalization() {
    zooKeeperAccess.close();
  }

  /**
   * Verify that the sequential topic records form a valid blockchain.
   */
  public void verifyDemoBlockchain() {

    // list Kafka topics as evidence that the Kafka broker is responsive
    final KafkaAccess kafkaAccess = new KafkaAccess(KAFKA_HOST_ADDRESSES);
    LOGGER.info("Kafka topics " + kafkaAccess.listTopics());
    kafkaAccess.close();

    // get the genesis hash for this blockchain from ZooKeeper
    final String path = ZK_GENESIS_PATH_PREFIX + blockchainName;
    final String sha256HashString = zooKeeperAccess.getDataString(path);
    LOGGER.warn("genesis hash for path " + path + " = " + sha256HashString);
    if (sha256HashString == null) {
      LOGGER.warn("no genesis hash found for the blockchain named " + blockchainName);
      return;
    }
    final SHA256Hash genesisSHA256Hash = new SHA256Hash(sha256HashString);
    TEObject previousTEObject = null;

    LOGGER.warn("now consuming messages from topic " + blockchainName);

    // the Kafka consumer
    KafkaConsumer<String, byte[]> kafkaConsumer;
    // the topics, which has only one topic, which is the the test blockchain name
    final List<String> topics = new ArrayList<>();

    topics.add(blockchainName);
    Properties props = new Properties();
    props.put("bootstrap.servers", KAFKA_HOST_ADDRESSES);
    props.put("group.id", KAFKA_GROUP_ID);
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", ByteArrayDeserializer.class.getName());

    kafkaConsumer = new KafkaConsumer<>(props);
    try {
      LOGGER.info("subscribing to " + topics);
      kafkaConsumer.subscribe(topics);
      // Kafka transmits messages in compressed JSON format.
      // Records are key value pairs per JSON
      //
      // In the Kafka Blockchain, the blockchain name is the topic.
      // The key is provided by the caller.
      // Kafka Blockchain Messages are wrapped with TEObjects which are serialized into bytes then passed to Kafka,
      // which then serializes the bytes into a key-value JSON object for compressed transmission.
      //
      // Processing at the recipient container happens in the reverse order.
      // The consumer receives the bytes from deserializing the received JSON message, then
      // deserializes the TEObject, which contains the Message object.
      LOGGER.info("consumer loop poll...");
      KafkaUtils.seekToBeginning(kafkaConsumer, blockchainName); // topic
      int recordCnt = 0;
      boolean isStillRunning = true;
      while (isStillRunning) {

        ConsumerRecords<String, byte[]> consumerRecords = kafkaConsumer.poll(Duration.ofSeconds(1)); // timeout
        if (consumerRecords.isEmpty()) {
          LOGGER.warn("...end of consumed records");
          isStillRunning = false;
          break;
        }
        for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
          if (++recordCnt % 100000 == 0) {
            LOGGER.warn("verified " + recordCnt);
          }
          LOGGER.info("received consumerRecord " + consumerRecord);
          final byte[] serializedTEObject = consumerRecord.value();
          final TEObject teObject = (TEObject) Serialization.deserialize(serializedTEObject);
          LOGGER.info("  deserialized teObject " + teObject);
          Serializable payload = teObject.getPayload();
          LOGGER.info("  payload: " + payload);

          if (isBlockchainGenesis) {
            // verify that this genesis tamper-evident object hashes as expected
            isBlockchainGenesis = false;
            if (!teObject.isValid()) {
              LOGGER.warn("The SHA-256 hash calculation is wrong for the first blockchain record " + teObject);
              isStillRunning = false;
              break;
            } else if (!teObject.getTEObjectHash().equals(genesisSHA256Hash)) {
              LOGGER.warn("The SHA-256 hash of the first blockchain record does not match the stored value " + teObject);
              LOGGER.warn("  stored SHA-256 hash       = " + genesisSHA256Hash.toString());
              LOGGER.warn("  first record SHA-256 hash = " + teObject.getTEObjectHash().toString());
              break;
            }
            LOGGER.info("  the genesis record verifies with the expected SHA-256 hash");
          } else {
            // verify that this tamper-evident record is a valid successor to the previous one
            if (!teObject.isValidSuccessor(previousTEObject)) {
              LOGGER.warn("The tamper-evident object is not a valid successor " + teObject + " in the " + blockchainName + " blockchain");
              isStillRunning = false;
              break;
            }
            LOGGER.info("  this record is a valid successor in the " + blockchainName + " blockchain ");
          }
          previousTEObject = teObject;
        }
      }
    } catch (WakeupException e) {
      // ignore for shutdown
    } finally {
      LOGGER.info("closing the consumer loop...");
      kafkaConsumer.commitSync();
      kafkaConsumer.unsubscribe();
      kafkaConsumer.close(
              1, // timeout
              TimeUnit.SECONDS);
    }
  }
}
