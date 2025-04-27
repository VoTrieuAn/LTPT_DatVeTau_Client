package controller.lichTrinh;

import java.time.LocalDate;

/**
 * Kiểm tra tính hợp lệ của dữ liệu đầu vào cho lịch trình.
 */
public class Validator {
    private static final int MIN_DAYS_AHEAD = 80;

    /**
     * Kiểm tra dữ liệu lịch trình.
     * @param trainCode Mã tàu.
     * @param startStation Tên ga khởi hành.
     * @param endStation Tên ga kết thúc.
     * @param departureDate Ngày khởi hành.
     * @param departureHour Giờ khởi hành.
     * @param departureMinute Phút khởi hành.
     * @return Thông báo lỗi nếu không hợp lệ, hoặc null nếu hợp lệ.
     */
    public String validateSchedule(String trainCode, String startStation, String endStation,
                                   LocalDate departureDate, Integer departureHour, Integer departureMinute) {
        if (startStation == null || startStation.isEmpty()) {
            return "Phải chọn ga khởi hành";
        }
        if (endStation == null || endStation.isEmpty()) {
            return "Phải chọn ga kết thúc";
        }
        if (trainCode == null || trainCode.isEmpty()) {
            return "Phải chọn tàu";
        }
        if (departureDate == null) {
            return "Phải chọn ngày khởi hành";
        }
        if (departureDate.isBefore(LocalDate.now().plusDays(MIN_DAYS_AHEAD))) {
            return "Ngày khởi hành phải được tạo trước " + MIN_DAYS_AHEAD + " ngày";
        }
        if (departureHour == null || departureMinute == null) {
            return "Phải chọn thời gian khởi hành";
        }
        return null;
    }
}
