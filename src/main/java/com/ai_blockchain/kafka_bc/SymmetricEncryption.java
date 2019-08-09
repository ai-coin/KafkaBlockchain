/*
 * SymmetricEncryption.java
 *
 * Created on December 18, 2018.
 *
 * Description:  Performs symmetric encryption where the same key is used to efficiently encrypt and decrypt the message. This code is adapted from security
 * articles.
 * <code>
 * https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9
 * http://moi.vonos.net/java/symmetric-encryption-bc/
 * </code>
 *
 * Copyright (C) 2018 Stephen L. Reed.
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
 *
 */
package com.ai_blockchain.kafka_bc;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 *
 * @author reed
 */
public class SymmetricEncryption {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(SymmetricEncryption.class);
  private static final int AES_NIVBITS = 128; // CBC Initialization Vector (same as cipher block size) [16 bytes]
  private KeyParameter aesKey; // computed as needed
  // the raw bytes of the key, maximum of 256 bits = 32 bytes
  private byte[] rawKeyData = new byte[32];

  /** Constructs a new SymmetricEncryption instance.
   * 
   * @param rawKeyData the raw bytes of the key
   */
  public SymmetricEncryption(final byte[] rawKeyData) {
    //Preconditions
    assert rawKeyData != null && rawKeyData.length == 32 : "rawKeyData must be 32 bytes in length";

    this.rawKeyData = rawKeyData;
  }

  /**
   * Encode a byte array with AES.
   *
   * @param plaintextBytes the plaintext (not encoded) bytes
   * @return the encoded ciphertext
   * @throws java.security.GeneralSecurityException when an exception occurs
   */
  public byte[] encode(byte[] plaintextBytes) throws GeneralSecurityException {
    //Preconditions
    assert plaintextBytes != null : "plaintextBytes must not be null";

    // Generate 128 bits of random data for use as the initial vector (IV). It is important to use a different IV for
    // each encrypted block of text, to ensure that the same string encrypted by two different sessions
    // does not give the same encrypted text string - that leads to obvious attack possibilities. Note
    // however that the IV does not need to be kept secret; it is a little bit like a 'salt' for a
    // password, which improves security even when the salt is stored in plaintext in a database or
    // prefixed to the encrypted file.
    byte[] ivData = new byte[AES_NIVBITS / 8];
    final SecureRandom secureRandom = new SecureRandom(); // Note: no  seed here, ie these values are truly random
    secureRandom.nextBytes(ivData);

    // Select encryption algorithm and padding : AES with CBC and PCKS#7
    final BlockCipherPadding blockCipherPadding = new PKCS7Padding();
    final BufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), blockCipherPadding);

    // Encrypt the input string using key + iv
    final KeyParameter keyParameter = getAESKey();
    final CipherParameters cipherParameters = new ParametersWithIV(keyParameter, ivData);

    bufferedBlockCipher.reset();
    bufferedBlockCipher.init(true, cipherParameters); // first param = encode/decode

    byte[] ciphertextBytes;
    try {
      final int bufferLength = bufferedBlockCipher.getOutputSize(plaintextBytes.length);
      ciphertextBytes = new byte[bufferLength];
      int nbrCiphertextBytes = bufferedBlockCipher.processBytes(plaintextBytes, 0, plaintextBytes.length, ciphertextBytes, 0);
      nbrCiphertextBytes += bufferedBlockCipher.doFinal(ciphertextBytes, nbrCiphertextBytes);

      if (nbrCiphertextBytes != ciphertextBytes.length) {
        throw new IllegalStateException("Unexpected behaviour : getOutputSize value incorrect");
      }
    } catch (InvalidCipherTextException | RuntimeException e) {
      throw new GeneralSecurityException("encryption failed");
    }

    // Return a byte array containing IV + encrypted input byte array
    byte[] bytesAll = new byte[ivData.length + ciphertextBytes.length];
    System.arraycopy(ivData, 0, bytesAll, 0, ivData.length);
    System.arraycopy(ciphertextBytes, 0, bytesAll, ivData.length, ciphertextBytes.length);
    return bytesAll;
  }

  /**
   * Decode a byte array which has first been encrypted with AES.
   *
   * @param ciphertextBytes the encoded bytes
   * @return the plaintext bytes
   * @throws java.security.GeneralSecurityException when an exception occurs
   */
  public byte[] decode(final byte[] ciphertextBytes) throws GeneralSecurityException {
    //Preconditions
    assert ciphertextBytes != null : "bytesEnc must not be null";

    // Extract the IV, which is stored in the next N bytes
    final int nbrIvBytes = AES_NIVBITS / 8;
    final byte[] ivBytes = new byte[nbrIvBytes];
    System.arraycopy(ciphertextBytes, 0, ivBytes, 0, nbrIvBytes);

    // Select encryption algorithm and padding : AES with CBC and PCKS#7.
    // Note that the "encryption strength" (128 or 256 bit key) is set by the KeyParameter object.
    final KeyParameter keyParameter = getAESKey();
    final CipherParameters cipherParameters = new ParametersWithIV(keyParameter, ivBytes);
    final BlockCipherPadding blockCipherPadding = new PKCS7Padding();
    final BufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), blockCipherPadding);

    // Decrypt all bytes that follow the IV
    bufferedBlockCipher.reset();
    bufferedBlockCipher.init(false, cipherParameters); // first param = encode/decode

    final byte[] plaintextBytes;

    try {
      final int buflen = bufferedBlockCipher.getOutputSize(ciphertextBytes.length - nbrIvBytes);
      final byte[] workingBuffer = new byte[buflen];
      int len = bufferedBlockCipher.processBytes(ciphertextBytes, nbrIvBytes, ciphertextBytes.length - nbrIvBytes, workingBuffer, 0);
      len += bufferedBlockCipher.doFinal(workingBuffer, len);

      // Note that getOutputSize returns a number which includes space for "padding" bytes to be stored in.
      // However we don't want these padding bytes; the "len" variable contains the length of the *real* data
      // (which is always less than the return value of getOutputSize.
      plaintextBytes = new byte[len];
      System.arraycopy(workingBuffer, 0, plaintextBytes, 0, len);
    } catch (InvalidCipherTextException e) {
      throw new GeneralSecurityException("decode failed");
    } catch (RuntimeException e) {
      throw new GeneralSecurityException("decryption failed");
    }
    return plaintextBytes;
  }

  /**
   * Returns the AES key for encryption or decryption.
   *
   * @return the AES key
   * @throws GeneralSecurityException
   */
  private KeyParameter getAESKey() throws GeneralSecurityException {
    if (aesKey != null) {
      return aesKey;
    }

    // Wrap the key data in an appropriate holder type 
    aesKey = new KeyParameter(rawKeyData);
    return aesKey;
  }

  /**
   * Sets the raw bytes of the key, maximum of 256 bits = 32 bytes.
   *
   * @param rawKeyData the raw bytes of the key
   */
  public void setRawKeyData(final byte[] rawKeyData) {
    //Preconditions
    assert rawKeyData != null && rawKeyData.length == 32 : "rawKeyData must be 32 bytes in length";

    this.rawKeyData = rawKeyData;
  }

}
