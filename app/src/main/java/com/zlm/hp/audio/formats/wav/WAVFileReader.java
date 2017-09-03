package com.zlm.hp.audio.formats.wav;


import com.zlm.hp.audio.AudioFileReader;
import com.zlm.hp.audio.TrackInfo;

public class WAVFileReader
        extends AudioFileReader {
    protected TrackInfo readSingle(TrackInfo trackInfo) {
        try {
            BaseWAVFileReader fileReader = new BaseWAVFileReader();
            fileReader.openFile(trackInfo.getFilePath());
            WavFileHeader audioHeader = fileReader.getmWavFileHeader();

            trackInfo.setChannels(audioHeader.mNumChannel);
            int frameSize = trackInfo.getChannels() *
                    2;
            trackInfo.setFrameSize(frameSize);
            trackInfo.setSampleRate(audioHeader.mSampleRate);

            long totalSamples = Math.round(audioHeader.mSubChunk2Size * 8 *
                    1.0D / (
                    audioHeader.mNumChannel * audioHeader.mBitsPerSample));
            trackInfo.setTotalSamples(totalSamples);

            trackInfo.setPlayedProgress(0L);
            trackInfo.setCodec(getFileExt(trackInfo.getFilePath()));
            trackInfo.setBitrate(audioHeader.mBiteRate);

            return trackInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("wav");
    }

    private static String getFileExt(String filePath) {
        int pos = filePath.lastIndexOf(".");
        if (pos == -1) {
            return "";
        }
        return filePath.substring(pos + 1).toLowerCase();
    }
}
