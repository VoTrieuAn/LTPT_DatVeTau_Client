package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import common.LoaiDon;
import common.LoaiVe;
import config.TrainTicketApplication;
import dao.LichTrinhDAO;
import dao.ToaTauDAO;
import dao.VeDAO;
import dao.impl.DonDoiTraDAOImpl;
import dao.impl.LichTrinhDAOImpl;
import dao.impl.VeDAOImpl;
import entity.*;
import jakarta.persistence.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.json.JSONException;
import org.json.JSONObject;
import util.ExportExcelUtil;
import util.HoadonCodeGeneratorUtil;
import util.VeCodeGeneratorUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoiTraController {
    public TextField phanTramTra_TraVe;
    public TextField tienHoanTra_TraVe;
    public Button traVeBtn;
    public ScrollPane danhSachToa;
    public Button cancel;
    public Label tienPhi;
    public Label TongTien;
    public TextField giaVe_DoiVe;
    public TextField tienBu;
    public TextField tienHoanTra;
    public TextField tienPhiDoi;
    public Button tinhTien;
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
    private final VeDAOImpl veDAO;
    private final DonDoiTraDAOImpl donDoiTraDAO;

    public Tab tab1;
    public Label cccd;
    public Tab tab2;
    public VBox vboxInfo;
    public Label cccd1;
    @FXML
    public ScrollPane scrollPane;
    public AnchorPane anchorPaneThanhToanRight;
    public AnchorPane anchorPaneDoiVeRight;
    public VBox vboxDoiVeLeft;
    public VBox vboxScrollPane1;
    public HBox hbox;
    public Button doiVe_Button;
    public ImageView qr_Icon;
    public Button quayLai_Button;
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
    public Button qr_Button1;
    public TabPane tabPane;
    public ImageView qr_Icon1;
    public Button clear;
    @FXML
    private GridPane gridPane;
    public VBox vboxScrollPane;
    public TextField textField_CCCD_DoiVe;
    public TextField textField_CCCD_TraVe;
    public VBox vboxTraVeLeft;
    public AnchorPane panelInfoTicket;
    @FXML
    private Button qr_Button;
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
    private ImageView cameraView;
    private HttpServer server;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isServerStarted = false;
    private AudioClip successSound;
    private MediaPlayer successSoundPlayer;
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
    private TableColumn<Ve, String> columnTrangThai;
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
    @PersistenceContext
    private EntityManager entityManager;
    private List<Ve> dsVe;
    private final ObservableList<Ve> listVeTab1 = FXCollections.observableArrayList();
    private final ObservableList<Ve> listVeTab2 = FXCollections.observableArrayList();
    private final LichTrinhDAO lichTrinhDAO =  new LichTrinhDAOImpl();
    private final EntityManagerFactory entityManagerFactory;
    private void setHgrow(ToggleButton button) {
        HBox.setHgrow(button, Priority.ALWAYS);
    }
    private void handleToggle(ToggleButton toggleButton, ImageView imageView) {
        if (toggleButton.isSelected()) {
            imageView.setImage(coachChoosingImage);
        } else {
            imageView.setImage(coachEmptyImage);
        }
        if (toggleButton.isSelected()) {
            for (ToggleButton button : new ToggleButton[]{toggleCoach1, toggleCoach2, toggleCoach3,
                    toggleCoach4, toggleCoach5, toggleCoach6,
                    toggleCoach7, toggleCoach8, toggleCoach9,
                    toggleCoach10}) {
                if (button != toggleButton) {
                    button.setSelected(false);
                }
            }
        }
    }
    private void startServer() throws IOException {
        if (server == null) {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/receive-qr", new QRHandler());
            server.setExecutor(executorService);
            server.start();
            isServerStarted = true;
            System.out.println("Server started on port 8080");
        }
    }

    @FXML
    public void handleQrButtonAction() {
        if (!isServerStarted) {
            try {
                startServer();
            } catch (IOException e) {
                System.err.println("Lỗi khi khởi động server: " + e.getMessage());
            }
        }
    }

    private class QRHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String qrCode = new String(exchange.getRequestBody().readAllBytes());
                System.out.println("Mã QR nhận được: " + qrCode);
                try {
                    JSONObject jsonObject = new JSONObject(qrCode);
                    if (jsonObject.has("qrcode")) {
                        String qrValue = jsonObject.getString("qrcode");
                        Ve ve = veDAO.timKiemId(qrValue);
                        successSoundPlayer.setVolume(1.0);
                        successSoundPlayer.play();
                        Platform.runLater(() -> {
                            successSoundPlayer.stop();
                            successSoundPlayer.play();
                            if (ve != null) {
                                if ("tab1".equals(currentTab)) {
                                    listVeTab1.clear();
                                    listVeTab1.add(ve);
                                    tableViewVe.setItems(FXCollections.observableArrayList(listVeTab1));
                                } else if ("tab2".equals(currentTab)) {
                                    listVeTab2.clear();
                                    listVeTab2.add(ve);
                                    tableViewTraVe.setItems(FXCollections.observableArrayList(listVeTab2));
                                }
                            } else {
                                System.out.println("Không tìm thấy vé với mã QR: " + qrValue);
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Không tìm thấy vé");
                                alert.setHeaderText(null);
                                alert.setContentText("Không tìm thấy vé với mã QR: " + qrValue);
                                alert.showAndWait();
                            }
                        });
                    } else {
                        System.out.println("Khóa 'qrcode' không tồn tại trong JSON.");
                    }
                } catch (JSONException e) {
                    System.err.println("Lỗi phân tích cú pháp JSON: " + e.getMessage());
                }
                String response = "QR Code received";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
    private String currentTab = "tab1";
    @Override
    protected void finalize() throws Throwable {
        if (server != null) {
            server.stop(0);
        }
        executorService.shutdown();
        super.finalize();
    }
    public DoiTraController(DonDoiTraDAOImpl donDoiTraDAO) {
        this.donDoiTraDAO = donDoiTraDAO;
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
        veDAO = new VeDAOImpl();
    }
    public DoiTraController() {
        this.donDoiTraDAO = new DonDoiTraDAOImpl();
        entityManagerFactory = Persistence.createEntityManagerFactory("default");
        veDAO = new VeDAOImpl();
    }
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ObservableList<DonDoiTra> danhSachDon = FXCollections.observableArrayList();
    @FXML
    public void initialize() {
        refreshUI();
        coachEmptyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_empty.png")));
        coachFullImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_full.png")));
        coachChoosingImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/Coach_choosing.png")));
        String soundPath = Objects.requireNonNull(getClass().getResource("/view/media/notify-short.mp3")).toString();
        Media successSound = new Media(soundPath);
        successSoundPlayer = new MediaPlayer(successSound);
        try {
            startServer();
        } catch (IOException ignored) {}
        AnchorPane.setLeftAnchor(panelInfoTicket, vboxTraVeLeft.getWidth());
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
        ngayLapDon2_DatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isBefore(ngayLapDon1_DatePicker.getValue())) {
                // Đảm bảo người dùng không chọn ngày nhỏ hơn ngày ở ngayLapDon1_DatePicker
                ngayLapDon2_DatePicker.setValue(ngayLapDon1_DatePicker.getValue());
            }
        });
        timDonTheoNgay.setOnAction(event -> {
            LocalDate ngayBatDau = ngayLapDon1_DatePicker.getValue();
            LocalDate ngayKetThuc = ngayLapDon2_DatePicker.getValue();
            if (ngayBatDau == null || ngayKetThuc == null) {
                showAlert(Alert.AlertType.INFORMATION, "Lỗi", "Chưa chọn ngày!");
                return;
            }
            if (ngayKetThuc.isBefore(ngayBatDau)) {
                showAlert(Alert.AlertType.INFORMATION, "Lỗi", "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
                return;
            }
            List<DonDoiTra> danhSachDon = donDoiTraDAO.timDonDoiTraTrongKhoangNgay(ngayBatDau, ngayKetThuc);
            if (danhSachDon.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Kết quả", "Không tìm thấy đơn đổi trả vé trong khoảng ngày này!");
            } else {
                ObservableList<DonDoiTra> donDoiTraObservableList = FXCollections.observableArrayList(danhSachDon);
                tableXemLichSu.setItems(donDoiTraObservableList);
            }
        });
        timKiemTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String maDDT = timKiemTextField.getText();
                DonDoiTra donDoiTra = donDoiTraDAO.timKiemId(maDDT);

                if (donDoiTra != null) {
                    ObservableList<DonDoiTra> observableList = FXCollections.observableArrayList(donDoiTra);
                    tableXemLichSu.setItems(observableList);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Không tìm thấy");
                    alert.setHeaderText(null);
                    alert.setContentText("Không tìm thấy đơn với mã: " + maDDT);
                    alert.showAndWait();
                }
            }
        });
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
        textField_CCCD_DoiVe.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String cccd = textField_CCCD_DoiVe.getText().trim();
                loadVe(cccd);
            }
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
        textField_CCCD_TraVe.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String cccd = textField_CCCD_TraVe.getText().trim();
                loadVeTraVe(cccd);
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
        tableViewTraVe.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateFieldsTra(newValue);
                Double tien = tinhTienHoanTra(newValue);
            }
        });
        traVeBtn.setOnAction(event -> {
            Ve ve = tableViewTraVe.getSelectionModel().getSelectedItem();
            Double tienPhi = 0.0;
            Double tienHoanTra = Double.parseDouble(tienHoanTra_TraVe.getText().trim());
            Double tienBu= 0.0;
            taoDonDoiTra(ve,tienPhi,tienHoanTra,tienBu);
            String maVe = maVe_TraVe.getText().trim();
            if (maVe.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Chưa chọn vé");
                return;
            }
            //Có Vé
            boolean success = traVe(ve);
            if (success) {
                // Vé đang 1
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Trả vé thành công!");
                refreshUI();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy vé hoặc vé không thể trả!");
            }
        });
        doiVe_Button.setOnAction(event -> onDoiVeButtonClicked());
        tab1.setOnSelectionChanged(event -> refreshUI());
        tab2.setOnSelectionChanged(event -> refreshUI());
        tab3.setOnSelectionChanged(event -> refreshUI());
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> currentTab = newTab.getId());
        clear.setOnAction(event -> {
            refreshUI();
        });
        btnXuatExcel.setOnAction(event -> {
            try {
                xuatExcel();
            } catch (IOException e) {
                throw new RuntimeException(e);
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
    private void loadToaTaus(String maTau, LichTrinh lichTrinh) {
        toggleGroup = new ToggleGroup();
        List<ToaTau> toaTaus = TrainTicketApplication.getInstance()
                .getDatabaseContext()
                .newEntityDAO(ToaTauDAO.class)
                .timToaTauTheoMaTau(maTau);
        if (toaTaus.isEmpty()) {
            return;
        }
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
                refreshGhe(selectedToa, lichTrinh);
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
                        HoaDon hoaDon = getHoaDonTuVe(ve);
                        selectedGhe = (Ghe) seatButton.getUserData();
                        giaVeCu_DoiVe.setText(String.valueOf((int)ve.getGiaVe()));
                        giaVeMoi_DoiVe.setText(String.valueOf((int)selectedGhe.getGiaGhe()));
                        double tienHT = selectedGhe.getGiaGhe() < ve.getGiaVe() ? ve.getGiaVe() - selectedGhe.getGiaGhe() : 0;
                        double tienBu = selectedGhe.getGiaGhe() > ve.getGiaVe() ? selectedGhe.getGiaGhe() - ve.getGiaVe() : 0;
                        double tienPhi = 20000;
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
    public void refreshGhe(ToaTau toaTau, LichTrinh lichTrinh) {
        List<Ghe> gheDaDat = veDAO.getGheDaDatTrongToa(toaTau, lichTrinh);
        List<Ghe> danhSachGhe = getDanhSachGhe(toaTau);
        loadGhevaTinhTien(toaTau, lichTrinh, danhSachGhe, gheDaDat);
    }
    private List<Ghe> getDanhSachGhe(ToaTau toaTau) {
        return toaTau.getGheList();
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
    public void loadVe(String cccd) {
        System.out.println("Danh sách vé: " + dsVe);
        List<Ve> dsVe = TrainTicketApplication.getInstance()
                .getDatabaseContext()
                .newEntityDAO(VeDAO.class)
                .timVeTheoCCCD(cccd);
        if(dsVe == null){
            showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Không tìm thấy vé!");
        }
        listVe.clear();
        listVe.addAll(dsVe);
        System.out.println("Danh sách vé: " + dsVe);
        tableViewVe.setItems(listVe);
    }
    public void loadChuyenTauCungGa(String gaDi, String gaDen) {
        List<LichTrinh> chuyenTauList = lichTrinhDAO.getChuyenTauCungGaLonHonNgayHienTai(gaDi,gaDen);
        ObservableList<LichTrinh> chuyensTauObservableList = FXCollections.observableArrayList(chuyenTauList);
        tableViewLoadTau.setItems(chuyensTauObservableList);
    }
    public Ve doiVe(Ve veCu, Ghe gheMoi, LichTrinh lichTrinhMoi, HoaDon hoaDonMoi, ToaTau toaTauMoi) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Ve veMoi = new Ve();
        try {
            entityManager.getTransaction().begin();
            veCu.setTrangThaiVe(false);
            entityManager.merge(veCu);
            veMoi.setMaVe(taoMaVe(toaTauMoi, lichTrinhMoi, gheMoi));
            veMoi.setGiaVe(gheMoi.getGiaGhe());
            System.out.println(veMoi.getGiaVe());
            veMoi.setThueSuatGtgt(veCu.getThueSuatGtgt());
            veMoi.setNgayMua(veCu.getNgayMua());
            veMoi.setLoaiVe(veCu.getLoaiVe());
            veMoi.setHoTen(veCu.getHoTen());
            veMoi.setCccd(veCu.getCccd());
            veMoi.setNgaySinhTreEm(veCu.getNgaySinhTreEm());
            veMoi.setLoaiKh(veCu.getLoaiKh());
            veMoi.setTrangThaiVe(true);
            veMoi.setNgaySuaDoi(new Timestamp(System.currentTimeMillis()));
            veMoi.setGheByMaGhe(gheMoi);
            veMoi.setLichtrinhByMaLt(lichTrinhMoi);
            veMoi.setHoadonByMaHd(hoaDonMoi);
            entityManager.persist(veMoi);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        return veMoi;
    }
    private ToaTau selectedToa;
    private Ghe selectedGhe;
    @FXML
    private void onDoiVeButtonClicked() {
        if (tableViewVe.getSelectionModel().getSelectedItem() != null) {
            Ve ve = tableViewVe.getSelectionModel().getSelectedItem();
            LichTrinh lichTrinh = (LichTrinh) tableViewLoadTau.getSelectionModel().getSelectedItem();
            HoaDon hoaDon = getHoaDonTuVe(ve);
            System.out.println("Ok");
            if (lichTrinh != null && selectedGhe != null && selectedToa != null) {
                Ve veMoi = doiVe(ve, selectedGhe, lichTrinh, hoaDon, selectedToa);
                luuDoiVe(veMoi);
                double tienHT = veMoi.getGiaVe() < ve.getGiaVe() ? ve.getGiaVe() - veMoi.getGiaVe() : 0;
                double tienBu = veMoi.getGiaVe() > ve.getGiaVe() ? veMoi.getGiaVe() - ve.getGiaVe() : 0;
                double tienPhi = 20000;
                DonDoiTra donDoiTra = taoDonDoiTra(veMoi, tienPhi,tienHT,tienBu);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi vé thành công!");
                refreshUI();
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Chưa chọn toa, ghế hoặc chuyển tàu!!");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Chưa chọn vé!");
        }
    }
    private void luuDoiVe(Ve veMoi) {
        try {
            EntityManager em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            em.persist(veMoi);
            em.getTransaction().commit();
            System.out.println("Vé mới đã được lưu.");
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu vé mới: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public HoaDon getHoaDonTuVe(Ve ve) {
        return ve.getHoadonByMaHd();
    }
    // Refund
    public void loadVeTraVe(String cccd) {
        List<Ve> dsVe = TrainTicketApplication.getInstance()
                .getDatabaseContext()
                .newEntityDAO(VeDAO.class)
                .timVeTheoCCCD(cccd);
        System.out.println(dsVe);
        if(dsVe == null){
            showAlert(Alert.AlertType.INFORMATION, "Thất bại", "Không tìm thấy vé!");
        }
        listVe.clear();
        listVe.addAll(dsVe);
        tableViewTraVe.setItems(listVe);
    }
    public boolean traVe(Ve ve) {
        if (ve == null) {
            return false;
        }
        ve.setTrangThaiVe(false);
        veDAO.capNhatVe(ve);
        return true;
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
    public Double tinhTienHoanTra(Ve ve) {
        LocalDateTime now = LocalDateTime.now();
        java.sql.Date ngayKhoiHanh = ve.getLichtrinhByMaLt().getNgayKhoiHanh();
        Time gioDi = ve.getLichtrinhByMaLt().getGioDi();
        LocalDateTime ngayGioKhoiHanh = convertToLocalDateTime(ngayKhoiHanh, gioDi);
        long hoursToDeparture = ChronoUnit.HOURS.between(now, ngayGioKhoiHanh);
        Double giaVe = ve.getGiaVe();
        Double tienHoanTra = 0.0;
        Double phanTram = 0.0;
        if (hoursToDeparture >= 48) {
            // Trả vé trước từ 48 giờ trở lên
            // 10% giá vé
            phanTram = 0.1;
            tienHoanTra = giaVe * phanTram;
        } else if (hoursToDeparture >= 4) {
            // Trả vé trước từ 4 giờ đến dưới 48 giờ
            // 20% giá vé
            phanTram = 0.2;
            tienHoanTra = giaVe * phanTram;
        }
        phanTramTra_TraVe.setText(String.valueOf((int) (phanTram * 100)));
        tienHoanTra_TraVe.setText(String.valueOf(tienHoanTra.intValue()));
        return tienHoanTra;
    }
    public LocalDateTime convertToLocalDateTime(java.sql.Date ngayKhoiHanh, Time gioDi) {
        LocalDateTime dateTime = ngayKhoiHanh.toLocalDate().atStartOfDay();
        LocalDateTime finalDateTime = dateTime.plusHours(gioDi.getHours())
                .plusMinutes(gioDi.getMinutes());
        return finalDateTime;
    }
    public DonDoiTra taoDonDoiTra(Ve ve, Double tienPhi,Double tienHoanTra,Double tienBu ) {
        DonDoiTra donDoiTra = new DonDoiTra();
        donDoiTra.setMaDonDoiTra(taoMaDonDoiTra());
        donDoiTra.setTienPhi(tienPhi);
        donDoiTra.setTienHoanTra(tienHoanTra);
        donDoiTra.setTienBu(tienBu);
        LoaiDon loaiDon = (tienPhi == 0.0) ? LoaiDon.Don_Tra : LoaiDon.Don_Doi;
        donDoiTra.setLoaiDon(loaiDon);
        donDoiTra.setNgayLap(Timestamp.valueOf(LocalDateTime.now()));
        donDoiTra.setVeByMaVe(ve);
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default"); // Sử dụng tên Persistence Unit của bạn
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            DonDoiTra donDoiTraMerged = entityManager.merge(donDoiTra);
            entityManager.getTransaction().commit();
            return donDoiTraMerged;
        } catch (RuntimeException e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }
    public String taoMaDonDoiTra() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayStr = today.format(formatter);
            String jpql = "SELECT COUNT(d) FROM DonDoiTra d WHERE CAST(d.ngayLap AS LocalDate) = :today";
            TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
            query.setParameter("today", today);
            Long count = query.getSingleResult();
            int soTuTang = count.intValue() + 1;
            String maDonDoiTra = String.format("DDT-%s%04d", today.format(DateTimeFormatter.ofPattern("ddMMyyyy")), soTuTang);
            return maDonDoiTra;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            entityManager.close();
            entityManagerFactory.close();
        }
    }
    public String taoMaVe(ToaTau toaTau, LichTrinh lichTrinh, Ghe ghe) {
        String maTau = toaTau.getTauByMaTau().getMaTau();
        String maToa = toaTau.getMaTt();
        String maGhe = ghe.getMaGhe();
        return VeCodeGeneratorUtil.generateMaVe(maTau, maToa, maGhe);
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