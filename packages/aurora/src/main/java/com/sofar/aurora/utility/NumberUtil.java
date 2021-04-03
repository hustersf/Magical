package com.sofar.aurora.utility;

public class NumberUtil {

  public static String formatSeconds(int seconds) {
    int h = 0;
    int m = 0;
    int s = 0;
    if (seconds > 60) {
      m = seconds / 60;
      s = seconds % 60;
      if (m > 60) {
        h = m / 60;
        m = m % 60;
      }
    }
    StringBuffer sb = new StringBuffer();
    if (h > 0) {
      sb.append(h);
      sb.append(":");
    }
    if (m > 0) {
      if (m < 10) {
        sb.append("0");
      }
      sb.append(m);
      sb.append(":");
    }

    if (s > 0) {
      if (s < 10) {
        sb.append("0");
      }
      sb.append(s);
    }
    return sb.toString();
  }
}
