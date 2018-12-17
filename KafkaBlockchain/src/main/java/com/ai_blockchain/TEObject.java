/*
 * TEObject.java
 *
 * Created on May 26, 2017, 10:35:09 AM
 *
 * Description: Provides an immutable tamper-evident serialized object.
 *
 * Copyright (C) May 26, 2017 by AI Blockchain, all rights reserved.
 *
 */
package com.ai_blockchain;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public class TEObject implements Serializable {

  // the serial version UID, which by being explicitly declared allows changes to this class without voiding
  // the previously created serializations
  static final long serialVersionUID = 1L;

  // the logger
  private static final Logger LOGGER = Logger.getLogger(TEObject.class);
  // the serialized payload bytes
  private final byte[] payloadBytes;
  // the SHA256 hash of the previous TEObject, which is not present for the first TEObject in the hash chain
  private final SHA256Hash previousTEObjectHash;
  // the SHA256 hash of the previous hash plus the hash of the payload
  private final SHA256Hash teObjectHash;
  // the serial number
  private final long serialNbr;

  /**
   * Constructs a new TEObject instance.
   *
   * @param payload the payload object to be made tamper evident in a hash chain
   * @param previousTEObject the previous TEObject in the hash chain
   * @param serialNbr the serial number
   */
  public TEObject(
          final Serializable payload,
          final TEObject previousTEObject,
          final long serialNbr) {
    this(
            payload,
            (previousTEObject == null) ? null : previousTEObject.teObjectHash,
            serialNbr);
  }

  /**
   * Constructs a new TEObject instance.
   *
   * @param payload the payload object to be made tamper evident in a hash chain
   * @param previousTEObjectHash the SHA256 hash of the previous TEObject, which is not present for the first TEObject
   * in the hash chain
   * @param serialNbr the serial number
   */
  public TEObject(
          final Serializable payload,
          final SHA256Hash previousTEObjectHash,
          final long serialNbr) {
    //Preconditions
    assert payload != null : "payload must not be null";
    assert serialNbr >= 0 : "serialNbr must be a non-negative number";

    payloadBytes = Serialization.serialize(payload);
    this.previousTEObjectHash = previousTEObjectHash;
    this.serialNbr = serialNbr;

    // hash the previous hash if present, then hash the payload
    final MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    messageDigest.reset();
    if (previousTEObjectHash != null) {
      messageDigest.update(previousTEObjectHash.getBytes());
    }
    messageDigest.update(payloadBytes);
    final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
    byteBuffer.putLong(
            0, // index
            serialNbr); // value
    messageDigest.update(byteBuffer.array());
    teObjectHash = new SHA256Hash(messageDigest.digest());
  }

  /**
   * Returns the serial number.
   *
   * @return the serial number
   */
  public long getSerialNbr() {
    return serialNbr;
  }

  /**
   * Returns a copy of the immutable payload bytes.
   *
   * @return a copy of the immutable payload bytes
   */
  public byte[] getPayloadBytes() {
    return payloadBytes.clone();
  }

  /**
   * Returns the deserialized payload object.
   *
   * @return the deserialized payload object
   */
  public Serializable getPayload() {
    return Serialization.deserialize(payloadBytes);
  }

  /**
   * Gets the SHA256 hash of the previous tamper-evident object, which is not present for the first such object in the
   * hash chain.
   *
   * @return the SHA256 hash of the previous tamper-evident object or null if no predecessor
   */
  public SHA256Hash getPreviousHash() {
    return previousTEObjectHash;
  }

  /**
   * Gets the SHA256 hash of the previous hash plus the hash of the payload.
   *
   * @return the SHA256 hash of the previous hash plus the hash of the payload
   */
  public SHA256Hash getTEObjectHash() {
    return teObjectHash;
  }

  /**
   * Returns whether this tamper-evident object is a valid successor to the previous TEObject, which is checked only for
   * valid deserialization and reserialization.
   *
   * @param previousTEObject the previous tamper-evident object
   * @return whether this tamper-evident object is a valid successor to the previous TEObject
   */
  public boolean isValidSuccessor(final TEObject previousTEObject) {
    //Preconditions
    assert previousTEObject != null : "previousTEObject must not be null";

    if (!previousTEObject.isValid()) {
      return false;
    }
    if (!this.isValid()) {
      return false;
    }

    // return whether the hash chain is verified
    return this.previousTEObjectHash.equals(previousTEObject.teObjectHash);
  }

  /**
   * Return whether this tamper evident object's fields can be rehashed to the recorded value.
   *
   * @return whether the fields and the SHA-256 hash value for this object are consistent
   */
  public boolean isValid() {
    return this.equals(new TEObject(
            getPayload(),
            previousTEObjectHash,
            serialNbr));
  }

  /**
   * Returns a hash code for this object.
   *
   * @return a hash code for this object
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 23 * hash + java.util.Arrays.hashCode(this.payloadBytes);
    hash = 23 * hash + Objects.hashCode(this.previousTEObjectHash);
    hash = 23 * hash + Objects.hashCode(this.teObjectHash);
    return hash;
  }

  /**
   * Returns whether another object equals this one.
   *
   * @param obj the other object
   * @return whether another object equals this one
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TEObject other = (TEObject) obj;
    if (!java.util.Arrays.equals(this.payloadBytes, other.payloadBytes)) {
      return false;
    }
    if (!Objects.equals(this.serialNbr, other.serialNbr)) {
      return false;
    }
    if (!Objects.equals(this.previousTEObjectHash, other.previousTEObjectHash)) {
      return false;
    }
    return Objects.equals(this.teObjectHash, other.teObjectHash);
  }

  /**
   * Returns a string representation of this object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (new StringBuilder())
            .append("[TEObject ")
            .append(serialNbr)
            .append(", wrapping a payload of ")
            .append(payloadBytes.length)
            .append(" bytes]")
            .toString();
  }
}
