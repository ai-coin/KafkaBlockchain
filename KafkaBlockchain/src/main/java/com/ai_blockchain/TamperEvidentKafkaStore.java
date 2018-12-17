/**
 * TamperEvidentKafkaStore.java
 *
 * Created on Nov 29, 2017, 1:19:12 AM
 *
 * Description: Provides a tamper-evident store for binary objects looked by by a string valued key.
 *
 * Copyright (C) Nov 29, 2017 Stephen L. Reed.
 */
package com.ai_blockchain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.log4j.Logger;

public class TamperEvidentKafkaStore {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(TamperEvidentKafkaStore.class);
  // the persistent Kafka record locator dictionary, key --> Kafka record locator
  //TODO use ECHACHE
  private final Map<String, KafkaRecordLocator> kafkaRecordLocatorDictionary = new HashMap<>();

  /**
   * Constructs a new TamperEvidentJavaClassStore instance.
   */
  public TamperEvidentKafkaStore() {
  }

  static class KafkaRecordLocator implements Serializable {

    // the key
    private final String key;
    // the Kafka topic
    private final String topic;
    // the Kafka partition of the topic
    private final int partition;
    // the Kafka offset within the partition
    private final long offset;

    KafkaRecordLocator(
            final String key,
            final String topic,
            final int partition,
            final long offset) {
      //Preconditions
      assert key != null && !key.isEmpty() : "key must be a non-empty string";
      assert topic != null && !topic.isEmpty() : "kafkaIPAddress must be a non-empty string";
      assert partition >= 0 : "partition must not be negative";
      assert offset >= 0 : "offset must not be negative";

      this.key = key;
      this.topic = topic;
      this.partition = partition;
      this.offset = offset;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
      return new StringBuilder()
              .append("[KafkaRecordLocator for ")
              .append(key)
              .append(", topic: ")
              .append(topic)
              .append(", partition: ")
              .append(partition)
              .append(", offset: ")
              .append(offset)
              .append("]")
              .toString();
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code for this object
     */
    @Override
    public int hashCode() {
      int hash = 5;
      hash = 23 * hash + Objects.hashCode(this.key);
      return hash;
    }

    /**
     * Returns whether the given object equals this object.
     *
     * @param obj the given object
     * @return whether the given object equals this object
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final KafkaRecordLocator other = (KafkaRecordLocator) obj;
      if (this.partition != other.partition) {
        return false;
      }
      if (this.offset != other.offset) {
        return false;
      }
      if (!Objects.equals(this.key, other.key)) {
        return false;
      }
      return Objects.equals(this.topic, other.topic);
    }

  }
  //TODO method to populate the store from reading the staging server class files and producing Kafka records

  //TODO method to get class bytes for a given class name by looking up the Kafka locator and using that to retrieve the class bytes
  //TODO unit tests, and class loader set up by the node runtime
}
