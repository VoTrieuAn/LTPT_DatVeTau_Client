package controller.BanVe;

import controller.Menu.MenuNhanVienController;
import entity.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import rmi.RMIServiceLocator;
import service.HanhKhachService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

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

    private String ui = "ThemHanhKhach";
    private HanhKhach hanhKhach;
    private ObservableList<Ve> veList = FXCollections.observableArrayList();
    private final Map<ToaTau, Set<Ghe>> gheDaChonMapDi = new HashMap<>();
    private final Map<ToaTau, Set<Ghe>> gheDaChonMapVe = new HashMap<>();
    private String gaDi;
    private String gaDen;
    private LocalDate ngayDiLD;
    private LocalDate ngayVeLD;
    private boolean isMotChieu;
    private NhanVien nhanVien;
    private final HanhKhachService hanhKhachService = RMIServiceLocator.getHanhKhachService();

    @FXML
    public void initialize() {
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public void setVeList(List<Ve> veListParam,
                          Map<ToaTau, Set<Ghe>> gheDaChonMapDiParam,
                          Map<ToaTau, Set<Ghe>> gheDaChonMapVeParam,
                          String gaDiParam,
                          String gaDenParam,
                          LocalDate ngayDiLDParam,
                          LocalDate ngayVeLDParam,
                          boolean isMotChieuParam) {
        this.veList.setAll(veListParam);
        this.gheDaChonMapDi.clear();
        this.gheDaChonMapDi.putAll(gheDaChonMapDiParam);
        this.gheDaChonMapVe.clear();
        this.gheDaChonMapVe.putAll(gheDaChonMapVeParam);
        this.gaDi = gaDiParam;
        this.gaDen = gaDenParam;
        this.ngayDiLD = ngayDiLDParam;
        this.ngayVeLD = ngayVeLDParam;
        this.isMotChieu = isMotChieuParam;
    }

    @FXML
    void controller(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnTroLai) {
            if (!labelDanhSachHanhKhach.getText().equalsIgnoreCase("Danh sách hành khách")) {
                xacNhanLuu("BanVe/BanVe");
            } else {
                xacNhanLuu("HanhKhach/HanhKhach");
            }
        } else if (source == btnLuuLai) {
            luuLai();
        }
    }

    @FXML
    void mouseClicked(MouseEvent event) {
        Object source = event.getSource();
        if (source == labelDanhSachHanhKhach) {
            if (!labelDanhSachHanhKhach.getText().equalsIgnoreCase("Danh sách hành khách")) {
                xacNhanLuu("BanVe/BanVe");
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
                    xacNhanLuu("BanVe/BanVe");
                } else {
                    xacNhanLuu("HanhKhach/HanhKhach");
                }
            }
        }
    }

    private void xacNhanLuu(String ui) {
        if (isFormFilled()) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn muốn lưu thông tin không?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                troLai(ui);
            } else if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                HanhKhach hanhKhachNew = saveHanhKhach();
                if (hanhKhachNew != null) {
                    this.hanhKhach = hanhKhachNew;
                    showAlert("Thông báo", "Thêm thành công!", Alert.AlertType.INFORMATION);
                    troLai(ui);
                } else {
                    showAlert("Thông báo", "Thêm thất bại", Alert.AlertType.WARNING);
                }
            }
        } else {
            troLai(ui);
        }
    }

    private void luuLai() {
        HanhKhach hanhKhachNew = saveHanhKhach();
        if (hanhKhachNew != null) {
            this.hanhKhach = hanhKhachNew;
            showAlert("Thông báo", "Thêm thành công!", Alert.AlertType.INFORMATION);
            if (!ui.equalsIgnoreCase("ThemHanhKhach")) {
                troLai(ui);
            }
            resetAllFields();
        } else {
            showAlert("Lỗi", "Xảy ra lỗi", Alert.AlertType.ERROR);
        }
    }

    private HanhKhach saveHanhKhach() {
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

    private void troLai(String ui) {
        if (ui.equalsIgnoreCase("BanVe/BanVe")) {
            BanVeController banVeController = MenuNhanVienController.instance.readyUI(ui).getController();
            banVeController.setHanhKhach(hanhKhach);
            banVeController.setVeList(new ArrayList<>(veList), gheDaChonMapDi, gheDaChonMapVe, gaDi, gaDen, ngayDiLD, ngayVeLD, isMotChieu);
            banVeController.setNhanVien(nhanVien);
            for (Object ve : veList) {
                System.out.println(ve.toString());
            }
        } else {
            MenuNhanVienController.instance.readyUI(ui);
        }
    }

    private boolean isFormFilled() {
        return !hotenDemTextField.getText().trim().isEmpty() ||
                !tenTextField.getText().trim().isEmpty() ||
                ngaySinhTextField.getValue() != null ||
                !soDienThoaiTextField.getText().trim().isEmpty() ||
                !diaChiTextField.getText().trim().isEmpty() ||
                !cccdTextField.getText().trim().isEmpty();
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

    private void resetAllFields() {
        hotenDemTextField.setText("");
        tenTextField.setText("");
        ngaySinhTextField.setValue(null);
        soDienThoaiTextField.setText("");
        diaChiTextField.setText("");
        cccdTextField.setText("");
    }

    public void setUrl(String nameUrl, String currentPage) {
        labelDanhSachHanhKhach.setText(nameUrl);
        this.ui = currentPage;
    }
}