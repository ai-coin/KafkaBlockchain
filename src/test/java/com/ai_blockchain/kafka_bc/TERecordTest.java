/**
 * Copyright (C) December 17, 2018 by AI Blockchain, all rights reserved.
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

import java.io.IOException;
import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author reed
 */
public class TERecordTest {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(TERecordTest.class);
  // the hash chain
  static TEHashChain teHashChain = new TEHashChain();
  static TERecord teRecord1;
  static TERecord instance;
  static TERecord teRecord3;

  public TERecordTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    // create three tamper evident objects in a hash chain
    teRecord1 = new TERecord(
            "test payload 1", // payload
            null, // previousTERecord
            1); // serialNbr
    teHashChain.appendTEObject(teRecord1);

    instance = new TERecord(
            "test payload 2", // payload
            teRecord1, // previousTERecord
            2); // serialNbr
    teHashChain.appendTEObject(instance);

    teRecord3 = new TERecord(
            "test payload 3", // payload
            instance, // previousTERecord
            3); // serialNumber
    teHashChain.appendSerializable("test payload 3");
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
   * Test of getPayloadBytes method, of class TERecord.
   */
  @Test
  public void testGetPayloadBytes() {
    LOGGER.info("getPayloadBytes");
    byte[] result = instance.getPayloadBytes();
    byte[] expectedResult = Serialization.serialize("test payload 2");
    assertTrue(Arrays.areEqual(expectedResult, result));
  }

  /**
   * Test of getPreviousHash method, of class TERecord.
   */
  @Test
  public void testGetPreviousTERecordHash() {
    LOGGER.info("getPreviousTERecordHash");
    SHA256Hash result = instance.getPreviousHash();
    assertEquals("0eae1eac907785f36f7ebbed5c9111995ede4c84728428eb6659a2166695e234", result.toString());
  }

  /**
   * Test of getTeObjectHash method, of class TERecord.
   */
  @Test
  public void testGetTeObjectHash() {
    LOGGER.info("getTeObjectHash");
    SHA256Hash result = instance.getTERecordHash();
    assertEquals("aa415219a530324cb51ba93d01a0654cbcd30f487d1c3fbc1ea65496ce4ea3b3", result.toString());
  }


  /**
   * Test of isValidSuccessor method, of class TERecord.
   */
  @Test
  public void testIsValidSuccessor() {
    LOGGER.info("isValidSuccessor");
    assertTrue(instance.isValidSuccessor(teRecord1));
    assertFalse(instance.isValidSuccessor(instance));
    assertFalse(instance.isValidSuccessor(teRecord3));
  }

  /**
   * Test of hashCode method, of class TERecord.
   */
  @Test
  public void testHashCode() {
    LOGGER.info("hashCode");
    assertEquals(-147191931, teRecord1.hashCode());
  }

  /**
   * Test of toString method, of class TERecord.
   */
  @Test
  public void testToString() {
    LOGGER.info("toString");
    assertEquals("[TERecord 1, wrapping a payload of 21 bytes]", teRecord1.toString());
  }

  public void testTERecord() throws IOException {
    LOGGER.info("testTERecord");
    final String testData1 = "test data 1";
    final byte[] testData1Bytes = testData1.getBytes("UTF-8");
    final String testData1BytesHex = Hex.toHexString(testData1Bytes);
    assertEquals("7465737420646174612031", testData1BytesHex);
    final byte[] serializedTestData1Bytes = Hex.decode(testData1BytesHex);
    final String deserializedTestData1 = new String(serializedTestData1Bytes, "UTF-8");
    assertEquals("test data 1", deserializedTestData1);
    assertEquals(testData1, deserializedTestData1);
    SHA256Hash previousTERecordHash = SHA256Hash.makeSHA256Hash(""); // mock value for unit test
    final TERecord teObject = new TERecord(
            testData1Bytes,
            previousTERecordHash,
            1);
    final byte[] payload = (byte[]) teObject.getPayload();
    final String deserializedPayload = new String(payload, "UTF-8");
    assertEquals("test data 1", deserializedPayload);

  }
}