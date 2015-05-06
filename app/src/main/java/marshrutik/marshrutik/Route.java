package marshrutik.marshrutik;


import com.google.gson.annotations.SerializedName;

public class Route {
    @SerializedName("route_id") private int routeId;
    @SerializedName("typeofmovement_name") private String typeOfMove;
    @SerializedName("route_description") private String routeDescription;
    @SerializedName("route_name") private String routeTitle;
    @SerializedName("route_img_url") private String imgUrl;
    @SerializedName("themes") private String[] themes;

    public int getRouteId() {return routeId;}
    public String getTypeOfMove() {return typeOfMove;}
    public String getRouteDescription() {return routeDescription; }
    public String getRouteTitle() { return routeTitle; }
    public String getThemes(int i) {return themes[i]; }
    public String getImgUrl() { return imgUrl; }

}
