package com.sofar.utility.cipher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;
import android.util.Log;

import com.sofar.utility.FileUtil;

public class MD5Util {
  private static final String TAG = "Md5Util";

  static final char[] HEX_CHARS = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f'
  };

  private static final char[] HEX_DIGITS = new char[]{
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  private static final char[] FIRST_CHAR = new char[256];
  private static final char[] SECOND_CHAR = new char[256];

  static {
    for (int i = 0; i < 256; i++) {
      FIRST_CHAR[i] = HEX_DIGITS[(i >> 4) & 0xF];
      SECOND_CHAR[i] = HEX_DIGITS[i & 0xF];
    }
  }

  public static String hexdigest(String string) {
    String s = null;
    try {
      s = hexdigest(string.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return s;
  }

  public static String hexdigest(byte[] bytes) {
    String s = null;
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(bytes);
      byte[] tmp = md.digest();
      char[] str = new char[32];
      int k = 0;

      for (int i = 0; i < 16; ++i) {
        byte byte0 = tmp[i];
        str[k++] = HEX_CHARS[byte0 >>> 4 & 15];
        str[k++] = HEX_CHARS[byte0 & 15];
      }

      s = new String(str);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return s;
  }

  public static String md5(String string) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(string.getBytes());
      byte b[] = md.digest();
      int i;
      StringBuilder buf = new StringBuilder();
      for (byte element : b) {
        i = element;
        if (i < 0) {
          i += 256;
        }
        if (i < 16) {
          buf.append("0");
        }
        buf.append(Integer.toHexString(i));
      }
      return buf.toString();
    } catch (Exception e) {
      return null;
    }
  }

  public static String sha1(String str) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      md.update(str.getBytes());
      byte[] digest = md.digest();
      return toHexString(digest, 0, digest.length);
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * get hex string of specified bytes
   */
  public static String toHexString(byte[] bytes, int off, int len) {
    if (bytes == null) {
      throw new NullPointerException("bytes is null");
    }
    if (off < 0 || (off + len) > bytes.length) {
      throw new IndexOutOfBoundsException();
    }
    char[] buff = new char[len * 2];
    int v;
    int c = 0;
    for (int i = 0; i < len; i++) {
      v = bytes[i + off] & 0xff;
      buff[c++] = HEX_CHARS[(v >> 4)];
      buff[c++] = HEX_CHARS[(v & 0x0f)];
    }
    return new String(buff, 0, len * 2);
  }

  public static byte[] getFileMD5Digest(File file) throws NoSuchAlgorithmException, IOException {
    if (file == null) {
      return null;
    }
    FileInputStream inStream = new FileInputStream(file);
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[4096];
      int readCount;
      while ((readCount = inStream.read(buffer)) != -1) {
        md.update(buffer, 0, readCount);
      }
      return md.digest();
    } catch (Exception e) {
      Log.e(TAG, "getting file md5 digest error.", e);
      return null;
    } finally {
      FileUtil.closeQuietly(inStream);
    }
  }

  // 有IO操作，注意切换线程
  public static byte[] getFileMD5Digest(String path) throws NoSuchAlgorithmException, IOException {
    if (TextUtils.isEmpty(path)) {
      return null;
    }
    File file = new File(path);
    return getFileMD5Digest(file);
  }

  // 有IO操作，注意切换线程
  public static String getFileMD5(String path) {
    if (TextUtils.isEmpty(path)) {
      return null;
    }
    File file = new File(path);
    return getFileMD5(file);
  }

  // 有IO操作，注意切换线程
  public static String getFileMD5(File file) {
    try {
      byte[] fileMD5Digest = getFileMD5Digest(file);
      if (fileMD5Digest == null || fileMD5Digest.length == 0) {
        return null;
      }
      return encodeHex(fileMD5Digest, true);
    } catch (NoSuchAlgorithmException | IOException e) {
      Log.e(TAG, "cannot calculate md5 of file", e);
    }
    return null;
  }

  private static String encodeHex(byte[] array, boolean zeroTerminated) {
    char[] cArray = new char[array.length * 2];

    int j = 0;
    for (int i = 0; i < array.length; i++) {
      int index = array[i] & 0xFF;
      if (index == 0 && zeroTerminated) {
        break;
      }

      cArray[j++] = FIRST_CHAR[index];
      cArray[j++] = SECOND_CHAR[index];
    }

    return new String(cArray, 0, j);
  }
}
