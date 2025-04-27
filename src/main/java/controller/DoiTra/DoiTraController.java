package controller.DoiTra;

import common.LoaiVe;
import entity.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import rmi.RMIServiceLocator;
import util.ExportExcelUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class DoiTraController {
    public TextField phanTramTra_TraVe;
    public TextField tienHoanTra_TraVe;
    public Button traVeBtn;
    public ScrollPane danhSachToa;
    public Button cancel;
    public Label TongTien;
    public TextField tienHoanTra;
    public TextField tienPhiDoi;
    public TextField giaVeCu_DoiVe;
    public Label TongTien1;
    public TextField giaVeMoi_DoiVe;
    public TextField tienBuThem;
    public Button btnXuatExcel;
    public TextField maNvTextField;
    public TextField timKiemTextField;
    public DatePicker ngayLapDon1_DatePicker;
    public DatePicker ngayLapDon2_DatePicker;
    public HBox hBoxPage;

    // Table xem lịch sử
    public TableView<DonDoiTra> tableXemLichSu;
    public TableColumn<DonDoiTra, String> maDon;
    public TableColumn<DonDoiTra, String> loaiDon;
    public TableColumn<DonDoiTra, Date> ngayLapDon;
    public TableColumn<DonDoiTra, String> maVe;
    public TableColumn<DonDoiTra, Double> tienBuDon;
    public TableColumn<DonDoiTra, Double> tienPhiDon;
    public TableColumn<DonDoiTra, Double> tienHoanTraDon;
    //--------------------

    public Tab tab1;
    public Label cccd;
    public Tab tab2;
    public VBox vboxInfo;
    public Label cccd1;
    @FXML
    public ScrollPane scrollPane;
    public AnchorPane anchorPaneDoiVeRight;
    public VBox vboxDoiVeLeft;
    public VBox vboxScrollPane1;
    public HBox hbox;
    public Button doiVe_Button;
    @FXML
    public HBox hboxToaTaus;
    public TableView tableViewLoadTau;
    public TableView<Ve> tableViewVe;
    public TableView<Ve> tableViewTraVe;
    public TextField maVe_TraVe;
    public TextField tenNguoiMua_TraVe;
    public TextField tenTau_TraVe;
    public TextField gaDi_TraVe;
    public TextField gaDen_TraVe;
    public TextField toaTau_TraVe;
    public TextField soGhe_TraVe;
    public TextField giaVe_TraVe;
    public Button timDonTheoNgay;
    public Tab tab3;
    public TabPane tabPane;
    public Button clear;
    @FXML
    public ImageView urlImage;
    public VBox hbox_CCCD;
    public HBox hbox_GiaVe;
    @FXML
    private GridPane gridPane;
    public VBox vboxScrollPane;
    public TextField textField_CCCD_DoiVe;
    public TextField textField_CCCD_TraVe;
    public VBox vboxTraVeLeft;
    public AnchorPane panelInfoTicket;
    private Image coachEmptyImage;
    private Image coachFullImage;
    private Image coachChoosingImage;
    @FXML
    private ImageView coachImage1;
    @FXML
    private ImageView coachImage2;
    @FXML
    private ImageView coachImage3;
    @FXML
    private ImageView coachImage4;
    @FXML
    private ImageView coachImage5;
    @FXML
    private ImageView coachImage6;
    @FXML
    private ImageView coachImage7;
    @FXML
    private ImageView coachImage8;
    @FXML
    private ImageView coachImage9;
    @FXML
    private ImageView coachImage10;
    @FXML
    private ToggleButton toggleCoach1;
    @FXML
    private ToggleButton toggleCoach2;
    @FXML
    private ToggleButton toggleCoach3;
    @FXML
    private ToggleButton toggleCoach4;
    @FXML
    private ToggleButton toggleCoach5;
    @FXML
    private ToggleButton toggleCoach6;
    @FXML
    private ToggleButton toggleCoach7;
    @FXML
    private ToggleButton toggleCoach8;
    @FXML
    private ToggleButton toggleCoach9;
    @FXML
    private ToggleButton toggleCoach10;
    private ToggleGroup toggleGroup;
    private ToggleGroup toggleGroupGhe;

    private final Image seatEmptyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/seat_empty.png")));
    private final Image seatChoosingImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/seat_choosing.png")));
    private final Image seatFullImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/seat_full.png")));
    @FXML
    private TableView<Ve> tableView1;

    @FXML
    private TableColumn<Ve, String> columnMaVe;

    @FXML
    private TableColumn<Ve, String> columnTenNguoiMua;

    @FXML
    private TableColumn<Ve, String> columnNgayMua;

    @FXML
    private TableColumn<Ve, String> columnLoaiVe;

    @FXML
    private TableColumn<LichTrinh, String> columnTau;
    @FXML
    private TableColumn<LichTrinh, String> columnGioDi;
    @FXML
    private TableColumn<LichTrinh, String> columnGioDen;
    @FXML
    private TableColumn<LichTrinh, String> columnNgayDi;
    @FXML
    private TableColumn<LichTrinh, Date> columnNgayDen;
    @FXML
    private TableColumn<Ve, String> colTau;
    @FXML
    private TableColumn<Ve, String> colGaDi;
    @FXML
    private TableColumn<Ve, String> colGaDen;
    @FXML
    private TableColumn<Ve, String> colNgayMua;
    private List<Ve> dsVe;
    private void setHgrow(ToggleButton button) {
        HBox.setHgrow(button, Priority.ALWAYS);
    }

    private String currentTab = "tab1";


    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ObservableList<DonDoiTra> danhSachDon = FXCollections.observableArrayList();
    public void update30_4(){
        tab1.setStyle("-fx-background-color: #fa8484;");
        tab2.setStyle("-fx-background-color: #fa8484;");
        tab3.setStyle("-fx-background-color: #fa8484;");
        hbox_CCCD.setStyle("-fx-background-color: #fa8484;");
        hbox_GiaVe.setStyle("-fx-background-color: #fa8484;");
        tableViewLoadTau.setStyle("-fx-background-color: #fa8484;");
        tableViewVe.setStyle("-fx-background-color: #fa8484;");
        tableXemLichSu.setStyle("-fx-background-color: #fa8484;");
        hboxToaTaus.setStyle("-fx-background-color: #fa8484;");
        scrollPane.setStyle("-fx-background-color: #fa8484;");
        columnMaVe.setStyle("-fx-background-color: #fafa84;");
        columnTenNguoiMua.setStyle("-fx-background-color: #fafa84;");
        columnNgayMua.setStyle("-fx-background-color: #fafa84;");
        columnLoaiVe.setStyle("-fx-background-color: #fafa84;");
        textField_CCCD_DoiVe.setStyle("-fx-background-color: #fafa84;");
        giaVeCu_DoiVe.setStyle("-fx-background-color: #fafa84;");
        giaVeMoi_DoiVe.setStyle("-fx-background-color: #fafa84;");
        tienBuThem.setStyle("-fx-background-color: #fafa84;");
        tienPhiDoi.setStyle("-fx-background-color: #fafa84;");
        tienHoanTra.setStyle("-fx-background-color: #fafa84;");
        columnTau.setStyle("-fx-background-color: #fafa84;");
        columnGioDi.setStyle("-fx-background-color: #fafa84;");
        columnGioDen.setStyle("-fx-background-color: #fafa84;");
        columnNgayDi.setStyle("-fx-background-color: #fafa84;");
        columnNgayDen.setStyle("-fx-background-color: #fafa84;");

    }
    @FXML
    public void initialize() {
        refreshUI();
        update30_4();
        String imageUrl = "https://res.cloudinary.com/dbv9csgia/image/upload/v1745734131/download_c2yrd0.png";
        Image image = new Image(imageUrl);
        urlImage.setImage(image);
        coachEmptyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_empty.png")));
        coachFullImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_full.png")));
        coachChoosingImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_choosing.png")));
        AnchorPane.setLeftAnchor(panelInfoTicket, vboxTraVeLeft.getWidth());
        // set cột table
        maDon.setCellValueFactory(new PropertyValueFactory<>("maDonDoiTra"));
        loaiDon.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLoaiDon().getName())
        );
        ngayLapDon.setCellValueFactory(new PropertyValueFactory<>("ngayLap"));
        maVe.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getVeByMaVe().getMaVe())
        );
        tienBuDon.setCellValueFactory(new PropertyValueFactory<>("tienBu"));
        tienPhiDon.setCellValueFactory(new PropertyValueFactory<>("tienPhi"));
        tienHoanTraDon.setCellValueFactory(new PropertyValueFactory<>("tienHoanTra"));

        // Gán dữ liệu cho bảng
        configureDatePicker(ngayLapDon1_DatePicker);
        configureDatePicker(ngayLapDon2_DatePicker);
        ngayLapDon2_DatePicker.setDisable(true);

        vboxTraVeLeft.widthProperty().addListener((observable, oldValue, newValue) -> AnchorPane.setLeftAnchor(panelInfoTicket, newValue.doubleValue()));

        Label customPlaceholder = new Label("Không Tìm Thấy");
        tableViewVe.setPlaceholder(customPlaceholder);
        TableColumn<Ve, String> gaDiColumn = new TableColumn<>("Ga Đi");
        TableColumn<Ve, String> gaDenColumn = new TableColumn<>("Ga Đến");
        TableColumn<Ve, Timestamp> ngayMuaColumn = new TableColumn<>("Ngày Mua");
        TableColumn<Ve, Double> giaVeColumn = new TableColumn<>("Giá Vé");
        tableViewVe.getColumns().addAll(gaDiColumn, gaDenColumn, ngayMuaColumn, giaVeColumn);
        columnMaVe.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMaVe()));
        columnTenNguoiMua.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHoTen()));
        columnNgayMua.setCellValueFactory(cellData -> {
            Timestamp timestamp = cellData.getValue().getNgayMua();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp);
            return new SimpleStringProperty(formattedDate);
        });
        columnLoaiVe.setCellValueFactory(cellData -> {
            LoaiVe loaiVe = cellData.getValue().getLoaiVe();
            return new SimpleStringProperty(loaiVe != null ? loaiVe.getName() : "");
        });
        columnTau.setCellValueFactory(cellData -> {
            Tau tau = cellData.getValue().getTauByMaTau();
            return new SimpleStringProperty(tau != null ? tau.getTenTau() : "");
        });
        columnGioDi.setCellValueFactory(new PropertyValueFactory<>("gioDi"));
        columnGioDen.setCellValueFactory(new PropertyValueFactory<>("gioDen"));
        columnNgayDi.setCellValueFactory(new PropertyValueFactory<>("ngayKhoiHanh"));
        columnNgayDen.setCellValueFactory(new PropertyValueFactory<>("ngayDen"));

        tableViewLoadTau.setOnMouseClicked(event -> {
            LichTrinh selectedLichTrinh = (LichTrinh) tableViewLoadTau.getSelectionModel().getSelectedItem();
            if (selectedLichTrinh != null && selectedLichTrinh.getTauByMaTau() != null) {
                gridPane.getChildren().clear();
                String maTau = selectedLichTrinh.getTauByMaTau().getMaTau();
                loadToaTaus(maTau,selectedLichTrinh);
            }
        });
        colTau.setCellValueFactory(cellData -> {
            Ve ve = cellData.getValue();
            if (ve.getLichtrinhByMaLt() != null
                    && ve.getLichtrinhByMaLt().getTauByMaTau() != null) {
                return new SimpleStringProperty(ve.getLichtrinhByMaLt().getTauByMaTau().getTenTau());
            }
            return new SimpleStringProperty("N/A");
        });
        colGaDi.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLichtrinhByMaLt().getGaKhoiHanh())
        );
        colGaDen.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLichtrinhByMaLt().getGaKetThuc())
        );
        colNgayMua.setCellValueFactory(new PropertyValueFactory<>("ngayMua"));


        //Done =========================================================
        tableViewTraVe.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateFieldsTra(newValue);
                Map<String, Double> result = null;
                try {

                    result = RMIServiceLocator.getVeService().tinhTienHoanTra(newValue);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                if (result != null) {
                    double tienHoanTra = result.get("tienHoanTra");
                    double phanTram = result.get("phanTram");

                    tienHoanTra_TraVe.setText(String.valueOf((int) tienHoanTra));
                    phanTramTra_TraVe.setText(String.valueOf((int) (phanTram * 100)));
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Không thể tính tiền hoàn trả!");
                }
            }
        });
        tab1.setOnSelectionChanged(event -> refreshUI());
        tab2.setOnSelectionChanged(event -> refreshUI());
        tab3.setOnSelectionChanged(event -> refreshUI());

        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> currentTab = newTab.getId());

        LocalDate today = LocalDate.now();
        ngayLapDon1_DatePicker.setValue(today);
        ngayLapDon2_DatePicker.setValue(today);
        ngayLapDon2_DatePicker.setDisable(false);
        ngayLapDon2_DatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });
    }

    private void configureDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return date.format(dateFormatter);
                }
                return "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                }
                return null;
            }
        });
    }
    @FXML
    private void setNgayBatDauEQNgayKetThuc() {
        ngayLapDon1_DatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ngayLapDon2_DatePicker.setDisable(false);
                ngayLapDon2_DatePicker.setValue(newValue);
                ngayLapDon2_DatePicker.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(empty || date.isBefore(newValue));
                    }
                });
            }
        });
    }
    @FXML
    private void setNgayKetThucGTENgayBatDau() {
        ngayLapDon2_DatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            LocalDate ngayBatDau = ngayLapDon1_DatePicker.getValue();
            if (newValue != null && ngayBatDau != null && newValue.isBefore(ngayBatDau)) {
                ngayLapDon2_DatePicker.setValue(ngayBatDau);
            }
        });
    }
    @FXML
    private void exportExcel() {
        btnXuatExcel.setOnAction(event -> {
            try {
                xuatExcel();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xuất Excel thất bại!\n" + e.getMessage());
            }
        });
    }
    @FXML
    private void ClearForm() {
        clear.setOnAction(event -> refreshUI());
    }

    //Done ===========================
    @FXML
    private void traVe() {
        traVeBtn.setOnAction(event -> {
            Ve ve = tableViewTraVe.getSelectionModel().getSelectedItem();
            String maVe = maVe_TraVe.getText().trim();

            if (ve == null || maVe.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Chưa chọn vé");
                return;
            }

            try {
                Double tienHoanTra = Double.parseDouble(tienHoanTra_TraVe.getText().trim());
                Double tienPhi = 0.0;
                Double tienBu = 0.0;

                RMIServiceLocator.getDonDoiTraService().taoDonDoiTra(ve,tienPhi,tienHoanTra,tienBu);
                boolean success = RMIServiceLocator.getVeService().traVe(ve);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Trả vé thành công!");
                    refreshUI();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy vé hoặc vé không thể trả!");
                }

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền hoàn trả không hợp lệ!");
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @FXML
    private void doiVe(){
        doiVe_Button.setOnAction(event -> onDoiVeButtonClicked());
    }
    private boolean isNgayHopLe(LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        if (ngayBatDau == null || ngayKetThuc == null) {
            showAlert(Alert.AlertType.INFORMATION, "Lỗi", "Chưa chọn ngày!");
            return false;
        }
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            showAlert(Alert.AlertType.INFORMATION, "Lỗi", "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
            return false;
        }
        return true;
    }

    //Done ================================================
    @FXML
    private void timDonDoiTra(ActionEvent event){
        LocalDate ngayBatDau = ngayLapDon1_DatePicker.getValue();
        LocalDate ngayKetThuc = ngayLapDon2_DatePicker.getValue();

        if (!isNgayHopLe(ngayBatDau, ngayKetThuc)) return;
        java.sql.Date start = java.sql.Date.valueOf(ngayBatDau);
        java.sql.Date end = java.sql.Date.valueOf(ngayKetThuc);
        List<DonDoiTra> danhSachDon = null;
        try {
            danhSachDon = RMIServiceLocator.getDonDoiTraService().timDonDoiTraTrongKhoangNgay(start, end);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        if (danhSachDon.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Kết quả", "Không tìm thấy đơn đổi trả vé trong khoảng ngày này!");
        } else {
            tableXemLichSu.setItems(FXCollections.observableArrayList(danhSachDon));
        }
    }
    @FXML
    private void timDonDoiTraTheoMa(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String ma = timKiemTextField.getText().trim();

            if (ma.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng nhập mã đơn đổi trả.");
                return;
            }

            try {
                DonDoiTra don = RMIServiceLocator.getDonDoiTraService().timKiemId(ma);
                if (don == null) {
                    showAlert(Alert.AlertType.INFORMATION, "Kết quả", "Không tìm thấy đơn đổi trả với mã đã nhập.");
                } else {
                    tableXemLichSu.setItems(FXCollections.observableArrayList(don));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm đơn đổi trả.");
            }
        }
    }

    @FXML
    private void inputDoiVe(KeyEvent event){
            if (event.getCode() == KeyCode.ENTER) {
                String cccd = textField_CCCD_DoiVe.getText().trim();
                loadVe(cccd);
            }
    }
    @FXML
    private void inputTraVe(KeyEvent event){
            if (event.getCode() == KeyCode.ENTER) {
                String cccd = textField_CCCD_TraVe.getText().trim();
                loadVeTraVe(cccd);
            }
    }

    //Done ====================================================
    @FXML
    private void enterDeTimKiem(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String maVe = timKiemTextField.getText().trim();
            System.out.println(maVe);
            if (maVe.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập cccd/passport!");
                return;
            }

            DonDoiTra donDoiTra = null;
            try {
                donDoiTra = RMIServiceLocator.getDonDoiTraService().timKiemId(maVe);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

            if (donDoiTra != null) {
                ObservableList<DonDoiTra> list = FXCollections.observableArrayList(donDoiTra);
                tableXemLichSu.setItems(list);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy", "Không tìm thấy đơn với mã: " + maVe);
            }
        }
    }

    // Done =======================================================
    private void loadToaTaus(String maTau, LichTrinh lichTrinh) {
        List<ToaTau> toaTaus = null;
        try {
            toaTaus = RMIServiceLocator.getToaTauService().getToaTauByMaTau(maTau);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        if (toaTaus.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, maTau, "Không tìm đươợc");
            return;
        }

        toggleGroup = new ToggleGroup();
        hboxToaTaus.getChildren().clear();
        for (ToaTau toa : toaTaus) {
            VBox vboxToa = new VBox();
            vboxToa.setAlignment(Pos.CENTER);
            vboxToa.setSpacing(0);
            ToggleButton toggleButton = new ToggleButton();
            ImageView imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_empty.png"))));
            toggleButton.setGraphic(imageView);
            toggleButton.setUserData(toa);
            toggleButton.setToggleGroup(toggleGroup);
            toggleButton.setOnAction(e -> {
                for (Toggle toggle : toggleGroup.getToggles()) {
                    ToggleButton btn = (ToggleButton) toggle;
                    btn.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_empty.png")))));
                }
                if (toggleButton.isSelected()) {
                    toggleButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_choosing.png")))));
                }
                selectedToa = (ToaTau) toggleButton.getUserData();
                try {
                    refreshGhe(selectedToa, lichTrinh);
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            });
            vboxToa.getChildren().add(toggleButton);
            Label label = new Label("Toa " + toa.getSoToa());
            label.setAlignment(Pos.CENTER);
            vboxToa.getChildren().add(label);
            hboxToaTaus.getChildren().add(vboxToa);
        }
    }

    private void loadGhevaTinhTien(ToaTau toaTau, LichTrinh lichTrinh, List<Ghe> danhSachGhe, List<Ghe> gheDaDat) {
        gridPane.getChildren().clear();
        toggleGroupGhe = new ToggleGroup();
        if (toaTau == null || lichTrinh == null) {
            return;
        }
        int soLuongGhe = danhSachGhe.size();
        int soHangCho1Cot = 4;
        int tongSoCot = (int) Math.ceil((double) soLuongGhe / soHangCho1Cot);
        int seatIndex = 0;
        for (int col = 0; col < tongSoCot; col++) {
            for (int row = 0; row <= soHangCho1Cot; row++) {
                if (row == 2) {
                    ToggleButton aisleButton = new ToggleButton("Lối đi");
                    aisleButton.setPrefSize(80, 20);
                    aisleButton.setDisable(true);
                    gridPane.add(aisleButton, col, row);
                } else {
                    if (seatIndex >= soLuongGhe) break;
                    Ghe ghe = danhSachGhe.get(seatIndex);
                    ToggleButton seatButton = new ToggleButton();
                    setSeatGraphic(seatButton, ghe, gheDaDat);
                    if (gheDaDat.contains(ghe)) {
                        seatButton.setSelected(true);
                        seatButton.setDisable(true);
                        setSeatGraphic(seatButton, ghe, gheDaDat);
                    }
                    seatButton.setUserData(ghe);
                    seatButton.setToggleGroup(toggleGroupGhe);
                    seatButton.setOnAction(e -> {
                        for (Toggle toggle : toggleGroupGhe.getToggles()) {
                            ToggleButton btn = (ToggleButton) toggle;
                            Ghe btnGhe = (Ghe) btn.getUserData();
                            if (!btn.isSelected()) {
                                updateSeatGraphic(btn, false);
                            }
                        }
                        updateSeatGraphic(seatButton, true);
                        Ve ve = tableViewVe.getSelectionModel().getSelectedItem();
                        //======================================
                        HoaDon hoaDon = ve.getHoadonByMaHd();
                        //====================================== Note
                        selectedGhe = (Ghe) seatButton.getUserData();
                        giaVeCu_DoiVe.setText(String.valueOf((int)ve.getGiaVe()));
                        giaVeMoi_DoiVe.setText(String.valueOf((int)selectedGhe.getGiaGhe()));
                        // Done ==================================
                        double[] tien = null;
                        try {
                            tien = RMIServiceLocator.getVeService().tinhTienHoanTraVaChenhLech(selectedGhe, ve);
                        } catch (RemoteException ex) {
                            throw new RuntimeException(ex);
                        }
                        double tienHT = tien[0];
                        double tienBu = tien[1];
                        double tienPhi = tien[2];

                        tienBuThem.setText(String.valueOf((int)tienBu));
                        tienPhiDoi.setText(String.valueOf((int)tienPhi));
                        tienHoanTra.setText(String.valueOf((int)tienHT));
                    });
                    gridPane.add(seatButton, col, row);
                    seatIndex++;
                }
            }
        }
        gridPane.setHgap(5);
        gridPane.setVgap(5);
    }
    private void setSeatGraphic(ToggleButton seatButton, Ghe ghe, List<Ghe> gheDaDat) {
        ImageView seatImageView = new ImageView();
        seatImageView.setFitHeight(30);
        seatImageView.setFitWidth(30);
        seatButton.setGraphic(seatImageView);
        if (isSeatBooked(ghe, gheDaDat)) {
            seatImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/seat_full.png"))));
            seatButton.setDisable(true);
        } else {
            seatImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/seat_empty.png"))));
            seatButton.setDisable(false);
        }
    }
    private void updateSeatGraphic(ToggleButton seatButton, boolean isSelected) {
        ImageView seatImageView = (ImageView) seatButton.getGraphic();
        if (seatImageView != null) {
            seatImageView.setImage(isSelected ? seatChoosingImage : seatEmptyImage);
        }
    }
    private boolean isSeatBooked(Ghe ghe, List<Ghe> gheDaDat) {
        for (Ghe bookedSeat : gheDaDat) {
            if (bookedSeat.getMaGhe().equals(ghe.getMaGhe()) && bookedSeat.isTrangThai()) {
                return true;
            }
        }
        return false;
    }

    // NOTE======================
    public void refreshGhe(ToaTau toaTau, LichTrinh lichTrinh) throws RemoteException {
        List<Ghe> gheDaDat = RMIServiceLocator.getVeService().getGheDaDat(toaTau, lichTrinh);
        List<Ghe> danhSachGhe = toaTau.getGheList();
        loadGhevaTinhTien(toaTau, lichTrinh, danhSachGhe, gheDaDat);
    }

    @FXML
    public void onTableViewVeClickedDoiVe(MouseEvent event) {
        if (tableViewVe.getSelectionModel().getSelectedItem() != null) {
            Ve ve = tableViewVe.getSelectionModel().getSelectedItem();
            gridPane.getChildren().clear();
            hboxToaTaus.getChildren().clear();
            Ve selectedVe = tableViewVe.getSelectionModel().getSelectedItem();
            LichTrinh lichTrinh = selectedVe.getLichtrinhByMaLt();
            if (lichTrinh != null) {
                String gaDi = lichTrinh.getGaKhoiHanh();
                String gaDen = lichTrinh.getGaKetThuc();
                Date NgayDi  = lichTrinh.getNgayKhoiHanh();
                Date NgayDen  = lichTrinh.getNgayDen();
                loadChuyenTauCungGa(gaDi, gaDen);
            }
        }
    }
    @FXML
    public void onActionCancel(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy");
        alert.setHeaderText("Bạn có chắc chắn muốn hủy những thao tác trên?");
        ButtonType buttonTypeYes = new ButtonType("Có");
        ButtonType buttonTypeNo = new ButtonType("Không");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                if (tableViewVe.getSelectionModel().getSelectedItem() != null) {
                    Ve ve = tableViewVe.getSelectionModel().getSelectedItem();
                }
                refreshUI();
            }
        });
    }
    private final ObservableList<Ve> listVe = FXCollections.observableArrayList();

    //Done =============================
    public void loadVe(String cccd) {
        System.out.println("Danh sách vé: " + dsVe);
        List<Ve> dsVe = null;
        try {
            dsVe = RMIServiceLocator.getVeService().getVeById(cccd);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        if(dsVe == null){
            showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Không tìm thấy vé!");
        }
        listVe.clear();
        assert dsVe != null;
        listVe.addAll(dsVe);
        System.out.println("Danh sách vé: " + dsVe);
        tableViewVe.setItems(listVe);
    }

    //Done==================================================================
    public void loadChuyenTauCungGa(String gaDi, String gaDen) {
        List<LichTrinh> chuyenTauList = null;
        try {
            chuyenTauList = RMIServiceLocator.getLichTrinhService().getChuyenTauCungGaLonHonNgayHienTai(gaDi,gaDen);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        ObservableList<LichTrinh> chuyensTauObservableList = FXCollections.observableArrayList(chuyenTauList);
        tableViewLoadTau.setItems(chuyensTauObservableList);
    }

    //Done ===============================================================
    public Ve doiVe(Ve veCu, Ghe gheMoi, LichTrinh lichTrinhMoi, HoaDon hoaDonMoi, ToaTau toaTauMoi) {
        Ve veMoi = null;
        try {
            veMoi = RMIServiceLocator.getVeService().updateVe(veCu, toaTauMoi, lichTrinhMoi, gheMoi, hoaDonMoi);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return veMoi;
    }
    private ToaTau selectedToa;
    private Ghe selectedGhe;

    //Done =================================
    @FXML
    private void onDoiVeButtonClicked() {
        if (tableViewVe.getSelectionModel().getSelectedItem() != null) {
            Ve ve = tableViewVe.getSelectionModel().getSelectedItem();
            LichTrinh lichTrinh = (LichTrinh) tableViewLoadTau.getSelectionModel().getSelectedItem();
            HoaDon hoaDon = ve.getHoadonByMaHd();
            System.out.println("Ok");
            if (lichTrinh != null && selectedGhe != null && selectedToa != null) {
                Ve veMoi = doiVe(ve, selectedGhe, lichTrinh, hoaDon, selectedToa);
                if (veMoi != null) {
                    try {
                        RMIServiceLocator.getVeService().luuDoiVe(veMoi);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Lỗi khi lưu vé mới sau khi đổi vé.", e);
                    }
                } else {
                    System.out.println("Đổi vé thất bại, không có vé mới để lưu.");
                }
                double tienHT = veMoi.getGiaVe() < ve.getGiaVe() ? ve.getGiaVe() - veMoi.getGiaVe() : 0;
                double tienBu = veMoi.getGiaVe() > ve.getGiaVe() ? veMoi.getGiaVe() - ve.getGiaVe() : 0;
                double tienPhi = 20000;
                try {
                    DonDoiTra donDoiTra = RMIServiceLocator.getDonDoiTraService().taoDonDoiTra(ve,tienPhi,tienHT,tienBu);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi vé thành công!");
                refreshUI();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Chưa chọn toa, ghế hoặc chuyển tàu!!");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Chưa chọn vé!");
        }
    }
    //DOne ========================================
    public void loadVeTraVe(String cccd) {
        List<Ve> dsVe = null;
        try {
            dsVe = RMIServiceLocator.getVeService().getVeById(cccd);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        System.out.println(dsVe);
        if(dsVe == null){
            showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Không tìm thấy vé!");
        }
        listVe.clear();
        assert dsVe != null;
        listVe.addAll(dsVe);
        tableViewTraVe.setItems(listVe);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void refreshUI() {
        textField_CCCD_TraVe.clear();
        if (maVe_TraVe != null)
            maVe_TraVe.clear();
        tenNguoiMua_TraVe.clear();
        tenTau_TraVe.clear();
        gaDi_TraVe.clear();
        gaDen_TraVe.clear();
        toaTau_TraVe.clear();
        soGhe_TraVe.clear();
        giaVe_TraVe.clear();
        phanTramTra_TraVe.clear();
        tienHoanTra_TraVe.clear();
        tableViewTraVe.getItems().clear();

        gridPane.getChildren().clear();
        hboxToaTaus.getChildren().clear();
        tableViewVe.getItems().clear();
        tableViewLoadTau.getItems().clear();
        textField_CCCD_DoiVe.clear();
        tienPhiDoi.clear();
        tienHoanTra.clear();
        tienBuThem.clear();
        giaVeCu_DoiVe.clear();
        giaVeMoi_DoiVe.clear();

        ngayLapDon1_DatePicker.setValue(null);
        ngayLapDon2_DatePicker.setValue(null);
        timKiemTextField.clear();
        tableXemLichSu.setItems(null);

    }
    private void updateFieldsTra(Ve selectedVe) {
        maVe_TraVe.setText(selectedVe.getMaVe());
        tenNguoiMua_TraVe.setText(selectedVe.getHoTen());
        tenTau_TraVe.setText(selectedVe.getLichtrinhByMaLt().getTauByMaTau().getTenTau());
        gaDi_TraVe.setText(selectedVe.getLichtrinhByMaLt().getGaKhoiHanh());
        gaDen_TraVe.setText(selectedVe.getLichtrinhByMaLt().getGaKetThuc());
        toaTau_TraVe.setText(selectedVe.getGheByMaGhe().getToatauByMaTt().getMaTt());
        soGhe_TraVe.setText(selectedVe.getGheByMaGhe().getMaGhe());
        giaVe_TraVe.setText(String.valueOf((int) selectedVe.getGiaVe()));
    }

    public LocalDateTime convertToLocalDateTime(java.sql.Date ngayKhoiHanh, Time gioDi) {
        LocalDateTime dateTime = ngayKhoiHanh.toLocalDate().atStartOfDay();
        LocalDateTime finalDateTime = dateTime.plusHours(gioDi.getHours())
                .plusMinutes(gioDi.getMinutes());
        return finalDateTime;
    }

    private void xuatExcel() throws IOException {
        Stage stage = (Stage) tableXemLichSu.getScene().getWindow();
        if(tableXemLichSu.getItems() == null){
            showAlert(Alert.AlertType.INFORMATION,"Nhắc Nhở","Ấn tìm để lấy đơn sau đó ấn xuất excel");
        }else{
            ExportExcelUtil.exportTableViewToExcel(tableXemLichSu, stage);
            refreshUI();
        }
    }
}
// 772034599555
// NV240045
// 1262wko3