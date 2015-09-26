package me.abarrow.hash.sha;

import java.math.BigInteger;
import java.util.Arrays;

import me.abarrow.core.CryptoUtils;
import me.abarrow.hash.Hasher;

public abstract class SHA32Hash extends Hasher {

  private static final int MIN_PADDING_BYTES = 9;
  protected int[] hash;
  protected int[] W;

  @Override
  protected final byte[] computeHash(BigInteger dataLength, byte[] remainder, int remainderLength) {

    if (remainderLength == 0) {
      fillPadding(dataLength, remainder, 0);
      hashBlock(remainder, 0);
    } else if ((getBlockBytes() - remainderLength) < SHA32Hash.MIN_PADDING_BYTES) {
      remainder[remainderLength] = CryptoUtils.ONE_AND_SEVEN_ZEROES_BYTE;
      hashBlock(remainder, 0);

      Arrays.fill(remainder, 0, remainder.length, (byte) 0);
      appendWithLength(dataLength, remainder);
      hashBlock(remainder, 0);
    } else {
      fillPadding(dataLength, remainder, remainderLength);
      hashBlock(remainder, 0);
    }

    byte[] result = CryptoUtils.intArrayToByteArray(new byte[getHashByteLength()], 0, hash, isHashLittleEndian());
    reset();
    return result;
  }

  private void fillPadding(BigInteger totalLength, byte[] padded, int startIndex) {
    padded[startIndex] = CryptoUtils.ONE_AND_SEVEN_ZEROES_BYTE;
    appendWithLength(totalLength, padded);
  }

  private void appendWithLength(BigInteger totalLength, byte[] padded) {
    CryptoUtils.longToBytes(totalLength.multiply(BigInteger.valueOf(8)).longValue(), padded, padded.length - 8, isHashLittleEndian());
  }

  @Override
  protected void reset() {
    super.reset();
    int[] intialHashes = getInitialHashes();
    if (hash == null) {
      hash = Arrays.copyOf(intialHashes, intialHashes.length);
      W = new int[getWLength()];
    } else {
      System.arraycopy(intialHashes, 0, hash, 0, intialHashes.length);
      CryptoUtils.fillWithZeroes(W);
    }
  }
  
  public boolean isHashLittleEndian() {
    return false;
  }
  
  protected abstract int[] getInitialHashes();
  protected abstract int getWLength();

}
