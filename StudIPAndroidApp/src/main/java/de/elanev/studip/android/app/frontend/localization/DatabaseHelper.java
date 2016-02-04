package de.elanev.studip.android.app.frontend.localization;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nils on 24.04.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/de.elanev.studip.android.app/databases/";

    private static String DB_NAME ="wifi_prints.db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /*
    public DatabaseHelper(Context ctx) {
        super(ctx, "wifi_prints.db", null, 3);
    }
    */

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    public void createDatabase() throws IOException {

        boolean dbExists = checkDatabase();

        if(dbExists) {
            //Nothing to do here
        } else {
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }

    }

    private boolean checkDatabase() {
/*
        SQLiteDatabase checkDB = null;

        try {

            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch(SQLiteException e) {
            //DB does not exist
        }

        if(checkDB != null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
        */
        File dbFile = myContext.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    private void copyDatabase() throws IOException {
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDatabase() throws IOException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    public SQLiteDatabase getWritableDatabase() {
        if(myDataBase != null) {
            return myDataBase;
        } else {
            try {
                createDatabase();
                openDatabase();
                return myDataBase;
            } catch (IOException e) {
                Log.d("DatabaseHelper", "Could not access database");
            }
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        db.execSQL("CREATE TABLE places (" +
                    " _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " place TEXT NOT NULL, " +
                    " fingerprint TEXT NOT NULL);");
         */

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        db.execSQL("DROP TABLE IF EXISTS places");
        onCreate(db);
        */
    }
}





//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Nils on 24.04.2015.
 */
/*
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context ctx) {
        super(ctx, "wifi_prints.db", null, 3);
    }

    @Override

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE places (" +
                    " _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " place TEXT NOT NULL, " +
                    " fingerprint TEXT NOT NULL);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS places");
        onCreate(db);
    }
}
*/