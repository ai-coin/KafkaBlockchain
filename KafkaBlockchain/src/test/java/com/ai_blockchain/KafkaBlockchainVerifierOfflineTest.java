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
package com.ai_blockchain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author reed
 */
public class KafkaBlockchainVerifierOfflineTest {
  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainVerifierOfflineTest.class);
  
  public KafkaBlockchainVerifierOfflineTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of verifyKafkaBlockchain method, of class KafkaBlockchainVerifier.
   */
  @Test
  public void testVerifyKafkaBlockchain() {
    LOGGER.info("verifyKafkaBlockchain");
    Collection<String> blockchainNames = new ArrayList<>();
    KafkaBlockchainVerifier instance = new KafkaBlockchainVerifier();
    Map result = instance.verifyKafkaBlockchain(blockchainNames);
    assertEquals("{}", result.toString());
  }

  /**
   * Test of getKafkaHostAddresses method, of class KafkaBlockchainVerifier.
   */
  @Test
  public void testGetKafkaHostAddresses() {
    LOGGER.info("getKafkaHostAddresses");
    KafkaBlockchainVerifier instance = new KafkaBlockchainVerifier();
    assertEquals("kafka:9092", instance.getKafkaHostAddresses());
  }
  
}
