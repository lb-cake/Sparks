package com.bandw.sparks;

import android.database.sqlite.SQLiteDatabase;

public class SparksDbSchema {

    public static final class SavedTable {
        public static final String NAME = "saved";

        public static final class Columns {
            public static final String UUID = "uuid";
            public static final String URL = "url_s";
            public static final String TITLE = "title";
            public static final String OWNER = "owner";
            public static final String DATE = "date";
        }
    }

}
