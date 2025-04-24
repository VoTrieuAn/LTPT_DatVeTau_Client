package controller;

import com.jfoenix.controls.JFXButton;
import config.TrainTicketApplication;
import dao.HoaDonDAO;
import dao.TaiKhoanDAO;
import dao.VeDAO;
import dao.impl.HoaDonDAOImpl;
import dao.impl.VeDAOImpl;
import entity.TaiKhoan;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class KetCaController {

    public ImageView userImage;
    @FXML
    private JFXButton Btn_1000;

    @FXML
    private JFXButton Btn_1500;

    @FXML
    private JFXButton Btn_500;

    @FXML
    private JFXButton Btn_XacNhan;

    @FXML
    private Label label_TienPhaiNop;

    @FXML
    private Label label_TongDoanhThu;

    @FXML
    private Label label_TongHoaDon;

    @FXML
    private Label label_TongSoVe;
    @FXML
    private Label text_TenNhanVien;

    @FXML
    private Label text_maNV;
    @FXML
    private TextField textField_TienNhan;
    private HoaDonDAO hoaDonDAO = new HoaDonDAOImpl();
    private VeDAO veDAO = new VeDAOImpl();
    private TaiKhoan taiKhoan; // Chưa sử dụng được

    // Định dạng số tiền với dấu phẩy phân cách hàng nghìn
    DecimalFormat decimalFormat = new DecimalFormat("#,###");
    @FXML
    public void initialize() {
        chonTienMacDinh();
        setupEnterKeyHandler();  // Thiết lập sự kiện Enter cho TextField
        Btn_XacNhan.setOnAction(event -> {
            Optional<ButtonType> chon = showAlertConfirm("Bạn có chắc chắn muốn kết ca?");
            if(chon.isPresent() && chon.get().getButtonData() == ButtonBar.ButtonData.YES){
                showAlert("Thông báo", "Kết ca thành công!", Alert.AlertType.INFORMATION);
                dangXuat();
            }
        });
    }

    private void chonTienMacDinh() {
        Btn_500.setOnAction(actionEvent -> {
            String formattedAmount = decimalFormat.format(500000);
            textField_TienNhan.setText(formattedAmount);
        });

        Btn_1000.setOnAction(actionEvent -> {
            String formattedAmount = decimalFormat.format(1000000);
            textField_TienNhan.setText(formattedAmount);
        });

        Btn_1500.setOnAction(actionEvent -> {
            String formattedAmount = decimalFormat.format(1500000);
            textField_TienNhan.setText(formattedAmount);
        });
    }
    //    NV240052
//    osw9du9w
    private void capNhatText(){
        String maNv = taiKhoan.getNhanvienByMaNv().getMaNv();
        String tenNv = taiKhoan.getNhanvienByMaNv().getHoTenDem() +" " + taiKhoan.getNhanvienByMaNv().getTen();
        if(!taiKhoan.getNhanvienByMaNv().isGioiTinh()){
            userImage.setImage(new Image("/view/images/man-user-svgrepo-com.png"));
        }else {
            userImage.setImage(new Image("/view/images/woman-svgrepo-com.png"));
        }
        text_maNV.setText(maNv);
        text_TenNhanVien.setText(tenNv);
        Date homNay = new Date(System.currentTimeMillis());
        Double doanhThu = hoaDonDAO.getTongDoanhThuTheoNgayVaNhanVien(homNay, maNv);
        Long soHoaDon = hoaDonDAO.countHoaDonTheoNgayVaNhanVien(homNay, maNv);
        Long soVe = veDAO.countVeBanTheoNgayVaNhanVien(homNay, maNv);

        label_TongDoanhThu.setText(decimalFormat.format(doanhThu));
        label_TongSoVe.setText(soVe + "");
        label_TongHoaDon.setText(soHoaDon + "");

        // Lấy giá trị tiền nhận từ textField_TienNhan và tính tiền nộp lại
        String tienNhanText = textField_TienNhan.getText().replaceAll("[^\\d,]", "");
        try {
            long tienNhan = Long.parseLong(tienNhanText.replace(",", ""));  // Chuyển đổi tiền nhận thành số
            Double tienPhaiNop = tienNhan + doanhThu;  // Tính tổng tiền phải nộp lại
            label_TienPhaiNop.setText(decimalFormat.format(tienPhaiNop));  // Hiển thị kết quả
        } catch (NumberFormatException e) {
            // Nếu giá trị trong TextField không hợp lệ
            System.out.println("Invalid input");
        }
    }

    // Thiết lập sự kiện nhấn Enter
    private void setupEnterKeyHandler() {
        textField_TienNhan.setOnAction(event -> {
            // Gọi hàm xử lý khi người dùng nhấn Enter
            onEnterPressed();
        });
    }

    // Hàm xử lý khi nhấn Enter
    private void onEnterPressed() {
        // Định dạng lại giá trị trong TextField
        String text = textField_TienNhan.getText().replaceAll("[^\\d,]", "");
        try {
            long value = Long.parseLong(text.replace(",", ""));
            String formattedValue = decimalFormat.format(value);
            textField_TienNhan.setText(formattedValue);  // Cập nhật giá trị đã được định dạng
        } catch (NumberFormatException e) {
            // Nếu giá trị không hợp lệ, không làm gì cả
            System.out.println("Invalid input");
        }
    }

    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        capNhatText();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.showAndWait();
    }

    private Optional<ButtonType> showAlertConfirm(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(content);
        ButtonType buttonLuu = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType buttonKhongLuu = new ButtonType("Không", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonLuu, buttonKhongLuu);
        return alert.showAndWait();
    }

    private void dangXuat() {
        try {
            // Lấy Stage hiện tại và đóng nó
            Stage currentStage = (Stage) Btn_XacNhan.getScene().getWindow(); // Sửa tùy vào nút xác nhận bạn dùng
            currentStage.close();

            // Tải giao diện màn hình đăng nhập
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/Login.fxml"));
            Parent loginRoot = loader.load();

            // Tạo Stage mới cho màn hình đăng nhập
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.setTitle("Đăng nhập");
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Debug nếu lỗi xảy ra
        }
    }
}
