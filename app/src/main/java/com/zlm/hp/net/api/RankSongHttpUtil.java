package com.zlm.hp.net.api;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.NetUtil;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.net.HttpClientUtils;
import com.zlm.hp.net.entity.SongInfoResult;
import com.zlm.hp.net.model.HttpResult;
import com.zlm.hp.utils.MediaUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangliangming on 2017/7/30.
 */
public class RankSongHttpUtil {

    /**
     * 获取排行里面的歌曲列表
     *
     * @param rankid
     * @param ranktype
     * @param page
     * @param pagesize
     * @return
     * @throws Exception
     * @author zhangliangming
     * @date 2017年7月2日
     */
    public static HttpResult rankSong(HPApplication hPApplication, Context context, String rankid, String ranktype, String page,
                                      String pagesize) {

        HttpResult httpResult = new HttpResult();

        if (!NetUtil.isNetworkAvailable(context)) {
            httpResult.setStatus(HttpResult.STATUS_NONET);
            httpResult.setErrorMsg("当前网络不可用");

            return httpResult;
        }

//
        if (hPApplication.isWifi()) {
            if (!NetUtil.isWifi(context)) {
                httpResult.setStatus(HttpResult.STATUS_NOWIFI);
                httpResult.setErrorMsg("当前网络不是wifi");

                return httpResult;
            }
        }

        try {

            Map<String, Object> returnResult = new HashMap<String, Object>();

            String url = "http://mobilecdn.kugou.com/api/v3/rank/song";
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("plat", "0");
            params.put("version", "8352");
            params.put("with_res_tag", "1");
            params.put("ranktype", ranktype);
            params.put("rankid", rankid);
            params.put("page", page);
            params.put("pagesize", pagesize);
            // 获取数据
            String result = HttpClientUtils.httpGetRequest(url, params);
            if (result != null) {
                result = result.substring(result.indexOf("{"),
                        result.lastIndexOf("}") + 1);
                JSONObject jsonNode = new JSONObject(result);
                int status = jsonNode.getInt("status");
                if (status == 1) {
                    httpResult.setStatus(HttpResult.STATUS_SUCCESS);

                    JSONObject dataJsonNode = jsonNode.getJSONObject("data");
                    returnResult.put("total", dataJsonNode.getInt("total"));
                    JSONArray infoJsonNode = dataJsonNode.getJSONArray("info");
                    List<AudioInfo> lists = new ArrayList<AudioInfo>();
                    for (int i = 0; i < infoJsonNode.length(); i++) {
                        JSONObject infoDataNode = infoJsonNode.getJSONObject(i);


                        String fileName = infoDataNode.getString("filename");

                        String regex = "\\s*-\\s*";
                        String[] temps = fileName.split(regex);
                        if (temps.length < 2) {
                            continue;
                        }
                        //去掉首尾空格
                        String singerName = fileName.split(regex)[0].trim();
                        String songName = fileName.split(regex)[1].trim();

                        AudioInfo audioInfo = new AudioInfo();
                        audioInfo.setFileSize(infoDataNode.getLong("filesize"));
                        audioInfo.setFileSizeText(MediaUtil.getFileSize(audioInfo.getFileSize()));
                        audioInfo.setHash(infoDataNode.getString("hash").toLowerCase());
                        audioInfo.setSongName(songName);
                        audioInfo.setSingerName(singerName.equals("")?"未知":singerName);
                        audioInfo.setFileExt(infoDataNode.getString("extname"));
                        audioInfo.setDuration(infoDataNode.getInt("duration") * 1000);
                        audioInfo.setDurationText(MediaUtil.parseTimeToString((int) audioInfo.getDuration()));
                        audioInfo.setType(AudioInfo.NET);
                        audioInfo.setStatus(AudioInfo.INIT);


                        SongInfoResult songInfoResult = SongInfoHttpUtil.songInfo(context, audioInfo.getHash());
                        if (songInfoResult != null) {
                            audioInfo.setDownloadUrl(songInfoResult.getUrl());
                          // audioInfo.setAlbumUrl(songInfoResult.getImgUrl());
                        }

                        lists.add(audioInfo);
                    }
                    returnResult.put("rows", lists);
                    httpResult.setResult(returnResult);

                } else {
                    httpResult.setStatus(HttpResult.STATUS_ERROR);
                    httpResult.setErrorMsg("请求出错!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            httpResult.setStatus(HttpResult.STATUS_ERROR);
            httpResult.setErrorMsg(e.getMessage());
        }
        return httpResult;

    }
}
