package marshrutik.marshrutik;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //Ассоциируем serachable конфигурацию с SearchView. Для поиска
        SearchManager searchManager =
                (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView)menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); //это чтобы после первого нажатия сразу показывалась срока поиска
        searchView.requestFocus();  //после открытия окошка ввода фокус был помещен в него
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(getApplicationContext(),
                        "submit query " + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(getApplicationContext(),
                        "on Query text changed" + newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        //TODO:сделать так, что бы открывалась клавиатура
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getApplicationContext(),
                        SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_add_route:
                addRoute();
                return true;
            case R.id.action_search:
//                searchRoute();
                return true;
            case R.id.action_login:
                Intent loginIntent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(loginIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    private void searchRoute() {
//        //Поиск в ActionBar, в который вводится название города и вызывается активность
//        //с выдачей результатов по городу, а так же возможностью уточнить запрос
//        Toast.makeText(getApplicationContext(),
//                "Вызван поиск", Toast.LENGTH_SHORT).show();
//    }

    private void addRoute() {
        //функция добавления маршурута, вызывается из Action Bar
        Toast.makeText(getApplicationContext(),
                "Вызвано добавление маршрута", Toast.LENGTH_SHORT).show();
        Intent addRouteIntent = new Intent(getApplicationContext(), AddRouteActivity.class);
        startActivity(addRouteIntent);

    }
}
