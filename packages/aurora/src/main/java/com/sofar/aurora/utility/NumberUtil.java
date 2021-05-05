package com.sofar.aurora.utility;

public class NumberUtil {


  public static String formatPlayDuration(int seconds) {
    int h = 0;
    int m = 0;
    int s;
    if (seconds > 60) {
      m = seconds / 60;
      s = seconds % 60;
      if (m > 60) {
        h = m / 60;
        m = m % 60;
      }
    } else {
      s = seconds;
    }

    StringBuffer sb = new StringBuffer();
    if (h > 0) {
      sb.append(h);
      sb.append(":");
    }

    if (m < 10) {
      sb.append("0");
    }
    sb.append(m);
    sb.append(":");

    if (s < 10) {
      sb.append("0");
    }
    sb.append(s);
    return sb.toString();
  }
}
