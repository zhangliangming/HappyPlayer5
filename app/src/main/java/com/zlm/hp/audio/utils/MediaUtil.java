package com.zlm.hp.audio.utils;

import java.text.DecimalFormat;

public class MediaUtil
{
  public static String getFileExt(String filePath)
  {
    int pos = filePath.lastIndexOf(".");
    if (pos == -1) {
      return "";
    }
    return filePath.substring(pos + 1).toLowerCase();
  }
  
  public static String formatTime(int progress)
  {
    progress /= 1000;
    int minute = progress / 60;
    
    int second = progress % 60;
    minute %= 60;
    return String.format("%02d:%02d", new Object[] { Integer.valueOf(minute), Integer.valueOf(second) });
  }
  
  public static String getFileSize(long fileS)
  {
    DecimalFormat df = new DecimalFormat("#.00");
    String fileSizeString = "";
    if (fileS < 1024L) {
      fileSizeString = df.format(fileS) + "B";
    } else if (fileS < 1048576L) {
      fileSizeString = df.format(fileS / 1024.0D) + "K";
    } else if (fileS < 1073741824L) {
      fileSizeString = df.format(fileS / 1048576.0D) + "M";
    } else {
      fileSizeString = df.format(fileS / 1.073741824E9D) + "G";
    }
    return fileSizeString;
  }
}
