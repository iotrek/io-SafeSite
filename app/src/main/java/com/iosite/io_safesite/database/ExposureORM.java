package com.iosite.io_safesite.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.iosite.io_safesite.Pojo.ExposureData;
import com.iosite.io_safesite.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class ExposureORM {
    private static final String TAG = "ExposureORM";

    private static final String TABLE_NAME = "exposure";

    private static final String COMMA_SEP = ", ";
    private static final String COLUMN_DATE = "pubdate";

    private static final String COLUMN_ID = "unique_id";
    private static final String COLUMN_BEACON_ID = "beacon_id";
    private static final String COLUMN_MEAN_DISTANCE = "mean_distance";
    private static final String COLUMN_MEDIAN_DISTANCE = "median_distance";
    private static final String COLUMN_MIN_DISTANCE = "min_distance";
    private static final String COLUMN_MAX_DISTANCE = "max_distance";
    private static final String COLUMN_MEAN_RSSI = "mean_rssi";
    private static final String COLUMN_MEAN_TX_POWER = "mean_tx_power";
    private static final String COLUMN_EXPOSURE_START = "exposure_start";
    private static final String COLUMN_EXPOSURE_END = "exposure_end";
    private static final String COLUMN_NUMBER_DATA = "number_of_data";
    private static final String COLUMN_IS_SENT = "is_sent";

    private static final String COLUMN_TEXT_TYPE = "TEXT";
    private static final String COLUMN_LONG_TYPE = "LONG";
    private static final String COLUMN_INT_TYPE = "INT";
    private static final String COLUMN_PRIMARY_KEY_TYPE = "INTEGER NOT NULL PRIMARY KEY";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_LONG_TYPE + COMMA_SEP +
                    COLUMN_BEACON_ID + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_MEAN_DISTANCE + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_MEDIAN_DISTANCE + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_MIN_DISTANCE + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_MAX_DISTANCE + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_MEAN_RSSI + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_MEAN_TX_POWER + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_EXPOSURE_START + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_EXPOSURE_END + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_NUMBER_DATA + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_IS_SENT + " " + COLUMN_INT_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static boolean insertExposureData(ExposureData exposureData)
    {
        ContentValues values = postToContentValues(exposureData);
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();

        boolean success = false;
        try {
            if (database != null) {
                long postId = database.insert(TABLE_NAME, "null", values);
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

    private static ContentValues postToContentValues(ExposureData exposureData)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, System.currentTimeMillis());
        values.put(COLUMN_BEACON_ID, exposureData.beaconId);
        values.put(COLUMN_MEAN_DISTANCE, exposureData.mean_distance);
        values.put(COLUMN_MEDIAN_DISTANCE, exposureData.median_distance);
        values.put(COLUMN_MIN_DISTANCE, exposureData.min_distance);
        values.put(COLUMN_MAX_DISTANCE, exposureData.max_distance);
        values.put(COLUMN_MEAN_RSSI, exposureData.mean_rssi);
        values.put(COLUMN_MEAN_TX_POWER, exposureData.mean_txPower);
        values.put(COLUMN_EXPOSURE_START, exposureData.exposure_start);
        values.put(COLUMN_EXPOSURE_END, exposureData.exposure_end);
        values.put(COLUMN_NUMBER_DATA, exposureData.numberOf_data);
        values.put(COLUMN_IS_SENT, 0);
        return values;
    }

    public static LinkedList<ExposureData> getExposureData() {
        LinkedList<ExposureData> data = new LinkedList<>();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    ExposureData exposureValue = new ExposureData();
                    exposureValue.unique_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    exposureValue.beaconId = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));
                    exposureValue.mean_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_DISTANCE));
                    exposureValue.median_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MEDIAN_DISTANCE));
                    exposureValue.min_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MIN_DISTANCE));
                    exposureValue.max_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MAX_DISTANCE));
                    exposureValue.mean_rssi = cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_RSSI));
                    exposureValue.mean_txPower = cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_TX_POWER));
                    exposureValue.exposure_start = cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_START));
                    exposureValue.exposure_end = cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_END));
                    exposureValue.numberOf_data = cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_DATA));
                    data.add(exposureValue);
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return data;
    }

    public static LinkedList<ExposureData> getNotSentExposureData()
    {
        LinkedList<ExposureData> data = new LinkedList<>();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "  + COLUMN_IS_SENT + " = '0'", null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    ExposureData exposureValue = new ExposureData();
                    exposureValue.unique_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    exposureValue.beaconId = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));
                    exposureValue.mean_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_DISTANCE));
                    exposureValue.median_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MEDIAN_DISTANCE));
                    exposureValue.min_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MIN_DISTANCE));
                    exposureValue.max_distance = cursor.getString(cursor.getColumnIndex(COLUMN_MAX_DISTANCE));
                    exposureValue.mean_rssi = cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_RSSI));
                    exposureValue.mean_txPower = cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_TX_POWER));
                    exposureValue.exposure_start = cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_START));
                    exposureValue.exposure_end = cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_END));
                    exposureValue.numberOf_data = cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_DATA));
                    data.add(exposureValue);
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return data;
    }

    public static JSONArray getExposureDataJsonArray(int startCount, int endCount) {
        JSONArray jsonArray = new JSONArray();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            Log.e("CursorCountSize", String.valueOf(cursor.getCount()));
            if (cursor.getCount() > startCount) {
                cursor.moveToPosition(startCount);
                //if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                if (cursor != null && cursor.getCount() > 0) {
                    do {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(Constants.PARAM_ID, String.valueOf(cursor.getLong(cursor.getColumnIndex(COLUMN_ID))));
                            jsonObject.put(Constants.PARAM_BEACON_ID, cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEAN_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEDIAN_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MEDIAN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MIN_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MIN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MAX_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MAX_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEAN_RSSI, cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_RSSI)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEAN_TX_POWER, cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_TX_POWER)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_START, cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_START)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_END, cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_END)));
                            jsonObject.put(Constants.PARAM_DATA_SETS, cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_DATA)));
                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (cursor.getPosition() == endCount) {
                            cursor.close();
                            break;
                        }
                    }
                    while (cursor.moveToNext());
                }
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return jsonArray;
    }

    // todo: update the sql query to get json array of not-sent data
    public static JSONArray getNotSentExposureDataJsonArray(int startCount, int endCount) {
        JSONArray jsonArray = new JSONArray();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "  + COLUMN_IS_SENT + " = '0'", null);
            Log.e("CursorCountSize", String.valueOf(cursor.getCount()));
            if (cursor.getCount() > startCount) {
                cursor.moveToPosition(startCount);
                //if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                if (cursor != null && cursor.getCount() > 0) {
                    do {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(Constants.PARAM_ID, String.valueOf(cursor.getLong(cursor.getColumnIndex(COLUMN_ID))));
                            jsonObject.put(Constants.PARAM_BEACON_ID, cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEAN_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEDIAN_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MEDIAN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MIN_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MIN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MAX_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_MAX_DISTANCE)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEAN_RSSI, cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_RSSI)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_MEAN_TX_POWER, cursor.getString(cursor.getColumnIndex(COLUMN_MEAN_TX_POWER)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_START, cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_START)));
                            jsonObject.put(Constants.PARAM_EXPOSURE_END, cursor.getString(cursor.getColumnIndex(COLUMN_EXPOSURE_END)));
                            jsonObject.put(Constants.PARAM_DATA_SETS, cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER_DATA)));
                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (cursor.getPosition() == endCount) {
                            cursor.close();
                            break;
                        }
                    }
                    while (cursor.moveToNext());
                }
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return jsonArray;
    }

    public static boolean
    updateIsSentColumn(int isRead) {
        ContentValues values = markReadDataAsSent(isRead);
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        int notRead;
        if (isRead == 1) {
            notRead = 0;
        } else {
            notRead = 1;
        }

        boolean success = false;
        try {  // NAME + " = ? AND " + LASTNAME + " = ?"
            if (database != null) {
                long updateId = database.update(TABLE_NAME, values, COLUMN_IS_SENT + " = ?",
                        new String[]{String.valueOf(notRead)});
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

    private static ContentValues markReadDataAsSent(int isRead) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_SENT, isRead);
        return values;
    }


}
