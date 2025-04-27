package controller;

import controller.Menu.MenuController;
import entity.LichTrinh;
import entity.Tau;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import rmi.RMIServiceLocator;
import service.LichTrinhService;
import util.ExportExcelUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class LichTrinhController {
    @FXML
    private TableView<LichTrinh> tableLichTrinh;
    @FXML
    private TableColumn<LichTrinh, String> maLTColumn, gaKhoiHanhColumn, gaKetThucColumn, trangThaiColumn, maTauColumn;
    @FXML
    private TableColumn<LichTrinh, LocalDate> ngayDiColumn, ngayDenColumn;
    @FXML
    private TableColumn<LichTrinh, LocalTime> gioDiColumn, gioDenColumn;

    @FXML
    private DatePicker picker_NgayDI;
    @FXML
    private Button btnLamMoi, btn_XuatExcel, btnThem, btn_Loc;
    @FXML
    private ToggleButton toggle_ChuaKhoiHanh, toggle_DaHuy, toggle_DaKhoiHanh;

    private LichTrinhService lichTrinhService = RMIServiceLocator.getLichTrinhService();
    private Map<ToggleButton, Integer> toggleTrangThaiMap = new HashMap<>();
    @FXML
    private CheckBox checkbox_LocCacNgaySau;

    public void initialize() {
        loadDataVaoTable();


        // Khởi tạo ánh xạ ToggleButton với trạng thái
        toggleTrangThaiMap.put(toggle_ChuaKhoiHanh, 0); // Trạng thái 0: Chưa khởi hành
        toggleTrangThaiMap.put(toggle_DaKhoiHanh, 1);   // Trạng thái 1: Đã khởi hành
        toggleTrangThaiMap.put(toggle_DaHuy, -1);

        // Thêm sự kiện thay đổi trạng thái cho tất cả ToggleButton
        toggleTrangThaiMap.keySet().forEach(toggle ->
                toggle.setOnAction(event -> capNhatLichTrinh())
        );
        picker_NgayDI.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                capNhatLichTrinh();
        });

        checkbox_LocCacNgaySau.setOnAction(event -> {
            capNhatLichTrinh();
        });
    }

    private void loadDataVaoTable() {
        // Ánh xạ các cột với các thuộc tính của Entity
        maLTColumn.setCellValueFactory(new PropertyValueFactory<>("maLt"));
        gaKhoiHanhColumn.setCellValueFactory(new PropertyValueFactory<>("gaKhoiHanh"));
        gaKetThucColumn.setCellValueFactory(new PropertyValueFactory<>("gaKetThuc"));
        ngayDiColumn.setCellValueFactory(new PropertyValueFactory<>("ngayKhoiHanh"));
        gioDiColumn.setCellValueFactory(new PropertyValueFactory<>("gioDi"));
        ngayDenColumn.setCellValueFactory(new PropertyValueFactory<>("ngayDen"));
        gioDenColumn.setCellValueFactory(new PropertyValueFactory<>("gioDen"));
        maTauColumn.setCellValueFactory(cellData -> {
            Tau tau = cellData.getValue().getTauByMaTau(); // Lấy đối tượng Tau
            return tau != null ? new SimpleStringProperty(tau.getMaTau()) : new SimpleStringProperty("");
        });
        trangThaiColumn.setCellValueFactory(cellData -> {
            int trangThai = cellData.getValue().getTrangThai();
            String text = switch (trangThai) {
                case -1 -> "Đã hủy";
                case 0 -> "Chưa khởi hành";
                case 1 -> "Đã khởi hành";
                default -> "Không xác định";
            };
            return new SimpleStringProperty(text);
        });

        loadData();
    }

    private void loadData() {
        try {
            List<LichTrinh> lichTrinhList = lichTrinhService.getAllLichTrinh();
            if (lichTrinhList != null) {
                tableLichTrinh.getItems().setAll(lichTrinhList);
            } else {
                System.err.println("Không có dữ liệu nào từ database!");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void showThemLichTrinh() {
        MenuController.instance.readyUI("LichTrinh/ThemLichTrinh");
    }

    public void capNhatLichTrinh() {
        try {
            // Lấy các trạng thái được chọn từ toggle buttons
            Set<Integer> trangThaiSet = toggleTrangThaiMap.entrySet().stream()
                    .filter(entry -> entry.getKey().isSelected())  // Kiểm tra toggleButton có được chọn không
                    .map(Map.Entry::getValue)  // Lấy giá trị trạng thái (trangThai)
                    .collect(Collectors.toSet());  // Tạo set chứa các trạng thái

            // Lấy giá trị ngày đi từ DatePicker (cần kiểm tra nếu ngày không null)
            LocalDate ngayDi = picker_NgayDI.getValue();  // picker_NgayDI là đối tượng DatePicker

            // Nếu ngày đi không được chọn (null), gọi hàm với chỉ trạng thái
            List<LichTrinh> dsLT;
            if (ngayDi != null) {
                if (checkbox_LocCacNgaySau.isSelected()) {
                    dsLT = lichTrinhService.getLichTrinhSauHoacBangNgayDi(trangThaiSet, ngayDi);
                }
                // Lọc thêm theo ngày đi nếu đã chọn ngày
                else {
                    dsLT = lichTrinhService.getLichTrinhTheoTrangThai(trangThaiSet, ngayDi);
                }
            } else {
                // Nếu không có ngày, chỉ lọc theo trạng thái
                dsLT = lichTrinhService.getLichTrinhTheoTrangThai(trangThaiSet, null);
            }

            // Cập nhật dữ liệu vào tableView
            tableLichTrinh.getItems().setAll(dsLT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHuyLichTrinh(ActionEvent event) {
        try {
            LichTrinh lichTrinhDuocChon = tableLichTrinh.getSelectionModel().getSelectedItem();
            System.out.println(lichTrinhDuocChon);
            if (lichTrinhDuocChon == null) {
                showAlert("Cảnh báo", "Vui lòng chọn lịch trình muốn hủy", Alert.AlertType.WARNING);
                return;
            }
            if (lichTrinhDuocChon.getTrangThai() == 1) {
                showAlert("Lỗi", "Không thể xóa lịch trình đã khởi hành", Alert.AlertType.ERROR);
                return;
            }
            LocalDate ngayKhoiHanh = lichTrinhDuocChon.getNgayKhoiHanh().toLocalDate();
            if (ngayKhoiHanh.isBefore(LocalDate.now().plusDays(80))) {
                showAlert("Lỗi", "Không thể xóa lịch trình trước 80 ngày", Alert.AlertType.ERROR);
                return;
            }

            Optional<ButtonType> result = showAlertConfirm("Bạn có chắc chắn muốn xóa lịch trình?");
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.YES) {
                boolean isUpdated = lichTrinhService.updateTrangThai(lichTrinhDuocChon.getMaLt(), -1);
                if (isUpdated) {
                    // Cập nhật trạng thái của đối tượng trong ObservableList
                    lichTrinhDuocChon.setTrangThai(-1);

                    // Refresh TableView để hiển thị thay đổi
                    tableLichTrinh.refresh();

                    showAlert("Thông báo", "Xóa lịch trình thành công", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Lỗi", "Đã có lỗi xảy ra, vui lòng thử lại", Alert.AlertType.ERROR);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void xuatExcel() throws IOException {
        Stage stage = (Stage) tableLichTrinh.getScene().getWindow();
        ExportExcelUtil.exportTableViewToExcel(tableLichTrinh, stage);
    }

    @FXML
    void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == btnThem) {
            showThemLichTrinh();
        }
        if (source == btnLamMoi) {
            MenuController.instance.readyUI("LichTrinh");
        }
        if (source == btn_XuatExcel) {
            xuatExcel();
        }
    }

    @FXML
    void keyPressed(KeyEvent event) {
        Object source = event.getSource();
        if (event.getCode() == KeyCode.F5) MenuController.instance.readyUI("LichTrinh");
    }

    @FXML
    void mouseClicked(MouseEvent event) {

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
        alert.getButtonTypes().setAll(buttonLuu, buttonKhongLuu);
        return alert.showAndWait();
    }
}

