/*
 * KafkaBlockchainVerifier.java
 *
 * Created on Mar 14, 2012, 10:49:55 PM
 *
 * Description: Verifies the behavior of the KafkaBlockchainManager skill.
 *
 * Copyright (C) Jan 18, 2018, Stephen L. Reed, AI Coin, Inc.
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
package com.ai_blockchain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public class KafkaBlockchainVerifier {

  // the log4j logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainVerifier.class);
  // the blockchain verification error status
  public static final int ERROR_STATUS = 100;

  /**
   * Constructs a new KafkaBlockchainVerifier instance.
   */
  public KafkaBlockchainVerifier() {
  }

  /**
   * Verify the given blockchains.
   *
   * @param blockchainNames the collection of blockchain names
   * @return the verification results dictionary, blockchain name to results array, result1 is zero if OK, otherwise 100 and result2 is the error message string
   *
   */
  public Map<String, Object[]> verifyKafkaBlockchain(final Collection<String> blockchainNames) {
    //Preconditions
    assert blockchainNames != null : "blockchainNames must not be null";

    LOGGER.info("verifying Kafka blockchains " + blockchainNames);
    final Map<String, Object[]> verificationResultsDictionary = new HashMap<>();
    
    
    for (final String blockchainName : blockchainNames) {
      assert blockchainName != null && !blockchainName.isEmpty();
      LOGGER.info("verifying Kafka blockchain named " + blockchainName + "...");

      // consume the given blockchain topic from its beginning, and verify the chain and the hash of each tamper-evident object
      //
      final KafkaConsumer<String, byte[]> kafkaConsumer;
      final List<String> topics = new ArrayList<>();
      final String topic = blockchainName;
      // the topics, which has only one topic, which is formed from the given blockchain name
      topics.add(topic);
      LOGGER.info("  topics: " + topics);
      final Properties properties = new Properties();
      properties.put("bootstrap.servers", getKafkaHostAddresses());
      properties.put("group.id", "verifying consumer of hash-chained data messages");
      properties.put("key.deserializer", StringDeserializer.class.getName());
      properties.put("value.deserializer", ByteArrayDeserializer.class.getName());
      kafkaConsumer = new KafkaConsumer<>(properties);
      LOGGER.info("  subscribing to " + topics);
      kafkaConsumer.subscribe(topics);
      kafkaConsumer.poll(100); // timeout
      kafkaConsumer.seekToBeginning(new ArrayList<>());
      TEObject previousTEObject = null;
      int status = 0;
      String description = null;
      try {
        while (true) {
          ConsumerRecords<String, byte[]> consumerRecords = kafkaConsumer.poll(1_000);
          if (consumerRecords.isEmpty()) {
            break;
          }
          for (ConsumerRecord<String, byte[]> consumerRecord : consumerRecords) {
            if (LOGGER.isDebugEnabled()) {
              LOGGER.info("  received consumerRecord " + consumerRecord);
            }
            final byte[] serializedTEObject = consumerRecord.value();
            final TEObject teObject;
            try {
              teObject = (TEObject) Serialization.deserialize(serializedTEObject);
            } catch (Exception ex) {
              status = ERROR_STATUS;
              description = ex.getMessage();
              break;
            }
            LOGGER.info("  deserialized teObject " + teObject);
            if (previousTEObject == null) {
              LOGGER.info("    processing first one");
            } else {
              final boolean isValidSuccessor = teObject.isValidSuccessor(previousTEObject);
              LOGGER.info("    isValidSuccessor: " + isValidSuccessor + " of " + previousTEObject);
            }
            previousTEObject = teObject;

          }
          kafkaConsumer.commitAsync();
        }
      } catch (WakeupException ex) {
        // ignore for shutdown
      } finally {
        kafkaConsumer.close();
      }
      final Object[] results = {status, description};
      verificationResultsDictionary.put(blockchainName, results);
    }
    return verificationResultsDictionary;
  }

  /**
   * Gets the production Kafka host IP address . The unit test mock class overrides this method because the test script
   * does not originate from within a container, as would otherwise might be the case in production where everything could run in a
   * container.
   *
   * @return the production Kafka host IP address
   */
  protected String getKafkaHostAddresses() {
    return "kafka:9092";
  }

}
