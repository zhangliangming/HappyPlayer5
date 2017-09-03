package com.zlm.hp.net.api;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.NetUtil;
import com.zlm.hp.net.HttpClientUtils;
import com.zlm.hp.net.entity.SearchArtistPicResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索歌手写真
 * Created by zhangliangming on 2017/8/29.
 */

public class SearchArtistPicUtil {
    /**
     * 搜索歌手写真图片
     *
     * @param hPApplication
     * @param context
     * @param singerName
     * @param width
     * @param height
     * @param type          pc/app
     * @return
     * @throws Exception
     */
    public static List<SearchArtistPicResult> searchArtistPic(HPApplication hPApplication, Context context, String singerName, String width,
                                                              String height, String type) {

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
            Map<String, Object> returnResult = new HashMap<String, Object>();

            String url = "http://artistpicserver.kuwo.cn/pic.web";
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("type", "big_artist_pic");
            params.put("pictype", "url");
            params.put("content", "list");
            params.put("id", "0");
            params.put("name", singerName);
            params.put("from", type);
            params.put("json", "1");
            params.put("version", "1");
            params.put("width", width);
            params.put("height", height);

            // 获取数据
            String result = HttpClientUtils.httpGetRequest(url, params);
            if (result != null) {

                JSONObject jsonNode = new JSONObject(result);
                JSONArray arrayJsonNode = jsonNode.getJSONArray("array");
                if (arrayJsonNode != null) {


                    List<SearchArtistPicResult> lists = new ArrayList<SearchArtistPicResult>();
                    for (int i = 0; i < arrayJsonNode.length(); i++) {

                        JSONObject arrayInfo = arrayJsonNode.getJSONObject(i);

                        SearchArtistPicResult searchArtistPicResult = new SearchArtistPicResult();

                        String imgUrl = null;
                        if (type.equals("app")) {
                            imgUrl = arrayInfo.getString("key");
                        } else {

                            if (arrayInfo.has("bkurl")) {
                                imgUrl = arrayInfo.getString("bkurl");
                            } else {
                                continue;
                            }
                        }
                        searchArtistPicResult.setImgUrl(imgUrl);
                        searchArtistPicResult.setSinger(singerName);

                        lists.add(searchArtistPicResult);

                    }

                    //排序
                    Collections.sort(lists, new Comparator<SearchArtistPicResult>() {

                        @Override
                        public int compare(SearchArtistPicResult o1, SearchArtistPicResult o2) {
                            return o1.getImgUrl().compareTo(o2.getImgUrl());
                        }
                    });

                    return lists;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
