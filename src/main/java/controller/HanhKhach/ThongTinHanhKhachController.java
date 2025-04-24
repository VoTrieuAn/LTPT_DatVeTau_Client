package controller.HanhKhach;

import controller.Menu.MenuNhanVienController;
import entity.HanhKhach;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ThongTinHanhKhachController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button btnSua;

    @FXML
    private Button btnTroLai;

    @FXML
    private Label cccdLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label hoTeDemLabel;

    @FXML
    private Label labelDanhSachhanhKhach;

    @FXML
    private Label maLabel;

    @FXML
    private Label ngaySinhLabel;

    @FXML
    private Label sdtLabel;

    @FXML
    private Label tenLabel;

    private HanhKhach hanhKhach;

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
        if (source == labelDanhSachhanhKhach) {
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
        if (hanhKhach != null) {
            CapNhatHanhKhachController capNhatHanhKhachController =
                    MenuNhanVienController.instance.readyUI("HanhKhach/CapNhatHanhKhach").getController();
            capNhatHanhKhachController.setHanhKhach(hanhKhach);
        }
    }

    public void setHanhKhach(HanhKhach hanhKhach) {
        this.hanhKhach = hanhKhach;
        hienThiThongTin(hanhKhach);
    }
    public void hienThiThongTin(HanhKhach hanhKhach) {
        if (hanhKhach != null) {
            maLabel.setText(hanhKhach.getMaHk());
            hoTeDemLabel.setText(hanhKhach.getHoTenDem());
            tenLabel.setText(hanhKhach.getTen());
            ngaySinhLabel.setText(String.valueOf(hanhKhach.getNgaySinh().toLocalDate()));
            sdtLabel.setText(hanhKhach.getSdt());
            emailLabel.setText(hanhKhach.getEmail());
            cccdLabel.setText(hanhKhach.getCccd());
        }
    }
    private void troLai() {
        MenuNhanVienController.instance.readyUI("HanhKhach/HanhKhach");
    }
}
