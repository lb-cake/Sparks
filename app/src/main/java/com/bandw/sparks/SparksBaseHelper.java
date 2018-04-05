package com.bandw.sparks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.bandw.sparks.SparksDbSchema.SavedTable;

public class SparksBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "sparks.db";
    private static SparksBaseHelper sparksDb;

    //singleton pattern
    private SparksBaseHelper(Context context) { super(context, DATABASE_NAME, null, VERSION); }

    public static synchronized SparksBaseHelper getInstance(Context context) {
        if (sparksDb == null)
            sparksDb = new SparksBaseHelper(context);
        return sparksDb;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + SavedTable.NAME + "(" +
                SavedTable.Columns.UUID + ", " +
                SavedTable.Columns.TITLE + ", " +
                SavedTable.Columns.DATE +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void getWritableDatabase(onDBReadyListener listener) {
        new CreateDBAsyncTask().execute(listener);
    }

    public interface onDBReadyListener {
        void onDBReady(SQLiteDatabase db);
    }

    private class CreateDBAsyncTask extends AsyncTask<onDBReadyListener, Void, SQLiteDatabase> {
        onDBReadyListener listener;
        @Override
        protected SQLiteDatabase doInBackground(onDBReadyListener... params) {
            listener = params[0];
            return SparksBaseHelper.sparksDb.getWritableDatabase();
        }

        @Override
        protected void onPostExecute(SQLiteDatabase db) {
            //Make the callback
            listener.onDBReady(db);
        }
    }
}
