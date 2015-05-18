package marshrutik.marshrutik;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class RouteInfoActivity extends ActionBarActivity {
    public static final String KEY_START_ROUTEPART = "START_ROUTEPART";
    public static final String KEY_END_ROUTEPART = "END_ROUTEPART";
    public static final String KEY_TITLE_ROUTEPART = "TITLE_ROUTEPART";
    public static final String KEY_DESC_ROUTEPART = "DESC_ROUTEPART";
    public static final String KEY_QUANTITY_ROUTEPART = "QUATITY_ROUTEPART";
    public static final String KEY_ROUTEPART_ID = "ROUTEPART_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_info);

        TextView textViewTitle = (TextView)findViewById(R.id.textViewTitle);
        TextView textViewDescription = (TextView)findViewById(R.id.textViewDescription);

        final Intent handleIntent = getIntent();
        textViewTitle.setText(handleIntent.getStringExtra(SearchResultActivity.KEY_TITLE));
        textViewDescription.setText(handleIntent.getStringExtra(SearchResultActivity.KEY_DESCRIPTION));

        Button buttonShowMap = (Button)findViewById(R.id.buttonShowMap);
        buttonShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint(API.BASE_URL)
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .build();
                API service = restAdapter.create(API.class);
                service.getRouteInfo(getIntent().getIntExtra(SearchResultActivity.KEY_ID, -1),
                        new Callback<List<Route>>() {
                            @Override
                            public void success(List<Route> route, Response response) {
                                Log.d("TAG", "success, yeaaaah!!");
                                Intent showMapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                                showMapIntent.putExtra(KEY_QUANTITY_ROUTEPART, route.get(0).getRoutePartsLength());
                                showMapIntent.putExtra(SearchResultActivity.KEY_UPDATE,
                                        handleIntent.getBooleanExtra(SearchResultActivity.KEY_UPDATE, false));
                                showMapIntent.putExtra(SearchResultActivity.KEY_ID,
                                        handleIntent.getIntExtra(SearchResultActivity.KEY_ID, -1));
                                for (int i = 0; i < route.get(0).getRoutePartsLength(); i++) {
                                    Log.d("TAG", "Start latlng: " + route.get(0).getStartRoutepart(i));
                                    Log.d("TAG", " End latlng: " + route.get(0).getEndRoutepart(i));
                                    Log.d("TAG", " Part id: " + route.get(0).getRoutepartId(i));
                                    Log.d("TAG", " Part title: " + route.get(0).getRoutepartTitle(i));
                                    Log.d("TAG", " Part desc: " + route.get(0).getRoutepartDesc(i));

                                    showMapIntent.putExtra(KEY_START_ROUTEPART + i, route.get(0).getStartRoutepart(i));
                                    showMapIntent.putExtra(KEY_END_ROUTEPART + i, route.get(0).getEndRoutepart(i));
                                    showMapIntent.putExtra(KEY_TITLE_ROUTEPART + i, route.get(0).getRoutepartTitle(i));
                                    showMapIntent.putExtra(KEY_DESC_ROUTEPART + i, route.get(0).getRoutepartDesc(i));
                                    showMapIntent.putExtra(KEY_ROUTEPART_ID + i, route.get(0).getRpId(i));
                                }
                                startActivity(showMapIntent);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d("TAG", "fail, bro");
                                Log.e("RETROFIT ERROR", error.getMessage());

                            }
                        });

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_info, menu);
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
}
