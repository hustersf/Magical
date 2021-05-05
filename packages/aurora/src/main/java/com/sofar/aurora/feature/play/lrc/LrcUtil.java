package com.sofar.aurora.feature.play.lrc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.text.format.DateUtils;

/**
 * 歌词文本示例
 * <p>
 * ---------
 * [ti:]
 * [ar:陈小春]
 * [al:97古惑仔战无不胜]
 * [by:]
 * [offset:0]
 * [00:00.17]乱世巨星 - 陈小春
 * [00:01.46]词：Wasabi
 * [00:01.88]曲：伍乐城
 * [00:02.89]
 * [00:19.55]叱咤风云 我任意闯万众仰望
 * [00:23.79]叱咤风云 我绝不需往后看
 * [00:28.18]翻天覆地 我定我写自我的法律
 * [00:34.70]这凶悍闪烁眼光的野狼
 * ---------
 */
public class LrcUtil {

  public static List<LrcEntry> parseLrcList(String s) {
    List<LrcEntry> lrcList = new ArrayList<>();
    String[] ss = s.split("\n");
    for (int i = 0; i < ss.length; i++) {
      if ((ss[i].indexOf("[ar:") != -1) || (ss[i].indexOf("[ti:") != -1)
        || (ss[i].indexOf("[by:") != -1)
        || (ss[i].indexOf("[al:") != -1)
        || (ss[i].indexOf("[offset:") != -1) || s.equals(" ")) {
        continue;
      }

      ss[i] = ss[i].replace("[", "");

      // 关键代码，歌词用的时候需要对时间进行排序
      String splitLrc_data[] = ss[i].split("]");
      if (splitLrc_data.length > 1) {
        for (int j = 0; j < splitLrc_data.length - 1; j++) {
          long time = TimeStr(splitLrc_data[j]);
          String text = splitLrc_data[splitLrc_data.length - 1];
          LrcEntry item = new LrcEntry(time, text);
          lrcList.add(item);
        }
      }
    }

    Collections.sort(lrcList);
    return lrcList;
  }

  private static int TimeStr(String timeStr) {
    timeStr = timeStr.replace(":", ".");
    timeStr = timeStr.replace(".", "@");
    String timeData[] = timeStr.split("@");
    int currentTime = 0;
    // 分离出分、秒并转换为整型
    try {
      int minute = Integer.parseInt(timeData[0]);
      int second = Integer.parseInt(timeData[1]);
      int millisecond = Integer.parseInt(timeData[2]);
      currentTime = (minute * 60 + second) * 1000 + millisecond * 10;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return currentTime;
  }

  private static List<LrcEntry> parseLine(String line) {
    if (TextUtils.isEmpty(line)) {
      return null;
    }

    line = line.trim();
    Matcher lineMatcher = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d\\d\\])+)(.+)").matcher(line);
    if (!lineMatcher.matches()) {
      return null;
    }

    String times = lineMatcher.group(1);
    String text = lineMatcher.group(3);
    List<LrcEntry> entryList = new ArrayList<>();

    Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)\\]").matcher(times);
    while (timeMatcher.find()) {
      long min = Long.parseLong(timeMatcher.group(1));
      long sec = Long.parseLong(timeMatcher.group(2));
      long mil = Long.parseLong(timeMatcher.group(3));
      long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;
      entryList.add(new LrcEntry(time, text));
    }
    return entryList;
  }
}
