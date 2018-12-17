/*
 * Serialization.java
 *
 * Created on May 11, 2017, 12:50:31 PM
 *
 * Description: Provides Java object serialization methods to byte arrays, and the reverse. Because the IOExceptions
 * should not occur, they are wrapped in runtime exceptions to save calling methods the burden of handling them.
 *
 * Copyright (C) May 11, 2017 by Stephen L. Reed, all rights reserved.
 *
 */
package com.ai_blockchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.log4j.Logger;

/**
 *
 * @author reed
 */
public class Serialization {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(Serialization.class);
  // the indicator that debugging is enabled
  private static final boolean IS_DEBUG_ENABLED = LOGGER.isDebugEnabled();

  /**
   * Prevents the construction of a new Serialization instance, because this is a static utility class.
   */
  private Serialization() {
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

    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final ObjectOutput objectOutput;
    try {
      objectOutput = new ObjectOutputStream(byteArrayOutputStream);
      objectOutput.writeObject(obj);
      objectOutput.flush();
      return byteArrayOutputStream.toByteArray();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        byteArrayOutputStream.close();
      } catch (IOException ex) {
        // ignore close exception
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

    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
    ObjectInput objectInput = null;
    try {
      objectInput = new ObjectInputStream(byteArrayInputStream);
      return (Serializable) objectInput.readObject();
    } catch (IOException | ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    } finally {
      try {
        if (objectInput != null) {
          objectInput.close();
        }
      } catch (IOException ex) {
        // ignore close exception
      }
    }
  }

  public static Serializable serializeDeserialize(final Serializable obj) {
    //Preconditions
    assert obj != null : "obj must not be null";

    return deserialize(serialize(obj));
  }


}
