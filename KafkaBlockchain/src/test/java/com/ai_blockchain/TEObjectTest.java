package com.ai_blockchain;

import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author reed
 */
public class TEObjectTest {

  // the logger
  private static final Logger LOGGER = Logger.getLogger(TEObjectTest.class);
  // the hash chain
  static TEHashChain teHashChain = new TEHashChain();
  static TEObject teObject1;
  static TEObject instance;
  static TEObject teObject3;

  public TEObjectTest() {
  }

  @BeforeClass
  public static void setUpClass() {
    // create three tamper evident objects in a hash chain
    teObject1 = new TEObject(
            "test payload 1", // payload
            (SHA256Hash) null, // previousTEObjectHash
            1); // serialNbr
    teHashChain.appendTEObject(teObject1);

    instance = new TEObject(
            "test payload 2", // payload
            teObject1.getTEObjectHash(), // previousTEObjectHash
            2); // serialNbr
    teHashChain.appendTEObject(instance);

    teObject3 = new TEObject(
            "test payload 3", // payload
            instance, // previousTEObject
            3); // serialNumber
    teHashChain.appendSerializable("test payload 3");
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

  /**
   * Test of getPayloadBytes method, of class TEObject.
   */
  @Test
  public void testGetPayloadBytes() {
    LOGGER.info("getPayloadBytes");
    byte[] result = instance.getPayloadBytes();
    byte[] expectedResult = Serialization.serialize("test payload 2");
    assertTrue(Arrays.areEqual(expectedResult, result));
  }

  /**
   * Test of getPreviousHash method, of class TEObject.
   */
  @Test
  public void testGetPreviousTEObjectHash() {
    LOGGER.info("getPreviousTEObjectHash");
    SHA256Hash result = instance.getPreviousHash();
    assertEquals("0eae1eac907785f36f7ebbed5c9111995ede4c84728428eb6659a2166695e234", result.toString());
  }

  /**
   * Test of getTeObjectHash method, of class TEObject.
   */
  @Test
  public void testGetTeObjectHash() {
    LOGGER.info("getTeObjectHash");
    SHA256Hash result = instance.getTEObjectHash();
    assertEquals("aa415219a530324cb51ba93d01a0654cbcd30f487d1c3fbc1ea65496ce4ea3b3", result.toString());
  }

  /**
   * Test of isValid method, of class TEObject.
   */
  @Test
  public void testIsValid() {
    LOGGER.info("isValid");
    assertTrue(instance.isValid());

    TEObject badTEObject = new MockTEObject(
            "test payload 3", // payload
            instance, // previousTEObject
            3); // serialNumber
    assertFalse(badTEObject.isValid());
  }

  /**
   * Test of isValidSuccessor method, of class TEObject.
   */
  @Test
  public void testIsValidSuccessor() {
    LOGGER.info("isValidSuccessor");
    assertTrue(instance.isValidSuccessor(teObject1));
    assertFalse(instance.isValidSuccessor(instance));
    assertFalse(instance.isValidSuccessor(teObject3));
  }

  /**
   * Test of equals method, of class TEObject.
   */
  @Test
  public void testEquals() {
    LOGGER.info("equals");
    assertEquals(teObject1, teObject1);
    final TEObject copyTEObject = new TEObject(
            instance.getPayload(),
            instance.getPreviousHash(),
            instance.getSerialNbr());
    assertEquals(instance, copyTEObject);
    assertFalse(instance.equals(teObject1));
  }

  /**
   * Test of hashCode method, of class TEObject.
   */
  @Test
  public void testHashCode() {
    LOGGER.info("hashCode");
    assertEquals(691564675, teObject1.hashCode());
  }

  /**
   * Test of toString method, of class TEObject.
   */
  @Test
  public void testToString() {
    LOGGER.info("toString");
    assertEquals("[TEObject 1, wrapping a payload of 21 bytes]", teObject1.toString());
  }

}
