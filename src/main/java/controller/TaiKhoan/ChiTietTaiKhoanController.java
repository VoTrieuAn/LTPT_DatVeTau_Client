package controller.TaiKhoan;

import common.LoaiNhanVien;
import config.TrainTicketApplication;
import controller.Menu.MenuController;
import dao.TaiKhoanDAO;
import dao.impl.TaiKhoanDAOImpl;
import entity.NhanVien;
import entity.TaiKhoan;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import service.EntityService;
import service.impl.TaiKhoanServiceImpl;
import util.EmailSenderUlti;
import util.PasswordGeneratorUtil;
import util.PasswordUtil;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChiTietTaiKhoanController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button btnTaoLaiMatKhau;

    @FXML
    private Button btnTroLai;

    @FXML
    private Label chucVuNVLabel;

    @FXML
    private Label hoTenNVLabel;

    @FXML
    private Label labelDanhSachTaiKhoan;

    @FXML
    private Label maNVLabel;

    @FXML
    private Label maTKLabel;

    @FXML
    private Label ngayDNLabel;

    @FXML
    private Label ngayDXLabel;

    @FXML
    private Label ngaySDLabel;

    @FXML
    private Label sdtNVLabel;

    @FXML
    private Label tenTKLabel;

    private TaiKhoan taiKhoan;
    @FXML
    public void initialize() {
        Platform.runLater(() -> btnTroLai.requestFocus());
    }
    @FXML
    void controller(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnTroLai) {
            troLai();
        } else if (source == btnTaoLaiMatKhau) {
            taoLaiMatKhau();
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

    @FXML
    void mouseClicked(MouseEvent event) {
        Object source = event.getSource();
        if (source == labelDanhSachTaiKhoan) {
            troLai();
        }
    }
    // Chức năng
    private void troLai() {
        MenuController.instance.readyUI("TaiKhoan/TaiKhoan");
    }
    private void taoLaiMatKhau() {
        Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc chắn muốn tạo lại mật khẩu?");
        if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
            return;
        }
        if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
            String matKhauNew = PasswordGeneratorUtil.generatePassword();
            this.taiKhoan.setMatKhau(PasswordUtil.hashPassword(matKhauNew));
            this.taiKhoan.setNgaySuaDoi(Timestamp.valueOf(LocalDateTime.now()));
            try {
//                TrainTicketApplication.getInstance()
//                        .getDatabaseContext()
//                        .newEntityDAO(TaiKhoanDAO.class)
//                        .capNhat(this.taiKhoan);
                new TaiKhoanServiceImpl(new TaiKhoanDAOImpl()).capNhat(this.taiKhoan);
//                ExecutorService emailService = Executors.newSingleThreadExecutor();
//                emailService.submit(() -> {
//                    String content = "Tên đăng nhập là: " + this.taiKhoan.getNhanvienByMaNv().getMaNv() + "\n"
//                            + "Mật khẩu mới của bạn là: " + matKhauNew;
//                    EmailSenderUlti.sendEmail(this.taiKhoan.getNhanvienByMaNv().getEmail(), "Tài khoản và mật khẩu", content);
//                });
                ExecutorService emailService = Executors.newCachedThreadPool();

                emailService.submit(() -> {
                    String content = "Tên đăng nhập là: " + this.taiKhoan.getNhanvienByMaNv().getMaNv() + "\n"
                            + "Mật khẩu mới của bạn là: " + matKhauNew;
                    EmailSenderUlti.sendEmail(this.taiKhoan.getNhanvienByMaNv().getEmail(), "Tài khoản và mật khẩu", content);
                });
                emailService.shutdown();
                showAlert("Thông báo", "Tạo lại mật khẩu thành công!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Xãy ra lỗi", "Tạo lại mật khẩu thất bại!", Alert.AlertType.ERROR);
            }
        }
    }
    // End Chức Năng
    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setThongTinTaiKhoan(taiKhoan);
        setThongTinNhanVien(taiKhoan.getNhanvienByMaNv());
    }
    private void setThongTinTaiKhoan(TaiKhoan taiKhoan) {
        maTKLabel.setText(taiKhoan.getMaTk());
        tenTKLabel.setText(taiKhoan.getTenTaiKhoan());
        ngayDNLabel.setText(String.valueOf(taiKhoan.getNgayDangNhap()));
        ngayDXLabel.setText(String.valueOf(taiKhoan.getNgayDangXuat()));
        ngaySDLabel.setText(String.valueOf(taiKhoan.getNgaySuaDoi()));
    }
    private void setThongTinNhanVien(NhanVien nhanVien) {
        String chucVu = nhanVien.getLoaiNv() ==
                LoaiNhanVien.BAN_VE ? LoaiNhanVien.BAN_VE.getName() : LoaiNhanVien.QUAN_LI_BAN_VE.getName();
        String hoTen = nhanVien.getHoTenDem() + " " + nhanVien.getTen();
        maNVLabel.setText(nhanVien.getMaNv());
        hoTenNVLabel.setText(hoTen);
        sdtNVLabel.setText(nhanVien.getSdt());
        chucVuNVLabel.setText(chucVu);
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