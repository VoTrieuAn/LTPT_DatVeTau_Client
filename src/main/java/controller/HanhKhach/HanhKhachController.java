package controller.HanhKhach;

import controller.Menu.MenuNhanVienController;
import entity.HanhKhach;
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
import service.impl.HanhKhachServiceImpl;
import util.ComponentUtil;
import util.ExportExcelUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.*;

public class HanhKhachController {
    @FXML
    private AnchorPane anchorPaneMain;

    @FXML
    private Button btnTatCa;

    @FXML
    private Button btnThem;

    @FXML
    private Button btnXuatExcel;

    @FXML
    private TableView<HanhKhach> tableHanhKhach;
    @FXML
    private TableColumn<HanhKhach, String> maHKColumn;

    @FXML
    private TableColumn<HanhKhach, String> hoTenDemColumn;

    @FXML
    private TableColumn<HanhKhach, String> tenColumn;

    @FXML
    private TableColumn<HanhKhach, Date> ngaySinhColumn;

    @FXML
    private TableColumn<HanhKhach, String> sdtColumn;

    @FXML
    private TableColumn<HanhKhach, String> emailColumn;
    @FXML
    private TableColumn<HanhKhach, String> cccdColumn;
    @FXML
    private TextField timKiemTextField;
    @FXML
    private HBox hBoxPage;
    private ObservableList<HanhKhach> danhSachHanhKhach = FXCollections.observableArrayList();
    private List<HanhKhach> danhSachHanhKhachDB;
    private final int LIMIT = 15;

    private String status = "all";
    @FXML
    private void initialize() throws RemoteException {
        setValueTable();
        loadData();
        phanTrang(danhSachHanhKhachDB.size());
    }
    @FXML
    void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == btnThem) {
            showThemHanhKhach();
        } else if (source == btnXuatExcel) {
            xuatExcel();
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
        } else if (source == tableHanhKhach) {
            showThongTin(event.getClickCount());
        } else if (source == anchorPaneMain) {
            huyChonDong();
        }
    }
    // Xuất Execl
    private void xuatExcel() throws IOException {
        Stage stage = (Stage) tableHanhKhach.getScene().getWindow();
        danhSachHanhKhach.clear();
        danhSachHanhKhach.addAll(danhSachHanhKhachDB);
        tableHanhKhach.setItems(danhSachHanhKhach);
        ExportExcelUtil.exportTableViewToExcel(tableHanhKhach, stage);
        danhSachHanhKhach.clear();
        loadData();
    }
    /**
     * @Param Các hàm chức năng
     * */
    // Tìm kiếm
    private void timKiem() {
        // Tạo một FilteredList dựa trên danh sách gốc
        // Thực hiện chổ này giúp nếu field rỗng thì nó sẽ load lại tất cả nhưng tôi muốn chỉ ở trang 1
        ObservableList<HanhKhach> newDanhSachHanhKhach = FXCollections.observableArrayList();
        newDanhSachHanhKhach.addAll(danhSachHanhKhachDB);
        hBoxPage.setVisible(false);
        FilteredList<HanhKhach> filteredData = new FilteredList<>(newDanhSachHanhKhach, p -> true);
        // Thêm bộ lọc cho ô tìm kiếm
        timKiemTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(hanhKhach -> {
                // Nếu ô tìm kiếm rỗng, hiển thị tất cả dữ liệu
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                // Kiểm tra từng cột xem có chứa giá trị tìm kiếm không
                if (hanhKhach.getMaHk().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (hanhKhach.getHoTenDem().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (hanhKhach.getTen().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (hanhKhach.getSdt().contains(lowerCaseFilter)) {
                    return true;
                } else if (hanhKhach.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (hanhKhach.getCccd().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else {
                    String hoTen = hanhKhach.getHoTenDem() + " " + hanhKhach.getTen();
                    if (hoTen.toLowerCase().contains(lowerCaseFilter)) {
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
        tableHanhKhach.setItems(filteredData);
    }
    public void showThemHanhKhach() {
        MenuNhanVienController.instance.readyUI("HanhKhach/ThemHanhKhach");
    }
    /**
     * @Param Hàm phân trang cho traạng thái và tác vụ thường
     * */
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
                    int endIndex = Math.min(skip + LIMIT, danhSachHanhKhachDB.size());
                    List<HanhKhach> hanhKhaches = danhSachHanhKhachDB.subList(skip, endIndex);
                    loadData(hanhKhaches);
                }
            });
        });
    }
    /**
     * @Param Hàm phân trang, nhận vào danh sách đã lọc (Cho tìm kiếm)
     */
    private void phanTrang(FilteredList<HanhKhach> danhSachPhanTrang) {
        hBoxPage.getChildren().clear();
        loadCountPage(danhSachPhanTrang.size());

        hBoxPage.getChildren().forEach(button -> {
            ((Button) button).setOnAction(event -> {
                String value = ((Button) button).getText();
                int skip = (Integer.parseInt(value) - 1) * LIMIT;

                // Giới hạn phần tử cuối cùng để tránh vượt quá kích thước của danh sách
                int endIndex = Math.min(skip + LIMIT, danhSachPhanTrang.size());
                List<HanhKhach> hanhKhaches = danhSachPhanTrang.subList(skip, endIndex);
                loadData(hanhKhaches); // Cập nhật dữ liệu hiển thị trong TableView
            });
        });
    }
    private int locDuLieuTheoTrangThai(String status) {
        danhSachHanhKhach.clear();
        List<HanhKhach> hanhKhaches = null;
        int soLuongBanGhi = 0;
        if (status.equalsIgnoreCase("all"))  {
            hanhKhaches = danhSachHanhKhachDB.subList(0, Math.min(LIMIT, danhSachHanhKhachDB.size()));
            soLuongBanGhi = danhSachHanhKhachDB.size();
        }
        danhSachHanhKhach.addAll(Objects.requireNonNull(hanhKhaches));
        tableHanhKhach.refresh();
        tableHanhKhach.setItems(danhSachHanhKhach);
        return soLuongBanGhi;
    }
    private void loadCountPage(int soLuongBanGhi) {
        int soLuongTrang = (int) Math.ceil((double) soLuongBanGhi / LIMIT);
        for (int i = 0; i < soLuongTrang; i++) {
            Button button = ComponentUtil.createButton(String.valueOf(i + 1), 14);
            hBoxPage.getChildren().add(button);
        }
    }
    // Hàm để tải dữ liệu vào TableView
    private void loadData() throws RemoteException {
        EntityService<HanhKhach> hanhKhachService = (EntityService<HanhKhach>) RMIServiceLocator.getHanhKhachService();
        danhSachHanhKhachDB = hanhKhachService.getDanhSach("HanhKhach.list",HanhKhach.class);
        List<HanhKhach> top15HanhKhach = danhSachHanhKhachDB.subList(0, Math.min(danhSachHanhKhachDB.size(), LIMIT));
        danhSachHanhKhach.addAll(top15HanhKhach);
        tableHanhKhach.setItems(danhSachHanhKhach);
    }
    private void loadData(List<HanhKhach> hanhKhaches) {
        danhSachHanhKhach.clear();
        danhSachHanhKhach.addAll(hanhKhaches);
        tableHanhKhach.setItems(danhSachHanhKhach);
    }
    private void setValueTable() {
        maHKColumn.setCellValueFactory(new PropertyValueFactory<>("maHk"));
        hoTenDemColumn.setCellValueFactory(new PropertyValueFactory<>("hoTenDem"));
        tenColumn.setCellValueFactory(new PropertyValueFactory<>("ten"));
        ngaySinhColumn.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        sdtColumn.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cccdColumn.setCellValueFactory(new PropertyValueFactory<>("cccd"));
    }
    private void huyChonDong() {
        tableHanhKhach.getSelectionModel().clearSelection();
    }
    private void showThongTin(int countClick) {
        if (countClick == 2) {
            HanhKhach hanhKhach = tableHanhKhach.getSelectionModel().getSelectedItem();
            if (hanhKhach != null) {
                ThongTinHanhKhachController thongTinHanhKhachController =
                        MenuNhanVienController.instance.readyUI("HanhKhach/ThongTinHanhKhach").getController();
                thongTinHanhKhachController.setHanhKhach(hanhKhach);
            }
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
}
