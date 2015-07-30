package me.abarrow.mac.hmac;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import me.abarrow.core.CryptoException;
import me.abarrow.core.CryptoUtils;
import me.abarrow.hash.Hasher;
import me.abarrow.mac.MAC;
import me.abarrow.stream.DynamicByteQueue;
import me.abarrow.stream.StreamRunnable;

public class HMAC implements MAC {

  private static final byte O_PAD_BYTE = 0x5c;
  private static final byte I_PAD_BYTE = 0x36;

  private int blockBytes;
  private int hashByteLength;

  private Hasher hasher;

  private byte[] iPadKey;
  private byte[] oPadKey;

  public HMAC(Hasher hashMaker) {
    hasher = hashMaker;
    blockBytes = hasher.getBlockBytes();
    hashByteLength = hasher.getHashByteLength();
  }

  public HMAC(Hasher hashMaker, byte[] key) {
    this(hashMaker);
    setMACKey(key);
  }

  public byte[] computeHash(byte[] key, byte[] message) {
    return computeHash(key, message, new byte[hashByteLength], 0);
  }

  public byte[] computeHash(byte[] key, byte[] message, byte[] out, int start) {
    byte[] padded = new byte[blockBytes];
    byte[] padKey = new byte[blockBytes];
    byte[] padKeyHash = new byte[hashByteLength];

    if (key.length > blockBytes) {
      key = hasher.addBytes(key).computeHash();
    }

    if (key.length < blockBytes) {
      // right pad with zereos
      System.arraycopy(key, 0, padded, 0, key.length);
      key = padded;
    }

    Arrays.fill(padKey, I_PAD_BYTE);
    CryptoUtils.xorByteArrays(padKey, key, padKey);

    hasher.addBytes(padKey).addBytes(message).computeHash(padKeyHash, 0);

    Arrays.fill(padKey, O_PAD_BYTE);
    CryptoUtils.xorByteArrays(padKey, key, padKey);

    hasher.addBytes(padKey).addBytes(padKeyHash).computeHash(out, start);

    CryptoUtils.fillWithZeroes(padded);
    CryptoUtils.fillWithZeroes(padKey);
    return out;
  }

  public boolean checkHash(byte[] key, byte[] message, byte[] hmac) {
    return CryptoUtils.arrayEquals(computeHash(key, message), hmac);
  }

  public String computeHashString(byte[] key, byte[] message) {
    return CryptoUtils.byteArrayToHexString(computeHash(key, message));
  }

  public String computeHashString(String key, String message) {
    return CryptoUtils.byteArrayToHexString(computeHash(key.getBytes(), message.getBytes()));
  }

  public int getHMACByteLength() {
    return hashByteLength;
  }

  public StreamRunnable tag(final boolean tagOnly) {
    return new StreamRunnable() {
      @Override
      public void process(InputStream in, OutputStream out) throws IOException {
        innerStream(tagOnly, true, in, out);
      }
    };
  }
  
  private void innerStream(boolean tagOnly, boolean isTagging, InputStream in, OutputStream out) throws IOException {
    if (!hasMACKey()) {
      throw new IOException(new CryptoException(CryptoException.NO_KEY));
    }
    
    boolean append = !tagOnly && isTagging;
    boolean store = !isTagging && !tagOnly;
    DynamicByteQueue queue = null;
    byte[] messageMac = null;
    if (store) {
      queue = new DynamicByteQueue();
    }
    
    hasher.addBytes(iPadKey);
    
    byte[] buffer = new byte[blockBytes];
    byte[] old = new byte[blockBytes];
    byte[] swap;
    boolean hasRead = false;
    while (true) {
      int read = in.read(buffer);
      if (read == blockBytes) {
        if (hasRead) {
          //do something with old
          hasher.addBytes(old);
          if (append) {
            out.write(old);
          } else if (store) {
            queue.write(old);
          }
        }
        swap = old;
        old = buffer;
        buffer = swap;
      } else if (read == -1) {
        if (hasRead) {
          if (isTagging) {
            //last complete block of plaintext
            hasher.addBytes(old);
            if (append) {
              out.write(old);
            }
          } else {
            //last block contains the hash
            int macStartIndex = blockBytes - hashByteLength;
            hasher.addBytes(old, 0, macStartIndex);
            if (store) {
              queue.write(old, 0, macStartIndex);
            }
            messageMac = Arrays.copyOfRange(old, macStartIndex, blockBytes);
          }
        } else {
          if (!isTagging) {
            throw new IOException(new CryptoException(CryptoException.NO_MAC));
          } else {
            //authenticate empty text
            //ok I guess
          }
        }
        break;
      } else {
        byte[] partialBlock = Arrays.copyOf(buffer, read);
        if (isTagging) {
          if (hasRead) {
            hasher.addBytes(old);
            if (append) {
              out.write(old);
            }
          }
          hasher.addBytes(partialBlock);
          if (append) {
            out.write(partialBlock);
          }
        } else {
          if (read >= hashByteLength) {
            //none of the old block is part of the hmac
            if (hasRead) {
              hasher.addBytes(old);
              if (store) {
                out.write(old);
              }
            }
            int macStartIndex = read - hashByteLength;
            //the partial block contains the hmac
            messageMac = Arrays.copyOfRange(partialBlock, macStartIndex, read);
            //the partial block might also contain some data to be hashed
            hasher.addBytes(partialBlock, 0, macStartIndex);
            if (store) {
              out.write(partialBlock, 0, macStartIndex);
            }
          } else {
            int macStartIndex = hashByteLength - read;
            messageMac = new byte[hashByteLength];
            //all of the partial block is part of the hmac
            System.arraycopy(partialBlock, 0, messageMac, macStartIndex, read);
            //some of the old block is part of the hmac
            int oldMacIndex = blockBytes - macStartIndex;
            System.arraycopy(old, oldMacIndex, messageMac, 0, macStartIndex);
            //but some of the last block is data to be hashed
            hasher.addBytes(old, 0, oldMacIndex);
            if (store) {
              out.write(old, 0, oldMacIndex);
            }
          }
        }
        CryptoUtils.fillWithZeroes(partialBlock);
        break;
      }
    }
    CryptoUtils.fillWithZeroes(buffer);
    CryptoUtils.fillWithZeroes(old);

    byte[] firstPass = hasher.computeHash();
    hasher.addBytes(oPadKey).addBytes(firstPass);
    CryptoUtils.fillWithZeroes(firstPass);
    byte[] hash = hasher.computeHash();
    if (!isTagging) {
      if(!CryptoUtils.arrayEquals(hash, messageMac)) {
        throw new IOException(new CryptoException(CryptoException.MAC_DOES_NOT_MATCH));
      }
    }
    if (store) {
      queue.doneWriting();
      queue.readTo(out, blockBytes);
    }
    out.write(hash);
    CryptoUtils.fillWithZeroes(hash);
  }

  @Override
  public byte[] tag(byte[] data, boolean tagOnly) throws CryptoException {
    return innerTag(data, 0, data.length, tagOnly);
  }

  private byte[] innerTag(byte[] data, int start, int length, boolean tagOnly) throws CryptoException {
    if (!hasMACKey()) {
      throw new CryptoException(CryptoException.NO_KEY);
    }
    byte[] firstPass = hasher.addBytes(iPadKey).addBytes(data, start, length).computeHash();
    hasher.addBytes(oPadKey).addBytes(firstPass);
    CryptoUtils.fillWithZeroes(firstPass);
    if (tagOnly) {
      return hasher.computeHash();
    } else {
      byte[] out = new byte[length + hashByteLength];
      System.arraycopy(data, start, out, 0, length);
      return hasher.computeHash(out, length);
    }
  }
  
  @Override
  public StreamRunnable checkTag(final boolean checkTagOnly) {
    return new StreamRunnable(){
      @Override
      public void process(InputStream in, OutputStream out) throws IOException {
        innerStream(checkTagOnly, false, in, out);
      }
    };
  }

  @Override
  public byte[] checkTag(byte[] data, boolean checkTagOnly) throws CryptoException {
    int len = data.length;
    int unTaggedLen = len - hashByteLength;
    byte[] tag = Arrays.copyOfRange(data, unTaggedLen, len);
    
    byte[] computedTag = innerTag(data, 0, unTaggedLen, true);
    
    if (CryptoUtils.arrayEquals(computedTag, tag)) {
      if (checkTagOnly) {
        return new byte[0];
      } else {
        return Arrays.copyOf(data, unTaggedLen);
      }
    } else {
      throw new CryptoException(CryptoException.MAC_DOES_NOT_MATCH);
    }
  }

  @Override
  public void setMACKey(byte[] key) {
    byte[] padded = new byte[blockBytes];

    if (key.length > blockBytes) {
      key = hasher.addBytes(key).computeHash();
    }

    if (key.length < blockBytes) {
      // right pad with zereos
      System.arraycopy(key, 0, padded, 0, key.length);
      key = padded;
    }

    iPadKey = new byte[blockBytes];
    Arrays.fill(iPadKey, I_PAD_BYTE);
    CryptoUtils.xorByteArrays(iPadKey, key, iPadKey);

    oPadKey = new byte[blockBytes];
    Arrays.fill(oPadKey, O_PAD_BYTE);
    CryptoUtils.xorByteArrays(oPadKey, key, oPadKey);
  }

  @Override
  public int getTagLength() {
    return hashByteLength;
  }

  @Override
  public boolean hasMACKey() {
    return iPadKey != null;
  }

  @Override
  public void removeMACKey() {
    CryptoUtils.fillWithZeroes(iPadKey);
    CryptoUtils.fillWithZeroes(oPadKey);
    iPadKey = null;
    oPadKey = null;
  }
}
