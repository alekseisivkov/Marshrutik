package marshrutik.marshrutik;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class AddRouteActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);
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
        EditText countryName = (EditText)findViewById(R.id.editTextCountry);
        EditText cityName = (EditText)findViewById(R.id.editTextCity);
        String country, city;
        Intent mapsIntent = new Intent(getApplicationContext(), MapsActivity.class);
        country = countryName.getText().toString();
        city = cityName.getText().toString();
        mapsIntent.putExtra("country", country);
        mapsIntent.putExtra("city", city);
        startActivity(mapsIntent);
    }
}
