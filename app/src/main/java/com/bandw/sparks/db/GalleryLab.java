package com.bandw.sparks.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bandw.sparks.db.SparksDbSchema.SavedTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wap5053 on 4/17/18.
 */

public class GalleryLab {
    private static GalleryLab sGalleryLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private GalleryLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new SparksBaseHelper(mContext).getWritableDatabase();
    }

    public static GalleryLab get(Context context){
        if (sGalleryLab == null) {
            sGalleryLab = new GalleryLab(context);
        }
        return sGalleryLab;
    }

    public List<GalleryItem> getGalleryItems() {
        List<GalleryItem> items = new ArrayList<>();

        try (GalleryItemCursorWrapper cursor = queryGalleryItems(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                items.add(cursor.getGalleryItem());
                cursor.moveToNext();
            }
        }
        return items;
    }

    public void addGalleryItem(GalleryItem g) {
        ContentValues values = getContentValues(g);
        mDatabase.insert(SavedTable.NAME, null, values);
    }

    public GalleryItem getGalleryItem(String id) {

        try (GalleryItemCursorWrapper cursor = queryGalleryItems(
                SavedTable.Columns.UUID + "= ?",
                new String[]{id}
        )) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getGalleryItem();
        }
    }

    public void updateGalleryItem(GalleryItem galleryItem) {
        String id = galleryItem.getId();
        ContentValues values = getContentValues(galleryItem);

        mDatabase.update(SavedTable.NAME, values, SavedTable.Columns.UUID + " = ?",
                new String[] {id});
    }

    private GalleryItemCursorWrapper queryGalleryItems(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                SavedTable.NAME,
                null, //columns - null selects all columns
                whereClause,
                whereArgs,
                null, //group by
                null, //having
                null //order by
        );
        return new GalleryItemCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(GalleryItem galleryItem) {
        ContentValues values = new ContentValues();
        values.put(SavedTable.Columns.UUID, galleryItem.getId());
        values.put(SavedTable.Columns.TITLE, galleryItem.getCaption());
        values.put(SavedTable.Columns.URL, galleryItem.getURL());
        values.put(SavedTable.Columns.OWNER, galleryItem.getURL());
        return values;
    }
}
