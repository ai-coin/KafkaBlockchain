/*
 * SHA256Hash.java
 *
 * Created on May 26, 2017, 11:57:38 AM
 *
 * Description: A type safe immutable container for bytes representing a SHA-256 hash.
 *
 * Copyright (C) May 26, 2017 by AI Blockchain, all rights reserved.
 *
 */
package com.ai_blockchain;

//import com.google.common.primitives.Ints;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 *
 * @author reed
 */
public class SHA256Hash implements Serializable, Comparable<SHA256Hash> {

  // enforce that SHA-256 has length 32
  public static final int SHA256_LENGTH = 32;
  // the SHA-256 hash
  private final byte[] hashBytes;

  /** Construct a new SHA256Hash instance from the given hash bytes.
   * 
   * @param hashBytes the given hash bytes 
   */
  public SHA256Hash(final byte[] hashBytes) {
    //Preconditions
    if (hashBytes.length != SHA256_LENGTH) {
      throw new IllegalArgumentException("SHA-256 hash must have length "+ SHA256_LENGTH);
    }
    
    this.hashBytes = hashBytes;
  }
  
  /**
   * Creates a new instance that wraps the given hash value (represented as a hex string).
   *
   * @param hexString a hash value represented as a hex string
   * @throws IllegalArgumentException if the given string is not a valid hex string, or if it does not represent exactly 32 bytes
   */
  public SHA256Hash(final String hexString) {
    //Preconditions
    if (hexString.length() != (SHA256_LENGTH * 2)) {
      throw new IllegalArgumentException("SHA-256 hash hex string must have length "+ (SHA256_LENGTH * 2));
    }
    
    hashBytes = ByteUtils.hexStringToByteArray(hexString);
  }

  /** Makes a SHA256Hash instance by hashing the given serializable object.
   * 
   * @param serializable the given serializable object
   * @return a SHA256Hash instance
   */
  public static SHA256Hash makeSHA256Hash(final Serializable serializable) {
    final MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    messageDigest.reset();
    messageDigest.update(Serialization.serialize(serializable));
    return new SHA256Hash(messageDigest.digest());
  }
  
  
  /**
   * Returns the last four bytes of the wrapped hash as the integer Java hash code.
   */
  @Override
  public int hashCode() {
    return hashBytes[SHA256_LENGTH - 4] << 24 
            | (hashBytes[SHA256_LENGTH - 3] & 0xFF) << 16 
            | (hashBytes[SHA256_LENGTH - 2] & 0xFF) << 8 
            | (hashBytes[SHA256_LENGTH - 1] & 0xFF);
  }

  /**
   * Returns whether this object equals the given object.
   *
   * @param obj the given object
   * @return whether this object equals the given object
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SHA256Hash other = (SHA256Hash) obj;
    return Arrays.equals(this.hashBytes, other.hashBytes);
  }

  /** Returns a string representation of this object.
   * 
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return ByteUtils.toHex(hashBytes);
  }

  /**
   * Returns a copy of the immutable byte array
   * 
   * @return a copy of the immutable byte array 
   */
  public byte[] getBytes() {
    return hashBytes.clone();
  }

  /**
   * Compares this SHA256Hash object with another one.
   *
   * @param other the other SHA256Hash object
   * @return -1 if less than, 0 if equal, otherwise return +1
   */
  @Override
  public int compareTo(final SHA256Hash other) {
    for (int i = SHA256_LENGTH - 1; i >= 0; i--) {
      final int thisByte = this.hashBytes[i] & 0xff;
      final int otherByte = other.hashBytes[i] & 0xff;
      if (thisByte > otherByte) {
        return 1;
      }
      if (thisByte < otherByte) {
        return -1;
      }
    }
    return 0;
  }
}
