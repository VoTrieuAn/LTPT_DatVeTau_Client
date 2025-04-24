package controller.Menu;/* AnVo
                        
                        @author: Admin
                        Date: 01/12/2024
                        Time: 5:11 PM
                        
                        ProjectName: workspace.xml
                        */

import com.jfoenix.controls.JFXButton;
import config.TrainTicketApplication;
import controller.NhanVien.NhanVienController;
import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class MenuController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private MenuItem dangXuatButton;

    @FXML
    private MenuItem doiMatKhauButton;

    @FXML
    private Label hoTenNVLabel;

    @FXML
    private JFXButton khuyenMaiButton;

    @FXML
    private JFXButton lichTrinhButton;

    @FXML
    private Label maNVLabel;

    @FXML
    private JFXButton nhanVienButton;

    @FXML
    private JFXButton taiKhoanButton;

    @FXML
    private JFXButton thongKeButton;

    @FXML
    private MenuItem thongTinCaNhanButton;
    private TaiKhoan taiKhoan;

    public static MenuController instance;

    @FXML
    public void initialize() {
        instance = this;
    }

    @FXML
    void handleLichTrinhAction(ActionEvent event) {
        readyUI("LichTrinh");
    }

    @FXML
    void handleKhuyenMaiAction(ActionEvent event) {
        readyUI("KhuyenMai/KhuyenMai");
    }

    @FXML
    public void handleNhanVienAction(ActionEvent event) {
        readyUI("NhanVien/NhanVien");
    }

    @FXML
    private void handleThongKeAction(ActionEvent event) {
        readyUI("ThongKe");
    }

    @FXML
    void handleTaiKhoanAction(ActionEvent event) {
        readyUI("TaiKhoan/TaiKhoan");
    }

    @FXML
    void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == dangXuatButton) {
            dangXuat(source);
        } else if (source == thongTinCaNhanButton) {
            thongTinCaNhan();
        } else if (source == doiMatKhauButton) {
            doiMatKhau();
        }
    }

    // Chức năng
    private void dangXuat(Object source) {
        Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc muốn đăng xuất?");
        if (buttonType.isPresent() && buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
            return;
        }
        if (buttonType.isPresent() && buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
            // Cập nhật lại ngày giờ đăng nhập
            this.taiKhoan.setNgayDangXuat(Timestamp.valueOf(LocalDateTime.now()));
            TrainTicketApplication.getInstance()
                    .getDatabaseContext()
                    .newEntityDAO(TaiKhoanDAO.class)
                    .capNhat(taiKhoan);
            // Để Nhân chỉnh lại - load lên bị lỗi
            try {
                if (!(source instanceof MenuItem menuItem)) {
                    throw new IllegalArgumentException("Nooo");
                }
                Stage currentStage = (Stage) menuItem.getParentPopup().getOwnerWindow();
                currentStage.close();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/Login.fxml"));
                Parent loginRoot = loader.load();
                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(loginRoot));
                loginStage.setTitle("Đăng nhập");
                loginStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Để Nhân chỉnh lại - load lên bị lỗi
        }
    }

    private void thongTinCaNhan() {
        ThongTinCaNhanController thongTinCaNhanController = readyUI("Menu/ThongTinCaNhan").getController();
        thongTinCaNhanController.setTaiKhoan(this.taiKhoan);
    }

    private void doiMatKhau() {
        DoiMatKhauController doiMatKhauController = readyUI("Menu/DoiMatKhau").getController();
        if (taiKhoan != null) {
            doiMatKhauController.setTaiKhoan(taiKhoan);
        }
    }

    public FXMLLoader readyUI(String ui) {
        Parent root = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            fxmlLoader.setLocation(getClass().getResource("/view/fxml/" + ui + ".fxml"));
            root = fxmlLoader.load();
            borderPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader;
    }

    // End chức năng
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

    public void setThongTin(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        String hoTen = taiKhoan.getNhanvienByMaNv().getHoTenDem() + " " + taiKhoan.getNhanvienByMaNv().getTen();
        hoTenNVLabel.setText(hoTen);
        maNVLabel.setText(taiKhoan.getNhanvienByMaNv().getMaNv());
        thongTinCaNhan();
    }
}
