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

public class MockTEObject extends TEObject {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(MockTEObject.class);

  /**
   * Constructs a new MockTEObject instance.
   *
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
