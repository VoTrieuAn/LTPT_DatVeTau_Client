package controller.thongKe;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import common.LoaiGhe;
import common.LoaiHanhKhach;
import dao.impl.HoaDonDAOImpl;
import dao.impl.LichTrinhDAOImpl;
import dao.impl.VeDAOImpl;
import entity.HoaDon;
import entity.Ve;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TongQuanController {
    public ScrollPane mainScrollPane;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private JFXButton Btn_Loc, Btn_Xuat;
    @FXML
    private Label numHoaDon, numHanhKhach, numVeTau, numDoanhThu, textThongTinVeBan, textThongTinDoanhThu, textTyLeCacTuyenDuocDat, textTyLeHanhKhach, textTyLeLoaiGhe, textDoanhThuTau, textSoChuyenMoiTau, textThoiGianDiDuocChon;
    @FXML
    private JFXComboBox<Integer> cboboxThang, cboboxNam;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private AreaChart<String, Number> chartDoanhThu;
    @FXML
    private BarChart<Double, String> chartDoanhThuTau;
    @FXML
    private BarChart<String, Number> chartDoanhThuTheoNam, chartSoLuongVeBan, chartSoChuyenDi;
    @FXML
    private PieChart chartLoaiKhachHang, chartLoaiGhe, chartCacTuyen;
    @FXML
    private DatePicker datepick_end, datepick_start;
    @FXML
    private JFXRadioButton radio_cuThe, radio_thangNam;
    @FXML
    private LineChart<String, Number> chartThoiGian;


    private final HoaDonDAOImpl hddao;
    private final VeDAOImpl vedao;
    private final LichTrinhDAOImpl ltdao;

    public TongQuanController() {
        this.ltdao = new LichTrinhDAOImpl();
        this.hddao = new HoaDonDAOImpl();
        this.vedao = new VeDAOImpl();
    }
    DecimalFormat decimalFormat = new DecimalFormat("#,##0 VNĐ");
    @FXML
    public void initialize() {
        buttonGroup();
        setupComboBoxes();
        displayData();
        chartDoanhThuTheoNam.setVisible(false);
        mainPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if(newScene != null){
                newScene.setOnKeyPressed(keyEvent -> {
                    if(keyEvent.getCode() == KeyCode.F5)
                        Btn_Loc.fire();
                });
            }
        });
        Btn_Loc.setOnAction(actionEvent -> {
            displayData();
            if(radio_cuThe.isSelected()) {
                checkDatePicker();
            }

        });
    }

    private void checkDatePicker(){
        if(datepick_start.getValue().isAfter(LocalDate.now()) ||  datepick_end.getValue().isAfter(LocalDate.now())){
            Alert alert = new Alert(Alert.AlertType.ERROR); // Loại thông báo là ERROR
            alert.setTitle("");
            alert.setHeaderText(null); // Không cần tiêu đề phụ
            alert.setContentText("Chưa có dữ liệu"); // Nội dung thông báo
            // Hiển thị dialog
            alert.showAndWait();
            datepick_end.setValue(LocalDate.now());
            datepick_start.setValue(LocalDate.now());
        }
        if(datepick_end.getValue() == null) {
            datepick_end.setValue(datepick_start.getValue());
        }
        if (datepick_end.getValue().isBefore(datepick_start.getValue())) {
            Alert alert = new Alert(Alert.AlertType.ERROR); // Loại thông báo là ERROR
            alert.setTitle("Lỗi");
            alert.setHeaderText(null); // Không cần tiêu đề phụ
            alert.setContentText("Ngày kết thúc không thể trước ngày bắt đầu!"); // Nội dung thông báo
            // Hiển thị dialog
            alert.showAndWait();
            datepick_end.setValue(datepick_start.getValue());
        }
    }
    private void buttonGroup(){
        radio_thangNam.setSelected(true);
        radio_thangNam.setOnAction(actionEvent -> radio_cuThe.setSelected(!radio_thangNam.isSelected()));
        radio_cuThe.setOnAction(actionEvent -> radio_thangNam.setSelected(!radio_cuThe.isSelected()));
    }

    private void setupComboBoxes() {
        for (int i = 0; i <= 12; i++) {
            cboboxThang.getItems().add(i);
        }
        int currentYear = LocalDate.now().getYear();
        for (int i = 2021; i <= currentYear; i++) {
            cboboxNam.getItems().add(i);
        }
        cboboxThang.setValue(LocalDate.now().getMonthValue() - 1);
        cboboxNam.setValue(currentYear);

        datepick_start.setValue(LocalDate.now());
        datepick_end.setValue(LocalDate.now());
    }

    private void displayData() {
        if (radio_thangNam.isSelected()) {
            Integer selectedNam = cboboxNam.getValue();
            if(cboboxThang.getValue() != 0){
                showMonthlyData(selectedNam);
            }
            else {
                showYearlyData(selectedNam);
            }
        }
        else if(radio_cuThe.isSelected()){
            showCustomDateRangeData();
        }
    }
    private void showMonthlyData(Integer selectedNam){
        chartDoanhThuTheoNam.setVisible(false);
        chartDoanhThu.setVisible(true);

        Integer selectedThang = cboboxThang.getValue();
//        ============ UpDate 4 Ô TOP ==============
        List<HoaDon> dsHD = hddao.getAllHoaDonTheoThang(selectedThang, selectedNam);
        List<Ve> dsVe = vedao.getAllVeTheoThang(selectedThang, selectedNam);
        List<String> dsHK = vedao.getHanhKhachTheoThang(selectedThang, selectedNam);
        Double tongDoanhThu = hddao.getTongDoanhThuTheoThang(selectedThang, selectedNam);
        String formattedDoanhThu = decimalFormat.format(tongDoanhThu);
        numDoanhThu.setText(formattedDoanhThu);
        updateHoaDonInfo(dsHD);
        updateVeTauInfo(dsVe);
        updateHanhKhachInfo(dsHK);
//        ============ END - UpDate 4 Ô TOP ==============
//        ============ UpDate Chart ==============
        Map<Double, String> doanhThuTauTheoThang = vedao.doanhThuTauTheoThang(selectedThang, selectedNam);
        Map<LoaiHanhKhach, Integer> countLoaiHK = vedao.countLoaiKhachHangTheoThang(selectedThang, selectedNam);
        Map<LoaiGhe, Integer> countLoaiGhe = vedao.countLoaiGheTheoThang(selectedThang, selectedNam);
        Map<String, Integer> countSoTuyenMoiTau = ltdao.countTuyenMoiTauTheoThang(selectedThang, selectedNam);
        Map<String, Integer> countSoTuyen = vedao.countSoTuyenTheoThang(selectedThang, selectedNam);
        Map<String, Integer> countSoGioDi = vedao.countThoiGianDiTheoThang(selectedThang, selectedNam);

        updateChartDoanhThu(dsHD);
        updateChartSoVe(dsVe);
        updateChartKhachHang(countLoaiHK);
        updateChartGhe(countLoaiGhe);
        updateChartDoanhThuTau(doanhThuTauTheoThang);
        updateChartSoChuyenTau(countSoTuyenMoiTau);
        updateChartSoTuyen(countSoTuyen);
        updateChartGioDi(countSoGioDi);
    }
    private void showYearlyData(Integer selectedNam) {
        chartDoanhThuTheoNam.setVisible(true);
        chartDoanhThu.setVisible(false);

        Map<String, Double> doanhThuTungThang = hddao.getDoanhThuTheoNam(selectedNam);
//        ============ UpDate 4 Ô TOP ==============
        List<HoaDon> dsHD = hddao.getHoaDonTheoNam(selectedNam);
        List<Ve> dsVe = vedao.getAllVeTheoNam(selectedNam);
        List<String> dsHK = vedao.getHanhKhachTheoNam(selectedNam);
        Double tongDoanhThu = hddao.getTongDoanhThuTheoNam(selectedNam);
        String formattedDoanhThu = decimalFormat.format(tongDoanhThu);
        numDoanhThu.setText(formattedDoanhThu);
        updateHoaDonInfo(dsHD);
        updateVeTauInfo(dsVe);
        updateHanhKhachInfo(dsHK);
//        ============ END - UpDate 4 Ô TOP ==============
//        ============ UpDate Chart ==============
        Map<Double, String> doanhThuTauTheoNam= vedao.doanhThuTauTheoNam(selectedNam);
        Map<LoaiHanhKhach, Integer> countLoaiHK = vedao.countLoaiKhachHangTheoNam(selectedNam);
        Map<LoaiGhe, Integer> countLoaiGhe = vedao.countLoaiGheTheoNam(selectedNam);
        Map<String, Integer> countSoVe = vedao.countSoVeTheoNam(selectedNam);
        Map<String, Integer> countSoTuyenMoiTau = ltdao.countTuyenMoiTauTheoNam(selectedNam);
        Map<String, Integer> countSoTuyen = vedao.countSoTuyenTheoNam(selectedNam);
        Map<String, Integer> countSoGioDi = vedao.countThoiGianDiTheoNam(selectedNam);

        updateChartDoanhThuTheoNam(doanhThuTungThang);
        updateChartSoVeTheoNam(countSoVe);
        updateChartKhachHang(countLoaiHK);
        updateChartGhe(countLoaiGhe);
        updateChartDoanhThuTau(doanhThuTauTheoNam);
        updateChartSoChuyenTau(countSoTuyenMoiTau);
        updateChartSoTuyen(countSoTuyen);
        updateChartGioDi(countSoGioDi);
    }
    private void showCustomDateRangeData() {
        chartDoanhThuTheoNam.setVisible(false);
        chartDoanhThu.setVisible(true);
        LocalDate start = datepick_start.getValue();
        LocalDate end = datepick_end.getValue();
//        ============ UpDate 4 Ô TOP ==============
        List<HoaDon> dsHD = hddao.getHoaDonTheoNgayCuThe(start, end);
        List<Ve> dsVe = vedao.getAllVeTheoNgayCuThe(start, end);
        List<String> dsHK = vedao.getHanhKhachTheoNgayCuThe(start, end);
        Double tongDoanhThu = hddao.getTongDoanhThuTheoNgayCuThe(start, end);
        String formattedDoanhThu = decimalFormat.format(tongDoanhThu);
        numDoanhThu.setText(formattedDoanhThu);
        updateHoaDonInfo(dsHD);
        updateVeTauInfo(dsVe);
        updateHanhKhachInfo(dsHK);
//        ============ END - UpDate 4 Ô TOP ==============
//        ============ UpDate Chart ==============
        Map<LoaiGhe, Integer> countLoaiGhe = vedao.countLoaiGheTheoNgayCuThe(start, end);
        Map<LoaiHanhKhach, Integer> countLoaiHK = vedao.countLoaiKhachHangTheoNgayCuThe(start, end);
        Map<Double, String> doanhThuTau= vedao.doanhThuTauTheoNgayCuThe(start, end);
        Map<String, Integer> countSoTuyenMoiTau = ltdao.countTuyenMoiTauTheoNgayCuThe(start, end);
        Map<String, Integer> countSoTuyen = vedao.countSoTuyenTheoNgayCuThe(start, end);
        Map<String, Integer> countSoGioDi = vedao.countThoiGianDiTheoNgayCuThe(start, end);

        updateChartDoanhThu(dsHD);
        updateChartSoVe(dsVe);
        updateChartKhachHang(countLoaiHK);
        updateChartGhe(countLoaiGhe);
        updateChartDoanhThuTau(doanhThuTau);
        updateChartSoChuyenTau(countSoTuyenMoiTau);
        updateChartSoTuyen(countSoTuyen);
        updateChartGioDi(countSoGioDi);
    }
    //        ============ UpDate 4 Ô TOP ==============
    private void updateHoaDonInfo(List<HoaDon> dsHD) {
        if (dsHD != null && !dsHD.isEmpty()) {
            numHoaDon.setText(String.valueOf(dsHD.size()));
        } else {
            numHoaDon.setText("0");
        }
    }

    private void updateVeTauInfo(List<Ve> dsVe) {
        if (dsVe != null && !dsVe.isEmpty()) {
            numVeTau.setText(String.valueOf(dsVe.size()));
        } else {
            numVeTau.setText("0");
        }
    }

    private void updateHanhKhachInfo(List<String> dsHK) {
        if (dsHK != null && !dsHK.isEmpty()) {
            numHanhKhach.setText(String.valueOf(dsHK.size()));
        } else {
            numHanhKhach.setText("0");
        }
    }
    //        ============ END - UpDate 4 Ô TOP ==============

    //        ============ UpDate Các Biểu Đồ ==============
    private void updateChartDoanhThu(List<HoaDon> dsHD) {
        chartDoanhThu.getData().clear();
        xAxis.setAutoRanging(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        if(radio_thangNam.isSelected()) {
            int selectedNam = cboboxNam.getValue();
            int selectedThang = cboboxThang.getValue();

            // Tạo danh sách đầy đủ các ngày trong tháng với doanh thu mặc định là 0
            Map<String, Double> doanhThuTheoNgay = new LinkedHashMap<>();
            YearMonth yearMonth = YearMonth.of(selectedNam, selectedThang);
            int daysInMonth = yearMonth.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(selectedNam, selectedThang, day);
                doanhThuTheoNgay.put(date.format(formatter), 0.0); // Set doanh thu là 0 cho mọi ngày trước tiên
            }

            // Cập nhật doanh thu cụ thể trong Map
            for (HoaDon hoaDon : dsHD) {
                LocalDate ngayLap = hoaDon.getNgayLap().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String ngay = ngayLap.format(formatter);
                Double tongTien = hoaDon.getTongTien();
                doanhThuTheoNgay.put(ngay, doanhThuTheoNgay.getOrDefault(ngay, 0.0) + tongTien);
            }
            // Tạo Series cho biểu đồ
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu theo ngày");
            chartDoanhThu.setTitle("DOANH THU THEO NGÀY TRONG THÁNG " + selectedThang + " NĂM " + selectedNam);
            for (Map.Entry<String, Double> entry : doanhThuTheoNgay.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
                series.getData().add(data);
                // Lắng nghe sự kiện khi Node được tạo
                data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                    if (newNode != null) { // Khi Node được tạo
                        Tooltip tooltip = new Tooltip("Doanh Thu: " + decimalFormat.format(entry.getValue()));
                        tooltip.setShowDelay(Duration.millis(100));
                        tooltip.setHideDelay(Duration.millis(200));
                        Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                    }
                });
            }
            // Cập nhật biểu đồ
            chartDoanhThu.getData().add(series);

            // ========== Cập nhật thông tin Doanh Thu =============
            double maxDoanhThu = 0;
            double minDoanhThu = Double.MAX_VALUE;
            double minDoanhThu2 = Double.MAX_VALUE;
            double totalDoanhThu = 0;
            int countDays = doanhThuTheoNgay.size();
            List<String> daysWithMaxRevenue = new ArrayList<>();
            List<String> daysWithMinRevenue = new ArrayList<>();
            List<String> daysWithMinKhac0 = new ArrayList<>();

            int daysWithRevenue = 0; // Biến đếm số ngày có doanh thu khác 0
            double previousTotalDoanhThu = hddao.getTongDoanhThuTheoThang(selectedThang - 1, selectedNam);
            double revenueGrowth = 0; // Tăng trưởng doanh thu

            // Duyệt qua từng ngày để tính các thông số
            for (Map.Entry<String, Double> entry : doanhThuTheoNgay.entrySet()) {
                String ngay = entry.getKey();
                double doanhThu = entry.getValue();

                // Cập nhật tổng doanh thu
                totalDoanhThu += doanhThu;

                // Kiểm tra và cập nhật mức doanh thu cao nhất
                if (doanhThu > maxDoanhThu) {
                    maxDoanhThu = doanhThu;
                    daysWithMaxRevenue.clear(); // Xóa danh sách cũ nếu tìm thấy doanh thu cao hơn
                    daysWithMaxRevenue.add(ngay); // Thêm ngày hiện tại vào danh sách
                } else if (doanhThu == maxDoanhThu) {
                    daysWithMaxRevenue.add(ngay); // Thêm ngày vào danh sách nếu doanh thu bằng mức cao nhất
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất
                if (doanhThu < minDoanhThu) {
                    minDoanhThu = doanhThu;
                    daysWithMinRevenue.clear();
                    daysWithMinRevenue.add(ngay);
                } else if (doanhThu == minDoanhThu) {
                    daysWithMinRevenue.add(ngay);
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất khác 0
                if (doanhThu < minDoanhThu2 && doanhThu != 0) {
                    minDoanhThu2 = doanhThu;
                    daysWithMinKhac0.clear();
                    daysWithMinKhac0.add(ngay);
                } else if (doanhThu == minDoanhThu2) {
                    daysWithMinKhac0.add(ngay);
                }

                // Đếm số ngày có doanh thu khác 0
                if (doanhThu > 0) {
                    daysWithRevenue++;
                }
            }

            // Tính doanh thu trung bình
            double averageDoanhThu = totalDoanhThu / countDays;

            // Tính tăng trưởng doanh thu nếu có dữ liệu kỳ trước
            if (previousTotalDoanhThu > 0) {
                revenueGrowth = ((totalDoanhThu - previousTotalDoanhThu) / previousTotalDoanhThu) * 100;
            }

            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu cao nhất
            String daysWithMaxText = String.join(", ", daysWithMaxRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất
            String daysWithMinText = String.join(", ", daysWithMinRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất khác 0
            String daysWithMinText2 = String.join(", ", daysWithMinKhac0);

            // Cập nhật thông tin cho label
            String infoText = String.format(
                    "Doanh thu cao nhất: %s (Ngày: %s)\nDoanh thu thấp nhất: %s (Ngày: %s)\nDoanh thu thấp nhất khác 0: %s (Ngày: %s)\nDoanh thu trung bình: %s\nSố ngày có doanh thu: %d/%d\nTăng trưởng doanh thu so với tháng trước: %.2f%%",
                    decimalFormat.format(maxDoanhThu), daysWithMaxText,
                    decimalFormat.format(minDoanhThu), daysWithMinText,
                    decimalFormat.format(minDoanhThu2), daysWithMinText2,
                    decimalFormat.format(averageDoanhThu),
                    daysWithRevenue,countDays,
                    revenueGrowth
            );
            textThongTinDoanhThu.setText(infoText);
        }
        else if (radio_cuThe.isSelected()){
            LocalDate startDate = datepick_start.getValue();
            LocalDate endDate = datepick_end.getValue();
            // Tạo danh sách các ngày trong khoảng thời gian đã chọn
            Map<String, Double> doanhThuTheoNgay = new LinkedHashMap<>();
            while (!startDate.isAfter(endDate)) {
                doanhThuTheoNgay.put(startDate.format(formatter), 0.0); // Set doanh thu là 0 cho mọi ngày trước tiên
                startDate = startDate.plusDays(1); // Chuyển đến ngày tiếp theo
            }

            // Cập nhật doanh thu cụ thể trong Map
            for (HoaDon hoaDon : dsHD) {
                LocalDate ngayLap = hoaDon.getNgayLap().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String ngay = ngayLap.format(formatter);
                Double tongTien = hoaDon.getTongTien();
                if (doanhThuTheoNgay.containsKey(ngay)) {
                    doanhThuTheoNgay.put(ngay, doanhThuTheoNgay.get(ngay) + tongTien);
                }
            }
            // Tạo Series cho biểu đồ
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu theo ngày");
            chartDoanhThu.setTitle("DOANH THU THEO NGÀY TỪ " + datepick_start.getValue().format(DateTimeFormatter.ofPattern("dd/MM")) + " ĐẾN " + datepick_end.getValue().format(DateTimeFormatter.ofPattern("dd/MM")));

            for (Map.Entry<String, Double> entry : doanhThuTheoNgay.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
                series.getData().add(data);
                // Lắng nghe sự kiện khi Node được tạo
                data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                    if (newNode != null) { // Khi Node được tạo
                        Tooltip tooltip = new Tooltip("Doanh Thu: " + decimalFormat.format(entry.getValue()));
                        tooltip.setShowDelay(Duration.millis(100));
                        tooltip.setHideDelay(Duration.millis(200));
                        Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                    }
                });
            }
            // Cập nhật biểu đồ
            chartDoanhThu.getData().add(series);

            // ========== Cập nhật thông tin Doanh Thu =============
            double maxDoanhThu = 0;
            double minDoanhThu = Double.MAX_VALUE;
            double minDoanhThu2 = Double.MAX_VALUE;
            double totalDoanhThu = 0;
            int countDays = doanhThuTheoNgay.size();
            List<String> daysWithMaxRevenue = new ArrayList<>();
            List<String> daysWithMinRevenue = new ArrayList<>();
            List<String> daysWithMinKhac0 = new ArrayList<>();

            int daysWithRevenue = 0; // Biến đếm số ngày có doanh thu khác 0

            // Duyệt qua từng ngày để tính các thông số
            for (Map.Entry<String, Double> entry : doanhThuTheoNgay.entrySet()) {
                String ngay = entry.getKey();
                double doanhThu = entry.getValue();

                // Cập nhật tổng doanh thu
                totalDoanhThu += doanhThu;

                // Kiểm tra và cập nhật mức doanh thu cao nhất
                if (doanhThu > maxDoanhThu) {
                    maxDoanhThu = doanhThu;
                    daysWithMaxRevenue.clear(); // Xóa danh sách cũ nếu tìm thấy doanh thu cao hơn
                    daysWithMaxRevenue.add(ngay); // Thêm ngày hiện tại vào danh sách
                } else if (doanhThu == maxDoanhThu) {
                    daysWithMaxRevenue.add(ngay); // Thêm ngày vào danh sách nếu doanh thu bằng mức cao nhất
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất
                if (doanhThu < minDoanhThu) {
                    minDoanhThu = doanhThu;
                    daysWithMinRevenue.clear();
                    daysWithMinRevenue.add(ngay);
                } else if (doanhThu == minDoanhThu) {
                    daysWithMinRevenue.add(ngay);
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất khác 0
                if (doanhThu < minDoanhThu2 && doanhThu != 0) {
                    minDoanhThu2 = doanhThu;
                    daysWithMinKhac0.clear();
                    daysWithMinKhac0.add(ngay);
                } else if (doanhThu == minDoanhThu2) {
                    daysWithMinKhac0.add(ngay);
                }

                // Đếm số ngày có doanh thu khác 0
                if (doanhThu > 0) {
                    daysWithRevenue++;
                }
            }

            // Tính doanh thu trung bình
            double averageDoanhThu = totalDoanhThu / countDays;

            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu cao nhất
            String daysWithMaxText = String.join(", ", daysWithMaxRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất
            String daysWithMinText = String.join(", ", daysWithMinRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất khác 0
            String daysWithMinText2 = String.join(", ", daysWithMinKhac0);

            // Cập nhật thông tin cho label
            String infoText = String.format(
                    "Doanh thu cao nhất: %s (Ngày: %s)\nDoanh thu thấp nhất: %s (Ngày: %s)\nDoanh thu thấp nhất khác 0: %s (Ngày: %s)\nDoanh thu trung bình: %s\nSố ngày có doanh thu: %d/%d",
                    decimalFormat.format(maxDoanhThu), daysWithMaxText,
                    decimalFormat.format(minDoanhThu), daysWithMinText,
                    decimalFormat.format(minDoanhThu2), daysWithMinText2,
                    decimalFormat.format(averageDoanhThu),
                    daysWithRevenue, countDays
            );
            textThongTinDoanhThu.setText(infoText);

        }
    }
    private void updateChartSoVe(List<Ve> dsVe) {
        chartSoLuongVeBan.getData().clear();
        NumberAxis yAxis = (NumberAxis) chartSoLuongVeBan.getYAxis();
        xAxis.setAutoRanging(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        if(radio_thangNam.isSelected()){
            int selectedNam = cboboxNam.getValue();
            int selectedThang = cboboxThang.getValue();

            // Tạo danh sách đầy đủ các ngày trong tháng với số lượng vé mặc định là 0
            Map<String, Integer> veBanTheoNgay = new LinkedHashMap<>();
            YearMonth yearMonth = YearMonth.of(selectedNam, selectedThang);
            int daysInMonth = yearMonth.lengthOfMonth();
            for(int day = 1; day <= daysInMonth; day++){
                LocalDate date = LocalDate.of(selectedNam, selectedThang, day);
                veBanTheoNgay.put(date.format(formatter), 0); // Set số lượng vé là 0 cho mọi ngày trước tiên
            }

            // Cập nhật số lượng vé bán cụ thể trong Map
            for (Ve ve : dsVe){
                LocalDate ngayLap = ve.getNgayMua().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String ngay = ngayLap.format(formatter);
                veBanTheoNgay.put(ngay, veBanTheoNgay.getOrDefault(ngay, 0) + 1); // Tăng số lượng vé bán
            }

            // Tạo Series cho biểu đồ
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Số lượng vé bán theo ngày");
            chartSoLuongVeBan.setTitle("SỐ LƯỢNG VÉ BÁN THEO NGÀY TRONG THÁNG " + selectedThang + " NĂM " + selectedNam);

            for (Map.Entry<String, Integer> entry : veBanTheoNgay.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
                series.getData().add(data);
                // Lắng nghe sự kiện khi Node được tạo
                data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                    if (newNode != null) { // Khi Node được tạo
                        Tooltip tooltip = new Tooltip("Số vé: " + entry.getValue());
                        tooltip.setShowDelay(Duration.millis(100));
                        tooltip.setHideDelay(Duration.millis(200));
                        Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                    }
                });
            }
            // Cập nhật biểu đồ
            chartSoLuongVeBan.getData().add(series);
            // ========== Cập nhật thông tin Doanh Thu =============
            int maxSoVe = 0;
            int minSoVe = Integer.MAX_VALUE;
            int minSoVe2 = Integer.MAX_VALUE;
            int totalDoanhThu = 0;
            int countDays = veBanTheoNgay.size();
            List<String> daysWithMaxRevenue = new ArrayList<>();
            List<String> daysWithMinRevenue = new ArrayList<>();
            List<String> daysWithMinKhac0 = new ArrayList<>();

            int daysWithRevenue = 0;
            int previousTotalDoanhThu = vedao.getAllVeTheoThang(selectedThang - 1, selectedNam).size();
            double revenueGrowth = 0;

            // Duyệt qua từng ngày để tính các thông số
            for (Map.Entry<String, Integer> entry : veBanTheoNgay.entrySet()) {
                String ngay = entry.getKey();
                double doanhThu = entry.getValue();

                // Cập nhật tổng doanh thu
                totalDoanhThu += (int) doanhThu;

                // Kiểm tra và cập nhật mức doanh thu cao nhất
                if (doanhThu > maxSoVe) {
                    maxSoVe = (int) doanhThu;
                    daysWithMaxRevenue.clear();
                    daysWithMaxRevenue.add(ngay);
                } else if (doanhThu == maxSoVe) {
                    daysWithMaxRevenue.add(ngay);
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất
                if (doanhThu < minSoVe) {
                    minSoVe = (int) doanhThu;
                    daysWithMinRevenue.clear();
                    daysWithMinRevenue.add(ngay);
                } else if (doanhThu == minSoVe) {
                    daysWithMinRevenue.add(ngay);
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất khác 0
                if (doanhThu < minSoVe2 && doanhThu != 0) {
                    minSoVe2 = (int) doanhThu;
                    daysWithMinKhac0.clear();
                    daysWithMinKhac0.add(ngay);
                } else if (doanhThu == minSoVe2) {
                    daysWithMinKhac0.add(ngay);
                }

                // Đếm số ngày có doanh thu khác 0
                if (doanhThu > 0) {
                    daysWithRevenue++;
                }
            }

            // Tính doanh thu trung bình
            double averageDoanhThu = (double) totalDoanhThu / countDays;

            // Tính tăng trưởng doanh thu nếu có dữ liệu kỳ trước
            if (previousTotalDoanhThu > 0) {
                revenueGrowth = ((double) (totalDoanhThu - previousTotalDoanhThu) / previousTotalDoanhThu) * 100;
            }

            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu cao nhất
            String daysWithMaxText = String.join(", ", daysWithMaxRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất
            String daysWithMinText = String.join(", ", daysWithMinRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất khác 0
            String daysWithMinText2 = String.join(", ", daysWithMinKhac0);

            // Cập nhật thông tin cho label
            String infoText = String.format(
                    "Số vé cao nhất: %d (Ngày: %s)\nSố vé thấp nhất: %d (Ngày: %s)\nSố vé thấp nhất khác 0: %d (Ngày: %s)\nSố vé trung bình: %.1f\nSố ngày có bán được vé: %d/%d\nTăng trưởng số vé so với tháng trước: %.2f%%",
                    maxSoVe, daysWithMaxText,
                    minSoVe, daysWithMinText,
                    minSoVe2, daysWithMinText2,
                    averageDoanhThu,
                    daysWithRevenue,countDays,
                    revenueGrowth
            );
            textThongTinVeBan.setText(infoText);
            yAxis.setUpperBound(maxSoVe + 1);
        }
        else if (radio_cuThe.isSelected()){
            LocalDate startDate = datepick_start.getValue();
            LocalDate endDate = datepick_end.getValue();

            // Tạo danh sách các ngày trong khoảng thời gian đã chọn
            Map<String, Integer> veBanTheoNgay = new LinkedHashMap<>();
            while (!startDate.isAfter(endDate)) {
                veBanTheoNgay.put(startDate.format(formatter), 0); // Set số lượng vé là 0 cho mọi ngày trước tiên
                startDate = startDate.plusDays(1); // Chuyển đến ngày tiếp theo
            }

            // Cập nhật số lượng vé bán cụ thể trong Map
            for (Ve ve : dsVe) {
                LocalDate ngayLap = ve.getNgayMua().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String ngay = ngayLap.format(formatter);
                if (veBanTheoNgay.containsKey(ngay)) {
                    veBanTheoNgay.put(ngay, veBanTheoNgay.get(ngay) + 1); // Tăng số lượng vé bán
                }
            }

            // Tạo Series cho biểu đồ
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Số lượng vé bán theo ngày");
            chartSoLuongVeBan.setTitle("SỐ LƯỢNG VÉ BÁN THEO NGÀY TỪ " + datepick_start.getValue().format(DateTimeFormatter.ofPattern("dd/MM")) + " ĐẾN " + datepick_end.getValue().format(DateTimeFormatter.ofPattern("dd/MM")));

            for (Map.Entry<String, Integer> entry : veBanTheoNgay.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
                series.getData().add(data);
                // Lắng nghe sự kiện khi Node được tạo
                data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                    if (newNode != null) { // Khi Node được tạo
                        Tooltip tooltip = new Tooltip("Số vé: " + entry.getValue());
                        tooltip.setShowDelay(Duration.millis(100));
                        tooltip.setHideDelay(Duration.millis(200));
                        Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                    }
                });
            }

            // Cập nhật biểu đồ
            chartSoLuongVeBan.getData().add(series);

            // ========== Cập nhật thông tin Doanh Thu =============
            int maxSoVe = 0;
            int minSoVe = Integer.MAX_VALUE;
            int minSoVe2 = Integer.MAX_VALUE;
            int totalSoVe = dsVe.size();
            int countDays = veBanTheoNgay.size();
            List<String> daysWithMaxRevenue = new ArrayList<>();
            List<String> daysWithMinRevenue = new ArrayList<>();
            List<String> daysWithMinKhac0 = new ArrayList<>();

            int daysWithRevenue = 0; // Biến đếm số ngày có doanh thu khác 0

            // Duyệt qua từng ngày để tính các thông số
            for (Map.Entry<String, Integer> entry : veBanTheoNgay.entrySet()) {
                String ngay = entry.getKey();
                double soVe = entry.getValue();

                // Kiểm tra và cập nhật mức doanh thu cao nhất
                if (soVe > maxSoVe) {
                    maxSoVe = (int) soVe;
                    daysWithMaxRevenue.clear(); // Xóa danh sách cũ nếu tìm thấy doanh thu cao hơn
                    daysWithMaxRevenue.add(ngay); // Thêm ngày hiện tại vào danh sách
                } else if (soVe == maxSoVe) {
                    daysWithMaxRevenue.add(ngay); // Thêm ngày vào danh sách nếu doanh thu bằng mức cao nhất
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất
                if (soVe < minSoVe) {
                    minSoVe = (int) soVe;
                    daysWithMinRevenue.clear();
                    daysWithMinRevenue.add(ngay);
                } else if (soVe == minSoVe) {
                    daysWithMinRevenue.add(ngay);
                }

                // Kiểm tra và cập nhật mức doanh thu thấp nhất khác 0
                if (soVe < minSoVe2 && soVe != 0) {
                    minSoVe2 = (int) soVe;
                    daysWithMinKhac0.clear();
                    daysWithMinKhac0.add(ngay);
                } else if (soVe == minSoVe2) {
                    daysWithMinKhac0.add(ngay);
                }

                // Đếm số ngày có doanh thu khác 0
                if (soVe > 0) {
                    daysWithRevenue++;
                }
            }

            // Tính doanh thu trung bình
            double averageDoanhThu = (double) totalSoVe / countDays;

            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu cao nhất
            String daysWithMaxText = String.join(", ", daysWithMaxRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất
            String daysWithMinText = String.join(", ", daysWithMinRevenue);
            // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất khác 0
            String daysWithMinText2 = String.join(", ", daysWithMinKhac0);

            // Cập nhật thông tin cho label
            String infoText = String.format(
                    "Số vé cao nhất: %d (Ngày: %s)\nSố vé thấp nhất: %d (Ngày: %s)\nSố vé thấp nhất khác 0: %s (Ngày: %s)\nSố vé bán được trung bình: %.1f\nSố ngày bán được vé: %d/%d",
                    maxSoVe, daysWithMaxText,
                    minSoVe, daysWithMinText,
                    minSoVe2, daysWithMinText2,
                    averageDoanhThu,
                    daysWithRevenue, countDays
            );
            textThongTinVeBan.setText(infoText);
            yAxis.setUpperBound(maxSoVe + 1);
        }
    }

    private void updateChartDoanhThuTheoNam(Map<String, Double> dsMap){
        chartDoanhThuTheoNam.getData().clear();
        xAxis.setAutoRanging(true);
        int selectedNam = cboboxNam.getValue();

        // Chuyển đổi Map thành List và sắp xếp theo thứ tự tháng (1 đến 12)
        List<Map.Entry<String, Double>> entryList = new ArrayList<>(dsMap.entrySet());

        entryList.sort((entry1, entry2) -> {
            // Loại bỏ tiền tố "Tháng " và chuyển đổi tháng thành số nguyên để so sánh
            int month1 = Integer.parseInt(entry1.getKey().replace("Tháng ", "").trim());
            int month2 = Integer.parseInt(entry2.getKey().replace("Tháng ", "").trim());
            return Integer.compare(month1, month2);
        });
        // Tạo Series cho biểu đồ
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu theo tháng");
        chartDoanhThuTheoNam.setTitle("DOANH THU CÁC THÁNG TRONG NĂM " + selectedNam);
        for (Map.Entry<String, Double> entry : entryList) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
            // Lắng nghe sự kiện khi Node được tạo
            data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                if (newNode != null) { // Khi Node được tạo
                    Tooltip tooltip = new Tooltip("Doanh Thu: " + decimalFormat.format(entry.getValue()));
                    tooltip.setShowDelay(Duration.millis(100));
                    tooltip.setHideDelay(Duration.millis(200));
                    Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                }
            });
        }
        // Cập nhật biểu đồ
        chartDoanhThuTheoNam.getData().add(series);

        // ========== Cập nhật thông tin Doanh Thu =============
        double maxDoanhThu = 0;
        double minDoanhThu = Double.MAX_VALUE;
        double totalDoanhThu = hddao.getTongDoanhThuTheoNam(selectedNam);
        List<String> monthWithMaxRevenue = new ArrayList<>();
        List<String> monthWithMinRevenue = new ArrayList<>();

        double previousTotalDoanhThu = hddao.getTongDoanhThuTheoNam(selectedNam - 1);
        double revenueGrowth = 0; // Tăng trưởng doanh thu

        // Duyệt qua từng ngày để tính các thông số
        for (Map.Entry<String, Double> entry : dsMap.entrySet()) {
            String thang = entry.getKey();
            double doanhThu = entry.getValue();

            // Kiểm tra và cập nhật mức doanh thu cao nhất
            if (doanhThu > maxDoanhThu) {
                maxDoanhThu = doanhThu;
                monthWithMaxRevenue.clear(); // Xóa danh sách cũ nếu tìm thấy doanh thu cao hơn
                monthWithMaxRevenue.add(thang); // Thêm ngày hiện tại vào danh sách
            } else if (doanhThu == maxDoanhThu) {
                monthWithMaxRevenue.add(thang); // Thêm ngày vào danh sách nếu doanh thu bằng mức cao nhất
            }

            // Kiểm tra và cập nhật mức doanh thu thấp nhất
            if (doanhThu < minDoanhThu) {
                minDoanhThu = doanhThu;
                monthWithMinRevenue.clear();
                monthWithMinRevenue.add(thang);
            } else if (doanhThu == minDoanhThu) {
                monthWithMinRevenue.add(thang);
            }
        }

        // Tính doanh thu trung bình
        double averageDoanhThu = totalDoanhThu / dsMap.size();

        // Tính tăng trưởng doanh thu nếu có dữ liệu kỳ trước
        if (previousTotalDoanhThu > 0) {
            revenueGrowth = ((totalDoanhThu - previousTotalDoanhThu) / previousTotalDoanhThu) * 100;
        }

        // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu cao nhất
        String monthsWithMaxText = String.join(", ", monthWithMaxRevenue);
        // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất
        String monthsWithMinText = String.join(", ", monthWithMinRevenue);

        // Cập nhật thông tin cho label
        String infoText = String.format(
                "Doanh thu cao nhất: %s (Tháng: %s)\nDoanh thu thấp nhất: %s (Tháng: %s)\nDoanh thu trung bình: %s\nTăng trưởng doanh thu so với năm trước: %.2f%%",
                decimalFormat.format(maxDoanhThu), monthsWithMaxText,
                decimalFormat.format(minDoanhThu), monthsWithMinText,
                decimalFormat.format(averageDoanhThu),
                revenueGrowth
        );
        textThongTinDoanhThu.setText(infoText);
    }
    private void updateChartSoVeTheoNam(Map<String, Integer> dsMap){
        chartSoLuongVeBan.getData().clear();
        NumberAxis yAxis = (NumberAxis) chartSoLuongVeBan.getYAxis();
        xAxis.setAutoRanging(true);
        int selectedNam = cboboxNam.getValue();

        // Chuyển đổi Map thành List và sắp xếp theo thứ tự tháng (1 đến 12)
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(dsMap.entrySet());

        entryList.sort((entry1, entry2) -> {
            // Loại bỏ tiền tố "Tháng " và chuyển đổi tháng thành số nguyên để so sánh
            int month1 = Integer.parseInt(entry1.getKey().replace("Tháng ", "").trim());
            int month2 = Integer.parseInt(entry2.getKey().replace("Tháng ", "").trim());
            return Integer.compare(month1, month2);
        });
        // Tạo Series cho biểu đồ
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số vé theo tháng");
        chartSoLuongVeBan.setTitle("SỐ VÉ BÁN CÁC THÁNG TRONG NĂM " + selectedNam);
        for (Map.Entry<String, Integer> entry : entryList) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
            // Lắng nghe sự kiện khi Node được tạo
            data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                if (newNode != null) { // Khi Node được tạo
                    Tooltip tooltip = new Tooltip("Số vé: " + entry.getValue());
                    tooltip.setShowDelay(Duration.millis(100));
                    tooltip.setHideDelay(Duration.millis(200));
                    Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                }
            });

        }
        // Cập nhật biểu đồ
        chartSoLuongVeBan.getData().add(series);

        // ========== Cập nhật thông tin Doanh Thu =============
        int maxSoVe = 0;
        int minSoVe = Integer.MAX_VALUE;
        int totalVe = dsMap.values().stream().mapToInt(Integer::intValue).sum();
        List<String> monthsWithMaxRevenue = new ArrayList<>();
        List<String> monthsWithMinRevenue = new ArrayList<>();

        int daysWithRevenue = 0;
        int previousTotalDoanhThu = vedao.getAllVeTheoNam(selectedNam - 1).size();
        double revenueGrowth = 0;

        // Duyệt qua từng tháng để tính các thông số
        for (Map.Entry<String, Integer> entry : dsMap.entrySet()) {
            String ngay = entry.getKey();
            double doanhThu = entry.getValue();

            // Kiểm tra và cập nhật mức doanh thu cao nhất
            if (doanhThu > maxSoVe) {
                maxSoVe = (int) doanhThu;
                monthsWithMaxRevenue.clear();
                monthsWithMaxRevenue.add(ngay);
            } else if (doanhThu == maxSoVe) {
                monthsWithMaxRevenue.add(ngay);
            }

            // Kiểm tra và cập nhật mức doanh thu thấp nhất
            if (doanhThu < minSoVe) {
                minSoVe = (int) doanhThu;
                monthsWithMinRevenue.clear();
                monthsWithMinRevenue.add(ngay);
            } else if (doanhThu == minSoVe) {
                monthsWithMinRevenue.add(ngay);
            }
        }

        // Tính doanh thu trung bình
        double averageDoanhThu = (double) totalVe / dsMap.size();

        // Tính tăng trưởng doanh thu nếu có dữ liệu kỳ trước
        if (previousTotalDoanhThu > 0) {
            revenueGrowth = ((double) (totalVe - previousTotalDoanhThu) / previousTotalDoanhThu) * 100;
        }

        // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu cao nhất
        String daysWithMaxText = String.join(", ", monthsWithMaxRevenue);
        // Chuẩn bị chuỗi văn bản cho các ngày có doanh thu thấp nhất
        String daysWithMinText = String.join(", ", monthsWithMinRevenue);

        // Cập nhật thông tin cho label
        String infoText = String.format(
                "Số vé cao nhất: %d (Tháng: %s)\nSố vé thấp nhất: %d (Tháng: %s)\nSố vé trung bình: %.0f\nTăng trưởng số vé so với năm trước: %.2f%%",
                maxSoVe, daysWithMaxText,
                minSoVe, daysWithMinText,
                averageDoanhThu,
                revenueGrowth
        );
        textThongTinVeBan.setText(infoText);
        yAxis.setUpperBound(maxSoVe + 1);
    }

    private void updateChartKhachHang(Map<LoaiHanhKhach, Integer> dskh) {
        chartLoaiKhachHang.getData().clear();

        // Tính tổng giá trị của tất cả các phần trong biểu đồ
        double total = dskh.values().stream().mapToInt(Integer::intValue).sum();
        // Lặp qua các mục trong danh sách và thêm vào biểu đồ
        for (Map.Entry<LoaiHanhKhach, Integer> entry : dskh.entrySet()) {
            // Tạo phần của Pie chart
            PieChart.Data slice = new PieChart.Data(entry.getKey().getName(), entry.getValue());

            // Tính phần trăm của phần này
            double percentage = (entry.getValue() / total) * 100;

            // Cập nhật tên của phần để hiển thị phần trăm
            slice.setName(String.format("%s (%.1f%%)", entry.getKey().getName(), percentage));

            // Thêm phần vào biểu đồ
            chartLoaiKhachHang.getData().add(slice);

            // Thêm Tooltip cho phần pie chart
            Tooltip tooltip = new Tooltip(String.format("%s: %.1f%% \nSố lượng: %d", entry.getKey().getName(), percentage, entry.getValue()));

            // Đặt thời gian trễ khi hiển thị Tooltip (0 ms để không có delay)
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.setHideDelay(Duration.millis(200));  // Vẫn giữ thời gian ẩn Tooltip

            // Đảm bảo Tooltip được áp dụng đúng vào mỗi phần của Pie chart
            Tooltip.install(slice.getNode(), tooltip);
        }


    }

    private void updateChartGhe(Map<LoaiGhe, Integer> dsGhe) {
        chartLoaiGhe.getData().clear();

        // Tính tổng giá trị của tất cả các phần trong biểu đồ
        double total = dsGhe.values().stream().mapToInt(Integer::intValue).sum();

        // Lặp qua các mục trong danh sách và thêm vào biểu đồ
        for (Map.Entry<LoaiGhe, Integer> entry : dsGhe.entrySet()) {
            // Tạo phần của Pie chart
            PieChart.Data slice = new PieChart.Data(entry.getKey().getName(), entry.getValue());

            // Tính phần trăm của phần này
            double percentage = (entry.getValue() / total) * 100;

            // Cập nhật tên của phần để hiển thị phần trăm
            slice.setName(String.format("%s (%.1f%%)", entry.getKey().getName(), percentage));

            // Thêm phần vào biểu đồ
            chartLoaiGhe.getData().add(slice);

            // Thêm Tooltip cho phần pie chart
            Tooltip tooltip = new Tooltip(String.format("%s: %.1f%% \nSố lượng: %d", entry.getKey().getName(), percentage, entry.getValue()));

            // Đặt thời gian trễ khi hiển thị Tooltip (0 ms để không có delay)
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.setHideDelay(Duration.millis(200));  // Vẫn giữ thời gian ẩn Tooltip

            // Đảm bảo Tooltip được áp dụng đúng vào mỗi phần của Pie chart
            Tooltip.install(slice.getNode(), tooltip);
        }
    }

    private void updateChartDoanhThuTau(Map<Double, String> dsTau) {
        chartDoanhThuTau.getData().clear();
        xAxis.setAutoRanging(true);

        // Sắp xếp danh sách các tàu theo doanh thu tăng dần
        List<Map.Entry<Double, String>> sortedList = new ArrayList<>(dsTau.entrySet());
        sortedList.sort(Map.Entry.comparingByValue());

        // Tạo series mới cho biểu đồ
        XYChart.Series<Double, String> series = new XYChart.Series<>();
        chartDoanhThuTau.setLegendVisible(false);

        // Tạo danh sách màu sắc cho các tàu
        List<String> colors = Arrays.asList("red", "blue", "green", "orange", "purple", "pink", "brown");
        AtomicInteger colorIndex = new AtomicInteger();

        // Tính toán tổng doanh thu và các giá trị cần thiết khác
        double totalRevenue = 0;
        double highestRevenue = Double.MIN_VALUE;
        double lowestRevenue = Double.MAX_VALUE;
        String highestRevenueShip = "";
        String lowestRevenueShip = "";

        // Thêm dữ liệu vào series, mỗi tàu sẽ có màu riêng và tooltip
        for (Map.Entry<Double, String> entry : sortedList) {

            double revenue = entry.getKey();
            String ship = entry.getValue();

            // Cập nhật doanh thu cao nhất và thấp nhất
            if (revenue > highestRevenue) {
                highestRevenue = revenue;
                highestRevenueShip = ship;
            }
            if (revenue < lowestRevenue) {
                lowestRevenue = revenue;
                lowestRevenueShip = ship;
            }

            totalRevenue += revenue;
            XYChart.Data<Double, String> data = new XYChart.Data<>(entry.getKey(), entry.getValue());

            // Gán màu cho từng tàu
            data.nodeProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.setStyle("-fx-bar-fill: " + colors.get(colorIndex.get() % colors.size()) + ";");
                    colorIndex.getAndIncrement();

                    // Thêm Tooltip cho từng cột
                    Tooltip tooltip = new Tooltip(entry.getValue() + "\nDoanh thu: " + decimalFormat.format(entry.getKey()));
                    // Đặt thời gian trễ khi hiển thị Tooltip (ví dụ: 100 ms)
                    tooltip.setShowDelay(Duration.millis(100));
                    // Đặt thời gian trễ khi ẩn Tooltip (ví dụ: 200 ms)
                    tooltip.setHideDelay(Duration.millis(200));
                    Tooltip.install(newValue, tooltip);
                }
            });
            // Cập nhật label thông tin bổ sung
            String averageRevenue = decimalFormat.format(totalRevenue / sortedList.size());
            String chartInfo = String.format("""
                            Tàu có doanh thu cao nhất: %s - %s
                            Tàu có doanh thu thấp nhất: %s - %s
                            Tổng doanh thu: %s
                            Doanh thu trung bình: %s
                            """,
                    highestRevenueShip, decimalFormat.format(highestRevenue),
                    lowestRevenueShip, decimalFormat.format(lowestRevenue),
                    decimalFormat.format(totalRevenue),
                    averageRevenue);

            // Hiển thị thông tin trên giao diện (label, hoặc bất cứ component nào khác)
            textDoanhThuTau.setText(chartInfo); // Giả sử bạn có label này trong giao diện
            // Thêm dữ liệu vào series
            series.getData().add(data);
        }

        // Làm mới biểu đồ
        chartDoanhThuTau.getData().add(series);
    }

    private void updateChartSoChuyenTau(Map<String, Integer> dsMap){
        chartSoChuyenDi.getData().clear();
        NumberAxis yAxis = (NumberAxis) chartSoChuyenDi.getYAxis();
        xAxis.setAutoRanging(true);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        // Sắp xếp danh sách các tàu theo doanh thu tăng dần
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(dsMap.entrySet());
        sortedList.sort(Map.Entry.comparingByKey());

        // Tạo danh sách màu sắc cho các tàu
        List<String> colors = Arrays.asList("red", "blue", "green", "orange", "purple", "pink", "brown");
        AtomicInteger colorIndex = new AtomicInteger();

        // Tạo Series cho biểu đồ
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        chartSoChuyenDi.setLegendVisible(false);

        for (Map.Entry<String, Integer> entry : sortedList) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
            // Lắng nghe sự kiện khi Node được tạo
            data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                if (newNode != null) { // Khi Node được tạo
                    newNode.setStyle("-fx-bar-fill: " + colors.get(colorIndex.get() % colors.size()) + ";");
                    colorIndex.getAndIncrement();

                    Tooltip tooltip = new Tooltip("Số tuyến: " + entry.getValue());
                    tooltip.setShowDelay(Duration.millis(100));
                    tooltip.setHideDelay(Duration.millis(200));
                    Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                }
            });
        }
        // Cập nhật biểu đồ
        chartSoChuyenDi.getData().add(series);
    }

    private void updateChartSoTuyen(Map<String, Integer> dsMap) {
        chartCacTuyen.getData().clear();

        // Tính tổng giá trị của tất cả các phần
        double total = dsMap.values().stream().mapToInt(Integer::intValue).sum();

        // Sắp xếp các mục theo số lượng giảm dần
        List<Map.Entry<String, Integer>> sortedEntries = dsMap.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // Sắp giảm dần
                .toList();

        // Giới hạn số node hiển thị (10 node đầu tiên)
        int maxNodes = 5;
        List<Map.Entry<String, Integer>> topEntries = sortedEntries.stream().limit(maxNodes).toList();

        // Nhóm các mục còn lại thành "Khác"
        int otherTotal = sortedEntries.stream()
                .skip(maxNodes)
                .mapToInt(Map.Entry::getValue)
                .sum();

        // Tạo danh sách kết hợp: Top + Khác
        List<Map.Entry<String, Integer>> finalEntries = new ArrayList<>(topEntries);
        if (otherTotal > 0) {
            finalEntries.add(Map.entry("Khác (n = " + otherTotal + ")", otherTotal));
        }

        // Lặp qua các mục và thêm vào biểu đồ
        for (Map.Entry<String, Integer> entry : finalEntries) {
            // Tạo phần của Pie chart
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());

            // Tính phần trăm của phần này
            double percentage = (entry.getValue() / total) * 100;

            // Cập nhật tên của phần để hiển thị phần trăm
            slice.setName(String.format("%s (%.1f%%)", entry.getKey(), percentage));

            // Thêm phần vào biểu đồ
            chartCacTuyen.getData().add(slice);

            // Thêm Tooltip cho phần pie chart
            Tooltip tooltip = new Tooltip(String.format("%s: %.1f%% \nSố lượng: %d", entry.getKey(), percentage, entry.getValue()));

            // Đặt thời gian trễ khi hiển thị Tooltip (0 ms để không có delay)
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.setHideDelay(Duration.millis(200)); // Vẫn giữ thời gian ẩn Tooltip

            // Đảm bảo Tooltip được áp dụng đúng vào mỗi phần của Pie chart
            Tooltip.install(slice.getNode(), tooltip);
        }

        // Đảm bảo legend hiển thị đúng
        chartCacTuyen.setLegendVisible(true);
    }

    private void updateChartGioDi(Map<String, Integer> dsMap) {
        chartThoiGian.getData().clear();
        chartThoiGian.setLegendVisible(false);

        // Sắp xếp Map theo key (thời gian)
        Map<String, Integer> sortedMap = dsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // Sắp xếp theo key (thời gian)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));  // Lưu vào LinkedHashMap để giữ thứ tự sắp xếp

        // Tạo Series cho biểu đồ
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int highestTickets = Integer.MIN_VALUE;
        int lowestTickets = Integer.MAX_VALUE;
        String highestTime = "";
        String lowestTime = "";

        // Cập nhật tổng vé và các giá trị cao nhất/thấp nhất
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            String time = entry.getKey();
            int tickets = entry.getValue();

            if (tickets > highestTickets) {
                highestTickets = tickets;
                highestTime = time;
            }
            if (tickets < lowestTickets) {
                lowestTickets = tickets;
                lowestTime = time;
            }

            XYChart.Data<String, Number> data = new XYChart.Data<>(time, tickets);
            series.getData().add(data);

            // Lắng nghe sự kiện khi Node được tạo
            data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                if (newNode != null) { // Khi Node được tạo
                    Tooltip tooltip = new Tooltip("Số vé: " + tickets);
                    tooltip.setShowDelay(Duration.millis(100));
                    tooltip.setHideDelay(Duration.millis(200));
                    Tooltip.install(newNode, tooltip); // Gắn Tooltip vào Node
                }
            });
        }

        // Cập nhật thông tin trên giao diện
        String chartInfo = String.format("Thời gian cao điểm: %s - %d vé\n" +
                        "Thời gian thấp điểm: %s - %d vé",
                highestTime, highestTickets,
                lowestTime, lowestTickets);

        // Hiển thị thông tin trên giao diện (label, hoặc bất cứ component nào khác)
        textThoiGianDiDuocChon.setText(chartInfo); // Giả sử bạn có label này trong giao diện

        // Cập nhật biểu đồ
        chartThoiGian.getData().add(series);
    }

}
