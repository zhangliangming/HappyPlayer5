package com.zlm.hp.net.api;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.NetUtil;
import com.zlm.hp.net.HttpClientUtils;
import com.zlm.hp.net.entity.SearchLyricsResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索歌词
 * Created by zhangliangming on 2017/8/13.
 */

public class SearchLyricsUtil {

    /**
     * 搜索歌词
     *
     * @param hPApplication
     * @param context
     * @param keyword       歌曲名（不为空）：singerName + " - " + songName
     * @param duration      歌曲总时长(毫秒)（不为空）
     * @param hash          歌曲Hash值
     * @return
     * @throws Exception
     */
    public static List<SearchLyricsResult> searchLyrics(HPApplication hPApplication, Context context, String keyword, String duration, String hash) {
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
            String url = "http://lyrics.kugou.com/search";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("ver", "1");
            params.put("man", "yes");
            params.put("client", "pc");
            params.put("keyword", keyword);
            params.put("duration", duration);
            if (hash != null && !hash.equals("")) {
                params.put("hash", hash);
            }

            // 获取数据
            String result = HttpClientUtils.httpGetRequest(url, params);
            if (result != null) {
                JSONObject jsonNode = new JSONObject(result);
                int status = jsonNode.getInt("status");
                if (status == 200) {
                    List<SearchLyricsResult> lists = new ArrayList<SearchLyricsResult>();
                    JSONArray candidatesNode = jsonNode.getJSONArray("candidates");
                    for (int i = 0; i < candidatesNode.length(); i++) {
                        JSONObject candidateNode = candidatesNode.getJSONObject(i);

                        SearchLyricsResult searchLyricsResult = new SearchLyricsResult();
                        searchLyricsResult.setId(candidateNode.getString("id"));
                        searchLyricsResult.setAccesskey(candidateNode.getString(
                                "accesskey"));
                        searchLyricsResult.setDuration(candidateNode
                                .getString("duration"));
                        searchLyricsResult.setSingerName(candidateNode
                                .getString("singer"));
                        searchLyricsResult.setSongName(candidateNode.getString("song"));

                        lists.add(searchLyricsResult);
                    }

                    return lists;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
