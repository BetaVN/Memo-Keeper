package com.example.memokeeper.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.memokeeper.MainScreen.MemoInfo;

public final class MemoContract {

    public static final class MemoEntry implements BaseColumns {

        public final static String TABLE_NAME = "Enemy";
        public final static String _ID = BaseColumns._ID;
        public final static String COLLUMN_MEMO_TITLE = "title";
        public final static String COLLUMN_MEMO_CONTENT = "content";
        public final static String COLLUMN_MEMO_ATTACHMENT = "attachment";
        public final static String COLLUMN_MEMO_DATE = "date";
        public final static String INTEGER = "INTEGER";
        public final static String TEXT = "TEXT";


        public final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " " + INTEGER + " PRIMARY KEY AUTOINCREMENT, "
                + COLLUMN_MEMO_TITLE + " " + TEXT + " NOT NULL, "
                + COLLUMN_MEMO_CONTENT + " " + TEXT + " NOT NULL, "
                + COLLUMN_MEMO_DATE + " " + TEXT + " NOT NULL, "
                + COLLUMN_MEMO_ATTACHMENT + " " + TEXT + ")";

        public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }

    public class MemoDbHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = MemoEntry.TABLE_NAME + ".db";

        public MemoDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(MemoEntry.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
            db.execSQL(MemoEntry.DROP_TABLE);
            onCreate(db);
        }

        public void addNewMemo(MemoInfo newMemo) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues memo = new ContentValues();
            memo.put(MemoEntry.COLLUMN_MEMO_TITLE, newMemo.memoTitle);
            memo.put(MemoEntry.COLLUMN_MEMO_CONTENT, newMemo.memoText);
            memo.put(MemoEntry.COLLUMN_MEMO_ATTACHMENT, "");
            memo.put(MemoEntry.COLLUMN_MEMO_DATE, "");

            long result = db.insert(MemoEntry.TABLE_NAME, null, memo);
            if (result == -1) {
                Log.d("Database", "Memo added");
            }
            else {
                Log.d("Database", "Failed to add memo");
            }
        }

        public Cursor getAllMemo() {
            SQLiteDatabase db = this.getReadableDatabase();
            String[] projection = { MemoEntry.COLLUMN_MEMO_TITLE, MemoEntry.COLLUMN_MEMO_CONTENT, MemoEntry.COLLUMN_MEMO_ATTACHMENT, MemoEntry.COLLUMN_MEMO_DATE};
            Cursor result = db.query( MemoEntry.TABLE_NAME, projection, null, null, null, null, MemoEntry._ID + " DESC");
            return result;
        }
    }
}
