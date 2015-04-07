package marshrutik.marshrutik;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.Callback;

public class DatabaseTable {
    private final DatabaseOpenHelper databaseOpenHelper;

    private static final String TAG = "CitiesDatabase";

    public static final String BASE_URL = "https://rocky-headland-7761.herokuapp.com/api";

    //колонки, которые мы включим в нашу таблицу
    public static final String COL_CITY_ID = "CITY_ID";
    public static final String COL_CITY = "CITY";

    private static final String DATABASE_NAME = "CitiesRU";
    private static final int DATABASE_VERSION = 8;
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
                        COL_CITY_ID + ", " +
                        COL_CITY + ")";


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
            RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .build();
            API service = restAdapter.create(API.class);
            Callback<List<City>> callback = new Callback<List<City>>() {
                @Override
                public void success(List<City> cities, Response response) {
                    for (int i = 0; i<cities.size(); i++) {
                        Log.d("ALARM", String.valueOf(cities.get(i).getCityId())
                                + " " + cities.get(i).getCityName());
                        long id = addCity(cities.get(i).getCityId(),
                                cities.get(i).getCityName());
                        if (id < 0) {
                            Log.e(TAG, "Unable to add city: " + cities.get(i).getCityName());
                        }
                    }
                }
                @Override
                public void failure(RetrofitError error) {
                    Log.d("ALARM", "There are called failure");
                }
            };
            service.getCity(callback); /*вызывает функцию считывания информации из REST API
                                    в callback'e обрабатываю данные и передаю в addCity() */
//            //реализация для добавления в SQLite таблицу из файла в raw
//            final Resources resources = helperContext.getResources();
//            InputStream inputStream = resources.openRawResource(R.raw.cities);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            try {
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    String[] strings = TextUtils.split(line, "—");
//                    if (strings.length < 2) continue;
//                    long id = addCity(strings[0], strings[1].trim());
//                    if (id < 0) {
//                        Log.e(TAG, "Unable to add city: " + strings[0].trim());
//                    }
//                }
//            } finally {
//                reader.close();
//            }
        }

        public long addCity(int cityId, String city) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_CITY_ID, cityId);
            initialValues.put(COL_CITY, city);

            return database.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }
}
