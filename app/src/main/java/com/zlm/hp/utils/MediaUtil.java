package com.zlm.hp.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.zlm.hp.R;
import com.zlm.hp.model.AudioInfo;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import base.utils.DateUtil;
import base.utils.PreferencesUtil;

/**
 * 媒体处理娄
 * Created by zhangliangming on 2017/8/3.
 */
public class MediaUtil {

    private static final String SELECTION = MediaStore.Audio.AudioColumns.SIZE + " >= ? AND " + MediaStore.Audio.AudioColumns.DURATION + " >= ?";

    public interface ForeachListener {
        /**
         * 遍历前
         */
        void before();
        /**
         * 遍历
         *
         * @param audioInfoList
         */
        void foreach(List<AudioInfo> audioInfoList);

        /**
         * 过滤 true则跳过
         *
         * @param hash
         * @return
         */
        boolean filter(String hash);
    }

    /**
     * 扫描歌曲
     */
    @NonNull
    public static void scanMusic(Context context, ForeachListener foreachListener) {
        List<AudioInfo> musicList = new ArrayList<>();

        long filterSize = Long.parseLong(getFilterSize(context)) * 1024;
        long filterTime = Long.parseLong(getFilterTime(context)) * 1000;

        String fileSizeStr = getFileSize(filterSize);
        String durationStr = parseTimeToString(filterTime);
        System.out.printf("过滤文件大小：" + fileSizeStr + ", 过滤文件时长：" + durationStr);

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        BaseColumns._ID,
                        MediaStore.Audio.AudioColumns.IS_MUSIC,
                        MediaStore.Audio.AudioColumns.TITLE,
                        MediaStore.Audio.AudioColumns.ARTIST,
                        MediaStore.Audio.AudioColumns.ALBUM,
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        MediaStore.Audio.AudioColumns.DATA,
                        MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                        MediaStore.Audio.AudioColumns.SIZE,
                        MediaStore.Audio.AudioColumns.DURATION
                },
                SELECTION,
                new String[]{
                        String.valueOf(filterSize),
                        String.valueOf(filterTime)
                },
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }

        if(foreachListener != null) {
            foreachListener.before();
        }

        while (cursor.moveToNext()) {
            // 是否为音乐
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.IS_MUSIC));
            if (isMusic == 0) {
                continue;
            }

            long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
            String fileName = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)));
            long fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));

            File audioFile = new File(filePath);
            //歌曲文件hash值
            String hash = MD5Util.getFileMd5(audioFile).toLowerCase();

            if (foreachListener != null) {
                if (foreachListener.filter(hash)) {
                    continue;
                }
            }

            if (artist.equals("<unknown>")) {
                if (title.contains("-")) {
                    String regex = "\\s*-\\s*";
                    String[] temps = title.split(regex);
                    if (temps.length >= 2) {
                        //去掉首尾空格
                        artist = title.split(regex)[0].trim();
                        title = title.split(regex)[1].trim();
                    }
                } else {
                    artist = context.getString(R.string.unknown);
                }
            }

            String fileSizeText = getFileSize(fileSize);
            String durationText = parseTimeToString(duration);
            //歌曲文件后缀名
            String fileExt = getFileExt(filePath);

            AudioInfo audioInfo = new AudioInfo();
            audioInfo.setHash(hash);
            audioInfo.setCreateTime(DateUtil.parseDateToString(new Date()));
            audioInfo.setDuration(duration);
            audioInfo.setDurationText(durationText);
            audioInfo.setType(AudioInfo.LOCAL);
            audioInfo.setStatus(AudioInfo.FINISH);
            audioInfo.setSongName(title);
            audioInfo.setSingerName(artist);
            audioInfo.setFileExt(fileExt);
            audioInfo.setFilePath(filePath);
            audioInfo.setFileSize(fileSize);
            audioInfo.setFileSizeText(fileSizeText);

            musicList.add(audioInfo);
        }
        cursor.close();

        if(foreachListener != null) {
            foreachListener.foreach(musicList);
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
    public static String parseTimeToString(long time) {

        time /= 1000;
        long minute = time / 60;
        // int hour = minute / 60;
        long second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
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

    public static Uri getMediaStoreAlbumCoverUri(long albumId) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, albumId);
    }

    public static boolean isAudioControlPanelAvailable(Context context) {
        return isIntentAvailable(context, new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL));
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, PackageManager.GET_RESOLVED_FILTER) != null;
    }

    public static String getFilterSize(Context context) {
        return PreferencesUtil.getStringValue(context, context.getString(R.string.setting_key_filter_size), "0");
    }

    public static void setFilterSize(Context context, String value) {
        PreferencesUtil.putValue(context, context.getString(R.string.setting_key_filter_size), value);
    }

    public static String getFilterTime(Context context) {
        return PreferencesUtil.getStringValue(context, context.getString(R.string.setting_key_filter_time), "0");
    }

    public static void setFilterTime(Context context, String value) {
        PreferencesUtil.putValue(context, context.getString(R.string.setting_key_filter_time), value);
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

}
