package marshrutik.marshrutik;


import java.util.ArrayList;
import java.util.List;
//класс для обработки ответа сервера о добавлении маршрута
public class RouteIdAnswer {
    private String code;
    private int status;
    private String message;
    private attributes attr;
//    RouteIdAnswer() {
//        this.attr = new ArrayList<>();
//    }
    public int getRouteId() {
        return attr.route_id;
    }

    private class attributes {
        int route_id;

//        public int getRouteId() {
//            return route_id;
//        }
    }
}
