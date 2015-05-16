package hash.md;

import hash.Hasher;

import java.math.BigInteger;
import java.util.Arrays;

import core.CryptoUtils;

public class MD5 extends Hasher {

  private static final int[] s = new int[] { 7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21, };

  private static final int[] K = new int[] { 0xd76aa478, 0xe8c7b756, 0x242070db, 0xc1bdceee, 0xf57c0faf, 0x4787c62a,
      0xa8304613, 0xfd469501, 0x698098d8, 0x8b44f7af, 0xffff5bb1, 0x895cd7be, 0x6b901122, 0xfd987193, 0xa679438e,
      0x49b40821, 0xf61e2562, 0xc040b340, 0x265e5a51, 0xe9b6c7aa, 0xd62f105d, 0x02441453, 0xd8a1e681, 0xe7d3fbc8,
      0x21e1cde6, 0xc33707d6, 0xf4d50d87, 0x455a14ed, 0xa9e3e905, 0xfcefa3f8, 0x676f02d9, 0x8d2a4c8a, 0xfffa3942,
      0x8771f681, 0x6d9d6122, 0xfde5380c, 0xa4beea44, 0x4bdecfa9, 0xf6bb4b60, 0xbebfbc70, 0x289b7ec6, 0xeaa127fa,
      0xd4ef3085, 0x04881d05, 0xd9d4d039, 0xe6db99e5, 0x1fa27cf8, 0xc4ac5665, 0xf4292244, 0x432aff97, 0xab9423a7,
      0xfc93a039, 0x655b59c3, 0x8f0ccc92, 0xffeff47d, 0x85845dd1, 0x6fa87e4f, 0xfe2ce6e0, 0xa3014314, 0x4e0811a1,
      0xf7537e82, 0xbd3af235, 0x2ad7d2bb, 0xeb86d391 };

  private static final int[] INITIAL_HASHES = new int[] { 0x67452301, 0xefcdab89, 0x98badcfe, 0x10325476 };

  private static final int BLOCK_BITS = 512;
  private static final int BLOCK_BYTES = MD5.BLOCK_BITS / 8;
  private static final int MIN_PADDING_BYTES = 9;

  private int[] hash;

  private int[] M;
  byte[] padded;

  public MD5() {
    reset();
  }

  @Override
  public byte[] computeHash(byte[] out, int start) {

    int copiedLength = toHashPos;

    if (copiedLength == 0) {
      fillPadding(padded, 0);
      hashBlock(padded, 0);
    } else if ((MD5.BLOCK_BYTES - copiedLength) < MD5.MIN_PADDING_BYTES) {
      System.arraycopy(toHash, 0, padded, 0, copiedLength);
      padded[copiedLength] = CryptoUtils.ONE_AND_SEVEN_ZEROES_BYTE;
      hashBlock(padded, 0);

      Arrays.fill(padded, 0, padded.length, (byte) 0);
      appendWithLength(padded);
      hashBlock(padded, 0);
    } else {
      System.arraycopy(toHash, 0, padded, 0, copiedLength);
      fillPadding(padded, copiedLength);
      hashBlock(padded, 0);
    }
    
    CryptoUtils.fillWithZeroes(padded);
    return CryptoUtils.intArrayToByteArray(out, start, hash, true);
    
  }

  private void fillPadding(byte[] padded, int startIndex) {
    padded[startIndex] = CryptoUtils.ONE_AND_SEVEN_ZEROES_BYTE;
    appendWithLength(padded);
  }

  private void appendWithLength(byte[] padded) {
    CryptoUtils.longToBytes(totalLength.multiply(BigInteger.valueOf(8)).longValue(), padded, padded.length - 8, true);
  }

  protected void hashBlock(byte[] bytes, int start) {
    int A = hash[0];
    int B = hash[1];
    int C = hash[2];
    int D = hash[3];

    int temp;
    int F;
    int g;

    for (int i = 0; i < 64; i++) {
      if (i < 16) {
        M[i] = CryptoUtils.intFromBytes(bytes, start + i * 4, true);
        F = (B & C) | ((~B) & D);
        g = i;
      } else if (i < 32) {
        F = (D & B) | ((~D) & C);
        g = (5 * i + 1) % 16;
      } else if (i < 48) {
        F = CryptoUtils.intParity(B, C, D);
        g = (3 * i + 5) % 16;
      } else {
        F = C ^ (B | (~D));
        g = (7 * i) % 16;
      }
      temp = B + CryptoUtils.rotateIntLeft(A + F + MD5.K[i] + M[g], MD5.s[i % 4 + 4 * (i / 16)]);
      A = D;
      D = C;
      C = B;
      B = temp;
    }

    hash[0] = hash[0] + A;
    hash[1] = hash[1] + B;
    hash[2] = hash[2] + C;
    hash[3] = hash[3] + D;
  }

  @Override
  public Hasher reset() {
    super.reset();
    if (hash == null) {
      hash = Arrays.copyOf(MD5.INITIAL_HASHES, 4);
      M = new int[16];
      padded = new byte[MD5.BLOCK_BYTES];
    } else {
      CryptoUtils.fillWithZeroes(padded);
      CryptoUtils.fillWithZeroes(M);
      System.arraycopy(MD5.INITIAL_HASHES, 0, hash, 0, 4);
    }
    return this;
  }

  @Override
  public int getBlockBytes() {
    return MD5.BLOCK_BYTES;
  }

  @Override
  public int getHashByteLength() {
    return 16;
  }

}
