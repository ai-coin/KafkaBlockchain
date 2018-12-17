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
public class KafkaBlockchainInfoTest {
  // the logger
  private static final Logger LOGGER = Logger.getLogger(KafkaBlockchainInfoTest.class);
  
  public KafkaBlockchainInfoTest() {
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
   * Test of getBlockchainName method, of class KafkaBlockchainInfo.
   */
  @Test
  public void testGetBlockchainName() {
    LOGGER.info("getBlockchainName");
    KafkaBlockchainInfo instance = makeKafkaBlockchainInfo();
    assertEquals("test blockchain", instance.getBlockchainName());
  }

  /**
   * Test of getSHA256Hash method, of class KafkaBlockchainInfo.
   */
  @Test
  public void testGetSHA256Hash() {
    LOGGER.info("getSHA256Hash");
    KafkaBlockchainInfo instance = makeKafkaBlockchainInfo();
    assertEquals("45aeddf736951256e38dcbb5beab9834e6e12be46fd51d1a00ae7bbf8ac67e29",  instance.getSHA256Hash().toString());
  }

  /**
   * Test of getSerialNbr method, of class KafkaBlockchainInfo.
   */
  @Test
  public void testGetSerialNbr() {
    LOGGER.info("getSerialNbr");
    KafkaBlockchainInfo instance = makeKafkaBlockchainInfo();
    assertEquals(1234, instance.getSerialNbr());
  }

  /**
   * Test of getTimestamp method, of class KafkaBlockchainInfo.
   */
  @Test
  public void testGetTimestamp() {
    LOGGER.info("getTimestamp");
    KafkaBlockchainInfo instance = makeKafkaBlockchainInfo();
    assertNotNull(instance.getTimestamp());
  }

  /**
   * Test of toString method, of class KafkaBlockchainInfo.
   */
  @Test
  public void testToString() {
    LOGGER.info("toString");
    KafkaBlockchainInfo instance = makeKafkaBlockchainInfo();
    assertTrue(instance.toString().startsWith("[KafkaBlockchainInfo test blockchain, serial 1234, hash45aeddf736951256e38dcbb5beab9834e6e12be46fd51d1a00ae7bbf8ac67e29, timestamp"));
  }

  /**
   * Test of hashCode method, of class KafkaBlockchainInfo.
   */
  @Test
  public void testHashCode() {
    LOGGER.info("hashCode");
    KafkaBlockchainInfo instance = makeKafkaBlockchainInfo();
    assertEquals(-1156026338, instance.hashCode());
  }

  /**
   * Test of equals method, of class KafkaBlockchainInfo.
   */
  @Test
  public void testEquals() {
    LOGGER.info("equals");
    Object obj = makeKafkaBlockchainInfo();
    KafkaBlockchainInfo instance = makeKafkaBlockchainInfo();
    assertEquals(instance, obj);
  }
  
  private static KafkaBlockchainInfo makeKafkaBlockchainInfo() {
    final SHA256Hash sha256Hash = SHA256Hash.makeSHA256Hash("abc");
    return new KafkaBlockchainInfo(
          "test blockchain", // blockchainName
          sha256Hash,
          1234); // serialNbr
  }
  
}
