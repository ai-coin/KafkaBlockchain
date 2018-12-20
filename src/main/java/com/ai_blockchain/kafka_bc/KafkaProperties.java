/*
 * KafkaProperties.java
 *
 * Created on May 25, 2017, 6:46:25 PM
 *
 * Description: Contains constant values for commonly used Kafka properties.
 *
 * Copyright (C) May 25, 2017 by AI Blockchain, all rights reserved.
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
 */
package com.ai_blockchain.kafka_bc;

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
