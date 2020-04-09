package com.iosite.io_safesite.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CacheORM {
    private static final String TAG = "CacheORM";

    private static final String TABLE_NAME = "cache";

    private static final String COMMA_SEP = ", ";
    private static final String COLUMN_BODY_TYPE = "TEXT";
    private static final String COLUMN_BODY = "body";

    private static final String COLUMN_URL_TYPE = "TEXT";
    private static final String COLUMN_URL = "url";

    private static final String COLUMN_DATE_TYPE = "TEXT";
    private static final String COLUMN_DATE = "pubdate";


    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_BODY + " " + COLUMN_BODY_TYPE + COMMA_SEP +
                    COLUMN_URL + " " + COLUMN_URL_TYPE + COMMA_SEP +
                    COLUMN_DATE + " " + COLUMN_DATE_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.ENGLISH);


    /**
     * Fetches a single Post identified by the specified ID
     *
     * @param context
     * @param url
     * @return
     */
    public static String getCache(Context context, String url) {
        try {
            DataBaseManager dbManager = DataBaseManager.getInstance();
            SQLiteDatabase database = dbManager.openDatabase();
            String json = null;
            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT * FROM " + CacheORM.TABLE_NAME + " WHERE " + CacheORM.COLUMN_URL + " = '" + url + "'", null);

                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    json = cursor.getString(cursor.getColumnIndex(COLUMN_BODY));

                }

//                dbManager.closeDatabase();
            }

            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DataBaseManager dbManager = DataBaseManager.getInstance();
            dbManager.closeDatabase();
        }
    }

    /**
     * Inserts a Post object into the local database
     *
     * @param context
     * @param url
     * @return
     */
    public static boolean insertCache(Context context, String url, String json) {
        if (getCache(context, url) != null) {
            return updatePost(context, url, json);
        }

        ContentValues values = postToContentValues(url, json);

        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();

        boolean success = false;
        try {
            if (database != null) {
                long postId = database.insert(CacheORM.TABLE_NAME, "null", values);

                success = true;
            }
        } catch (NullPointerException ex) {
        } finally {
            if (database != null) {
                dbManager.closeDatabase();
            }
        }

        return success;
    }

    public static boolean updatePost(Context context, String url, String json) {
        ContentValues values = postToContentValues(url, json);
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();

        boolean success = false;
        try {
            if (database != null) {
                database.update(CacheORM.TABLE_NAME, values, CacheORM.COLUMN_URL + " = '" + url + "'", null);
                success = true;
            }
        } catch (NullPointerException ex) {
        } finally {
            if (database != null) {
                dbManager.closeDatabase();
            }
        }

        return success;
    }

    /**
     * Packs a Post object into a ContentValues map for use with SQL inserts.
     *
     * @param url
     * @param json
     * @return
     */
    private static ContentValues postToContentValues(String url, String json) {
        ContentValues values = new ContentValues();
        values.put(CacheORM.COLUMN_BODY, json);
        values.put(CacheORM.COLUMN_URL, url);
//        values.put(CacheORM.COLUMN_DATE, _dateFormat.format(post.getDate()));

        return values;
    }



    public static void deleteAllData(Context mContext) {
        try {
            DatabaseWrapper databaseWrapper = new DatabaseWrapper(mContext);
            SQLiteDatabase database = databaseWrapper.getReadableDatabase();
            if (database != null) {
                int count = database.delete(TABLE_NAME, "1", null);
                database.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
