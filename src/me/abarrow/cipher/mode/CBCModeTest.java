package me.abarrow.cipher.mode;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import me.abarrow.cipher.Cipher;
import me.abarrow.cipher.aes.AES;
import me.abarrow.core.CryptoException;
import me.abarrow.core.CryptoUtils;
import me.abarrow.padding.PKCS7;
import me.abarrow.padding.Padding;
import me.abarrow.padding.ZeroPadding;

import org.junit.Test;

public class CBCModeTest {
  @Test
  public void testEncoding() throws CryptoException, InterruptedException, IOException {
    byte[] key = CryptoUtils.parseHexString("123afc45778932543cdabb432645123afc457789b2543cdabb432645b4326408");
    byte[] iv = CryptoUtils.parseHexString("8051f5bb13d68fcb5f8e25dd890228ac");
    byte[] plainText = CryptoUtils.parseHexString("48656c6c6f20686f772061726520796f752c2061726520796f752061207261626269743f");
    byte[] expectedCipherText = CryptoUtils.parseHexString("6232d0a50c4e00a30cfb161bcc3a4dd84079a729f94dde6429887d8ba50752c909f12f7266ba4eaab8991375e0b938ec");
    byte[] expectedPlainText = CryptoUtils.parseHexString("48656c6c6f20686f772061726520796f752c2061726520796f752061207261626269743f000000000000000000000000");
    testCase(key, iv,  plainText, expectedCipherText, expectedPlainText, new ZeroPadding());
    
    key = CryptoUtils.parseHexString("ac5800ac3cb59c7c14f36019e43b44fe");
    iv = CryptoUtils.parseHexString("f013ce1ec901b5b60a85a986b3b72eba");
    plainText = CryptoUtils.parseHexString("f6cee5ff28fd");
    expectedCipherText = CryptoUtils.parseHexString("e8a846fd9718507371604504d4ca1ac7");
    expectedPlainText = plainText;
    testCase(key, iv,  plainText, expectedCipherText, expectedPlainText, new PKCS7());
  }
  
  private void testCase(byte[] key, byte[] iv, byte[] plainText,
      byte[] expectedCipherText, byte[] expectedPlainText, Padding padding)
      throws InterruptedException, IOException, CryptoException {
    Cipher cbcMode = new CBCMode(new AES(key), padding, iv).setIVPrepending(false);
    
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    cbcMode.encrypt().runSync(new ByteArrayInputStream(plainText), o);
    assertArrayEquals(expectedCipherText, cbcMode.encrypt().runSync(plainText));
    assertArrayEquals(expectedCipherText,  o.toByteArray());

    o = new ByteArrayOutputStream();
    cbcMode.decrypt().runSync(new ByteArrayInputStream(expectedCipherText), o);
    assertArrayEquals(expectedPlainText, cbcMode.decrypt().runSync(expectedCipherText));
    assertArrayEquals(expectedPlainText, o.toByteArray());
    
  }
  
}
