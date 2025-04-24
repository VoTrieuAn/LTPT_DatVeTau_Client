package controller.NhanVien;

import common.LoaiNhanVien;
import config.DatabaseContext;
import config.TrainTicketApplication;
import controller.Menu.MenuController;
import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import dao.impl.NhanVienDAOImpl;
import dao.impl.TaiKhoanDAOImpl;
import entity.NhanVien;
import entity.TaiKhoan;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import rmi.RMIServiceLocator;
import service.EntityService;
import service.impl.NhanVienServiceImpl;
import service.impl.TaiKhoanServiceImpl;
import util.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThemNhanVienController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button btnThemMoi;

    @FXML
    private Button btnHuyBo;

    @FXML
    private Button btnLuuLai;

    @FXML
    private Button btnTroLai;

    @FXML
    private TableColumn<NhanVien, String> cccdColumn;

    @FXML
    private TableColumn<NhanVien, String> chucVuColumn;

    @FXML
    private TableColumn<NhanVien, String> emailColumn;

    @FXML
    private TableColumn<NhanVien, String> gioiTinhColumn;

    @FXML
    private TableColumn<NhanVien, String> hoTenDemColumn;

    @FXML
    private TableColumn<NhanVien, String> maNVColumn;

    @FXML
    private TableColumn<NhanVien, Date> ngaySinhColumn;

    @FXML
    private TableColumn<NhanVien, String> sdtColumn;
    @FXML
    private TableColumn<NhanVien, String> tenColumn;
    @FXML
    private TableColumn<NhanVien, String> trangThaiColumn;
    @FXML
    private TableView<NhanVien> tableThemNv;
    @FXML
    private Label labelDanhSachNv;
    @FXML
    private ComboBox<String> chucVuCombobox;
    @FXML
    private ComboBox<String> gioiTinhCombobox;
    @FXML
    private ComboBox<String> trangThaiCombobox;
    @FXML
    private TextField cccdTextField;
    @FXML
    private TextField diaChiTextField;
    @FXML
    private TextField soDienThoaiTextField;
    @FXML
    private TextField hotenDemTextField;
    @FXML
    private DatePicker ngaySinhTextField;
    @FXML
    private TextField tenTextField;
    private ObservableList<NhanVien> danhSachNhanVien = FXCollections.observableArrayList();
    //    Gọi sự kiện
    @FXML
    public void initialize() {
        loadGioiTinhCombobox();
        loadChucVuCombobox();
        loadTrangThaiCombobox();
        Platform.runLater(() -> hotenDemTextField.requestFocus());
    }
    @FXML
    void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == btnTroLai) {
            troLaiBtn();
        } else if (source == btnThemMoi) {
            themMoi();
        } else if (source == btnHuyBo) {
            huyBoDong();
        } else if (source == btnLuuLai) {
            luuLai();
        }
    }
    @FXML
    void keyPressed(KeyEvent event) throws IOException {
        Object source = event.getSource();
        if (source == anchorPane) {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                luuLai();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                troLaiBtn();
            } else if (event.getCode() == KeyCode.ENTER) {
                themMoi();
            } else if (event.getCode() == KeyCode.X) {
                huyBoDong();
            }
        }
    }
    @FXML
    void mouseClickedEvent(MouseEvent event) throws IOException {
        Object source = event.getSource();
        if (source == labelDanhSachNv) {
            troLaiBtn();
        } else if (source == tableThemNv) {
            hienThiThongTin(event.getClickCount());
        } else if (source == anchorPane) {
            huyChonDong();
        }
    }
    //    End gọi sự kiện
//    Sự kiện
    public void showNhanVien() {
        MenuController.instance.readyUI("NhanVien/NhanVien");
    }
    /**
     * @Param Hàm cho nút "Trở lại" hoặc phím "ESC"
     * Khi bấm vào nó sẽ trở lại trang danh sách
     * */
    private void troLaiBtn() throws RemoteException {
        xacNhanLuu();
    }
    /**
     * @Param Hàm cho label "Danh sách nhân viên"
     * Khi nhấn vào sẽ trở lại trang danh sách nhân viên
     * Khi bấm vào nó sẽ trở lại trang danh sách
     * */
    private void troVeDanhSach(Object source) throws RemoteException {
        xacNhanLuu();
    }
    private void themMoi() throws RemoteException {
        boolean kiemTra = load();
        setValueTable();
        if (danhSachNhanVien.size() != 0) {
            btnLuuLai.setDisable(false);
        }
        if (kiemTra) {
            resetAllFiled();
        }
        tableThemNv.getSelectionModel().clearSelection();
    }
    private void hienThiThongTin(int soLuongClicked) {
        if (soLuongClicked == 1) {
            int index = tableThemNv.getSelectionModel().getFocusedIndex();
            if (index >= 0) {
                NhanVien nhanVien = tableThemNv.getSelectionModel().getSelectedItem();
                hotenDemTextField.setText(nhanVien.getHoTenDem());
                tenTextField.setText(nhanVien.getTen());
                gioiTinhCombobox.setValue(nhanVien.isGioiTinh() ? gioiTinhCombobox.getItems().get(1) : gioiTinhCombobox.getItems().get(0));
                ngaySinhTextField.setValue(nhanVien.getNgaySinh().toLocalDate());
                soDienThoaiTextField.setText(nhanVien.getSdt());
                diaChiTextField.setText(nhanVien.getEmail());
                cccdTextField.setText(nhanVien.getCccd());
                chucVuCombobox.setValue(nhanVien.getLoaiNv().getName());
                trangThaiCombobox.setValue(nhanVien.isTrangThai() ? trangThaiCombobox.getItems().get(0) : trangThaiCombobox.getItems().get(1));
                btnHuyBo.setDisable(false);
            }
        }
    }

    private void huyChonDong() {
        NhanVien nhanVien = tableThemNv.getSelectionModel().getSelectedItem();
        if (nhanVien != null) {
            tableThemNv.getSelectionModel().clearSelection();
            resetAllFiled();
        }
    }
    private void huyBoDong() {
        int index = tableThemNv.getSelectionModel().getFocusedIndex();
        if (index >= 0) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc chắn muốn hủy?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                NhanVien nhanVien = tableThemNv.getSelectionModel().getSelectedItem();
                if (nhanVien != null) {
                    danhSachNhanVien.remove(nhanVien);
                    tableThemNv.refresh();
                }
                resetAllFiled();
            } else if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                tableThemNv.getSelectionModel().clearSelection();
            }
        }
    }
    private void luuLai() throws RemoteException {
        int index = tableThemNv.getSelectionModel().getFocusedIndex();
        EntityService<NhanVien> nhanVienService = RMIServiceLocator.getNhanVienService();
        EntityService<TaiKhoan> taiKhoanService = RMIServiceLocator.getTaiKhoanService();
        if (index >= 0) {
            NhanVien nhanVien = tableThemNv.getSelectionModel().getSelectedItem();
            if (nhanVien != null) {
                try {
                    nhanVienService.them(nhanVien);
                    String matKhau = PasswordGeneratorUtil.generatePassword();
                    String maTk = AccountCodeGeneratorUtil.generateAccountCode();
                    String tenTK = nhanVien.getMaNv();
                    String matKhauHash = PasswordUtil.hashPassword(matKhau);
                    TaiKhoan taiKhoan = new TaiKhoan(maTk, tenTK, matKhau, new Timestamp(System.currentTimeMillis()),nhanVien);
                    ExecutorService emailService = Executors.newSingleThreadExecutor();
                    emailService.submit(() -> {
                        String content = "Tên đăng nhập là: " + nhanVien.getMaNv() + "\n"
                                + "Mật khẩu của bạn là: " + matKhau;
                        EmailSenderUlti.sendEmail(nhanVien.getEmail(), "Tài khoản và mật khẩu", content);
                    });
                    emailService.shutdown();
                    showAlert("Thông báo", "Thêm nhân viên thành công!", Alert.AlertType.CONFIRMATION);
                    tableThemNv.getItems().remove(nhanVien);
                    tableThemNv.getSelectionModel().clearSelection();
                    resetAllFiled();
                } catch (Exception e) {
                    showAlert("Xãy ra lỗi", "Thêm nhân viên thất bại! Xãy ra lỗi", Alert.AlertType.ERROR);
                }
            }
        } else {
            try{
                // Thêm danh sách nhân viên
//                databaseContext.newEntityDAO(NhanVienDAO.class).themNhieu(danhSachNhanVien);
                nhanVienService.themNhieu(danhSachNhanVien);
                //Một list mật khẩu và tài khoản
                List<String> matKhaus = new ArrayList<>();
                List<TaiKhoan> taiKhoans = new ArrayList<>();
                danhSachNhanVien.forEach(nhanVien -> {
                    String matKhau = PasswordGeneratorUtil.generatePassword();
                    matKhaus.add(matKhau);
                    String maTk = AccountCodeGeneratorUtil.generateAccountCode();
                    String tenTK = nhanVien.getMaNv();
                    matKhau = PasswordUtil.hashPassword(matKhau);
                    TaiKhoan taiKhoan = taiKhoan = new TaiKhoan(maTk, tenTK, matKhau, new Timestamp(System.currentTimeMillis()),nhanVien);
                    taiKhoans.add(taiKhoan);
                });
                // Thêm toàn bộ tài khoản vào
//                databaseContext.newEntityDAO(TaiKhoanDAO.class).themNhieu(taiKhoans);
                taiKhoanService.themNhieu(taiKhoans);
                // Tạo ra 5 luồng để chuyển email
                ExecutorService emailService = Executors.newFixedThreadPool(5);
                for(NhanVien nhanVien : danhSachNhanVien) {
                    emailService.submit(() -> {
                        String content = "Tên đăng nhập là: " + nhanVien.getMaNv() + "\n"
                                + "Mật khẩu của bạn là: " + matKhaus.get(0);
                        EmailSenderUlti.sendEmail(nhanVien.getEmail(), "Tài khoản và mật khẩu", content);
                        matKhaus.remove(0);
                    });
                }
                emailService.shutdown();
                showAlert("Thông báo", "Lưu thành công", Alert.AlertType.CONFIRMATION);
                resetAllFiled();
            } catch (Exception e) {
                showAlert("Xãy ra lỗi", "Lưu thất bại! Xãy ra lỗi", Alert.AlertType.ERROR);
            }
            danhSachNhanVien.clear();
            tableThemNv.refresh();
        }
    }
    //    End Sự kiện
//    Loading
    private void loadGiaoDien(Object source) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NhanVienController.class.getResource("/view/fxml/NhanVien/NhanVien.fxml"));
        Stage stage = (Stage) ((Node) source).getScene().getWindow();
        Scene scene = new Scene(fxmlLoader.load(),  anchorPane.getWidth(),  anchorPane.getHeight());
        stage.setScene(scene);
        stage.show();
    }
    private void loadGioiTinhCombobox() {
        gioiTinhCombobox.setItems(FXCollections.observableArrayList(
                "Nam", "Nữ"
        ));
        gioiTinhCombobox.setValue("Nam");
    }
    private void loadChucVuCombobox() {
        chucVuCombobox.setItems(FXCollections.observableArrayList(
                LoaiNhanVien.BAN_VE.getName()
        ));
        chucVuCombobox.setValue(LoaiNhanVien.BAN_VE.getName());
    }
    private void loadTrangThaiCombobox() {
        trangThaiCombobox.setItems(FXCollections.observableArrayList(
                "Đang làm việc","Ngừng làm việc"
        ));

        trangThaiCombobox.setValue("Đang làm việc");
    }
    /**
     * @Param Hàm load NhanVien vào table
     * */
    private boolean load() throws RemoteException {
        NhanVien nhanVien = getNhanVienNew();
        if (nhanVien != null) {
            danhSachNhanVien.add(nhanVien);
            tableThemNv.setItems(danhSachNhanVien);
            return true;
        }
        return false;
    }
    //    End load
    private void xacNhanLuu() throws RemoteException {
        if (danhSachNhanVien.size() != 0) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn có muốn lưu không?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                showNhanVien();
            } else if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                // Lưu xong chuyển lại trang trước
                luuLai();
                showNhanVien();
            }
        } else {
            showNhanVien();
        }
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
        cccdColumn.setCellValueFactory(new PropertyValueFactory<>("cccd"));
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
    private NhanVien getNhanVienNew() throws RemoteException {
        String hoTenDem = hotenDemTextField.getText().trim();
        String ten = tenTextField.getText().trim();
        LocalDate ngaySinh = ngaySinhTextField.getValue();
        String sdt = soDienThoaiTextField.getText().trim();
        String email = diaChiTextField.getText().trim();
        String cccd = cccdTextField.getText().trim();

        EntityService<NhanVien> nhanVienService = RMIServiceLocator.getNhanVienService();
        if (hoTenDem.isEmpty()) {
            showAlert("Cảnh Báo", "Vui lòng nhập họ tên đệm!", Alert.AlertType.WARNING);
            hotenDemTextField.requestFocus();
            return null;
        }
        if (ten.isEmpty()) {
            showAlert("Cảnh Báo","Vui lòng nhập tên!", Alert.AlertType.WARNING);
            tenTextField.requestFocus();
            return null;
        }

        if (ngaySinh == null) {
            showAlert("Cảnh Báo","Vui lòng chọn ngày sinh!", Alert.AlertType.WARNING);
            ngaySinhTextField.requestFocus();
            return null;
        } else {
            LocalDate today = LocalDate.now();
            int age = Period.between(ngaySinh, today).getYears();

            if (age < 18) {
                showAlert("Cảnh Báo", "Nhân viên chưa đủ 18 tuổi!", Alert.AlertType.WARNING);
                return null;
            }
        }

        if (sdt.isEmpty()) {
            showAlert("Cảnh Báo","Vui lòng nhập số điện thoại!", Alert.AlertType.WARNING);
            soDienThoaiTextField.requestFocus();
            return null;
        } else {
            String regexPhone = "(84|0)[987531][0-9]{8,9}\\b";
            if (!sdt.matches(regexPhone)) {
                showAlert("Cảnh Báo", "Số điện thoại không hơp lệ", Alert.AlertType.WARNING);
                soDienThoaiTextField.requestFocus();
                return null;
            }
        }

        if (email.isEmpty()) {
            showAlert("Cảnh Báo", "Vui lòng nhập email!", Alert.AlertType.WARNING);
            diaChiTextField.requestFocus();
            return null;
        } else {
            if (!email.matches("^[a-zA-Z][a-zA-Z0-9]+@[a-zA-Z]+(\\.[a-zA-Z]+)+$")) {
                showAlert("Cảnh Báo", "Email không hợp lệ!", Alert.AlertType.WARNING);
                diaChiTextField.requestFocus();
                return null;
            }
        }

        if (cccd.isEmpty()) {
            showAlert("Cảnh Báo", "Vui lòng nhập CCCD!", Alert.AlertType.WARNING);
            cccdTextField.requestFocus();
            return null;
        } else {
            if (!cccd.matches("[0-9]{12}")) {
                showAlert("Cảnh Báo", "Số CCCD không hợp lệ!", Alert.AlertType.WARNING);
                cccdTextField.requestFocus();
                return null;
            } else {
                Map<String, Object> filter = new HashMap<>();
                filter.put("cccd", cccd);
//                List<NhanVien> nhanViens = databaseContext
//                        .newEntityDAO(NhanVienDAO.class)
//                        .getDanhSach(NhanVien.class, filter);
                List<NhanVien> nhanViens = nhanVienService.getDanhSach(NhanVien.class, filter);
                if (!nhanViens.isEmpty()) {
                    showAlert("Cảnh Báo", "Số CCCD đã tồn tại!", Alert.AlertType.WARNING);
                    return null;
                }
            }
        }
        String maNv = EmployeeCodeGeneratorUtil.generateEmployeeCode();
        boolean gioiTinh = gioiTinhCombobox.getValue().contentEquals("Nữ");
        LoaiNhanVien chucVu = chucVuCombobox.getValue().contentEquals(LoaiNhanVien.BAN_VE.getName()) ? LoaiNhanVien.BAN_VE : LoaiNhanVien.QUAN_LI_BAN_VE;
        boolean trangThai = trangThaiCombobox.getValue().contentEquals("Đang làm việc");
        return new NhanVien(maNv, hoTenDem, ten, gioiTinh, cccd, sdt, email, Date.valueOf(ngaySinh), chucVu, trangThai, new Timestamp(System.currentTimeMillis()));
    }
    private void resetAllFiled() {
        hotenDemTextField.setText("");
        tenTextField.setText("");
        gioiTinhCombobox.setValue(gioiTinhCombobox.getItems().get(0));
        ngaySinhTextField.setValue(null);
        soDienThoaiTextField.setText("");
        diaChiTextField.setText("");
        cccdTextField.setText("");
        chucVuCombobox.setValue("");
        chucVuCombobox.setValue(chucVuCombobox.getItems().get(0));
        trangThaiCombobox.setValue(trangThaiCombobox.getItems().get(0));
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