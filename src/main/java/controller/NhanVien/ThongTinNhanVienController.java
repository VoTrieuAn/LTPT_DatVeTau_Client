package controller.NhanVien;

import controller.Menu.MenuController;
import entity.NhanVien;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ThongTinNhanVienController {
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button btnSua;

    @FXML
    private Button btnTroLai;

    @FXML
    private Label cccdLabel;

    @FXML
    private Label chucVuLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label gioiTinhLabel;

    @FXML
    private Label hoTeDemLabel;

    @FXML
    private Label labelDanhSachNhanVien;

    @FXML
    private Label maLabel;

    @FXML
    private Label ngaySinhLabel;

    @FXML
    private Label sdtLabel;

    @FXML
    private Label tenLabel;

    @FXML
    private Label trangThaiLabel;
    private NhanVien nhanVien;

    @FXML
    public void initialize() {
        Platform.runLater(() -> btnTroLai.requestFocus());
    }
    @FXML
    void controller(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnSua) {
            showThongTin();
        } else if (source == btnTroLai) {
            troLai();
        }
    }

    @FXML
    void mouseClicked(MouseEvent event) {
        Object source = event.getSource();
        if (source == labelDanhSachNhanVien) {
            troLai();
        }
    }
    @FXML
    void keyPressed(KeyEvent event) {
        Object source = event.getSource();
        if (source == anchorPane) {
            if (event.getCode() == KeyCode.ESCAPE) {
                troLai();
            }
        }
    }
    private void showThongTin() {
        if (nhanVien != null) {
            CapNhatNhanVienController capNhatNhanVienController =
                    MenuController.instance.readyUI("NhanVien/CapNhatNhanVien").getController();
            capNhatNhanVienController.setNhanVien(nhanVien);
        }
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
        hienThiThongTin(nhanVien);
    }
    public void hienThiThongTin(NhanVien nhanVien) {
        if (nhanVien != null) {
            maLabel.setText(nhanVien.getMaNv());
            hoTeDemLabel.setText(nhanVien.getHoTenDem());
            tenLabel.setText(nhanVien.getTen());
            gioiTinhLabel.setText(nhanVien.isGioiTinh() ? "Nữ" : "Nam");
            ngaySinhLabel.setText(String.valueOf(nhanVien.getNgaySinh().toLocalDate()));
            sdtLabel.setText(nhanVien.getSdt());
            emailLabel.setText(nhanVien.getEmail());
            cccdLabel.setText(nhanVien.getCccd());
            chucVuLabel.setText(nhanVien.getLoaiNv().getName());
            trangThaiLabel.setText(nhanVien.isTrangThai() ? "Đang làm việc" : "Ngừng làm việc");
        }
    }
    private void troLai() {
        MenuController.instance.readyUI("NhanVien/NhanVien");
    }
}