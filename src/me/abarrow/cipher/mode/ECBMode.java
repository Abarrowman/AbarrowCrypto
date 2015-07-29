package me.abarrow.cipher.mode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import me.abarrow.cipher.BlockCipher;
import me.abarrow.cipher.Cipher;
import me.abarrow.core.CryptoException;
import me.abarrow.core.CryptoUtils;
import me.abarrow.padding.Padding;
import me.abarrow.stream.StreamRunnable;

public class ECBMode implements Cipher {

  private BlockCipher core;
  private Padding padding;
  private int blockSize;

  public ECBMode(BlockCipher c, Padding p) {
    core = c;
    blockSize = core.getBlockBytes();
    padding = p.setBlockSize(blockSize);
  }

  @Override
  public byte[] encrypt(byte[] unpadded) throws CryptoException {
    byte[] input = padding.pad(unpadded);
    int blockCount = input.length / blockSize;
    byte[] output = new byte[input.length];
    for (int n = 0, pos = 0; n < blockCount; n++, pos += blockSize) {
      core.encryptBlock(input, pos, output, pos);
    }
    return output;
  }

  @Override
  public byte[] decrypt(byte[] input) throws CryptoException {
    if ((input.length % blockSize) != 0) {
      throw new CryptoException(CryptoException.INVALID_LENGTH);
    }
    int blockCount = input.length / blockSize;
    byte[] output = new byte[input.length];
    for (int n = 0, pos = 0; n < blockCount; n++, pos += blockSize) {
      core.decryptBlock(input, pos, output, pos);
    }
    return padding.unpad(output);
  }

  @Override
  public StreamRunnable encrypt() {
    return new StreamRunnable() {
      @Override
      public void process(InputStream in, OutputStream out) throws IOException {
        byte[] block = new byte[blockSize];
        byte[] output = new byte[blockSize];
        byte[] pointer;
        boolean going = true;
        try {
          while (going) {
            int read = in.read(block);
            if (read == blockSize) {
              pointer = block;
            } else {
              going = false;
              pointer = padding.pad(Arrays.copyOf(block, read < 0 ? 0 : read));
              if (pointer.length == 0) {
                break;
              }
            }
            core.encryptBlock(pointer, output);
            out.write(output);
          }
        } catch (CryptoException e) {
          throw new IOException(e);
        } finally {
          CryptoUtils.fillWithZeroes(output);
          CryptoUtils.fillWithZeroes(block);
        }
      }
    };
  }

  @Override
  public StreamRunnable decrypt() {
    return new StreamRunnable() {
      @Override
      public void process(InputStream in, OutputStream out) throws IOException {
        byte[] block = new byte[blockSize];
        byte[] output = new byte[blockSize];
        boolean hasOutput = false;
        try {
          while (true) {
            int read = in.read(block);
            if (read == blockSize) {
              if (hasOutput) {
                out.write(output);
              }
              core.decryptBlock(block, output);
              hasOutput = true;
              continue;
            }
            if (read != -1) {
              throw new CryptoException(CryptoException.INVALID_LENGTH);
            }
            break;
          }
          if (hasOutput) {
            out.write(padding.unpad(output));
          }
        } catch (CryptoException e) {
          throw new IOException(e);
        } finally {
          CryptoUtils.fillWithZeroes(output);
          CryptoUtils.fillWithZeroes(block);
        }
      }
    };
  }

  @Override
  public Cipher setKey(byte[] key) {
    core.setKey(key);
    return this;
  }

  @Override
  public boolean hasKey() {
    return core.hasKey();
  }

  @Override
  public Cipher removeKey() {
    core.removeKey();
    return this;
  }

  
  @Override
  public Cipher setIV(byte[] initVector) { 
    return this;
  }

  @Override
  public boolean hasIV() {
    return false;
  }

  @Override
  public byte[] getIV() {
    return null;
  }

  @Override
  public boolean isIVPrepending() {
    return false;
  }

  @Override
  public Cipher setIVPrepending(boolean ivPrepending) {
    return this;
  }

}
