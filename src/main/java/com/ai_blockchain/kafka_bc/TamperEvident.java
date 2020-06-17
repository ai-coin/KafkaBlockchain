/*
 * TamperEvident.java
 *
 * Created on Jun 17, 2020
 *
 * Description: Defines a tamper evident object or record that can be contained
 * in the TEHashChain.
 * .
 * Copyright 2020 reed.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ai_blockchain.kafka_bc;

/**
 *
 * @author reed
 */
public interface TamperEvident {
  
  /**
   * Returns the serial number.
   *
   * @return the serial number
   */
  public long getSerialNbr();
  
  /**
   * Returns a copy of the immutable payload bytes.
   *
   * @return a copy of the immutable payload bytes
   */
  public byte[] getPayloadBytes();
  
  /**
   * Return whether this tamper evident object's fields can be rehashed to the recorded value.
   *
   * @return whether the fields and the SHA-256 hash value for this object are consistent
   */
  public boolean isValid();

  /**
   * Gets the SHA256 hash of the previous hash plus the hash of the payload.
   *
   * @return the SHA256 hash of the previous hash plus the hash of the payload
   */
  public SHA256Hash getTEHash();

}