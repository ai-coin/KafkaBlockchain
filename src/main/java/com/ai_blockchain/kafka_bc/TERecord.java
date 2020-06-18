/*
 * TERecord.java
 *
 * Created on May 26, 2017, 10:35:09 AM
 *
 * Description: Provides an immutable tamper-evident serialized object.
 *
 * Copyright (C) May 26, 2017 by AI Blockchain, all rights reserved.
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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public record TERecord(
        // the serialized payload bytes
        byte[] payloadBytes,
        // the SHA256 hash of the previous TERecord, which is not present for the first TERecord in the hash chain
        SHA256Hash previousTERecordHash,
        // the SHA256 hash of the previous hash plus the hash of the payload
        SHA256Hash teRecordHash,
        // the serial number
        long serialNbr)
        implements Serializable, TamperEvident {

  // the serial version UID, which by being explicitly declared allows changes to this class without voiding
  // the previously created serializations
  static final long serialVersionUID = 1L;

  // the logger
  private static final Logger LOGGER = Logger.getLogger(TERecord.class);

  /**
   * Constructs a new TERecord instance.
   */
  public TERecord    {
    //Preconditions
    assert payloadBytes != null : "payloadBytes must not be null";
    assert serialNbr >= 0 : "serialNbr must be a non-negative number";
    assert teRecordHash != null : "teRecordHash must not be null";
  }

  /**
   * Constructs a new TERecord instance.
   *
   * @param payload the payload object to be made tamper evident in a hash chain
   * @param previousTERecord the previous TERecord in the hash chain
   * @param serialNbr the serial number
   */
  public TERecord(
          final Serializable payload,
          final TERecord previousTERecord,
          final long serialNbr) {
    this(Serialization.serialize(payload),
            (previousTERecord == null) ? null : previousTERecord.teRecordHash,
            serialNbr);
  }

  /**
   * Constructs a new TERecord instance.
   *
   * @param payload the payload object to be made tamper evident in a hash chain
   * @param previousTERecordHash the SHA256 hash of the previous TERecord, which is not present for the first TERecord in the hash chain
   * @param serialNbr the serial number the serial number
   */
  protected TERecord(
          final byte[] payloadBytes,
          final SHA256Hash previousTERecordHash,
          final long serialNbr) {
    this(
            payloadBytes,
            previousTERecordHash,
            computeTERecordHash(
                    previousTERecordHash,
                    payloadBytes,
                    serialNbr),
            serialNbr);
  }

  /**
   * Computes the SHA-256 hash using the payload bytes, the serial number, and the previous hash in the blockchain.
   *
   * @param previousTERecordHash the previous TERecord in the hash chain
   * @param payloadBytesthe serialized payload bytes
   * @param serialNbr
   * @return
   */
  private static SHA256Hash computeTERecordHash(
          final SHA256Hash previousTERecordHash,
          final byte[] payloadBytes,
          final long serialNbr) {

    // hash the previous hash if present, then hash the payload
    final MessageDigest messageDigest;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
    messageDigest.reset();
    if (previousTERecordHash != null) {
      messageDigest.update(previousTERecordHash.getBytes());
    }
    messageDigest.update(payloadBytes);
    final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
    byteBuffer.putLong(
            0, // index
            serialNbr); // value
    messageDigest.update(byteBuffer.array());
    return new SHA256Hash(messageDigest.digest());
  }

  /**
   * Returns the serial number.
   *
   * @return the serial number
   */
  @Override
  public long getSerialNbr() {
    return serialNbr;
  }

  /**
   * Returns a copy of the immutable payload bytes.
   *
   * @return a copy of the immutable payload bytes
   */
  @Override
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
   * Gets the SHA256 hash of the previous tamper-evident object, which is not present for the first such object in the hash chain.
   *
   * @return the SHA256 hash of the previous tamper-evident object or null if no predecessor
   */
  public SHA256Hash getPreviousHash() {
    return previousTERecordHash;
  }

  /**
   * Gets the SHA256 hash of the previous hash plus the hash of the payload.
   *
   * @return the SHA256 hash of the previous hash plus the hash of the payload
   */
  @Override
  public SHA256Hash getTEHash() {
    return teRecordHash;
  }

  /**
   * Gets the SHA256 hash of the previous hash plus the hash of the payload.
   *
   * @return the SHA256 hash of the previous hash plus the hash of the payload
   */
  public SHA256Hash getTERecordHash() {
    return teRecordHash;
  }

  /**
   * Returns whether this tamper-evident object is a valid successor to the previous TERecord, which is checked only for valid
   * deserialization and reserialization.
   *
   * @param previousTERecord the previous tamper-evident object
   * @return whether this tamper-evident object is a valid successor to the previous TERecord
   */
  public boolean isValidSuccessor(final TERecord previousTERecord) {
    //Preconditions
    assert previousTERecord != null : "previousTERecord must not be null";

    if (!previousTERecord.isValid()) {
      return false;
    }
    if (!this.isValid()) {
      return false;
    }

    // return whether the hash chain is verified
    return this.previousTERecordHash.equals(previousTERecord.teRecordHash);
  }

  /**
   * Return whether this tamper evident object's fields can be rehashed to the recorded value.
   *
   * @return whether the fields and the SHA-256 hash value for this object are consistent
   */
  @Override
  public boolean isValid() {
    final TERecord teRecord1 = new TERecord(
            Serialization.serialize(getPayload()),
            previousTERecordHash,
            serialNbr);
    if (!Arrays.equals(this.payloadBytes, teRecord1.payloadBytes)) {
      return false;
    } else if (this.previousTERecordHash == null && teRecord1.previousTERecordHash != null) {
      return false;
    } else if (this.previousTERecordHash != null && teRecord1.previousTERecordHash == null) {
      return false;
    } else if ((this.previousTERecordHash != null && teRecord1.previousTERecordHash != null)
            && (!this.previousTERecordHash.equals(teRecord1.previousTERecordHash))) {
      return false;
    } else if (!this.teRecordHash.equals(teRecord1.teRecordHash)) {
      return false;
    } else {
      return this.serialNbr == teRecord1.serialNbr;
    }
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
    hash = 23 * hash + Objects.hashCode(this.previousTERecordHash);
    hash = 23 * hash + Objects.hashCode(this.teRecordHash);
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
    final TERecord other = (TERecord) obj;
    if (!java.util.Arrays.equals(this.payloadBytes, other.payloadBytes)) {
      return false;
    }
    if (!Objects.equals(this.serialNbr, other.serialNbr)) {
      return false;
    }
    if (!Objects.equals(this.previousTERecordHash, other.previousTERecordHash)) {
      return false;
    }
    return Objects.equals(this.teRecordHash, other.teRecordHash);
  }

  /**
   * Returns a string representation of this object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (new StringBuilder())
            .append("[TERecord ")
            .append(serialNbr)
            .append(", wrapping a payload of ")
            .append(payloadBytes.length)
            .append(" bytes]")
            .toString();
  }
}