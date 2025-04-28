package controller.HoaDon;

import common.LoaiHanhKhach;
import common.LoaiNhanVien;
import controller.Menu.MenuNhanVienController;
import entity.HanhKhach;
import entity.HoaDon;
import entity.NhanVien;
import entity.Ve;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;
import rmi.RMIServiceLocator;
import service.EntityService;
import util.BarcodeUtil;

import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChiTietHoaDonController {
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Button btnTroLai;
    @FXML
    private Button btnInHoaDon;
    @FXML
    private Label cccdHKLabel;

    @FXML
    private TableColumn<List<Ve>, String> donGiaColumn;

    @FXML
    private TableColumn<List<Ve>, String> donViColumn;

    @FXML
    private Label emailHKLabel;

    @FXML
    private Label hoTenHKLabel;

    @FXML
    private Label labelDanhSachHoaDon;

    @FXML
    private Label loaiNVLabel;

    @FXML
    private Label maHDLabel;

    @FXML
    private Label ngayLapHoaDonLabel;

    @FXML
    private Label nvThucHienLabel;

    @FXML
    private Label sdtHKLabel;

    @FXML
    private Label sdtNVHDLabel;

    @FXML
    private TableColumn<List<Ve>, String> soLuongColumn;

    @FXML
    private TableColumn<List<Ve>, String> sttColumn;

    @FXML
    private TableView<List<Ve>> tableHoaDon;

    @FXML
    private TableColumn<List<Ve>, String> tenDichVuColumn;

    @FXML
    private TableColumn<List<Ve>, String> tgtgtColumn;

    @FXML
    private Label trangThaiLabel;

    @FXML
    private Label tongTienLabel;
    @FXML
    private TableColumn<List<Ve>, String> ttCoThueColumn;
    private ObservableList<List<Ve>> danhSachHoaDon = FXCollections.observableArrayList();
    private HoaDon hoaDon;
    @FXML
    public void initialize() {
        Platform.runLater(() -> btnTroLai.requestFocus());
    }
    @FXML
    void controller(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnTroLai) {
            troLai();
        }
        else if (source == btnInHoaDon) {
            generateReport(this.hoaDon);
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
        if (source == labelDanhSachHoaDon) {
            troLai();
        } else if (source == anchorPane) {
            huyChonDong();
        }
    }
    // Bắt sự kiện
    private void troLai() {
        MenuNhanVienController.instance.readyUI("HoaDon/HoaDon");
    }
    private void huyChonDong() {
        tableHoaDon.getSelectionModel().clearSelection();
    }
    private void setValueTable() {
        sttColumn.setCellValueFactory(cellData -> {
            int soTT = tableHoaDon.getItems().indexOf(cellData.getValue()) + 1;
            return new ReadOnlyObjectWrapper<>(soTT).asString();
        });

        tenDichVuColumn.setCellValueFactory(cellData -> {
            List<Ve> ves = cellData.getValue();
            if (ves != null && !ves.isEmpty()) {
                Ve ve = ves.get(0);
                String tenDichVu = "Vé tàu cao tốc" + " - "
                        + "tuyến "
                        + ve.getLichtrinhByMaLt().getGaKhoiHanh()
                        + "-"
                        + ve.getLichtrinhByMaLt().getGaKetThuc()
                        + ", "
                        + ve.getNgayMua()
                        + ". Loại vé "
                        + ve.getLoaiKh().getName();
                return new SimpleStringProperty(tenDichVu);
            } else {
                System.out.println("Rỗng rồi");
            }
            return new SimpleStringProperty("");
        });

        donViColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Vé"));

        soLuongColumn.setCellValueFactory(cellData -> {
            List<Ve> ves = cellData.getValue();
            if (ves != null && !ves.isEmpty()) {
                int soLuong = ves.size();
                return new ReadOnlyObjectWrapper<>(soLuong).asString();
            }
            return new ReadOnlyObjectWrapper<>(0).asString();
        });

        donGiaColumn.setCellValueFactory(cellData -> {
            List<Ve> ves = cellData.getValue();
            if (ves != null && !ves.isEmpty()) {
                Ve ve = ves.get(0);

                double donGia = ve.getGiaVe();
                String donGiaString = new DecimalFormat("#.### đ").format(donGia);
                return new SimpleStringProperty(donGiaString);
            } else {
                System.out.println("Rỗng rồi");
            }
            return new SimpleStringProperty(new DecimalFormat("#.### đ").format(0.0));
        });

        tgtgtColumn.setCellValueFactory(cellData -> {
            List<Ve> ves = cellData.getValue();
            if (ves != null && !ves.isEmpty()) {
                Ve ve = ves.get(0);
                return new SimpleStringProperty(String.format("%.0f", ve.getThueSuatGtgt() * 100) + " %");
            }
            return new SimpleStringProperty("0 %");
        });

        ttCoThueColumn.setCellValueFactory(cellData -> {
            List<Ve> ves = cellData.getValue();
            if (ves != null && !ves.isEmpty()) {
                Ve ve = ves.get(0);
                double thanhTien = ve.getGiaVe() * ves.size() * 1.1;
                String donGiaString = new DecimalFormat("#.### đ").format(thanhTien);
                return new SimpleStringProperty(donGiaString);
            }
            return new SimpleStringProperty(new DecimalFormat("#.### đ").format(0.0));
        });
    }
    private void loadData(HoaDon hoaDon) throws RemoteException {
        List<List<Ve>> veList = new ArrayList<>();
        List<List<Ve>> veFilter = new ArrayList<>();
        List<Ve> veNguoiCaoTuoi = new ArrayList<>();
        List<Ve> veNguoiLon = new ArrayList<>();
        List<Ve> veHocSinh = new ArrayList<>();
        List<Ve> veTreEm6to10 = new ArrayList<>();
        List<Ve> veTreEmDuoi6 = new ArrayList<>();
        Map<String, Object> filter = new HashMap<>();
        filter.put("hoadonByMaHd", hoaDon);
        EntityService<Ve> veService = RMIServiceLocator.getVeService();
        List<Ve> veDanhSach = veService.getDanhSach(Ve.class, filter);
        for (Ve ve : veDanhSach) {
            if (ve.getLoaiKh() == LoaiHanhKhach.NGUOI_CAO_TUOI) {
                veNguoiCaoTuoi.add(ve);
            } else if (ve.getLoaiKh() == LoaiHanhKhach.NGUOI_LON) {
                veNguoiLon.add(ve);
            } else if (ve.getLoaiKh() == LoaiHanhKhach.TRE_EM_6_DEN_10) {
                veTreEm6to10.add(ve);
            } else if (ve.getLoaiKh() == LoaiHanhKhach.HOC_SINH_SINH_VIEN) {
                veHocSinh.add(ve);
            } else if (ve.getLoaiKh() == LoaiHanhKhach.TRE_EM_DUOI_6) {
                veTreEmDuoi6.add(ve);
            }
        }
        // Đưa tất cả danh sách các vé vào
        veFilter.add(veNguoiCaoTuoi);
        veFilter.add(veNguoiLon);
        veFilter.add(veHocSinh);
        veFilter.add(veTreEm6to10);
        veFilter.add(veTreEmDuoi6);
        // End đưa tất cả danh sách các vé vào
        // Lọc danh sách các vé không rỗng
        veFilter.stream().filter(ves -> !ves.isEmpty()).forEach(veList::add);
        // End lọc danh sách các vé không rỗng
        danhSachHoaDon.addAll(veList);
        tableHoaDon.setItems(danhSachHoaDon);
    }
    public void setHoaDon(HoaDon hoaDon) throws RemoteException {
        setValueTable();
        loadData(hoaDon);
        setThanhTien(hoaDon);
        setThongTinHoaDon(hoaDon);
        setThongTinHanhKhach(hoaDon);
    }
    private void setThanhTien(HoaDon hoaDon) {
        tongTienLabel.setText(new DecimalFormat("#.### đ").format(hoaDon.getTongTien()));
    }
    private void setThongTinHoaDon(HoaDon hoaDon) {
        NhanVien nhanVien = hoaDon.getNhanvienByMaNv();
        maHDLabel.setText(hoaDon.getMaHd());
        LocalDate localDate =  Timestamp.valueOf(String.valueOf(hoaDon.getNgayLap()))
                .toLocalDateTime().toLocalDate();
        ngayLapHoaDonLabel.setText(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(localDate));
        String hoTenNV = nhanVien.getHoTenDem() + " " + nhanVien.getTen();
        nvThucHienLabel.setText(hoTenNV);
        sdtNVHDLabel.setText(nhanVien.getSdt());
        loaiNVLabel.setText(
                nhanVien.getLoaiNv() == LoaiNhanVien.BAN_VE ?
                        LoaiNhanVien.BAN_VE.getName() :
                        LoaiNhanVien.QUAN_LI_BAN_VE.getName()
        );
        trangThaiLabel.setText(
                nhanVien.isTrangThai() ? "Đang làm việc" : "Ngừng làm việc"
        );
    }
    private void setThongTinHanhKhach(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
        HanhKhach hanhKhach = hoaDon.getHanhkhachByMaHk();
        String hoTenHK = hanhKhach.getHoTenDem() + " " + hanhKhach.getTen();
        hoTenHKLabel.setText(hoTenHK);
        sdtHKLabel.setText(hanhKhach.getSdt());
        emailHKLabel.setText(hanhKhach.getEmail());
        cccdHKLabel.setText(hanhKhach.getCccd());
    }

    public void generateReport(HoaDon hoaDon) {
        Connection connection = null;
        try {
            HashMap<String, Object> para = new HashMap<>();
            para.put("maHD", hoaDon.getMaHd());

            // Tạo QR Code dựa trên maVe
            BufferedImage qrCodeImage = BarcodeUtil.generateBarcodeImage(hoaDon.getMaHd(), 154, 50);
            para.put("qrCodeImage", qrCodeImage);

            // Tạo một kết nối mới (thay vì sử dụng EntityManager)
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/trainticket", "root", "1234567890");

            JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("/view/report/Invoice.jrxml"));

            // Điền dữ liệu vào báo cáo
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, para, connection);

            // Hiển thị báo cáo trong một cửa sổ mới
            JasperViewer.viewReport(jasperPrint, false);
        } catch (JRException e) {
            e.printStackTrace();
            showAlert("Lỗi JasperReports", "Lỗi khi tạo báo cáo: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Xảy ra lỗi khi tạo báo cáo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.show();
    }
}
