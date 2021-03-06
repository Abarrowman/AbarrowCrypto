package me.abarrow.core;

import java.io.IOException;
import java.util.Arrays;

public class CryptoUtils {
  
  public static final byte[] EMPTY_BYTES = new byte[0];

  public static final byte ONE_AND_SEVEN_ZEROES_BYTE = (byte) 0x80;
  public static final byte ZERO_BYTE = (byte) 0;
  public static final byte ONE_BYTE = (byte) 1;
  
  private static char[] NIBBLE_CHARS = new char[] {
      '0',
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      'a',
      'b',
      'c',
      'd',
      'e',
      'f'
  };

  private static int[] LAST_N_BITS = new int[] {
      // [0] 0000 0000
      0x0,
      // [1] 0000 0001
      0x1,
      // [2] 0000 0011
      0x3,
      // [3] 0000 0111
      0x7,
      // [4] 0000 1111
      0xf,
      // [5] 0001 1111
      0x1f,
      // [6] 0011 1111
      0x3f,
      // [7] 0111 1111
      0x7f,
      // [8] 1111 1111
      0xff };

  private static int[] FIRST_N_BITS = new int[] {
      // [0] 0000 0000
      0x0,
      // [1] 1000 0000
      0x80,
      // [2] 1100 0000
      0xc0,
      // [3] 1110 0000
      0xe0,
      // [4] 1111 0000
      0xf0,
      // [5] 1111 1000
      0xf8,
      // [6] 1111 1100
      0xfc,
      // [7] 1111 1110
      0xfe,
      // [8] 1111 1111
      0xff };

  public static void fillWithZeroes(byte[] data) {
    if (data != null) {
      for (int n = 0; n < data.length; n++) {
        data[n] = CryptoUtils.ZERO_BYTE;
      }
    }
  }

  public static void fillWithZeroes(char[] data) {
    for (int n = 0; n < data.length; n++) {
      data[n] = 0;
    }
  }

  public static void fillWithZeroes(long[] data) {
    for (int n = 0; n < data.length; n++) {
      data[n] = 0;
    }
  }

  public static void fillWithZeroes(int[] data) {
    for (int n = 0; n < data.length; n++) {
      data[n] = 0;
    }
  }
  
  public static byte reverseByteBitOrder(byte x) {
	  // http://graphics.stanford.edu/%7Eseander/bithacks.html#ReverseByteWith64BitsDiv
	  return (byte)(((x  & 0xff) * 0x0202020202L & 0x010884422010L) % 1023L);
  }
  
  public static int reverseIntBitOrder(int a) {
    byte lowest = reverseByteBitOrder((byte)(a & 0xff));
    byte low = reverseByteBitOrder((byte)((a >>> 8) & 0xff));
    byte high = reverseByteBitOrder((byte)((a >>> 16) & 0xff));
    byte highest = reverseByteBitOrder((byte)((a >>> 24) & 0xff));
    
    return (highest & 0xff) | ((high & 0xff) << 8) | ((low & 0xff) << 16) | ((lowest & 0xff) << 24);
  }

  public static int swapEndianness(int x) {
    return CryptoUtils.intFromBytes(CryptoUtils.intToBytes(x, new byte[4], 0, false), 0, true);
  }

  public static long swapEndianness(long x) {
    return CryptoUtils.longFromBytes(CryptoUtils.longToBytes(x, new byte[8], 0, false), 0, true);
  }

  public static int[] swapEndianness(int[] ints) {
    return CryptoUtils.intArrayFromBytes(CryptoUtils.intArrayToByteArray(ints, false), 0, 4 * ints.length, true);
  }
  
  public static byte[] littleBitEndianToLittleEndian(byte[] values) {
    for (int n = 0; n < values.length; n++) {      
      values[n] = (byte)(
          ((values[n] & 0x1) << 7) | 
          ((values[n] & 0x2) << 5) | 
          ((values[n] & 0x4) << 3) | 
          ((values[n] & 0x8) << 1) | 
          ((values[n] & 0x10) >>> 1) | 
          ((values[n] & 0x20) >>> 3) | 
          ((values[n] & 0x40) >>> 5) |
          ((values[n] & 0x80) >>> 7)); 
    }
    return values;
  }

  public static int rotateIntLeft(int x, int digits) {
    return (x << digits) | (x >>> (32 - digits));
  }

  public static int rotateIntRight(int x, int digits) {
    return (x >>> digits) | (x << (32 - digits));
  }

  public static long rotateLongLeft(long x, long digits) {
    return (x << digits) | (x >>> (64 - digits));
  }

  public static long rotateLongRight(long x, long digits) {
    return (x >>> digits) | (x << (64 - digits));
  }

  public static byte[] longToBytes(long val, byte[] bytes, int start) {
    longToBytes(val, bytes, start, false);
    return bytes;
  }

  public static byte[] longToBytes(long val, byte[] bytes, int start, boolean isLittleEndian) {
    if (isLittleEndian) {
      bytes[start + 7] = (byte) ((val >>> 56) & 0xff);
      bytes[start + 6] = (byte) ((val >>> 48) & 0xff);
      bytes[start + 5] = (byte) ((val >>> 40) & 0xff);
      bytes[start + 4] = (byte) ((val >>> 32) & 0xff);
      bytes[start + 3] = (byte) ((val >>> 24) & 0xff);
      bytes[start + 2] = (byte) ((val >>> 16) & 0xff);
      bytes[start + 1] = (byte) ((val >>> 8) & 0xff);
      bytes[start] = (byte) (val & 0xff);
    } else {
      bytes[start] = (byte) ((val >>> 56) & 0xff);
      bytes[start + 1] = (byte) ((val >>> 48) & 0xff);
      bytes[start + 2] = (byte) ((val >>> 40) & 0xff);
      bytes[start + 3] = (byte) ((val >>> 32) & 0xff);
      bytes[start + 4] = (byte) ((val >>> 24) & 0xff);
      bytes[start + 5] = (byte) ((val >>> 16) & 0xff);
      bytes[start + 6] = (byte) ((val >>> 8) & 0xff);
      bytes[start + 7] = (byte) (val & 0xff);
    }
    return bytes;
  }

  public static byte[] utf16CharArrayToByteAray(char[] input, byte[] output) {
    for (int i = 0; (i < output.length) && ((i / 2) < input.length); i++) {
      int letter = input[i / 2];
      output[i] = (byte) ((letter >>> (8 * (1 - i % 2))) & 0xff);
    }
    return output;
  }

  public static byte[] utf8CharArrayToByteAray(char[] input, byte[] output) {
    for (int i = 0; i < (output.length) && (i < input.length); i++) {
      output[i] = (byte) (input[i] & 0xff);
    }
    return output;
  }

  public static byte[] intToBytes(int val, byte[] bytes, int start) {
    return intToBytes(val, bytes, start, false);
  }

  public static void xorIntArray(int[] ints, int val) {
    for (int i = 0; i < ints.length; i++) {
      ints[i] ^= val;
    }
  }

  public static int[] xorIntArray(int[] a, int[] b, int[] out) {
    for (int i = 0; i < a.length; i++) {
      out[i] = a[i] ^ b[i];
    }
    return out;
  }

  public static byte[] intToBytes(int val, boolean isLittleEndian) {
    return intToBytes(val, new byte[4], 0, isLittleEndian);
  }

  public static byte[] longToBytes(long val, boolean isLittleEndian) {
    return longToBytes(val, new byte[8], 0, isLittleEndian);
  }

  public static byte[] intToBytes(int val, byte[] bytes, int start, boolean isLittleEndian) {
    if (isLittleEndian) {
      bytes[start + 3] = (byte) ((val >>> 24) & 0xff);
      bytes[start + 2] = (byte) ((val >>> 16) & 0xff);
      bytes[start + 1] = (byte) ((val >>> 8) & 0xff);
      bytes[start] = (byte) (val & 0xff);
    } else {
      bytes[start] = (byte) ((val >>> 24) & 0xff);
      bytes[start + 1] = (byte) ((val >>> 16) & 0xff);
      bytes[start + 2] = (byte) ((val >>> 8) & 0xff);
      bytes[start + 3] = (byte) (val & 0xff);
    }
    return bytes;
  }

  public static int[] intArrayFromBytes(byte[] bytes, int start, int byteLength) {
    return intArrayFromBytes(bytes, start, byteLength, false);
  }

  public static int[] intArrayFromBytes(byte[] bytes, int start, int byteLength, boolean isLittleEndian) {
    return intArrayFromBytes(new int[byteLength / 4], 0, bytes, start, byteLength, isLittleEndian);
  }

  public static int[] intArrayFromBytes(int[] result, int intStart, byte[] bytes, int byteStart, int byteLength,
      boolean isLittleEndian) {
    for (int i = intStart, max = intStart + byteLength / 4, j = byteStart; i < max; i++, j += 4) {
      result[i] = CryptoUtils.intFromBytes(bytes, j, isLittleEndian);
    }

    return result;
  }

  public static long[] longArrayFromBytes(byte[] bytes, int start, int byteLength) {
    return longArrayFromBytes(bytes, start, byteLength, false);
  }

  public static long[] longArrayFromBytes(byte[] bytes, int start, int byteLength, boolean isLittleEndian) {
    return longArrayFromBytes(new long[byteLength / 8], 0, bytes, start, byteLength / 8, isLittleEndian);
  }

  public static long[] longArrayFromBytes(long[] result, int resultStart, byte[] bytes, int byteStart,
      int resultLength, boolean isLittleEndian) {

    for (int i = 0; i < resultLength; i++) {
      result[resultStart + i] = CryptoUtils.longFromBytes(bytes, byteStart + i * 8, isLittleEndian);
    }

    return result;
  }

  public static long[] xorLongArrayFromBytes(long[] result, int resultStart, byte[] bytes, int byteStart,
      int resultLength, boolean isLittleEndian) {

    for (int i = 0; i < resultLength; i++) {
      result[resultStart + i] ^= CryptoUtils.longFromBytes(bytes, byteStart + i * 8, isLittleEndian);
    }

    return result;
  }

  public static int intFromBytes(byte[] bytes, int start) {
    return CryptoUtils.intFromBytes(bytes, start, false);
  }

  /*
   */
  public static int circularlyExtractIntFromBytes(byte[] bytes, int[] start) {
    // big endian
    int startVal = start[0];
    int data = 0;
    for (int q = 0; q < 4; q++) {
      data = (data << 8) | (bytes[startVal] & 0xff);
      startVal = (startVal + 1) % bytes.length;
    }
    start[0] = startVal;
    return data;
  }

  public static int intFromBytes(byte[] bytes, int start, boolean isLittleEndian) {
    if (isLittleEndian) {
      return ((bytes[start + 3] & 0xff) << 24) + ((bytes[start + 2] & 0xff) << 16) + ((bytes[start + 1] & 0xff) << 8)
          + (bytes[start] & 0xff);
    } else {
      return ((bytes[start] & 0xff) << 24) + ((bytes[start + 1] & 0xff) << 16) + ((bytes[start + 2] & 0xff) << 8)
          + (bytes[start + 3] & 0xff);
    }

  }

  public static long longFromBytes(byte[] bytes, int start) {
    return longFromBytes(bytes, start, false);
  }

  public static long longFromBytes(byte[] bytes, int start, boolean isLittleEndian) {
    if (isLittleEndian) {
      return ((bytes[start + 7] & 0xffL) << 56) + ((bytes[start + 6] & 0xffL) << 48)
          + ((bytes[start + 5] & 0xffL) << 40) + ((bytes[start + 4] & 0xffL) << 32)
          + ((bytes[start + 3] & 0xffL) << 24) + ((bytes[start + 2] & 0xffL) << 16) + ((bytes[start + 1] & 0xffL) << 8)
          + (bytes[start] & 0xffL);
    } else {
      return ((bytes[start] & 0xffL) << 56) + ((bytes[start + 1] & 0xffL) << 48) + ((bytes[start + 2] & 0xffL) << 40)
          + ((bytes[start + 3] & 0xffL) << 32) + ((bytes[start + 4] & 0xffL) << 24)
          + ((bytes[start + 5] & 0xffL) << 16) + ((bytes[start + 6] & 0xffL) << 8) + (bytes[start + 7] & 0xffL);
    }
  }
  
  public static int safeLittleBitEndianIntFromBytes(byte[] bytes, int start) {
    int output = 0;    
    int idx = start;
    for (int i = 0; (i < 4) && (idx < bytes.length); i++) {
      output |= (reverseByteBitOrder(bytes[idx]) & 0xff) << (8 * i);
      idx++;
    }
    return output;
  }
  
  public static int safeLittleEndianIntFromBytes(byte[] bytes, int start) {
    int output = 0;    
    int idx = start;
    for (int i = 0; (i < 4) && (idx < bytes.length); i++) {
      output |= (bytes[idx] & 0xff) << (8 * i);
      idx++;
    }
    return output;
  }
  
  public static int safeBigEndianIntFromBytes(byte[] bytes, int endIndex) {
    int output = 0;    
    int idx = endIndex;
    for (int i = 0; (i < 4) && (idx >= 0); i++) {
      output |= (bytes[idx] & 0xff) << (8 * i);
      idx--;
    }
    return output;
  }

  public static long safeLongFromBytes(byte[] bytes, int start) {
    long output = 0;

    int index = start;

    int shift = 56;

    while ((bytes.length > index) && (shift >= 0)) {
      output += (bytes[index] & 0xffL) << shift;
      index++;
      shift -= 8;
    }

    return output;
  }

  public static byte[] safeLongToBytes(long val, byte[] bytes, int start, boolean isLittleEndian) {
    int index = start;
    if (isLittleEndian) {
      int shift = 0;

      while ((bytes.length > index) && (shift <= 56)) {
        bytes[index] = (byte) ((val >> shift) & 0xffL);
        index++;
        shift += 8;
      }
    } else {
      int shift = 56;

      while ((bytes.length > index) && (shift >= 0)) {
        bytes[index] = (byte) ((val >> shift) & 0xffL);
        index++;
        shift -= 8;
      }
    }

    return bytes;
  }

  public static byte[] intArrayToByteArray(int[] ints) {
    return intArrayToByteArray(ints, false);
  }

  public static byte[] intArrayToByteArray(int[] ints, boolean isLittleEndian) {
    return intArrayToByteArray(new byte[ints.length * 4], 0, ints, isLittleEndian);
  }

  public static byte[] intArrayToByteArray(byte[] bytes, int byteStart, int[] ints, boolean isLittleEndian) {
    for (int i = 0; i < ints.length; i++) {
      intToBytes(ints[i], bytes, byteStart + i * 4, isLittleEndian);
    }
    return bytes;
  }

  public static int[] longArrayToIntArray(long[] input, int[] output) {
    for (int i = 0, j = 0, end = input.length; i < end; i++, j += 2) {
      output[j] = (int) (input[i] >>> 32);
      output[j + 1] = (int) (input[i] & 0xffffffff);
    }
    return output;
  }

  public static byte[] longArrayToByteArray(long[] longs) {
    return longArrayToByteArray(longs, false);
  }

  public static byte[] longArrayToByteArray(long[] longs, boolean isLittleEndian) {
    return longArrayToByteArray(new byte[longs.length * 8], 0, longs, longs.length, isLittleEndian);
  }

  public static byte[] longArrayToByteArray(byte[] bytes, int byteStart, long[] longs, int numLongs,
      boolean isLittleEndian) {
    for (int i = 0, j = byteStart; i < numLongs; i++, j += 8) {
      longToBytes(longs[i], bytes, j, isLittleEndian);
    }
    return bytes;
  }

  public static byte[] safeLongArrayToByteArray(byte[] bytes, int byteStart, long[] longs, int numLongs,
      boolean isLittleEndian) {
    for (int i = 0; i < numLongs; i++) {
      safeLongToBytes(longs[i], bytes, byteStart + i * 8, isLittleEndian);
    }
    return bytes;
  }

  public static String intArrayToHexString(int[] ints) {
    StringBuilder s = new StringBuilder();

    for (int i = 0; i < ints.length; i++) {
      s.append(String.format("%08x", ints[i]));
    }

    return s.toString();
  }

  public static String longArrayToHexString(long[] longs) {
    StringBuilder s = new StringBuilder();

    for (int i = 0; i < longs.length; i++) {

      s.append(String.format("%016x", longs[i]));
    }

    return s.toString();
  }

  public static String byteArrayToHexString(byte[] bytes) {
    //return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    
    StringBuilder builder = new StringBuilder();
    
    for (byte b : bytes) {
      int iv = b & 0xff;
      int low = iv & 0xf;
      int high = (iv >>> 4) & 0xf;
      builder.append(NIBBLE_CHARS[high]);
      builder.append(NIBBLE_CHARS[low]);
    }
    
    return builder.toString();
  }
  
  private static final int parseHexChar(int codePoint) {
    if ((codePoint >= 'a') && (codePoint <= 'f')) {
      return 10 + codePoint - 'a';
    } else if ((codePoint >= '0') && (codePoint <= '9')) {
      return codePoint - '0';
    } else {
      throw new IllegalArgumentException("Cannot parse hex char [" + new String(Character.toChars(codePoint)) + "]");
    }
  }

  public static byte[] parseHexString(String hex) {
    int[] codePoints = hex.toLowerCase().codePoints().toArray();
    byte[] out = new byte[codePoints.length / 2];
    for (int n = 0; n < out.length; n++) {
      out[n] = (byte)((parseHexChar(codePoints[2 * n]) << 4) | parseHexChar(codePoints[2 * n + 1]));
    }
    return out;
  }

  public static String hexStringToBinaryString(String hex) {
    String binaryBlock = hex.replace("0", "0000").replace("1", "0001").replace("2", "0010").replace("3", "0011")
        .replace("4", "0100").replace("5", "0101").replace("6", "0110").replace("7", "0111").replace("8", "1000")
        .replace("9", "1001").replace("a", "1010").replace("b", "1011").replace("c", "1100").replace("d", "1101")
        .replace("e", "1110").replace("f", "1111");

    StringBuilder binaryString = new StringBuilder();

    for (int i = 0; (i + 7) < binaryBlock.length(); i += 8) {
      if (i != 0) {
        binaryString.append(' ');
      }
      binaryString.append(binaryBlock.substring(i, i + 8));
    }

    return binaryString.toString();
  }

  public static String byteArrayToBinaryString(byte[] bytes) {
    return hexStringToBinaryString(byteArrayToHexString(bytes));
  }

  public static int intCh(int x, int y, int z) {
    return (x & y) ^ ((~x) & z);
  }

  public static int intParity(int x, int y, int z) {
    return x ^ y ^ z;
  }

  public static int intMaj(int x, int y, int z) {
    return (x & y) ^ (x & z) ^ (y & z);
  }

  public static long longCh(long x, long y, long z) {
    return (x & y) ^ ((~x) & z);
  }

  public static long longMaj(long x, long y, long z) {
    return (x & y) ^ (x & z) ^ (y & z);
  }

  public static long upperSigmaZeroFiveTwelve(long x) {
    return CryptoUtils.rotateLongRight(x, 28) ^ CryptoUtils.rotateLongRight(x, 34) ^ CryptoUtils.rotateLongRight(x, 39);
  }

  public static long upperSigmaOneFiveTwelve(long x) {
    return CryptoUtils.rotateLongRight(x, 14) ^ CryptoUtils.rotateLongRight(x, 18) ^ CryptoUtils.rotateLongRight(x, 41);
  }

  public static long lowerSigmaZeroFiveTwelve(long x) {
    return CryptoUtils.rotateLongRight(x, 1) ^ CryptoUtils.rotateLongRight(x, 8) ^ (x >>> 7);
  }

  public static long lowerSigmaOneFiveTwelve(long x) {
    return CryptoUtils.rotateLongRight(x, 19) ^ CryptoUtils.rotateLongRight(x, 61) ^ (x >>> 6);
  }
  
  public static int upperSigmaZeroTwoFiftySix(int x) {
    return CryptoUtils.rotateIntRight(x, 2) ^ CryptoUtils.rotateIntRight(x, 13) ^ CryptoUtils.rotateIntRight(x, 22);
  }

  public static int upperSigmaOneTwoFiftySix(int x) {
    return CryptoUtils.rotateIntRight(x, 6) ^ CryptoUtils.rotateIntRight(x, 11) ^ CryptoUtils.rotateIntRight(x, 25);
  }

  public static int lowerSigmaZeroTwoFiftySix(int x) {
    return CryptoUtils.rotateIntRight(x, 7) ^ CryptoUtils.rotateIntRight(x, 18) ^ (x >>> 3);
  }

  public static int lowerSigmaOneTwoFiftySix(int x) {
    return CryptoUtils.rotateIntRight(x, 17) ^ CryptoUtils.rotateIntRight(x, 19) ^ (x >>> 10);
  }

  public static byte[] xorByteArrays(byte[] a, byte[] b) {
    if (a.length != b.length) {
      throw new IllegalArgumentException("Cannot xor byte arrays of unequal lenghts.");
    }
    return CryptoUtils.xorByteArrays(a, b, new byte[a.length]);
  }

  public static byte[] fillLastBytes(byte[] source, byte[] target, int maxToFill) {

    int startingByte = source.length - maxToFill;
    startingByte = (startingByte < 0) ? 0 : startingByte;
    int bytesToCopy = source.length - startingByte;

    System.arraycopy(source, startingByte, target, target.length - bytesToCopy, bytesToCopy);

    return target;
  }

  public static byte[] xorByteArrays(byte[] a, byte[] b, byte[] c) {
    return CryptoUtils.xorByteArrays(a, 0, b, 0, c, 0, a.length);
  }

  public static byte[] xorByteArrays(byte[] a, int aStart, byte[] b, int bStart, byte[] c, int cStart, int length) {
    for (int i = 0; i < length; i++) {
      c[i + cStart] = (byte) ((a[i + aStart] ^ b[i + bStart]) & 0xff);
    }
    return c;
  }

  public static byte[] permuteByteArray(byte[] input, byte[] output, int[] permutation) {
    return permuteByteArray(input, 0, output, 0, permutation, input.length);
  }

  public static byte[] permuteByteArray(byte[] input, int srcPos, byte[] output, int destPos, int[] permutation,
      int length) {

    for (int i = 0; i < length; i++) {
      output[destPos + i] = input[srcPos + permutation[i]];
    }
    return output;
  }

  public static int getBitFromByteArray(byte[] bytes, int bitIndex) {
    int bitPos = bitIndex % 8;
    return ((bytes[bitIndex / 8] & FIRST_N_BITS[bitPos + 1]) >>> (7 - bitPos)) & 0x1;
  }

  public static byte[] permuteByteArrayByBit(byte[] input, int srcPos, byte[] output, int[] permutation) {
    int c = 0;
    int n = 0;
    boolean going = true;
    while (going) {
      int val = 0;
      for (int i = 0; i < 8; i++) {
        // we've exhausted all the bits
        if (c >= permutation.length) {
          going = false;
          break;
        }

        // add the new bit
        val |= getBitFromByteArray(input, srcPos + permutation[c]) << (7 - i);
        c++;
      }
      if (output.length > n) {
        output[n] = (byte) val;
        n++;
      }
    }

    return output;
  }

  public static int permuteIntByBit(int input, int srcPos, int[] permutation) {
    int output = 0;
    for (int n = 0; n < permutation.length; n++) {
      output += ((input >>> (31 - permutation[n] - srcPos)) & 0x1) << (31 - n);
    }
    return output;
  }

  public static long permuteIntByBitToLong(int input, int srcPos, int[] permutation) {
    long inputAsLong = input;
    long output = 0;
    for (int n = 0; n < permutation.length; n++) {
      output += ((inputAsLong >>> (31 - permutation[n] - srcPos)) & 0x1) << (63 - n);
    }
    return output;
  }

  public static byte[] copyBitsFromByteArray(byte[] input, int startingSrcBit, int bitLength, byte[] output,
      int startingDestBit) {

    int currentDestBit = startingDestBit;

    int currentSrcBit = startingSrcBit;

    int lastSrcBit = startingSrcBit + bitLength;

    int srcBitsLeft = lastSrcBit - currentSrcBit;

    while (srcBitsLeft > 0) {
      int currentDestByte = currentDestBit / 8;
      int currestDestByteBitPos = currentDestBit - currentDestByte * 8;
      int bitsLeftOnDestByte = 8 - currestDestByteBitPos;

      int currentSrcByte = currentSrcBit / 8;
      int currestSrcByteBitPos = currentSrcBit - currentSrcByte * 8;
      int bitsLeftOnSrcByte = 8 - currestSrcByteBitPos;

      int bitsFromCurrentBytes = getLastBitsFromBytes(input[currentSrcByte] & 0xff, currestSrcByteBitPos,
          bitsLeftOnSrcByte, currestDestByteBitPos);

      int bitsToOrWithDest = Math.min(bitsLeftOnDestByte, Math.min(bitsLeftOnSrcByte, srcBitsLeft));

      output[currentDestByte] |= (bitsFromCurrentBytes & (FIRST_N_BITS[bitsToOrWithDest] >> currestDestByteBitPos));

      currentDestBit += bitsToOrWithDest;
      currentSrcBit += bitsToOrWithDest;

      srcBitsLeft -= bitsToOrWithDest;
    }

    return output;
  }

  public static byte[] rotateByteArrayRight(byte[] input, int bitLength, int bitsRight, byte[] output) {
    return copyBitsFromByteArray(input, bitLength - bitsRight, bitsRight, copyBitsFromByteArray(input, 0, bitLength
        - bitsRight, output, bitsRight), 0);
  }

  public static byte[] rotateByteArrayLeft(byte[] input, int bitLength, int bitsLeft, byte[] output) {
    return copyBitsFromByteArray(input, bitsLeft, bitLength - bitsLeft, copyBitsFromByteArray(input, 0, bitsLeft,
        output, bitLength - bitsLeft), 0);
  }

  public static int getLastBitsFromBytes(int byteInt, int start, int len) {
    return getLastBitsFromBytes(byteInt, start, len, 0);
  }

  public static int getLastBitsFromBytes(int byteInt, int start, int len, int pos) {
    int res = byteInt & LAST_N_BITS[len];
    return leftOrRightBitShift(res, pos - start);
  }

  public static int leftOrRightBitShift(int src, int digitsToShiftRightIsPositive) {
    if (digitsToShiftRightIsPositive > 0) {
      return src >>> digitsToShiftRightIsPositive;
    } else {
      return src << (-digitsToShiftRightIsPositive);
    }
  }

  public static int[] integerRange(int start, int end) {
    if (end < start) {
      throw new IllegalArgumentException("The end is less then the start!");
    }
    int[] values = new int[] { end - start + 1 };
    for (int i = 0; i < values.length; i++) {
      values[i] = start + i;
    }
    return values;
  }

  public static int multiplyFiniteFieldBytes(int a, int b, int fundPoly) {
    int p = 0;
    for (int n = 0; n < 8; n++) {
      if ((b & 0x01) == 1) {
        p ^= a;
      }
      b = b >>> 1;
      a = a << 1;
      if (a > 255) {
        a ^= fundPoly;
      }
    }
    return p;
  }
  
  public static byte[] multiplyFiniteFields(byte[] a, byte[] b, byte[] p, byte[] truncFundPoly) {
    return multiplyFiniteFieldsModifyingInputs(Arrays.copyOf(a, a.length), Arrays.copyOf(b, b.length), p, truncFundPoly);
  }
  
  public static byte[] rightShiftBitsInByteArray(byte[] bytes, int places) {
    int byteLength = bytes.length;
    int placeBytes = places / 8;
    int placeBits = places % 8;
    int invPlaceBits = 8 - placeBits;
    for(int n = byteLength - 1; n >= 0; n--) {
      int near = ((n - placeBytes) < 0) ? 0 : bytes[n - placeBytes] & 0xff;
      int far = ((n - placeBytes - 1) < 0) ? 0 : bytes[n - placeBytes - 1] & 0xff;
      bytes[n] = (byte) ((near >>> placeBits) | ((far << invPlaceBits) & 0xff));
    }
    return bytes;
  }
  
  public static byte[] leftShiftBitsInByteArray(byte[] bytes, int places) {
    int byteLength = bytes.length;
    int placeBytes = places / 8;
    int placeBits = places % 8;
    int invPlaceBits = 8 - placeBits;
    for(int n = 0; n < byteLength; n++) {
      int near = ((n + placeBytes) >= byteLength) ? 0 : bytes[n + placeBytes] & 0xff;
      int far = ((n + placeBytes + 1) >= byteLength) ? 0 : bytes[n + placeBytes + 1] & 0xff;
      bytes[n] = (byte) ((far >>> invPlaceBits) | ((near << placeBits) & 0xff));
    }
    return bytes;
  }
  
  public static byte[] multiplyFiniteFieldsModifyingInputs(byte[] a, byte[] b, byte[] p, byte[] truncFundPoly) {
    // This method use little endian bit notation (LS bit first MS bit last)
    CryptoUtils.fillWithZeroes(p);
    
    int bits = a.length * 8;
    int byteLength = a.length;
      
    for (int n = 0; (n < bits); n++) {
      if ((b[0] & 0x80) == 128) {
        CryptoUtils.xorByteArrays(a, 0, p, 0, p, 0, byteLength);
      }
      
      CryptoUtils.leftShiftBitsInByteArray(b, 1);
      int carry = a[0] & 0x1;
      CryptoUtils.rightShiftBitsInByteArray(a, 1);
      if (carry == 1) {
        CryptoUtils.xorByteArrays(a, 0, truncFundPoly, 0, a, 0, byteLength);
      }
    }
    return p;
  }
  
  public static boolean constantTimeArrayEquals(byte[] a, byte[] b) {
    int len = a.length;
    if (len != b.length) {
      return false;
    }
    int orred = 0;
    for (int n = 0; n < len; n++) {
      orred |= a[n] ^ b[n];
    }
    return (orred == 0);
  }
  
  public static boolean constantTimeArrayEquals(int[] a, int[] b) {
    int len = a.length;
    if (len != b.length) {
      return false;
    }
    int orred = 0;
    for (int n = 0; n < len; n++) {
      orred |= a[n] ^ b[n];
    }
    return (orred == 0);
  }
  
  public static boolean constantTimeSubArrayEquals(byte[] a, int aStart, byte[] b, int bStart, int len) {
    int bEnd = len + bStart;
    if ((a.length < (len + aStart)) || (b.length < bEnd)) {
      return false;
    }
    int orred = 0;
    int aIndex = aStart;
    for (int n = aIndex; n < bEnd; n++) {
      orred |= a[aIndex] ^ b[n];
      aIndex++;
    }
    return (orred == 0);
  }
  
  public static byte[] concatArrays(byte[] a, byte[] b) {
    byte[] c = new byte[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
  }

}
