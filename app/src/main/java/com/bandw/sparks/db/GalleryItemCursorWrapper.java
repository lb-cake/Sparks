package com.bandw.sparks.db;

import android.database.Cursor;
import android.database.CursorWrapper;

public class GalleryItemCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public GalleryItemCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public GalleryItem getGalleryItem() {
        String id = getString(getColumnIndex(SparksDbSchema.SavedTable.Columns.UUID));
        String title = getString(getColumnIndex(SparksDbSchema.SavedTable.Columns.TITLE));
        String  url = getString(getColumnIndex(SparksDbSchema.SavedTable.Columns.URL));
        String owner = getString(getColumnIndex(SparksDbSchema.SavedTable.Columns.OWNER));

        GalleryItem galleryItem = new GalleryItem();
        galleryItem.setId(id);
        galleryItem.setCaption(title);
        galleryItem.setURL(url);
        galleryItem.setOwner(owner);

        return galleryItem;
    }
}
