package marshrutik.marshrutik;

import android.content.Intent;
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
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API.BASE_URL)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            API service = restAdapter.create(API.class);
            service.sendRouteInfo(TEMP_TOKEN,
                    edTeRouteTitle.getText().toString(),
                    cityId,
                    edTeRouteDesc.getText().toString(),
                    1,
                    new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            Toast.makeText(getApplicationContext(),
                                    "YESS", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(getApplicationContext(),
                                    "Sorry, you are failed", Toast.LENGTH_SHORT).show();

                        }
                    });
        }
        Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
        mapsIntent.putExtra("city", edTeCityName.getText().toString());
        mapsIntent.putExtra("ADD", true);
        startActivity(mapsIntent);
    }
}
