package me.abhishekz.azhealthmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "AZHealthMonitor.db";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        //Table_Name="Records";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    }

    //Insert sensor data into table
    public boolean insertData(String t_name, String ts, float x, float y, float z){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TIMESTAMP", ts);
        contentValues.put("COL_X", x);
        contentValues.put("COL_Y", y);
        contentValues.put("COL_Z", z);
        long result = db.insert(t_name, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    //Method to create table in database
    public void createTable (String t_name){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + t_name);
        db.execSQL("CREATE TABLE " + t_name + " ( TIMESTAMP VARCHAR2(10), COL_X REAL, COL_Y REAL, COL_Z REAL)");
    }

    //Method to retrieve last 10 seconds from database
    public Cursor getAllData (String t_name){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + t_name + " ORDER BY TIMESTAMP DESC LIMIT 10", null);
        return res;
    }

    //Method to retrieve table names from downloaded db file
    public Cursor getLastTable (){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res_last = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' and name != 'android_metadata' ORDER BY name DESC LIMIT 1", null);
        return res_last;
    }

}
