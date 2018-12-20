/*
 * KafkaBlockchainBenchmark.java
 *
 * Created on December 17, 2018.
 *
 * Description:  Provides a benchmark tamper-evident blockchain on a stream of Kafka messages.
 *
 * Copyright (C) 2007 Stephen L. Reed.
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
 * Navigate to this project's directory, and launch the script in a third terminal session which runs the KafkaBlockchain benchmark all on one server.
 * > scripts/run-kafka-blockchain-blockchain.sh
 *
 * After the tests are complete, shut down the Kafka session with Ctrl-C.
 *
 * Shut down the ZooKeeper session with Ctrl-C.
 *
 * </code>
 */
package com.ai_blockchain.samples;

import com.ai_blockchain.KafkaAccess;
import com.ai_blockchain.KafkaBlockchainInfo;
import com.ai_blockchain.SHA256Hash;
import com.ai_blockchain.Serialization;
import com.ai_blockchain.TEObject;
import com.ai_blockchain.ZooKeeperAccess;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public class KafkaBlockchainBenchmark implements Callback {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainBenchmark.class);
  // the Kafka producer to which tamper evident messages are sent for transport to the Kafka broker.
  private KafkaProducer<String, byte[]> kafkaProducer;
  // the blockchain name (topic)
  private static final String BLOCKCHAIN_NAME = "kafka-benchmark-blockchain";
  // the list of Kafka broker seed addresses, formed as "host1:port1, host2:port2, ..."
  public static final String KAFKA_HOST_ADDRESSES = "localhost:9092";
  public final KafkaBlockchainInfo kafkaBlockchainInfo = new KafkaBlockchainInfo(
          BLOCKCHAIN_NAME, // topic
          SHA256Hash.makeSHA256Hash(""),
          1); // serialNbr
  // the ZooKeeper access object
  private final ZooKeeperAccess zooKeeperAccess;
  // the prefix used for ZooKeeper genesis data, the path has the format /KafkaBlockchain/kafka-benchmark-blockchain
  public static final String ZK_GENESIS_PATH_PREFIX = "/KafkaBlockchain/";
  // the indicator whether the first (genesis) blockchain record is being produced, in which case the hash and blockchain name are persisted 
  // in ZooKeeper for this benchmark - and for production would be stored in a secret-keeping facility.
  private boolean isBlockchainGenesis = true;

  /**
   * Constructs a new KafkaBlockchainBenchmark instance.
   */
  public KafkaBlockchainBenchmark() {
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
    final KafkaBlockchainBenchmark kafkaBlockchainDemo = new KafkaBlockchainBenchmark();
    kafkaBlockchainDemo.activateKafkaMessaging();
    kafkaBlockchainDemo.benchmarkKafkaBlockchain();
    kafkaBlockchainDemo.finalization();
  }

  /**
   * Closes the open resources.
   */
  public void finalization() {
    kafkaProducer.close();
    zooKeeperAccess.close();
  }

  /**
   * Creates demonstration payloads and puts them into a Kafka blockchain.
   */
  public void benchmarkKafkaBlockchain() {
    final int nbrIterations = 10000000;
    LOGGER.info("This benchmark creates a Kafka blockchain which is " + (nbrIterations / 1000000) + " million records in length...");
    final long startTimeMillis = System.currentTimeMillis();
    for (int i = 0; i < nbrIterations; i++) {
      produce(
              new DemoPayload("benchmark payload", i), // payload
              BLOCKCHAIN_NAME); // topic
    }
    final long durationMillis = System.currentTimeMillis() - startTimeMillis;
    final double durationSeconds = durationMillis / 1000.0d;
    final double iterationsPerSecond = nbrIterations / durationSeconds;
    LOGGER.info("nbrIterations       " + nbrIterations);
    LOGGER.info("durationMillis      " + durationMillis);
    LOGGER.info("durationSeconds     " + durationSeconds);
    LOGGER.info("iterationsPerSecond " + iterationsPerSecond);
  }

  /**
   * Activate Kafka messaging, including a producer and a consumer of tamper-evident payloads.
   */
  public void activateKafkaMessaging() {

    // list Kafka topics as evidence that the Kafka broker is responsive
    final KafkaAccess kafkaAccess = new KafkaAccess(KAFKA_HOST_ADDRESSES);

    LOGGER.info("activating Kafka messaging");
    /**
     * Because Kafka does not sequentially order in multiple partitions, one partition must be specified for a Kafka blockchain.
     */
    kafkaAccess.createTopic(
            BLOCKCHAIN_NAME, // topic
            1, // numPartitions
            (short) 3); // replicationFactor
    LOGGER.info("  Kafka topics " + kafkaAccess.listTopics());

    if (kafkaProducer == null) {
      final Properties props = new Properties();
      props.put("bootstrap.servers", KAFKA_HOST_ADDRESSES);
      props.put("client.id", "TEKafkaProducer");
      props.put("key.serializer", StringSerializer.class.getName());
      props.put("value.serializer", ByteArraySerializer.class.getName());
      kafkaProducer = new KafkaProducer<>(props);
    }
  }

  /**
   * Wraps the given payload as a tamper-evident object, computes the next blockchain hash and sends the tamper-evident object to the Kafka broker.
   *
   * @param payload the given payload
   * @param topic the topic, which is the blockchain name
   */
  public void produce(
          final Serializable payload,
          final String topic) {
    //Preconditions
    assert payload != null : "payload must not be null";
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("message topic " + topic);
    }

    final TEObject teObject = new TEObject(
            payload,
            kafkaBlockchainInfo.getSHA256Hash(),
            kafkaBlockchainInfo.getSerialNbr());

    if (isBlockchainGenesis) {
      isBlockchainGenesis = false;
      // make a unique path for the named blockchain
      final String path = ZK_GENESIS_PATH_PREFIX + BLOCKCHAIN_NAME;
      // record the SHA256 hash for the genesis record
      final String dataString = teObject.getTEObjectHash().toString();
      LOGGER.info("genesis hash for " + KafkaBlockchainDemo.KAFKA_DEMO_BLOCKCHAIN + "=" + dataString);
      // remove prior any prior versions
      if (zooKeeperAccess.exists(path)) {
        zooKeeperAccess.deleteRecursive(path);
      }
      // record the first produced block as the genesis
      zooKeeperAccess.setDataString(path, dataString);
    }

    kafkaBlockchainInfo.setSha256Hash(teObject.getTEObjectHash());
    kafkaBlockchainInfo.incrementSerialNbr();
    kafkaBlockchainInfo.setTimestamp(new Date());
    final byte[] serializedTEObject = Serialization.serialize(teObject);
    // send message with key
    final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
            topic,
            serializedTEObject); // value
    final Future<RecordMetadata> future = kafkaProducer.send(
            producerRecord,
            this);  // callback
  }

  /**
   * A callback method the user can implement to provide asynchronous handling of request completion. This method will be called when the record sent to the
   * server has been acknowledged. Exactly one of the arguments will be non-null.
   *
   * @param metadata The metadata for the record that was sent (i.e. the partition and offset). Null if an error occurred.
   * @param exception The exception thrown during processing of this record. Null if no error occurred. Possible thrown exceptions include:
   *
   * Non-Retriable exceptions (fatal, the message will never be sent):
   *
   * InvalidTopicException OffsetMetadataTooLargeException RecordBatchTooLargeException RecordTooLargeException UnknownServerException
   *
   * Retriable exceptions (transient, may be covered by increasing #.retries):
   *
   * CorruptRecordException InvalidMetadataException NotEnoughReplicasAfterAppendException NotEnoughReplicasException OffsetOutOfRangeException TimeoutException
   * UnknownTopicOrPartitionException
   */
  @Override
  public void onCompletion(final RecordMetadata metadata, final Exception exception) {
  }

  /**
   * Provides a demonstration payload for a Kafka blockchain.
   */
  static class DemoPayload implements Serializable {

    // the demo string data
    private final String string;
    // the demo integer data
    private final Integer integer;

    /**
     * Constructs a new DemoPayload instance.
     *
     * @param string the demo string data
     * @param integer the demo integer data
     */
    DemoPayload(
            final String string,
            final Integer integer) {
      this.string = string;
      this.integer = integer;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
      return new StringBuffer()
              .append("[DemoPayload, string=")
              .append(string)
              .append(", integer=")
              .append(integer)
              .append(']')
              .toString();
    }
  }
}
