package com.zlm.hp.audio.formats.mp3;


import com.zlm.hp.audio.AudioFileReader;
import com.zlm.hp.audio.TrackInfo;

import org.jaudiotagger.audio.mp3.LameFrame;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.audio.mp3.XingFrame;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.nio.charset.Charset;

import davaguine.jmac.info.ID3Tag;

public class MP3FileReader
        extends AudioFileReader {
    private static final int GAPLESS_DELAY = 529;
    protected static Charset defaultCharset = Charset.forName("iso8859-1");

    public TrackInfo readSingle(TrackInfo trackInfo) {
        TextEncoding.getInstanceOf().setDefaultNonUnicode(defaultCharset.name());
        ID3Tag.setDefaultEncoding(defaultCharset.name());
        MP3File mp3File = null;
        try {
            mp3File = new MP3File(trackInfo.getFile(), 14, true);
        } catch (Exception ignored) {
            System.out.println("Couldn't read file: " + trackInfo.getFile());
        }
        ID3v24Tag v24Tag = null;
        if (mp3File != null) {
            MP3AudioHeader mp3AudioHeader = mp3File.getMP3AudioHeader();
            copyHeaderFields(mp3AudioHeader, trackInfo);

            long totalSamples = trackInfo.getTotalSamples();
            int enc_delay = 529;

            XingFrame xingFrame = mp3AudioHeader.getXingFrame();
            if (xingFrame != null) {
                LameFrame lameFrame = xingFrame.getLameFrame();
                if (lameFrame != null) {
                    long length = totalSamples;
                    enc_delay += lameFrame.getEncDelay();
                    int enc_padding = lameFrame.getEncPadding() - 529;
                    if (enc_padding < length) {
                        length -= enc_padding;
                    }
                    if (totalSamples > length) {
                        totalSamples = length;
                    }
                } else {
                    totalSamples += 529L;
                }
            }
            totalSamples -= enc_delay;
            trackInfo.setTotalSamples(totalSamples);
        }
        return trackInfo;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp3");
    }
}
