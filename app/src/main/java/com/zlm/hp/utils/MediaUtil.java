package com.zlm.hp.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.zlm.hp.audio.AudioFileReader;
import com.zlm.hp.audio.TrackInfo;
import com.zlm.hp.audio.utils.AudioUtil;
import com.zlm.hp.libs.utils.DateUtil;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.StorageInfo;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * 媒体处理娄
 * Created by zhangliangming on 2017/8/3.
 */
public class MediaUtil {


    /**
     * 获取音频文件Cursor，过滤小于1分钟的音频文件
     *
     * @return
     */
    private static Cursor getAudioCursor(Context context) {

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        return cursor;
    }

    /**
     * 扫描本地歌曲
     *
     * @param activity
     * @param foreachListener
     */
    public static void scanLocalMusic(Activity activity, ForeachListener foreachListener) {

        List<StorageInfo> list = StorageListUtil
                .listAvaliableStorage(activity.getApplicationContext());
        if (list == null || list.size() == 0) {

        } else {
            List<String> filterFormatList = AudioUtil.getSupportAudioExts();
            String[] filterFormat = new String[filterFormatList.size()];
            filterFormatList.toArray(filterFormat);
            for (int i = 0; i < list.size(); i++) {
                StorageInfo storageInfo = list.get(i);
                scanLocalAudioFile(storageInfo.path, filterFormat, foreachListener);
            }
        }
    }

    /**
     * 扫描本地音频文件
     *
     * @param path
     * @param foreachListener
     */
    private static void scanLocalAudioFile(String path, String[] filterFormat, ForeachListener foreachListener) {
        File[] files = new File(path).listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File temp = files[i];
                if (temp.isFile()) {

                    String fileName = temp.getName();
                    String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();

                    for (int j = 0; j < filterFormat.length; j++) {
                        if (fileExt.equals(filterFormat[j])) {
                            handlerAudio(temp, foreachListener);
                            break;
                        }
                    }

                } else if (temp.isDirectory() && temp.getPath().indexOf("/.") == -1) // 忽略点文件（隐藏文件/文件夹）
                {
                    scanLocalAudioFile(temp.getPath(), filterFormat, foreachListener);
                }
            }
        }
    }

    /**
     * 处理歌曲
     *
     * @param audioFile
     * @param foreachListener
     */
    private static void handlerAudio(File audioFile, ForeachListener foreachListener) {


        //歌曲文件hash值
        String hash = MD5Util.getFileMd5(audioFile).toLowerCase();

        if (foreachListener != null) {
            if (foreachListener.filter(hash)) {
                return;
            }
        }
        //
        String singerName = "未知";
        String fileName = getFileNameWithoutExt(audioFile);
        String songName = fileName;
        if (fileName.contains("-")) {
            String regex = "\\s*-\\s*";
            String[] temps = fileName.split(regex);
            if (temps.length >= 2) {
                //去掉首尾空格
                singerName = fileName.split(regex)[0].trim();
                songName = fileName.split(regex)[1].trim();
            }
        }

        String filePath = audioFile.getPath();
        //歌曲文件后缀名
        String fileExt = getFileExt(filePath);

        //
        AudioFileReader audioFileReader = AudioUtil
                .getAudioFileReaderByFilePath(filePath);
        if (audioFileReader == null)
            return;
        TrackInfo trackInfoData = audioFileReader.read(audioFile);
        if (trackInfoData == null) {
            return;
        }

        //过滤时间短的歌曲
        int duration = (int) trackInfoData.getDuration();
        if (audioFile.length() < 1024 * 1024 || duration < 5000) {
            return;
        }

        String durationText = parseTimeToString(duration);

        // 歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
        long fileSize = audioFile.length();
        String fileSizeText = getFileSize(fileSize);


        if (foreachListener != null) {
            //
            AudioInfo audioInfo = new AudioInfo();
            audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
            audioInfo.setDuration(duration);
            audioInfo.setDurationText(durationText);
            audioInfo.setFileExt(fileExt);
            audioInfo.setFilePath(filePath);
            audioInfo.setFileSize(fileSize);
            audioInfo.setFileSizeText(fileSizeText);
            audioInfo.setHash(hash);
            audioInfo.setSongName(songName);
            audioInfo.setSingerName(singerName);
            audioInfo.setType(AudioInfo.LOCAL);
            audioInfo.setStatus(AudioInfo.FINISH);
            //
            foreachListener.foreach(audioInfo);
        }
    }

    /**
     * 扫描本地歌曲
     *
     * @param context
     */
    public static void scanLocalMusicByContentResolver(Context context, ForeachListener foreachListener) {

        LoggerUtil logger = LoggerUtil.getZhangLogger(context);
        Cursor cursor = getAudioCursor(context);
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();

            // 歌曲文件的路径 ：MediaStore.Audio.Media.DATA
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            File audioFile = new File(filePath);

            //歌曲文件hash值
            String hash = MD5Util.getFileMd5(audioFile).toLowerCase();

            if (foreachListener != null) {
                if (foreachListener.filter(hash)) {
                    continue;
                }
            }

            // 歌曲的歌手名： MediaStore.Audio.Media.ARTIST
            String singerName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            // 歌曲的名称 ：MediaStore.Audio.Media.TITLE
            String songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            if (singerName.equals("<unknown>")) {
                String fileName = getFileNameWithoutExt(audioFile);
                if (fileName.contains("-")) {
                    String regex = "\\s*-\\s*";
                    String[] temps = fileName.split(regex);
                    if (temps.length >= 2) {
                        //去掉首尾空格
                        singerName = fileName.split(regex)[0].trim();
                        songName = fileName.split(regex)[1].trim();
                    }
                } else {
                    singerName = "未知";
                }
            }


            //歌曲文件后缀名
            String fileExt = getFileExt(filePath);

            //歌曲ID：MediaStore.Audio.Media._ID
            //int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));


            // 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
            // String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

            //歌曲的总播放时长 ：MediaStore.Audio.Media.DURATION
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

            //如果时长为0，则自行解析
            if (duration == 0) {
                //
                AudioFileReader audioFileReader = AudioUtil
                        .getAudioFileReaderByFilePath(filePath);
                if (audioFileReader == null)
                    continue;
                TrackInfo trackInfoData = audioFileReader.read(audioFile);
                if (trackInfoData == null) {
                    continue;
                }
                duration = (int) trackInfoData.getDuration();
            }


            String durationText = parseTimeToString(duration);

            // 歌曲文件的大小 ：MediaStore.Audio.Media.SIZE
            long fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            String fileSizeText = getFileSize(fileSize);

            //歌曲类型
            //String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));


            if (foreachListener != null) {
                //
                AudioInfo audioInfo = new AudioInfo();
                audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
                audioInfo.setDuration(duration);
                audioInfo.setDurationText(durationText);
                audioInfo.setFileExt(fileExt);
                audioInfo.setFilePath(filePath);
                audioInfo.setFileSize(fileSize);
                audioInfo.setFileSizeText(fileSizeText);
                audioInfo.setHash(hash);
                audioInfo.setSongName(songName);
                audioInfo.setSingerName(singerName);
                audioInfo.setType(AudioInfo.LOCAL);
                audioInfo.setStatus(AudioInfo.FINISH);
                //
                foreachListener.foreach(audioInfo);
            }


        }
    }

    /**
     * 计算文件的大小，返回相关的m字符串
     *
     * @param fileS
     * @return
     */
    public static String getFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 整数时间转换成字符串
     *
     * @param time
     * @return
     */
    public static String parseTimeToString(int time) {

        time /= 1000;
        int minute = time / 60;
        // int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    /**
     * 获取歌曲长度
     *
     * @param trackLengthAsString
     * @return
     */
    private static long getTrackLength(String trackLengthAsString) {

        if (trackLengthAsString.contains(":")) {
            String temp[] = trackLengthAsString.split(":");
            if (temp.length == 2) {
                int m = Integer.parseInt(temp[0]);// 分
                int s = Integer.parseInt(temp[1]);// 秒
                int currTime = (m * 60 + s) * 1000;
                return currTime;
            }
        }
        return 0;
    }


    /**
     * 获取文件的后缀名
     *
     * @param filePath
     * @return
     */
    public static String getFileExt(String filePath) {
        int pos = filePath.lastIndexOf(".");
        if (pos == -1)
            return "";
        return filePath.substring(pos + 1).toLowerCase();
    }

    /**
     * 获取不带后缀名的文件名
     */
    public static String getFileNameWithoutExt(File file) {
        String filename = file.getName();
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     *
     */
    public interface ForeachListener {
        /**
         * 遍历
         *
         * @param audioInfo
         */
        void foreach(AudioInfo audioInfo);

        /**
         * 过滤 true则跳过
         *
         * @param hash
         * @return
         */
        boolean filter(String hash);
    }

}
