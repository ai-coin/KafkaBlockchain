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
package com.ai_blockchain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author reed
 */
public class SHA256HashTest {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(SHA256HashTest.class);
  // the test instance
  private static SHA256Hash instance;
  // the message digest (hasher)
  private static MessageDigest messageDigest;

  public SHA256HashTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    messageDigest.reset();
    // test a constructor
    final byte[] objectBytes = "this a test object for unit testing that can be converted easily to bytes".getBytes();
    messageDigest.update(objectBytes);
    instance = new SHA256Hash(messageDigest.digest());

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
   * Test hex string constructor of class SHA256Hash.
   */
  @Test
  public void testHexStringConstructor() {
    LOGGER.info("hex string constructor");
    final String hexString = instance.toString();
    assertEquals("71dc21f5d4e287b3d1c40bbe6ff0b6936b43f75559e1acf128bccbf9134da55e", hexString);
    final SHA256Hash constructedSHA256Hash = new SHA256Hash(hexString);
    assertEquals(instance, constructedSHA256Hash);
  }

  /**
   * Test makeSHA256Hash method of class SHA256Hash.
   */
  @Test
  public void testMakeSHA256Hash() {
    LOGGER.info("makeSHA256Hash");
    SHA256Hash instance1 = SHA256Hash.makeSHA256Hash("abc");
    assertEquals("45aeddf736951256e38dcbb5beab9834e6e12be46fd51d1a00ae7bbf8ac67e29", instance1.toString());
    assertEquals("effe2d1b68b4c18beb8f7dc5319b469e6492ec0f4a83eb5514173c66c0f11cb3", SHA256Hash.makeSHA256Hash(14).toString());
  }

  /**
   * Test of hashCode method, of class SHA256Hash.
   */
  @Test
  public void testHashCode() {
    LOGGER.info("hashCode");
    int result = instance.hashCode();
    assertEquals(323855710, result);
  }

  /**
   * Test of equals method, of class SHA256Hash.
   */
  @Test
  public void testEquals() {
    LOGGER.info("equals");
    byte[] otherObjectBytes = "this a test object for unit testing that can be converted easily to bytes".getBytes();
    messageDigest.update(otherObjectBytes);
    SHA256Hash otherSHA256Hash = new SHA256Hash(messageDigest.digest());
    assertEquals(instance, otherSHA256Hash);

    otherObjectBytes = "this a different test object for unit testing that can be converted easily to bytes".getBytes();
    messageDigest.update(otherObjectBytes);
    otherSHA256Hash = new SHA256Hash(messageDigest.digest());
    assertTrue(!instance.equals(otherSHA256Hash));
  }

  /**
   * Test of toString method, of class SHA256Hash.
   */
  @Test
  public void testToString() {
    LOGGER.info("toString");
    assertEquals("71dc21f5d4e287b3d1c40bbe6ff0b6936b43f75559e1acf128bccbf9134da55e", instance.toString());
  }

  /**
   * Test of getBytes method, of class SHA256Hash.
   */
  @Test
  public void testGetBytes() {
    LOGGER.info("getBytes");
    byte[] result = instance.getBytes();
    assertTrue(result.length == SHA256Hash.SHA256_LENGTH);
  }

  /**
   * Test of compareTo method, of class SHA256Hash.
   */
  @Test
  public void testCompareTo() {
    LOGGER.info("compareTo");
    byte[] otherObjectBytes = "this a test object for unit testing that can be converted easily to bytes".getBytes();
    messageDigest.update(otherObjectBytes);
    SHA256Hash otherSHA256Hash = new SHA256Hash(messageDigest.digest());
    assertEquals(0, instance.compareTo(otherSHA256Hash));

    otherObjectBytes = "this a different test object for unit testing that can be converted easily to bytes".getBytes();
    messageDigest.update(otherObjectBytes);
    otherSHA256Hash = new SHA256Hash(messageDigest.digest());
    assertEquals(1, instance.compareTo(otherSHA256Hash));
  }

}
