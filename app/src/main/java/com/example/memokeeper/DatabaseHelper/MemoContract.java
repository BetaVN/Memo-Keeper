package com.example.memokeeper.DatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.memokeeper.MainScreen.MemoInfo;

import java.util.ArrayList;

public final class MemoContract {

    public static final class MemoEntry implements BaseColumns {

        public final static String TABLE_NAME = "Memo";
        public final static String BACKUP_TABLE_NAME = "BackupMemo";
        public final static String _ID = BaseColumns._ID;
        public final static String COLLUMN_MEMO_TITLE = "title";
        public final static String COLLUMN_MEMO_CONTENT = "content";
        public final static String COLLUMN_MEMO_ATTACHMENT = "attachment";
        public final static String COLLUMN_MEMO_DATE = "date";
        public final static String COLLUMN_MEMO_HASH = "hash";
        public final static String INTEGER = "INTEGER";
        public final static String TEXT = "TEXT";


        public final static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " " + INTEGER + " , "
                + COLLUMN_MEMO_TITLE + " " + TEXT + " NOT NULL, "
                + COLLUMN_MEMO_CONTENT + " " + TEXT + " NOT NULL, "
                + COLLUMN_MEMO_DATE + " " + INTEGER + " NOT NULL, "
                + COLLUMN_MEMO_HASH + " " + TEXT + " PRIMARY KEY NOT NULL, "
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

        public Boolean addNewMemo(MemoInfo newMemo) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues memo = new ContentValues();
            memo.put(MemoEntry.COLLUMN_MEMO_TITLE, newMemo.memoTitle);
            memo.put(MemoEntry.COLLUMN_MEMO_CONTENT, newMemo.memoText);
            memo.put(MemoEntry.COLLUMN_MEMO_ATTACHMENT, newMemo.memoAttachment);
            memo.put(MemoEntry.COLLUMN_MEMO_DATE, newMemo.memoDate);
            memo.put(MemoEntry.COLLUMN_MEMO_HASH, newMemo.hash);

            if (db.insert(MemoEntry.TABLE_NAME, null, memo) > -1) {
                Log.d("Database", "Memo added successfully!");
                return true;
            }
            else {
                Log.d("Database", "Failed to add memo!");
                return false;
            }
        }

        public Cursor getAllMemo() {
            SQLiteDatabase db = this.getReadableDatabase();
            String[] projection = { MemoEntry.COLLUMN_MEMO_TITLE, MemoEntry.COLLUMN_MEMO_CONTENT, MemoEntry.COLLUMN_MEMO_ATTACHMENT, MemoEntry.COLLUMN_MEMO_DATE, MemoEntry.COLLUMN_MEMO_HASH};
            Cursor result = db.query( MemoEntry.TABLE_NAME, projection, "0=0", null, null, null, MemoEntry.COLLUMN_MEMO_DATE + " DESC");
            return result;
        }

        public void deleteMemo(String hash) {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db.delete(MemoEntry.TABLE_NAME, MemoEntry.COLLUMN_MEMO_HASH + " = ?", new String[]{hash}) > -1) {
                Log.d("Database", "Memo deleted successfully!");
            }
            else {
                Log.d("Database", "Can't find memo to delete!");
            }
        }

        public void updateMemo(MemoInfo updatedMemo) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues memo = new ContentValues();
            memo.put(MemoEntry.COLLUMN_MEMO_TITLE, updatedMemo.memoTitle);
            memo.put(MemoEntry.COLLUMN_MEMO_CONTENT, updatedMemo.memoText);
            memo.put(MemoEntry.COLLUMN_MEMO_ATTACHMENT, updatedMemo.memoAttachment);
            memo.put(MemoEntry.COLLUMN_MEMO_DATE, updatedMemo.memoDate);
            if (db.update(MemoEntry.TABLE_NAME, memo, MemoEntry.COLLUMN_MEMO_HASH + " = ?", new String[]{updatedMemo.hash}) > -1) {
                Log.d("Database", "Memo updated successfully!");
            }
            else {
                Log.d("Database", "Failed to update memo!");
            }
        }

        public Cursor getAllFolders() {
            SQLiteDatabase db = this.getReadableDatabase();
            String[] projection = {MemoEntry.COLLUMN_MEMO_HASH};
            Cursor result = db.query( MemoEntry.TABLE_NAME, projection, "0=0", null, null, null, MemoEntry.COLLUMN_MEMO_DATE + " DESC");
            return result;
        }

        public void updateFromBackup(ArrayList<MemoInfo> backupMemo) {
            for (MemoInfo memoInfo: backupMemo) {
                if (!addNewMemo(memoInfo)) {
                    updateMemo(memoInfo);
                }
            }
        }
    }
}
