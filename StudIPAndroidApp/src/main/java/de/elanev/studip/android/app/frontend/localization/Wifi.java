package de.elanev.studip.android.app.frontend.localization;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Nils on 24.04.2015.
 */
public class Wifi extends Application {

    private static SQLiteOpenHelper openHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        openHelper = new DatabaseHelper(this);
        Wifi.getWritableDatabase().execSQL("PRAGMA foreign_keys=ON;");
    }

    public static SQLiteDatabase getWritableDatabase() {
        return openHelper.getWritableDatabase();
    }

    public static SQLiteDatabase getReadableDatabase() {
        return openHelper.getReadableDatabase();
    }
}
