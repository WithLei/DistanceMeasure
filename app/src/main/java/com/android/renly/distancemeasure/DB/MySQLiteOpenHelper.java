package com.android.renly.distancemeasure.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    //单例模式
    private static final String DB_NAME = "distanceMeasure.db";
    public static final String TABLE_NAME = "experiment";
    private static final int VERSION = 1;
    private static MySQLiteOpenHelper instance;
    public static final String carId = "carId";
    public static final String measureTime = "measureTime";
    public static final String theTime = "time";
    public static final String measureResult = "result";
    public static final String theID = "_id";
    public static final String carDirection = "carDirection";
    public static final String startDistance = "startDistance";
    public static final String nowDistance = "nowDistance";

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MySQLiteOpenHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    public synchronized static MySQLiteOpenHelper getInstance(Context context){
        if(instance == null){
            instance = new MySQLiteOpenHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //数据库创建
        db.execSQL("create table if not exists " + TABLE_NAME + " (_id integer primary key autoincrement, " +
                "carId char(20), measureTime char(20), time char(20), result char(20), carDirection char(20), startDistance int(20), nowDistance int(20))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //数据库升级
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }
}
