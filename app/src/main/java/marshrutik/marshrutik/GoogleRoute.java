package marshrutik.marshrutik;


import java.util.List;

//класс для обработки результатов построения маршрута Гуглом
public class GoogleRoute {

    public List<gRoute> routes;

    public String getPoints() {
        return this.routes.get(0).overview_polyline.points;
    }
    class gRoute {
        OverviewPolyline overview_polyline;
    }
    class OverviewPolyline {
        String points;
    }
}
