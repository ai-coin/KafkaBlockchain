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
 */
package com.ai_blockchain.kafka_bc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.log4j.Logger;

/**
 * KafkaAccess.java
 *
 * Description: Provides Kafka access utilities, notably the ability to create a topic.
 *
 * Copyright (C) Jan 8, 2018, Stephen L. Reed.
 */
public class KafkaAccess {

  // the logger
  private final static Logger LOGGER = Logger.getLogger(KafkaAccess.class);
  // the admin client configuration properties
  private final Properties properties;
  // the admin client
  private final AdminClient adminClient;

  /**
   * Constructs a new KafkaAccess instance.
   *
   * @param kafkaHostAddresses the Kafka host addresses, such as 172.18.0.3:9092
   */
  public KafkaAccess(final String kafkaHostAddresses) {
    //Preconditions
    assert kafkaHostAddresses != null && !kafkaHostAddresses.isEmpty() : "kafkaHostAddresses must be a non-empty string";

    properties = new Properties();
    properties.put("bootstrap.servers", kafkaHostAddresses);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("KafkaAccess bootstrap servers: " + kafkaHostAddresses);
    }
    // the admin client
    adminClient = AdminClient.create(properties);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("KafkaAccess known topics...");
    }
    adminClient.listTopics();
  }

  /**
   * Constructs a new KafkaAccess instance using the provided connection properties.
   *
   * @param properties the provided properties, for example Confluent Cloud credentials and bootstrap URL
   */
  public KafkaAccess(final Properties properties) {
    //Preconditions
    assert properties != null : "properties must not be null";

    this.properties = properties;

    // the admin client
    adminClient = AdminClient.create(properties);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("KafkaAccess known topics...");
    }
    adminClient.listTopics();
  }

  /**
   * Returns whether Kafka is running locally by attempting to connect.
   *
   * @return whether Kafka is running
   */
  public static boolean isKafkaRunning() {
    final Properties properties = new Properties();
    properties.put("bootstrap.servers", "127.0.0.1:9092");
    properties.put("connections.max.idle.ms", 10000);
    properties.put("request.timeout.ms", 5000);
    try (final AdminClient adminClient = KafkaAdminClient.create(properties)) {
      final ListTopicsResult topics = adminClient.listTopics();
      final Set<String> names = topics.names().get();
      if (names.isEmpty()) {
        // case: if no topic found.
      }
      return true;
    } catch (InterruptedException | ExecutionException e) {
      return false;
    }
  }

  /**
   * Creates the given topic.
   *
   * @param topic the given topic
   * @param numPartitions number of partitions
   * @param replicationFactor the replication factor
   */
  public void createTopic(
          final String topic,
          final int numPartitions,
          final short replicationFactor) {
    //Preconditions
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";
    assert adminClient != null : "adminClient must not be null";

    final List<NewTopic> topics = new ArrayList<>();
    final NewTopic newTopic = new NewTopic(
            topic, // name
            numPartitions,
            replicationFactor);
    topics.add(newTopic);
    final CreateTopicsResult createTopicsResult = adminClient.createTopics(topics);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("  createTopicsResult topics=" + createTopicsResult.values().keySet());
    }

    //Postconditions
    assert createTopicsResult.values().keySet().size() == 1;
    final String key = createTopicsResult.values().keySet().iterator().next();
    assert key != null && !key.isEmpty() : "key must be a non-empty string";
    assert key.equals(topic);
  }

  /**
   * Deletes the given topic.
   *
   * @param topic the given topic
   */
  public void deleteTopic(final String topic) {
    //Preconditions
    assert topic != null && !topic.isEmpty() : "topic must be a non-empty string";
    assert adminClient != null : "adminClient must not be null";

    final List<String> topics = new ArrayList<>();
    topics.add(topic);
    final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("  deleteTopicsResult topics=" + deleteTopicsResult.values().keySet());
    }

    //Postconditions
    assert deleteTopicsResult.values().keySet().size() == 1;
    final String key = deleteTopicsResult.values().keySet().iterator().next();
    assert key != null && !key.isEmpty() : "key must be a non-empty string";
    assert key.equals(topic);
  }

  /**
   * List the topics available in the cluster with the default options.
   *
   * @return the topics available in the cluster
   */
  public List<String> listTopics() {
    //Preconditions
    assert adminClient != null : "adminClient must not be null";

    final ListTopicsResult listTopicsResult = adminClient.listTopics();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("  listTopicsResult=" + listTopicsResult);
    }
    final KafkaFuture<Set<String>> kafkaFutureNames = listTopicsResult.names();
    final Set<String> names;
    try {
      names = kafkaFutureNames.get(10, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException ex) {
      throw new RuntimeException(ex);
    }
    final List<String> topics = new ArrayList<>();
    topics.addAll(names);
    Collections.sort(topics);
    return topics;
  }

  /**
   * Closes this object and releases its resources.
   *
   */
  public void close() {
    adminClient.close();
  }
}
