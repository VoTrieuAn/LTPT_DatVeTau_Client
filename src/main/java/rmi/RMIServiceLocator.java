package rmi;

import service.*;

import java.rmi.Naming;

public class RMIServiceLocator {

    private static final String HOST = "localhost"; // hoặc IP như "192.168.1.10"
    private static final int PORT = 1099;

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
    public static LichTrinhService getLichTrinhService() {
        return safeLookup("LichTrinhService");
    }
    public static TaiKhoanService getTaiKhoanService() {
        return safeLookup("TaiKhoanService");
    }
    public static KhuyenMaiService getKhuyenMaiService() {
        return safeLookup("KhuyenMaiService");
    }
    public static TauService getTauService() {
        return safeLookup2("TauService", TauService.class);
    }

    public static ThongKeService getThongKeService() {
        return safeLookup2("ThongKeService", ThongKeService.class);
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

    private static <T> T safeLookup2(String serviceName, Class<T> clazz) {
        try {
            String url = String.format("rmi://%s:%d/%s", HOST, PORT, serviceName);
            Object stub = Naming.lookup(url);
            return clazz.cast(stub); // 👈 Dùng clazz.cast() thay vì ép kiểu trực tiếp
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Không thể kết nối đến service: " + serviceName);
            return null;
        }
    }

}
