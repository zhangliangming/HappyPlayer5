package com.zlm.hp.audio.utils;

public class AudioMathUtil
{
  public static long bytesToSamples(long bytes, int frameSize)
  {
    return Math.round((float)bytes / frameSize);
  }
  
  public static long samplesToBytes(long samples, int frameSize)
  {
    return samples * frameSize;
  }
  
  public static double samplesToMillis(long samples, int sampleRate)
  {
    return Math.round((float)samples / sampleRate * 1000.0F);
  }
  
  public static double bytesToMillis(long bytes, int frameSize, int sampleRate)
  {
    long l = bytesToSamples(bytes, frameSize);
    return samplesToMillis(l, sampleRate);
  }
  
  public static int convertBuffer(byte[] input, int[] output, int len, int sampleSizeInBits)
  {
    int bps = sampleSizeInBits / 8;
    int target = 0;
    int i = 0;
    while (target < len) {
      switch (bps)
      {
      case 1: 
        output[(i++)] = input[(target++)];
        break;
      case 2: 
        output[(i++)] = ((short)(input[(target++)] & 0xFF | input[(target++)] << 8));
        break;
      case 3: 
        output[(i++)] = 
        
          (input[(target++)] & 0xFF | input[(target++)] << 8 & 0xFF00 | input[(target++)] << 16);
      }
    }
    return i;
  }
  
  public static long millisToSamples(long millis, int sampleRate)
  {
    return Math.round((float)millis / 1000.0F * sampleRate);
  }
}
