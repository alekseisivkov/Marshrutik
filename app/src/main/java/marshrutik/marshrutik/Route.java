package marshrutik.marshrutik;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Route {
    @SerializedName("route_id") private int routeId;
    @SerializedName("typeofmovement_name") private String typeOfMove;
    @SerializedName("route_description") private String routeDescription;
    @SerializedName("route_name") private String routeTitle;
    @SerializedName("route_img_url") private String imgUrl;
    @SerializedName("themes") private String[] themes;
    private List<Routeparts> routeparts;
    Route() {
        this.routeparts = new ArrayList<>();
    }

    class Routeparts {
        int rp_id;
        int part_id;
        String rp_start_latitude;       //в стрингах потому, что в getStartRoutepart я возвращаю комбинацию lat,lng
        String rp_start_longitude;
        String rp_end_latitude;
        String rp_end_longitude;
        String rp_description;
        String rp_title;

    }
    //get'еры для routeparts
    public String getEndRoutepart(int position) {
        return routeparts.get(position).rp_end_latitude + "," + routeparts.get(position).rp_end_longitude;
    }
    public int getRpId(int position) {
        return routeparts.get(position).rp_id;
    }
    public String getStartRoutepart(int position) {
        return routeparts.get(position).rp_start_latitude + "," + routeparts.get(position).rp_start_longitude;
    }
    public int getRoutepartId(int position) {
        return routeparts.get(position).part_id;
    }
    public String getRoutepartTitle(int position) {
        return routeparts.get(position).rp_title;
    }
    public String getRoutepartDesc(int position) {
        return routeparts.get(position).rp_description;
    }

    public int getRoutePartsLength() {
        return routeparts.size();
    }


    //get'еры для Routes
    public int getRouteId() {return routeId;}
    public String getTypeOfMove() {return typeOfMove;}
    public String getRouteDescription() {return routeDescription; }
    public String getRouteTitle() { return routeTitle; }
    public String getThemes(int i) {
        if (themes.length!=0) {
            return themes[i]; }
        else {
            return "";  //если темы у маршрута нет, то возвращает пустая строка
        }
    }
    public String getImgUrl() { return imgUrl; }
}
