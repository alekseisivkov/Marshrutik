package marshrutik.marshrutik;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SearchResultActivity extends ActionBarActivity {
//    Bitmap resultBitmap;
    Drawable drawable;
    public static final String KEY_TITLE = "TITLE";
    public static final String KEY_DESCRIPTION = "DESCRIPTION";
    public static final String KEY_ID = "ID";
    public static final String KEY_THEME = "THEME";
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        //обрабатывает запрос, получая информацию из строки поиска
        DatabaseTable databaseTable = new DatabaseTable(this);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Cursor cursor = databaseTable.getCityMatches(query, null);
            if (cursor != null) {   //если найдено совпадение
                TextView textView = (TextView) findViewById(R.id.textViewSearchResult);
                textView.setText("Найдено совпадение: Город " + cursor.getString(1)); //0 - id, 1 - name
                showResults(cursor.getInt(0));
            }
            else {  //если запрос был некорректен или неверен
                Toast.makeText(getApplicationContext(),
                        "No result", Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void showResults(final int cityId) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API.BASE_URL)
                .build();
        API service = restAdapter.create(API.class);
        service.getRoutes(cityId, new Callback<List<Route>>() {
            @Override
            public void success(List<Route> routes, Response response) {
                //В случае, если запрос к сервису был удачен, то происходит обработка
                if (routes.size() != 0) {   //если есть маршруты, если база пуста, то будет 0
                    ArrayList<HashMap<String, Object>> resultList =
                            new ArrayList<HashMap<String, Object>>();
                    HashMap<String, Object> hashMap;
                    for (int i=0; i < routes.size(); i++) {
                        //В цикле создается хэшкарта, в которую заносятся данные. Потом это всё добавляется
                        //в ArrayList и далее в simpleAdapter, и в конце в listview
                        hashMap = new HashMap<>();
                        hashMap.put(KEY_ID, routes.get(i).getRouteId());
                        hashMap.put(KEY_TITLE, routes.get(i).getRouteTitle());
                        hashMap.put(KEY_DESCRIPTION, routes.get(i).getRouteDescription());
                        hashMap.put(KEY_THEME, routes.get(i).getThemes(0));
//                        try {
//                            new DownloadImageTask().execute(routes.get(i).getImgUrl()).get();
//                        } catch (InterruptedException | ExecutionException e) {
//                            Log.e("Error", e.getMessage());
//                            e.printStackTrace();
//                        }
//                        new DownloadImageTask().execute(routes.get(i).getImgUrl());
//                        hashMap.put("ICON", resultBitmap);
//                        hashMap.put("ICON", routes.get(i).getImgUrl());
//                        hashMap.put("ICON", drawable = dowloadBitmap(routes.get(i).getImgUrl()));
//                        hashMap.put("ICON", drawable);
                        hashMap.put("ICON", R.drawable.hermitage);
                        resultList.add(hashMap);
                    }
                    SimpleAdapter simpleAdapter = new SimpleAdapter(getBaseContext(),
                            resultList, R.layout.route_fragment,
                            new String[]{KEY_TITLE, KEY_DESCRIPTION, KEY_THEME, "ICON"},
                            new int[] {R.id.textViewRouteTitle, R.id.textViewRouteDescription,
                                    R.id.textViewRouteThemes, R.id.imageViewRoutePicture});
                    //TODO:заменить на ListFragment
                    ListView listView = (ListView)findViewById(R.id.listView);
                    listView.setAdapter(simpleAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            Log.d("TAG", "position: " + position + " id: " + id);
                            HashMap<String, Object> itemHashMap = (HashMap<String, Object>)
                                    parent.getItemAtPosition(position);
//                            Log.d("TAG", "route id: " + itemHashMap.get("ID").toString());
                            Intent routeInfoIntent = new Intent(getApplicationContext(),
                                    RouteInfoActivity.class);
                            routeInfoIntent.putExtra(KEY_TITLE, itemHashMap.get(KEY_TITLE).toString());
                            routeInfoIntent.putExtra(KEY_ID, itemHashMap.get(KEY_ID).toString());
                            routeInfoIntent.putExtra(KEY_DESCRIPTION, itemHashMap.get(KEY_DESCRIPTION).toString());
                            routeInfoIntent.putExtra(KEY_THEME, itemHashMap.get(KEY_THEME).toString());
                            startActivity(routeInfoIntent);
                        }
                    });
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(),
                        "Something gonna wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Drawable> {
//        ImageView bmImage;

        public DownloadImageTask() {
//            this.bmImage = bmImage;
        }

        protected Drawable doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            Drawable testDraw = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
//                testDraw = Drawable.createFromStream(in, urldisplay);
                testDraw = new BitmapDrawable(getResources(), mIcon11);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
//            return mIcon11;
            return testDraw;
        }

        @Override
        protected void onPostExecute(Drawable result) {
//            bmImage.setImageBitmap(result);
            drawable = result;
        }
    }
}
