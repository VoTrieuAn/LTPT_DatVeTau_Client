package rmi;

//import service.BanVeServiceRemote;
import service.*;
import serviceRemote.HanhKhachServiceRemote;
//import service.KhuyenMaiServiceRemote;
//import service.TimVeServiceRemote;

import java.rmi.Naming;

public class RMIServiceLocator {

    private static final String HOST = "localhost"; // hoặc IP như "192.168.1.10"
    private static final int PORT = 1099;

//    public static BanVeServiceRemote getBanVeService() {
//        return safeLookup("BanVeService");
//    }

//    public static TimVeServiceRemote getTimVeService() {
//        return safeLookup("TimVeService");
//    }
//        context.bind("rmi://localhost:1099/VeService", veService);
//        context.bind("rmi://localhost:1099/HanhKhacService", hanhKhachService);
//        context.bind("rmi://localhost:1099/NhanVienService", nhanVienService);
//        context.bind("rmi://localhost:1099/HoaDonService", hoaDonService);
//        context.bind("rmi://localhost:1099/TaiKhoanService", taiKhoanService);
    public static VeService getVeService() {
        return safeLookup("VeService");
    }
    public static HanhKhachService getHanhKhachService() {
        return safeLookup("HanhKhachService");
    }
    public static NhanVienService getNhanVienService() {
        return safeLookup("NhanVienService");
    }
    public static HoaDonService getHoaDonService() {
        return safeLookup("HoaDonService");
    }

    public static TaiKhoanService getTaiKhoanService() {
        return safeLookup("TaiKhoanService");
    }
//    public static KhuyenMaiServiceRemote getKhuyenMaiService() {
//        return safeLookup("KhuyenMaiService");
//    }

    @SuppressWarnings("unchecked")
    private static <T> T safeLookup(String serviceName) {
        try {
            String url = String.format("rmi://%s:%d/%s", HOST, PORT, serviceName);
            return (T) Naming.lookup(url);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Không thể kết nối đến service: " + serviceName);
            return null;
        }
    }
}
