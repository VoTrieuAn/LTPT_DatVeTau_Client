package controller.TaiKhoan;

import config.TrainTicketApplication;
import controller.Menu.MenuController;
import dao.TaiKhoanDAO;
import dao.impl.TaiKhoanDAOImpl;
import entity.NhanVien;
import entity.TaiKhoan;
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
import service.EntityService;
import service.impl.TaiKhoanServiceImpl;
import util.ComponentUtil;
import util.ExportExcelUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TaiKhoanController {

    @FXML
    private AnchorPane anchorPaneMain;

    @FXML
    private Button btnTatCa;

    @FXML
    private Button btnXuatExcel;

    @FXML
    private Button locButton;

    @FXML
    private HBox hBoxPage;

    @FXML
    private TableColumn<TaiKhoan, String> hoTenNVColumn;

    @FXML
    private TableColumn<TaiKhoan, String> maTKColumn;

    @FXML
    private TableColumn<TaiKhoan, Timestamp> ngayDNColumn;

    @FXML
    private TableColumn<TaiKhoan, Timestamp> ngayDXColumn;
    @FXML
    private TableColumn<TaiKhoan, Timestamp> ngaySuaDoiColumn;

    @FXML
    private TableView<TaiKhoan> tableTaiKhoan;

    @FXML
    private TableColumn<TaiKhoan, String> tenTKColumn;

    @FXML
    private TextField timKiemTextField;
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

    private ObservableList<TaiKhoan> danhSachTaiKhoan = FXCollections.observableArrayList();
    private List<TaiKhoan> danhSachTaiKhoanDB;
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
        phanTrang(danhSachTaiKhoanDB.size());
    }
    @FXML
    void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == btnXuatExcel) {
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
    void mouseClicked(MouseEvent event) {
        Object source = event.getSource();
        if (source == timKiemTextField) {
            timKiem();
        } else if (source == tableTaiKhoan) {
            showThongTin(event.getClickCount());
        }else if (source == anchorPaneMain) {
            huyChonDong();
        }
    }
    // Chức năng
    private int locDuLieuTheoTrangThai(String status) {
        danhSachTaiKhoan.clear();
        List<TaiKhoan> taiKhoans = null;
        int soLuongBanGhi = 0;
        if (status.equalsIgnoreCase("all"))  {
            taiKhoans = danhSachTaiKhoanDB.subList(0, Math.min(LIMIT, danhSachTaiKhoanDB.size()));
            soLuongBanGhi = danhSachTaiKhoanDB.size();
        }
        danhSachTaiKhoan.addAll(Objects.requireNonNull(taiKhoans));
        tableTaiKhoan.refresh();
        tableTaiKhoan.setItems(danhSachTaiKhoan);
        return soLuongBanGhi;
    }
    private void xuatExcel() throws IOException {
        Stage stage = (Stage) tableTaiKhoan.getScene().getWindow();
        danhSachTaiKhoan.clear();
        danhSachTaiKhoan.addAll(danhSachTaiKhoanDB);
        tableTaiKhoan.setItems(danhSachTaiKhoan);
        ExportExcelUtil.exportTableViewToExcel(tableTaiKhoan, stage);
        danhSachTaiKhoan.clear();
        loadData();
    }
    private void timKiem() {
        // Tạo một FilteredList dựa trên danh sách gốc
        // Thực hiện chổ này giúp nếu field rỗng thì nó sẽ load lại tất cả nhưng tôi muốn chỉ ở trang 1
        ObservableList<TaiKhoan> newDanhSachTaiKhoan = FXCollections.observableArrayList();
        newDanhSachTaiKhoan.addAll(danhSachTaiKhoanDB);
        hBoxPage.setVisible(false);
        FilteredList<TaiKhoan> filteredData = new FilteredList<>(newDanhSachTaiKhoan, p -> true);
        // Thêm bộ lọc cho ô tìm kiếm
        timKiemTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(taiKhoan -> {
                // Nếu ô tìm kiếm rỗng, hiển thị tất cả dữ liệu
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                String hoTenNv = taiKhoan.getNhanvienByMaNv().getHoTenDem() + " " + taiKhoan.getNhanvienByMaNv().getTen();
                // Kiểm tra từng cột xem có chứa giá trị tìm kiếm không
                if (taiKhoan.getMaTk().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (hoTenNv.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else {
                    if (taiKhoan.getTenTaiKhoan().toLowerCase().contains(lowerCaseFilter)) {
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
        tableTaiKhoan.setItems(filteredData);
    }
    private void huyChonDong() {
        tableTaiKhoan.getSelectionModel().clearSelection();
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
            EntityService<TaiKhoan> taiKhoanService = new TaiKhoanServiceImpl(new TaiKhoanDAOImpl());
//            List<TaiKhoan> taiKhoans = TrainTicketApplication.getInstance()
//                    .getDatabaseContext()
//                    .newEntityDAO(TaiKhoanDAO.class)
//                    .getDanhSachByDate(ngay, thang, nam, TaiKhoan.class, "ngayDangNhap");
            List<TaiKhoan> taiKhoans = taiKhoanService.getDanhSachByDate(ngay, thang, nam, TaiKhoan.class, "ngayDangNhap");
            loadData(taiKhoans);
            hBoxPage.setVisible(false);
        }
    }
    private void showThongTin(int countClick) {
        if (countClick == 2) {
            TaiKhoan taiKhoan = tableTaiKhoan.getSelectionModel().getSelectedItem();
            if (taiKhoan != null) {
                ChiTietTaiKhoanController chiTietTaiKhoanController =
                        MenuController.instance.readyUI("TaiKhoan/ChiTietTaiKhoan").getController();
                chiTietTaiKhoanController.setTaiKhoan(taiKhoan);
            }
        }
    }
    // End chức năng
    private void setValueTable() {
        maTKColumn.setCellValueFactory(new PropertyValueFactory<>("maTk"));
        tenTKColumn.setCellValueFactory(new PropertyValueFactory<>("tenTaiKhoan"));
        hoTenNVColumn.setCellValueFactory(cellData -> {
            TaiKhoan taiKhoan = cellData.getValue();
            NhanVien nhanVien = taiKhoan.getNhanvienByMaNv();
            String hoTen = nhanVien.getHoTenDem() + " " + nhanVien.getTen();
            return new SimpleStringProperty(hoTen);
        });
        ngayDNColumn.setCellValueFactory(new PropertyValueFactory<>("ngayDangNhap"));
        ngayDXColumn.setCellValueFactory(new PropertyValueFactory<>("ngayDangXuat"));
        ngaySuaDoiColumn.setCellValueFactory(new PropertyValueFactory<>("ngaySuaDoi"));
    }
    private void loadData() throws RemoteException {
        EntityService<TaiKhoan> taiKhoanService = new TaiKhoanServiceImpl(new TaiKhoanDAOImpl());
//        danhSachTaiKhoanDB = TrainTicketApplication.getInstance()
//                .getDatabaseContext()
//                .newEntityDAO(TaiKhoanDAO.class)
//                .getDanhSach("TaiKhoan.list", TaiKhoan.class);
        danhSachTaiKhoanDB = taiKhoanService.getDanhSach("TaiKhoan.list", TaiKhoan.class);
        List<TaiKhoan> filterTaiKhoan = danhSachTaiKhoanDB.stream()
                .filter(taiKhoan -> !taiKhoan.getNhanvienByMaNv().isDaXoa())
                .collect(Collectors.toCollection(ArrayList::new));
        List<TaiKhoan> top15HoaDon = filterTaiKhoan
                .subList(0, Math.min(filterTaiKhoan.size(), LIMIT));
        danhSachTaiKhoan.addAll(top15HoaDon);
        tableTaiKhoan.setItems(danhSachTaiKhoan);
    }
    // Phân trang
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
                    int endIndex = Math.min(skip + LIMIT, danhSachTaiKhoanDB.size());
                    List<TaiKhoan> taiKhoans = danhSachTaiKhoanDB.subList(skip, endIndex);
                    loadData(taiKhoans);
                }
            });
        });
    }
    private void phanTrang(FilteredList<TaiKhoan> danhSachPhanTrang) {
        hBoxPage.getChildren().clear();
        loadCountPage(danhSachPhanTrang.size());

        hBoxPage.getChildren().forEach(button -> {
            ((Button) button).setOnAction(event -> {
                String value = ((Button) button).getText();
                int skip = (Integer.parseInt(value) - 1) * LIMIT;

                // Giới hạn phần tử cuối cùng để tránh vượt quá kích thước của danh sách
                int endIndex = Math.min(skip + LIMIT, danhSachPhanTrang.size());
                List<TaiKhoan> taiKhoans = danhSachPhanTrang.subList(skip, endIndex);
                loadData(taiKhoans); // Cập nhật dữ liệu hiển thị trong TableView
            });
        });
    }
    private void loadCountPage(int soLuongBanGhi) {
        int soLuongTrang = (int) Math.ceil((double) soLuongBanGhi / LIMIT);
        for (int i = 0; i < soLuongTrang; i++) {
            Button button = ComponentUtil.createButton(String.valueOf(i + 1), 14);
            hBoxPage.getChildren().add(button);
        }
    }
    // End phân trang
    private void loadData(List<TaiKhoan> taiKhoans) {
        danhSachTaiKhoan.clear();
        danhSachTaiKhoan.addAll(taiKhoans);
        tableTaiKhoan.setItems(danhSachTaiKhoan);
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