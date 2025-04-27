package controller.lichTrinh;

import lombok.Data;

import java.util.*;

/**
 * Quản lý trạng thái tuyến đường, bao gồm ga khởi hành, ga kết thúc, và các ga trung gian.
 * Cung cấp các phương thức để chọn ga và tìm đường đi giữa các ga.
 */
@Data
public class RouteManager {
    private String startStation;
    private String endStation;
    private final List<String> intermediateStations;
    private final List<String> validIntermediateStations;
    private final Map<String, List<String>> stationConnections;

    /**
     * Khởi tạo RouteManager với danh sách kết nối giữa các ga.
     * @param connections Bản đồ chứa các ga và danh sách ga lân cận.
     */
    public RouteManager(Map<String, List<String>> connections) {
        this.stationConnections = connections;
        this.intermediateStations = new ArrayList<>();
        this.validIntermediateStations = new ArrayList<>();
    }

    /**
     * Chọn ga khởi hành và cập nhật trạng thái.
     * @param station Mã ga được chọn.
     * @return True nếu chọn thành công, false nếu không hợp lệ (trùng với ga kết thúc).
     */
    public boolean selectStartStation(String station) {
        if (endStation != null && endStation.equals(station)) {
            return false;
        }
        startStation = station;
        updateValidIntermediateStations();
        return true;
    }

    /**
     * Chọn ga kết thúc và cập nhật trạng thái.
     * @param station Mã ga được chọn.
     * @return True nếu chọn thành công, false nếu không hợp lệ (trùng với ga khởi hành).
     */
    public boolean selectEndStation(String station) {
        if (startStation != null && startStation.equals(station)) {
            return false;
        }
        endStation = station;
        updateValidIntermediateStations();
        return true;
    }

    /**
     * Chuyển đổi trạng thái ga trung gian (chọn hoặc bỏ chọn).
     * @param station Mã ga trung gian.
     */
    public void toggleIntermediateStation(String station) {
        if (validIntermediateStations.contains(station)) {
            if (intermediateStations.contains(station)) {
                intermediateStations.remove(station);
            } else {
                intermediateStations.add(station);
            }
        }
    }

    /**
     * Bỏ chọn ga khởi hành hoặc ga kết thúc.
     * @param station Mã ga cần bỏ chọn.
     */
    public void deselectStation(String station) {
        if (station.equals(startStation)) {
            startStation = null;
            intermediateStations.clear();
        } else if (station.equals(endStation)) {
            endStation = null;
            intermediateStations.clear();
        }
        updateValidIntermediateStations();
    }

    /**
     * Lấy danh sách ga theo thứ tự lịch trình (khởi hành, trung gian, kết thúc).
     * @return Danh sách mã ga.
     */
    public List<String> getScheduleStations() {
        List<String> result = new ArrayList<>();
        if (startStation != null) {
            result.add(startStation);
            result.addAll(intermediateStations);
            if (endStation != null) {
                result.add(endStation);
            }
        }
        return result;
    }

    public String getStartStation() {
        return startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public List<String> getIntermediateStations() {
        return new ArrayList<>(intermediateStations);
    }

    /**
     * Cập nhật danh sách ga trung gian hợp lệ dựa trên tuyến đường từ ga khởi hành đến ga kết thúc.
     */
    private void updateValidIntermediateStations() {
        validIntermediateStations.clear();
        if (startStation != null && endStation != null) {
            validIntermediateStations.addAll(findPath(startStation, endStation));
        }
    }

    /**
     * Tìm đường đi từ ga bắt đầu đến ga kết thúc bằng thuật toán DFS.
     * @param start Mã ga bắt đầu.
     * @param end Mã ga kết thúc.
     * @return Danh sách ga trên tuyến đường, hoặc danh sách rỗng nếu không tìm thấy.
     */
    private List<String> findPath(String start, String end) {
        Map<String, String> previous = new HashMap<>();
        Set<String> visitedRoutes = new HashSet<>();
        List<String> path = new ArrayList<>();

        if (dfs(start, end, previous, visitedRoutes)) {
            String current = end;
            while (current != null) {
                path.add(current);
                current = previous.get(current);
            }
            Collections.reverse(path);
        }
        return path;
    }

    /**
     * Thực hiện tìm kiếm theo chiều sâu (DFS) để tìm đường đi.
     * @param current Ga hiện tại.
     * @param target Ga đích.
     * @param previous Bản đồ lưu ga trước đó.
     * @param visitedRoutes Tập hợp các tuyến đã đi qua.
     * @return True nếu tìm thấy đường đi, false nếu không.
     */
    private boolean dfs(String current, String target, Map<String, String> previous, Set<String> visitedRoutes) {
        if (current.equals(target)) {
            return true;
        }
        for (String neighbor : stationConnections.getOrDefault(current, Collections.emptyList())) {
            String routeId1 = "line_" + current + "_" + neighbor;
            String routeId2 = "line_" + neighbor + "_" + current;
            if (!visitedRoutes.contains(routeId1) && !visitedRoutes.contains(routeId2) && !previous.containsKey(neighbor)) {
                visitedRoutes.add(routeId1);
                previous.put(neighbor, current);
                if (dfs(neighbor, target, previous, visitedRoutes)) {
                    return true;
                }
                previous.remove(neighbor);
            }
        }
        return false;
    }
}