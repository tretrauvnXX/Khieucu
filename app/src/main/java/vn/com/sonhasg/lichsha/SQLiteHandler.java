package vn.com.sonhasg.lichsha;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by nguyenphuoc on 04/19/2017.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "vn.com.sonhasg.lichsha";

    private static final String TABLE_USER = "NhanVien";

    private static final String FIELD_USER_ID = "id";
    private static final String FIELD_USER_LEVEL = "level";
    private static final String FIELD_USER_FULLNAME = "fullname";
    private static final String FIELD_USER_EMAIL = "email";
    private static final String FIELD_USER_PART = "part";
    private static final String FIELD_USER_PARTNAME = "partname";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + "(" + FIELD_USER_ID + " INTEGER PRIMARY KEY," + FIELD_USER_LEVEL + " TEXT," + FIELD_USER_FULLNAME + " TEXT," + FIELD_USER_EMAIL + " TEXT," + FIELD_USER_PART + " TEXT," + FIELD_USER_PARTNAME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public void addUser(String id, String level, String fullname, String email, String part, String partname) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FIELD_USER_ID, id);
        values.put(FIELD_USER_LEVEL, level);
        values.put(FIELD_USER_FULLNAME, fullname);
        values.put(FIELD_USER_EMAIL, email);
        values.put(FIELD_USER_PART, part);
        values.put(FIELD_USER_PARTNAME, partname);

        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put(FIELD_USER_ID, cursor.getString(0));
            user.put(FIELD_USER_LEVEL, cursor.getString(1));
            user.put(FIELD_USER_FULLNAME, cursor.getString(2));
            user.put(FIELD_USER_EMAIL, cursor.getString(3));
            user.put(FIELD_USER_PART, cursor.getString(4));
            user.put(FIELD_USER_PARTNAME, cursor.getString(5));
        }
        cursor.close();
        db.close();

        return user;
    }

    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USER, null, null);
        db.close();
    }
}