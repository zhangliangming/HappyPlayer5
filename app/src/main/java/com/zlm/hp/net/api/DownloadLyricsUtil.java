package com.zlm.hp.net.api;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.NetUtil;
import com.zlm.hp.net.HttpClientUtils;
import com.zlm.hp.net.entity.DownloadLyricsResult;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhangliangming on 2017/8/13.
 */

public class DownloadLyricsUtil {

    /**
     * 设置连接超时时间
     */
    public final static int CONNECT_TIMEOUT = 60;
    /**
     * 设置读取超时时间
     */
    public final static int READ_TIMEOUT = 100;
    /**
     * 设置写的超时时间
     */
    public final static int WRITE_TIMEOUT = 60;

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
    public static DownloadLyricsResult downloadLyrics(HPApplication hPApplication, Context context, String id, String accesskey, String fmt) {
        if (!NetUtil.isNetworkAvailable(context)) {
            return null;
        }
//
        if (hPApplication.isWifi()) {
            if (!NetUtil.isWifi(context)) {
                return null;
            }
        }
        try {
            String url = "http://lyrics.kugou.com/download";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("ver", "1");
            params.put("client", "pc");
            params.put("id", id);
            params.put("accesskey", accesskey);
            params.put("charset", "utf8");
            params.put("fmt", fmt);
            // 获取数据
            String result = HttpClientUtils.httpGetRequest(url, params);
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

    /**
     * 下载歌词
     *
     * @param hPApplication
     * @param context
     * @param keyword  singerName + " - " + songName
     * @param duration
     * @param hash
     * @return
     */
    public static byte[] downloadLyric(HPApplication hPApplication, Context context, String keyword, String duration, String hash) {
        if (!NetUtil.isNetworkAvailable(context)) {
            return null;
        }
//
        if (hPApplication.isWifi()) {
            if (!NetUtil.isWifi(context)) {
                return null;
            }
        }
        try {

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)// 设置读取超时时间
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)// 设置写的超时时间
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)// 设置连接超时时间
                    .build();

            String url = "http://mobilecdn.kugou.com/new/app/i/krc.php?keyword=" + keyword + "&timelength=" + duration + "&type=1&client=pc&cmd=200&hash=" + hash;
            Request.Builder builder = new Request.Builder();
            Request request = builder.get().url(url).build();
            Call call = client.newCall(request);
            // 执行请求
            Response response = call.execute();
            if (response.isSuccessful()) {
                //得到输入流
                InputStream is = response.body().byteStream();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] data = new byte[4096];
                int count = -1;
                while ((count = is.read(data, 0, 4096)) != -1)
                    outStream.write(data, 0, count);
                is.close();
                return outStream.toByteArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
