package com.iosite.io_safesite.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseWrapper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "iosite";
    private static final int DATABASE_VERSION = 2;

    public DatabaseWrapper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CacheORM.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ProximityORM.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(ExposureORM.SQL_CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int olderVersion, int newVersion) {
        /*if (newVersion >1){
            sqLiteDatabase.execSQL(CacheORM.SQL_CREATE_TABLE);
        }*/
    }
}
