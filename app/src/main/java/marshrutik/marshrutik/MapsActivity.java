package marshrutik.marshrutik;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapsActivity extends Activity implements OnMapReadyCallback {
    //символизирует, вызвана ли активность для добавления, или для просмотра маршрута
    private boolean isForAddRoute;

    //координаты Эрмитажа
    private final static double LAT_HERM1 = 59.939832;
    private final static double LAT_HERM2 = 30.31456;
    //координаты ст.м. Адмиралтейская
    private final static double LAT_ADM1 = 59.93587;
    private final static double LAT_ADM2 = 30.315201;
    //координаты Рос. гос. музея
    private final static double LAT_RUSMUS1 = 59.93379058;
    private final static double LAT_RUSMUS2 = 30.3203398;

    private ArrayList<Marker> markerList;
    private ArrayList<Polyline> polylines;
    private TextView textView;
    //TODO: заменить на адекватный токен
    private static final String TEMP_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0NjA3NTY1MjE2Mzcs" +
                    "InVzZXJuYW1lIjoibG9naW4iLCJmaXJzdG5hbWUiOiJ1c2VyIiwibGFzdG" +
                    "5hbWUiOiJ1c2VyIn0.ajZfhgVvmDRnLjj-40lSjEIGVI2FSGQlvTfaOj7GkD0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (getIntent().getBooleanExtra("ADD", false)) {
            isForAddRoute = true;
        }
        else {
            isForAddRoute = false;
        }
        markerList = new ArrayList<>();
        polylines = new ArrayList<>();

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
    //классы, для отправки на сервер в виде {token:[], array_parts:[lat, lng, title, id, desc and etc.}
    public class RoutePartsForSend {
        private String token;
        private List<RouteParts> array_parts;
        RoutePartsForSend() {
            array_parts = new ArrayList<>();
        }

        public void setToken(String mToken) {
            this.token = mToken;        }
        }
    private class RouteParts {
        private double routepart_start_latitude;
        private double routepart_start_longitude;
        private double routepart_end_latitude;
        private double routepart_end_longitude;
        private String routepart_description;
        private int route_id;
        private int part_id;
        private String routepart_title;

        public void setStartLat(double lat) { this.routepart_start_latitude = lat; }
        public void setStartLong(double lng) { this.routepart_start_longitude = lng; }
        public void setEndLat(double lat) {this.routepart_end_latitude = lat; }
        public void setEndLong(double lng) {this.routepart_end_longitude = lng; }
        public void setRouteId(int id) { this.route_id = id; }
        public void setPartId(int id) {this.part_id = id; }
        public void setRoutePartDesc(String desc) {this.routepart_description = desc; }
        public void setRoutepartTitle(String title) {this.routepart_title = title; }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        if (isForAddRoute) {    //если активность вызвана для добавления маршрута\
            Button sendButton = (Button)findViewById(R.id.buttonSend);
            sendButton.setVisibility(View.VISIBLE);
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RoutePartsForSend routes = new RoutePartsForSend();
                    routes.setToken(TEMP_TOKEN);
                    for (int i = 0; i < markerList.size()-1; i++) {
                        Log.d("TAG", "points: " + markerList.get(i).getPosition() + " ");
                        RouteParts routeParts = new RouteParts();
                        routeParts.setStartLat(markerList.get(i).getPosition().latitude);
                        routeParts.setStartLong(markerList.get(i).getPosition().longitude);
                        routeParts.setEndLat(markerList.get(i + 1).getPosition().latitude);
                        routeParts.setEndLong(markerList.get(i + 1).getPosition().longitude);
                        routeParts.setRouteId(16);
                        routeParts.setPartId(i+1);
                        routeParts.setRoutepartTitle("Default Title");
                        routeParts.setRoutePartDesc("Default Description");
                        routes.array_parts.add(routeParts);
                    }
                    RestAdapter restAdapter = new RestAdapter.Builder()
                            .setEndpoint(API.BASE_URL)
                            .setLogLevel(RestAdapter.LogLevel.FULL) //TODO: убрать full level
                            .build();
                    API service = restAdapter.create(API.class);
                    service.sendRouteParts(routes, new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                                        Toast.makeText(getApplicationContext(),
                                                "YEEES", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                                        Toast.makeText(getApplicationContext(),
                                                "NOOO", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(LAT_HERM1, LAT_HERM2), 14));

            map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    Marker marker = map.addMarker(new MarkerOptions().position(latLng)
                            .title(getResources().getString(R.string.marker_default_title))
                            .snippet(getResources().getString(R.string.marker_default_desc)));
                    marker.setDraggable(true);
                    markerList.add(marker);
                    updateRoute(map);
                }
            });
            //для динамического обновления полилиний маршрута
            map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    updateRoute(map);
                }
            });
            //свой InfoWindow для того, чтобы потом в нем динамически изменять title и snippet of marker
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View view = getLayoutInflater().inflate(R.layout.info_window, null);
                    textView = (TextView)view.findViewById(R.id.tvMarkerTitle);
                    textView.setText("getInfoContents");
                    return view;
                }
            });

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Toast.makeText(getApplicationContext(),
                            "On Info Window clicked", Toast.LENGTH_SHORT).show();
                    textView = (TextView)findViewById(R.id.tvMarkerTitle);
                    textView.setText("NEW Text here");
                }
            });
        }
        else {      //если для отображения маршрута
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
            Log.d("TAG", "origin " + origin);

            makeRoute(origin, destination, map);
            origin = destination;
            destination = String.valueOf(LAT_RUSMUS1) + "," + String.valueOf(LAT_RUSMUS2);
            makeRoute(origin, destination, map);
        }
    }
    private void updateRoute(GoogleMap map) {
//        map.clear();
        for (int i = 0; i < polylines.size(); i++) {
            polylines.get(i).remove();
        }
        if (markerList.size() > 1) {
            for (int i = 0;  i < markerList.size()-1; i++) {
                makeRoute(latToString(markerList.get(i).getPosition()),
                        latToString(markerList.get(i+1).getPosition()),
                        map);
            }
        }
    }
    private String latToString(LatLng latLng) {
        return String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);
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
                polylines.add(map.addPolyline(line));
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
