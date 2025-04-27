package controller.lichTrinh;

import entity.LichTrinh;
import entity.Tau;
import rmi.RMIServiceLocator;
import service.TauService;
import util.maLTGenerator;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleBuilder {

    private static final TauService tauService = RMIServiceLocator.getTauService();

    private static String layTenGaDayDu(String ga) {
        switch (ga) {
            case "LaoCai", "Lào Cai" -> {
                return "Lào Cai";
            }
            case "HaNoi", "Hà Nội" -> {
                return "Hà Nội";
            }
            case "HaiPhong", "Hải Phòng" -> {
                return "Hải Phòng";
            }
            case "ThanhHoa", "Thanh Hóa" -> {
                return "Thanh Hóa";
            }
            case "Vinh" -> {
                return "Vinh";
            }
            case "DongHoi", "Đồng Hới" -> {
                return "Đồng Hới";
            }
            case "Hue" -> {
                return "Huế";
            }
            case "DaNang", "Đà Nẵng" -> {
                return "Đà Nẵng";
            }
            case "QuyNhon", "Quy Nhơn" -> {
                return "Quy Nhơn";
            }
            case "NhaTrang" -> {
                return "Nha Trang";
            }
            case "PhanThiet", "Phan Thiết" -> {
                return "Phan Thiết";
            }
            case "SaiGon", "Sài Gòn" -> {
                return "Sài Gòn";
            }
            case "VungTau", "Vũng Tàu" -> {
                return "Vũng Tàu";
            }
            default -> throw new IllegalArgumentException("Ga không hợp lệ: " + ga);
        }
    }
//    private int tinhTongThoiGianTheo2Ga(List<String> gaList, String ga1, String ga2) {
//        int tong = 0;
//
//        // Lấy index của ga1 và ga2 trong danh sách gaList
//        int index1 = gaList.indexOf(ga1);
//        int index2 = gaList.indexOf(ga2);
//
//        // Duyệt qua các ga từ ga1 đến ga2 và tính tổng thời gian giữa từng cặp ga liên tiếp
//        for (int i = index1; i < index2; i++) {
//            String gaHienTai = gaList.get(i);
//            String gaTiepTheo = gaList.get(i + 1);
//
//            // Ghép tên ga để tìm trong travelTimes
//            String key1 = gaHienTai + "_" + gaTiepTheo;
//            String key2 = gaTiepTheo + "_" + gaHienTai;
//
//            // Kiểm tra và cộng thời gian vào tổng
//            if (travelTimes.containsKey(key1)) {
//                tong += travelTimes.get(key1);
//            } else if (travelTimes.containsKey(key2)) {
//                tong += travelTimes.get(key2);
//            }
//
//        }
//        return tong;
//    }

    public static List<LichTrinh> buildMultiSchedule(List<String> gaList, LocalDate ngayKhoiHanh, LocalTime gioKhoiHanh, String maTau) throws RemoteException {
        List<LichTrinh> dsLT = new ArrayList<>();

        List<java.sql.Time> gioDiList = new ArrayList<>();
        List<java.sql.Time> gioDenList = new ArrayList<>();
        List<java.sql.Date> ngayKhoiHanhList = new ArrayList<>();
        List<java.sql.Date> ngayKetThucList = new ArrayList<>();
        Map<String, Time> gaToTime = new HashMap<>();  // Map lưu trữ thời gian khởi hành của từng ga

        try {
            Tau tau = tauService.timKiemId(maTau);
            if (tau == null) return dsLT;

// Lặp qua danh sách ga để tính toán thời gian và ngày khởi hành, đến
            for (int i = 0; i < gaList.size() - 1; i++) {
                // Tính tổng thời gian giữa 2 ga
                int thoigian = MapManager.getTravelTimeBetween(gaList, gaList.get(i), gaList.get(i + 1));

                // Tính số phút vượt qua 24 giờ
                long daysToAdd = thoigian / 1440; // 1 ngày = 1440 phút
                long minutesRemaining = thoigian % 1440; // Số phút còn lại sau khi trừ đi số ngày

                String gaKH = layTenGaDayDu(gaList.get(i));
                if (!gaToTime.containsKey(gaKH)) {
                    gaToTime.put(gaKH, timeKH); // Lưu thời gian khởi hành cho ga này
                }

                Time timeKhoiHanhMoi = gaToTime.get(gaKH); // Lấy thời gian khởi hành của ga hiện tại
                LocalTime gioKhoiHanhLocalTime = timeKhoiHanhMoi.toLocalTime();

                // Tính giờ đến (gioDen)
                LocalTime gioDenLocalTime = gioKhoiHanhLocalTime.plusMinutes(minutesRemaining);
                Time gioDen = Time.valueOf(gioDenLocalTime);

                // Tính ngày đến và ngày khởi hành
                LocalDate ngayKhoiHanhMoi = ngayKH.plusDays(daysToAdd);
                if (gioDenLocalTime.isBefore(gioKhoiHanhLocalTime)) {
                    // Nếu giờ đến nhỏ hơn giờ khởi hành, nghĩa là chuyển qua ngày mới
                    ngayKhoiHanhMoi = ngayKhoiHanhMoi.plusDays(1);
                }

                Date dateKhoiHanhMoi = Date.valueOf(ngayKhoiHanhMoi);
                Date dateDen = Date.valueOf(ngayKhoiHanhMoi);

                String gaKT = layTenGaDayDu(gaList.get(i + 1));

                // Thêm vào list giờ và ngày
                gioDiList.add(timeKhoiHanhMoi);
                gioDenList.add(gioDen);
                ngayKhoiHanhList.add(dateKhoiHanhMoi);
                ngayKetThucList.add(dateDen);

                // Cập nhật thời gian khởi hành cho ga tiếp theo
                gaToTime.put(gaKT, Time.valueOf(gioDenLocalTime.plusMinutes(10)));

                // Cập nhật ngày khởi hành (ngayKH) cho vòng lặp kế tiếp
                ngayKH = ngayKhoiHanhMoi;

                // Sửa phần này với cú pháp chính xác khi truy cập vào các phần tử trong danh sách
                for (int i = 0; i < gaList.size() - 1; i++) { // Ga khởi hành
                    for (int j = i + 1; j < gaList.size(); j++) { // Ga kết thúc
                        // Sử dụng các giá trị tương ứng từ các danh sách thời gian và ngày
                        Time gioDi = gioDiList.get(i); // Sửa từ gioDiList[i] thành gioDiList.get(i)
                        Time gioDen = gioDenList.get(j - 1); // Sửa từ gioDenList[j - 1] thành gioDenList.get(j - 1)
                        Date ngayKhoiHanh = ngayKhoiHanhList.get(i); // Sử dụng get(i) thay cho i
                        Date ngayKetThuc = ngayKetThucList.get(j - 1); // Sử dụng get(j - 1)

                        String gaKH = layTenGaDayDu(gaList.get(i));
                        String gaKT = layTenGaDayDu(gaList.get(j));

                        LocalTime gioKhoiHanhLocalTime = gioDi.toLocalTime();
                        LocalDate ngayKhoiHanhLocalDate = ngayKhoiHanh.toLocalDate();
                        // Tạo mã lịch trình
                        String maLT = maLTGenerator.generateMaLT(gaKH, gaKT, ngayKhoiHanhLocalDate, gioKhoiHanhLocalTime);
                        // Thêm Lịch Trình vào danh sách
                        dsLT.add(new LichTrinh(maLT, gaKH, gaKT, ngayKhoiHanh, gioDen, gioDi, ngayKetThuc, 0, tauByMa));
                    }
                }
                return dsLT;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return dsLT;
    }

}
