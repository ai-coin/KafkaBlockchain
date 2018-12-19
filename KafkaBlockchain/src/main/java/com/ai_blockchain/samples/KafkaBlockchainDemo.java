/*
 * KafkaBlockchainDemo.java
 *
 * Created on December 17, 2018.
 *
 * Description:  Demonstrates a tamper-evident blockchain on a stream of Kafka messages.
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
 * Navigate to this project's directory, and launch the script in a third terminal session which runs the KafkaBlockchain demo.
 * > scripts/run-kafka-blockchain-demo.sh
 *
 * After the tests are complete, shut down the Kafka session with Ctrl-C.
 *
 * Shut down the ZooKeeper session with Ctrl-C.
 *
 * </code>
 *
 * Compare with a python blockchain implementation: https://hackernoon.com/a-blockchain-experiment-with-apache-kafka-97ee0ab6aefc
 */
package com.ai_blockchain.samples;

import com.ai_blockchain.KafkaAccess;
import com.ai_blockchain.KafkaBlockchainInfo;
import com.ai_blockchain.KafkaUtils;
import com.ai_blockchain.SHA256Hash;
import com.ai_blockchain.Serialization;
import com.ai_blockchain.TEObject;
import com.ai_blockchain.ZooKeeperAccess;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public class KafkaBlockchainDemo implements Callback {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainDemo.class);
  // the Kafka producer to which tamper evident messages are sent for transport to the Kafka broker.
  private KafkaProducer<String, byte[]> kafkaProducer;
  // the blockchain name (topic)
  public static final String BLOCKCHAIN_NAME_2 = "kafka-demo-blockchain-2";
  // the list of Kafka broker seed addresses, formed as "host1:port1, host2:port2, ..."
  public static final String KAFKA_HOST_ADDRESSES = "localhost:9092";
  // the Kafka message consumer group id
  private static final String KAFKA_GROUP_ID = "demo-blockchain-consumers";
  // the Kafka message consumer
  private ConsumerLoop consumerLoop;
  // the Kafka message consumer loop thread
  private Thread kafkaConsumerLoopThread;
  // the blockchain hash dictionary, blockchain name --> Kafka blockchain info
  // this demo only uses one blockchain however
  private final Map<String, KafkaBlockchainInfo> blockchainHashDictionary = new HashMap<>();
  // the ZooKeeper access object
  private final ZooKeeperAccess zooKeeperAccess;
  // the indicator whether the first (genesis) blockchain record is being produced, in which case the hash and blockchain name are persisted 
  // in ZooKeeper for this demonstration - and for production would be stored in a secret-keeping facility.
  private boolean isBlockchainGenesis = true;
  // the prefix used for ZooKeeper genesis data, the path has the format /KafkaBlockchain/demo-blockchain-genesis-<blockchain name>
  public static final String ZK_GENESIS_PATH_PREFIX = "/KafkaBlockchain/demo-blockchain-genesis-";

  /**
   * Constructs a new KafkaBlockchainDemo instance.
   */
  public KafkaBlockchainDemo() {
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
    final KafkaBlockchainDemo kafkaBlockchainDemo = new KafkaBlockchainDemo();
    kafkaBlockchainDemo.activateKafkaMessaging();
    kafkaBlockchainDemo.produceDemoBlockchain();
    kafkaBlockchainDemo.finalization();
  }

  /**
   * Closes the open resources.
   */
  public void finalization() {
    LOGGER.info("waiting 5 seconds for the demonstration blockchain consumer to complete processing ...");
    try {
      Thread.sleep(5_000);
    } catch (InterruptedException ex) {
      // ignore
    }
    // quit the consumer loop thread
    consumerLoop.terminate();
    kafkaProducer.close();
    zooKeeperAccess.close();
  }

  /**
   * Creates demonstration payloads and puts them into a Kafka blockchain.
   */
  public void produceDemoBlockchain() {
    produce(new DemoPayload("abc", 1), // payload
            BLOCKCHAIN_NAME_2); // topic
    produce(new DemoPayload("def", 2), // payload
            BLOCKCHAIN_NAME_2); // topic
    produce(new DemoPayload("ghi", 3), // payload
            BLOCKCHAIN_NAME_2); // topic
    produce(new DemoPayload("jkl", 4), // payload
            BLOCKCHAIN_NAME_2); // topic
  }

  /**
   * Activate Kafka messaging, including a producer and a consumer of tamper-evident payloads.
   */
  public void activateKafkaMessaging() {

    // list Kafka topics as evidence that the Kafka broker is responsive
    final KafkaAccess kafkaAccess = new KafkaAccess(KAFKA_HOST_ADDRESSES);

    LOGGER.info("activating Kafka messaging");
    kafkaAccess.createTopic(BLOCKCHAIN_NAME_2, // topic
            3, // numPartitions
            (short) 1); // replicationFactor
    LOGGER.info("  Kafka topics " + kafkaAccess.listTopics());
    kafkaAccess.close();

    final Properties props = new Properties();
    props.put("bootstrap.servers", KAFKA_HOST_ADDRESSES);
    props.put("client.id", "TEKafkaProducer");
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", ByteArraySerializer.class.getName());
    kafkaProducer = new KafkaProducer<>(props);

    consumerLoop = new ConsumerLoop(KAFKA_HOST_ADDRESSES);
    kafkaConsumerLoopThread = new Thread(consumerLoop);
    kafkaConsumerLoopThread.setName("kafkaConsumer");
    kafkaConsumerLoopThread.start();
    LOGGER.info("now consuming messages from topic " + BLOCKCHAIN_NAME_2);

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
    KafkaBlockchainInfo kafkaBlockchainInfo;
    synchronized (blockchainHashDictionary) {
      // get the previous message hash and serial number for the blockchain
      kafkaBlockchainInfo = blockchainHashDictionary.get(topic);
      if (kafkaBlockchainInfo == null) {
        kafkaBlockchainInfo = getKafkaTopicInfo(topic);
        if (kafkaBlockchainInfo == null) {
          kafkaBlockchainInfo = new KafkaBlockchainInfo(
                  topic,
                  SHA256Hash.makeSHA256Hash(""),
                  1); // serialNbr
          LOGGER.info("initial previousTEObjectHash: " + kafkaBlockchainInfo);
        } else {
          LOGGER.info("retrieved previousTEObjectHash: " + kafkaBlockchainInfo);
        }
        blockchainHashDictionary.put(topic, kafkaBlockchainInfo);
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("cached previousTEObjectHash: " + kafkaBlockchainInfo);
        }
      }
    }

    final TEObject teObject = new TEObject(
            payload,
            kafkaBlockchainInfo.getSHA256Hash(),
            kafkaBlockchainInfo.getSerialNbr());

    if (isBlockchainGenesis) {
      isBlockchainGenesis = false;
      // make a unique path for the named blockchain
      final String path = makeZooKeeperPath();
      // record the SHA256 hash for the genesis record
      final String dataString = teObject.getTEObjectHash().toString();
      LOGGER.info("genesis hash for " + KafkaBlockchainDemo.BLOCKCHAIN_NAME_2 + "=" + dataString);
      // remove prior any prior versions
      zooKeeperAccess.deleteRecursive(path);
      // record the first produced block as the genesis
      zooKeeperAccess.setDataString(path, dataString);
    }

    // cache the blockchain's current tip hash in the dictionary
    kafkaBlockchainInfo.setSha256Hash(teObject.getTEObjectHash());
    kafkaBlockchainInfo.incrementSerialNbr();
    kafkaBlockchainInfo.setTimestamp(new Date());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("updated kafkaBlockchainInfo " + kafkaBlockchainInfo);
    }
    final byte[] serializedTEObject = Serialization.serialize(teObject);
    final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
            topic,
            serializedTEObject); // value
    // ignoring the future (which indicates completion or cancellation) in this demonstration
    final Future<RecordMetadata> future = kafkaProducer.send(
            producerRecord,
            this);  // callback
    LOGGER.info("sent producer record " + teObject.toString());
  }

  /**
   * Gets the Kafka blockchain information for the demonstration consumer.
   *
   * @param topic the topic, which is the blockchain name
   * @return the Kafka blockchain information
   */
  private KafkaBlockchainInfo getKafkaTopicInfo(final String topic) {
    //Preconditions
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";

    final Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", KAFKA_HOST_ADDRESSES);
    consumerProperties.put("group.id", "get most recent hash-chained data message");
    consumerProperties.put("key.deserializer", StringDeserializer.class.getName());
    consumerProperties.put("value.deserializer", ByteArrayDeserializer.class.getName());
    return KafkaUtils.getKafkaTopicInfo(topic, consumerProperties);
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
   * Makes a ZooKeeper path to store the genesis hash for the demo blockchain.
   *
   * @return a ZooKeeper path
   */
  public static String makeZooKeeperPath() {
    // make a unique path for the named blockchain
    return ZK_GENESIS_PATH_PREFIX + BLOCKCHAIN_NAME_2;
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

  /**
   * Provides a Kafka consumer loop that polls for messages to the test topic.
   */
  static class ConsumerLoop implements Runnable {

    // the logger
    private static final Logger LOGGER = Logger.getLogger(ConsumerLoop.class);
    // the Kafka consumer
    private final KafkaConsumer<String, byte[]> kafkaConsumer;
    // the topics, which has only one topic, which is the the test blockchain name
    private final List<String> topics = new ArrayList<>();
    // the indicator used to stop the consumer loop thread when the test is completed
    private boolean isDone = false;

    /**
     * Constructs a new ConsumerLoop instance.
     *
     * @param kafkaHostAddresses the kafka host addresses, such as 172.18.0.2:9092
     */
    public ConsumerLoop(final String kafkaHostAddresses) {
      assert kafkaHostAddresses != null && !kafkaHostAddresses.isEmpty() : "kafkaHostAddresses must be a non-empty string";

      LOGGER.info("consuming messages for Kafka topic " + BLOCKCHAIN_NAME_2);
      topics.add(BLOCKCHAIN_NAME_2);
      Properties props = new Properties();
      props.put("bootstrap.servers", kafkaHostAddresses);
      props.put("group.id", KAFKA_GROUP_ID);
      props.put("key.deserializer", StringDeserializer.class.getName());
      props.put("value.deserializer", ByteArrayDeserializer.class.getName());
      kafkaConsumer = new KafkaConsumer<>(props);
    }

    /**
     * Terminates the consumer loop.
     */
    public void terminate() {
      isDone = true;
      kafkaConsumer.wakeup();
    }

    @Override
    public void run() {
      try {
        LOGGER.info("subscribing to " + topics);
        kafkaConsumer.subscribe(topics);
        while (!isDone) {
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
          ConsumerRecords<String, byte[]> consumerRecords = kafkaConsumer.poll(Long.MAX_VALUE); // timeout
          for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
            LOGGER.info("received consumerRecord " + new Date(consumerRecord.timestamp()));
            final byte[] serializedTEObject = consumerRecord.value();
            final TEObject teObject = (TEObject) Serialization.deserialize(serializedTEObject);
            LOGGER.info("  deserialized teObject " + teObject);
            Serializable payload = teObject.getPayload();
            LOGGER.info("  payload: " + payload);
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

}
