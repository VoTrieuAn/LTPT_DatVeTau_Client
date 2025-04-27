package controller.lichTrinh;

import com.jfoenix.controls.JFXCheckBox;
import config.DatabaseContext;
import config.TrainTicketApplication;
import controller.Menu.MenuController;
import dao.LichTrinhDAO;
import dao.TauDAO;
import dao.impl.TauDAOImpl;
import entity.LichTrinh;
import entity.Tau;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import util.maLTGenerator;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ThemLichTrinhController {
    private static final String CIRCLE_PREFIX = "circle_";

    @FXML private AnchorPane map_pane, anchorPane;
    @FXML private Button btnHuyBo, btnLuuLai, btnThemMoi, btnTroLai;
    @FXML private ComboBox<Integer> cboxGioKH, cboxGioKT, cboxPhutKH, cboxPhutKT;
    @FXML private ComboBox<String> cboxGaKhoiHanh, cboxGaKetThuc, cboxTau;
    @FXML private TableColumn<LichTrinh, String> gaKHColumn, gaKTColumn;
    @FXML private TableColumn<LichTrinh, LocalTime> gioKTColumn, gioKHColumn;
    @FXML private Label labelDanhSachLT, label_GaDung;
    @FXML private TableColumn<LichTrinh, String> maLTColumn, maTauColumn, trangThaiColumn;
    @FXML private TableColumn<LichTrinh, LocalDate> ngayKHColumn, ngayKTColumn;
    @FXML private DatePicker ngayKH_DatePicker, ngayKT_DatePicker;
    @FXML private TableView<LichTrinh> tableThemLT;
    @FXML private JFXCheckBox checkbox_DungAllGa;
    @FXML private Circle circle_DongHoi, circle_DaNang, circle_HaNoi, circle_HaiPhong, circle_Hue, circle_LaoCai, circle_NhaTrang, circle_PhanThiet, circle_QuyNhon,
            circle_SaiGon, circle_ThanhHoa, circle_Vinh, circle_VungTau;
    @FXML private Line line_DaNang_QuyNhon, line_DongHoi_Hue, line_HaNoi_HaiPhong, line_HaNoi_ThanhHoa, line_Hue_DaNang, line_LaoCai_HaNoi, line_NhaTrang_PhanThiet,
            line_PhanThiet_SaiGon, line_QuyNhon_NhaTrang, line_SaiGon_VungTau, line_ThanhHoa_Vinh, line_Vinh_DongHoi;

    private final ObservableList<LichTrinh> scheduleList = FXCollections.observableArrayList();
    private final TauDAO tauDao = new TauDAOImpl();
    private final LichTrinhDAO lichTrinhDao;
    private Map<String, List<String>> stationConnections;
    private Map<String, Line> routeLines;
    private Map<String, Circle> stationCircles;
    private Map<String, Integer> travelTimes;
    private RouteManager routeManager;
    private MapVisualizer mapVisualizer;
    private ScheduleGenerator scheduleGenerator;
    private Validator validator;
    private UIUpdater uiUpdater;
    private boolean highlightAllStations = false;

    public ThemLichTrinhController() {
        DatabaseContext databaseContext = TrainTicketApplication.getInstance().getDatabaseContext();
        this.lichTrinhDao = databaseContext.newEntityDAO(LichTrinhDAO.class);
    }

    @FXML
    public void initialize() {
        stationConnections = initializeStationConnections();
        routeLines = initializeRouteLines();
        stationCircles = initializeStationCircles();
        travelTimes = initializeTravelTimes();

        routeManager = new RouteManager(stationConnections);
        mapVisualizer = new MapVisualizer(stationCircles, routeLines, stationConnections);
        scheduleGenerator = new ScheduleGenerator(travelTimes);
        validator = new Validator();
        uiUpdater = new UIUpdater(cboxGaKhoiHanh, cboxGaKetThuc, label_GaDung, tableThemLT);

        loadTrainComboBox();
        loadTimeComboBox();
        loadStationComboBox();
        setupEventListeners();
    }

    private void setupEventListeners() {

    }

    private void handleHourSelection(ComboBox<Integer> hourComboBox, ComboBox<Integer> minuteComboBox) {
        Integer selectedHour = hourComboBox.getValue();
        if (selectedHour != null && selectedHour == 24) {
            minuteComboBox.setValue(0);
            minuteComboBox.setDisable(true);
        } else {
            minuteComboBox.setDisable(false);
        }
    }

    private void loadTrainComboBox() {
        List<Tau> trains = tauDao.getAllTau();
        ObservableList<String> trainCodes = FXCollections.observableArrayList(
                trains.stream().map(Tau::getMaTau).toList()
        );
        cboxTau.setItems(trainCodes);
    }

    private void loadTimeComboBox() {
        List<Integer> hours = new ArrayList<>();
        for (int i = 0; i <= 24; i++) {
            hours.add(i);
        }
        List<Integer> minutes = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            minutes.add(i);
        }
        cboxGioKH.setItems(FXCollections.observableArrayList(hours));
        cboxGioKT.setItems(FXCollections.observableArrayList(hours));
        cboxPhutKH.setItems(FXCollections.observableArrayList(minutes));
        cboxPhutKT.setItems(FXCollections.observableArrayList(minutes));
    }

    private void loadStationComboBox() {
        cboxGaKhoiHanh.setItems(FXCollections.observableArrayList(maLTGenerator.getStationMap().keySet()));
        cboxGaKetThuc.setItems(FXCollections.observableArrayList(maLTGenerator.getStationMap().keySet()));
    }

    @FXML
    public void onCircleClick(MouseEvent event) {
        Circle circle = (Circle) event.getSource();
        String station = extractStationId(circle);

        if (routeManager.getStartStation() == null && routeManager.getEndStation() == null) {
            selectStartStation(station, circle);
        } else if (routeManager.getStartStation() != null && routeManager.getEndStation() == null) {
            selectEndStation(station, circle);
        } else if (routeManager.getIntermediateStations().contains(station)) {
            toggleIntermediateStation(station, circle);
        } else {
            deselectStation(station, circle);
        }
        updateMapAndUI();
    }

    private String extractStationId(Circle circle) {
        String id = circle.getId().replace(CIRCLE_PREFIX, "");
        return mapLongCodeToShortCode(id);
    }

    private void selectStartStation(String station, Circle circle) {
        if (routeManager.selectStartStation(station)) {
            mapVisualizer.colorStation(station, Color.RED);
            uiUpdater.updateStationComboBox(cboxGaKhoiHanh, station);
        } else {
            showAlert("Cảnh báo", "Ga khởi hành không được trùng ga kết thúc", Alert.AlertType.WARNING);
        }
    }

    private void selectEndStation(String station, Circle circle) {
        if (routeManager.selectEndStation(station)) {
            mapVisualizer.colorStation(station, Color.BLUE);
            uiUpdater.updateStationComboBox(cboxGaKetThuc, station);
        } else {
            showAlert("Cảnh báo", "Ga kết thúc không được trùng ga khởi hành", Alert.AlertType.WARNING);
        }
    }

    private void toggleIntermediateStation(String station, Circle circle) {
        routeManager.toggleIntermediateStation(station);
        mapVisualizer.colorStation(station, routeManager.getIntermediateStations().contains(station) ? Color.ORANGE : Color.WHITE);
    }

    private void deselectStation(String station, Circle circle) {
        routeManager.deselectStation(station);
        mapVisualizer.resetRouteColors();
        if (routeManager.getStartStation() != null) {
            mapVisualizer.colorStation(routeManager.getStartStation(), Color.RED);
        }
        if (routeManager.getEndStation() != null) {
            mapVisualizer.colorStation(routeManager.getEndStation(), Color.BLUE);
        }
        uiUpdater.updateStationComboBox(station.equals(routeManager.getStartStation()) ? cboxGaKhoiHanh : cboxGaKetThuc, null);
    }

    private void updateMapAndUI() {
        mapVisualizer.colorRoute(
                routeManager.getStartStation(),
                routeManager.getEndStation(),
                routeManager.getIntermediateStations(),
                highlightAllStations
        );
        uiUpdater.updateIntermediateStationsLabel(routeManager.getScheduleStations());
    }

    @FXML
    public void actionGaKH(ActionEvent event) {
        String startStation = cboxGaKhoiHanh.getValue();
        if (startStation == null || startStation.equals("--Chọn ga--")) return;
        String stationCode = maLTGenerator.getStationMap().get(startStation);
        if (stationCode.equals(routeManager.getEndStation())) {
            showAlert("Cảnh báo", "Ga khởi hành không được trùng ga kết thúc", Alert.AlertType.WARNING);
            uiUpdater.updateStationComboBox(cboxGaKhoiHanh, null);
            mapVisualizer.resetRouteColors();
            return;
        }
        routeManager.selectStartStation(stationCode);
        mapVisualizer.colorStation(stationCode, Color.RED);
        updateMapAndUI();
    }

    @FXML
    public void actionGaKT(ActionEvent event) {
        String endStation = cboxGaKetThuc.getValue();
        if (endStation == null || endStation.equals("--Chọn ga--")) return;
        String stationCode = maLTGenerator.getStationMap().get(endStation);
        if (stationCode.equals(routeManager.getStartStation())) {
            showAlert("Cảnh báo", "Ga kết thúc không được trùng ga khởi hành", Alert.AlertType.WARNING);
            uiUpdater.updateStationComboBox(cboxGaKetThuc, null);
            mapVisualizer.resetRouteColors();
            return;
        }
        routeManager.selectEndStation(stationCode);
        mapVisualizer.colorStation(stationCode, Color.BLUE);
        updateMapAndUI();
    }

    @FXML
    private void themMoi() {
        String errorMessage = validator.validateSchedule(
                cboxTau.getValue(),
                cboxGaKhoiHanh.getValue(),
                cboxGaKetThuc.getValue(),
                ngayKH_DatePicker.getValue(),
                cboxGioKH.getValue(),
                cboxPhutKH.getValue()
        );

        if (errorMessage != null) {
            showAlert("Cảnh báo", errorMessage, Alert.AlertType.WARNING);
            focusInvalidField(errorMessage);
            return;
        }

        List<String> stations = routeManager.getScheduleStations();
        LocalTime departureTime = LocalTime.of(cboxGioKH.getValue(), cboxPhutKH.getValue());
        LocalDate departureDate = ngayKH_DatePicker.getValue();
        Tau train = tauDao.timKiemId(cboxTau.getValue());

        List<LichTrinh> schedules = scheduleGenerator.generateSchedules(stations, departureTime, departureDate, train);
        if (!schedules.isEmpty()) {
            scheduleList.clear();
            scheduleList.addAll(schedules);
            uiUpdater.updateScheduleTable(scheduleList, maLTColumn, gaKHColumn, gaKTColumn, ngayKHColumn, ngayKTColumn,
                    gioKHColumn, gioKTColumn, maTauColumn, trangThaiColumn);
            btnLuuLai.setDisable(false);
            tableThemLT.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void luuLai() {
        if (scheduleList.isEmpty()) {
            showAlert("Thông báo", "Danh sách lịch trình không có dữ liệu để lưu!", Alert.AlertType.WARNING);
            return;
        }

        try {
            for (LichTrinh lichTrinh : scheduleList) {
                lichTrinhDao.them(lichTrinh);
            }
            showAlert("Thông báo", "Lưu thành công!", Alert.AlertType.CONFIRMATION);
            resetUI();
        } catch (Exception e) {
            showAlert("Lỗi", "Có lỗi xảy ra khi lưu lịch trình: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void troLai() {
        if (!scheduleList.isEmpty()) {
            Optional<ButtonType> result = showAlertConfirm("Bạn có muốn lưu trước khi quay lại?");
            if (result.isPresent()) {
                ButtonType buttonType = result.get();
                if (buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
                    luuLai();
                    navigateToScheduleScreen();
                } else if (buttonType.getButtonData() == ButtonBar.ButtonData.NO) {
                    navigateToScheduleScreen();
                }
            }
        } else {
            navigateToScheduleScreen();
        }
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        if (event.getSource() == anchorPane) {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                luuLai();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                troLai();
            } else if (event.getCode() == KeyCode.ENTER) {
                themMoi();
            }
        }
    }

    private void focusInvalidField(String errorMessage) {
        if (errorMessage.contains("ga khởi hành")) {
            cboxGaKhoiHanh.requestFocus();
        } else if (errorMessage.contains("ga kết thúc")) {
            cboxGaKetThuc.requestFocus();
        } else if (errorMessage.contains("tàu")) {
            cboxTau.requestFocus();
        } else if (errorMessage.contains("ngày khởi hành")) {
            ngayKH_DatePicker.requestFocus();
        } else if (errorMessage.contains("thời gian khởi hành")) {
            cboxGioKH.requestFocus();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.show();
    }

    private Optional<ButtonType> showAlertConfirm(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(content);
        ButtonType buttonLuu = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType buttonKhongLuu = new ButtonType("Không", ButtonBar.ButtonData.NO);
        ButtonType buttonHuy = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonLuu, buttonKhongLuu, buttonHuy);
        return alert.showAndWait();
    }

    private void navigateToScheduleScreen() {
        MenuController.instance.readyUI("LichTrinh");
    }

    private void resetUI() {
        MenuController.instance.readyUI("LichTrinh/ThemLichTrinh");
    }

    private Map<String, List<String>> initializeStationConnections() {
        Map<String, List<String>> connections = new HashMap<>();
        connections.put("LC", List.of("HN"));
        connections.put("HN", List.of("LC", "HP", "TH"));
        connections.put("HP", List.of("HN"));
        connections.put("TH", List.of("HN", "Vinh"));
        connections.put("Vinh", List.of("TH", "DH"));
        connections.put("DH", List.of("Vinh", "HUE"));
        connections.put("HUE", List.of("DN", "DH"));
        connections.put("DN", List.of("HUE", "QN"));
        connections.put("QN", List.of("NT", "DN"));
        connections.put("NT", List.of("QN", "PT"));
        connections.put("PT", List.of("NT", "SG"));
        connections.put("SG", List.of("PT", "VT"));
        connections.put("VT", List.of("SG"));
        return connections;
    }

    private Map<String, Line> initializeRouteLines() {
        Map<String, Line> lines = new HashMap<>();
        lines.put("line_LC_HN", line_LaoCai_HaNoi);
        lines.put("line_HN_HP", line_HaNoi_HaiPhong);
        lines.put("line_HN_TH", line_HaNoi_ThanhHoa);
        lines.put("line_TH_Vinh", line_ThanhHoa_Vinh);
        lines.put("line_Vinh_DH", line_Vinh_DongHoi);
        lines.put("line_DH_HUE", line_DongHoi_Hue);
        lines.put("line_HUE_DN", line_Hue_DaNang);
        lines.put("line_DN_QN", line_DaNang_QuyNhon);
        lines.put("line_QN_NT", line_QuyNhon_NhaTrang);
        lines.put("line_NT_PT", line_NhaTrang_PhanThiet);
        lines.put("line_PT_SG", line_PhanThiet_SaiGon);
        lines.put("line_SG_VT", line_SaiGon_VungTau);
        return lines;
    }

    private Map<String, Circle> initializeStationCircles() {
        Map<String, Circle> circles = new HashMap<>();
        circles.put("circle_LC", circle_LaoCai);
        circles.put("circle_HN", circle_HaNoi);
        circles.put("circle_HP", circle_HaiPhong);
        circles.put("circle_TH", circle_ThanhHoa);
        circles.put("circle_Vinh", circle_Vinh);
        circles.put("circle_DH", circle_DongHoi);
        circles.put("circle_HUE", circle_Hue);
        circles.put("circle_DN", circle_DaNang);
        circles.put("circle_QN", circle_QuyNhon);
        circles.put("circle_NT", circle_NhaTrang);
        circles.put("circle_PT", circle_PhanThiet);
        circles.put("circle_VT", circle_VungTau);
        circles.put("circle_SG", circle_SaiGon);
        return circles;
    }

    private Map<String, Integer> initializeTravelTimes() {
        Map<String, Integer> times = new HashMap<>();
        times.put("LC_HN", 480);
        times.put("HN_HP", 150);
        times.put("HN_TH", 210);
        times.put("TH_Vinh", 150);
        times.put("Vinh_DH", 240);
        times.put("DH_HUE", 180);
        times.put("HUE_DN", 150);
        times.put("DN_QN", 360);
        times.put("QN_NT", 290);
        times.put("NT_PT", 240);
        times.put("PT_SG", 220);
        times.put("SG_VT", 120);
        return times;
    }

    private String mapLongCodeToShortCode(String longCode) {
        return switch (longCode) {
            case "HaNoi" -> "HN";
            case "SaiGon" -> "SG";
            case "DaNang" -> "DN";
            case "NhaTrang" -> "NT";
            case "Hue" -> "HUE";
            case "HaiPhong" -> "HP";
            case "VungTau" -> "VT";
            case "QuyNhon" -> "QN";
            case "PhanThiet" -> "PT";
            case "LaoCai" -> "LC";
            case "ThanhHoa" -> "TH";
            case "Vinh" -> "Vinh";
            case "DongHoi" -> "DH";
            default -> throw new IllegalArgumentException("Mã ga không hợp lệ: " + longCode);
        };
    }

    public void mouseClickedEvent(MouseEvent mouseEvent) {
    }

    public void controller(ActionEvent actionEvent) {
    }
}