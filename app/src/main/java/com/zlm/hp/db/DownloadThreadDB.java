package com.zlm.hp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zlm.hp.model.DownloadThreadInfo;

/**
 * 下载任务线程表
 *
 * @author zhangliangming
 */
public class DownloadThreadDB extends SQLiteOpenHelper {
    /**
     * 表名
     */
    public static final String TBL_NAME = "downloadThreadTbl";

    /**
     * 建表语句
     */
    public static final String CREATE_TBL = "create table " + TBL_NAME + "("
            + "tid text," + "threadNum int," + "threadID int,"
            + "downloadedSize int" + ")";

    private static DownloadThreadDB _downloadThreadDB;


    public DownloadThreadDB(Context context) {
        super(context, "hp_download.db", null, 2);
    }

    public static DownloadThreadDB getDownloadThreadDB(Context context) {
        if (_downloadThreadDB == null) {
            _downloadThreadDB = new DownloadThreadDB(context);
        }
        return _downloadThreadDB;
    }

    /**
     * 添加线程任务
     *
     * @param downloadThreadInfo
     */
    public void add(DownloadThreadInfo downloadThreadInfo) {
        ContentValues values = new ContentValues();

        values.put("tid", downloadThreadInfo.getTaskId());
        values.put("threadNum", downloadThreadInfo.getThreadNum());
        values.put("threadID", downloadThreadInfo.getThreadId());
        values.put("downloadedSize", downloadThreadInfo.getDownloadedSize());

        insert(values);
    }

    /**
     * @throws
     * @Title: insert
     * @Description: (插入数据)
     * @param: @param values
     * @param: @param skinTheme 该参数用于观察者通知使用
     * @return: void
     */
    private void insert(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insert(TBL_NAME, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取下载任务线程
     *
     * @param tid
     * @param threadNum
     * @param threadID
     * @return
     */
    public DownloadThreadInfo getDownloadThreadInfo(String tid, int threadNum, int threadID) {
        SQLiteDatabase db = getReadableDatabase();
        // 第一个参数String：表名
        // 第二个参数String[]:要查询的列名
        // 第三个参数String：查询条件
        // 第四个参数String[]：查询条件的参数
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制
        // 第七个参数String：对查询的结果进行排序
        Cursor cursor = db.rawQuery("select * from " + TBL_NAME
                        + " where tid=? and threadNum=? and threadID=? ",
                new String[]{tid + "", threadNum + "", threadID + ""});
        if (!cursor.moveToNext()) {
            return null;
        }
        DownloadThreadInfo downloadThreadInfo = getDownloadThreadInfo(cursor);
        cursor.close();
        return downloadThreadInfo;
    }

    /**
     * @param cursor
     * @return
     */
    private DownloadThreadInfo getDownloadThreadInfo(Cursor cursor) {
        DownloadThreadInfo downloadThreadInfo = new DownloadThreadInfo();

        downloadThreadInfo
                .setTaskId(cursor.getString(cursor.getColumnIndex("tid")));
        downloadThreadInfo.setThreadNum(cursor.getInt(cursor
                .getColumnIndex("threadNum")));
        downloadThreadInfo.setThreadId(cursor.getInt(cursor
                .getColumnIndex("threadID")));

        downloadThreadInfo
                .setDownloadedSize(cursor.getInt(cursor.getColumnIndex("downloadedSize")));

        return downloadThreadInfo;
    }

    /**
     * 更新线程任务
     *
     * @param tid
     * @param threadNum
     * @param threadID
     * @param downloadedSize
     */
    public void update(String tid, int threadNum, int threadID,
                       int downloadedSize) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("downloadedSize", downloadedSize);

        try {
            db.update(TBL_NAME, values,
                    "tid=? and threadNum=? and threadID=? ",
                    new String[]{tid, threadNum + "", threadID + ""});
        } catch (SQLException e) {
            Log.i("error", "update failed");
        }
    }

    /**
     * 线程任务是否存在
     *
     * @param tid
     * @param threadNum
     * @param threadID
     * @return
     */
    public boolean isExists(String tid, int threadNum, int threadID) {
        SQLiteDatabase db = getReadableDatabase();
        // 第一个参数String：表名
        // 第二个参数String[]:要查询的列名
        // 第三个参数String：查询条件
        // 第四个参数String[]：查询条件的参数
        // 第五个参数String:对查询的结果进行分组
        // 第六个参数String：对分组的结果进行限制
        // 第七个参数String：对查询的结果进行排序
        Cursor cursor = db.query(TBL_NAME, null,
                " tid=? and threadNum=? and threadID=? ", new String[]{tid, threadNum + "", threadID + ""}, null, null, null);
        if (!cursor.moveToNext()) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * 删除hash对应的数据
     */
    public void delete(String tid, int threadNum) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(TBL_NAME, "tid=? and threadNum=?", new String[]{tid, threadNum + ""});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取下载进度
     *
     * @param tid
     * @return
     */
    public int getDownloadedSize(String tid, int threadNum) {
        SQLiteDatabase db = getReadableDatabase();
        String args[] = {tid, threadNum + ""};
        Cursor cursor = db.rawQuery("SELECT sum(downloadedSize) as downloadedSize from " + TBL_NAME
                + " WHERE tid=? and threadNum=?", args);
        if (cursor.moveToNext()) {
            return cursor.getInt(cursor.getColumnIndex("downloadedSize"));
        }
        return 0;
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
