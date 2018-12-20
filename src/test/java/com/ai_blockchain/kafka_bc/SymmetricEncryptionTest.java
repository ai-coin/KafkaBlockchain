/*
 * Copyright 2018 reed.
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

import com.ai_blockchain.kafka_bc.SymmetricEncryption;
import com.ai_blockchain.kafka_bc.ByteUtils;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.digests.GeneralDigest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.prng.DigestRandomGenerator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author reed
 */
public class SymmetricEncryptionTest {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(SymmetricEncryption.class);
  // the UTF-8 character set
  private static final Charset UTF8 = Charset.forName("UTF-8");

  public SymmetricEncryptionTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testSymmetricEncryption() {
    try {
      final byte[] rawKeyBytes = new byte[32];
      (new DigestRandomGenerator(new MD5Digest())).nextBytes(rawKeyBytes);
      LOGGER.info("rawKeyBytes " + ByteUtils.toHex(rawKeyBytes));
      final String plaintext = "this is the test plaintext character string";
      LOGGER.info("plaintext \"" + plaintext + "\"");
      final byte[] plaintextBytes = plaintext.getBytes(UTF8);
      assertNotNull(plaintextBytes);
      final SymmetricEncryption instance = new SymmetricEncryption(rawKeyBytes);
      final byte[] ciphertextBytes = instance.encode(plaintextBytes);
      assertNotNull(ciphertextBytes);
      final byte[] decodedBytes = instance.decode(ciphertextBytes);
      final String decodedPlaintext = new String(decodedBytes, UTF8);
      LOGGER.info("decodedPlaintext \"" + decodedPlaintext + "\"");
      assertEquals(decodedPlaintext, plaintext);
    } catch (GeneralSecurityException ex) {
      fail(ex.toString());
    }
  }

}
