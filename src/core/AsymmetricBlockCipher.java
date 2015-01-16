package core;

public abstract class AsymmetricBlockCipher {
  
  protected abstract int getBlockBytes();
  
  public abstract void encryptBlock(byte[] input, int srcPos, byte[] output, int destPos);
  
  public abstract void decryptBlock(byte[] input, int srcPos, byte[] output, int destPos);
  
  public byte[] encrypt(byte[] input) {
    
    int blockBytes = getBlockBytes();
    byte[] output = new byte[blockBytes * ((input.length + blockBytes - 1) / blockBytes)];

    int i;

    for (i = 0; (i + blockBytes - 1) < input.length; i+= blockBytes) {
      encryptBlock(input, i, output, i);
    }    
    
    if (i < input.length) {
      //pad with 0s
      byte[] padding = new byte[blockBytes];
      System.arraycopy(input, i, padding, 0, input.length - i);
      encryptBlock(padding, 0, output, i);
    }
        
    return output;
  }
  
  public byte[] decrypt(byte[] input) {
    
    int blockBytes = getBlockBytes();
    byte[] output = new byte[blockBytes * ((input.length + blockBytes - 1) / blockBytes)];

    int i;

    for (i = 0; (i + blockBytes - 1) < input.length; i+= blockBytes) {
     decryptBlock(input, i, output, i);
    }    
    
    if (i < input.length) {
      //pad with 0s
      byte[] padding = new byte[blockBytes];
      System.arraycopy(input, i, padding, 0, input.length - i);
      encryptBlock(padding, 0, output, i);
    }
        
    return output;
  }

}
