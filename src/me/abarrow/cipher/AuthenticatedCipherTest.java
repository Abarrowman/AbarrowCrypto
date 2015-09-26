package me.abarrow.cipher;

import static org.junit.Assert.*;

import java.io.IOException;

import me.abarrow.cipher.aes.AES;
import me.abarrow.cipher.mode.CTRMode;
import me.abarrow.cipher.mode.ECBMode;
import me.abarrow.core.CryptoException;
import me.abarrow.core.CryptoUtils;
import me.abarrow.hash.sha.SHA256;
import me.abarrow.mac.MAC;
import me.abarrow.mac.hmac.HMAC;
import me.abarrow.padding.ZeroPadding;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;

import org.junit.Test;

public class AuthenticatedCipherTest {

  @Test
  public void test() throws IOException, CryptoException {
    byte[] cipherKey = "catsareevil".getBytes();
    byte[] macKey = "dogsareevil".getBytes();
    String plainString = "I blame the angry foxes for her death!";
    byte[] plain = plainString.getBytes();
    
    byte[] expectedEncrypted = parseHexBinary("0b47b1f05656b6ff4daa98ce50136f56f81ae812e2f96c8035f73e8e81765587f302739bc2d34e63d3c17e52decfe550");
    byte[] expectedTagged = parseHexBinary("0b47b1f05656b6ff4daa98ce50136f56f81ae812e2f96c8035f73e8e81765587f302739bc2d34e63d3c17e52decfe550b1123c3a17c1861c2a41ae75c0433a137d978ed5e3204b70a21c97739bcb23a2");
    
    
    Cipher cipher = new ECBMode(new AES(), new ZeroPadding());
    cipher.setKey(cipherKey);
    byte[] encrypted = cipher.encrypt().startSync(plain);
    assertArrayEquals(expectedEncrypted, encrypted);
    
    MAC mac = new HMAC(new SHA256());
    mac.setMACKey(macKey);
    byte[] tagged =  mac.tag(false).startSync(encrypted);
    assertArrayEquals(expectedTagged, tagged);
    
    byte[] unTagged = mac.checkTag(false).startSync(tagged);
    assertArrayEquals(expectedEncrypted, unTagged);

    byte[] decrypted = cipher.decrypt().startSync(unTagged);
    String decryptedString = new String(decrypted).replace("\0", "");
    assertEquals(plainString, decryptedString);    
    
    AuthenticatedCipher authCipher = new AuthenticatedCipher(new ECBMode(new AES(), new ZeroPadding()), new HMAC(new SHA256()));
    authCipher.setKey(cipherKey);
    authCipher.setMACKey(macKey);
    byte[] authEncrypted = authCipher.encrypt().startSync(plain);
    assertArrayEquals(expectedTagged, authEncrypted);

    byte[] authDecrypted = authCipher.decrypt().startSync(authEncrypted);
    String authDecryptedString = new String(authDecrypted).replace("\0", "");
    assertEquals(plainString, authDecryptedString);    

    
  }

}
