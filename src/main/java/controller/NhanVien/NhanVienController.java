package controller.NhanVien;/* AnVo

    @author: Admin
    Date: 29/10/2024
    Time: 8:24 PM

    ProjectName: TrainTicket
*/

import common.LoaiNhanVien;
import config.TrainTicketApplication;
import controller.Menu.MenuController;
import dao.NhanVienDAO;
import dao.impl.NhanVienDAOImpl;
import entity.NhanVien;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import rmi.RMIServiceLocator;
import service.EntityService;
import service.impl.NhanVienServiceImpl;
import util.ComponentUtil;
import util.ExportExcelUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

public class NhanVienController {
    @FXML
    private AnchorPane anchorPaneMain;
    @FXML
    private Button btnDangLamViec;

    @FXML
    private Button btnNgungLamViec;

    @FXML
    private Button btnTatCa;

    @FXML
    private Button btnThem;

    @FXML
    private Button btnXoaTatCa;
    @FXML
    private Button btnXuatExcel;

    @FXML
    private TextField timKiemTextField;
    @FXML
    private TableView<NhanVien> tableNhanVien;
    @FXML
    private TableColumn<NhanVien, String> maNVColumn;

    @FXML
    private TableColumn<NhanVien, String> hoTenDemColumn;

    @FXML
    private TableColumn<NhanVien, String> tenColumn;

    @FXML
    private TableColumn<NhanVien, String> gioiTinhColumn;

    @FXML
    private TableColumn<NhanVien, Date> ngaySinhColumn;

    @FXML
    private TableColumn<NhanVien, String> sdtColumn;

    @FXML
    private TableColumn<NhanVien, String> emailColumn;

    @FXML
    private TableColumn<NhanVien, String> chucVuColumn;
    @FXML
    private TableColumn<NhanVien, String> trangThaiColumn;
    @FXML
    private HBox hBoxPage;
    private ObservableList<NhanVien> danhSachNhanVien = FXCollections.observableArrayList();
    private List<NhanVien> danhSachNhanVienDB;
    private final int LIMIT = 15;
    private String status = "all";
    @FXML
    private void initialize() throws RemoteException {
        setValueTable();
        loadData();
        phanTrang(danhSachNhanVienDB.size());
    }
    @FXML
    private void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == btnThem) {
            showThemNhanVien();
        } else if (source == btnXuatExcel) {
            xuatExcel();
        } else if (source == btnXoaTatCa) {
            xoa();
        } else {
            int soLuongBanGhi = 0;
            if (source == btnTatCa) {
                status = "all";
                soLuongBanGhi = locDuLieuTheoTrangThai(status);
            } else if (source == btnDangLamViec) {
                status = "active";
                soLuongBanGhi = locDuLieuTheoTrangThai("active");
            } else if (source == btnNgungLamViec) {
                status = "inactive";
                soLuongBanGhi = locDuLieuTheoTrangThai(status);
            }
            hBoxPage.setVisible(true);
            phanTrang(soLuongBanGhi);
        }

    }
    @FXML
    void mouseClicked(MouseEvent event) throws IOException {
        Object source = event.getSource();
        if (source == timKiemTextField) {
            timKiem();
        } else if (source == tableNhanVien) {
            btnXoaTatCa.setDisable(false);
            showThongTin(event.getClickCount());
        } else if (source == anchorPaneMain) {
            huyChonDong();
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
    public void showThemNhanVien() {
        MenuController.instance.readyUI("NhanVien/ThemNhanVien");
    }
    //    Lọc theo trạng thái
    private int locDuLieuTheoTrangThai(String status) {
        danhSachNhanVien.clear();
        List<NhanVien> nhanViens = null;
        int soLuongBanGhi = 0;
        switch (status) {
            case "all" -> {
                nhanViens = danhSachNhanVienDB.subList(0, Math.min(LIMIT, danhSachNhanVienDB.size()));
                soLuongBanGhi = danhSachNhanVienDB.size();
            }
            case "active", "inactive" -> {
                // Lọc nhân viên theo trạng thái
                boolean isActive = status.equalsIgnoreCase("active");
                List<NhanVien> filteredList = danhSachNhanVienDB.stream()
                        .filter(nhanVien -> nhanVien.isTrangThai() == isActive)
                        .collect(Collectors.toList());

                // Kiểm tra kích thước trước khi gọi subList
                soLuongBanGhi = filteredList.size();
                if (soLuongBanGhi > 0) {
                    nhanViens = filteredList.subList(0, Math.min(LIMIT, soLuongBanGhi));
                }
            }
        }
        danhSachNhanVien.addAll(Objects.requireNonNull(nhanViens));
        tableNhanVien.refresh();
        tableNhanVien.setItems(danhSachNhanVien);
        return soLuongBanGhi;
    }
    // Tìm kiếm
    private void timKiem() {
        // Tạo một FilteredList dựa trên danh sách gốc
        // Thực hiện chổ này giúp nếu field rỗng thì nó sẽ load lại tất cả
        ObservableList<NhanVien> newDanhSachNhanVien = FXCollections.observableArrayList();
        newDanhSachNhanVien.addAll(danhSachNhanVienDB);
        hBoxPage.setVisible(false);
        FilteredList<NhanVien> filteredData = new FilteredList<>(newDanhSachNhanVien, p -> true);
        // Thêm bộ lọc cho ô tìm kiếm
        timKiemTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(nhanVien -> {
                // Nếu ô tìm kiếm rỗng, hiển thị tất cả dữ liệu
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                // Kiểm tra từng cột xem có chứa giá trị tìm kiếm không
                if (nhanVien.getMaNv().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getHoTenDem().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getTen().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getSdt().contains(lowerCaseFilter)) {
                    return true;
                } else if (nhanVien.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else {
                    String hoTen = nhanVien.getHoTenDem() + " " + nhanVien.getTen();
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
                if (hBoxPage.getChildren().get(0) instanceof Button firstButton) {
                    firstButton.fire(); // Kích hoạt sự kiện click
                }
            }
            phanTrang(filteredData);
        });
        // Đặt dữ liệu vào TableView
        tableNhanVien.setItems(filteredData);
    }
    // Phân trang
    // Xóa
    /**
     * @Param Hàm xóa 1 nhân viên
     * Note: Làm trước xóa một
     * */
    private void xoa() throws RemoteException {
        NhanVien nhanVien = tableNhanVien.getSelectionModel().getSelectedItem();
        EntityService<NhanVien> nhanVienService = RMIServiceLocator.getNhanVienService();
        if (nhanVien != null) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc chắn xóa?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                return;
            }
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                nhanVien.setDaXoa(true);
                boolean check = nhanVienService.capNhat(nhanVien);

                if (check) {
                    showAlert("Thông báo", "Xóa thành công!", Alert.AlertType.INFORMATION);
                    danhSachNhanVien.removeIf(NhanVien::isDaXoa);
                    tableNhanVien.refresh();
                } else  {
                    showAlert("Thông báo", "Xóa thất bại", Alert.AlertType.WARNING);
                }
            }
        }
    }
    // Xuất excel
    private void xuatExcel() throws IOException {
        Stage stage = (Stage) tableNhanVien.getScene().getWindow();
        danhSachNhanVien.clear();
        danhSachNhanVien.addAll(danhSachNhanVienDB);
        tableNhanVien.setItems(danhSachNhanVien);
        ExportExcelUtil.exportTableViewToExcel(tableNhanVien, stage);
        danhSachNhanVien.clear();
        loadData();
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
                    int endIndex = Math.min(skip + LIMIT, danhSachNhanVienDB.size());
                    List<NhanVien> nhanViens = danhSachNhanVienDB.subList(skip, endIndex);
                    loadData(nhanViens);
                } else {
                    List<NhanVien> nhanViens = danhSachNhanVienDB.stream()
                            .filter(nhanVien -> nhanVien.isTrangThai() == (status.equalsIgnoreCase("active")))
                            .collect(Collectors.toCollection(ArrayList::new));
                    int endIndex = Math.min(skip + LIMIT, nhanViens.size());
                    nhanViens = nhanViens.subList(skip, endIndex);
                    loadData(nhanViens);
                }
            });
        });
    }
    /**
     * @Param Hàm phân trang, nhận vào danh sách đã lọc (Cho tìm kiếm)
     */
    private void phanTrang(FilteredList<NhanVien> danhSachPhanTrang) {
        hBoxPage.getChildren().clear();
        loadCountPage(danhSachPhanTrang.size());

        hBoxPage.getChildren().forEach(button -> {
            ((Button) button).setOnAction(event -> {
                String value = ((Button) button).getText();
                int skip = (Integer.parseInt(value) - 1) * LIMIT;

                // Giới hạn phần tử cuối cùng để tránh vượt quá kích thước của danh sách
                int endIndex = Math.min(skip + LIMIT, danhSachPhanTrang.size());
                List<NhanVien> nhanViens = danhSachPhanTrang.subList(skip, endIndex);
                loadData(nhanViens); // Cập nhật dữ liệu hiển thị trong TableView
            });
        });
    }
    // Hàm để tải dữ liệu vào TableView
    private void loadData() throws RemoteException {
        Map<String, Object> filter = new HashMap<>();
        filter.put("daXoa", false);
        EntityService<NhanVien> nhanVienService = RMIServiceLocator.getNhanVienService();
        danhSachNhanVienDB = nhanVienService.getDanhSach(NhanVien.class, filter);
        List<NhanVien> top15NhanVien = danhSachNhanVienDB.subList(0, Math.min(danhSachNhanVienDB.size(), LIMIT));
        danhSachNhanVien.addAll(top15NhanVien);
        tableNhanVien.setItems(danhSachNhanVien);
    }
    private void loadData(List<NhanVien> nhanViens) {
        danhSachNhanVien.clear();
        danhSachNhanVien.addAll(nhanViens);
        tableNhanVien.setItems(danhSachNhanVien);
    }
    private void setValueTable() {
        maNVColumn.setCellValueFactory(new PropertyValueFactory<>("maNv"));
        hoTenDemColumn.setCellValueFactory(new PropertyValueFactory<>("hoTenDem"));
        tenColumn.setCellValueFactory(new PropertyValueFactory<>("ten"));
        gioiTinhColumn.setCellValueFactory(cellData -> {
            NhanVien nhanVien = cellData.getValue();
            String gioiTinh = nhanVien.isGioiTinh() ? "Nữ" : "Nam";
            return new SimpleStringProperty(gioiTinh);
        });
        ngaySinhColumn.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        sdtColumn.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        chucVuColumn.setCellValueFactory(cellData -> {
            NhanVien nhanVien = cellData.getValue();
            String chucVu = nhanVien.getLoaiNv() ==
                    LoaiNhanVien.BAN_VE ? LoaiNhanVien.BAN_VE.getName() : LoaiNhanVien.QUAN_LI_BAN_VE.getName();
            return new SimpleStringProperty(chucVu);
        });
        trangThaiColumn.setCellValueFactory(cellData -> {
            NhanVien nhanVien = cellData.getValue();
            String trangThai = nhanVien.isTrangThai() ? "Đang làm việc" : "Ngừng làm việc";
            return new SimpleStringProperty(trangThai);
        });
    }
    private void loadCountPage(int soLuongBanGhi) {
        int soLuongTrang = (int) Math.ceil((double) soLuongBanGhi / LIMIT);
        for (int i = 0; i < soLuongTrang; i++) {
            Button button = ComponentUtil.createButton(String.valueOf(i + 1), 14);
            hBoxPage.getChildren().add(button);
        }
    }
    private void huyChonDong() {
        tableNhanVien.getSelectionModel().clearSelection();
    }
    private void showThongTin(int countClick) {
        if (countClick == 2) {
            NhanVien nhanVien = tableNhanVien.getSelectionModel().getSelectedItem();
            if (nhanVien != null) {
                ThongTinNhanVienController thongTinNhanVienController =
                        MenuController.instance.readyUI("NhanVien/ThongTinNhanVien").getController();
                thongTinNhanVienController.setNhanVien(nhanVien);
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
