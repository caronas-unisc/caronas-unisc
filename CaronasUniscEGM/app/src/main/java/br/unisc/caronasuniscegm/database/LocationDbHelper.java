package br.unisc.caronasuniscegm.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String NUMERIC_TYPE = " NUMERIC";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_TABLE_LOCATION =
            "CREATE TABLE " + LocationContract.Location.TABLE_NAME + " (" +
                    LocationContract.Location._ID + " INTEGER PRIMARY KEY, " +
                    LocationContract.Location.NAME + TEXT_TYPE + COMMA_SEP +
                    LocationContract.Location.LATITUDE + NUMERIC_TYPE + COMMA_SEP +
                    LocationContract.Location.LONGITUDE + NUMERIC_TYPE + COMMA_SEP +
                    LocationContract.Location.WAYPOINTS + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_TABLE_LOCATION =
            "DROP TABLE IF EXISTS " + LocationContract.Location.TABLE_NAME;

    // Se você modificar o schema do banco, você deve incrementar a versão do software.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Pessoa.db";

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This br.unisc.aula8.database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_TABLE_LOCATION);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
