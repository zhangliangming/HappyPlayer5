package com.zlm.hp.audio;

import java.io.File;

public class TrackInfo
{
  private int sampleRate;
  private int channels;
  private int bps;
  private int bitrate;
  private long totalSamples;
  private boolean cueEmbedded;
  private String cueLocation;
  private String codec;
  private String encoder;
  private String filePath;
  private String fileExt;
  private long playedProgress;
  private long duration;
  private String durationStr;
  private long fileSize;
  private String fileSizeStr;
  private int frameSize;
  
  public int getSampleRate()
  {
    return this.sampleRate;
  }
  
  public void setSampleRate(int sampleRate)
  {
    this.sampleRate = sampleRate;
  }
  
  public int getChannels()
  {
    return this.channels;
  }
  
  public void setChannels(int channels)
  {
    this.channels = channels;
  }
  
  public int getBps()
  {
    return this.bps;
  }
  
  public void setBps(int bps)
  {
    this.bps = bps;
  }
  
  public int getBitrate()
  {
    return this.bitrate;
  }
  
  public void setBitrate(int bitrate)
  {
    this.bitrate = bitrate;
  }
  
  public long getTotalSamples()
  {
    return this.totalSamples;
  }
  
  public void setTotalSamples(long totalSamples)
  {
    this.totalSamples = totalSamples;
  }
  
  public String getFilePath()
  {
    return this.filePath;
  }
  
  public void setFilePath(String filePath)
  {
    this.filePath = filePath;
  }
  
  public String getFileExt()
  {
    return this.fileExt;
  }
  
  public void setFileExt(String fileExt)
  {
    this.fileExt = fileExt;
  }
  
  public boolean isCueEmbedded()
  {
    return this.cueEmbedded;
  }
  
  public void setCueEmbedded(boolean cueEmbedded)
  {
    this.cueEmbedded = cueEmbedded;
  }
  
  public String getCueLocation()
  {
    return this.cueLocation;
  }
  
  public void setCueLocation(String cueLocation)
  {
    this.cueLocation = cueLocation;
  }
  
  public String getCodec()
  {
    return this.codec;
  }
  
  public void setCodec(String codec)
  {
    this.codec = codec;
  }
  
  public String getEncoder()
  {
    return this.encoder;
  }
  
  public void setEncoder(String encoder)
  {
    this.encoder = encoder;
  }
  
  public long getPlayedProgress()
  {
    return this.playedProgress;
  }
  
  public void setPlayedProgress(long playedProgress)
  {
    this.playedProgress = playedProgress;
  }
  
  public long getDuration()
  {
    return this.duration;
  }
  
  public void setDuration(long duration)
  {
    this.duration = duration;
  }
  
  public String getDurationStr()
  {
    return this.durationStr;
  }
  
  public void setDurationStr(String durationStr)
  {
    this.durationStr = durationStr;
  }
  
  public long getFileSize()
  {
    return this.fileSize;
  }
  
  public void setFileSize(long fileSize)
  {
    this.fileSize = fileSize;
  }
  
  public String getFileSizeStr()
  {
    return this.fileSizeStr;
  }
  
  public void setFileSizeStr(String fileSizeStr)
  {
    this.fileSizeStr = fileSizeStr;
  }
  
  public File getFile()
  {
    return new File(getFilePath());
  }
  
  public int getFrameSize()
  {
    return this.frameSize;
  }
  
  public void setFrameSize(int frameSize)
  {
    this.frameSize = frameSize;
  }
}
