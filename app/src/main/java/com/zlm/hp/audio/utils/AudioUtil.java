package com.zlm.hp.audio.utils;

import com.zlm.hp.audio.AudioFileReader;
import com.zlm.hp.audio.formats.ape.APEFileReader;
import com.zlm.hp.audio.formats.flac.FLACFileReader;
import com.zlm.hp.audio.formats.mp3.MP3FileReader;
import com.zlm.hp.audio.formats.wav.WAVFileReader;

import java.util.ArrayList;

public class AudioUtil {
    private static ArrayList<AudioFileReader> readers = new ArrayList();


    static {
        readers.add(new MP3FileReader());
        readers.add(new APEFileReader());
        readers.add(new FLACFileReader());
        readers.add(new WAVFileReader());

    }

    public static AudioFileReader getAudioFileReaderByFilePath(String filePath) {
        String ext = getFileExt(filePath);
        for (AudioFileReader reader : readers) {
            if (reader.isFileSupported(ext)) {
                return reader;
            }
        }
        return null;
    }

    public static AudioFileReader getAudioFileReaderByFileExt(String fileExt) {
        fileExt = fileExt.toLowerCase();
        for (AudioFileReader reader : readers) {
            if (reader.isFileSupported(fileExt)) {
                return reader;
            }
        }
        return null;
    }


    private static String getFileExt(String filePath) {
        int pos = filePath.lastIndexOf(".");
        if (pos == -1) {
            return "";
        }
        return filePath.substring(pos + 1).toLowerCase();
    }
}
