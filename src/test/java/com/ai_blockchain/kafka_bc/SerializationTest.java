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

import com.ai_blockchain.kafka_bc.ByteUtils;
import com.ai_blockchain.kafka_bc.Serialization;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author reed
 */
public class SerializationTest {

  /**
   * the logger
   */
  private static final Logger LOGGER = Logger.getLogger(SerializationTest.class);

  public SerializationTest() {
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
   * Test of serialize method, of class Serialization.
   */
  @Test
  public void testSerialize() {
    LOGGER.info("serialize");
    byte[] result = Serialization.serialize("abc");
    Assert.assertEquals(10, result.length);
    byte[] expectedResult = {-84, -19, 0, 5, 116, 0, 3, 97, 98, 99};
    assertTrue(ByteUtils.areEqual(expectedResult, result));
  }

  /**
   * Test of deserialize method, of class Serialization.
   */
  @Test
  public void testDeserialize() {
    LOGGER.info("deserialize");
    byte[] bytes = {-84, -19, 0, 5, 116, 0, 3, 97, 98, 99};
    Assert.assertEquals("abc", Serialization.deserialize(bytes).toString());

    final TestObject testObject = makeTestObject();
    LOGGER.info("original:                    " + testObject);
    Assert.assertEquals(
            "[TestObject 4, instance d, nested "
            + "[TestObject 3, instance c, nested "
            + "[TestObject 2, instance b, nested "
            + "[TestObject 1, instance a]]]]",
            testObject.toString());
    byte[] testClassInstanceBytes = Serialization.serialize(testObject);
    final Object deserializedObject = Serialization.deserialize(testClassInstanceBytes);
    LOGGER.info("serialized and deserialized: " + deserializedObject);
    Assert.assertEquals(
            "[TestObject 4, instance d, nested "
            + "[TestObject 3, instance c, nested "
            + "[TestObject 2, instance b, nested "
            + "[TestObject 1, instance a]]]]",
            deserializedObject.toString());
  }

  /**
   * Makes a nested test object for unit testing serialization.
   */
  TestObject makeTestObject() {
    final TestObject testClass1 = new TestObject(
            1, // id
            "instance a", // description
            null); // nestedTestObject
    final TestObject testClass2 = new TestObject(
            2, // id
            "instance b", // description
            testClass1); // nestedTestObject
    final TestObject testClass3 = new TestObject(
            3, // id
            "instance c", // description
            testClass2); // nestedTestObject
    final TestObject testClass4 = new TestObject(
            4, // id
            "instance d", // description
            testClass3); // nestedTestObject
    return testClass4;
  }

  /**
   * Provides a test object for serialization.
   */
  static class TestObject implements Serializable {

    // the identification number
    private final int id;
    // the description
    private final String description;
    // the nested test object or null if none
    private final TestObject nestedTestObject;

    /**
     * Constructs a new TestClass instance.
     *
     * @param id the identification number
     * @param description the description
     * @param nestedTestClass the nested test object or null if none
     */
    TestObject(final int id,
            final String description,
            final TestObject nestedTestClass) {
      //Preconditions
      assert description != null && !description.isEmpty() : "description must be a non-empty string";

      this.id = id;
      this.description = description;
      this.nestedTestObject = nestedTestClass;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
      if (nestedTestObject == null) {
        return "[TestObject " + id + ", " + description + "]";
      } else {
        return "[TestObject " + id + ", " + description + ", nested " + nestedTestObject + "]";
      }
    }
  }
}
