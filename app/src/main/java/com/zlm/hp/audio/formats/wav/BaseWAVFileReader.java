package com.zlm.hp.audio.formats.wav;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BaseWAVFileReader {
    private DataInputStream mDataInputStream;
    private WavFileHeader mWavFileHeader;

    public boolean openFile(String filePath)
            throws IOException {
        if (this.mDataInputStream != null) {
            closeFile();
        }
        this.mDataInputStream = new DataInputStream(new FileInputStream(filePath));
        return readHeader();
    }

    public void closeFile()
            throws IOException {
        if (this.mDataInputStream != null) {
            this.mDataInputStream.close();
            this.mDataInputStream = null;
        }
    }

    public WavFileHeader getmWavFileHeader() {
        return this.mWavFileHeader;
    }

    public int readData(byte[] buffer, int offset, int count) {
        if ((this.mDataInputStream == null) || (this.mWavFileHeader == null)) {
            return -1;
        }
        try {
            int nbytes = this.mDataInputStream.read(buffer, offset, count);
            if (nbytes == -1) {
                return 0;
            }
            return nbytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean readHeader() {
        if (this.mDataInputStream == null) {
            return false;
        }
        WavFileHeader header = new WavFileHeader();

        byte[] intValue = new byte[4];
        byte[] shortValue = new byte[2];
        try {
            header.mChunkID = "" +

                    ((char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte());
            System.out.println("Read file chunkID:" + header.mChunkID);

            this.mDataInputStream.read(intValue);
            header.mChunkSize = byteArrayToInt(intValue);

            header.mFormat = "" +

                    ((char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte());

            header.mSubChunk1ID = "" +

                    ((char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte() + (char) this.mDataInputStream.readByte());

            this.mDataInputStream.read(intValue);
            header.mSubChunk1Size = byteArrayToInt(intValue);

            this.mDataInputStream.read(shortValue);
            header.mAudioFormat = byteArrayToShort(shortValue);

            this.mDataInputStream.read(shortValue);
            header.mNumChannel = byteArrayToShort(shortValue);

            this.mDataInputStream.read(intValue);
            header.mSampleRate = byteArrayToInt(intValue);

            this.mDataInputStream.read(intValue);
            header.mBiteRate = (byteArrayToInt(intValue) / 100);

            this.mDataInputStream.read(shortValue);
            header.mBlockAlign = byteArrayToShort(shortValue);

            this.mDataInputStream.read(shortValue);
            header.mBitsPerSample = byteArrayToShort(shortValue);

            //获取data标签
            String dataTag = "data";
            byte[] findDataByte = new byte[200];
            this.mDataInputStream.read(findDataByte);
            String findDataString = new String(findDataByte);

            if (findDataString != null && findDataString.lastIndexOf(dataTag) > 0 && findDataByte != null) {
                String flagString = "";
                int dataTagIndex = -1;
                for (int i = findDataByte.length - 1; i >= 0; i--) {
                    char c = (char) findDataByte[i];
                    if (dataTag.contains(c + "")) {
                        flagString = c + flagString;
                        if (c == 't') {
                            if (!flagString.equals("ta")) {
                                flagString = "";
                            }
                        } else if (c == 'd') {
                            if (!flagString.equals(dataTag)) {
                                flagString = "";
                            } else {
                                dataTagIndex = i;
                                break;
                            }
                        }
                    }
                }
                //
                if (dataTagIndex >= 0) {
                    int index = dataTagIndex + dataTag.length();
                    byte[] data = new byte[4];
                    for (int i = index, j = 0; j < data.length; j++, i++) {
                        data[j] = findDataByte[i];
                    }

                    header.mSubChunk2Size = byteArrayToInt(data);
                    this.mWavFileHeader = header;

                    return true;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
