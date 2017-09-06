package com.zlm.hp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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

public class SongSingerDB extends SQLiteOpenHelper {

    /**
     * 表名
     */
    public static final String TBL_NAME = "songSingerTbl";

    /**
     * 建表语句
     */
    public static final String CREATE_TBL = "create table " + TBL_NAME + "("
            + "hash text,imgUrl text,createTime text,singerName text" + ")";

    private static SongSingerDB _SongSingerDB;

    public SongSingerDB(Context context) {
        super(context, "hp_songsinger.db", null, 2);
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
        SQLiteDatabase db = getWritableDatabase();
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
     * 删除数据
     *
     * @param singerName
     * @param imgUrl
     */
    public void deleteFromSI(String singerName, String imgUrl) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "singerName=? and imgUrl=? ", new String[]{singerName, imgUrl});
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
        SQLiteDatabase db = getReadableDatabase();

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
     * 获取歌手写真图片
     *
     * @param singerNameArray
     * @return
     */
    public List<SongSingerInfo> getAllSingerImg(String[] singerNameArray, boolean loadOtherSinger) {
        List<SongSingerInfo> list = new ArrayList<SongSingerInfo>();
        for (int i = 0; i < singerNameArray.length; i++) {
            List<SongSingerInfo> temp = new ArrayList<SongSingerInfo>();
            String singerName = singerNameArray[i];
            SQLiteDatabase db = getReadableDatabase();
            String args[] = {singerName};
            Cursor cursor = db.query(TBL_NAME, null,
                    "singerName=?", args, null, null,
                    "createTime desc", null);

            while (cursor.moveToNext()) {

                SongSingerInfo songSingerInfo = new SongSingerInfo();

                String url = cursor.getString(cursor.getColumnIndex("imgUrl"));
                String createTime = cursor.getString(cursor.getColumnIndex("createTime"));

                //
                songSingerInfo.setCreateTime(createTime);
                songSingerInfo.setImgUrl(url);
                songSingerInfo.setSingerName(singerName);

                temp.add(songSingerInfo);
            }
            cursor.close();

            //存在其它歌手头像为空的情况，需要加载其他的歌手写真
            if (temp.size() == 0 && loadOtherSinger) {
                return new ArrayList<SongSingerInfo>();
            }

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
     * 获取所有的歌手写真图片
     *
     * @return
     */
    public Map<String, String> getAllImgUrlBySingerName(String singerName) {
        Map<String, String> result = new HashMap<String, String>();
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {singerName};
        Cursor cursor = db.query(TBL_NAME, null,
                "singerName=?", args, null, null,
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


    /**
     * 获取歌手写真图片个数
     *
     * @return
     */
    public int getAllImgUrlCount(String singerName) {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {singerName};
        Cursor cursor = db.rawQuery("select count(*)from " + TBL_NAME
                + " WHERE singerName=? ", args);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TBL);
        } catch (SQLException e) {
            Log.i("error", "create table failed");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        try {
            db.execSQL("drop table if exists " + TBL_NAME);
        } catch (SQLException e) {
            Log.i("error", "drop table failed");
        }
        onCreate(db);
    }


}
