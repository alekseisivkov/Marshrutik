package marshrutik.marshrutik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class AddRouteActivity extends ActionBarActivity {
    private EditText edTeCityName;
    private int cityId;
    private EditText edTeRouteTitle;
    private EditText edTeRouteDesc;

    public static final String KEY_CITY_NAME = "CITY_NAME";
    private SharedPreferences tokenPrefs;

    //TODO: заменить на адекватный токен
    private static final String TEMP_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0NjA3NTY1MjE2Mzcs" +
                    "InVzZXJuYW1lIjoibG9naW4iLCJmaXJzdG5hbWUiOiJ1c2VyIiwibGFzdG" +
                    "5hbWUiOiJ1c2VyIn0.ajZfhgVvmDRnLjj-40lSjEIGVI2FSGQlvTfaOj7GkD0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);
        edTeCityName = (EditText)findViewById(R.id.editTextCity);
        cityId = -1;
        final Button nextButton = (Button)findViewById(R.id.buttonNext);
        edTeRouteTitle = (EditText)findViewById(R.id.editTextRouteTitle);
        edTeRouteDesc = (EditText)findViewById(R.id.editTextRouteDecs);

        tokenPrefs = getSharedPreferences(LoginActivity.TOKEN_FILENAME, MODE_PRIVATE);

        nextButton.setEnabled(false); //без ввода правильного города, дальше уйти нельзя
        edTeRouteTitle.setEnabled(false);
        edTeRouteDesc.setEnabled(false);

        //для того, чтобы перейти далее можно было если город существует в базе
        edTeCityName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nextButton.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                DatabaseTable databaseTable = new DatabaseTable(getApplicationContext());
                Cursor cursor = databaseTable.getCityMatches(editable.toString(), null);
                if (cursor != null) {
                    nextButton.setEnabled(true);
                    cityId = cursor.getInt(0); //0-id, 1-cityName
                    edTeRouteTitle.setEnabled(true);
                    edTeRouteDesc.setEnabled(true);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_route, menu);
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

    public void onNextButtonClicked(View view) {
        Log.d("TAG", "city name " + edTeCityName.getText().toString() + " city id " + cityId);
        Log.d("TAG", "route title " + edTeRouteTitle.getText().toString() + " route desc " + edTeRouteDesc.getText());
        if (edTeRouteTitle.getText().length() != 0 || edTeRouteDesc.getText().length() != 0) {

            Log.d("tag", "token from add route: " + tokenPrefs.getString(LoginActivity.TOKEN, "token not found"));
            String userToken = tokenPrefs.getString(LoginActivity.TOKEN, "-1");
            if (userToken.equals("-1")) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                Toast.makeText(getApplicationContext(),
                        R.string.hint_login_to_add_route, Toast.LENGTH_SHORT).show();
                startActivity(loginIntent);
            } else {
                RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API.BASE_URL)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
                API service = restAdapter.create(API.class);
                service.sendRouteInfo(userToken,
                        edTeRouteTitle.getText().toString(),
                        cityId,
                        edTeRouteDesc.getText().toString(),
                        1,      //только пешком
                        new Callback<RouteIdAnswer>() {
                            @Override
                            public void success(RouteIdAnswer routeIdAnswer, Response response) {
                                Toast.makeText(getApplicationContext(),
                                        "YESS " + routeIdAnswer.getRouteId(), Toast.LENGTH_SHORT).show();

                                Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
                                mapsIntent.putExtra(KEY_CITY_NAME, edTeCityName.getText().toString());
                                mapsIntent.putExtra("ADD", true);
                                //передачи сгенерированного сервером ID маршрута в активность карты
                                mapsIntent.putExtra(SearchResultActivity.KEY_ID, routeIdAnswer.getRouteId());
                                startActivity(mapsIntent);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getApplicationContext(),
                                        "Sorry, you are failed", Toast.LENGTH_SHORT).show();
                                Log.e("RETROFIT ERROR", error.getMessage());
                            }
                        });
            }

        }
    }
}
