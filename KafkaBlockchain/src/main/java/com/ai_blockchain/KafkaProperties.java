/*
 * KafkaProperties.java
 *
 * Created on May 25, 2017, 6:46:25 PM
 *
 * Description: Contains constant values for commonly used Kafka properties.
 *
 * Copyright (C) May 25, 2017 by AI Blockchain, all rights reserved.
 *
 */
package com.ai_blockchain;

/**
 *
 * @author reed
 */
public class KafkaProperties {

  public static final int KAFKA_SERVER_PORT = 9092;
  public static final int KAFKA_PRODUCER_BUFFER_SIZE = 64 * 1024;
  public static final int CONNECTION_TIMEOUT_MILLIS = 100000;
  /**
   * Constructs a new KafkaProperties instance.
   */
  private KafkaProperties() {
  }

}
