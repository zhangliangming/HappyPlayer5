package com.zlm.hp.constants;

import java.io.File;

/**
 * @Description: 资源文件常量
 * @Author: zhangliangming
 * @Date: 2017/7/16 13:40
 * @Version:
 */
public class ResourceConstants {

    /**
     * app应用名
     */
    public final static String APPNAME = "HappyPlayer";


    /**
     * 临时目录
     */
    public final static String PATH_TEMP = "haplayer";

    /**
     * 全局异常日志目录
     */
    public final static String PATH_CRASH = PATH_TEMP + File.separator
            + "crash";

    /**
     * Logcat日志目录
     */
    public final static String PATH_LOGCAT = PATH_TEMP + File.separator
            + "logcat";
    /**
     * 歌词目录
     */
    public final static String PATH_LYRICS = PATH_TEMP + File.separator + "lyrics";
    /**
     * 歌曲目录
     */
    public final static String PATH_AUDIO = PATH_TEMP + File.separator + "audio";
    /**
     * 歌曲临时保存路径
     */
    public final static String PATH_AUDIO_TEMP = PATH_AUDIO + File.separator + "temp";
    /**
     * 歌手写真目录
     */
    public final static String PATH_SINGER = PATH_TEMP + File.separator
            + "singer";

    /**
     * 缓存
     */
    public final static String PATH_CACHE = PATH_TEMP + File.separator
            + "cache";
    /**
     * 图片缓存
     */
    public final static String PATH_CACHE_IMAGE = PATH_TEMP + File.separator
            + "cache" + File.separator + "image";
    /**
     * 歌曲缓存
     */
    public final static String PATH_CACHE_AUDIO = PATH_TEMP + File.separator
            + "cache" + File.separator + "audio";

    /**
     * 序列化对象保存路径
     */
    public final static String PATH_CACHE_SERIALIZABLE = PATH_TEMP + File.separator
            + "cache" + File.separator + "serializable";
}
