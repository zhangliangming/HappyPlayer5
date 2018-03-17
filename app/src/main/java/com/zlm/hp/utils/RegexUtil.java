package com.zlm.hp.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by KathLine on 2018/2/1.
 */

public class RegexUtil {

    public static boolean isPlay(String url) {
        boolean isPlay = false;
        if (!TextUtils.isEmpty(url)) {
            //是否是网址
            Pattern pattern = Pattern.compile("^([hH][tT][tT][pP]([sS]?):\\/\\/)?(\\S+\\.)+\\S{2,}$");
            Matcher matcher = pattern.matcher(url);
            if (matcher.matches()) {
                //根据后缀判断
                ArrayList<String> list = new ArrayList<>();
                Matcher m = Pattern.compile("\\.[A-Za-z0-9]{2,}$").matcher(url);
                while (m.find()) {
                    String s = m.group();
                    s = s.substring(1);
                    list.add(s);
                    if (isMusic(s)) {
                        // 音乐文件......................
                        System.out.println("This file is Music File,fileName=" + url + "，后缀" + s);
                        isPlay = true;
                    }
                    if (isVideo(s)) {
                        // 视频文件......................
                        System.out.println("This file is Video File,fileName=" + url + "，后缀" + s);
                        isPlay = true;
                    }
                    if (isPhoto(s)) {
                        // 图片文件......................
                        System.out.println("This file is Photo File,fileName=" + url + "，后缀" + s);
                    }
                }
            }
        }
        return isPlay;
    }

    /**
     * 判断是否是音乐文件
     *
     * @param extension 后缀名
     * @return
     */
    public static boolean isMusic(String extension) {
        if (extension == null)
            return false;

        final String ext = extension.toLowerCase();
        if (ext.equals("mp3") || ext.equals("m4a") || ext.equals("wav") || ext.equals("amr") || ext.equals("awb") ||
                ext.equals("aac") || ext.equals("flac") || ext.equals("mid") || ext.equals("midi") ||
                ext.equals("xmf") || ext.equals("rtttl") || ext.equals("rtx") || ext.equals("ota") ||
                ext.equals("wma") || ext.equals("ra") || ext.equals("mka") || ext.equals("m3u") || ext.equals("pls")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是图像文件
     *
     * @param extension 后缀名
     * @return
     */
    public static boolean isPhoto(String extension) {
        if (extension == null)
            return false;

        final String ext = extension.toLowerCase();
        if (ext.endsWith("jpg") || ext.endsWith("jpeg") || ext.endsWith("gif") || ext.endsWith("png") ||
                ext.endsWith("bmp") || ext.endsWith("wbmp")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是视频文件
     *
     * @param extension 后缀名
     * @return
     */
    public static boolean isVideo(String extension) {
        if (extension == null)
            return false;

        final String ext = extension.toLowerCase();
        if (ext.endsWith("mpeg") || ext.endsWith("mp4") || ext.endsWith("mov") || ext.endsWith("m4v") ||
                ext.endsWith("3gp") || ext.endsWith("3gpp") || ext.endsWith("3g2") ||
                ext.endsWith("3gpp2") || ext.endsWith("avi") || ext.endsWith("divx") ||
                ext.endsWith("wmv") || ext.endsWith("asf") || ext.endsWith("flv") ||
                ext.endsWith("mkv") || ext.endsWith("mpg") || ext.endsWith("rmvb") ||
                ext.endsWith("rm") || ext.endsWith("vob") || ext.endsWith("f4v")) {
            return true;
        }
        return false;
    }
}
