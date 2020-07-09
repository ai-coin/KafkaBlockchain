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
 * > cd ~/kafka_2.12-2.5.0; bin/zookeeper-server-start.sh config/zookeeper.properties
 * 
 * Launch Kafka in a second terminal session after ZooKeeper initializes.
 * > cd ~/kafka_2.12-2.5.0; bin/kafka-server-start.sh config/server.properties
 *
 * Navigate to this project's directory, and launch the script in a third terminal session which runs the KafkaBlockchain benchmark all on one server.
 * > scripts/run-kafka-blockchain-benchmark.sh
 *
 * After the tests are complete, shut down the Kafka session with Ctrl-C.
 *
 * Shut down the ZooKeeper session with Ctrl-C.
 *
 * </code>
 */
package com.ai_blockchain.kafka_bc.samples;

import com.ai_blockchain.kafka_bc.KafkaAccess;
import com.ai_blockchain.kafka_bc.KafkaBlockchainInfo;
import com.ai_blockchain.kafka_bc.KafkaUtils;
import com.ai_blockchain.kafka_bc.SHA256Hash;
import com.ai_blockchain.kafka_bc.Serialization;
import com.ai_blockchain.kafka_bc.TEObject;
import com.ai_blockchain.kafka_bc.ZooKeeperAccess;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
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
   * Activate Kafka messaging, including a producer and a consumer of tamper-evident payloads.
   */
  public void activateKafkaMessaging() {

    // list Kafka topics as evidence that the Kafka broker is responsive
    final KafkaAccess kafkaAccess = new KafkaAccess(KAFKA_HOST_ADDRESSES);

    LOGGER.info("activating Kafka messaging");
    /**
     * One producer with three partitions.
     */
    kafkaAccess.createTopic(
            BLOCKCHAIN_NAME, // topic
            3, // numPartitions
            (short) 1); // replicationFactor
    LOGGER.info("  Kafka topics " + kafkaAccess.listTopics());

    final Properties props = new Properties();
    props.put("bootstrap.servers", KAFKA_HOST_ADDRESSES);
    props.put("client.id", "TEKafkaProducer");
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", ByteArraySerializer.class.getName());
    kafkaProducer = new KafkaProducer<>(props);
  }

  /**
   * Closes the open resources.
   */
  public void finalization() {
    kafkaProducer.close();
    zooKeeperAccess.close();
  }

  /**
   * Creates ten million demonstration payloads and puts them into a Kafka blockchain.
   */
  public void benchmarkKafkaBlockchain() {
    final int nbrIterations = 10000000;
    LOGGER.info("This benchmark creates a Kafka blockchain which is " + (nbrIterations / 1000000) + " million records in length...");
    final long startTimeMillis = System.currentTimeMillis();
    for (int i = 0; i < nbrIterations; i++) {
      produce(
              new BenchmarkPayload("benchmark payload", i), // payload
              BLOCKCHAIN_NAME); // topic
    }
    final long durationMillis = System.currentTimeMillis() - startTimeMillis;
    final double durationSeconds = durationMillis / 1000.0d;
    final double iterationsPerSecond = nbrIterations / durationSeconds;
    LOGGER.info("nbrIterations       " + nbrIterations);
    LOGGER.info("durationMillis      " + durationMillis);
    LOGGER.info("durationSeconds     " + durationSeconds);
    LOGGER.info("iterationsPerSecond " + iterationsPerSecond);

    final Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", KAFKA_HOST_ADDRESSES);
    consumerProperties.put("group.id", "kafka-benchmark-group");
    consumerProperties.put("key.deserializer", StringDeserializer.class.getName());
    consumerProperties.put("value.deserializer", ByteArrayDeserializer.class.getName());

    final List<TopicPartition> assignedTopicPartitions = KafkaUtils.getAssignedTopicPartitions(
            BLOCKCHAIN_NAME,
            consumerProperties);
    LOGGER.info("the blockchain named " + BLOCKCHAIN_NAME + " has " + assignedTopicPartitions.size() + " partitions");
  }

  /**
   * Wraps the given payload as a tamper-evident object, computes the next blockchain hash and sends the tamper-evident 
   * object to the Kafka broker.
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
      LOGGER.info("genesis hash for " + BLOCKCHAIN_NAME + "=" + dataString);
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
   * Provides a benchmark payload for a Kafka blockchain. It implements Externalizable as a more efficient alternative to the default implementation for
   * Serializable which includes the class definition in the serialized output.
   */
  public static class BenchmarkPayload implements Externalizable {

    // the serialization version universal identifier
    private static final long serialVersionUID = 1L;

    // the demo string data
    private String string;
    // the demo integer data
    private Integer integer;

    /**
     * Constructs a default BenchmarkPayload instance.
     */
    public BenchmarkPayload() {
    }

    /**
     * Constructs a new BenchmarkPayload instance.
     *
     * @param string the demo string data
     * @param integer the demo integer data
     */
    BenchmarkPayload(
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
              .append("[BenchmarkPayload, string=")
              .append(string)
              .append(", integer=")
              .append(integer)
              .append(']')
              .toString();
    }

    /**
     * The object implements the writeExternal method to save its contents by calling the methods of DataOutput for its primitive values or calling the
     * writeObject method of ObjectOutput for objects, strings, and arrays.
     *
     * @serialData Overriding methods should use this tag to describe the data layout of this Externalizable object. List the sequence of element types and, if
     * possible, relate the element to a public/protected field and/or method of this Externalizable class.
     *
     * @param objectOutput the stream to write the object to
     * @exception IOException Includes any I/O exceptions that may occur
     */
    @Override
    public void writeExternal(final ObjectOutput objectOutput) throws IOException {
      //Preconditions
      assert objectOutput != null : "objectOutput must not be null";

      objectOutput.writeUTF(string);
      objectOutput.writeInt(integer);
    }

    /**
     * The object implements the readExternal method to restore its contents by calling the methods of DataInput for primitive types and readObject for objects,
     * strings and arrays. The readExternal method must read the values in the same sequence and with the same types as were written by writeExternal.
     *
     * @param objectInput the stream to read data from in order to restore the object
     * @exception IOException if I/O errors occur
     * @exception ClassNotFoundException If the class for an object being restored cannot be found.
     */
    @Override
    public void readExternal(final ObjectInput objectInput) throws IOException, ClassNotFoundException {
      //Preconditions
      assert objectInput != null : "objectInput must not be null";

      string = objectInput.readUTF();
      integer = objectInput.readInt();
    }
  }
}
