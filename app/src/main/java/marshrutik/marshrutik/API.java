package marshrutik.marshrutik;

import java.util.List;


import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.Callback;
import retrofit.http.Path;
import retrofit.http.Query;

public interface API {
    public static final String BASE_URL = "https://marshrutik.herokuapp.com/api";

    @GET("/cities/?country_id=1")
    public void getCity(Callback<List<City>> callback);

    @GET("/cities/{city_id}/routes")
    public void getRoutes(@Path("city_id") int cityId, Callback<List<Route>> callback);
    }
