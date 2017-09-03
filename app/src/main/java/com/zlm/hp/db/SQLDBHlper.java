package com.zlm.hp.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zlm.hp.constants.ResourceConstants;

/**
 * SQLDBHlper辅助类
 */
public class SQLDBHlper extends SQLiteOpenHelper {
    private static SQLDBHlper sqldbHlper;

    /**
     * 获取SQLDBHlper
     *
     * @param context
     * @return
     */
    public static SQLDBHlper getSQLDBHlper(Context context) {
        if (sqldbHlper == null) {
            sqldbHlper = new SQLDBHlper(context);
        }
        return sqldbHlper;
    }

    public SQLDBHlper(Context context) {
        super(context, ResourceConstants.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(AudioInfoDB.CREATE_TBL);
            db.execSQL(DownloadThreadDB.CREATE_TBL);
            db.execSQL(SongSingerDB.CREATE_TBL);
        } catch (SQLException e) {
            Log.i("error", "create table failed");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table if exists " + AudioInfoDB.TBL_NAME);
            db.execSQL("drop table if exists " + DownloadThreadDB.TBL_NAME);
            db.execSQL("drop table if exists " + SongSingerDB.TBL_NAME);
        } catch (SQLException e) {
            Log.i("error", "drop table failed");
        }
        onCreate(db);
    }

}
