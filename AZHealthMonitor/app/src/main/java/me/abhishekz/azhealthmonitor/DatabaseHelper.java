package me.abhishekz.azhealthmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "AZHealthMonitor.db";
    private String Table_Name="";

    public DatabaseHelper(Context context, String t_name) {
        super(context, DB_NAME, null, 1);
        Table_Name=t_name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + Table_Name + " ( TIMESTAMP VARCHAR2(10), COL_X REAL, COL_Y REAL, COL_Z REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        db.execSQL("DROP TABLE IF EXISTS " + Table_Name);
        onCreate(db);
    }

    public boolean insertData(String ts, float x, float y, float z){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("TIMESTAMP", ts);
        contentValues.put("COL_X", x);
        contentValues.put("COL_Y", y);
        contentValues.put("COL_Z", z);
        long result = db.insert(Table_Name, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData (){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + Table_Name + " ORDER BY TIMESTAMP DESC LIMIT 10", null);
        return res;
    }
}
