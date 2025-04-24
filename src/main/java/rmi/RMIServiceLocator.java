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

    public static VeService getVeService() {
        return safeLookup("VeService");
    }
    public static HanhKhachService getHanhKhachService() {
        return safeLookup("HanhKhacService");
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
