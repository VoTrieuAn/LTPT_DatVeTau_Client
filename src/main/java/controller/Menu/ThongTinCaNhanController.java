
package controller.Menu;

import common.LoaiNhanVien;
import entity.HoaDon;
import entity.NhanVien;
import entity.TaiKhoan;
import entity.Ve;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import rmi.RMIServiceLocator;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThongTinCaNhanController {

    @FXML
    private AnchorPane anchorPane;


    @FXML
    private Label cccdLabel;

    @FXML
    private Label chucVuNVLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label gioVaoLamNVLabel;

    @FXML
    private Label gioiTinhNVLabel;

    @FXML
    private Label hoTenNVLabel;


    @FXML
    private Label maNVLabel;

    @FXML
    private Label ngaySinhLabel;

    @FXML
    private Label sdtLabel;

    @FXML
    private Label tongSoHoaDonLabel;

    @FXML
    private Label tongSoVeLabel;

    @FXML
    private Label trangThaiLabel;
    private NhanVien nhanVien;

    private TaiKhoan taiKhoan;

    @FXML
    void controller(ActionEvent event) {

    }

    @FXML
    void keyPressed(KeyEvent event) {

    }

    @FXML
    void mouseClicked(MouseEvent event) {

    }
    public void setTaiKhoan(TaiKhoan taiKhoan) throws RemoteException {
        this.taiKhoan = taiKhoan;
        setNhanVien(taiKhoan.getNhanvienByMaNv());
    }
    public void setNhanVien(NhanVien nhanVien) throws RemoteException {
        setThongTinCongViec(nhanVien);
        setThongTinCongViecNangCao(nhanVien);
        setThongTinCaNhan(nhanVien);
    }
    private void setThongTinCongViec(NhanVien nhanVien)  {
        String hoTen = nhanVien.getHoTenDem() + " " + nhanVien.getTen();
        String gioiTinh = nhanVien.isGioiTinh() ? "Nữ" : "Nam";
        String chucVu = nhanVien.getLoaiNv() ==
                LoaiNhanVien.BAN_VE ? LoaiNhanVien.BAN_VE.getName() : LoaiNhanVien.QUAN_LI_BAN_VE.getName();
        String trangThai = nhanVien.isTrangThai() ? "Đang làm việc" : "Ngưng làm việc";
        maNVLabel.setText(nhanVien.getMaNv());
        hoTenNVLabel.setText(hoTen);
        gioiTinhNVLabel.setText(gioiTinh);
        chucVuNVLabel.setText(chucVu);
        trangThaiLabel.setText(trangThai);
    }
    private void setThongTinCongViecNangCao(NhanVien nhanVien) throws RemoteException {
        Map<String, Object> filter = new HashMap<>();
        filter.put("nhanvienByMaNv", nhanVien);
//        List<HoaDon> hoaDons = TrainTicketApplication.getInstance()
//                .getDatabaseContext()
//                .newEntityDAO(HoaDonDAO.class)
//                .getDanhSach(HoaDon.class, filter);
        List<HoaDon> hoaDons = RMIServiceLocator.getHoaDonService().getDanhSach(HoaDon.class, filter);
        if (this.taiKhoan != null) {
            String gioPhutGiay = String.format(
                    "%02d:%02d:%02d",
                    this.taiKhoan.getNgayDangNhap().getHours(),
                    this.taiKhoan.getNgayDangNhap().getMinutes(),
                    this.taiKhoan.getNgayDangNhap().getSeconds()
            );
            gioVaoLamNVLabel.setText(gioPhutGiay);
        }
        tongSoHoaDonLabel.setText(String.valueOf(hoaDons.size()));
        long tongSoVe = 0;
        if (!hoaDons.isEmpty()) {
            for (HoaDon hoaDon : hoaDons) {
                System.out.println(hoaDon);
                Map<String, Object> filterHD = new HashMap<>();
                filterHD.put("hoadonByMaHd", hoaDon);
                System.out.println(filterHD.size());
//                List<Ve> veDanhSach = TrainTicketApplication.getInstance()
//                        .getDatabaseContext()
//                        .newEntityDAO(VeDAO.class)
//                        .getDanhSach(Ve.class, filterHD);
                List<Ve> veDanhSach = RMIServiceLocator.getVeService().getDanhSach(Ve.class, filterHD);
                tongSoVe += veDanhSach.size();
            }
        }
        tongSoVeLabel.setText(String.valueOf(tongSoVe));
    }
    private void setThongTinCaNhan(NhanVien nhanVien) {
        String ngaySinh = new SimpleDateFormat("dd/MM/yyyy").format(nhanVien.getNgaySinh());
        ngaySinhLabel.setText(ngaySinh);
        cccdLabel.setText(nhanVien.getCccd());
        sdtLabel.setText(nhanVien.getSdt());
        emailLabel.setText(nhanVien.getEmail());
    }
}
