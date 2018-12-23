/*
 * KafkaBlockchainMultiplePartitionDemoVerification.java
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
 * > cd ~/kafka_2.11-2.1.0; bin/zookeeper-server-start.sh config/zookeeper.properties
 * 
 * Launch Kafka in a second terminal session after ZooKeeper initializes.
 * > cd ~/kafka_2.11-2.1.0; bin/kafka-server-start.sh config/server.properties
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public class KafkaBlockchainMultiplePartitionDemoVerification {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainMultiplePartitionDemoVerification.class);
  // the list of Kafka broker seed addresses, formed as "host1:port1, host2:port2, ..."
  public static final String KAFKA_HOST_ADDRESSES = "localhost:9092";
  // the ZooKeeper access object, which is used to retrieve the genesis block hash
  private final ZooKeeperAccess zooKeeperAccess;
  // the blockchain name, which is a Kafka topic
  private static String blockchainName;
  // the prefix used for ZooKeeper genesis data, the path has the format /KafkaBlockchain/<blockchain name>
  public static final String ZK_GENESIS_PATH_PREFIX = "/KafkaBlockchain/";
  // the blockchain consumers, one for each partition of the topic (blockchain name)
  private final List<KafkaBlockchainPartitionConsumer> kafkaBlockchainPartitionConsumers = new ArrayList<>();
  // the console terminal escape sequence for green text
  public static final String ANSI_GREEN = "\033[0;32m";
  public static final String ANSI_RESET = "\033[0m";

  /**
   * Constructs a new KafkaBlockchainDemoVerification instance.
   */
  public KafkaBlockchainMultiplePartitionDemoVerification() {
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
      blockchainName = "kafka-benchmark-blockchain";
      LOGGER.info("Verifying the default Kafka blockchain named " + blockchainName);
    }
    if (args != null && args.length > 1 && "-quiet".equals(args[1])) {
      Logger.getLogger(KafkaBlockchainMultiplePartitionDemoVerification.class).setLevel(Level.WARN);
      Logger.getLogger(KafkaUtils.class).setLevel(Level.WARN);
    }

    final KafkaBlockchainMultiplePartitionDemoVerification kafkaBlockchainDemoVerification = new KafkaBlockchainMultiplePartitionDemoVerification();
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
    LOGGER.info("genesis hash for path " + path + " = " + sha256HashString);
    if (sha256HashString == null) {
      LOGGER.warn("no genesis hash found for the blockchain named " + blockchainName);
      return;
    }
    final SHA256Hash genesisSHA256Hash = new SHA256Hash(sha256HashString);

    final Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", KAFKA_HOST_ADDRESSES);
    consumerProperties.put("group.id", "kafka-benchmark-group");
    consumerProperties.put("key.deserializer", StringDeserializer.class.getName());
    consumerProperties.put("value.deserializer", ByteArrayDeserializer.class.getName());

    final List<TopicPartition> assignedTopicPartitions = KafkaUtils.getAssignedTopicPartitions(
            blockchainName,
            consumerProperties);
    final int nbrPartitions = assignedTopicPartitions.size();
    LOGGER.info("the blockchain named " + blockchainName + " has " + nbrPartitions + " partitions");

    /**
     * Create a consumer for each partition of the topic (blockchain name), then each consumer will read records exclusively from a single partition.
     */
    for (int i = 0; i < nbrPartitions; i++) {
      kafkaBlockchainPartitionConsumers.add(
              new KafkaBlockchainPartitionConsumer(
                      consumerProperties,
                      assignedTopicPartitions.get(i))); // topicPartition
    }

    // start the partition consumers and they will each pause at their current record
    for (int i = 0; i < nbrPartitions; i++) {
      final KafkaBlockchainPartitionConsumer kafkaBlockchainPartitionConsumer = kafkaBlockchainPartitionConsumers.get(i);
      final Thread kafkaBlockchainPartitionConsumerThread = new Thread(kafkaBlockchainPartitionConsumer);
      LOGGER.info("starting consumer " + (i + 1));
      kafkaBlockchainPartitionConsumerThread.start();
    }

    LOGGER.info("waiting for each consumer to read a record from their respective partitions...");
    boolean isReady = false;
    while (!isReady) {
      isReady = true;
      for (int i = 0; i < nbrPartitions; i++) {
        final KafkaBlockchainPartitionConsumer kafkaBlockchainPartitionConsumer = kafkaBlockchainPartitionConsumers.get(i);
        if (kafkaBlockchainPartitionConsumer.isDone()) {
        } else if (kafkaBlockchainPartitionConsumer.getTeObject() == null) {
          isReady = false;
          break;
        }
      }
    }
    for (int i = 0; i < nbrPartitions; i++) {
      final KafkaBlockchainPartitionConsumer kafkaBlockchainPartitionConsumer = kafkaBlockchainPartitionConsumers.get(i);
      LOGGER.info("consumer " + (i + 1) + ", first record " + kafkaBlockchainPartitionConsumer.getTeObject());
      assert kafkaBlockchainPartitionConsumer.getTeObject() != null;
    }

    LOGGER.info("finding the genesis record for " + blockchainName + "...");
    // blockchain connects sucessive tamper-evident objects by storing the SHA-256 hash of the previous record as a field in the current record
    TEObject previousTEObject = null;
    // find and verify the first (genesis) record of the blockchain, whose SHA-256 was stored in ZooKeeper for this topic
    for (int i = 0; i < nbrPartitions; i++) {
      final KafkaBlockchainPartitionConsumer kafkaBlockchainPartitionConsumer = kafkaBlockchainPartitionConsumers.get(i);
      final TEObject teObject = kafkaBlockchainPartitionConsumer.getTeObject();
      assert teObject != null;
      if (genesisSHA256Hash.equals(teObject.getTEObjectHash())) {
        LOGGER.info("found genesis record " + teObject + ", having serialNbr " + teObject.getSerialNbr());
        previousTEObject = teObject;
        // release this consumer to advance to its next record
        kafkaBlockchainPartitionConsumer.resetTeObject();
        break;
      }
    }

    if (previousTEObject == null) {
      LOGGER.warn("genesis record not found for SHA-256 hash " + genesisSHA256Hash);
      System.exit(-1);
    }
    if (!previousTEObject.isValid()) {
      LOGGER.warn("The SHA-256 hash calculation is wrong for the first blockchain record " + previousTEObject);
      System.exit(-1);
    }

    /**
     * This thread spin locks waiting for the next serially numbered record to be unwrapped by a partition consumer.
     */
    boolean isStillRunning = true;
    // iterate over all the records by muliplexing the input ordered partitions of this topic (blockchain name)
    int nbrRecords = 0;
    while (isStillRunning) {

      // exit when all the consumers are done
      boolean areAllConsumersDone = true;
      for (int i = 0; i < nbrPartitions; i++) {
        final KafkaBlockchainPartitionConsumer kafkaBlockchainPartitionConsumer = kafkaBlockchainPartitionConsumers.get(i);
        if (kafkaBlockchainPartitionConsumer.isDone
                && kafkaBlockchainPartitionConsumer.getTeObject() == null) {
          // the consumer exhausted its partition and the main thread has retreived the tamper evident object
        } else {
          areAllConsumersDone = false;
          break;
        }
      }
      if (areAllConsumersDone) {
        LOGGER.info("all blockchain partition consumers are done");
        isStillRunning = false;
        continue;
      }

      assert previousTEObject != null;

      // figure out which consumer's record is the next sequence in the blockchain, and there could be none ready yet
      final long nextSerialNbr = previousTEObject.getSerialNbr() + 1;
      //LOGGER.info("finding successor " + nextSerialNbr);
      for (int i = 0; i < nbrPartitions; i++) {
        final KafkaBlockchainPartitionConsumer kafkaBlockchainPartitionConsumer = kafkaBlockchainPartitionConsumers.get(i);
        final TEObject teObject = kafkaBlockchainPartitionConsumer.getTeObject();
        if (teObject == null) {
          continue;
        }
        if (teObject.getSerialNbr() == nextSerialNbr) {
          LOGGER.info(ANSI_GREEN + "found successor " + teObject + " in partition " + i + ANSI_RESET);
          if (!previousTEObject.getTEObjectHash().equals(teObject.getPreviousHash())) {
            LOGGER.warn("The tamper-evident object is not a valid successor " + teObject + " in the " + blockchainName + " blockchain");
            isStillRunning = false;
            break;
          } else if (++nbrRecords % 100_000 == 0) {
            LOGGER.warn("verified " + nbrRecords);
          }
          previousTEObject = teObject;
          // release this consumer to advance to its next record
          kafkaBlockchainPartitionConsumer.resetTeObject();
        }
      }
    }

  }

  /**
   * Reads records from a Kafka partition, provided the number of consumers in the group equals the number of partitions.
   */
  static class KafkaBlockchainPartitionConsumer implements Runnable, Comparable<KafkaBlockchainPartitionConsumer> {

    // the kafka consumer, one per partition
    private final KafkaConsumer<String, byte[]> kafkaConsumer;
    // the topic partition
    private final TopicPartition topicPartition;
    // the partition
    private final int partition;
    // the current tamper evident object read from the partition associated with this consumer, which is shared with the main thread for ordering
    private volatile TEObject teObject = null;
    // the indicator whether this consumer is done
    private volatile boolean isDone = false;

    /**
     * Constructs a new KafkaBlockchainPartitionConsumer instance.
     *
     * @param topic the topic (blockchain name)
     * @param consumerProperties the Kafka consumer properties
     * @param threadNbr the thread number used for thread identification
     */
    KafkaBlockchainPartitionConsumer(
            final Properties consumerProperties,
            final TopicPartition topicPartition) {
      //Preconditions
      assert consumerProperties != null : "consumerProperties must not be null";
      assert topicPartition != null : "topicPartition must not be null";

      kafkaConsumer = new KafkaConsumer<>(consumerProperties);
      this.topicPartition = topicPartition;
      partition = topicPartition.partition();
    }

    /**
     * Consumes records from a partition.
     */
    @Override
    public void run() {
      try {
        Thread.currentThread().setName("consumer-" + partition);
        try {
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
          //
          final List<TopicPartition> topicPartitions = new ArrayList<>();
          topicPartitions.add(topicPartition);
          LOGGER.info("thread " + partition + " assigning topic partition " + topicPartitions);
          kafkaConsumer.assign(topicPartitions);
          LOGGER.info("thread " + partition + " seeking to the beginning of topic partition " + topicPartitions);
          kafkaConsumer.seekToBeginning(topicPartitions);
          int recordCnt = 0;
          boolean isStillRunning = true;
          while (isStillRunning) {
            LOGGER.info("thread " + partition + " consumer poll...");
            ConsumerRecords<String, byte[]> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1_000)); // timeout
            if (consumerRecords.isEmpty()) {
              LOGGER.warn("thread " + partition + "...end of consumed records");
              isStillRunning = false;
              break;
            }
            LOGGER.info("thread " + partition + " received records from partition " + consumerRecords.partitions());

            for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
              if (++recordCnt % 100000 == 0) {
                LOGGER.warn("consumer-" + partition + " read a total of " + recordCnt + " records from its partition");
              }
              LOGGER.debug("thread " + partition + " received consumerRecord " + consumerRecord);
              final byte[] serializedTEObject = consumerRecord.value();
              teObject = (TEObject) Serialization.deserialize(serializedTEObject);
              LOGGER.info("thread " + partition + "   deserialized teObject " + teObject);
              // spin-wait for the main thread to use up the current tamper evident object before examining the next one from this partition
              while (teObject != null) {
              }
            }
          }
        } catch (WakeupException e) {
          // ignore for shutdown
        } finally {
          LOGGER.info("thread " + partition + " closing the consumer");
          kafkaConsumer.commitSync();
          kafkaConsumer.unsubscribe();
          kafkaConsumer.close(
                  1, // timeout
                  TimeUnit.SECONDS);
        }
        isDone = true;
      } catch (Exception ex) {
        LOGGER.fatal(ex.getMessage());
        System.exit(-1);
      }
    }

    /**
     * Gets the current tamper evident object read from the partition associated with this consumer, which is shared with the main thread for ordering.
     *
     * @return the current tamper evident object
     */
    public TEObject getTeObject() {
      return teObject;
    }

    /**
     * Resets the current tamper evident object read from the partition associated with this consumer, which is shared with the main thread for ordering.
     */
    public void resetTeObject() {
      teObject = null;
    }

    /**
     * Gets whether this consumer is done.
     *
     * @return whether this consumer is done
     */
    public boolean isDone() {
      return isDone;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
      return new StringBuilder()
              .append("[Consumer of partition ")
              .append(partition)
              .append(", isDone=")
              .append(isDone)
              .append(", teObject=")
              .append(teObject)
              .append("]")
              .toString();
    }

    /**
     * Orders the partition consumers by partition number.
     *
     * @param other the other partition consumer
     * @return -1 if less than, zero if equal, otherwise +1
     */
    @Override
    public int compareTo(final KafkaBlockchainPartitionConsumer other) {
      return Integer.compare(this.partition, other.partition);
    }

  }
}
