/**
 * KafkaUtils.java
 *
 * Created on Jan 21, 2018, 8:15:15 PM
 *
 * Description: Kafka blockchain utility methods.
 *
 * Copyright (C) Jan 21, 2018 Stephen L. Reed.
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
 */
package com.ai_blockchain.kafka_bc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
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
   * Gets the topic partitions for the given topic as a list.
   *
   * @param topic the topic (blockchain name)
   * @param consumerProperties the Kafka consumer properties
   * @return the topic partitions for the given topic
   */
  public static List<TopicPartition>  getAssignedTopicPartitions(
          final String topic,
          final Properties consumerProperties) {
    //Preconditions
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";
    assert consumerProperties != null : "consumerProperties must not be null";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getNbrPartitionsForTopic, topic: " + topic);
    }
    final KafkaConsumer kafkaConsumer = new KafkaConsumer<>(consumerProperties);
    LOGGER.info("getNbrPartitionsForTopic, topic: " + topic);

    final Collection<String> topics = new ArrayList<>();
    topics.add(topic);
    // subscribe to the topic, then poll to trigger Kafka's lazy caching of the topic
    kafkaConsumer.subscribe(topics);
    kafkaConsumer.poll(100); // timeout
    // get the assigned partitions for this topic
    final List<TopicPartition> assignedTopicPartitions = new ArrayList<>();
    assignedTopicPartitions.addAll(kafkaConsumer.assignment());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("assignedTopicPartitions: " + assignedTopicPartitions);
    }
    kafkaConsumer.unsubscribe();
    kafkaConsumer.close(
            10, // timeout
            TimeUnit.SECONDS);
    return assignedTopicPartitions;
  }

  /**
   * Gets the previous blockchain tip hash and serial number from Kafka, for the given topic.
   *
   * @param topic the topic (blockchain name)
   * @param consumerProperties the Kafka consumer properties
   *
   * @return the Kafka blockchain info, or null if not found
   */
  public static KafkaBlockchainInfo getKafkaTopicInfo(
          final String topic,
          final Properties consumerProperties) {
    //Preconditions
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";
    assert consumerProperties != null : "consumerProperties must not be null";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getKafkaTopicInfo, topic: " + topic);
    }
    final KafkaConsumer kafkaConsumer = new KafkaConsumer<>(consumerProperties);

    final Collection<String> topics = new ArrayList<>();
    topics.add(topic);
    // subscribe to the topic, then poll to trigger Kafka's lazy caching of the topic
    kafkaConsumer.subscribe(topics);
    kafkaConsumer.poll(100); // timeout
    // get the assigned partitions for this topic, which for this application will be every partition for the topic
    final Collection<TopicPartition> assignedTopicPartitions = kafkaConsumer.assignment();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("assignedTopicPartitions: " + assignedTopicPartitions);
    }
    final Map<TopicPartition, Long> offsetDictionary = kafkaConsumer.endOffsets(assignedTopicPartitions);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("offsetDictionary: " + offsetDictionary);
    }
    for (final Map.Entry<TopicPartition, Long> entry : offsetDictionary.entrySet()) {
      // seek to the last written record offset for each of the assigned partitions for this topic
      final TopicPartition topicPartition = entry.getKey();
      final long offset;
      if (entry.getValue() > 0) {
        offset = entry.getValue() - 1;
      } else {
        offset = 0;
      }

      LOGGER.debug("seeking topicPartition: " + topicPartition + ", at offset " + offset);
      kafkaConsumer.seek(topicPartition, offset);
    }
    // read the most recent consumer record, deserialize the tamper evident object, and return its SHA hash value.
    final ConsumerRecords consumerRecords = kafkaConsumer.poll(100); // timeout
    kafkaConsumer.commitAsync();
    final Iterator<ConsumerRecord<String, byte[]>> consumerRecord_iter = consumerRecords.records(topic).iterator();
    final SHA256Hash previousSHA256Hash;
    final long serialNbr;
    if (consumerRecord_iter.hasNext()) {
      final ConsumerRecord<String, byte[]> consumerRecord = consumerRecord_iter.next();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("consumerRecord: " + consumerRecord);
      }
      final byte[] serializedTEObject = consumerRecord.value();
      TEObject teObject;
      try {
        teObject = (TEObject) Serialization.deserialize(serializedTEObject);
      } catch (Exception ex) {
        teObject = null;
      }
      if (teObject == null) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("  invalid record found for " + topic);
        }
        previousSHA256Hash = null;
        serialNbr = 0;
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("  deserialized teObject " + teObject);
        }
        previousSHA256Hash = teObject.getTEObjectHash();
        serialNbr = teObject.getSerialNbr();
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("  previousSHA256Hash " + previousSHA256Hash);
        }
      }
    } else {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("  no records found for " + topic);
      }
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
            topic,
            previousSHA256Hash,
            serialNbr);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("  kafkaBlockchainInfo " + kafkaBlockchainInfo);
    }
    return kafkaBlockchainInfo;
  }

  /**
   * Positions the given KafkaConsumer at the beginning of its partitions for the given topic.
   *
   * @param kafkaConsumer the given Kafka consumer
   * @param topic the topic
   */
  public static void seekToBeginning(
          final KafkaConsumer kafkaConsumer,
          final String topic) {
    //Preconditions
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";
    assert kafkaConsumer != null : "kafkaConsumer must not be null";

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getKafkaTopicInfo, topic: " + topic);
    }

    final Collection<String> topics = new ArrayList<>();
    topics.add(topic);
    // subscribe to the topic, then poll to trigger Kafka's lazy caching of the topic
    kafkaConsumer.subscribe(topics);
    kafkaConsumer.poll(100); // timeout
    // get the assigned partitions for this topic, which may not be all of them if there are other consumers existing in the group
    final Collection<TopicPartition> assignedTopicPartitions = kafkaConsumer.assignment();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("assignedTopicPartitions: " + assignedTopicPartitions);
    }
    kafkaConsumer.seekToBeginning(assignedTopicPartitions);
  }

}
