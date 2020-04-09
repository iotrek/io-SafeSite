package com.iosite.io_safesite.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.iosite.io_safesite.MyApplication;

public class DataBaseManager {

    private static DataBaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private int mOpenCounter;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance() {
        if (instance == null) {
            instance = new DataBaseManager();
            mDatabaseHelper = new DatabaseWrapper(MyApplication.getInstance());
        }
    }

    public static synchronized DataBaseManager getInstance() {
        if (instance == null) {
            initializeInstance();
        }

        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {

        mOpenCounter++;
        if (mOpenCounter == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }

        if (mDatabase == null) {
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }

        return mDatabase;
    }

    public synchronized void closeDatabase() {


        if (mDatabase != null && mDatabase.isOpen()) {
            mOpenCounter--;
            if (mOpenCounter == 0) {
                // Closing database
                mDatabase.close();

            }
        }
    }

}
