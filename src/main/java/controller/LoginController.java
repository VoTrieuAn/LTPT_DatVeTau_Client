package controller;

import common.LoaiNhanVien;
import controller.Menu.MenuNhanVienController;
import controller.Menu.MenuController;
import entity.TaiKhoan;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import rmi.RMIServiceLocator;
import util.PasswordUtil;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginController {
    public AnchorPane login_Page;
    public Button buttonLogin;
    public ImageView ImageLoginButton;
    private TaiKhoan taiKhoan;

    @FXML
    public void initialize() {
        PromtText();
        buttonHover();
    }

    @FXML
    private ImageView trainImageView;
    @FXML
    private ImageView whiteOverlay;

    public void buttonHover() {
        buttonLogin.setOnMouseEntered(event -> {
            ImageLoginButton.setFitWidth(150);
            ImageLoginButton.setFitHeight(50);
            ImageLoginButton.setTranslateX(-5);
            ImageLoginButton.setTranslateY(-12.5);
        });

        buttonLogin.setOnMouseExited(event -> {
            ImageLoginButton.setFitWidth(140);
            ImageLoginButton.setFitHeight(25);
            ImageLoginButton.setTranslateX(0);
            ImageLoginButton.setTranslateY(0);
        });
    }

    // Sự kiện hiệu ứng nút đăng nhập-----------------------------
    @FXML
    public void login(ActionEvent actionEvent) {
        buttonLogin.setVisible(false);
        trainImageView.setVisible(true);
        trainImageView.setLayoutX(buttonLogin.getLayoutX());
        trainImageView.setLayoutY(buttonLogin.getLayoutY());
        TranslateTransition transition = new TranslateTransition(Duration.seconds(1.5), trainImageView);
        transition.setByX(600);
        transition.setOnFinished(event -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1), whiteOverlay);
            scaleTransition.setOnFinished(e -> {
                buttonLogin.setVisible(true);
                try {
                    // Lấy ra thông tin Tài Khoàn
                    String tenDangNhap = textField_username.getText().trim();
                    String matKhau = textField_password.getText().trim();
                    if (tenDangNhap.isBlank() || matKhau.isBlank()) {
                        showAlert("Cảnh báo", "Vui lòng nhập đầy đủ tài khoản mật khẩu", Alert.AlertType.WARNING);
                    } else {
                        Map<String, Object> filter = new HashMap<>();
//                        filter.put("tenTaiKhoan", tenDangNhap);
//                        List<TaiKhoan> taiKhoans = TrainTicketApplication.getInstance()
//                                .getDatabaseContext()
//                                .newEntityDAO(TaiKhoanDAO.class)
//                                .getDanhSach(TaiKhoan.class, filter);
                        List<TaiKhoan> taiKhoans = RMIServiceLocator.getTaiKhoanService().getDanhSach(TaiKhoan.class, filter);
                        if (!taiKhoans.isEmpty()) {
                            this.taiKhoan = taiKhoans.get(0);
                            if (taiKhoan.kiemTraDangNhap(tenDangNhap, matKhau)) {
                                // Cập nhật lại ngày giờ đăng nhập
                                this.taiKhoan.setNgayDangNhap(Timestamp.valueOf(LocalDateTime.now()));
//                                TrainTicketApplication.getInstance()
//                                        .getDatabaseContext()
//                                        .newEntityDAO(TaiKhoanDAO.class)
//                                        .capNhat(taiKhoan);
                                RMIServiceLocator.getTaiKhoanService().capNhat(taiKhoan);
                                // Phân quyền
                                if (taiKhoan.getNhanvienByMaNv().getLoaiNv() == LoaiNhanVien.QUAN_LI_BAN_VE) {
                                    FXMLLoader fxmlLoader = new FXMLLoader(
                                            getClass().getResource("/view/fxml/Menu/Menu.fxml"));
                                    Parent root = fxmlLoader.load();
                                    Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                                    Scene scene = new Scene(root);

                                    MenuController menuController = fxmlLoader.getController();
                                    menuController.setThongTin(taiKhoan);

                                    stage.setScene(scene);
                                    stage.setMaximized(true);
                                    stage.show();
                                } else {
                                    FXMLLoader fxmlLoader = new FXMLLoader(
                                            getClass().getResource("/view/fxml/Menu/MenuNhanVien.fxml"));
                                    Parent root = fxmlLoader.load();
                                    Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                                    Scene scene = new Scene(root);

                                    MenuNhanVienController menuNhanVienController = fxmlLoader.getController();
                                    menuNhanVienController.setThongTin(taiKhoan);

                                    stage.setScene(scene);
                                    stage.setMaximized(true);
                                    stage.show();
                                }
                                showAlert("Thông báo", "Đăng nhập thành công!", Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("Cảnh báo", "Tên đăng nhập hoặc mật khẩu không chính xác! Vui lòng thử lại",
                                        Alert.AlertType.WARNING);
                            }
                        } else {
                            showAlert("Cảnh báo", "Tên đăng nhập hoặc mật khẩu không chính xác! Vui lòng thử lại",
                                    Alert.AlertType.WARNING);
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            whiteOverlay.setOpacity(1);
            scaleTransition.setFromX(1);
            scaleTransition.setFromY(1);
            scaleTransition.setToX(3);
            scaleTransition.setToY(3);

            scaleTransition.play();
        });
        transition.play();
    }

    // Sự kiện hiệu ứng ô textField-----------------------------
    @FXML
    private Label label_username;
    @FXML
    private TextField textField_username;
    @FXML
    private Label label_password;
    @FXML
    private PasswordField textField_password;

    public void PromtText() {
        textField_username.focusedProperty().addListener((observable, oldValuePos, newValuePos) -> {
            TranslateTransition transition = new TranslateTransition(Duration.millis(200), label_username);
            if (newValuePos) {
                transition.setToY(72 - label_username.getLayoutY() - 10);
            } else {
                if (textField_username.getText().isEmpty()) {
                    transition.setToY(92 - label_username.getLayoutY());
                }
            }
            transition.play();
        });
        textField_password.focusedProperty().addListener((observable, oldValuePos, newValuePos) -> {
            TranslateTransition transition = new TranslateTransition(Duration.millis(200), label_password);
            if (newValuePos) {
                transition.setToY(162 - label_password.getLayoutY() - 15);
            } else {
                if (textField_password.getText().isEmpty()) {
                    transition.setToY(177 - label_password.getLayoutY());
                }
            }
            transition.play();
        });
        label_username.setOnMouseClicked((MouseEvent event) -> {
            textField_username.requestFocus(); // Focus vào TextField
        });

        // Thêm sự kiện cho Label Password để focus vào TextField
        label_password.setOnMouseClicked((MouseEvent event) -> {
            textField_password.requestFocus(); // Focus vào TextField
        });

    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.show();
    }
}