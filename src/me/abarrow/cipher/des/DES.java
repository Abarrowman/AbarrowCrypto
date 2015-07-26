package me.abarrow.cipher.des;

import java.util.Arrays;

import me.abarrow.cipher.BlockCipher;
import me.abarrow.core.CryptoException;
import me.abarrow.core.CryptoUtils;

public class DES extends BlockCipher {

  public static final int ROUNDS = 16;
  private static final int BLOCK_BITS = 64;
  private static final int BLOCK_BYTES = DES.BLOCK_BITS / 8;
  
  public static final int[] KEY_LENGTHS = new int[] {8};

  public static final int[] IP = new int[] { 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46,
      38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3,
      61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7 };

  public static final int[] IP_PRIME = new int[] { 40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6,
      46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59,
      27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25 };

  public static final int[] E_CONSTANTS = new int[] { 32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12,
      13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1 };

  public static final int[] S1 = new int[] { 14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7, 0, 15, 7, 4, 14, 2,
      13, 1, 10, 6, 12, 11, 9, 5, 3, 8, 4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0, 15, 12, 8, 2, 4, 9, 1, 7,
      5, 11, 3, 14, 10, 0, 6, 13 };

  public static final int[] S2 = new int[] { 15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10, 3, 13, 4, 7, 15, 2,
      8, 14, 12, 0, 1, 10, 6, 9, 11, 5, 0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15, 13, 8, 10, 1, 3, 15, 4,
      2, 11, 6, 7, 12, 0, 5, 14, 9 };

  public static final int[] S3 = new int[] { 10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8, 
                                             13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
                                             13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7, 1, 10, 13, 0, 6, 9, 8,
      7, 4, 15, 14, 3, 11, 5, 2, 12 };

  public static final int[] S4 = new int[] { 7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15, 13, 8, 11, 5, 6, 15,
      0, 3, 4, 7, 2, 12, 1, 10, 14, 9, 10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4, 3, 15, 0, 6, 10, 1, 13, 8,
      9, 4, 5, 11, 12, 7, 2, 14 };

  public static final int[] S5 = new int[] { 2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9, 14, 11, 2, 12, 4, 7,
      13, 1, 5, 0, 15, 10, 3, 9, 8, 6, 4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14, 11, 8, 12, 7, 1, 14, 2,
      13, 6, 15, 0, 9, 10, 4, 5, 3 };

  public static final int[] S6 = new int[] { 12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11, 10, 15, 4, 2, 7, 12,
      9, 5, 6, 1, 13, 14, 0, 11, 3, 8, 9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6, 4, 3, 2, 12, 9, 5, 15, 10,
      11, 14, 1, 7, 6, 0, 8, 13 };

  public static final int[] S7 = new int[] { 4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1, 13, 0, 11, 7, 4, 9,
      1, 10, 14, 3, 5, 12, 2, 15, 8, 6, 1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2, 6, 11, 13, 8, 1, 4, 10,
      7, 9, 5, 0, 15, 14, 2, 3, 12 };

  public static final int[] S8 = new int[] { 13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7, 1, 15, 13, 8, 10, 3,
      7, 4, 12, 5, 6, 11, 0, 14, 9, 2, 7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8, 2, 1, 14, 7, 4, 10, 8, 13,
      15, 12, 9, 0, 3, 5, 6, 11 };

  public static final int[][] S_N = new int[][] { S1, S2, S3, S4, S5, S6, S7, S8 };

  public static final int[] P_CONSTANTS = new int[] { 16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2,
      8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25 };

  public static final int[] PC_1_L = new int[] { 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51,
      43, 35, 27, 19, 11, 3, 60, 52, 44, 36 };

  public static final int[] PC_1_R = new int[] { 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53,
      45, 37, 29, 21, 13, 5, 28, 20, 12, 4 };

  public static final int[] PC_2 = new int[] { 14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7,
      27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32 };

  public static final int[] LEFT_SHIFTS = new int[] { 1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1 };
  
  private long[] subKeys;
  
  public DES() {
  }
  
  public DES(byte[] key) {
    setKey(key);
    
  }

  @Override
  public int getBlockBytes() {
    return DES.BLOCK_BYTES;
  }

  private int S(int B, int n) {
    //B is 6 bits
    int row = ((B >>> 4) & 0x2) + (B & 0x1); // first bit of B followed by the last bit of B
    int column = (B >>> 1) & 0xf; //the middle 4 bits of B
    return DES.S_N[n - 1][row * 16 + column];
  }

  private int f(int R, long K) {
    long B = K ^ CryptoUtils.permuteIntByBitToLong(R, -1, DES.E_CONSTANTS);
    int preP = 0;

    for (int i = 1; i <= 8; i++) {           
      //get six bits from B
      //send them through an s bucket and add them to preP
      preP += S((int) (B >>> (64 - 6*i)), i) << (32 - (4 * i));
    }
                    
    int postP = CryptoUtils.permuteIntByBit(preP, -1, DES.P_CONSTANTS);
    return postP;
  }

  @Override
  public byte[] encryptBlock(byte[] input, int srcPos, byte[] output, int destPos) throws CryptoException {
    if (!hasKey()) {
      throw new CryptoException(CryptoException.NO_KEY);
    }
    byte[] premutated = new byte[DES.BLOCK_BYTES];
    byte[] temp = new byte[DES.BLOCK_BYTES];
    CryptoUtils.permuteByteArrayByBit(input, srcPos * 8 - 1, premutated, DES.IP);

    int[] LR = CryptoUtils.intArrayFromBytes(premutated, 0, 8);

    encryptBlock(LR);

    CryptoUtils.intToBytes(LR[0], premutated, 0);
    CryptoUtils.intToBytes(LR[1], premutated, 4);
    
    CryptoUtils.permuteByteArrayByBit(premutated, -1, temp, DES.IP_PRIME);
    
    CryptoUtils.copyBitsFromByteArray(temp, 0, DES.BLOCK_BITS, output, destPos * 8);
    
    Arrays.fill(temp, CryptoUtils.ZERO_BYTE);
    Arrays.fill(premutated, CryptoUtils.ZERO_BYTE);
    return output;
  }

  private void encryptBlock(int[] LR) {

    int L = LR[0];
    int R = LR[1];
    
    int oldL;

    for (int i = 1; i <= DES.ROUNDS; i++) {
      oldL = L;
      L = R;
      // replace with the proper key
      R = oldL ^ f(R, subKeys[i - 1]);
    }

    LR[0] = R;
    LR[1] = L;
  }

  private void decryptBlock(int[] RL) {

    int L = RL[1];
    int R = RL[0];
    
    int oldR;

    for (int i = DES.ROUNDS; i >= 1; i--) {
      oldR = R;
      R = L;
      // replace with the proper key
      L = oldR ^ f(L, subKeys[i - 1]);
    }

    RL[1] = R;
    RL[0] = L;
  }

  @Override
  public byte[] decryptBlock(byte[] input, int srcPos, byte[] output, int destPos) throws CryptoException {
    if (!hasKey()) {
      throw new CryptoException(CryptoException.NO_KEY);
    }
    byte[] premutated = new byte[DES.BLOCK_BYTES];
    byte[] temp = new byte[DES.BLOCK_BYTES];
    CryptoUtils.permuteByteArrayByBit(input, srcPos * 8 - 1, premutated, DES.IP);
    
    int[] RL = CryptoUtils.intArrayFromBytes(premutated, 0, 8);

    decryptBlock(RL);

    CryptoUtils.intToBytes(RL[0], premutated, 0);
    CryptoUtils.intToBytes(RL[1], premutated, 4);
    
    CryptoUtils.permuteByteArrayByBit(premutated, -1, temp, DES.IP_PRIME);
    
    CryptoUtils.copyBitsFromByteArray(temp, 0, DES.BLOCK_BITS, output, destPos * 8);
    
    Arrays.fill(temp, CryptoUtils.ZERO_BYTE);
    Arrays.fill(premutated, CryptoUtils.ZERO_BYTE);
    return output;
  }

  public int[] getValidKeyLengths() {
    return DES.KEY_LENGTHS;
  }

  @Override
  public void removeKey() {
    if (!hasKey()) {
      return;
    }
    CryptoUtils.fillWithZeroes(subKeys);
    subKeys = null;
    
  }

  @Override
  public void setKey(byte[] key) {
    removeKey();
    byte[] premutated = new byte[DES.BLOCK_BYTES];
    subKeys = new long[DES.ROUNDS];
    
    byte[] swap;
    byte[] fortyEightBits = new byte[6];
    byte[] thirtyTwoBits = new byte[4];
    byte[] leftKey = CryptoUtils.permuteByteArrayByBit(key, -1, new byte[4], DES.PC_1_L);
    byte[] rightKey = CryptoUtils.permuteByteArrayByBit(key, -1, new byte[4], DES.PC_1_R);

    for (int n = 0; n < DES.ROUNDS; n++) {      
     
      swap = CryptoUtils.rotateByteArrayLeft(leftKey, 28, DES.LEFT_SHIFTS[n], thirtyTwoBits);
      thirtyTwoBits = leftKey;
      leftKey = swap;
      Arrays.fill(thirtyTwoBits, CryptoUtils.ZERO_BYTE);
      
      swap = CryptoUtils.rotateByteArrayLeft(rightKey, 28, DES.LEFT_SHIFTS[n], thirtyTwoBits);
      thirtyTwoBits = rightKey;
      rightKey = swap;
      Arrays.fill(thirtyTwoBits, CryptoUtils.ZERO_BYTE);
      
      CryptoUtils
          .copyBitsFromByteArray(leftKey, 0, 28, premutated, 0);
      CryptoUtils.copyBitsFromByteArray(rightKey, 0, 28, premutated,
          28);
      
      CryptoUtils.permuteByteArrayByBit(premutated, -1, fortyEightBits, DES.PC_2);
      subKeys[n] = CryptoUtils.safeLongFromBytes(fortyEightBits, 0);
      
      
      Arrays.fill(premutated, CryptoUtils.ZERO_BYTE);
    }
    
    Arrays.fill(thirtyTwoBits, CryptoUtils.ZERO_BYTE);
  }

  @Override
  public boolean hasKey() {
    return subKeys != null;
  }

}
