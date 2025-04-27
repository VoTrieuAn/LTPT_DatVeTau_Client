package controller.lichTrinh;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapManager {

    private static final Map<String, Integer> travelTimes = new HashMap<>();

    static {
        travelTimes.put("LaoCai_HaNoi", 480);
        travelTimes.put("HaNoi_HaiPhong", 150);
        travelTimes.put("HaNoi_ThanhHoa", 210);
        travelTimes.put("ThanhHoa_Vinh", 150);
        travelTimes.put("Vinh_DongHoi", 240);
        travelTimes.put("DongHoi_Hue", 180);
        travelTimes.put("Hue_DaNang", 150);
        travelTimes.put("DaNang_QuyNhon", 360);
        travelTimes.put("QuyNhon_NhaTrang", 290);
        travelTimes.put("NhaTrang_PhanThiet", 240);
        travelTimes.put("PhanThiet_SaiGon", 220);
        travelTimes.put("SaiGon_VungTau", 120);
    }


    public static int getTravelTimeBetween(List<String> gaList, String ga1, String ga2) {
        int tong = 0;

        // Lấy index của ga1 và ga2 trong danh sách gaList
        int index1 = gaList.indexOf(ga1);
        int index2 = gaList.indexOf(ga2);

        // Duyệt qua các ga từ ga1 đến ga2 và tính tổng thời gian giữa từng cặp ga liên tiếp
        for (int i = index1; i < index2; i++) {
            String gaHienTai = gaList.get(i);
            String gaTiepTheo = gaList.get(i + 1);

            // Ghép tên ga để tìm trong travelTimes
            String key1 = gaHienTai + "_" + gaTiepTheo;
            String key2 = gaTiepTheo + "_" + gaHienTai;

            // Kiểm tra và cộng thời gian vào tổng
            if (travelTimes.containsKey(key1)) {
                tong += travelTimes.get(key1);
            } else if (travelTimes.containsKey(key2)) {
                tong += travelTimes.get(key2);
            }

        }
        return tong;
    }
}
