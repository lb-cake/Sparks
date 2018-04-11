package com.bandw.sparks.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface GalleryItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGalleryItem(GalleryItem item);

    @Delete
    void deleteGalleryItem(GalleryItem item);

    @Query("SELECT * FROM gallery_item")
    public GalleryItem[] loadAllItems();
}
