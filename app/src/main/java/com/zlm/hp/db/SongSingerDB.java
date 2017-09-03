package com.zlm.hp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.zlm.hp.libs.utils.DateUtil;
import com.zlm.hp.model.SongSingerInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 歌曲歌手数据库
 * Created by zhangliangming on 2017/9/2.
 */

public class SongSingerDB {

    /**
     * 表名
     */
    public static final String TBL_NAME = "songSingerTbl";

    /**
     * 建表语句
     */
    public static final String CREATE_TBL = "create table " + TBL_NAME + "("
            + "hash text,imgUrl text,createTime text,singerName text" + ")";

    private SQLDBHlper mDBHlper;

    private static SongSingerDB _SongSingerDB;

    public SongSingerDB(Context context) {
        mDBHlper = SQLDBHlper.getSQLDBHlper(context);
    }

    public static SongSingerDB getSongSingerDB(Context context) {
        if (_SongSingerDB == null) {
            _SongSingerDB = new SongSingerDB(context);
        }
        return _SongSingerDB;
    }

    /**
     * 获取ContentValues
     *
     * @param songSingerInfo
     * @return
     */
    private ContentValues getContentValues(SongSingerInfo songSingerInfo) {

        ContentValues values = new ContentValues();
        values.put("singerName", songSingerInfo.getSingerName());
        values.put("hash", songSingerInfo.getHash());
        values.put("imgUrl", songSingerInfo.getImgUrl());
        values.put("createTime", DateUtil.parseDateToString(new Date()));


        return values;
    }

    /**
     * 添加
     *
     * @param songSingerInfo
     * @return
     */
    public boolean add(SongSingerInfo songSingerInfo) {

        List<ContentValues> values = new ArrayList<ContentValues>();
        ContentValues value = getContentValues(songSingerInfo);
        values.add(value);

        return insert(values);
    }

    /**
     * 插入数据
     *
     * @param values
     * @return
     */
    private boolean insert(List<ContentValues> values) {
        SQLiteDatabase db = mDBHlper.getWritableDatabase();
        try {
            db.beginTransaction(); // 手动设置开始事务

            for (ContentValues value : values) {

                db.insert(TBL_NAME, null, value);
            }
            db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // 处理完成
        }
        return false;
    }

    /**
     * 删除hash对应的数据
     */
    public void delete(String hash, String singerName) {
        SQLiteDatabase db = mDBHlper.getWritableDatabase();
        try {
            db.delete(TBL_NAME, "hash=? and singerName=? ", new String[]{hash, singerName});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除数据
     *
     * @param hash
     * @param imgUrl
     */
    public void deleteFromImgUrl(String hash, String imgUrl) {
        SQLiteDatabase db = mDBHlper.getWritableDatabase();
        try {
            db.delete(TBL_NAME, "hash=? and imgUrl=? ", new String[]{hash, imgUrl});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否存在
     *
     * @param hash
     * @param imgUrl
     * @return
     */
    public boolean isExists(String hash, String imgUrl) {
        SQLiteDatabase db = mDBHlper.getReadableDatabase();

        Cursor cursor = db.query(TBL_NAME, new String[]{},
                " hash=? and imgUrl=? ", new String[]{hash, imgUrl}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * 获取所有的歌手写真
     *
     * @param hash
     * @return
     */
    public List<SongSingerInfo> getAllSingerImg(String hash) {
        List<SongSingerInfo> list = new ArrayList<SongSingerInfo>();
        List<String> singerNames = getAllSingerImgCategory(hash);
        for (int i = 0; i < singerNames.size(); i++) {
            List<SongSingerInfo> temp = new ArrayList<SongSingerInfo>();
            String singerName = singerNames.get(i);
            SQLiteDatabase db = mDBHlper.getReadableDatabase();
            String args[] = {hash, singerName};
            Cursor cursor = db.query(TBL_NAME, null,
                    "hash=? and singerName=?", args, null, null,
                    "createTime desc", null);
            while (cursor.moveToNext()) {

                SongSingerInfo songSingerInfo = new SongSingerInfo();

                String url = cursor.getString(cursor.getColumnIndex("imgUrl"));
                String createTime = cursor.getString(cursor.getColumnIndex("createTime"));

                //
                songSingerInfo.setCreateTime(createTime);
                songSingerInfo.setImgUrl(url);
                songSingerInfo.setHash(hash);
                songSingerInfo.setSingerName(singerName);

                temp.add(songSingerInfo);
            }
            cursor.close();

            int k = 0;
            for (int j = 0; j < temp.size(); j++) {
                if (list.size() == 0) {
                    list.add(temp.get(j));
                } else {
                    boolean flag = false;
                    for (; k < list.size(); k++) {
                        if (!list.get(k).getSingerName().equals(temp.get(j).getSingerName())) {
                            list.add(k, temp.get(j));
                            k += 2;
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        list.add(temp.get(j));
                    }
                }

            }

        }

        return list;
    }

    /**
     * 获取歌手写真分类
     *
     * @param hash
     * @return
     */
    public List<String> getAllSingerImgCategory(String hash) {
        // 第一个参数String：表名
        // 第二个参数String[]:要查询的列名
        // 第三个参数String：查询条件
        // 第四个参数String[]：查询条件的参数
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制
        // 第七个参数String：对查询的结果进行排序
        List<String> list = new ArrayList<String>();
        SQLiteDatabase db = mDBHlper.getReadableDatabase();
        String args[] = {hash};
        Cursor cursor = db.query(true, TBL_NAME, new String[]{"singerName"},
                "hash=?", args,
                null, null, "createTime desc", null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("singerName")));
        }
        cursor.close();

        return list;
    }

    /**
     * 获取所有的歌手写真图片
     *
     * @return
     */
    public Map<String, String> getAllImgUrl(String hash, String singerName) {
        Map<String, String> result = new HashMap<String, String>();
        SQLiteDatabase db = mDBHlper.getReadableDatabase();
        String args[] = {hash, singerName};
        Cursor cursor = db.query(TBL_NAME, null,
                "hash=? and singerName=?", args, null, null,
                "createTime desc", null);
        while (cursor.moveToNext()) {

            String url = cursor.getString(cursor.getColumnIndex("imgUrl"));
            String key = url.hashCode() + "";
            if (!result.containsKey(key)) {
                result.put(key, url);
            }
        }
        cursor.close();
        return result;
    }

}
