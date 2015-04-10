package marshrutik.marshrutik;

import com.google.gson.annotations.SerializedName;

//в классе хранятся и парсятся данные, которые получаются путем запроса к серверу
public class City {
    @SerializedName("city_id") private int cityId;
    @SerializedName("city_name") private  String cityName;

    public City(int mCityId, String mCityName) {
        super();
        cityId = mCityId;
        cityName = mCityName;
    }
    public void setCityName(String name) {
        this.cityName = name;
    }
    public String getCityName() {
        return cityName;
    }
    public void setCityId(int id) {cityId = id;}
    public int getCityId() { return cityId; }
}
