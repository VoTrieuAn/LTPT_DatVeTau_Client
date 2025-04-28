package controller.HanhKhach;

import controller.Menu.MenuNhanVienController;
import controller.BanVe.TimVeController;
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
import service.HanhKhachService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

public class ThemHanhKhachController {
    @FXML
    private AnchorPane anchorPane;
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
    private DatePicker ngaySinhTextField;

    @FXML
    private TextField soDienThoaiTextField;

    @FXML
    private TextField tenTextField;

    private final HanhKhachService hanhKhachService = RMIServiceLocator.getHanhKhachService();
    private String ui = "ThemHanhKhach";
    private HanhKhach hanhKhach;

    @FXML
    public void initialize() {
        Platform.runLater(() -> hotenDemTextField.requestFocus());
    }

    @FXML
    void controller(ActionEvent event) throws RemoteException {
        Object source = event.getSource();
        if (source == btnTroLai) {
            if (!labelDanhSachHanhKhach.getText().equalsIgnoreCase("Danh sách hành khách")) {
                xacNhanLuu("TimVe"); // Chổ này Phương Sửa lại cho hợp lý
            } else {
                xacNhanLuu("HanhKhach/HanhKhach");
            }
        } else if (source == btnLuuLai) {
            luuLai();
        }
    }

    @FXML
    void mouseClicked(MouseEvent event) throws RemoteException {
        Object source = event.getSource();
        if (source == labelDanhSachHanhKhach) {
            if (!labelDanhSachHanhKhach.getText().equalsIgnoreCase("Danh sách hành khách")) {
                xacNhanLuu("TimVe"); // Chổ này Phương Sửa lại cho hợp lý
            } else {
                xacNhanLuu("HanhKhach/HanhKhach");
            }
        }
    }

    @FXML
    void keyPressed(KeyEvent event) throws IOException {
        Object source = event.getSource();
        if (source == anchorPane) {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                luuLai();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                if (!labelDanhSachHanhKhach.getText().equalsIgnoreCase("Danh sách hành khách")) {
                    xacNhanLuu("TimVe"); // Chổ này Phương Sửa lại cho hợp lý
                } else {
                    xacNhanLuu("HanhKhach/HanhKhach");
                }
            }
        }
    }

    // Chức năng
    private void xacNhanLuu(String ui) throws RemoteException {
        HanhKhach hanhKhachNew = getHanhKhachNew();
        if (hanhKhachNew != null) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn muốn lưu thông tin không?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                troLai(ui);
            } else if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                EntityService<HanhKhach> hanhKhachService = (EntityService<HanhKhach>) RMIServiceLocator.getHanhKhachService();
                boolean check = hanhKhachService.them(hanhKhachNew);
                if (check) {
                    showAlert("Thông báo", "Thêm hành khách thành công!", Alert.AlertType.INFORMATION);
                    this.hanhKhach = hanhKhachNew;
                    troLai(ui);
                } else {
                    showAlert("Thông báo", "Thêm hành khách thất bại!", Alert.AlertType.WARNING);
                }
            }
        } else {
            troLai(ui);
        }
    }

    private void luuLai() throws RemoteException {
        HanhKhach hanhKhachNew = getHanhKhachNew();
        try {
            if (hanhKhachNew != null) {
                EntityService<HanhKhach> hanhKhachService = (EntityService<HanhKhach>) RMIServiceLocator.getHanhKhachService();
                boolean check = hanhKhachService.them(hanhKhachNew);
                if (check) {
                    showAlert("Thông báo", "Thêm hành khách thành công!", Alert.AlertType.INFORMATION);
                    this.hanhKhach = hanhKhachNew;
                    if (!ui.equalsIgnoreCase("ThemHanhKhach")) {
                        troLai(ui);
                    }
                    resetAllFiled();
                } else {
                    showAlert("Thông báo", "Thêm hành khách thất bại!", Alert.AlertType.WARNING);
                }
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Xãy ra lỗi", Alert.AlertType.ERROR);
        }
    }

    // Ở đây là Phương sử lý
    private void troLai(String ui) {
        if (ui.equalsIgnoreCase("TimVe")) {
            TimVeController timVeController = MenuNhanVienController.instance.readyUI(ui).getController();
            // Gọi hàm set ra để trả thông tin hành khách về form
            // timVeController.setHanhKhach(hanhKhach);
        } else {
            MenuNhanVienController.instance.readyUI(ui);
        }
    }

    private HanhKhach getHanhKhachNew() throws RemoteException {
        String hoTenDem = hotenDemTextField.getText().trim();
        String ten = tenTextField.getText().trim();
        LocalDate ngaySinh = ngaySinhTextField.getValue();
        String sdt = soDienThoaiTextField.getText().trim();
        String email = diaChiTextField.getText().trim();
        String cccd = cccdTextField.getText().trim();

        HanhKhach hanhKhachNew = null;
        try {
            hanhKhachNew = hanhKhachService.createHanhKhach(hoTenDem, ten, ngaySinh, sdt, email, cccd);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        if (hanhKhachNew == null) {
            showValidationError(hoTenDem, ten, ngaySinh, sdt, email, cccd);
        }
        return hanhKhachNew;
    }
    private void showValidationError(String hoTenDem, String ten, LocalDate ngaySinh, String sdt, String email, String cccd) {
        if (hoTenDem.isEmpty()) {
            showAlert("Cảnh Báo", "Vui lòng nhập họ tên đệm!", Alert.AlertType.WARNING);
            hotenDemTextField.requestFocus();
        } else if (ten.isEmpty()) {
            showAlert("Cảnh Báo", "Vui lòng nhập tên!", Alert.AlertType.WARNING);
            tenTextField.requestFocus();
        } else if (ngaySinh == null) {
            showAlert("Cảnh Báo", "Vui lòng chọn ngày sinh!", Alert.AlertType.WARNING);
            ngaySinhTextField.requestFocus();
        } else if (Period.between(ngaySinh, LocalDate.now()).getYears() < 18) {
            showAlert("Cảnh Báo", "Nhân viên chưa đủ 18 tuổi!", Alert.AlertType.WARNING);
        } else if (sdt.isEmpty() || !sdt.matches("(84|0)[987531][0-9]{8,9}\\b")) {
            showAlert("Cảnh Báo", "Số điện thoại không hợp lệ", Alert.AlertType.WARNING);
            soDienThoaiTextField.requestFocus();
        } else if (email.isEmpty() || !email.matches("^[a-zA-Z][a-zA-Z0-9]+@[a-zA-Z]+(\\.[a-zA-Z]+)+$")) {
            showAlert("Cảnh Báo", "Email không hợp lệ!", Alert.AlertType.WARNING);
            diaChiTextField.requestFocus();
        } else if (cccd.isEmpty() || !cccd.matches("[0-9]{12}")) {
            showAlert("Cảnh Báo", "Số CCCD không hợp lệ!", Alert.AlertType.WARNING);
            cccdTextField.requestFocus();
        } else {
            try {
                if (hanhKhachService.isCccdExists(cccd)) {
                    showAlert("Cảnh Báo", "Số CCCD đã tồn tại!", Alert.AlertType.WARNING);
                    cccdTextField.requestFocus();
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
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

    private void resetAllFiled() {
        hotenDemTextField.setText("");
        tenTextField.setText("");
        ngaySinhTextField.setValue(null);
        soDienThoaiTextField.setText("");
        diaChiTextField.setText("");
        cccdTextField.setText("");
    }

    // Dùng để lấy ra sau đó setText lại cho đường dẫn
    public void setUrl(String nameUrl, String currentPage) {
        labelDanhSachHanhKhach.setText(nameUrl);
        this.ui = currentPage;
    }
}
