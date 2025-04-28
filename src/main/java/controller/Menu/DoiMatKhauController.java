package controller.Menu;

import common.LoaiNhanVien;
import entity.TaiKhoan;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import rmi.RMIServiceLocator;
import util.PasswordUtil;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DoiMatKhauController {

    @FXML
    private Button btnLuuLai;

    @FXML
    private PasswordField mkCuTextField;

    @FXML
    private PasswordField mkMoiTextField;

    @FXML
    private PasswordField mkNhapLaiTextField;

    private TaiKhoan taiKhoan;

    @FXML
    void controller(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnLuuLai) {
            luuLai();
        }
    }
    private void luuLai()  {
        String matKhauCu = mkCuTextField.getText();
        if (this.taiKhoan.kiemTraDoiMatKhau(matKhauCu)) {
            String matKhauMoi = mkMoiTextField.getText();
            String matKhauMoiNhapLai = mkNhapLaiTextField.getText();
            if (matKhauMoi.equalsIgnoreCase(matKhauMoiNhapLai)) {
                this.taiKhoan.setMatKhau(PasswordUtil.hashPassword(matKhauMoi));
                this.taiKhoan.setNgaySuaDoi(Timestamp.valueOf(LocalDateTime.now()));
                try {
                    RMIServiceLocator.getTaiKhoanService().capNhat(this.taiKhoan);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                showAlert("Thông báo", "Đổi mật khẩu thành công", Alert.AlertType.INFORMATION);
                if (this.taiKhoan.getNhanvienByMaNv().getLoaiNv() == LoaiNhanVien.QUAN_LI_BAN_VE) {
                    MenuController.instance.readyUI("Menu/ThongTinCaNhan");
                } else {
                    MenuNhanVienController.instance.readyUI("Menu/ThongTinCaNhan");
                }
            } else {
                showAlert("Chú ý", "Mật khẩu nhập lại không trùng khớp", Alert.AlertType.WARNING);
            }
        } else {
            showAlert("Chú ý", "Mật khẩu cũ trùng khớp", Alert.AlertType.WARNING);
        }
    }

    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.show();
    }
}
