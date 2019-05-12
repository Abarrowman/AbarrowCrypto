package me.abarrow.padding;

import java.io.IOException;

import me.abarrow.stream.StreamProcess;

public abstract class Padding {
  protected int blockSize;
  
  public Padding() { 
    blockSize = 0;
  }
  
  public Padding(int bSize) { 
    blockSize = bSize;
  }
  
  public Padding setBlockSize(int bSize) {
    blockSize = bSize;
    return this;
  }
  
  public byte[] pad(byte[] input) {
    return pad(input, 0, input.length);
  }
  public byte[] unpad(byte[] input) {
    return unpad(input, 0, input.length);
  }
  
  public abstract byte[] pad(byte[] input, int start, int len);
  public abstract byte[] unpad(byte[] input, int start, int len);
  public abstract StreamProcess pad() throws IOException;
  public abstract StreamProcess unpad() throws IOException;
}
