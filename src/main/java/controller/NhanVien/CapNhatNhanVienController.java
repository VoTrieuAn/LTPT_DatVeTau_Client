package controller.NhanVien;/* AnVo
    
    @author: Admin
    Date: 02/11/2024
    Time: 11:31 PM
    
    ProjectName: workspace.xml 
*/
import common.LoaiNhanVien;
import config.TrainTicketApplication;
import controller.Menu.MenuController;
import dao.NhanVienDAO;
import dao.impl.NhanVienDAOImpl;
import entity.NhanVien;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import rmi.RMIServiceLocator;
import service.EntityService;
import service.impl.NhanVienServiceImpl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

public class CapNhatNhanVienController {

    @FXML
    private Button btnHoanTac;
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button btnLuuLai;

    @FXML
    private Button btnTroLai;
    @FXML
    private TextField cccdTextField;
    @FXML
    private TextField maNvTextField;

    @FXML
    private ComboBox<String> chucVuCombobox;

    @FXML
    private TextField diaChiTextField;

    @FXML
    private ComboBox<String> gioiTinhCombobox;

    @FXML
    private TextField hotenDemTextField;

    @FXML
    private Label labelDanhSachNhanVien;

    @FXML
    private Label labelThongtTinChiTiet;

    @FXML
    private DatePicker ngaySinhTextField;

    @FXML
    private TextField soDienThoaiTextField;

    @FXML
    private TextField tenTextField;

    @FXML
    private ComboBox<String> trangThaiCombobox;
    private NhanVien nhanVien;

    @FXML
    void controller(ActionEvent event) throws RemoteException {
        Object source = event.getSource();
        if (source == btnTroLai) {
            xacNhanLuu("ThongTinNhanVien");
        } else if (source == btnLuuLai) {
            luuLai();
        } else if (source == btnHoanTac) {
            hoanTac();
        }
    }
    @FXML
    void mouseClicked(MouseEvent event) throws IOException {
        Object source = event.getSource();
        if (source == labelDanhSachNhanVien) {
            xacNhanLuu("NhanVien/NhanVien");
        } else if (source == labelThongtTinChiTiet) {
            xacNhanLuu("NhanVien/ThongTinNhanVien");
        }
    }

    @FXML
    void keyPressed(KeyEvent event) throws RemoteException {
        Object source = event.getSource();
        if (source == anchorPane) {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                luuLai();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                troLai("ThongTinNhanVien");
            } else if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                hoanTac();
            }
        }
    }
    @FXML
    public void initialize() {
        loadGioiTinhCombobox();
        loadChucVuCombobox();
        loadTrangThaiCombobox();
        ngaySinhTextField.getEditor().setFont(Font.font(14));
        Platform.runLater(() -> hotenDemTextField.requestFocus());
    }
    //    Sự kiện
    private void luuLai() throws RemoteException {
        NhanVien nhanVienNew = getNhanVienNew();
        if (nhanVienNew != null) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc chắn thay đổi?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                return;
            }
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                EntityService<NhanVien> nhanVienService = RMIServiceLocator.getNhanVienService();
                boolean check = nhanVienService.capNhat(nhanVienNew);
                if (check) {
                    showAlert("Thông báo", "Cập nhật thành công!", Alert.AlertType.INFORMATION);
                    this.nhanVien = nhanVienNew;
                    troLai("ThongTinNhanVien");
                } else  {
                    showAlert("Thông báo", "Cập nhật thất bại", Alert.AlertType.WARNING);
                }
            }
        } else {
            showAlert("Lỗi", "Xãy ra lỗi", Alert.AlertType.ERROR);
        }
    }
    private void hoanTac() throws RemoteException {
        Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc chắn muốn hoàn tác?");
        if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
            return;
        }
        EntityService<NhanVien> nhanVienService = RMIServiceLocator.getNhanVienService();
        boolean check = nhanVienService.capNhat(nhanVien);
        if (check) {
            showAlert("Thông báo", "Hoàn tác thành công!", Alert.AlertType.INFORMATION);
            hienThiThongTin(nhanVien);
        } else  {
            showAlert("Thông báo", "Hoàn tác thất bại!", Alert.AlertType.WARNING);
        }
    }

    //  End Sự kiện
    private NhanVien getNhanVienNew() {
        String maNv = nhanVien.getMaNv().trim();
        String hoTenDem = hotenDemTextField.getText().trim();
        String ten = tenTextField.getText().trim();
        LocalDate ngaySinh = ngaySinhTextField.getValue();
        String sdt = soDienThoaiTextField.getText().trim();
        String email = diaChiTextField.getText().trim();
        String cccd = cccdTextField.getText().trim();
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
            String regexPhone = "(84|0)[35789][0-9]{8,9}\\b";
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
            }
        }
        boolean gioiTinh = gioiTinhCombobox.getValue().contentEquals("Nữ");
        LoaiNhanVien chucVu = chucVuCombobox.getValue().contentEquals(LoaiNhanVien.BAN_VE.getName()) ? LoaiNhanVien.BAN_VE : LoaiNhanVien.QUAN_LI_BAN_VE;
        boolean trangThai = trangThaiCombobox.getValue().contentEquals("Đang làm việc");
        return new NhanVien(maNv, hoTenDem, ten, gioiTinh, cccd, sdt, email, Date.valueOf(ngaySinh), chucVu, trangThai, new Timestamp(System.currentTimeMillis()));
    }
    private void loadGioiTinhCombobox() {
        gioiTinhCombobox.setItems(FXCollections.observableArrayList(
                "Nam", "Nữ"
        ));
        gioiTinhCombobox.setValue("Nam");
        gioiTinhCombobox.getEditor().setFont(Font.font(14));
    }
    private void loadChucVuCombobox() {
        chucVuCombobox.setItems(FXCollections.observableArrayList(
                LoaiNhanVien.BAN_VE.getName(), LoaiNhanVien.QUAN_LI_BAN_VE.getName()
        ));
        chucVuCombobox.setValue(LoaiNhanVien.BAN_VE.getName());
        chucVuCombobox.getEditor().setFont(Font.font(14));
    }

    private void loadTrangThaiCombobox() {
        trangThaiCombobox.setItems(FXCollections.observableArrayList(
                "Đang làm việc","Ngừng làm việc"
        ));
        trangThaiCombobox.setValue("Đang làm việc");
        trangThaiCombobox.getEditor().setFont(Font.font(14));
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
    public void hienThiThongTin(NhanVien nhanVien) {
        if (nhanVien != null) {
            maNvTextField.setText(nhanVien.getMaNv());
            hotenDemTextField.setText(nhanVien.getHoTenDem());
            tenTextField.setText(nhanVien.getTen());
            gioiTinhCombobox.setValue(nhanVien.isGioiTinh() ? gioiTinhCombobox.getItems().get(1) : gioiTinhCombobox.getItems().get(0));
            ngaySinhTextField.setValue(nhanVien.getNgaySinh().toLocalDate());
            soDienThoaiTextField.setText(nhanVien.getSdt());
            diaChiTextField.setText(nhanVien.getEmail());
            cccdTextField.setText(nhanVien.getCccd());
            chucVuCombobox.setValue(nhanVien.getLoaiNv().getName());
            trangThaiCombobox.setValue(nhanVien.isTrangThai() ? trangThaiCombobox.getItems().get(0) : trangThaiCombobox.getItems().get(1));
        }
    }
    private void troLai(String ui) {
        if (nhanVien != null && ui.equalsIgnoreCase("NhanVien/ThongTinNhanVien")) {
            ThongTinNhanVienController thongTinNhanVienController =
                    MenuController.instance.readyUI(ui).getController();
            thongTinNhanVienController.setNhanVien(nhanVien);
        } else {
            MenuController.instance.readyUI("NhanVien/NhanVien");
        }
    }
    private void xacNhanLuu(String ui) throws RemoteException {
        NhanVien nhanVienNew = getNhanVienNew();
        if (nhanVienNew != null && !nhanVienNew.equals(nhanVien)) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn lưu cập nhật không?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                troLai(ui);
            } else if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                EntityService<NhanVien> nhanVienService = RMIServiceLocator.getNhanVienService();
                boolean check = nhanVienService.capNhat(nhanVienNew);
                if (check) {
                    showAlert("Thông báo", "Cập nhật thành công!", Alert.AlertType.INFORMATION);
                    this.nhanVien = nhanVienNew;
                    troLai(ui);
                } else  {
                    showAlert("Thông báo", "Cập nhật thất bại", Alert.AlertType.WARNING);
                }
            }
        } else {
            troLai(ui);
        }
    }
    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        hienThiThongTin(nhanVien);
    }

}
