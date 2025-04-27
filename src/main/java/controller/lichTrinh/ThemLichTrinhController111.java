package controller.lichTrinh;

import com.jfoenix.controls.JFXCheckBox;
import controller.Menu.MenuController;
import entity.LichTrinh;
import entity.Tau;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import rmi.RMIServiceLocator;
import service.KhuyenMaiService;
import service.LichTrinhService;
import service.TauService;
import util.*;

public class ThemLichTrinhController111 {
    @FXML
    private AnchorPane map_pane, anchorPane;
    @FXML
    private Button btnHuyBo, btnLuuLai, btnThemMoi, btnTroLai;
    @FXML
    private ComboBox<Integer> cboxGioKH, cboxGioKT, cboxPhutKH, cboxPhutKT;
    @FXML
    private ComboBox<String> cboxGaKhoiHanh, cboxGaKetThuc, cboxTau;
    @FXML
    private TableColumn<LichTrinh, String> gaKHColumn, gaKTColumn;
    @FXML
    private TableColumn<LichTrinh, LocalTime> gioKTColumn, gioKHColumn;
    @FXML
    private Label labelDanhSachLT, label_GaDung;
    @FXML
    private TableColumn<LichTrinh, String> maLTColumn, maTauColumn, trangThaiColumn;
    @FXML
    private TableColumn<LichTrinh, LocalDate> ngayKHColumn, ngayKTColumn;
    @FXML
    private DatePicker ngayKH_DatePicker, ngayKT_DatePicker;
    @FXML
    private TableView<LichTrinh> tableThemLT;
    @FXML
    private JFXCheckBox checkbox_DungAllGa;
    private boolean isDungAllGaChecked = false;
    @FXML
    private Circle circle_DongHoi, circle_DaNang, circle_HaNoi, circle_HaiPhong, circle_Hue, circle_LaoCai, circle_NhaTrang, circle_PhanThiet, circle_QuyNhon,
            circle_SaiGon, circle_ThanhHoa, circle_Vinh, circle_VungTau;
    @FXML
    private Line line_DaNang_QuyNhon, line_DongHoi_Hue, line_HaNoi_HaiPhong, line_HaNoi_ThanhHoa, line_Hue_DaNang, line_LaoCai_HaNoi, line_NhaTrang_PhanThiet,
            line_PhanThiet_SaiGon, line_QuyNhon_NhaTrang, line_SaiGon_VungTau, line_ThanhHoa_Vinh, line_Vinh_DongHoi;

    private final ObservableList<LichTrinh> danhSachLichTrinh = FXCollections.observableArrayList();
    private final TauService tauService = RMIServiceLocator.getTauService();
    private LichTrinhService lichTrinhService = RMIServiceLocator.getLichTrinhService();;
    Map<String, String> dsGa = new HashMap<>();

    @FXML
    public void initialize() {
        loadTauComboBox();
        loadThoiGianComboBox();
        loadGaComboBox();

        // Sự kiện thay đổi giá trị giờ (ComboBox)
        cboxGioKH.setOnAction(event -> {
            Integer selectedHour = cboxGioKH.getValue();
            if (selectedHour != null && selectedHour == 24) {
                cboxPhutKH.setValue(0);
                cboxPhutKH.setDisable(true);  // Tắt chọn phút
            } else {
                cboxPhutKH.setDisable(false);  // Cho phép chọn phút lại
            }
        });

        // Sự kiện thay đổi giá trị giờ (ComboBox)
        cboxGioKT.setOnAction(event -> {
            Integer selectedHour = cboxGioKT.getValue();
            if (selectedHour != null && selectedHour == 24) {
                cboxPhutKT.setValue(0);
                cboxPhutKT.setDisable(true);  // Tắt chọn phút
            } else {
                cboxPhutKT.setDisable(false);  // Cho phép chọn phút lại
            }
        });

        khoiTaoDanhSachKetNoiCacGa();
        khoiTaoCacLines();
        khoiTaoCacCircles();
        khoiTaoThoiGianGiua2Ga();
        checkbox_DungAllGa.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isDungAllGaChecked = newValue;
        });
    }

    public void loadTauComboBox() {
        try {
            List<Tau> danhSachTau = tauService.getAllTau();
            // Lấy danh sách mã tàu từ danh sách tàu
            ObservableList<String> danhSachMaTau = FXCollections.observableArrayList(
                    danhSachTau.stream()
                            .map(Tau::getMaTau) // Lấy ra maTau từ mỗi đối tượng Tau
                            .toList()           // Chuyển thành List
            );
            // Set danh sách mã tàu vào ComboBox
            cboxTau.setItems(danhSachMaTau);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

    public void loadThoiGianComboBox() {
        List<Integer> dsGio = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            dsGio.add(i);
        }
        List<Integer> dsPhut = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            dsPhut.add(i);
        }
        cboxGioKH.setItems(FXCollections.observableArrayList(dsGio));
        cboxGioKT.setItems(FXCollections.observableArrayList(dsGio));
        cboxPhutKH.setItems(FXCollections.observableArrayList(dsPhut));
        cboxPhutKT.setItems(FXCollections.observableArrayList(dsPhut));
    }

    private void khoiTaoDanhSachGa(){
        dsGa.put("Hà Nội", "HaNoi");
        dsGa.put("Sài Gòn", "SaiGon");
        dsGa.put("Đà Nẵng", "DaNang");
        dsGa.put("Nha Trang", "NhaTrang");
        dsGa.put("Huế", "Hue");
        dsGa.put("Hải Phòng", "HaiPhong");
        dsGa.put("Vũng Tàu", "VungTau");
        dsGa.put("Quy Nhơn", "QuyNhon");
        dsGa.put("Phan Thiết", "PhanThiet");
        dsGa.put("Lào Cai", "LaoCai");
        dsGa.put("Thanh Hóa", "ThanhHoa");
        dsGa.put("Vinh", "Vinh");
        dsGa.put("Đồng Hới", "DongHoi");
    }
    public void loadGaComboBox() {
        khoiTaoDanhSachGa();
        cboxGaKhoiHanh.setItems(FXCollections.observableArrayList(dsGa.keySet().stream().toList()));
        cboxGaKetThuc.setItems(FXCollections.observableArrayList(dsGa.keySet()));
    }

    public boolean kiemTraLichTrinh(){
        String maTau = cboxTau.getValue();
        String gaKH = cboxGaKhoiHanh.getValue();
        String gaKT = cboxGaKetThuc.getValue();
        LocalDate ngayKH = ngayKH_DatePicker.getValue();
        if(gaKH == null){
            showAlert("Cảnh báo", "Phải chọn ga khởi hành", Alert.AlertType.WARNING);
            cboxGaKhoiHanh.requestFocus();
            return false;
        }
        if(gaKT == null){
            showAlert("Cảnh báo", "Phải chọn ga kết thúc", Alert.AlertType.WARNING);
            cboxGaKetThuc.requestFocus();
            return false;
        }
        if(maTau == null){
            showAlert("Cảnh báo", "Phải chọn tàu", Alert.AlertType.WARNING);
            cboxTau.requestFocus();
            return false;
        }
        if(ngayKH == null){
            showAlert("Cảnh báo", "Phải chọn ngày khởi hành", Alert.AlertType.WARNING);
            ngayKH_DatePicker.requestFocus();
            return false;
        }
        if(ngayKH.isBefore(LocalDate.now().plusDays(80))){
            showAlert("Cảnh báo", "Ngày khởi hành phải được tạo trước 80 ngày", Alert.AlertType.WARNING);
            ngayKH_DatePicker.requestFocus();
            return false;
        }
        if(cboxGioKH.getValue() == null || cboxPhutKH.getValue() == null){
            showAlert("Cảnh báo", "Phải chọn thời gian khởi hành", Alert.AlertType.WARNING);
            cboxGioKH.requestFocus();
            return false;
        }
        return true;
    }

    private boolean loadLTvaoTable(){
        danhSachLichTrinh.clear();
        if(kiemTraLichTrinh())
            dsLTbenduoi = taoDanhSachLichTrinh(gaTheoLichTrinhList);
        capNhatThongTin();
        if(!dsLTbenduoi.isEmpty()){
            danhSachLichTrinh.addAll(dsLTbenduoi);
            tableThemLT.setItems(danhSachLichTrinh);
            return true;
        }
        return false;
    }

    private void setValueTable() {
        maLTColumn.setCellValueFactory(new PropertyValueFactory<>("maLt"));
        gaKHColumn.setCellValueFactory(new PropertyValueFactory<>("gaKhoiHanh"));
        gaKTColumn.setCellValueFactory(new PropertyValueFactory<>("gaKetThuc"));
        ngayKHColumn.setCellValueFactory(new PropertyValueFactory<>("ngayKhoiHanh"));
        ngayKTColumn.setCellValueFactory(new PropertyValueFactory<>("ngayDen"));
        gioKHColumn.setCellValueFactory(new PropertyValueFactory<>("gioDi"));
        maTauColumn.setCellValueFactory(cellData -> {
            Tau tau = cellData.getValue().getTauByMaTau(); // Lấy đối tượng Tau
            return tau != null ? new SimpleStringProperty(tau.getMaTau()) : new SimpleStringProperty("");
        });
        gioKTColumn.setCellValueFactory(new PropertyValueFactory<>("gioDen"));
        trangThaiColumn.setCellValueFactory(cellData -> {
            int trangThai = cellData.getValue().getTrangThai();
            String text = switch (trangThai) {
                case -1 -> "Đã hủy";
                case 0 -> "Chưa khởi hành";
                case 1 -> "Đã khởi hành";
                default -> "Không xác định";
            };
            return new SimpleStringProperty(text);
        });
    }

    private void themMoi() {
        boolean kiemTra = loadLTvaoTable();
        setValueTable();
        if (!danhSachLichTrinh.isEmpty()) {
            btnLuuLai.setDisable(false);
        }
        tableThemLT.getSelectionModel().clearSelection();
    }
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.show();
    }

    // ================== Xử lí bản đồ ====================
    private Map<String, List<String>> connections;
    private Map<String, Line> lines;
    private Map<String, Circle> circles;
    private Map<String, Integer> travelTimes;
    private void khoiTaoDanhSachKetNoiCacGa(){
        connections = new HashMap<>();
        connections.put("LaoCai", List.of("HaNoi"));
        connections.put("HaNoi", List.of("LaoCai", "HaiPhong", "ThanhHoa"));
        connections.put("HaiPhong", List.of("HaNoi"));
        connections.put("ThanhHoa", List.of("HaNoi", "Vinh"));
        connections.put("Vinh", List.of("ThanhHoa", "DongHoi"));
        connections.put("DongHoi", List.of("Vinh", "Hue"));
        connections.put("Hue", List.of("DaNang", "DongHoi"));
        connections.put("DaNang", List.of("Hue", "QuyNhon"));
        connections.put("QuyNhon", List.of("NhaTrang", "DaNang"));
        connections.put("NhaTrang", List.of("QuyNhon", "PhanThiet"));
        connections.put("PhanThiet", List.of("NhaTrang", "SaiGon"));
        connections.put("SaiGon", List.of("PhanThiet", "VungTau"));
        connections.put("VungTau", List.of("SaiGon"));
    }
    private void khoiTaoCacLines(){
        lines = new HashMap<>();
        lines.put("line_LaoCai_HaNoi", line_LaoCai_HaNoi);
        lines.put("line_HaNoi_HaiPhong", line_HaNoi_HaiPhong);
        lines.put("line_HaNoi_ThanhHoa", line_HaNoi_ThanhHoa);
        lines.put("line_ThanhHoa_Vinh", line_ThanhHoa_Vinh);
        lines.put("line_Vinh_DongHoi", line_Vinh_DongHoi);
        lines.put("line_DongHoi_Hue", line_DongHoi_Hue);
        lines.put("line_Hue_DaNang", line_Hue_DaNang);
        lines.put("line_DaNang_QuyNhon", line_DaNang_QuyNhon);
        lines.put("line_QuyNhon_NhaTrang", line_QuyNhon_NhaTrang);
        lines.put("line_NhaTrang_PhanThiet", line_NhaTrang_PhanThiet);
        lines.put("line_PhanThiet_SaiGon", line_PhanThiet_SaiGon);
        lines.put("line_SaiGon_VungTau", line_SaiGon_VungTau);
    }
    private void khoiTaoCacCircles(){
        circles = new HashMap<>();
        circles.put("circle_LaoCai", circle_LaoCai);
        circles.put("circle_HaNoi", circle_HaNoi);
        circles.put("circle_HaiPhong", circle_HaiPhong);
        circles.put("circle_ThanhHoa", circle_ThanhHoa);
        circles.put("circle_Vinh", circle_Vinh);
        circles.put("circle_DongHoi", circle_DongHoi);
        circles.put("circle_Hue", circle_Hue);
        circles.put("circle_DaNang", circle_DaNang);
        circles.put("circle_QuyNhon", circle_QuyNhon);
        circles.put("circle_NhaTrang", circle_NhaTrang);
        circles.put("circle_PhanThiet", circle_PhanThiet);
        circles.put("circle_VungTau", circle_VungTau);
        circles.put("circle_SaiGon", circle_SaiGon);
    }

    private void khoiTaoThoiGianGiua2Ga(){
        travelTimes = new HashMap<>();
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
    private final List<String> gaConListTam = new ArrayList<>();
    private List<String> gaConList = new ArrayList<>();
    private List<String> gaDungList = new ArrayList<>();
    private List<String> gaTheoLichTrinhList = new ArrayList<>();
    private String startGa, endGa;
    private Circle circleDiHT, circleDenHT;
    private List<LichTrinh> dsLTbenduoi = new ArrayList<>();

    public void onCircleClick(MouseEvent mouseEvent) {
        // Lấy đối tượng Circle từ sự kiện
        Circle clickedCircle = (Circle) mouseEvent.getSource();
        String ga = clickedCircle.getId().replace("circle_", "");

        if (startGa == null && endGa == null) {
            // Nếu cả start và end đều chưa được chọn, chọn điểm đầu
            startGa = ga;
            System.out.println("Start: " + startGa);
            //Cập nhật cbobox
            capNhatComboBoxGa(cboxGaKhoiHanh, startGa);
            // Đổi màu điểm bắt đầu
            clickedCircle.setFill(Color.RED);
        } else if (startGa != null && endGa == null) {
            // Nếu đã chọn điểm đầu nhưng chưa có điểm kết thúc, chọn điểm kết thúc
            if (startGa.equals(ga)) {
                // Nếu click lại vào ga đã chọn là start, hủy chọn điểm bắt đầu
                startGa = null;
                cboxGaKhoiHanh.setValue("--Chọn ga--");
                clickedCircle.setFill(Color.WHITE); // Hoặc màu mặc định
                System.out.println("Điểm bắt đầu bị hủy");

            } else {
                // Chọn điểm kết thúc
                endGa = ga;
                System.out.println("End: " + endGa);
                capNhatComboBoxGa(cboxGaKetThuc, endGa);
                // Kiểm tra và tô màu đường đi
                toMauDuongDi(startGa, endGa);
                // Đổi màu điểm kết thúc
                clickedCircle.setFill(Color.BLUE);
            }
        } else if (startGa == null) {
            // Nếu không có startGa nhưng có endGa
            if (endGa.equals(ga)) {
                // Nếu click lại vào ga đã chọn là end, hủy chọn điểm kết thúc
                endGa = null;
                cboxGaKetThuc.setValue("--Chọn ga--");
                clickedCircle.setFill(Color.WHITE); // Hoặc màu mặc định
                System.out.println("Điểm kết thúc bị hủy");
            } else {
                // Chọn lại điểm bắt đầu
                startGa = ga;
                System.out.println("Start: " + startGa);
                capNhatComboBoxGa(cboxGaKhoiHanh, startGa);
                toMauDuongDi(startGa, endGa);
                clickedCircle.setFill(Color.RED);
                // Kiểm tra lại màu cho ga kết thúc nếu cần
                if (endGa != null) {
                    Circle endCircle = getCircleByGa(endGa);
                    if (endCircle != null) {
                        endCircle.setFill(Color.BLUE); // Giữ màu cho ga kết thúc nếu nó vẫn tồn tại
                    }
                }
            }
        } else {
            // Nếu đã chọn cả start và end rồi, thì chỉ cho phép hủy chọn
            if (startGa.equals(ga)) {
                // Nếu click lại vào ga đã chọn là start, hủy chọn điểm bắt đầu
                startGa = null;
                cboxGaKhoiHanh.setValue("--Chọn ga--");
                xoaMau();
                clickedCircle.setFill(Color.WHITE); // Hoặc màu mặc định
                if (endGa != null) {
                    Circle endCircle = getCircleByGa(endGa);
                    if (endCircle != null) {
                        endCircle.setFill(Color.BLUE); // Giữ màu cho ga kết thúc nếu nó vẫn tồn tại
                    }
                }
                System.out.println("Điểm bắt đầu bị hủy");
            } else if (endGa.equals(ga)) {
                // Nếu click lại vào ga đã chọn là end, hủy chọn điểm kết thúc
                endGa = null;
                cboxGaKetThuc.setValue("--Chọn ga--");
                xoaMau();
                clickedCircle.setFill(Color.WHITE); // Hoặc màu mặc định
                if (startGa != null) {
                    Circle startCircle = getCircleByGa(startGa);
                    if (startCircle != null) {
                        startCircle.setFill(Color.RED); // Giữ màu cho ga kết thúc nếu nó vẫn tồn tại
                    }
                }
                System.out.println("Điểm kết thúc bị hủy");
            } else {
                // Kiểm tra xem ga có nằm trong danh sách ga con hợp lệ
                if (gaConList.contains(ga)) {
                    // Xử lý click vào các ga con
                    Circle circle = getCircleByGa(ga);

                    if (circle != null) {
                        // Kiểm tra trạng thái hiện tại của ga con
                        if (circle.getFill() == Color.ORANGE) {
                            // Nếu ga con đang được chọn (màu cam), bỏ chọn
                            circle.setFill(Color.WHITE);
                            System.out.println("Ga con " + ga + " bỏ chọn");
                        } else {
                            // Nếu ga con chưa được chọn (màu trắng), chọn lại ga con
                            circle.setFill(Color.ORANGE);
                            System.out.println("Ga con " + ga + " được chọn");
                        }
                    }
                } else {
                    System.out.println("Ga con " + ga + " không hợp lệ để chọn hoặc bỏ chọn");
                }
            }
        }
        capNhatThongTin();
    }
    public void actionGaKH(ActionEvent actionEvent) {
        String gaKhoiHanh = cboxGaKhoiHanh.getValue();
        if(gaKhoiHanh == null || gaKhoiHanh.equals("--Chọn ga--")) return;
        startGa = dsGa.get(gaKhoiHanh);
        if(startGa.equals(endGa) ) {
            showAlert("Cảnh báo", "Ga khởi hành không được trùng ga kết thúc", Alert.AlertType.WARNING);
            cboxGaKhoiHanh.setValue("--Chọn ga--");
            circleDiHT.setFill(Color.WHITE); // Màu mặc định
            xoaMau();
            return;
        }
        System.out.println("Start: " + startGa);

        // Reset màu cho Circle hiện tại
        if (circleDiHT != null) {
            circleDiHT.setFill(Color.WHITE); // Màu mặc định
            xoaMau();
        }

        // Lấy Circle mới và đặt màu đỏ
        Circle circleDuocChon = getCircleByGa(startGa);
        if (circleDuocChon != null) {
            circleDuocChon.setFill(Color.RED);
            circleDiHT = circleDuocChon;
        }

        // Cập nhật thông tin
        capNhatThongTin();
    }

    public void actionGaKT(ActionEvent actionEvent) {
        String gaKetThuc = cboxGaKetThuc.getValue();
        if (gaKetThuc == null || gaKetThuc.equals("--Chọn ga--")) return;
        endGa = dsGa.get(gaKetThuc);
        if(endGa.equals(startGa) ) {
            showAlert("Cảnh báo", "Ga kết thúc không được trùng ga bắt đầu", Alert.AlertType.WARNING);
            cboxGaKetThuc.setValue("--Chọn ga--");
            circleDenHT.setFill(Color.WHITE); // Màu mặc định
            xoaMau();
            return;
        }
        System.out.println("End: " + endGa);

        // Reset màu cho Circle hiện tại
        if (circleDenHT != null) {
            circleDenHT.setFill(Color.WHITE); // Màu mặc định
            xoaMau();
        }

        // Lấy Circle mới và đặt màu xanh dương
        Circle circleDuocChon = getCircleByGa(endGa);
        if (circleDuocChon != null) {
            circleDuocChon.setFill(Color.BLUE);
            circleDenHT = circleDuocChon;
        }

        // Cập nhật thông tin
        capNhatThongTin();
    }
    private void capNhatThongTin(){
        if (startGa == null || endGa == null){
            gaConList.clear();
            gaDungList.clear();
            gaTheoLichTrinhList.clear();
        }
        // Cập nhat thông tin khi chọn lại ga
        if(startGa != null && endGa != null){
            System.out.println("Đã chọn ga bắt đầu và kết thúc");
            toMauDuongDi(startGa, endGa);

            gaConList = layTenGaTuLinesDuocChon();
            gaDungList = layCacGaDung();
//            gaDungList.forEach(x -> System.out.println(x));
            gaTheoLichTrinhList = keepStartAndEndWithCommonMiddle(gaConList, gaDungList);

            // Lấy danh sách các ga giữa, bỏ ga đầu và ga cuối
            List<String> gaDungNe = new ArrayList<>(gaTheoLichTrinhList.subList(1, gaTheoLichTrinhList.size() - 1));

            for (int i = 0; i < gaDungNe.size(); i++) {
                String gaTach = gaDungNe.get(i);

                // Tìm tên đầy đủ bằng cách duyệt qua các giá trị (tên viết tắt là value)
                for (Map.Entry<String, String> entry : dsGa.entrySet()) {
                    if (entry.getValue().equals(gaTach)) {
                        gaDungNe.set(i, entry.getKey()); // Cập nhật tên đầy đủ vào list
                        break;
                    }
                }
            }

            // Ghép các phần tử lại thành chuỗi và cập nhật label
            String result = String.join(", ", gaDungNe);
            label_GaDung.setText(result);

//            gaTheoLichTrinhList.forEach(x -> System.out.println(x));
        }else System.out.println("Chưa chọn ga bắt đầu và kết thúc");
    }
    private void capNhatComboBoxGa(ComboBox<String> cbox,String valueGa) {
        // Duyệt qua tất cả các entry trong Map để tìm value
        for (Map.Entry<String, String> entry : dsGa.entrySet()) {
            // Kiểm tra xem value có trùng khớp với chuỗi input
            if (entry.getValue().equals(valueGa)) {
                // Cập nhật ComboBox với key (tên đầy đủ của ga)
                cbox.setValue(entry.getKey());
                break;  // Dừng vòng lặp khi đã tìm thấy
            }
        }
    }

    private void toMauDuongDi(String start, String end) {
        // Khởi tạo các cấu trúc lưu trữ
        Map<String, String> gaTruoc = new HashMap<>(); // Lưu ga trước của mỗi ga
        Set<String> duongDaDi = new HashSet<>(); // Lưu tuyến đã đi qua
        gaConListTam.add(start);
        // Bắt đầu DFS
        dfsTimDuongDi(start, end, gaTruoc, duongDaDi);

        // Tô màu đường đi
        String hienTai = end;
        while (gaTruoc.containsKey(hienTai)) {
            String truoc = gaTruoc.get(hienTai);

            // Tìm cả hai chiều để lấy line
            String lineID1 = "line_" + truoc + "_" + hienTai;
            String lineID2 = "line_" + hienTai + "_" + truoc;

            Line line = lines.get(lineID1);
            if (line == null) {
                line = lines.get(lineID2); // Nếu chiều thứ nhất không có, thử chiều ngược lại
            }

            if (line != null) {
                line.setStroke(Color.ORANGE); // Đổi màu Line
            }

            // Tô màu ga nếu checkbox "Dừng tất cả các ga" được chọn
            if (isDungAllGaChecked) {
                Circle circle = getCircleByGa(hienTai);
                if (circle != null) {
                    circle.setFill(Color.ORANGE);
                }
            }
            hienTai = truoc;
        }

        // Tô màu ga đầu (start) nếu checkbox "Dừng tất cả các ga" được chọn
        if (isDungAllGaChecked) {
            Circle startCircle = getCircleByGa(start);
            if (startCircle != null) {
                startCircle.setFill(Color.RED);
            }
        }
        if(isDungAllGaChecked){
            Circle endCircle = getCircleByGa(end);
            if (endCircle != null){
                endCircle.setFill(Color.BLUE);
            }
        }
    }

    private boolean dfsTimDuongDi(String gaHienTai, String gaKetThuc, Map<String, String> gaTruoc, Set<String> duongDaDi) {
        // Nếu đã đến ga kết thúc, kết thúc DFS
        if (gaHienTai.equals(gaKetThuc)) {
            return true;
        }

        // Duyệt các ga kế tiếp
        for (String neighbor : connections.getOrDefault(gaHienTai, Collections.emptyList())) {
            // Tìm cả hai chiều để đảm bảo không bỏ sót tuyến
            String lineID1 = "line_" + gaHienTai + "_" + neighbor;
            String lineID2 = "line_" + neighbor + "_" + gaHienTai;

            // Kiểm tra tuyến có hợp lệ (chưa đi qua)
            if ((!duongDaDi.contains(lineID1) && !duongDaDi.contains(lineID2)) && !gaTruoc.containsKey(neighbor)) {
                duongDaDi.add(lineID1); // Đánh dấu tuyến đã đi qua
                gaTruoc.put(neighbor, gaHienTai); // Lưu ga trước
                gaConListTam.add(neighbor);
                if (dfsTimDuongDi(neighbor, gaKetThuc, gaTruoc, duongDaDi)) {
                    return true; // Dừng nếu tìm thấy đường đi
                }
                // Nếu không tìm thấy đường đi, quay lui
                gaTruoc.remove(neighbor);
            }
        }

        return false; // Không tìm thấy đường đi
    }


    private void xoaMau() {
        // Nếu startGa bị hủy
        if (startGa == null) {
            // Đặt lại màu cho các ga con (trừ startGa)
            for (String ga : connections.keySet()) {
                Circle circle = getCircleByGa(ga);
                if (circle != null) {
                    circle.setFill(Color.WHITE);  // Đặt lại màu cho các ga con
                }
            }
        }

        // Nếu endGa bị hủy
        if (endGa == null) {
            // Đặt lại màu cho các ga con (trừ endGa)
            for (String ga : connections.keySet()) {
                Circle circle = getCircleByGa(ga);
                if (circle != null) {
                    circle.setFill(Color.WHITE);  // Đặt lại màu cho các ga con
                }
            }
        }

        // Đặt lại màu các đường nối thành màu trắng
        for (Line line : lines.values()) {
            line.setStroke(Color.WHITE);  // Đặt lại màu cho các đường nối
        }
    }
    private List<String> layTenGaTuLinesDuocChon(){
        List<Line> lineCams = new ArrayList<>();
        // Duyệt qua các Line trong Map
        for(Map.Entry<String, Line> entry : lines.entrySet()){
            Line line = entry.getValue();
            // Kiểm tra xem Line có màu mong muốn không
            if (line.getStroke().equals(Color.ORANGE)) {
                lineCams.add(line);
            }
        }

        Set<String> cacGa = new HashSet<>();
        for(Line line : lineCams){
            String lineId = line.getId();

            //Tách chuỗi lấy tên các ga
            String[] parts = lineId.split("_");
            if(parts.length == 3)
            {
                String ga1 = parts[1];
                String ga2 = parts[2];
                cacGa.add(ga1);
                cacGa.add(ga2);
            }
        }
        return gaConListTam.stream().filter(cacGa::contains).collect(Collectors.toList());
    }

    private List<String> layCacGaDung() {
        Set<String> cacGa = new HashSet<>();
        for (Map.Entry<String, Circle> entry : circles.entrySet()) {
            Circle circle = entry.getValue();
            if (circle.getFill().equals(Color.ORANGE)) {
                // Lấy phần tử sau dấu gạch dưới trong id của Circle
                String circleId = circle.getId();
                String[] parts = circleId.split("_");
                if (parts.length == 2) {
                    cacGa.add(parts[1]);
                }
            }
        }
        return new ArrayList<>(cacGa); // Chuyển Set thành List và trả về
    }

    private Circle getCircleByGa(String ga){
        return (Circle) map_pane.lookup("#circle_" + ga);
    }

    private List<LichTrinh> taoDanhSachLichTrinh(List<String> list) {
        try {
            // Lấy thông tin ban đầu: thời gian khởi hành và ngày khởi hành
            Time timeKH = Time.valueOf(LocalTime.of(cboxGioKH.getValue(), cboxPhutKH.getValue()));
            LocalDate ngayKH = ngayKH_DatePicker.getValue();
            Date dateKhoiHanh = Date.valueOf(ngayKH);
            String maTau = cboxTau.getValue();
            Tau tauByMa = tauService.timKiemId(maTau);

            List<Time> gioDiList = new ArrayList<>();
            List<Time> gioDenList = new ArrayList<>();
            List<Date> ngayKhoiHanhList = new ArrayList<>();
            List<Date> ngayKetThucList = new ArrayList<>();
            List<LichTrinh> dsLT = new ArrayList<>();
            Map<String, Time> gaToTime = new HashMap<>();  // Map lưu trữ thời gian khởi hành của từng ga

            // Lặp qua danh sách ga để tính toán thời gian và ngày khởi hành, đến
            for (int i = 0; i < list.size() - 1; i++) {
                // Tính tổng thời gian giữa 2 ga
                int thoigian = tinhTongThoiGianTheo2Ga(list.get(i), list.get(i + 1));

                // Tính số phút vượt qua 24 giờ
                long daysToAdd = thoigian / 1440; // 1 ngày = 1440 phút
                long minutesRemaining = thoigian % 1440; // Số phút còn lại sau khi trừ đi số ngày

                String gaKH = layTenGaDayDu(list.get(i));
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

                String gaKT = layTenGaDayDu(list.get(i + 1));

                // Thêm vào list giờ và ngày
                gioDiList.add(timeKhoiHanhMoi);
                gioDenList.add(gioDen);
                ngayKhoiHanhList.add(dateKhoiHanhMoi);
                ngayKetThucList.add(dateDen);

                // Cập nhật thời gian khởi hành cho ga tiếp theo
                gaToTime.put(gaKT, Time.valueOf(gioDenLocalTime.plusMinutes(10)));

                // Cập nhật ngày khởi hành (ngayKH) cho vòng lặp kế tiếp
                ngayKH = ngayKhoiHanhMoi;
            }

            // Sửa phần này với cú pháp chính xác khi truy cập vào các phần tử trong danh sách
            for (int i = 0; i < list.size() - 1; i++) { // Ga khởi hành
                for (int j = i + 1; j < list.size(); j++) { // Ga kết thúc
                    // Sử dụng các giá trị tương ứng từ các danh sách thời gian và ngày
                    Time gioDi = gioDiList.get(i); // Sửa từ gioDiList[i] thành gioDiList.get(i)
                    Time gioDen = gioDenList.get(j - 1); // Sửa từ gioDenList[j - 1] thành gioDenList.get(j - 1)
                    Date ngayKhoiHanh = ngayKhoiHanhList.get(i); // Sử dụng get(i) thay cho i
                    Date ngayKetThuc = ngayKetThucList.get(j - 1); // Sử dụng get(j - 1)

                    String gaKH = layTenGaDayDu(list.get(i));
                    String gaKT = layTenGaDayDu(list.get(j));

                    LocalTime gioKhoiHanhLocalTime = gioDi.toLocalTime();
                    LocalDate ngayKhoiHanhLocalDate = ngayKhoiHanh.toLocalDate();
                    // Tạo mã lịch trình
                    String maLT = maLTGenerator.generateMaLT(gaKH, gaKT, ngayKhoiHanhLocalDate, gioKhoiHanhLocalTime);
                    // Thêm Lịch Trình vào danh sách
                    dsLT.add(new LichTrinh(maLT, gaKH, gaKT, ngayKhoiHanh, gioDen, gioDi, ngayKetThuc, 0, tauByMa));
                }
            }
            return dsLT;
        }catch (RemoteException e){
            e.printStackTrace();
            return null;
        }

        // In danh sách Lịch Trình để kiểm tra
//        list.forEach(x -> System.out.println(x));
//        dsLT.forEach(x -> System.out.println(x));
//        gioDiList.forEach(x -> System.out.println(x));
//        gioDenList.forEach(x -> System.out.println(x));
//        ngayKhoiHanhList.forEach(x -> System.out.println(x));
//        ngayKetThucList.forEach(x -> System.out.println(x));

    }


    private int tinhTongThoiGianTheo2Ga(String ga1, String ga2) {
        int tong = 0;

        // Lấy index của ga1 và ga2 trong danh sách gaConList
        int index1 = gaConList.indexOf(ga1);
        int index2 = gaConList.indexOf(ga2);

        // Duyệt qua các ga từ ga1 đến ga2 và tính tổng thời gian giữa từng cặp ga liên tiếp
        for (int i = index1; i < index2; i++) {
            String gaHienTai = gaConList.get(i);
            String gaTiepTheo = gaConList.get(i + 1);

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

    private String layTenGaDayDu(String ga){
        for(Map.Entry<String, String> entry : dsGa.entrySet()){
            if(entry.getValue().equals(ga))
                return entry.getKey();
        }
        return "Không tìm thấy ga";
    }

    private void luuLai(){
        // Lấy dữ liệu từ bảng
        List<LichTrinh> lichTrinhList = new ArrayList<>(tableThemLT.getItems());

        // Kiểm tra nếu danh sách không rỗng
        if (!lichTrinhList.isEmpty()) {
            try {
                boolean success = lichTrinhService.luuDanhSachLichTrinh(lichTrinhList);

                if (success) {
                    showAlert("Thông báo", "Lưu thành công!", Alert.AlertType.CONFIRMATION);
                    MenuController.instance.readyUI("LichTrinh/ThemLichTrinh");
                } else {
                    showAlert("Thông báo", "Một số lịch trình không thể lưu!", Alert.AlertType.WARNING);
                }

            } catch (RemoteException e) {
                e.printStackTrace(); // In stack trace để dễ debug
                showAlert("Lỗi", "Có lỗi xảy ra khi lưu lịch trình!", Alert.AlertType.ERROR);
            }
        } else {
            // Thông báo nếu danh sách lịch trình rỗng
            showAlert("Thông báo", "Danh sách lịch trình không có dữ liệu để lưu!", Alert.AlertType.WARNING);
        }
    }
    private void trangLichTrinh(){
        MenuController.instance.readyUI("LichTrinh");
    }
    private void xacNhanLuu(){
        if(!danhSachLichTrinh.isEmpty()){
            Optional<ButtonType> buttonType = AlertHelper.showConfirm("Bạn có muốn lưu không?");
            if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                trangLichTrinh();
            } else if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                // Lưu xong chuyển lại trang trước
                luuLai();
                trangLichTrinh();
            }
        }else {
            trangLichTrinh();
        }
    }
    @FXML
    void controller(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnThemMoi) {
            themMoi();
        }
        if(source == btnLuuLai){
            luuLai();
        }
        if(source == btnTroLai){
            xacNhanLuu();
            System.out.println("CLicked");
        }
    }
    @FXML
    void keyPressed(KeyEvent event) throws IOException {
        Object source = event.getSource();
        if (source == anchorPane) {
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                luuLai();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                xacNhanLuu();
            } else if (event.getCode() == KeyCode.ENTER) {
                themMoi();
            } else if (event.getCode() == KeyCode.X) {
//                Sửa tên hàm lại cho dễ hiểu hơn
//                huyBoDong();
            }
        }
    }

    @FXML
    void mouseClickedEvent(MouseEvent event) {

    }

    public static List<String> keepStartAndEndWithCommonMiddle(List<String> list1, List<String> list2) {
        list1 = list1.stream().distinct().collect(Collectors.toList());
        // Nếu list1 có ít hơn 2 phần tử, trả về ngay list1
        if (list1.size() < 2) {
            return list1;
        }

        // Lấy phần tử đầu và cuối của list1
        String first = list1.get(0);
        String last = list1.get(list1.size() - 1);

        // Lọc các phần tử giữa mà có trong list2
        List<String> middle = new ArrayList<>();
        for (int i = 1; i < list1.size() - 1; i++) {
            if (list2.contains(list1.get(i))) {
                middle.add(list1.get(i));
            }
        }

        // Ghép phần tử đầu, các phần tử chung và phần tử cuối lại
        List<String> result = new ArrayList<>();
        result.add(first);
        result.addAll(middle);
        result.add(last);

        return result;
    }
}
