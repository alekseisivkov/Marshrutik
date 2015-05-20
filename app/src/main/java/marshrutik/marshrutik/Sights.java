package marshrutik.marshrutik;


import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

//класс для приема ответа от сервера, соедржащий список достопримечательностей из их названия и координат
public class Sights {
    @SerializedName("sight_name") private String sight_name;
    @SerializedName("sight_longitude") private String  sight_longitude;
    @SerializedName("sight_latitude" )private String  sight_latitude;

    private boolean sightSelected;

    public String getSightName() {
        return sight_name;
    }
    public String  getLatLng() {
        return sight_latitude + "," + sight_longitude;
    }
    public boolean isSightSelected() {
        return sightSelected;
    }
    public void setSightSelected(boolean isChecked) {
        sightSelected = isChecked;
    }

}
