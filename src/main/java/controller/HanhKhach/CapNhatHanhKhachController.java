package controller.HanhKhach;

import controller.Menu.MenuNhanVienController;
import entity.HanhKhach;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import rmi.RMIServiceLocator;
import service.EntityService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

public class CapNhatHanhKhachController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button btnHoanTac;

    @FXML
    private Button btnLuuLai;

    @FXML
    private Button btnTroLai;

    @FXML
    private TextField cccdTextField;

    @FXML
    private TextField diaChiTextField;

    @FXML
    private TextField hotenDemTextField;

    @FXML
    private Label labelDanhSachHanhKhach;

    @FXML
    private Label labelThongtTinChiTiet;

    @FXML
    private TextField maTextField;

    @FXML
    private DatePicker ngaySinhTextField;

    @FXML
    private TextField soDienThoaiTextField;

    @FXML
    private TextField tenTextField;
    private HanhKhach hanhKhach;
    @FXML
    public void initialize() {
        Platform.runLater(() -> hotenDemTextField.requestFocus());
    }
    @FXML
    void controller(ActionEvent event) throws RemoteException {
        Object source = event.getSource();
        if (source == btnTroLai) {
            xacNhanLuu("ThongTinHanhKhach");
        } else if (source == btnLuuLai) {
            luuLai();
        } else if (source == btnHoanTac) {
            hoanTac();
        }
    }
    @FXML
    void mouseClicked(MouseEvent event) throws IOException {
        Object source = event.getSource();
        if (source == labelDanhSachHanhKhach) {
            xacNhanLuu("HanhKhach/HanhKhach");
        } else if (source == labelThongtTinChiTiet) {
            xacNhanLuu("HanhKhach/ThongTinHanhKhach");
        }
    }

    @FXML
    void keyPressed(KeyEvent event) throws RemoteException {
        Object source = event.getSource();
        if (source == anchorPane) {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                luuLai();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                troLai("ThongTinHanhKhach");
            } else if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                hoanTac();
            }
        }
    }
    //    Sự kiện
    private void luuLai() throws RemoteException {
        HanhKhach hanhKhachNew = getHanhKhachNew();
        if (hanhKhachNew != null) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc chắn thay đổi?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                return;
            }
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                EntityService<HanhKhach> hanhKhacService = (EntityService<HanhKhach>) RMIServiceLocator.getHanhKhachService();
                boolean check = hanhKhacService.capNhat(hanhKhachNew);
                if (check) {
                    showAlert("Thông báo", "Cập nhật thành công!", Alert.AlertType.INFORMATION);
                    this.hanhKhach = hanhKhachNew;
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
        EntityService<HanhKhach> hanhKhacService = (EntityService<HanhKhach>) RMIServiceLocator.getHanhKhachService();
        boolean check = hanhKhacService.capNhat(hanhKhach);
        if (check) {
            showAlert("Thông báo", "Hoàn tác thành công!", Alert.AlertType.INFORMATION);
            hienThiThongTin(hanhKhach);
        } else  {
            showAlert("Thông báo", "Hoàn tác thất bại!", Alert.AlertType.WARNING);
        }
    }
    private void xacNhanLuu(String ui) throws RemoteException {
        HanhKhach hanhKhachNew = getHanhKhachNew();
        if (hanhKhachNew != null && !hanhKhachNew.equals(hanhKhach)) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn có muốn lưu cập nhật?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                troLai(ui);
            } else if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                // Lưu xong chuyển lại trang trước
                EntityService<HanhKhach> hanhKhacService = (EntityService<HanhKhach>) RMIServiceLocator.getHanhKhachService();
//                boolean check = TrainTicketApplication.getInstance()
//                        .getDatabaseContext()
//                        .newEntityDAO(HanhKhachDAO.class)
//                        .capNhat(hanhKhachNew);
                boolean check = hanhKhacService.capNhat(hanhKhachNew);
                if (check) {
                    showAlert("Thông báo", "Cập nhật thành công!", Alert.AlertType.INFORMATION);
                    this.hanhKhach = hanhKhachNew;
                    troLai(ui);
                } else  {
                    showAlert("Thông báo", "Cập nhật thất bại", Alert.AlertType.WARNING);
                }
            }
        } else {
            troLai(ui);
        }
    }
    private void troLai(String ui) {
        if (hanhKhach != null && ui.equalsIgnoreCase("HanhKhach/ThongTinHanhKhach")) {
            ThongTinHanhKhachController thongTinHanhKhachController =
                    MenuNhanVienController.instance.readyUI(ui).getController();
            thongTinHanhKhachController.setHanhKhach(hanhKhach);
        } else {
            MenuNhanVienController.instance.readyUI("HanhKhach/HanhKhach");
        }
    }
    public void hienThiThongTin(HanhKhach hanhKhach) {
        if (hanhKhach != null) {
            maTextField.setText(hanhKhach.getMaHk());
            hotenDemTextField.setText(hanhKhach.getHoTenDem());
            tenTextField.setText(hanhKhach.getTen());
            ngaySinhTextField.setValue(hanhKhach.getNgaySinh().toLocalDate());
            soDienThoaiTextField.setText(hanhKhach.getSdt());
            diaChiTextField.setText(hanhKhach.getEmail());
            cccdTextField.setText(hanhKhach.getCccd());
        }
    }
    public void setHanhKhach(HanhKhach hanhKhach) {
        this.hanhKhach = hanhKhach;
        hienThiThongTin(hanhKhach);
    }
    private HanhKhach getHanhKhachNew() {
        String maHk = hanhKhach.getMaHk();
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
        return new HanhKhach(maHk, hoTenDem, ten, cccd, sdt, email, Date.valueOf(ngaySinh), new Timestamp(System.currentTimeMillis()));
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
