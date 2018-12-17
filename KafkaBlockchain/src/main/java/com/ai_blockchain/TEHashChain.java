/*
 * TEHashChain.java
 *
 * Created on May 26, 2017, 3:09:57 PM
 *
 * Description: Provides a verifying hash chain container.
 *
 * Copyright (C) May 26, 2017 by AI Blockchain, all rights reserved.
 *
 */
package com.ai_blockchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public final class TEHashChain {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(TEHashChain.class);
  // the hash chain
  private final List<TEObject> teObjects = new ArrayList<>();
  // the SHA-256 hash of the last item
  private SHA256Hash sha256Hash = null;

  /**
   * Constructs a new TEHashChain instance. The chain is formed by including the hash from the previous entry into the hash calculation for
   * the current entry. Thus the action of adding a new element involves the effort of hashing the current entry, but verification involves
   * recalculating the hashes of the chain items from the beginning.
   */
  public TEHashChain() {
  }

  /**
   * Gets the SHA-256 hash of the last tamper-evident item on the hash chain, or null if the chain is empty.
   *
   * @return the SHA-256 hash of the last tamper-evident item on the hash chain, or null if the chain is empty
   */
  public SHA256Hash getSHA256Hash() {
    return sha256Hash;
  }

  /**
   * Appends the given serializable object to the tamper-evident hash chain.
   *
   * @param obj the given serializable object
   */
  public void appendSerializable(final Serializable obj) {
    //Preconditions
    assert obj != null : "obj must not be null";

    final TEObject teObject = new TEObject(
            obj, // payload
            sha256Hash, // previousTEObjectHash
            teObjects.size() + 1); // serialNbr
    appendTEObject(teObject);
  }

  /**
   * Verifies the given tamper evident object and adds it to this tamper evident hash chain.
   *
   * @param teObject the given tamper evident object
   */
  public void appendTEObject(final TEObject teObject) {
    //Preconditions
    assert teObject != null : "teObject must not be null";

    // make another tamper evident object using the serialied contents of the given one
    final TEObject verifyingTEObject = new TEObject(
            Serialization.deserialize(teObject.getPayloadBytes()), // payload
            sha256Hash, // previousTEObjectHash
            teObject.getSerialNbr());
    if (!teObject.equals(verifyingTEObject)) {
      throw new IllegalArgumentException("given tamper evident object is not consistent " + teObject);
    }
    teObjects.add(teObject);
    sha256Hash = teObject.getTEObjectHash();
  }

  /**
   * Gets the tamper-evident object at the specified index location.
   *
   * @param index the specified index location
   * @return the tamper evident object
   */
  public TEObject getTEObject(final int index) {
    //Preconditions
    assert index >= 0 && index < teObjects.size() :
            "invalid index " + index + ", must be in the range [0 ... " + (teObjects.size() - 1) + "]";

    return teObjects.get(index);
  }

  /**
   * Returns the size of the tamper-evident hash chain.
   *
   * @return the size
   */
  public int size() {
    return teObjects.size();
  }

  /**
   * Returns whether the tamper-evident hash chain is empty.
   *
   * @return the size
   */
  public boolean isEmpty() {
    return teObjects.isEmpty();
  }

  /**
   * Provides a hash code for this object.
   *
   * @return a hash code for this object
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 67 * hash + Objects.hashCode(this.teObjects);
    return hash;
  }

  /**
   * Returns whether this object equals another one.
   *
   * @param obj the other object
   * @return whether this object equals another one
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
    final TEHashChain other = (TEHashChain) obj;
    return Objects.equals(this.teObjects, other.teObjects);
  }

  /**
   * Returns a string representation of this object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    return (new StringBuilder())
            .append("[TEHashChain size ")
            .append(teObjects.size())
            .append(']')
            .toString();
  }

  /**
   * Validates the tamper-evident hash chain beginning at the indexed item, assuming the prior chain items are valid.
   *
   * @return whether the hash chain is either empty, or that its items are a valid hash chain
   */
  public boolean isValid() {
    if (isEmpty()) {
      assert sha256Hash == null;
      return true;
    } else {
      assert sha256Hash != null;
      return isValid(0);
    }
  }

  /**
   * Validates the tamper-evident hash chain beginning at the indexed item, assuming the prior chain items are valid.
   *
   * @param index the index from which to begin validating the chain, with value 0 indicating the whole chain
   * @return whether the specified portion of the hash chain is valid
   */
  public boolean isValid(final int index) {
    //Preconditions
    assert index >= 0 : "index must not be negative";
    assert index < teObjects.size() : "index must specify a value within the chain, whose size is " + teObjects.size();

    final int teObjects_size = teObjects.size();
    TEObject previousTEObject;
    if (index > 0) {
      previousTEObject = teObjects.get(index - 1);
    } else {
      previousTEObject = null;
    }
    for (int i = index; i < teObjects_size; i++) {
      final TEObject teObject = teObjects.get(i);
      // make another tamper evident object using the serialized contents of the given one
      final Serializable verifyingPayload = Serialization.deserialize(teObject.getPayloadBytes());
      final TEObject verifyingTEObject = new TEObject(
              verifyingPayload, // payload
              previousTEObject, // previousTEObjectHash
              teObject.getSerialNbr());
      if (!teObject.equals(verifyingTEObject)) {
        return false;
      }
      previousTEObject = teObject;
    }
    // check the last item's hash
    return sha256Hash.equals(teObjects.get(teObjects_size - 1).getTEObjectHash());
  }
}
