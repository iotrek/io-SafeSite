package com.iosite.io_safesite.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.iosite.io_safesite.Pojo.ProximityData;
import com.iosite.io_safesite.Util.Constants;
import com.iosite.io_safesite.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

public class ProximityORM {
    private static final String TAG = "ProximityORM";

    private static final String TABLE_NAME = "proximity";

    private static final String COMMA_SEP = ", ";
    private static final String COLUMN_DATE = "pubdate";

    private static final String COLUMN_ID = "unique_id";
    private static final String COLUMN_BEACON_ID = "beacon_id";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_RSSI = "rssi";
    private static final String COLUMN_TX_POWER = "tx_power";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_IS_COMPUTED = "is_computed";

    private static final String COLUMN_TEXT_TYPE = "TEXT";
    private static final String COLUMN_LONG_TYPE = "LONG";
    private static final String COLUMN_INT_TYPE = "INT";
    private static final String COLUMN_BOOLEAN_TYPE = "BOOLEAN";
    private static final String COLUMN_PRIMARY_KEY_TYPE = "INTEGER NOT NULL PRIMARY KEY";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_LONG_TYPE + COMMA_SEP +
                    COLUMN_BEACON_ID + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_DISTANCE + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_RSSI + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_TX_POWER + " " + COLUMN_TEXT_TYPE + COMMA_SEP +
                    COLUMN_CREATED_AT + " " + COLUMN_LONG_TYPE + COMMA_SEP +
                    COLUMN_IS_COMPUTED + " " + COLUMN_INT_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static boolean isDataAvailableInDB() {
        try {
            DataBaseManager dbManager = DataBaseManager.getInstance();
            SQLiteDatabase database = dbManager.openDatabase();
            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
                if (cursor.getCount() > 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ProximityData getLatestDataFromDb() {
        try {
            DataBaseManager dbManager = DataBaseManager.getInstance();
            SQLiteDatabase database = dbManager.openDatabase();
            if (database != null) {
                Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
                if (cursor.getCount() > 0) {
                    ProximityData data = new ProximityData();
                    cursor.moveToFirst();
                    data.beaconId = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));
                    data.distance = cursor.getString(cursor.getColumnIndex(COLUMN_DISTANCE));
                    data.rssi = cursor.getString(cursor.getColumnIndex(COLUMN_RSSI));
                    data.txPower = cursor.getString(cursor.getColumnIndex(COLUMN_TX_POWER));
                    data.createdAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT));
                    return data;

                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            DataBaseManager dbManager = DataBaseManager.getInstance();
            dbManager.closeDatabase();
        }
    }

    public static boolean insertProximityData(String beaconId, String distance, String rssi, String txPower) {
        ContentValues values = postToContentValues(beaconId, distance, rssi, txPower);
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

    private static ContentValues postToContentValues(String beaconId, String distance, String rssi, String txPower) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, System.currentTimeMillis());
        values.put(COLUMN_BEACON_ID, beaconId);
        values.put(COLUMN_DISTANCE, distance);
        values.put(COLUMN_RSSI, rssi);
        values.put(COLUMN_TX_POWER, txPower);
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        values.put(COLUMN_IS_COMPUTED, 0);
        return values;
    }

    public static ArrayList<ProximityData> getProximityData_kamran() {
        ArrayList<ProximityData> data = new ArrayList<>();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    ProximityData value = new ProximityData();
                    value.unique_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    value.beaconId = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));
                    value.distance = cursor.getString(cursor.getColumnIndex(COLUMN_DISTANCE));
                    value.rssi = cursor.getString(cursor.getColumnIndex(COLUMN_RSSI));
                    value.txPower = cursor.getString(cursor.getColumnIndex(COLUMN_TX_POWER));
                    value.createdAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT));
                    data.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return data;
    }

    public static LinkedList<ProximityData> getProximityData() {
        LinkedList<ProximityData> data = new LinkedList<>();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    ProximityData value = new ProximityData();
                    value.unique_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    value.beaconId = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));
                    value.distance = cursor.getString(cursor.getColumnIndex(COLUMN_DISTANCE));
                    value.rssi = cursor.getString(cursor.getColumnIndex(COLUMN_RSSI));
                    value.txPower = cursor.getString(cursor.getColumnIndex(COLUMN_TX_POWER));
                    value.createdAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT));
                    data.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return data;
    }

    public static JSONArray getProximityDataJsonArray(int startCount, int endCount) {
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
                            jsonObject.put(Constants.PARAM_PROXIMITY_RSSI, cursor.getString(cursor.getColumnIndex(COLUMN_RSSI)));
                            jsonObject.put(Constants.PARAM_PROXIMITY_TX_POWER, cursor.getString(cursor.getColumnIndex(COLUMN_TX_POWER)));
                            jsonObject.put(Constants.PARAM_PROXIMITY_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_PROXIMITY_TIMESTAMP, Util.getDateFromMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT))));
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

    public static LinkedList<ProximityData> getProximityDataForUser(String user_id) {
        LinkedList<ProximityData> data = new LinkedList<>();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_BEACON_ID + "=?" + " ORDER BY " +
                    COLUMN_CREATED_AT + " ASC ", new String[] { user_id });
//            SELECT * FROM Customers
//            WHERE Country='Mexico';
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    ProximityData value = new ProximityData();
                    value.unique_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    value.beaconId = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));
                    value.distance = cursor.getString(cursor.getColumnIndex(COLUMN_DISTANCE));
                    value.rssi = cursor.getString(cursor.getColumnIndex(COLUMN_RSSI));
                    value.txPower = cursor.getString(cursor.getColumnIndex(COLUMN_TX_POWER));
                    value.createdAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT));
                    data.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return data;
    }

    public static LinkedList<ProximityData> getIsComputedProximityDataForUser(String user_id, int unComputed) {
        LinkedList<ProximityData> data = new LinkedList<>();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_BEACON_ID + " = ? AND " + COLUMN_IS_COMPUTED + " = '0'" + " ORDER BY " +
                    COLUMN_CREATED_AT + " ASC ", new String[] { user_id });
//            SELECT * FROM Customers
//            WHERE Country='Mexico';
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    ProximityData value = new ProximityData();
                    value.unique_id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    value.beaconId = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));
                    value.distance = cursor.getString(cursor.getColumnIndex(COLUMN_DISTANCE));
                    value.rssi = cursor.getString(cursor.getColumnIndex(COLUMN_RSSI));
                    value.txPower = cursor.getString(cursor.getColumnIndex(COLUMN_TX_POWER));
                    value.createdAt = cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT));
                    data.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return data;
    }

    public static boolean updateIsComputedColumnForUser(String user_id, int markRead) {
        ContentValues values = markReadDataAsComputed(markRead);
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        int notMarkRead;
        if (markRead == 1) {
            notMarkRead = 0;
        } else {
            notMarkRead = 1;
        }

        boolean success = false;
        try {  // NAME + " = ? AND " + LASTNAME + " = ?"
            if (database != null) {
                long updateId = database.update(TABLE_NAME, values, COLUMN_BEACON_ID + " = ? AND " + COLUMN_IS_COMPUTED + " = ?",
                        new String[]{user_id, String.valueOf(notMarkRead)});
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

    public static boolean updateIsComputedColumn(int markRead) {
        ContentValues values = markReadDataAsComputed(markRead);
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        int notMarkRead;
        if (markRead == 1) {
            notMarkRead = 0;
        } else {
            notMarkRead = 1;
        }

        boolean success = false;
        try {
            if (database != null) {
                long updateId = database.update(TABLE_NAME, values,
                                COLUMN_IS_COMPUTED + "=" + String.valueOf(notMarkRead),
                        null);
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

    private static ContentValues markReadDataAsComputed(int markRead) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_COMPUTED, markRead);
        return values;
    }


    // todo: not complete yet
    public static JSONArray getProximityDataJsonArrayForUser(String user_id, int startCount, int endCount) {
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
                            jsonObject.put(Constants.PARAM_PROXIMITY_RSSI, cursor.getString(cursor.getColumnIndex(COLUMN_RSSI)));
                            jsonObject.put(Constants.PARAM_PROXIMITY_TX_POWER, cursor.getString(cursor.getColumnIndex(COLUMN_TX_POWER)));
                            jsonObject.put(Constants.PARAM_PROXIMITY_DISTANCE, cursor.getString(cursor.getColumnIndex(COLUMN_DISTANCE)));
                            jsonObject.put(Constants.PARAM_PROXIMITY_TIMESTAMP, Util.getDateFromMillis(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT))));
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

    public static LinkedList<String> getAllUniqueUserIDs() {
        LinkedList<String> uniqueUserList = new LinkedList<>();
        DataBaseManager dbManager = DataBaseManager.getInstance();
        SQLiteDatabase database = dbManager.openDatabase();
        if (database != null) {
            Cursor cursor = database.rawQuery("SELECT DISTINCT " + COLUMN_BEACON_ID + " FROM " + TABLE_NAME, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    String value = cursor.getString(cursor.getColumnIndex(COLUMN_BEACON_ID));;
                    uniqueUserList.add(value);
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbManager.closeDatabase();
        }
        return uniqueUserList;
    }

}
