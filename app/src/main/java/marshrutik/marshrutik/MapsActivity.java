package marshrutik.marshrutik;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
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
    //если активность вызвана для изменения маршрута
    private boolean isForRouteUpdate;
    //если для отображения оптимального маршрута по достопримечательностям
    private boolean isForOptimizeRoute;
    //по умолчанию, карта центрируется на Эрмитаже в Петербург
    private final static double LAT_DEFAULT = 59.939832;
    private final static double LNG_DEFAULT = 30.31456;

    private ArrayList<Marker> markerList;
    private ArrayList<Polyline> polylines;
    private int routeId;
    private LatLng centerMap;
    //настройки, хранящие токен пользователя
    private SharedPreferences tokenPrefs;

    //TODO: заменить на адекватный токен
    private static final String TEMP_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0NjA3NTY1MjE2Mzcs" +
                    "InVzZXJuYW1lIjoibG9naW4iLCJmaXJzdG5hbWUiOiJ1c2VyIiwibGFzdG" +
                    "5hbWUiOiJ1c2VyIn0.ajZfhgVvmDRnLjj-40lSjEIGVI2FSGQlvTfaOj7GkD0";

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
        private int routepart_id;

        public void setStartLat(double lat) { this.routepart_start_latitude = lat; }
        public void setStartLong(double lng) { this.routepart_start_longitude = lng; }
        public void setEndLat(double lat) {this.routepart_end_latitude = lat; }
        public void setEndLong(double lng) {this.routepart_end_longitude = lng; }
        public void setRouteId(int id) { this.route_id = id; }
        public void setPartId(int id) {this.part_id = id; }
        public void setRoutePartDesc(String desc) {this.routepart_description = desc; }
        public void setRoutepartTitle(String title) {this.routepart_title = title; }
        public void setRoutepartId(int id) { this.routepart_id = id; }
    }
    //класс для получения информации от Google о координатах города. Для центрирования карты
    public class CityGeoCoordinates {
        public List<Results> results;

        public LatLng getLatLng() {
            return new LatLng(this.results.get(0).geometry.location.lat, results.get(0).geometry.location.lng);
        }
        class Results {
            Geometry geometry;
        }
        class Geometry {
            LocationN location;
        }
        class LocationN {
            double lat;
            double lng;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tokenPrefs = getSharedPreferences(LoginActivity.TOKEN_FILENAME, MODE_PRIVATE);

        centerMap = new LatLng(LAT_DEFAULT, LNG_DEFAULT);

        isForAddRoute = getIntent().getBooleanExtra("ADD", false);  //если карта для отображения маршрута - true, иначе - false
        isForRouteUpdate = getIntent().getBooleanExtra(SearchResultActivity.KEY_UPDATE, false);
        isForOptimizeRoute = getIntent().getBooleanExtra(MainActivity.KEY_OPITIMIZED_ROUTE, false);

        markerList = new ArrayList<>();
        polylines = new ArrayList<>();
        routeId = getIntent().getIntExtra(SearchResultActivity.KEY_ID, -1);

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
        //TODO: заменить на switch/case, и сделать одну int переменную идентификатор.
        if (isForOptimizeRoute) {
            drawRouteOnMap(map);
        }
        if (isForAddRoute || isForRouteUpdate) {    //если активность вызвана для добавления маршрута
            if (isForRouteUpdate) {
                drawRouteOnMap(map);
            }
            makeMapEditable(map);
        }
        //isForAddRoute = false
        else {      //если для отображения маршрута
            drawRouteOnMap(map);
        }
    }
    private void updateRoute(GoogleMap map) {
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
    private void drawRouteOnMap(final GoogleMap map){
        if (isForOptimizeRoute) {
            Intent prevIntent = getIntent();
            final String origin = prevIntent.getStringExtra(MainActivity.KEY_ORIGIN);
            final String destination = prevIntent.getStringExtra(MainActivity.KEY_DESTINATION);
            final String waypoint = prevIntent.getStringExtra(MainActivity.KEY_WAYPOINTS);
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(API.BASE_GOOGLEAPI_URL)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .build();
            API service = restAdapter.create(API.class);
            service.getGoogleRoute(origin, destination, waypoint, true, "ru", "walking", new Callback<GoogleRoute>() {
                @Override
                public void success(GoogleRoute googleRoute, Response response) {
                    Toast.makeText(getApplicationContext(), "YESS", Toast.LENGTH_SHORT).show();
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(StringToLatLng(origin), 14));

                    map.addMarker(new MarkerOptions().position(StringToLatLng(origin)));
                    map.addMarker(new MarkerOptions().position(StringToLatLng(destination)));

                    String waypoints[] = waypoint.split("\\|");
                    for (int i = 1; i < waypoints.length; i++) { // i=0 - это optimize:true
                        Log.d("TAG", "waypoint: " + waypoints[i]);
                        map.addMarker(new MarkerOptions().position(StringToLatLng(waypoints[i])));
                    }
                    Log.d("TAG", "waypoint lenght: " + waypoints.length);
                    for (int i = 0; i < googleRoute.getWaypointOrder().length; i++) {
                        Log.d("TAG", "order: " + googleRoute.getWaypointOrder()[i]);
                    }
                    Log.d("TAG", "order lenght: " + googleRoute.getWaypointOrder().length);
                    makeRoute(origin, waypoints[Integer.parseInt(googleRoute.getWaypointOrder()[0])+1], map);
                    for (int i = 1; i < waypoints.length; i++) {
                        if (i == waypoints.length - 1) {
                            makeRoute(waypoints[Integer.parseInt(googleRoute.getWaypointOrder()[i-1]) + 1], destination, map);
                        }
                        else {
                            makeRoute(waypoints[Integer.parseInt(googleRoute.getWaypointOrder()[i - 1]) + 1],
                                    waypoints[Integer.parseInt(googleRoute.getWaypointOrder()[i]) + 1], map);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "NOO", Toast.LENGTH_SHORT).show();
                    Log.e("RETROFIT", error.getMessage());
                }
            });
        }
        else {
            //TODO: добавить проверку на наличие данных
            int size = getIntent().getIntExtra(RouteInfoActivity.KEY_QUANTITY_ROUTEPART, -1);
            Intent prevIntent = getIntent();
            if (size != -1) {
                centerMap = StringToLatLng(prevIntent.getStringExtra(RouteInfoActivity.KEY_START_ROUTEPART + 0));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerMap, 14));
                for (int i = 0; i < size; i++) {
                    Marker markerEnd = null;
                    if (i > 0) {
                        makeRoute(prevIntent.getStringExtra(RouteInfoActivity.KEY_START_ROUTEPART + i),
                                prevIntent.getStringExtra(RouteInfoActivity.KEY_START_ROUTEPART + (i - 1)), map);
                    }
                    if (i == size - 1) {
                        makeRoute(prevIntent.getStringExtra(RouteInfoActivity.KEY_START_ROUTEPART + i),
                                prevIntent.getStringExtra(RouteInfoActivity.KEY_END_ROUTEPART + i), map);
                        markerEnd = map.addMarker(new MarkerOptions()
                                .position(StringToLatLng(
                                        prevIntent.getStringExtra(RouteInfoActivity.KEY_END_ROUTEPART + i)))
                                .title(getResources().getString(R.string.marker_end_route_title))
                                .snippet(getResources().getString(R.string.marker_end_route_desc)));
                    }
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(StringToLatLng(
                                    prevIntent.getStringExtra(RouteInfoActivity.KEY_START_ROUTEPART + i)))
                            .title(prevIntent.getStringExtra(RouteInfoActivity.KEY_TITLE_ROUTEPART + i))
                            .snippet(prevIntent.getStringExtra(RouteInfoActivity.KEY_DESC_ROUTEPART + i)));
                    if (isForRouteUpdate) {
                        markerList.add(marker);
                        if (i == size - 1) {
                            markerList.add(markerEnd);
                            markerEnd.setDraggable(true);
                        }
                        marker.setDraggable(true);
                        updateRoute(map);
                    }

                }
            }
        }
    }
    //функция добавления возможностей для редактирования маркеров, названий и их перетаскивания
    private void makeMapEditable(final GoogleMap map) {
        //перемещение карты в центр города
        if (isForAddRoute) moveCameraOnMap(map);   //если для добавление, то так, если
        //кнопка отправки маршрута на сервер
        Button sendButton = (Button)findViewById(R.id.buttonSend);
        sendButton.setVisibility(View.VISIBLE);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RoutePartsForSend routes = new RoutePartsForSend();     //TODO: добавить AlertDialog подтверждения отправки маршрута
                String userToken = tokenPrefs.getString(LoginActivity.TOKEN, "-1");
                if (userToken.equals("-1")) {
                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    Toast.makeText(getApplicationContext(),
                            R.string.hint_login_to_add_route, Toast.LENGTH_SHORT).show();
                    startActivity(loginIntent);
                } else {
                    routes.setToken(userToken);
                    for (int i = 0; i < markerList.size() - 1; i++) {
                        Log.d("TAG", "points: " + markerList.get(i).getPosition() + " ");
                        final RouteParts routeParts = new RouteParts();
                        routeParts.setStartLat(markerList.get(i).getPosition().latitude);
                        routeParts.setStartLong(markerList.get(i).getPosition().longitude);
                        routeParts.setEndLat(markerList.get(i + 1).getPosition().latitude);
                        routeParts.setEndLong(markerList.get(i + 1).getPosition().longitude);
                        if (isForRouteUpdate) {
                            routeParts.setRoutepartId(getIntent().getIntExtra(RouteInfoActivity.KEY_ROUTEPART_ID + i, -1));
                        }
                        if (routeId != -1) {
                            routeParts.setRouteId(routeId);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    R.string.error_routeId_not_found, Toast.LENGTH_SHORT).show();
                        }
                        routeParts.setPartId(i + 1);
                        routeParts.setRoutepartTitle(markerList.get(i).getTitle());
                        routeParts.setRoutePartDesc(markerList.get(i).getSnippet());
                        routes.array_parts.add(routeParts);
//                    //так как у Севы сделаны PUT только по одной части, отсылаем много запросов и удаляем из массива отосланные
//                    if (isForRouteUpdate) {
//                        RestAdapter restAdapter = new RestAdapter.Builder()
//                                .setEndpoint(API.BASE_URL)
//                                .setLogLevel(RestAdapter.LogLevel.FULL)
//                                .build();
//                        API service = restAdapter.create(API.class);
//                        service.updateRoutepart(routes, new Callback<Response>() {
//                            @Override
//                            public void success(Response response, Response response2) {
//                                Toast.makeText(getApplicationContext(),
//                                        "sucess", Toast.LENGTH_SHORT).show();
//                                routes.array_parts.remove(routeParts);
//                            }
//
//                            @Override
//                            public void failure(RetrofitError error) {
//                                Toast.makeText(getApplicationContext(),
//                                        "Something gonna wrong", Toast.LENGTH_SHORT).show();
//                                Log.e("RETROFIT", error.getMessage());
//                            }
//                        });
//
//                    }

                    }

                    RestAdapter restAdapter = new RestAdapter.Builder()
                            .setEndpoint(API.BASE_URL)
                            .setLogLevel(RestAdapter.LogLevel.FULL) //TODO: убрать full level
                            .build();
                    API service = restAdapter.create(API.class);
                    if (isForAddRoute) {
                        service.sendRouteParts(routes, new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.hint_send_route_success, Toast.LENGTH_LONG).show();
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getApplicationContext(),
                                        "NOOO", Toast.LENGTH_SHORT).show();
                                Log.e("RETROFIT", error.getMessage());
                            }
                        });
                    } else if (isForRouteUpdate) {
                        service.updateRoutepart(routes, new Callback<Response>() {
                            @Override
                            public void success(Response response, Response response2) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.hint_route_update_success, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getApplicationContext(),
                                        "Something gonna wrong", Toast.LENGTH_SHORT).show();
                                Log.e("RETROFIT", error.getMessage());
                            }
                        });
                    }
                }
            }
        });

        if (isForAddRoute) {
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
        }
        //для динамического обновления полилиний маршрута
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
//                    updateRoute(map);     пока не включал, не уверен, что хорошо скажется на производительности
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                updateRoute(map);
            }
        });

//            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//                @Override
//                public View getInfoWindow(Marker marker) {
//                    return null;
//                }
//
//                @Override
//                public View getInfoContents(Marker marker) {
//                    return getLayoutInflater().inflate(R.layout.info_window, null);
//                }
//            });

        //свой InfoWindow для того, чтобы потом в нем динамически изменять title и snippet of marker
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                LayoutInflater factory = LayoutInflater.from(MapsActivity.this);
                final View editTextView = factory.inflate(R.layout.alert_two_edittext, null);

                final EditText editTextTitle = (EditText)editTextView.findViewById(R.id.editTextTitleRoute);
                final EditText editTextDesc = (EditText)editTextView.findViewById(R.id.editTextDescRoute);
                //TODO: сделать, чтобы при появлении диалога выскакивала клавиатура
                AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(R.string.hint_alert_dialog_title)
                        .setView(editTextView)
                        .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                marker.setTitle(editTextTitle.getText().toString());
                                marker.setSnippet(editTextDesc.getText().toString());
                                marker.hideInfoWindow();
                                marker.showInfoWindow();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.button_cancel, null)
                        .create();
                alertDialog.show();
            }
        });
    }

    private void moveCameraOnMap(final GoogleMap map) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API.BASE_GOOGLEAPI_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        API service = restAdapter.create(API.class);
        service.getCityGeoCoord(getIntent().getStringExtra(AddRouteActivity.KEY_CITY_NAME),
                new Callback<CityGeoCoordinates>() {
                    @Override
                    public void success(CityGeoCoordinates cityGeoCoordinates, Response response) {
                        Toast.makeText(getApplicationContext(),
                                "YESS", Toast.LENGTH_SHORT).show();
                        centerMap = cityGeoCoordinates.getLatLng();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                centerMap, 12));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getApplicationContext(),
                                "NOO", Toast.LENGTH_SHORT).show();
                        Log.e("RETROFIT", error.getMessage());
                    }
                });
    }

    //function to convert latitude\longtitude to String
    private String latToString(LatLng latLng) {
        return String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);
    }
    private LatLng StringToLatLng(String coords) {
        String[] latlngs = coords.split(",");
        return new LatLng(Double.parseDouble(latlngs[0]), Double.parseDouble(latlngs[1]));
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
