package controller.HoaDon;

import config.TrainTicketApplication;
import controller.Menu.MenuNhanVienController;
import dao.HoaDonDAO;
import dao.impl.HoaDonDAOImpl;
import entity.HoaDon;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import rmi.RMIServiceLocator;
import service.EntityService;
import service.impl.HoaDonServiceimpl;
import util.ComponentUtil;
import util.ExportExcelUtil;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class HoaDonController {

    @FXML
    private AnchorPane anchorPaneMain;

    @FXML
    private Button btnTatCa;

    @FXML
    private Button btnXuat;

    @FXML
    private Button locButton;

    @FXML
    private HBox hBoxPage;

    @FXML
    private TableColumn<HoaDon, String> hoTenHKColumn;

    @FXML
    private TableColumn<HoaDon, String> hoTenNVColumn;

    @FXML
    private TableColumn<HoaDon, String> maColumn;

    @FXML
    private TextField maNvTextField;

    @FXML
    private TableColumn<HoaDon, Timestamp> ngayLapColumn;

    @FXML
    private TableView<HoaDon> tableHoaDon;

    @FXML
    private TextField timKiemTextField;

    @FXML
    private TableColumn<HoaDon, String> tongTienColumn;
    @FXML
    private CheckBox ngayCheckbox;

    @FXML
    private ComboBox<String> ngayComboBox;

    @FXML
    private CheckBox thangCheckbox;

    @FXML
    private ComboBox<String> thangComboBox;

    @FXML
    private CheckBox namCheckbox;

    @FXML
    private ComboBox<String> namComboBox;

    private ObservableList<HoaDon> danhSachHoaDon = FXCollections.observableArrayList();
    private List<HoaDon> danhSachHoaDonDB;
    private final int LIMIT = 15;
    private String status = "all";
    @FXML
    private void initialize() throws RemoteException {
        setValueTable();
        loadData();
        loadNgayComboBox();
        loadThangComboBox();
        loadNamComboBox();
        setDefaultComboBox();
        phanTrang(danhSachHoaDonDB.size());
    }
    @FXML
    void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == btnXuat) {
            xuatExcel();
        } else if (source == thangComboBox) {
            selectThangComboBox();
        } else if (source == namComboBox) {
            selectNamCombox();
        } else if (source == locButton) {
            filterNgayThangNam();
        } else {
            int soLuongBanGhi = 0;
            if (source == btnTatCa) {
                status = "all";
                hBoxPage.setVisible(true);
                soLuongBanGhi = locDuLieuTheoTrangThai(status);
            }
            phanTrang(soLuongBanGhi);
        }
    }
    @FXML
    void keyPressed(KeyEvent event) {
        Object source = event.getSource();
        if (source == anchorPaneMain) {
            if (event.getCode() == KeyCode.ESCAPE) {
                huyChonDong();
            }
        }
    }
    @FXML
    void mouseClicked(MouseEvent event) throws RemoteException {
        Object source = event.getSource();
        if (source == timKiemTextField) {
            timKiem();
        } else if (source == tableHoaDon) {
            showThongTin(event.getClickCount());
        } else if (source == anchorPaneMain) {
            huyChonDong();
        }
    }
    // Chức năng
    private void timKiem() {
        // Tạo một FilteredList dựa trên danh sách gốc
        // Thực hiện chổ này giúp nếu field rỗng thì nó sẽ load lại tất cả nhưng tôi muốn chỉ ở trang 1
        ObservableList<HoaDon> newDanhSachHoaDon = FXCollections.observableArrayList();
        newDanhSachHoaDon.addAll(danhSachHoaDonDB);
        hBoxPage.setVisible(false);
        FilteredList<HoaDon> filteredData = new FilteredList<>( newDanhSachHoaDon, p -> true);
        // Thêm bộ lọc cho ô tìm kiếm
        timKiemTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(hoaDon -> {
                // Nếu ô tìm kiếm rỗng, hiển thị tất cả dữ liệu
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                String hoTenHk = hoaDon.getHanhkhachByMaHk().getHoTenDem() + " " + hoaDon.getHanhkhachByMaHk().getTen();
                String hoTenNv = hoaDon.getNhanvienByMaNv().getHoTenDem() + " " + hoaDon.getNhanvienByMaNv().getTen();
                // Kiểm tra từng cột xem có chứa giá trị tìm kiếm không
                if (hoaDon.getMaHd().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (hoTenNv.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else {
                    if (hoTenHk.toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                }
                return false; // Không tìm thấy
            });
            // Cập nhật phân trang với danh sách đã lọc
            hBoxPage.setVisible(true);
            phanTrang(filteredData);
            if (hBoxPage.getChildren().size() > 0) {
                if (hBoxPage.getChildren().get(0) instanceof Button) {
                    Button firstButton = (Button) hBoxPage.getChildren().get(0);
                    firstButton.fire(); // Kích hoạt sự kiện click
                }
            }
        });
        // Đặt dữ liệu vào TableView
        tableHoaDon.setItems(filteredData);
    }
    private void showThongTin(int countClick) throws RemoteException {
        if (countClick == 2) {
            HoaDon hoaDon = tableHoaDon.getSelectionModel().getSelectedItem();
            if (hoaDon != null) {
                ChiTietHoaDonController chiTietHoaDonController =
                        MenuNhanVienController.instance.readyUI("HoaDon/ChiTietHoaDon").getController();
                chiTietHoaDonController.setHoaDon(hoaDon);
            }
        }
    }
    private void phanTrang(int soLuongBanGhi) {
        hBoxPage.getChildren().clear();
        loadCountPage(soLuongBanGhi);
        // Code bắt sự kiện
        hBoxPage.getChildren().forEach(button -> {
            ((Button)button).setOnAction(event -> {
                String value = ((Button) button).getText();
                int skip = (Integer.parseInt(value) - 1) * LIMIT;
                // Giới hạn phần tử cuối cùng để tránh vượt quá kích thước của danh sách
                if (status.equalsIgnoreCase("all")) {
                    int endIndex = Math.min(skip + LIMIT, danhSachHoaDonDB.size());
                    List<HoaDon> hoaDons = danhSachHoaDonDB.subList(skip, endIndex);
                    loadData(hoaDons);
                }
            });
        });
    }
    /**
     * @Param Hàm phân trang, nhận vào danh sách đã lọc (Cho tìm kiếm)
     */
    private void phanTrang(FilteredList<HoaDon> danhSachPhanTrang) {
        hBoxPage.getChildren().clear();
        loadCountPage(danhSachPhanTrang.size());

        hBoxPage.getChildren().forEach(button -> {
            ((Button) button).setOnAction(event -> {
                String value = ((Button) button).getText();
                int skip = (Integer.parseInt(value) - 1) * LIMIT;

                // Giới hạn phần tử cuối cùng để tránh vượt quá kích thước của danh sách
                int endIndex = Math.min(skip + LIMIT, danhSachPhanTrang.size());
                List<HoaDon> hoaDons = danhSachPhanTrang.subList(skip, endIndex);
                loadData(hoaDons); // Cập nhật dữ liệu hiển thị trong TableView
            });
        });
    }
    private int locDuLieuTheoTrangThai(String status) {
        danhSachHoaDon.clear();
        List<HoaDon> hoaDons = null;
        int soLuongBanGhi = 0;
        if (status.equalsIgnoreCase("all"))  {
            hoaDons = danhSachHoaDonDB.subList(0, Math.min(LIMIT, danhSachHoaDonDB.size()));
            soLuongBanGhi = danhSachHoaDonDB.size();
        }
        danhSachHoaDon.addAll(Objects.requireNonNull(hoaDons));
        tableHoaDon.refresh();
        tableHoaDon.setItems(danhSachHoaDon);
        return soLuongBanGhi;
    }
    private void loadCountPage(int soLuongBanGhi) {
        int soLuongTrang = (int) Math.ceil((double) soLuongBanGhi / LIMIT);
        for (int i = 0; i < soLuongTrang; i++) {
            Button button = ComponentUtil.createButton(String.valueOf(i + 1), 14);
            hBoxPage.getChildren().add(button);
        }
    }
    private void xuatExcel() throws IOException {
        Stage stage = (Stage) tableHoaDon.getScene().getWindow();
        danhSachHoaDon.clear();
        danhSachHoaDon.addAll(danhSachHoaDonDB);
        tableHoaDon.setItems(danhSachHoaDon);
        ExportExcelUtil.exportTableViewToExcel(tableHoaDon, stage);
        danhSachHoaDon.clear();
        loadData();
    }
    private void selectThangComboBox() {
        //Chọn tháng thì luôn chọn vào ngày hôm nay
        ngayComboBox.setValue(ngayTrongThang2());
    }
    private void selectNamCombox() {
        //Chọn năm thì lấy ra tháng ngày vào cho load lại ngày nếu là năm nhuận và tháng 2 mà chọn lớn hơn 29 thì reset lại
        String thangString = thangComboBox.getValue();
        ngayComboBox.setValue(ngayTrongThang2());
        thangComboBox.setValue(thangString);
    }
    private void filterNgayThangNam() throws RemoteException {
        boolean ngayCheck = ngayCheckbox.isSelected();
        boolean thangCheck = thangCheckbox.isSelected();
        boolean namCheck = namCheckbox.isSelected();
        if (!ngayCheck && !thangCheck && !namCheck) {
            showAlert("Chú ý", "Vui lòng chọn ít nhất một trong các mục: Ngày, Tháng hoặc Năm.", Alert.AlertType.INFORMATION);
        } else {
            int ngay = ngayCheck ? Integer.parseInt(ngayComboBox.getValue()) : -1;
            System.out.println("Ngày " + ngay );
            int thang = thangCheck ? Integer.parseInt(thangComboBox.getValue()) : -1;
            System.out.println("Tháng " + thang);
            int nam = namCheck ? Integer.parseInt(namComboBox.getValue()) : -1;
            System.out.println("Nam " + nam);
            EntityService<HoaDon> hoaDonService = RMIServiceLocator.getHoaDonService();
            List<HoaDon> hoaDons = hoaDonService.getDanhSachByDate(ngay, thang, nam, HoaDon.class, "ngayLap");
            loadData(hoaDons);
            hBoxPage.setVisible(false);
        }
    }
    // Hàm để tải dữ liệu vào TableView
    private void setValueTable() {
        maColumn.setCellValueFactory(new PropertyValueFactory<>("maHd"));
        ngayLapColumn.setCellValueFactory(new PropertyValueFactory<>("ngayLap"));
        hoTenHKColumn.setCellValueFactory(cellData -> {
            HoaDon hoaDon = cellData.getValue();
            String fullName = hoaDon.getHanhkhachByMaHk().getHoTenDem() + " " + hoaDon.getHanhkhachByMaHk().getTen();
            return new SimpleStringProperty(fullName);
        });
        tongTienColumn.setCellValueFactory(cellData -> {
            HoaDon hoaDon = cellData.getValue();
            double tongTien = hoaDon.getTongTien();
            String tongTienString = new DecimalFormat("#.### đ").format(tongTien);
            return new SimpleStringProperty(tongTienString);
        });
        hoTenNVColumn.setCellValueFactory(cellData -> {
            HoaDon hoaDon = cellData.getValue();
            String fullName = hoaDon.getNhanvienByMaNv().getHoTenDem() + " " + hoaDon.getNhanvienByMaNv().getTen();
            return new SimpleStringProperty(fullName);
        });
    }
    private void loadData() throws RemoteException {
        EntityService<HoaDon> hoaDonService = RMIServiceLocator.getHoaDonService();
        danhSachHoaDonDB = hoaDonService.getDanhSach("HoaDon.list", HoaDon.class);
        List<HoaDon> top15HoaDon = danhSachHoaDonDB.subList(0, Math.min(danhSachHoaDonDB.size(), LIMIT));
        danhSachHoaDon.addAll(top15HoaDon);
        tableHoaDon.setItems(danhSachHoaDon);
    }
    private void loadData(List<HoaDon> hoaDons) {
        danhSachHoaDon.clear();
        danhSachHoaDon.addAll(hoaDons);
        tableHoaDon.setItems(danhSachHoaDon);
    }
    private void huyChonDong() {
        tableHoaDon.getSelectionModel().clearSelection();
    }
    // Lọc theo ngay thang nam
    private void loadNgayComboBox() {
        String keyString = thangComboBox.getValue();
        int ngayTrongThang = 31;
        if (keyString != null) {
            int key = Integer.parseInt(keyString);
            switch (key) {
                case 1, 3, 5, 7, 8, 10, 12 -> {}
                case 2 -> {
                    String namString = namComboBox.getValue();
                    if (namString != null) {
                        int year = Integer.parseInt(namString);
                        ngayTrongThang = isNamNhuan(year) ? 29 : 28;
                    } else {
                        ngayTrongThang = 29;
                    }
                }
                default -> ngayTrongThang = 30;
            }
        }
        ngayComboBox.getItems().clear();
        for (int i = 1; i <= ngayTrongThang; i++) {
            ngayComboBox.getItems().add(String.valueOf(i));
        }
    }
    private void loadThangComboBox() {
        thangComboBox.getItems().clear();
        for(int i = 1; i <= 12; i++) {
            thangComboBox.getItems().add(String.valueOf(i));
        }
    }
    private void loadNamComboBox() {
        namComboBox.getItems().clear();
        for(int i = 2004; i <= LocalDate.now().getYear(); i++) {
            namComboBox.getItems().add(String.valueOf(i));
        }
    }
    private boolean isNamNhuan(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
    private void setDefaultComboBox() {
        ngayComboBox.setValue(String.valueOf(LocalDate.now().getDayOfMonth()));
        thangComboBox.setValue(String.valueOf(LocalDate.now().getMonthValue()));
        namComboBox.setValue(String.valueOf(LocalDate.now().getYear()));
    }
    // End lọc theo ngay thang nam
    private String ngayTrongThang2() {
        String ngayString = ngayComboBox.getValue();
        String thangString = thangComboBox.getValue();
        String namString = namComboBox.getValue();
        loadNgayComboBox();
        if (thangString.equalsIgnoreCase("2")) {
            int ngay = Integer.parseInt(ngayString);
            int nam = Integer.parseInt(namString);
            if (isNamNhuan(nam)) {
                if (ngay > 29) {
                    ngayString = "29";
                }
            } else {
                if (ngay > 28) {
                    ngayString = "28";
                }
            }
        }
        return ngayString;
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.show();
    }
}
