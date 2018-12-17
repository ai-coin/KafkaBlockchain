/*
 * ByteUtils.java
 *
 * Created on April 17, 2007, 6:53 PM
 *
 * Description:  Byte array utilities adapted from org.apache.commons.id.uuid.Bytes.
 *
 * Copyright (C) 2007 Stephen L. Reed.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.UUID;

/**
 *
 * @author reed
 */
public final class ByteUtils {

  // the hex characters
  private static final byte[] HEX_CHARACTERS = {
    '0', '1', '2', '3',
    '4', '5', '6', '7',
    '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f'
  };

  /**
   * Hide constructor in this utility class.
   */
  private ByteUtils() {
  }

  /**
   * Returns a compact string from the given bytes. The 8-bit character set is specified to retain byte values.
   *
   * @param bytes the given bytes
   *
   * @return a compact string
   */
  public static String to8BitString(final byte[] bytes) {
    try {
      return new String(bytes, "ISO-8859-1");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Returns the corresponding bytes of the given compact string. The 8-bit character set is specified to retain byte values.
   *
   * @param string the given string
   *
   * @return he corresponding bytes
   */
  public static byte[] from8BitString(final String string) {
    try {
      return string.getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Returns the lower 8 bits of the given int as a byte.
   *
   * @param i the given int
   *
   * @return the lower 8 bits of the given int as a byte
   */
  public static byte toUnsignedByte(final int i) {
    return (byte) (i & 0x000000ff);
  }

  /**
   * Serializes the given object into a byte array.
   *
   * @param obj the given object
   *
   * @return the serialized byte array
   */
  public static byte[] serialize(final Serializable obj) {
    //Preconditions
    assert obj != null : "obj must not be null";

    ObjectOutputStream objectOutputStream = null;
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(obj);
      objectOutputStream.close();
      return byteArrayOutputStream.toByteArray();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        if (objectOutputStream != null) {
          objectOutputStream.close();
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  /**
   * Deserializes the given byte array into an object.
   *
   * @param bytes the given byte array
   *
   * @return the deserialized object
   */
  public static Serializable deserialize(final byte[] bytes) {
    //Preconditions
    assert bytes != null : "obj must not be null";

    final InputStream inputStream = new ByteArrayInputStream(bytes);
    ObjectInputStream objectInputStream = null;
    try {
      objectInputStream = new ObjectInputStream(inputStream);
      return (Serializable) objectInputStream.readObject();
    } catch (IOException | ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        if (objectInputStream != null) {
          objectInputStream.close();
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  /**
   * Returns the array of bytes resulting from a new UUID.
   *
   * @return the array of bytes resulting from a new UUID
   */
  public static byte[] makeUUIDBytes() {
    final UUID uuid = UUID.randomUUID();
    return append(toBytes(uuid.getMostSignificantBits()), toBytes(uuid.getLeastSignificantBits()));
  }

  /**
   * Returns true if the given byte array is all zeros.
   *
   * @param bytes the given byte array
   *
   * @return true if the given byte array is all zeros
   */
  public static boolean isZero(final Byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";

    for (byte myByte : bytes) {
      if (myByte != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the given byte array is all zeros.
   *
   * @param bytes the given byte array
   *
   * @return true if the given byte array is all zeros
   */
  public static boolean isZero(final byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";

    for (byte myByte : bytes) {
      if (myByte != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the given byte array is not all zeros.
   *
   * @param bytes the given byte array
   *
   * @return true if the given byte array is not all zeros
   */
  public static boolean isNonZero(final Byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";

    for (byte myByte : bytes) {
      if (myByte == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the given byte array is not all zeros.
   *
   * @param bytes the given byte array
   *
   * @return true if the given byte array is not all zeros
   */
  public static boolean isNonZero(final byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";

    for (byte myByte : bytes) {
      if (myByte == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns an array of Byte objects for the given UUID.
   *
   * @param uuid the UUID
   *
   * @return an array of Byte objects for the given UUID
   */
  public static Byte[] toByteObjectArray(final UUID uuid) {
    //Preconditions
    assert uuid != null : "uuid must not be null";

    final byte[] byteArray = toBytes(uuid);
    final Byte[] bytes = new Byte[byteArray.length];
    for (int i = 0; i < byteArray.length; i++) {
      bytes[i] = byteArray[i];
    }
    return bytes;
  }

  /**
   * Returns an array of Byte objects for the given byte array.
   *
   * @param bytes the byte array
   *
   * @return an array of Byte objects for the given byte array
   */
  public static Byte[] toByteObjectArray(final byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";

    final Byte[] byteObjectArray = new Byte[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      byteObjectArray[i] = bytes[i];
    }
    return byteObjectArray;
  }

  /**
   * Returns an array of bytes for the given Byte object array.
   *
   * @param byteObjectArray the given Byte object array
   *
   * @return an array of bytes for the given Byte object array
   */
  public static byte[] toByteArray(final Byte[] byteObjectArray) {
    //Preconditions
    assert byteObjectArray != null : "byteObjectArray must not be null";

    final byte[] bytes = new byte[byteObjectArray.length];
    for (int i = 0; i < byteObjectArray.length; i++) {
      bytes[i] = byteObjectArray[i];
    }
    return bytes;
  }

  /**
   * Appends two bytes array into one.
   *
   * @param bytes1 the first given byte array
   * @param bytes2 the second given byte array
   *
   * @return the byte array resulting from appending the two given byte arrays
   */
  public static byte[] append(final byte[] bytes1, final byte[] bytes2) {
    //Preconditions
    assert bytes1 != null : "bytes1 must not be null";
    assert bytes2 != null : "bytes2 must not be null";

    final byte[] bytes3 = new byte[bytes1.length + bytes2.length];
    System.arraycopy(bytes1, 0, bytes3, 0, bytes1.length);
    System.arraycopy(bytes2, 0, bytes3, bytes1.length, bytes2.length);
    return bytes3;
  }

  /**
   * Returns a 8-byte array built from a long.
   *
   * @param number the long number to convert
   *
   * @return a byte array
   */
  public static byte[] toBytes(final long number) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(8);
    byteBuffer.putLong(number);
    return byteBuffer.array();
  }

  /**
   * Builds a long from first 8 bytes of the array.
   *
   * @param bytes the byte array to convert
   *
   * @return a long
   */
  public static long toLong(final byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";
    assert bytes.length == 8 : "the byte array must be length 8";

    return ((((long) bytes[7]) & 0xFF)
            + ((((long) bytes[6]) & 0xFF) << 8)
            + ((((long) bytes[5]) & 0xFF) << 16)
            + ((((long) bytes[4]) & 0xFF) << 24)
            + ((((long) bytes[3]) & 0xFF) << 32)
            + ((((long) bytes[2]) & 0xFF) << 40)
            + ((((long) bytes[1]) & 0xFF) << 48)
            + ((((long) bytes[0]) & 0xFF) << 56));
  }

  /**
   * Returns a non-negative long from the given 4 byte array, which is represented in a little-endian manner.
   *
   * @param bytes the given 4 byte little-endian array
   * @return a non-negative long
   */
  public static long toUint32LittleEndian(byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";
    assert bytes.length == 4 : "the byte array must be length 4";

    return (bytes[0] & 0xFFL)
            | ((bytes[1] & 0xFFL) << 8)
            | ((bytes[2] & 0xFFL) << 16)
            | ((bytes[3] & 0xFFL) << 24);
  }

  /**
   * Returns a 4-byte array built from an int.
   *
   * @param number the int number to convert
   *
   * @return a byte array
   */
  public static byte[] toBytes(final int number) {
    final ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    byteBuffer.putInt(number);
    return byteBuffer.array();
  }

  /**
   * Returns the 16-byte array from the given UUID.
   *
   * @param uuid the given UUID
   *
   * @return the 16-byte array from the given UUID
   */
  public static byte[] toBytes(final UUID uuid) {
    //Preconditions
    assert uuid != null : "uuid must not be null";

    return append(toBytes(uuid.getMostSignificantBits()), toBytes(uuid.getLeastSignificantBits()));
  }

  /**
   * Returns a hexadecimal string representation of the given byte array.
   *
   * @param bytes the given byte array
   *
   * @return a hexadecimal string representation of the given byte array
   */
  public static String toHex(final byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";

    final int byteArray_len = bytes.length;
    final StringBuilder stringBuilder = new StringBuilder(2 * byteArray_len);
    for (int i = 0; i < byteArray_len; i++) {
      final int value = bytes[i] & 0xff;
      stringBuilder.append((char) HEX_CHARACTERS[value >> 4]);
      stringBuilder.append((char) HEX_CHARACTERS[value & 0xf]);
    }
    return stringBuilder.toString();
  }

  /**
   * Returns a string representation of the given byte.
   *
   * @param byte1 the given byte
   *
   * @return a string representation of the given byte
   */
  public static String toHex(final Byte byte1) {
    final int value = byte1 & 0xff;
    final StringBuilder stringBuilder = new StringBuilder(2);
    stringBuilder.append((char) HEX_CHARACTERS[value >> 4]);
    stringBuilder.append((char) HEX_CHARACTERS[value & 0xf]);
    return stringBuilder.toString();
  }

  /**
   * Returns the integer represented by the given hex digit character.
   *
   * @param hexCharacter the given hex digit character
   *
   * @return the integer represented by the given hex digit character
   */
  public static int fromHex(final char hexCharacter) {
    final char lowerCaseHexCharacter = Character.toLowerCase(hexCharacter);
    for (int i = 0; i < 16; i++) {
      if (HEX_CHARACTERS[i] == lowerCaseHexCharacter) {
        return i;
      }
    }
    assert false : "invalid hex digit";
    return -1;
  }

  /**
   * Returns the byte represented by the two hex digits.
   *
   * @param string two hex digits
   *
   * @return the byte represented by the two hex digits
   */
  public static byte fromHex(final String string) {
    //Preconditions
    assert string != null && string.length() == 2 : "string must have length 2";

    final char c0 = string.charAt(0);
    final char c1 = string.charAt(1);
    return (byte) (16 * fromHex(c0) + fromHex(c1));
  }

  /**
   * Returns the byte array represented by the given hex string.
   *
   * @param hexString the given hex string
   * @return the byte array represented by the given hex string
   */
  public static byte[] hexStringToByteArray(final String hexString) {
    //Preconditions
    assert hexString != null : "hexString must not be null";
    assert hexString.length() % 2 == 0 : "hexString must have an even length, divisible by 2 with no remainder";

    final int len = hexString.length();
    final byte[] bytes = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
              + Character.digit(hexString.charAt(i + 1), 16));
    }
    return bytes;
  }

  public static byte from2HexDigits(final String string) {
    //Preconditions
    assert string != null && string.length() == 2 : "string must have length 2";

    final char c0 = string.charAt(0);
    final char c1 = string.charAt(1);
    return (byte) (16 * fromHex(c0) + fromHex(c1));
  }

  /**
   * Convert the byte array to an int. The array must be four or less in length.
   *
   * @param bytes The byte array
   *
   * @return the integer
   */
  public static int byteArrayToInt(final byte[] bytes) {
    //Preconditions
    assert bytes != null : "bytes must not be null";

    if (bytes.length > 4) {
      throw new IllegalArgumentException("must be four or less bytes");
    }

    return byteArrayToInt(bytes, 0);
  }

  /**
   * Convert the byte array to an int starting from the given offset, and continuing for up to four bytes.
   *
   * @param bytes The byte array
   * @param offset The array offset
   *
   * @return the integer
   */
  public static int byteArrayToInt(final byte[] bytes, final int offset) {
    //Preconditions
    assert bytes != null : "bytes must not be null";
    assert offset >= 0 : "offset must not be negative";

    final int length;
    if ((bytes.length - offset) > 4) {
      length = 4;
    } else {
      length = bytes.length - offset;
    }
    int value = 0;
    for (int i = 0; i < length; i++) {
      final int shift = (length - 1 - i) * 8;
      value += (bytes[i + offset] & 0x000000FF) << shift;
    }
    return value;
  }

  /**
   * Compares two byte arrays for equality.
   *
   * @param bytes1 the first given byte array
   * @param bytes2 the second given byte array
   *
   * @return True if the arrays have identical contents.
   */
  public static boolean areEqual(final byte[] bytes1, final byte[] bytes2) {
    //Preconditions
    assert bytes1 != null : "bytes1 must not be null";
    assert bytes2 != null : "bytes2 must not be null";

    final int aLength = bytes1.length;
    if (aLength != bytes2.length) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (bytes1[i] != bytes2[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares two byte arrays for equality.
   *
   * @param bytes1 the first given byte array
   * @param bytes2 the second given byte array
   *
   * @return True if the arrays have identical contents.
   */
  public static boolean areEqual(final Byte[] bytes1, final Byte[] bytes2) {
    //Preconditions
    assert bytes1 != null : "bytes1 must not be null";
    assert bytes2 != null : "bytes2 must not be null";

    final int aLength = bytes1.length;
    if (aLength != bytes2.length) {
      return false;
    }
    for (int i = 0; i < aLength; i++) {
      if (!bytes1[i].equals(bytes2[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares two Byte arrays as specified by <code>Comparable</code> with respect to each byte as a signed integer.
   *
   * @param lhs - left hand value in the comparison operation.
   * @param rhs - right hand value in the comparison operation.
   *
   * @return a negative integer, zero, or a positive integer as <code>lhs</code> is less than, equal to, or greater than <code>rhs</code>.
   */
  public static int compareTo(final Byte[] lhs, final Byte[] rhs) {
    //Preconditions
    assert lhs != null : "lhs must not be null";
    assert rhs != null : "rhs must not be null";

    return compareTo(toByteArray(lhs), toByteArray(rhs));
  }

  /**
   * Compares two byte arrays as specified by <code>Comparable</code> with respect to each byte as a signed integer.
   *
   * @param lhs - left hand value in the comparison operation.
   * @param rhs - right hand value in the comparison operation.
   *
   * @return a negative integer, zero, or a positive integer as <code>lhs</code> is less than, equal to, or greater than <code>rhs</code>.
   */
  public static int compareTo(final byte[] lhs, final byte[] rhs) {
    //Preconditions
    assert lhs != null : "lhs must not be null";
    assert rhs != null : "rhs must not be null";

    if (lhs == rhs) {
      return 0;
    }
    if (lhs.length != rhs.length) {
      return ((lhs.length < rhs.length) ? -1 : +1);
    }
    for (int i = 0; i < lhs.length; i++) {
      if (lhs[i] < rhs[i]) {
        return -1;
      } else if (lhs[i] > rhs[i]) {
        return 1;
      }
    }
    return 0;
  }

  /**
   * Provides a byte array comparator with respect to each byte as a signed integer.
   */
  public static class ByteArrayComparator implements Comparator<Byte[]>, Serializable {

    /**
     * the serialization version ID
     */
    /**
     */
    public ByteArrayComparator() {
    }

    /**
     * Compares the two given byte arrays with respect to each byte as a signed integer.
     *
     * @param bytes1 the first given byte array
     * @param bytes2 the second given byte array
     *
     * @return a negative integer, zero, or a positive integer as <code>lhs</code> is less than, equal to, or greater than <code>rhs</code>
     */
    @Override
    public int compare(final Byte[] bytes1, final Byte[] bytes2) {
      //Preconditions
      assert bytes1 != null : "bytes1 must not be null";
      assert bytes2 != null : "bytes2 must not be null";

      return compareTo(toByteArray(bytes1), toByteArray(bytes2));
    }
  }
}
