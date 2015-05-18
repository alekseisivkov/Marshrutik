package marshrutik.marshrutik;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    private Button optimizeRoute;
    private SharedPreferences tokenPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenPrefs = getSharedPreferences(LoginActivity.TOKEN_FILENAME, MODE_PRIVATE);
        optimizeRoute = (Button)findViewById(R.id.button);
        optimizeRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "Функция в разработке, скоро вы сможете ее использовать", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
//        checkLogin(menu);
//        menu.getItem(1).setVisible(false);
        String token = tokenPrefs.getString(LoginActivity.TOKEN, "-1");
        if (!token.equals("-1")) {
            menu.findItem(R.id.action_register).setVisible(false);
        }

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
//                Toast.makeText(getApplicationContext(),
//                        "submit query " + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(getApplicationContext(),
//                        "on Query text changed" + newText, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        //TODO:сделать так, что бы открывалась клавиатура
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final String token = tokenPrefs.getString(LoginActivity.TOKEN, "-1");
        if (!token.equals("-1")) {
            //если вход выполнен
            menu.findItem(R.id.action_register).setVisible(false);
            MenuItem loginItem = menu.findItem(R.id.action_login);
            loginItem.setTitle(R.string.action_logout);
            menu.findItem(R.id.action_profile).setVisible(true);
            loginItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    SharedPreferences.Editor editor = tokenPrefs.edit();
                    editor.remove(LoginActivity.TOKEN); //удаляем токен и данные о том, что вход выполнен
                    editor.apply();
                    return true;
                }
            });
        }
        else {
            //действия, для появления пункта регистрации и входа и для убирания профиля
            menu.findItem(R.id.action_register).setVisible(true);
            menu.findItem(R.id.action_profile).setVisible(false);
            MenuItem loginItem = menu.findItem(R.id.action_login);
            loginItem.setTitle(R.string.action_login);
            loginItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //TODO: вынести в отдельную функцию
                    Intent loginIntent = new Intent(getApplicationContext(),
                            LoginActivity.class);
                    startActivity(loginIntent);
                    return true;
                }
            });
        }
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.action_register:
                Intent registerIntent = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(registerIntent);
                return true;
            case R.id.action_profile:
                showProfile();
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
    private void showProfile() {
        Intent userRouteSearchIntent = new Intent(getApplicationContext(), SearchResultActivity.class);
        startActivity(userRouteSearchIntent);
    }
    private void addRoute() {
        //функция добавления маршурута, вызывается из Action Bar
        Intent addRouteIntent = new Intent(getApplicationContext(), AddRouteActivity.class);
        startActivity(addRouteIntent);
    }
}
