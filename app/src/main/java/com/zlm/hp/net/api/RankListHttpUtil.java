package com.zlm.hp.net.api;

import android.content.Context;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.NetUtil;
import com.zlm.hp.net.HttpClientUtils;
import com.zlm.hp.net.entity.RankListResult;
import com.zlm.hp.net.model.HttpResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 排行列表请求
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/30 20:23
 * @Throws:
 */
public class RankListHttpUtil {

    /**
     * 获取排行 列表
     *
     * @param context
     * @return
     */
    public static HttpResult rankList(HPApplication hPApplication, Context context) {

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

            String url = "http://mobilecdn.kugou.com/api/v3/rank/list";
            Map<String, Object> params = new HashMap<String, Object>();

            params.put("apiver", "4");
            params.put("withsong", "1");
            params.put("showtype", "2");
            params.put("parentid", "0");
            params.put("plat", "0");
            params.put("version", "8352");
            params.put("with_res_tag", "1");
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
                    List<RankListResult> lists = new ArrayList<RankListResult>();
                    for (int i = 0; i < infoJsonNode.length(); i++) {
                        JSONObject infoDataNode = infoJsonNode.getJSONObject(i);

                        RankListResult rankListResult = new RankListResult();
                        rankListResult.setBanner7Url(infoDataNode.getString("banner7url").replace("{size}", "400"));
                        rankListResult.setBannerUrl(infoDataNode.getString("bannerurl").replace("{size}", "400"));
                        rankListResult.setImgUrl(infoDataNode.getString("imgurl").replace("{size}", "400"));
                        rankListResult.setRankId(infoDataNode.getString("rankid"));
                        rankListResult.setRankName(infoDataNode.getString("rankname"));
                        rankListResult.setRankType(infoDataNode.getString("ranktype"));

                        //
                        JSONArray songInfosJsonArray = infoDataNode.getJSONArray("songinfo");
                        String[] songNames = new String[songInfosJsonArray.length()];
                        for (int j = 0; j < songInfosJsonArray.length(); j++) {
                            songNames[j] = songInfosJsonArray.getJSONObject(j).getString("songname");
                        }
                        rankListResult.setSongNames(songNames);

                        lists.add(rankListResult);

                    }
                    returnResult.put("rows", lists);
                    httpResult.setResult(returnResult);

                } else {
                    httpResult.setStatus(HttpResult.STATUS_ERROR);
                    httpResult.setErrorMsg(jsonNode.toString());
                }

                return httpResult;
            } else {
                httpResult.setStatus(HttpResult.STATUS_ERROR);
                httpResult.setErrorMsg("请求出错!");
            }
        } catch (Exception e) {
            e.printStackTrace();

            httpResult.setStatus(HttpResult.STATUS_ERROR);
            httpResult.setErrorMsg(e.getMessage());
        }
        return httpResult;

    }

}
