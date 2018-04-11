package com.bandw.sparks.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Image.class}, version = 1)
public abstract class SparksDatabase extends RoomDatabase {

    /**
     * @return the DAO for the Image table
     */
    @SuppressWarnings("WeakerAccess")
    public abstract ImageDao imageDao();

    /** The only instance **/
    private static SparksDatabase sInstance;

    /**
     * Gets the singleton instance of the SparksDatabase
     *
     * @param context The context.
     * @return The singleton instance of SparksDatabase
     */

    public static synchronized SparksDatabase getsInstance(Context context) {
        if (sInstance == null){
            sInstance = Room
                    .databaseBuilder(context.getApplicationContext(), SparksDatabase.class, "sparks")
                    .build();
        }
        return sInstance;
    }
}
