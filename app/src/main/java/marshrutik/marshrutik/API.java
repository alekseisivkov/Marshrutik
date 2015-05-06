package marshrutik.marshrutik;

import java.util.List;


import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.Callback;
import retrofit.http.Path;
import retrofit.http.Query;

public interface API {
    public static final String BASE_URL = "https://marshrutik.herokuapp.com/api";
    public static final String BASE_GOOGLEAPI_URL = "https://maps.googleapis.com";

    @GET("/cities/?country_id=1")
    public void getCity(Callback<List<City>> callback);

    @GET("/cities/{city_id}/routes")
    public void getRoutes(@Path("city_id") int cityId, Callback<List<Route>> callback);

    @GET("/routes/{route_id}")
    public void getRouteInfo(@Path("{route_id") int routeId, Callback<Route> callback);

    @GET("/maps/api/directions/json")
    public void getGoogleRoute(
            @Query(value = "origin", encodeValue = false) String position,
            @Query(value = "destination", encodeValue = false) String destination,
            @Query("sensor") boolean sensor,
            @Query("language") String language,
            @Query("mode") String mode,
            Callback<GoogleRoute> callback);

    }
