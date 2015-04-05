package marshrutik.marshrutik;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DatabaseTable {
    private final DatabaseOpenHelper databaseOpenHelper;

    private static final String TAG = "CitiesDatabase";

    //колонки, которые мы включим в нашу таблицу
    public static final String COL_CITY = "CITY";
    public static final String COL_COUNTRY = "COUNTRY";

    private static final String DATABASE_NAME = "Cities";
    private static final int DATABASE_VERSION = 5;
    private static final String FTS_VIRTUAL_TABLE = "FTS";



    public DatabaseTable(Context context) {
        databaseOpenHelper = new DatabaseOpenHelper(context);
    }
    public Cursor getCityMatches(String query, String[] columns) {
        String selection = COL_CITY + " MATCH ?";
        if (query.contains("-")) {      //SQLite не умеет искать с тире, поэтому меняем все тире
            query = query.replace("-", "_");    //на знак любого 1 символа
        }
        String[] selectionArgs = new String[] {query};
        //если требуется найти только по начальной части запроса, то {query+"*"}

        return query(selection, selectionArgs, columns);
    }
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }



    private static class DatabaseOpenHelper extends SQLiteOpenHelper {
        //класс, который считывает данные из внещнего источника (сейчас это файл cities.txt /res/raw
        //и записывает их в SQLite базу

        private final Context helperContext;
        private SQLiteDatabase database;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_CITY + ", " +
                        COL_COUNTRY + ")";


        DatabaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            helperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            database = db;
            database.execSQL(FTS_TABLE_CREATE);
            loadInformation();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //вызывается только при смене версии БД. Версия описана констой сверху
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    +newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
        private void loadInformation() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        loadCities();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        private void loadCities() throws IOException {
            final Resources resources = helperContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.cities);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] strings = TextUtils.split(line, "—");
                    if (strings.length < 2) continue;
                    long id = addCity(strings[0], strings[1].trim());
                    if (id < 0) {
                        Log.e(TAG, "Unable to add city: " + strings[0].trim());
                    }
                }
            } finally {
                reader.close();
            }
        }
        public long addCity(String city, String country) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_CITY, city);
            initialValues.put(COL_COUNTRY, country);

            return database.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }
}
