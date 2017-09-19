package com.zlm.hp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.model.DownloadInfo;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;

/**
 * 下载数据处理
 * Created by zhangliangming on 2017/9/8.
 */

public class DownloadInfoDB extends SQLiteOpenHelper {

    private Context mContext;

    /**
     * 表名
     */
    public static final String TBL_NAME = "downloadInfoTbl";

    /**
     * 建表语句
     */
    public static final String CREATE_TBL = "create table " + TBL_NAME + "("
            + "dhash text," + "downloadedSize long"
            + ")";
    private static DownloadInfoDB _DownloadInfoDB;

    public DownloadInfoDB(Context context) {
        super(context, "hp_downloadinfo.db", null, 2);
        this.mContext = context;
    }

    public static DownloadInfoDB getAudioInfoDB(Context context) {
        if (_DownloadInfoDB == null) {
            _DownloadInfoDB = new DownloadInfoDB(context);
        }
        return _DownloadInfoDB;
    }

    /**
     * 获取ContentValues
     *
     * @param downloadInfo
     */
    private ContentValues getContentValues(DownloadInfo downloadInfo) {

        ContentValues values = new ContentValues();
        values.put("dhash", downloadInfo.getDHash());
        values.put("downloadedSize", downloadInfo.getDownloadedSize());

        return values;
    }


    /**
     * 添加下载任务
     *
     * @param downloadInfo
     */
    public void add(DownloadInfo downloadInfo) {
        boolean addFlag = insert(getContentValues(downloadInfo));
        if (addFlag) {
            AudioInfoDB.getAudioInfoDB(mContext).addDonwloadAudio(downloadInfo.getAudioInfo());
        }
    }

    /**
     * @throws
     * @Title: insert
     * @Description: (插入数据)
     * @param: @param values
     * @param: @param skinTheme 该参数用于观察者通知使用
     * @return: void
     */
    private boolean insert(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insert(TBL_NAME, null, values);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除hash对应的数据
     */
    public void delete(String dhash) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "dhash=?", new String[]{dhash});

            //删除歌曲
            AudioInfoDB.getAudioInfoDB(mContext).deleteDonwloadAudio(dhash);
            //删除任务线程
            DownloadThreadDB.getDownloadThreadDB(mContext).delete(dhash, DownloadAudioManager.threadNum);

            //删除本地缓存文件
            String tempFilePath = ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_AUDIO_TEMP, dhash + ".temp");
            File tempFile = new File(tempFilePath);
            if (tempFile.exists()) {
                tempFile.delete();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否存在
     *
     * @param dhash
     * @return
     */
    public boolean isExists(String dhash) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TBL_NAME, new String[]{},
                " dhash=?", new String[]{dhash}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * 获取下载任务
     *
     * @param downloadInfo
     * @return
     */
    public void getDownloadInfoByHash(DownloadInfo downloadInfo, String hash) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TBL_NAME
                + " where dhash=?", new String[]{hash});
        if (!cursor.moveToNext()) {
            return;
        }
        getDownloadInfoFrom(cursor, downloadInfo);
        cursor.close();
    }

    /**
     * 获取下载
     *
     * @param cursor
     * @return
     */
    public void getDownloadInfoFrom(Cursor cursor, DownloadInfo downloadInfo) {

        downloadInfo.setDHash(cursor.getString(cursor.getColumnIndex("dhash")));
        downloadInfo.setDownloadedSize(cursor.getLong(cursor.getColumnIndex("downloadedSize")));

    }

    /**
     * 更新
     *
     * @param dhash
     * @param downloadedSize
     */
    public void update(String dhash, int downloadedSize, int status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("downloadedSize", downloadedSize);

        try {
            db.update(TBL_NAME, values,
                    "dhash=? ",
                    new String[]{dhash});

            AudioInfoDB.getAudioInfoDB(mContext).updateDonwloadInfo(dhash, status);
        } catch (SQLException e) {
            Log.i("error", "update failed");
        }
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
