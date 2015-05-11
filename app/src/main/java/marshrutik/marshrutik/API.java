package marshrutik.marshrutik;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;

public interface API {
    String BASE_URL = "https://marshrutik.herokuapp.com/api";
    String BASE_GOOGLEAPI_URL = "https://maps.googleapis.com";

    @GET("/cities/?country_id=1")
    void getCity(Callback<List<City>> callback);

    @GET("/cities/{city_id}/routes")
    void getRoutes(@Path("city_id") int cityId, Callback<List<Route>> callback);

    @GET("/routes/{route_id}")
    void getRouteInfo(@Path("{route_id") int routeId, Callback<Route> callback);

    @GET("/maps/api/directions/json")
    void getGoogleRoute(
            @Query(value = "origin", encodeValue = false) String position,
            @Query(value = "destination", encodeValue = false) String destination,
            @Query("sensor") boolean sensor,
            @Query("language") String language,
            @Query("mode") String mode,
            Callback<GoogleRoute> callback);

    @FormUrlEncoded
    @POST("/routes/")
    void sendRouteInfo(@Field("token") String token,
                       @Field("route_name") String routeTitle,
                       @Field("city_id") int cityId,
                       @Field("route_description") String routeDesc,
                       @Field("typeofmovement_id") int typeOfMove,
                       Callback<Response> callback);

    @POST("/routeparts/")
    void sendRouteParts(@Body MapsActivity.RoutePartsForSend routes,
                        Callback<Response> callback);
    }
