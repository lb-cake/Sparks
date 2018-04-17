package com.bandw.sparks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bandw.sparks.db.GalleryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by wap5053 on 4/17/18.
 */

public class GalleryLab {
    private static GalleryLab sGalleryLab;
    private List<GalleryItem> mGalleryItems;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    private GalleryLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new SparksBaseHelper(mContext).getWritableDatabase();
        mGalleryItems = new ArrayList<>();
    }

    public static GalleryLab get(Context context){
        if (sGalleryLab == null) {
            sGalleryLab = new GalleryLab(context);
        }
        return sGalleryLab;
    }

    public List<GalleryItem> getGalleryItems() {
        return mGalleryItems;
    }

    public GalleryItem getGalleryItem(int id) {
        for (GalleryItem galleryItem: mGalleryItems) {
            if (galleryItem.get_id() == id) {
                return galleryItem;
            }
        }
        return null;
    }
}
