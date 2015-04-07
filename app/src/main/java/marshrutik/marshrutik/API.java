package marshrutik.marshrutik;

import java.util.List;


import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.Callback;
import retrofit.http.Path;
import retrofit.http.Query;

public interface API {
    @GET("/cities/?country_id=1")
    public void getCity(Callback<List<City>> callback);
    }
