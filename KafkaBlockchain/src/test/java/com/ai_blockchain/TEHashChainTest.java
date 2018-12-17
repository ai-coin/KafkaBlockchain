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

import java.io.Serializable;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author reed
 */
public class TEHashChainTest {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(TEHashChainTest.class);

  public TEHashChainTest() {
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
   * Test of getSHA256Hash method, of class TEHashChain.
   */
  @Test
  public void testGetSHA256Hash() {
    LOGGER.info("getSHA256Hash");
    TEHashChain instance = new TEHashChain();
    SHA256Hash result = instance.getSHA256Hash();
    assertNull(result);
    instance.appendSerializable("test payload 1");
    assertEquals("0eae1eac907785f36f7ebbed5c9111995ede4c84728428eb6659a2166695e234", instance.getSHA256Hash().toString());
  }

  /**
   * Test of appendTEObject method, of class TEHashChain.
   */
  @Test
  public void testAppendTEObject() {
    LOGGER.info("appendTEObject");
    TEObject teObject = new TEObject(
            "test payload 1",
            (SHA256Hash) null,
            1); // serialNbr
    TEHashChain instance = new TEHashChain();
    assertTrue(instance.isValid());
    assertEquals(0, instance.size());
    assertTrue(instance.isEmpty());
    instance.appendTEObject(teObject);
    assertEquals(1, instance.size());
    assertTrue(instance.isValid());
    assertFalse(instance.isEmpty());
  }

  /**
   * Test of appendSerialzable method, of class TEHashChain.
   */
  @Test
  public void testAppendSerialzable() {
    LOGGER.info("appendSerialzable");
    Serializable obj = "test payload 1";
    TEHashChain instance = new TEHashChain();
    assertTrue(instance.isValid());
    assertEquals(0, instance.size());
    assertTrue(instance.isEmpty());
    instance.appendSerializable(obj);
    assertEquals(1, instance.size());
    assertTrue(instance.isValid());
    assertFalse(instance.isEmpty());
  }

  /**
   * Test of getTEObject method, of class TEHashChain.
   */
  @Test
  public void testGetTEObject() {
    LOGGER.info("getTEObject");
    Serializable obj = "test payload 1";
    TEHashChain instance = new TEHashChain();
    instance.appendSerializable(obj);
    int index = 0;
    TEObject result = instance.getTEObject(index);
    assertEquals("test payload 1", result.getPayload());
  }

  /**
   * Test of size method, of class TEHashChain.
   */
  @Test
  public void testSize() {
    LOGGER.info("size");
    TEHashChain instance = new TEHashChain();
    assertEquals(0, instance.size());
    instance.appendSerializable("test payload 1");
    instance.appendSerializable("test payload 2");
    instance.appendSerializable("test payload 3");
    instance.appendSerializable("test payload 4");
    instance.appendSerializable("test payload 5");
    instance.appendSerializable("test payload 6");
    instance.appendSerializable("test payload 7");
    instance.appendSerializable("test payload 8");
    instance.appendSerializable("test payload 9");
    instance.appendSerializable("test payload 10");
    assertEquals(10, instance.size());
    assertTrue(instance.isValid());
  }

  /**
   * Test of hashCode method, of class TEHashChain.
   */
  @Test
  public void testHashCode() {
    LOGGER.info("hashCode");
    TEHashChain instance = new TEHashChain();
    assertEquals(202, instance.hashCode());
  }

  /**
   * Test of equals method, of class TEHashChain.
   */
  @Test
  public void testEquals() {
    LOGGER.info("equals");
    TEHashChain instance = new TEHashChain();
    instance.appendSerializable("test payload 1");
    instance.appendSerializable("test payload 2");
    instance.appendSerializable("test payload 3");

    TEHashChain other = new TEHashChain();
    other.appendSerializable("test payload 1");
    other.appendSerializable("test payload 2");
    other.appendSerializable("test payload 3");
    assertEquals(instance, other);

    assertFalse(instance.equals((Object) null));
    assertFalse(instance.equals(1));
    other.appendSerializable("test payload 4");
    assertFalse(instance.equals(other));
  }

  /**
   * Test of toString method, of class TEHashChain.
   */
  @Test
  public void testToString() {
    LOGGER.info("toString");
    TEHashChain instance = new TEHashChain();
    instance.appendSerializable("test payload 1");
    instance.appendSerializable("test payload 2");
    instance.appendSerializable("test payload 3");
    assertEquals("[TEHashChain size 3]", instance.toString());
  }

  /**
   * Test of isValid method, of class TEHashChain.
   */
  @Test
  public void testIsValid() {
    LOGGER.info("isValid");
    TEHashChain instance = new TEHashChain();
    assertTrue(instance.isValid());

    instance.appendSerializable("test payload 1");
    assertTrue(instance.isValid());
    assertTrue(instance.isValid(0));

    instance.appendSerializable("test payload 2");
    assertTrue(instance.isValid());
    assertTrue(instance.isValid(0));

    instance.appendSerializable("test payload 3");
    assertTrue(instance.isValid());
    assertTrue(instance.isValid(0));
    assertTrue(instance.isValid(1));
    assertTrue(instance.isValid(2));
    try {
      instance.isValid(3);
      fail();
    } catch (Throwable throwable) {
      // ignore
    }
  }

}
