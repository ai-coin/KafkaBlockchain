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
import java.util.Arrays;
import java.util.UUID;
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
public class ByteUtilsTest {

  /**
   * the logger
   */
  private static final Logger LOGGER = Logger.getLogger(ByteUtilsTest.class);

  public ByteUtilsTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of to8BitString and from8BitString methods, of class ByteUtils.
   */
  @Test
  public void testTo8BitString() {
    LOGGER.info("to8BitString");
    byte[] bytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10};
    assertEquals(21, bytes.length);
    String string = ByteUtils.to8BitString(bytes);
    assertEquals(21, string.length());
    byte[] bytes2 = ByteUtils.from8BitString(string);
    assertTrue(ByteUtils.areEqual(bytes, bytes2));
  }

  /**
   * Test of toUnsignedByte method, of class ByteUtils.
   */
  @Test
  public void testToUnsignedByte() {
    LOGGER.info("toUnsignedByte");
    assertEquals(0, ByteUtils.toUnsignedByte(0));
    assertEquals(1, ByteUtils.toUnsignedByte(1));
    assertEquals(-1, ByteUtils.toUnsignedByte(255));
    assertEquals(0, ByteUtils.toUnsignedByte(256));
    assertEquals(-1, ByteUtils.toUnsignedByte(-1));
  }

  /**
   * Test of from2HexDigits method, of class ByteUtils.
   */
  @Test
  public void testFrom2HexDigits() {
    LOGGER.info("from2HexDigits");
    assertEquals(0, ByteUtils.fromHex('0'));
    assertEquals(1, ByteUtils.fromHex('1'));
    assertEquals(2, ByteUtils.fromHex('2'));
    assertEquals(3, ByteUtils.fromHex('3'));
    assertEquals(4, ByteUtils.fromHex('4'));
    assertEquals(5, ByteUtils.fromHex('5'));
    assertEquals(6, ByteUtils.fromHex('6'));
    assertEquals(7, ByteUtils.fromHex('7'));
    assertEquals(8, ByteUtils.fromHex('8'));
    assertEquals(9, ByteUtils.fromHex('9'));
    assertEquals(10, ByteUtils.fromHex('A'));
    assertEquals(11, ByteUtils.fromHex('B'));
    assertEquals(12, ByteUtils.fromHex('C'));
    assertEquals(13, ByteUtils.fromHex('D'));
    assertEquals(14, ByteUtils.fromHex('E'));
    assertEquals(15, ByteUtils.fromHex('F'));
    assertEquals(10, ByteUtils.fromHex('a'));
    assertEquals(11, ByteUtils.fromHex('b'));
    assertEquals(12, ByteUtils.fromHex('c'));
    assertEquals(13, ByteUtils.fromHex('d'));
    assertEquals(14, ByteUtils.fromHex('e'));
    assertEquals(15, ByteUtils.fromHex('f'));

    assertEquals((byte) 0, ByteUtils.from2HexDigits("00"));
    assertEquals((byte) 1, ByteUtils.from2HexDigits("01"));
    assertEquals((byte) 16, ByteUtils.from2HexDigits("10"));
    assertEquals((byte) 255, ByteUtils.from2HexDigits("ff"));
  }

  /**
   * Test of hexStringToByteArray method, of class ByteUtils.
   */
  @Test
  public void testHexStringToByteArray() {
    LOGGER.info("hexStringToByteArray");
    final byte[] bytes1 = ByteUtils.serialize("abcdefg");
    final String bytes1Hex = ByteUtils.toHex(bytes1);
    assertEquals("aced000574000761626364656667", bytes1Hex);
    final byte[] result = ByteUtils.hexStringToByteArray(bytes1Hex);
    assertTrue(Arrays.equals(bytes1, result));

    final byte[] bytes2 = new byte[0];
    final String bytes2Hex = ByteUtils.toHex(bytes2);
    assertNotNull(bytes2Hex);
    assertEquals("", bytes2Hex);
  }

  /**
   * Test of serialize method, of class ByteUtils.
   */
  @Test
  public void testSerialize() {
    LOGGER.info("serialize");
    byte[] result = ByteUtils.serialize("abc");
    assertEquals(10, result.length);
    byte[] expectedResult = {-84, -19, 0, 5, 116, 0, 3, 97, 98, 99};
    assertTrue(ByteUtils.areEqual(expectedResult, result));
  }

  /**
   * Test of deserialize method, of class ByteUtils.
   */
  @Test
  public void testDeserialize() {
    LOGGER.info("deserialize");
    byte[] bytes = {-84, -19, 0, 5, 116, 0, 3, 97, 98, 99};
    assertEquals("abc", ByteUtils.deserialize(bytes).toString());
  }

  /**
   * Test of makeUUIDBytes method, of class ByteUtils.
   */
  @Test
  public void testMakeUUIDBytes() {
    LOGGER.info("makeUUIDBytes");
    byte[] result = ByteUtils.makeUUIDBytes();
    assertEquals(16, result.length);
  }

  /**
   * Test of isZero method, of class ByteUtils.
   */
  @Test
  public void testIsZero_ByteArr() {
    LOGGER.info("isZero");
    Byte[] bytes1 = {0, 0, 0};
    boolean result = ByteUtils.isZero(bytes1);
    assertEquals(true, result);
    Byte[] bytes2 = {0, 0, 1};
    result = ByteUtils.isZero(bytes2);
    assertEquals(false, result);
  }

  /**
   * Test of isZero method, of class ByteUtils.
   */
  @Test
  public void testIsZero_byteArr() {
    LOGGER.info("isZero");
    byte[] bytes1 = {0, 0, 0};
    boolean result = ByteUtils.isZero(bytes1);
    assertEquals(true, result);
    byte[] bytes2 = {0, 0, 1};
    result = ByteUtils.isZero(bytes2);
    assertEquals(false, result);
  }

  /**
   * Test of isNonZero method, of class ByteUtils.
   */
  @Test
  public void testIsNonZero_ByteArr() {
    LOGGER.info("isNonZero");
    Byte[] bytes1 = {0, 0, 0};
    boolean result = ByteUtils.isNonZero(bytes1);
    assertEquals(false, result);
    Byte[] bytes2 = {0, 0, 1};
    result = ByteUtils.isNonZero(bytes2);
    assertEquals(false, result);
    Byte[] bytes3 = {1, 2, 3};
    result = ByteUtils.isNonZero(bytes3);
    assertEquals(true, result);
  }

  /**
   * Test of isNonZero method, of class ByteUtils.
   */
  @Test
  public void testIsNonZero_byteArr() {
    LOGGER.info("isNonZero");
    byte[] bytes1 = {0, 0, 0};
    boolean result = ByteUtils.isNonZero(bytes1);
    assertEquals(false, result);
    byte[] bytes2 = {0, 0, 1};
    result = ByteUtils.isNonZero(bytes2);
    assertEquals(false, result);
    byte[] bytes3 = {1, 2, 3};
    result = ByteUtils.isNonZero(bytes3);
    assertEquals(true, result);
  }

  /**
   * Test of toByteObjectArray method, of class ByteUtils.
   */
  @Test
  public void testToByteObjectArray_UUID() {
    LOGGER.info("toByteObjectArray");
    UUID uuid = UUID.randomUUID();
    Byte[] result = ByteUtils.toByteObjectArray(uuid);
    assertNotNull(result);
    assertEquals(16, result.length);
  }

  /**
   * Test of toByteObjectArray method, of class ByteUtils.
   */
  @Test
  public void testToByteObjectArray_byteArr() {
    LOGGER.info("toByteObjectArray");
    byte[] byteArray = ByteUtils.makeUUIDBytes();
    Byte[] result = ByteUtils.toByteObjectArray(byteArray);
    assertNotNull(result);
    assertEquals(byteArray.length, result.length);
    assertEquals(Byte.valueOf(byteArray[0]), result[0]);
  }

  /**
   * Test of toByteArray method, of class ByteUtils.
   */
  @Test
  public void testToByteArray() {
    LOGGER.info("toByteArray");
    Byte[] byteArray = {0, 1, 3};
    byte[] expResult = {0, 1, 3};
    byte[] result = ByteUtils.toByteArray(byteArray);
    assertEquals(ByteUtils.toHex(expResult), ByteUtils.toHex(result));
  }

  /**
   * Test of append method, of class ByteUtils.
   */
  @Test
  public void testAppend() {
    LOGGER.info("append");
    byte[] byteArray1 = {1, 2, 3};
    byte[] byteArray2 = {4, 5};
    byte[] expResult = {1, 2, 3, 4, 5};
    byte[] result = ByteUtils.append(byteArray1, byteArray2);
    assertEquals(ByteUtils.toHex(expResult), ByteUtils.toHex(result));
  }

  /**
   * Test of toBytes method, of class ByteUtils.
   */
  @Test
  public void testToBytes_long() {
    LOGGER.info("toBytes");
    long number1 = 0L;
    byte[] expResult1 = {0, 0, 0, 0, 0, 0, 0, 0};
    byte[] result1 = ByteUtils.toBytes(number1);
    assertEquals(ByteUtils.toHex(expResult1), ByteUtils.toHex(result1));
    long number2 = 256L;
    byte[] expResult2 = {0, 0, 0, 0, 0, 0, 1, 0};
    byte[] result2 = ByteUtils.toBytes(number2);
    assertEquals(ByteUtils.toHex(expResult2), ByteUtils.toHex(result2));
  }

  /**
   * Test of toBytes method, of class ByteUtils.
   */
  @Test
  public void testToBytes_int() {
    LOGGER.info("toBytes");
    int number1 = 0;
    byte[] expResult1 = {0, 0, 0, 0};
    byte[] result1 = ByteUtils.toBytes(number1);
    assertEquals(ByteUtils.toHex(expResult1), ByteUtils.toHex(result1));
    int number2 = 256;
    byte[] expResult2 = {0, 0, 1, 0};
    byte[] result2 = ByteUtils.toBytes(number2);
    assertEquals(ByteUtils.toHex(expResult2), ByteUtils.toHex(result2));
  }

  /**
   * Test of toLong method, of class ByteUtils.
   */
  @Test
  public void testToLong() {
    LOGGER.info("toLong");
    byte[] byteArray1 = {0, 0, 0, 0, 0, 0, 0, 0};
    long expResult1 = 0L;
    long result1 = ByteUtils.toLong(byteArray1);
    assertEquals(expResult1, result1);
    byte[] byteArray2 = {0, 0, 0, 0, 0, 0, 1, 0};
    long expResult2 = 256L;
    long result2 = ByteUtils.toLong(byteArray2);
    assertEquals(expResult2, result2);
  }

  /**
   * Test of toUint32LittleEndian method, of class ByteUtils.
   */
  @Test
  public void testToUint32LittleEndian() {
    LOGGER.info("toUint32LittleEndian");
    byte[] byteArray1 = {0, 0, 0, 0};
    long expResult1 = 0L;
    long result1 = ByteUtils.toUint32LittleEndian(byteArray1);
    assertEquals(expResult1, result1);
    byte[] byteArray2 = {0x55, 0, 0, 0};
    long expResult2 = 85L;
    long result2 = ByteUtils.toUint32LittleEndian(byteArray2);
    assertEquals(expResult2, result2);
  }

  /**
   * Test of toBytes method, of class ByteUtils.
   */
  @Test
  public void testToBytes_UUID() {
    LOGGER.info("toBytes");
    UUID uuid = UUID.randomUUID();
    byte[] result = ByteUtils.toBytes(uuid);
    assertNotNull(result);
    assertEquals(16, result.length);
  }

  /**
   * Test of toHex method, of class ByteUtils.
   */
  @Test
  public void testToHex_byteArr() {
    LOGGER.info("toHex");
    byte[] byteArray = {-1, 0, 1, 2, 3, 4, 5};
    String expResult = "ff000102030405";
    String result = ByteUtils.toHex(byteArray);
    assertEquals(expResult, result);
  }

  /**
   * Test of toHex method, of class ByteUtils.
   */
  @Test
  public void testToHex_Byte() {
    LOGGER.info("toHex");
    Byte byte1 = -1;
    String expResult = "ff";
    String result = ByteUtils.toHex(byte1);
    assertEquals(expResult, result);
  }

  /**
   * Test of byteArrayToInt method, of class ByteUtils.
   */
  @Test
  public void testByteArrayToInt() {
    LOGGER.info("byteArrayToInt");
    byte[] byteArray1 = {0, 0, 0};
    assertEquals(0, ByteUtils.byteArrayToInt(byteArray1));
    byte[] byteArray2 = {0, 1, 0};
    assertEquals(256, ByteUtils.byteArrayToInt(byteArray2));
    byte[] byteArray3 = {1, 2, 3};
    int result = ByteUtils.byteArrayToInt(byteArray3, 2);
    assertEquals(3, result);
    byte[] byteArray4 = {1, 0, 0, 0};
    int expectedResult = 256 * 256 * 256;
    assertEquals(expectedResult, ByteUtils.byteArrayToInt(byteArray4));
    byte[] byteArray5 = {1, 0, 0, 0, 0};
    assertEquals(expectedResult, ByteUtils.byteArrayToInt(byteArray5, 0));
    byte[] byteArray6 = {1, 2, 3, 4, 5};
    expectedResult = (4 * 256) + 5;
    assertEquals(expectedResult, ByteUtils.byteArrayToInt(byteArray6, 3));
    assertEquals(5, ByteUtils.byteArrayToInt(byteArray6, 4));
    assertEquals(0, ByteUtils.byteArrayToInt(byteArray6, 5));
  }

  /**
   * Test of areEqual method, of class ByteUtils.
   */
  @Test
  public void testAreEqual_byteArr_byteArr() {
    LOGGER.info("areEqual");
    byte[] byteArray1 = {-1, 0, 1};
    byte[] byteArray2 = {-1, 5, 1};
    assertFalse(ByteUtils.areEqual(byteArray1, byteArray2));
    byte[] byteArray3 = {-1, 0, 1};
    assertTrue(ByteUtils.areEqual(byteArray1, byteArray3));
  }

  /**
   * Test of areEqual method, of class ByteUtils.
   */
  @Test
  public void testAreEqual_ByteArr_ByteArr() {
    LOGGER.info("areEqual");
    Byte[] byteArray1 = {-1, 0, 1};
    Byte[] byteArray2 = {-1, 5, 1};
    assertFalse(ByteUtils.areEqual(byteArray1, byteArray2));
    Byte[] byteArray3 = {-1, 0, 1};
    assertTrue(ByteUtils.areEqual(byteArray1, byteArray3));
  }

  /**
   * Test of compareTo method, of class ByteUtils.
   */
  @Test
  public void testCompareTo_ByteArr_ByteArr() {
    LOGGER.info("compareTo");
    Byte[] lhs = {0, 0, 1};
    Byte[] rhs1 = {0, 0, 1};
    assertEquals(0, ByteUtils.compareTo(lhs, rhs1));
    Byte[] rhs2 = {0, 0, 2};
    assertEquals(-1, ByteUtils.compareTo(lhs, rhs2));
    Byte[] rhs3 = {-1, 0, 2};
    assertEquals(1, ByteUtils.compareTo(lhs, rhs3));
  }

  /**
   * Test of compareTo method, of class ByteUtils.
   */
  @Test
  public void testCompareTo_byteArr_byteArr() {
    LOGGER.info("compareTo");
    byte[] lhs = {0, 0, 1};
    byte[] rhs1 = {0, 0, 1};
    assertEquals(0, ByteUtils.compareTo(lhs, rhs1));
    byte[] rhs2 = {0, 0, 2};
    assertEquals(-1, ByteUtils.compareTo(lhs, rhs2));
    byte[] rhs3 = {-1, 0, 2};
    assertEquals(1, ByteUtils.compareTo(lhs, rhs3));
  }
}
