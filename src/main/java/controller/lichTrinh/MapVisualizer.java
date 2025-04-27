package controller.lichTrinh;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.*;

/**
 * Quản lý hiển thị bản đồ, bao gồm tô màu các ga và tuyến đường.
 */
public class MapVisualizer {
    private static final String CIRCLE_PREFIX = "circle_";
    private static final String LINE_PREFIX = "line_";

    private final Map<String, Circle> stationCircles;
    private final Map<String, Line> routeLines;
    private final Map<String, List<String>> stationConnections;

    /**
     * Khởi tạo MapVisualizer với các vòng tròn, đường nối, và kết nối ga.
     * @param circles Bản đồ chứa các vòng tròn đại diện cho ga.
     * @param lines Bản đồ chứa các đường nối đại diện cho tuyến.
     * @param connections Bản đồ chứa các kết nối giữa các ga.
     */
    public MapVisualizer(Map<String, Circle> circles, Map<String, Line> lines, Map<String, List<String>> connections) {
        this.stationCircles = circles;
        this.routeLines = lines;
        this.stationConnections = connections;
    }

    /**
     * Tô màu cho một ga.
     * @param station Mã ga.
     * @param color Màu cần tô.
     */
    public void colorStation(String station, Color color) {
        Circle circle = stationCircles.get(CIRCLE_PREFIX + station);
        if (circle != null) {
            circle.setFill(color);
        }
    }

    /**
     * Tô màu tuyến đường từ ga khởi hành đến ga kết thúc.
     * @param startStation Mã ga khởi hành.
     * @param endStation Mã ga kết thúc.
     * @param intermediateStations Danh sách ga trung gian được chọn.
     * @param highlightAllStations True nếu tô màu tất cả ga trung gian.
     */
    public void colorRoute(String startStation, String endStation, List<String> intermediateStations, boolean highlightAllStations) {
        resetRouteColors();
        if (startStation == null || endStation == null) {
            return;
        }

        Map<String, String> previous = new HashMap<>();
        Set<String> visitedRoutes = new HashSet<>();
        if (findPath(startStation, endStation, previous, visitedRoutes)) {
            String current = endStation;
            while (previous.containsKey(current)) {
                String previousStation = previous.get(current);
                String lineId1 = LINE_PREFIX + previousStation + "_" + current;
                String lineId2 = LINE_PREFIX + current + "_" + previousStation;
                Line line = routeLines.get(lineId1);
                if (line == null) {
                    line = routeLines.get(lineId2);
                }
                if (line != null) {
                    line.setStroke(Color.ORANGE);
                }
                if (highlightAllStations) {
                    colorStation(current, Color.ORANGE);
                }
                current = previousStation;
            }
            if (highlightAllStations) {
                colorStation(startStation, Color.RED);
                colorStation(endStation, Color.BLUE);
            }
            for (String station : intermediateStations) {
                colorStation(station, Color.ORANGE);
            }
        }
    }

    /**
     * Đặt lại màu cho tất cả ga và tuyến đường về màu mặc định (trắng).
     */
    public void resetRouteColors() {
        stationCircles.values().forEach(circle -> circle.setFill(Color.WHITE));
        routeLines.values().forEach(line -> line.setStroke(Color.WHITE));
    }

    /**
     * Tìm đường đi từ ga bắt đầu đến ga kết thúc bằng thuật toán DFS.
     * @param start Mã ga bắt đầu.
     * @param end Mã ga kết thúc.
     * @param previous Bản đồ lưu ga trước đó.
     * @param visitedRoutes Tập hợp các tuyến đã đi qua.
     * @return True nếu tìm thấy đường đi, false nếu không.
     */
    private boolean findPath(String start, String end, Map<String, String> previous, Set<String> visitedRoutes) {
        if (start.equals(end)) {
            return true;
        }
        for (String neighbor : stationConnections.getOrDefault(start, Collections.emptyList())) {
            String routeId1 = LINE_PREFIX + start + "_" + neighbor;
            String routeId2 = LINE_PREFIX + neighbor + "_" + start;
            if (!visitedRoutes.contains(routeId1) && !visitedRoutes.contains(routeId2) && !previous.containsKey(neighbor)) {
                visitedRoutes.add(routeId1);
                previous.put(neighbor, start);
                if (findPath(neighbor, end, previous, visitedRoutes)) {
                    return true;
                }
                previous.remove(neighbor);
            }
        }
        return false;
    }
}