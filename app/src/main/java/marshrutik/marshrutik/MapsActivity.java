package marshrutik.marshrutik;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapsActivity extends Activity implements OnMapReadyCallback {
    //координаты Эрмитажа
    private final static double LAT_HERM1 = 59.939832;
    private final static double LAT_HERM2 = 30.31456;
    //координаты ст.м. Адмиралтейская
    private final static double LAT_ADM1 = 59.93587;
    private final static double LAT_ADM2 = 30.315201;
    //координаты Рос. гос. музея
    private final static double LAT_RUSMUS1 = 59.93379058;
    private final static double LAT_RUSMUS2 = 30.3203398;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        MapFragment map = (MapFragment)getFragmentManager()
                .findFragmentById(R.id.map);
        map.getMapAsync(this);  //после этого момента всё уходит в Callback onMapReady
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
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

    @Override
    public void onMapReady(final GoogleMap map) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(LAT_HERM1, LAT_HERM2), 14));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(LAT_HERM1, LAT_HERM2))
                .title(getResources().getString(R.string.hermitage_title))
                .snippet(getResources().getString(R.string.hermitage_desc)));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(LAT_ADM1, LAT_ADM2))
                .title(getResources().getString(R.string.admiralt_title))
                .snippet(getResources().getString(R.string.admiralt_desc)));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(LAT_RUSMUS1, LAT_RUSMUS2))
                .title(getResources().getString(R.string.rusmus_title))
                .snippet(getResources().getString(R.string.rusmus_desc)));

        String origin = String.valueOf(LAT_HERM1) + "," + String.valueOf(LAT_HERM2);
        String destination = String.valueOf(LAT_ADM1) + "," + String.valueOf(LAT_ADM2);

        makeRoute(origin, destination, map);
        origin = destination;
        destination = String.valueOf(LAT_RUSMUS1) + "," + String.valueOf(LAT_RUSMUS2);
        makeRoute(origin, destination, map);


    }
    //функция, которая рисует маршрут по данных гугла между двумя точка старта и финиша
    private void makeRoute(String start, String finish, final GoogleMap map) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API.BASE_GOOGLEAPI_URL)
                .build();
        API gRouteService = restAdapter.create(API.class);
        gRouteService.getGoogleRoute(start, finish, true, "ru", "walking" , new Callback<GoogleRoute>() {
            @Override
            public void success(GoogleRoute googleRoute, Response response) {
                List<LatLng> mPoints = PolyUtil.decode(googleRoute.getPoints());
                PolylineOptions line = new PolylineOptions();
                line.width(15.0f).color(getResources().getColor(R.color.route_line_color));
//                нужно для масштабирования карты, пока не реализовавыл
//                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (int i = 0; i < mPoints.size(); i++) {
                    line.add(mPoints.get(i));
                }
                map.addPolyline(line);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(),
                        "Вы не должны это видеть", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(),
                        "А вообще проблемы у Гугла и его маршрутов", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
