/**
 * MockTEObject.java
 *
 * Created on Jan 21, 2018, 7:43:16 PM
 *
 * Description: .
 *
 * Copyright (C) Jan 21, 2018 Stephen L. Reed.
 */
package com.ai_blockchain;

import java.io.Serializable;
import org.apache.log4j.Logger;

public class MockTEObject extends TEObject {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(MockTEObject.class);

  /**
   * Constructs a new MockTEObject instance.
   * @param payload
   * @param previousTEObject
   * @param serialNbr
   */
  public MockTEObject(
          final Serializable payload,
          final TEObject previousTEObject,
          final long serialNbr) {
    super(
            payload,
            previousTEObject,
            serialNbr);
  }

  /**
   * Returns the serial number, which is a bogus 0 for this mock object.
   *
   * @return the serial number
   */
  public long getSerialNbr() {
    return 0;
  }
}
