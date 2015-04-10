package marshrutik.marshrutik;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SearchResultActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
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
                showResults(cursor);
            }
            else {  //если запрос был некорректен или неверен
                Toast.makeText(getApplicationContext(),
                        "No result", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showResults(final Cursor cursor) {
        TextView textView = (TextView) findViewById(R.id.textViewSearchResult);
        textView.setText("Найдено совпадение: Город " + cursor.getString(1)); //0 - id, 1 - name

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API.BASE_URL)
                .build();
        API service = restAdapter.create(API.class);
        service.getRoutes(Integer.valueOf(cursor.getString(0)), new Callback<List<Route>>() {
            @Override
            public void success(List<Route> routes, Response response) {
                if (routes.size() != 0) {   //если есть маршруты, если база пуста, то будет 0
                    for (int i=0; i < routes.size(); i++) {
                        RouteFragment routeFragment = new RouteFragment();
                        Bundle args = new Bundle();
                        args.putString("description", routes.get(i).getRouteDescription());
                        args.putString("title", routes.get(i).getRouteTitle());
                        routeFragment.setArguments(args);

                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.fragmentContainer, routeFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(),
                        "Something gonna wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
