package com.cslending.zc.collapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDB {
    public static final String KEY_ID = "id";
    public static final String KEY_LNAME = "lastname";
    public static final String KEY_LOAN = "cn";
    public static final String KEY_AMORT = "amort";
    public static final String KEY_AMT = "amt";
    public static final String KEY_PAID = "paid";
    public static final String KEY_GCODE = "gcode";
    public static final String KEY_SENT = "sent";
    public static final String KEY_REMARK = "rem";
    public static final String KEY_IMAGE = "img";
    public static final String KEY_TIMESTAMP = "dt";

    private static final String DATABASE_NAME = "SQLiteDB";
    private static final String DATABASE_TABLE = "csl_db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
            "create table csl_db (" + KEY_ID + " text not null, " + KEY_LNAME + " text not null, " + KEY_LOAN + " text not null, " + KEY_AMORT + " text not null, " + KEY_AMT + " text default '0' not null, " + KEY_PAID + " text not null, " + KEY_GCODE + " text not null, " + KEY_SENT + " text default '0' not null, " + KEY_REMARK + " text default '' not null, " + KEY_IMAGE + " text default '' not null, " + KEY_TIMESTAMP + " text default '' not null);";

    private final DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public SQLiteDB(Context ctx) {
        DBHelper = new DatabaseHelper(ctx);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS csl_db");
            onCreate(db);
        }
    }

    public void open() throws SQLException {
        db = DBHelper.getWritableDatabase();
    }

    public void read() throws SQLException {
        db = DBHelper.getReadableDatabase();
    }

    public void close() throws SQLException {
        DBHelper.close();
    }

    public void regenDB() throws SQLException {
        db.execSQL("DROP TABLE IF EXISTS csl_db");
        db.execSQL(DATABASE_CREATE);
    }

    public Cursor queryOfflineData()
    {
        return db.rawQuery("SELECT * FROM " + DATABASE_TABLE, null);
    }

    public void SyncOfflineDB(String id, String lname, String loan, String amort, String amt, String paid, String code) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ID, id);
        initialValues.put(KEY_LNAME, lname);
        initialValues.put(KEY_LOAN, loan);
        initialValues.put(KEY_AMORT, amort);
        initialValues.put(KEY_AMT, amt);
        initialValues.put(KEY_PAID, paid);
        initialValues.put(KEY_GCODE, code);

        db.insert(DATABASE_TABLE, null, initialValues);
    }

    public Cursor getLoanfromQR(String qrcode) {
        return db.rawQuery("SELECT lastname, amort FROM " + DATABASE_TABLE + " WHERE cn = ? AND paid = '-'", new String[] {qrcode});
    }

    public void payYes(String qrcode, String pay, String dt, String rem, String image) {
        ContentValues args = new ContentValues();
        args.put(KEY_PAID, "Yes");
        args.put(KEY_AMT, String.valueOf(pay));
        args.put(KEY_SENT, "0");
        args.put(KEY_REMARK, rem);
        args.put(KEY_IMAGE, image);
        args.put(KEY_TIMESTAMP, dt);

        db.update(DATABASE_TABLE, args, KEY_LOAN + " = ?", new String[] {qrcode});
    }

    public void payNo(String qrcode, String dt, String rem, String image) {
        ContentValues args = new ContentValues();
        args.put(KEY_PAID, "No");
        args.put(KEY_REMARK, rem);
        args.put(KEY_IMAGE, image);
        args.put(KEY_TIMESTAMP, dt);
        db.update(DATABASE_TABLE, args, KEY_LOAN + " = ?", new String[] {qrcode});
    }

    public Cursor getUnsyncedData() {
        return db.rawQuery("SELECT id, amt, paid, dt, rem, img FROM " + DATABASE_TABLE + " WHERE sent = '0' AND (paid = 'Yes' OR paid = 'No')", null);
    }

    public void setPaySent(String id) {
        ContentValues args = new ContentValues();
        args.put(KEY_SENT, "1");
        db.update(DATABASE_TABLE, args, KEY_ID + " = ?", new String[] {id});
    }

    public Cursor getLoanfromSC(String sc) {
        return db.rawQuery("SELECT cn FROM " + DATABASE_TABLE + " WHERE gcode = ? AND paid = '-'", new String[] {sc});
    }

    public Cursor queryTable() {
        return db.rawQuery("SELECT lastname, cn, paid, amt FROM " + DATABASE_TABLE + " ORDER BY lastname ASC", null);
    }
}
