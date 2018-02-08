package com.zlm.hp.media.net.api;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.media.net.HttpClientUtils;
import com.zlm.hp.media.net.entity.DownloadLyricsResult;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import base.utils.NetUtil;
import okhttp3.ResponseBody;

/**
 * Created by zhangliangming on 2017/8/13.
 */

public class DownloadLyricsUtil {

    static final String url = "http://lyrics.kugou.com/download";
    static String downloadLyricUrl = "http://mobilecdn.kugou.com/new/app/i/krc.php";

    /**
     * 下载歌词文件
     *
     * @param id        （不为空）
     * @param accesskey （不为空）
     * @param fmt       lrc 或 krc （不为空）
     * @return
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月1日
     */
    public static DownloadLyricsResult downloadLyrics(Context context, String id, String accesskey, String fmt) {
        if (!NetUtil.isNetworkAvailable(context)) {
            return null;
        }
//
        if (HPApplication.getInstance().isWifi()) {
            if (!NetUtil.isWifi(context)) {
                return null;
            }
        }
        try {

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("ver", "1");
            params.put("client", "pc");
            params.put("id", id);
            params.put("accesskey", accesskey);
            params.put("charset", "utf8");
            params.put("fmt", fmt);
            // 获取数据
            ResponseBody response = HttpClientUtils.httpGetRequest(url, params);
            String result = response.string();
            if (result != null) {
                JSONObject jsonNode = new JSONObject(result);
                int status = jsonNode.getInt("status");
                if (status == 200) {


                    DownloadLyricsResult downloadLyricsResult = new DownloadLyricsResult();
                    downloadLyricsResult.setCharset("utf8");
                    downloadLyricsResult.setContent(jsonNode.getString("content"));
                    downloadLyricsResult.setFmt(jsonNode.getString("fmt"));

                    return downloadLyricsResult;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void cancelDownloadLyricsFile() {
        HttpClientUtils.cancelTag(url);
    }

    /**
     * 下载歌词
     *
     * @param context
     * @param keyword  singerName + " - " + songName
     * @param duration
     * @param hash
     * @return
     */
    public static byte[] downloadLyric(Context context, String keyword, String duration, String hash) {
        if (!NetUtil.isNetworkAvailable(context)) {
            return null;
        }
//
        if (HPApplication.getInstance().isWifi()) {
            if (!NetUtil.isWifi(context)) {
                return null;
            }
        }
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("keyword", keyword);
            params.put("timelength", duration);
            params.put("type", 1);
            params.put("client", "pc");
            params.put("hash", hash);
            params.put("cmd", 200);
            // 获取数据
            ResponseBody response = HttpClientUtils.httpGetRequest(downloadLyricUrl, params);
            InputStream is = response.byteStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int count = -1;
            while ((count = is.read(data, 0, 4096)) != -1)
                outStream.write(data, 0, count);
            is.close();
            return outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void cancelDownloadLyrics() {
        HttpClientUtils.cancelTag(downloadLyricUrl);
    }
}
