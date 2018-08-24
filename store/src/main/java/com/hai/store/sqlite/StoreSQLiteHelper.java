package com.hai.store.sqlite;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.hai.store.sqlite.DBConstant.CREATE_SQL;
import static com.hai.store.sqlite.DBConstant.DB_NAME;
import static com.hai.store.sqlite.DBConstant.VERSION;

class StoreSQLiteHelper extends SQLiteOpenHelper {

    private static StoreSQLiteHelper INSTANCE;

    private StoreSQLiteHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static StoreSQLiteHelper getInstance(Context context) {
        if (null == INSTANCE) {
            synchronized (StoreSQLiteHelper.class) {
                if (null == INSTANCE) {
                    INSTANCE = new StoreSQLiteHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
