/*
 * Copyright (C) 2018 by Stephen L. Reed.
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
 * <code>
 * This test performs KafkaBlockchain operations using a test Kafka broker configured per "Kafka Quickstart" https://kafka.apache.org/quickstart .
 * Launch ZooKeeper in a terminal session
 * > cd ~/kafka_2.11-2.1.0; bin/zookeeper-server-start.sh config/zookeeper.properties
 * 
 * Launch Kafka in a second terminal session after ZooKeeper initializes.
 * > cd ~/kafka_2.11-2.1.0; bin/kafka-server-start.sh config/server.properties
 *
 * Navigate to this project's scripts directory, and launch the script in a third terminal session which runs the KafkaBlockchain online tests.
 * > ./run-test-kafka-blockchain.sh
 *
 * After the tests are complete, shut down the Kafka session with Ctrl-C.
 *
 * Shut down the ZooKeeper session with Ctrl-C.
 *
 * </code>
 */
package com.ai_blockchain;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author reed
 */
public class TestKafkaBlockchainOnline implements Callback {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainVerifierOfflineTest.class);
  // the test blockchain message consumer thread
  private static Thread consumerLoopThread;

  // the Kafka producer to which tamper evident messages are sent for logging
  private KafkaProducer<String, byte[]> kafkaProducer;
  // the received test messages
  private static final List<String> RECEIVED_TEST_MESSAGES = new ArrayList<>();
  // the topic which is the test blockchain name
  private static final String TOPIC = "test-topic-2";

  /**
   * Executes this test application.
   *
   * @param args the command line parameters (unused)
   */
  public static void main(final String[] args) {
    final TestKafkaBlockchainOnline testKafkaBlockchainOnline = new TestKafkaBlockchainOnline();
    testKafkaBlockchainOnline.testCreateTopic();
    testKafkaBlockchainOnline.testListTopics();
    try {
      testKafkaBlockchainOnline.test();
    } catch (IOException ex) {
      fail(ex.getMessage());
    }
  }

  /**
   * Test of createTopic method, of class KafkaAccess.
   */
  public void testCreateTopic() {
    LOGGER.info("---------------------------------------------------------------");
    LOGGER.info("createTopic");
    String topic = "test";
    final String kafkaHostAddresses = "localhost:9092";
    LOGGER.info("  kafkaHostAddresses: " + kafkaHostAddresses);
    final KafkaAccess instance = new KafkaAccess(kafkaHostAddresses);
    instance.createTopic(
            topic,
            1, // numPartitions
            (short) 1); // replicationFactor
    final List<String> topics = instance.listTopics();
    LOGGER.info("  list topics=" + topics);
    assertTrue(topics.contains(topic));
    instance.close();
  }

  /**
   * Test of listTopics method, of class KafkaAccess.
   */
  public void testListTopics() {
    LOGGER.info("---------------------------------------------------------------");
    LOGGER.info("listTopics");
    final String kafkaHostAddresses = "localhost:9092";
    LOGGER.info("kafkaHostAddresses: " + kafkaHostAddresses);
    KafkaAccess instance = new KafkaAccess(kafkaHostAddresses);
    final List<String> topics = instance.listTopics();
    Collections.sort(topics);
    LOGGER.info("list topics...");
    for (final String topic : topics) {
      LOGGER.info("  " + topic);
    }
    instance.close();
  }

  public void test() throws IOException {
    LOGGER.info("starting KafkaProducerConsumerTest unit tests...");

    LOGGER.info("creating topic if not present...");
    final KafkaAccess kafkaAccess = new KafkaAccess("localhost:9092");
    kafkaAccess.createTopic(
            TOPIC,
            3, // numPartitions
            (short) 1); // replicationFactor

    LOGGER.info("constructing Kafka data producer...");
    final Properties producerProperties = new Properties();
    producerProperties.put("bootstrap.servers", "localhost:9092");
    //producerProperties.put("client.id", "TEDataKafkaProducer");
    producerProperties.put("key.serializer", StringSerializer.class.getName());
    producerProperties.put("value.serializer", ByteArraySerializer.class.getName());
    producerProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
    kafkaProducer = new KafkaProducer<>(producerProperties);

    final ConsumerLoop consumerLoop = new ConsumerLoop("localhost:9092");
    consumerLoopThread = new Thread(consumerLoop);
    consumerLoopThread.start();
    LOGGER.info("waiting 30 seconds for the test blockchain consumer to initialize ...");
    try {
      Thread.sleep(30_000);
    } catch (InterruptedException ex) {
      // ignore
    }
    produceRecord(
            TOPIC, // topic
            "test-key-1", // key,
            "test data 1".getBytes("UTF-8"), // data
            1); // serialNbr
    produceRecord(
            TOPIC, // topic
            "test-key-2", // key,
            "test data 2".getBytes("UTF-8"), // data
            2); // serialNbr
    produceRecord(
            TOPIC, // topic
            "test-key-3", // key,
            "test data 3".getBytes("UTF-8"), // data
            3); // serialNbr
    LOGGER.info("waiting 5 seconds for the test blockchain consumer to complete processing ...");
    try {
      Thread.sleep(5_000);
    } catch (InterruptedException ex) {
      // ignore
    }
    Collections.sort(RECEIVED_TEST_MESSAGES);
    assertEquals("[test data 1, test data 2, test data 3]", RECEIVED_TEST_MESSAGES.toString());
    kafkaProducer.flush();
    kafkaProducer.close(
            1, // timeout
            TimeUnit.SECONDS);
    consumerLoop.terminate();
  }

  /**
   * Puts the given data and retrieval key into the Kafka blockchain.
   *
   * @param topic the topic
   * @param key the given key used for subsequent retrieval
   * @param data the given data as bytes
   * @param serialNbr the serial number
   *
   * @return the index position of the Kafka blockchain record in the Kafka log file for the given blockchain name
   */
  public long produceRecord(
          final String topic,
          final String key,
          final byte[] data,
          final long serialNbr) {
    //Preconditions
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";
    assert key != null && !key.isEmpty() : "key must be a non-empty string";
    assert data != null : "data must not be null";
    assert data.length > 0 : "data must not be empty";
    assert kafkaProducer != null : "kafkaProducer must not be null";

    final Map<MetricName, ? extends Metric> metricsDictionary = kafkaProducer.metrics();
    LOGGER.debug("metricsDictionary: " + metricsDictionary);

    SHA256Hash previousTEObjectHash = SHA256Hash.makeSHA256Hash(""); // mock value for unit test
    final TEObject teObject = new TEObject(
            data,
            previousTEObjectHash,
            serialNbr);
    previousTEObjectHash = teObject.getTEObjectHash();
    LOGGER.info("previousTEObjectHash " + previousTEObjectHash);
    LOGGER.info("saving Kafka blockchain data teObject " + teObject + ", topic '" + topic + "', key '" + key + "'...");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("saving Kafka blockchain data " + teObject + ", topic '" + topic + "', key '" + key + "'...");
    }
    final byte[] serializedTEObject = Serialization.serialize(teObject);
    // send message with key
    final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
            topic,
            key,
            serializedTEObject); // value
    final Future<RecordMetadata> future = kafkaProducer.send(
            producerRecord,
            this);  // callback
    LOGGER.info("send producer record OK");

    return 0;
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
  public void onCompletion(
          final RecordMetadata metadata,
          final Exception exception) {
    //Preconditions
    assert metadata == null && exception != null || metadata != null && exception == null : "one of the arguments must be non-null, the other null";

    if (metadata == null) {
      LOGGER.info("  onCompletion, metadata: " + metadata);
    } else {
      LOGGER.info("  onCompletion, exception: " + exception);
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
     * Constructs a new ConsumerLoop instance.d
     *
     * @param kafkaHostAddresses the kafka host addresses, such as 172.18.0.2:9092
     */
    public ConsumerLoop(final String kafkaHostAddresses) {
      assert kafkaHostAddresses != null && !kafkaHostAddresses.isEmpty() : "kafkaHostAddresses must be a non-empty string";

      LOGGER.info("consuming inbound messages for Kafka topic " + TOPIC);
      topics.add(TOPIC);
      Properties props = new Properties();
      props.put("bootstrap.servers", kafkaHostAddresses);
      props.put("group.id", "test-consumer-group-id");
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
            LOGGER.info("received consumerRecord " + consumerRecord);
            final byte[] serializedTEObject = consumerRecord.value();
            final TEObject teObject = (TEObject) Serialization.deserialize(serializedTEObject);
            LOGGER.info("  deserialized teObject " + teObject);
            String string = null;
            try {
              string = new String((byte[]) teObject.getPayload(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
              fail(ex.getMessage());
            }
            LOGGER.info("  payload string: " + string);
            synchronized (RECEIVED_TEST_MESSAGES) {
              RECEIVED_TEST_MESSAGES.add(string);
            }
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
