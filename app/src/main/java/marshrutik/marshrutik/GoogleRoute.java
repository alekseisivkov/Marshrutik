package marshrutik.marshrutik;


import java.util.List;

//класс для обработки результатов построения маршрута Гуглом
public class GoogleRoute {

    public List<gRoute> routes;

    public String getPoints() {
        return this.routes.get(0).overview_polyline.points;
    }
    public String[] getWaypointOrder() {
        return this.routes.get(0).waypoint_order;
    }
    class gRoute {
        OverviewPolyline overview_polyline;
        String[] waypoint_order;
    }
    class OverviewPolyline {
        String points;
    }
}
