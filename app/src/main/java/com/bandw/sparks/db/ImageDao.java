package com.bandw.sparks.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertImage(Image image);

    @Delete
    void deleteImage(Image image);

    @Query("SELECT * FROM image")
    public Image[] loadAllImages();
}
