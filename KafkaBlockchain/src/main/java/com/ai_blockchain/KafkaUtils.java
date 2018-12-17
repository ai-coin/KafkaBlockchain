/**
 * KafkaUtils.java
 *
 * Created on Jan 21, 2018, 8:15:15 PM
 *
 * Description: .
 *
 * Copyright (C) Jan 21, 2018 Stephen L. Reed.
 */
package com.ai_blockchain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;

public class KafkaUtils {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaUtils.class);

  /**
   * Prevents the construction of this utility class having only static methods.
   */
  private KafkaUtils() {
  }

  /**
   * Gets the previous blockchain tip hash and serial number from Kafka.
   *
   * @param topicPrefix the optional topic prefix, such as "blockchain_"
   * @param blockchainName the blockchain name, such as "test-blockchain-001"
   * @param kafkaHostAddresses the kafka host addresses, such as "172.18.0.3:9092"
   *
   * @return the Kafka blockchain info, or null if not found
   */
  public static KafkaBlockchainInfo getKafkaBlockchainInfo(
          final String topicPrefix,
          final String blockchainName,
          final String kafkaHostAddresses) {
    //Preconditions
    assert blockchainName != null && !blockchainName.isEmpty() : "blockchainName must be a non-empty string";
    assert kafkaHostAddresses != null && !kafkaHostAddresses.isEmpty() : "kafkaIPAddress must be a non-empty string";

    LOGGER.info("getPreviousSHA256Hash, blockchainName: " + blockchainName);
    final Properties consumerProperties = new Properties();
    consumerProperties.put("bootstrap.servers", kafkaHostAddresses);
    consumerProperties.put("group.id", "get most recent hash-chained data message");
    consumerProperties.put("key.deserializer", StringDeserializer.class.getName());
    consumerProperties.put("value.deserializer", ByteArrayDeserializer.class.getName());
    final KafkaConsumer kafkaConsumer = new KafkaConsumer<>(consumerProperties);

    final Collection<String> topics = new ArrayList<>();
    final String topic = topicPrefix + blockchainName;
    topics.add(topic);
    // subscribe to the topic, then poll to trigger Kafka's lazy caching of the topic
    kafkaConsumer.subscribe(topics);
    kafkaConsumer.poll(100); // timeout
    // get the assigned partitions for this topic, which for this application will be every partition for the topic
    final Collection<TopicPartition> assignedTopicPartitions = kafkaConsumer.assignment();
    LOGGER.info("assignedTopicPartitions: " + assignedTopicPartitions);
    final Map<TopicPartition, Long> offsetDictionary = kafkaConsumer.endOffsets(assignedTopicPartitions);
    LOGGER.info("offsetDictionary: " + offsetDictionary);
    for (final Map.Entry<TopicPartition, Long> entry : offsetDictionary.entrySet()) {
      // seek to the last written record offset for each of the assigned partitions for this topic
      final TopicPartition topicPartition = entry.getKey();
      final long offset = entry.getValue() - 1;
      if (offset > 0) {
        LOGGER.info("seeking topicPartition: " + topicPartition + ", at offset " + offset);
        kafkaConsumer.seek(topicPartition, offset);
      } else {
        LOGGER.info("found initialized topic: " + topicPartition + ", at offset " + offset);
        return null;
      }
    }
    // read the most recent consumer record, deserialize the tamper evident object, and return its SHA hash value.
    final ConsumerRecords consumerRecords = kafkaConsumer.poll(Long.MAX_VALUE); // timeout
    kafkaConsumer.commitAsync();
    final Iterator<ConsumerRecord<String, byte[]>> consumerRecord_iter = consumerRecords.records(topic).iterator();
    SHA256Hash previousSHA256Hash;
    long serialNbr;
    if (consumerRecord_iter.hasNext()) {
      final ConsumerRecord<String, byte[]> consumerRecord = consumerRecord_iter.next();
      LOGGER.info("consumerRecord: " + consumerRecord);
      final byte[] serializedTEObject = consumerRecord.value();
      try {
        final TEObject teObject = (TEObject) Serialization.deserialize(serializedTEObject);
        LOGGER.info("  deserialized teObject " + teObject);
        previousSHA256Hash = teObject.getTEObjectHash();
        serialNbr = teObject.getSerialNbr();
        LOGGER.info("  previousSHA256Hash " + previousSHA256Hash);
      } catch (Exception ex) {
        LOGGER.info("  obsolete records found for " + topic);
        previousSHA256Hash = null;
        serialNbr = 0;
      }
    } else {
      LOGGER.warn("  no records found for " + topic);
      previousSHA256Hash = null;
      serialNbr = 0;
    }
    kafkaConsumer.unsubscribe();
    kafkaConsumer.close(
            10, // timeout
            TimeUnit.SECONDS);
    if (previousSHA256Hash == null) {
      return null;
    }
    final KafkaBlockchainInfo kafkaBlockchainInfo = new KafkaBlockchainInfo(
            blockchainName,
            previousSHA256Hash,
            serialNbr);
    return kafkaBlockchainInfo;
  }
}
