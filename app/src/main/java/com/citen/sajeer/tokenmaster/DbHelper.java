package com.citen.sajeer.tokenmaster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josepg4 on 7/5/17.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";

    private static final String DATABASE_NAME = "AdDatabase";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_ADLIST = "adDetails";

    private static final String _ID = "_id";
    private static final String ADSPACEID = "adSpaceId";
    private static final String ADNAME = "adName";
    private static final String ADFILENAME = "adFileName";
    private static final String ADPATH = "adPath";
    private static final String STATUS = "adStatus";

    private static DbHelper mDbHelper;

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TOKEN_TABLE = "CREATE TABLE " + TABLE_ADLIST +
                "(" +
                _ID + " INTEGER PRIMARY KEY ," +
                ADSPACEID + " INTEGER," +
                STATUS + " INTEGER," +
                ADNAME + " TEXT," +
                ADFILENAME + " TEXT," +
                ADPATH + " TEXT" +
                ")";
        db.execSQL(CREATE_TOKEN_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADLIST);

            onCreate(db);
        }
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    static synchronized DbHelper getInstance(Context context) {
        if(mDbHelper == null){
            mDbHelper = new DbHelper(context.getApplicationContext());
        }
        return mDbHelper;
    }

    void insertAd(AdData adData, int adStatus){
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            ContentValues values = new ContentValues();
            values.put(ADSPACEID, adData.getAdSpaceId());
            values.put(ADNAME, adData.getDisplayName());
            values.put(ADFILENAME, adData.getFileName());
            values.put(ADPATH, adData.getDirectoryPath());
            values.put(STATUS, adStatus);

            db.insertOrThrow(TABLE_ADLIST, null, values);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            Log.d(TAG, "added something");
            db.endTransaction();
        }
    }

    ArrayList<AdData> getAdList(int adSpaceId){

        ArrayList<AdData> adList = new ArrayList<>();

        String AD_LIST_SELECT_QUERY = "SELECT * FROM " + TABLE_ADLIST + " WHERE " + ADSPACEID + " = " + Integer.toString(adSpaceId) + " ORDER BY " + STATUS +  " ASC";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(AD_LIST_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                do {

                    AdData readData = new AdData();
                    readData.setAdSpaceId(cursor.getInt(cursor.getColumnIndex(ADSPACEID)));
                    readData.setDirectoryPath(cursor.getString(cursor.getColumnIndex(ADPATH)));
                    readData.setDisplayName(cursor.getString(cursor.getColumnIndex(ADNAME)));
                    readData.setFileName(cursor.getString(cursor.getColumnIndex(ADFILENAME)));

                    adList.add(readData);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return adList;

    }

    boolean isFileNamePersent(int asSpaceId, String fileName){

        boolean isFilePresent = false;

        String USER_DETAIL_SELECT_QUERY = "SELECT * FROM " + TABLE_ADLIST + " WHERE " + ADSPACEID + " = " + asSpaceId + " AND " + ADFILENAME + " = '" + fileName + "'";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(USER_DETAIL_SELECT_QUERY, null);

        try {
            if (cursor.moveToFirst()) {
                isFilePresent = true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return isFilePresent;
    }

    void updateAdListStatus(List<AdData> adList, int adSpaceId){
        int loop = 0;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            while (loop < adList.size()) {
                ContentValues values = new ContentValues();
                values.put(STATUS, loop);

                db.update(TABLE_ADLIST, values, ADFILENAME + " = ? AND " + ADSPACEID + " = ?" , new String[]{adList.get(loop).getFileName(), Integer.toString(adSpaceId)});
                loop++;

            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    void removeSingleAd(String adfileName, int adSpaceId) {
        //Open the database
        SQLiteDatabase database = this.getWritableDatabase();

        //Execute sql query to remove from database
        //NOTE: When removing by String in SQL, value must be enclosed with ''
        database.execSQL("DELETE FROM " + TABLE_ADLIST + " WHERE " + ADSPACEID + " = "+ Integer.toString(adSpaceId) + " AND " + ADFILENAME + " = '" + adfileName + "'");

        //Close the database
        database.close();
    }
}
