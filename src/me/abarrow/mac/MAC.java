package me.abarrow.mac;

import me.abarrow.core.CryptoException;
import me.abarrow.stream.StreamProcess;

public interface MAC {
  //Calculates the authentication tag and either appends it to the to the end of the input or just outputs the tag.
  public StreamProcess tag(boolean tagOnly);
  public byte[] tag(byte[] data, boolean tagOnly) throws CryptoException;
  
  //Buffer the input, calculate and compare authentication tags if they match output the input with
  // the tag removed and throwing if they don't match.
  public StreamProcess checkTag(boolean checkOnly);
  public byte[] checkTag(byte[] data, boolean checkOnly) throws CryptoException;
 
  public MAC setKey(byte[] key) throws CryptoException;
  public boolean hasKey();
  public MAC removeKey();

  public int getTagLength();
}