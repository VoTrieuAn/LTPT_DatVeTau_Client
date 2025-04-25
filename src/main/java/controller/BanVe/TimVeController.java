package controller.BanVe;

import common.LoaiToa;
import controller.Menu.MenuNhanVienController;
import entity.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import rmi.RMIServiceLocator;
import service.LichTrinhService;
import util.VeCodeGeneratorUtil;

import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TimVeController implements Initializable {
    @FXML private ComboBox<String> comboBoxGaDi;
    @FXML private ComboBox<String> comboBoxGaDen;
    @FXML private RadioButton motChieuRadio;
    @FXML private RadioButton khuHoiRadio;
    @FXML private DatePicker ngayDiDatePicker;
    @FXML private DatePicker ngayVeDatePicker;
    @FXML private Button btnTimKiem;
    @FXML private AnchorPane anchorPaneMain;
    @FXML private AnchorPane anchorPaneMain1;
    @FXML private TableView<LichTrinh> tableViewDi;
    @FXML private TableView<LichTrinh> tableViewVe;
    @FXML private Label loaiToaGhe;
    @FXML private GridPane toaTauGridPane;
    @FXML private GridPane gridPaneGheTrai;
    @FXML private Label luotDilb;
    @FXML private Label dsGhelb;
    @FXML private Label toaTaulb;
    @FXML private AnchorPane toaTauPane;
    @FXML private AnchorPane dsGheContaner;

    private Button selectedToaButton;
    private LichTrinhService timVeService;

    private final Map<ToaTau, Set<Ghe>> gheDaChonMapDi = new HashMap<>();
    private final Map<ToaTau, Set<Ghe>> gheDaChonMapVe = new HashMap<>();
    private final ObservableList<String> masterGaDiList = FXCollections.observableArrayList();
    private final ObservableList<String> masterGaDenList = FXCollections.observableArrayList();
    private final ObservableList<LichTrinh> dataLichTrinhDi = FXCollections.observableArrayList();
    private final ObservableList<LichTrinh> dataLichTrinhVe = FXCollections.observableArrayList();
    private ObservableList<Ve> veList = FXCollections.observableArrayList();

    private String gaDi;
    private String gaDen;
    private LocalDate ngayDiLD;
    private LocalDate ngayVeLD;
    private boolean isMotChieu;
    private LichTrinh selectedLichTrinhDi;
    private LichTrinh selectedLichTrinhVe;
    private List<Node> toaTauChildren;
    private List<Node> gheTraiChildren;
    private List<Node> tableLuotDiContainer;
    private List<Node> ttContainer;
    private List<Node> dsgContainer;

    private NhanVien nhanVien;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Kết nối tới RMI service
            timVeService = RMIServiceLocator.getLichTrinhService();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kết nối tới TimVeService: " + e.getMessage());
            return;
        }

        // Lưu trạng thái giao diện
        toaTauChildren = new ArrayList<>(toaTauGridPane.getChildren());
        gheTraiChildren = new ArrayList<>(gridPaneGheTrai.getChildren());
        tableLuotDiContainer = new ArrayList<>(anchorPaneMain.getChildren());
        ttContainer = new ArrayList<>(toaTauPane.getChildren());
        dsgContainer = new ArrayList<>(dsGheContaner.getChildren());

        VBox parentVBox = (VBox) anchorPaneMain.getParent();
        parentVBox.getChildren().remove(anchorPaneMain1);
        VBox.setVgrow(anchorPaneMain, Priority.ALWAYS);

        loadGaDiGaDen();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        ngayDiDatePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
            }
        });

        ngayVeDatePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
            }
        });

        comboBoxGaDi.setEditable(true);
        comboBoxGaDen.setEditable(true);

        setupAutoComplete(comboBoxGaDi, masterGaDiList);
        setupAutoComplete(comboBoxGaDen, masterGaDenList);

        comboBoxGaDi.getEditor().textProperty().addListener((obs, oldText, newText) -> checkGaDiVaGaDen());
        comboBoxGaDen.getEditor().textProperty().addListener((obs, oldText, newText) -> checkGaDiVaGaDen());

        chonLoaiVe();
        setupTableView(tableViewDi);
        setupTableView(tableViewVe);

        try {
            List<LichTrinh> lichTrinhDanhSach = timVeService.getLichTrinhByNgayKhoiHanhAndTrangThai(
                    Date.valueOf(LocalDate.now()), 0);
            tableViewDi.setItems(FXCollections.observableArrayList(lichTrinhDanhSach));
            if (!lichTrinhDanhSach.isEmpty()) {
                tableViewDi.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách lịch trình: " + e.getMessage());
        }

        ngayDiLD = ngayDiDatePicker.getValue();
        ngayVeLD = ngayVeDatePicker.getValue();

        tableViewDi.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                toaTaulb.setVisible(false);
                toaTaulb.setManaged(false);
                gridPaneGheTrai.getChildren().clear();
                gridPaneGheTrai.getChildren().addAll(gheTraiChildren);
                loaiToaGhe.setText("");
                luotDilb.setText("LƯỢT ĐI");
                toaTaulb.setText("TOA TÀU");
                dsGhelb.setText("Danh Sách Ghế");
                if (ngayDiLD != null) {
                    selectedLichTrinhDi = newValue;
                    tableViewVe.getSelectionModel().clearSelection();
                    hienThiToaTau(newValue.getTauByMaTau().getMaTau(), ngayDiLD, true);
                } else {
                    hienThiToaTau(newValue.getTauByMaTau().getMaTau(), LocalDate.now(), true);
                }
            }
        });

        tableViewVe.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                toaTaulb.setVisible(false);
                toaTaulb.setManaged(false);
                gridPaneGheTrai.getChildren().clear();
                gridPaneGheTrai.getChildren().addAll(gheTraiChildren);
                loaiToaGhe.setText("");
                luotDilb.setText("LƯỢT ĐI");
                toaTaulb.setText("TOA TÀU");
                dsGhelb.setText("Danh Sách Ghế");
                if (ngayVeLD != null) {
                    selectedLichTrinhVe = newValue;
                    tableViewDi.getSelectionModel().clearSelection();
                    hienThiToaTau(newValue.getTauByMaTau().getMaTau(), ngayVeLD, false);
                } else {
                    hienThiToaTau(newValue.getTauByMaTau().getMaTau(), LocalDate.now(), false);
                }
            }
        });
    }

    private void hienThiToaTau(String maTau, LocalDate lichTrinh, boolean isDi) {
        try {
            List<ToaTau> toaTauList = timVeService.timToaTauTheoMaTau(maTau);
            if (toaTauList == null || toaTauList.isEmpty()) {
                System.out.println("Không tìm thấy toa tàu nào cho mã tàu: " + maTau);
                toaTauGridPane.getChildren().clear();
                return;
            }

            toaTauGridPane.getChildren().clear();
            toaTauGridPane.getRowConstraints().clear();
            toaTauGridPane.getColumnConstraints().clear();

            int maxRows = 10;
            int maxColumns = 1;

            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100);
            toaTauGridPane.getColumnConstraints().add(columnConstraints);

            for (int i = 0; i < maxRows; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setPrefHeight(50);
                rowConstraints.setVgrow(Priority.NEVER);
                toaTauGridPane.getRowConstraints().add(rowConstraints);
            }

            boolean defaultSelected = false;

            for (int i = 0; i < toaTauList.size(); i++) {
                Button toaButton = new Button();
                ToaTau toaTau = toaTauList.get(i);

                List<Ghe> danhSachGhe = timVeService.layDanhSachGheTrongToa(toaTau.getMaTt());
                List<Ghe> gheDaMua = timVeService.layDanhSachGheDaMua(
                        toaTau.getTauByMaTau().getMaTau(),
                        Date.valueOf(lichTrinh));

                // Gỡ lỗi: Ghi lại các đối tượng Ghe
                System.out.println("Danh sách ghế đã mua cho toa " + toaTau.getMaTt() + ":");
                for (Ghe ghe : gheDaMua) {
                    System.out.println("Ghế: " + ghe.getMaGhe() + ", ToaTau: " + (ghe.getToatauByMaTt() != null ? ghe.getToatauByMaTt().getMaTt() : "null"));
                }

                long soGheDaMuaTrongToa = gheDaMua.stream()
                        .filter(ghe -> ghe.getToatauByMaTt() != null && ghe.getToatauByMaTt().getMaTt().equals(toaTau.getMaTt()))
                        .count();

                boolean tatCaGheDaMua = danhSachGhe.size() > 0 && danhSachGhe.size() == soGheDaMuaTrongToa;

                toaButton.setText("Toa " + toaTau.getSoToa());
                Image iconImage = new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream("/view/images/train_11088660.png")));
                ImageView iconView = new ImageView(iconImage);
                iconView.setFitHeight(30);
                iconView.setFitWidth(30);
                toaButton.setGraphic(iconView);

                if (tatCaGheDaMua) {
                    toaButton.setStyle("-fx-border-color: red; -fx-border-radius: 5; -fx-background-radius: 5;");
                    toaButton.setDisable(true);
                } else {
                    toaButton.setStyle(getButtonStyle(toaTau.getLoaiToa()));
                }

                Tooltip tooltip = new Tooltip(toaTau.getLoaiToa().getName());
                tooltip.setShowDelay(Duration.ZERO);
                toaButton.setTooltip(tooltip);

                toaButton.setMaxWidth(Double.MAX_VALUE);
                toaButton.setMaxHeight(Double.MAX_VALUE);

                toaButton.setOnAction(event -> {
                    System.out.println("Bạn đã chọn toa: " + toaTau.getSoToa());
                    loaiToaGhe.setText("Toa số " + toaTau.getSoToa() + ": " + toaTau.getLoaiToa().getName());

                    if (selectedToaButton != null) {
                        ToaTau previousToa = (ToaTau) selectedToaButton.getUserData();
                        selectedToaButton.setStyle(getButtonStyle(previousToa.getLoaiToa()));
                    }

                    toaButton.setStyle(getButtonStyle(toaTau.getLoaiToa())
                            + "-fx-border-color: #2bbaba; "
                            + "-fx-border-width: 2; "
                            + "-fx-border-style: solid; "
                            + "-fx-border-radius: 5;");

                    selectedToaButton = toaButton;
                    toaTaulb.setVisible(false);
                    hienThiGheTrongToa(toaTau, lichTrinh, isDi);
                });

                toaButton.setUserData(toaTau);

                int row = i;
                int column = 0;
                toaTauGridPane.add(toaButton, column, row);

                if (!defaultSelected && !toaButton.isDisabled()) {
                    selectedToaButton = toaButton;
                    toaButton.setStyle(getButtonStyle(toaTau.getLoaiToa())
                            + "-fx-border-color: #2bbaba; "
                            + "-fx-border-width: 2; "
                            + "-fx-border-style: solid; "
                            + "-fx-border-radius: 5;");

                    loaiToaGhe.setText("Toa số " + toaTau.getSoToa() + ": " + toaTau.getLoaiToa().getName());
                    hienThiGheTrongToa(toaTau, lichTrinh, isDi);
                    defaultSelected = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hiển thị toa tàu: " + e.getMessage());
        }
    }

    private String getButtonStyle(LoaiToa loaiToa) {
        switch (loaiToa) {
            case GIUONG_NAM:
                return "-fx-background-color: #0066CC; -fx-border-radius: 5; -fx-background-radius: 5;";
            case NGOI_MEM:
                return "-fx-background-color: #33CC66; -fx-border-radius: 5; -fx-background-radius: 5;";
            case NGOI_CUNG:
                return "-fx-background-color: #66CCCC; -fx-border-radius: 5; -fx-background-radius: 5;";
            default:
                return "-fx-background-color: #FFFFFF; -fx-border-radius: 5; -fx-background-radius: 5;";
        }
    }

    private void hienThiGheTrongToa(ToaTau toaTau, LocalDate lichTrinh, boolean isDi) {
        gridPaneGheTrai.getChildren().clear();
        gridPaneGheTrai.getRowConstraints().clear();
        gridPaneGheTrai.getColumnConstraints().clear();

        gridPaneGheTrai.setHgap(10);
        gridPaneGheTrai.setVgap(10);

        if (!(gridPaneGheTrai.getParent() instanceof ScrollPane)) {
            ScrollPane scrollPaneLeft = new ScrollPane(gridPaneGheTrai);
            scrollPaneLeft.setFitToWidth(true);
            scrollPaneLeft.setPrefHeight(400);
        }

        Map<ToaTau, Set<Ghe>> gheDaChonMap = isDi ? gheDaChonMapDi : gheDaChonMapVe;
        try {
            List<Ghe> danhSachGhe = timVeService.layDanhSachGheTrongToa(toaTau.getMaTt());
            int totalSeats = danhSachGhe.size();
            List<Ghe> gheDaMua = timVeService.layDanhSachGheDaMua(
                    toaTau.getTauByMaTau().getMaTau(),
                    Date.valueOf(lichTrinh));
            Set<String> gheDaMuaSet = new HashSet<>();
            for (Ghe ghe : gheDaMua) {
                gheDaMuaSet.add(ghe.getMaGhe());
            }

            gheDaChonMap.putIfAbsent(toaTau, new HashSet<>());

            for (int i = 0; i < totalSeats; i++) {
                final Ghe ghe = danhSachGhe.get(i);
                final String maGhe = ghe.getMaGhe();
                VBox content = new VBox();
                Label soGheLabel = new Label("" + ghe.getSoGhe());
                soGheLabel.setStyle("-fx-font-size: 14px;");
                Label giaGheLabel = new Label(String.format("%.0fK", ghe.getGiaGhe() / 1000));
                giaGheLabel.setStyle("-fx-font-size: 12px;");

                content.getChildren().addAll(soGheLabel, giaGheLabel);
                content.setAlignment(Pos.CENTER);

                ToggleButton gheButton = new ToggleButton();
                gheButton.setGraphic(content);
                gheButton.setPrefSize(100, 100);
                gheButton.setMaxSize(100, 100);

                // Kiểm tra trạng thái ghế
                if (gheDaMuaSet.contains(maGhe)) {
                    // Ghế đã mua: vô hiệu hóa và viền đỏ
                    gheButton.setDisable(true);
                    gheButton.setStyle("-fx-border-color: red; -fx-border-radius: 10; -fx-background-radius: 10;");
                } else if (gheDaChonMap.get(toaTau).contains(ghe)) {
                    // Ghế đã chọn: tô màu xanh, cho phép tương tác
                    gheButton.setSelected(true);
                    gheButton.setStyle(
                            "-fx-border-color: #2bbaba; -fx-border-width: 3; -fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");
                    gheButton.setDisable(false); // Có thể tương tác
                } else {
                    // Ghế chưa chọn: màu trắng, có thể tương tác
                    gheButton.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;");
                    gheButton.setDisable(false); // Có thể tương tác
                }

                // Gắn sự kiện chọn/bỏ chọn
                gheButton.setOnAction(event -> {
                    if (!gheDaMuaSet.contains(maGhe)) {
                        LichTrinh selectedLichTrinh = isDi ? selectedLichTrinhDi : selectedLichTrinhVe;
                        if (selectedLichTrinh == null) {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn lịch trình!");
                            gheButton.setSelected(false);
                            return;
                        }

                        if (gheButton.isSelected()) {
                            // Khóa ghế trên server
                            Task<Boolean> lockTask = new Task<>() {
                                @Override
                                protected Boolean call() throws Exception {
                                    return timVeService.lockSeat(maGhe, toaTau.getTauByMaTau().getMaTau(), Date.valueOf(lichTrinh));
                                }
                            };
                            lockTask.setOnSucceeded(taskEvent -> {
                                boolean lockSuccess = lockTask.getValue();
                                Platform.runLater(() -> {
                                    if (lockSuccess) {
                                        gheButton.setStyle(
                                                "-fx-border-color: #2bbaba; -fx-border-width: 3; -fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");
                                        try {
                                            // Kiểm tra trùng lặp vé
                                            boolean veTonTai = veList.stream().anyMatch(
                                                    existingVe -> existingVe.getGheByMaGhe().equals(ghe) &&
                                                            existingVe.getLichtrinhByMaLt().equals(selectedLichTrinh));
                                            if (veTonTai) {
                                                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vé đã tồn tại!");
                                                gheButton.setSelected(false);
                                                unlockSeatAsync(maGhe, gheButton);
                                                return;
                                            }

                                            // Tạo vé mới
                                            Ve ve = new Ve();
                                            ve.setMaVe(VeCodeGeneratorUtil.generateMaVe(
                                                    toaTau.getTauByMaTau().getMaTau(), toaTau.getMaTt(), ghe.getMaGhe()));
                                            ve.setGheByMaGhe(ghe);
                                            ve.setLichtrinhByMaLt(selectedLichTrinh);
                                            ve.setGiaVe(ghe.getGiaGhe());
                                            ve.setThueSuatGtgt(0.1);
                                            ve.setNgayMua(new Timestamp(System.currentTimeMillis()));
                                            ve.setTrangThaiVe(true);
                                            ve.setNgaySuaDoi(null);

                                            veList.add(ve);
                                            gheDaChonMap.get(toaTau).add(ghe);
                                            System.out.println("Danh sách vé sau khi thêm: " + veList);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm vé: " + e.getMessage());
                                            gheButton.setSelected(false);
                                            unlockSeatAsync(maGhe, gheButton);
                                        }
                                    } else {
                                        gheButton.setSelected(false);
                                        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ghế này vừa được người khác chọn!");
                                    }
                                });
                            });
                            lockTask.setOnFailed(taskEvent -> {
                                Platform.runLater(() -> {
                                    gheButton.setSelected(false);
                                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể khóa ghế: " + lockTask.getException().getMessage());
                                });
                            });
                            new Thread(lockTask).start();
                        } else {
                            // Bỏ chọn ghế
                            Task<Void> unlockTask = new Task<>() {
                                @Override
                                protected Void call() throws Exception {
                                    timVeService.unlockSeat(maGhe);
                                    return null;
                                }
                            };
                            unlockTask.setOnSucceeded(taskEvent -> {
                                Platform.runLater(() -> {
                                    gheButton.setStyle(
                                            "-fx-background-color: white; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;");
                                    try {
                                        veList.removeIf(ve -> ve.getGheByMaGhe().equals(ghe) &&
                                                ve.getLichtrinhByMaLt().equals(selectedLichTrinh));
                                        gheDaChonMap.get(toaTau).remove(ghe);
                                        System.out.println("Danh sách vé sau khi xóa: " + veList);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa vé: " + e.getMessage());
                                    }
                                });
                            });
                            unlockTask.setOnFailed(taskEvent -> {
                                Platform.runLater(() -> {
                                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy khóa ghế: " + unlockTask.getException().getMessage());
                                    try {
                                        veList.removeIf(ve -> ve.getGheByMaGhe().equals(ghe) &&
                                                ve.getLichtrinhByMaLt().equals(selectedLichTrinh));
                                        gheDaChonMap.get(toaTau).remove(ghe);
                                        System.out.println("Danh sách vé sau khi xóa: " + veList);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa vé: " + e.getMessage());
                                    }
                                });
                            });
                            new Thread(unlockTask).start();
                        }
                    }
                });

                int row = i / 4;
                int column = i % 4;
                if (column >= 2) {
                    column++;
                }
                dsGhelb.setVisible(false);
                gridPaneGheTrai.add(gheButton, column, row);
            }

            for (int i = 0; i < 5; i++) {
                ColumnConstraints columnConstraints = new ColumnConstraints();
                columnConstraints.setPercentWidth(20);
                gridPaneGheTrai.getColumnConstraints().add(columnConstraints);
            }

            for (int i = 0; i < (totalSeats / 4) + 1; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setPrefHeight(100);
                gridPaneGheTrai.getRowConstraints().add(rowConstraints);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hiển thị ghế: " + e.getMessage());
        }
    }

    // Helper method to unlock a seat asynchronously
    private void unlockSeatAsync(String maGhe, ToggleButton gheButton) {
        Task<Void> unlockTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                timVeService.unlockSeat(maGhe);
                return null;
            }
        };
        unlockTask.setOnFailed(taskEvent -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy khóa ghế: " + unlockTask.getException().getMessage());
            });
        });
        new Thread(unlockTask).start();
    }

    private void loadGaDiGaDen() {
        try {
            masterGaDiList.addAll(timVeService.getDistinctGaKhoiHanh());
            masterGaDenList.addAll(timVeService.getDistinctGaKetThuc());
            if (!masterGaDiList.isEmpty()) {
                comboBoxGaDi.getSelectionModel().selectFirst();
            }
            if (!masterGaDenList.isEmpty()) {
                comboBoxGaDen.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách ga: " + e.getMessage());
        }

        comboBoxGaDi.setOnAction(event -> {
            String gaDiDaChon = comboBoxGaDi.getSelectionModel().getSelectedItem();
            if (gaDiDaChon != null) {
                System.out.println("GA ĐI đã chọn: " + gaDiDaChon);
            }
        });

        comboBoxGaDen.setOnAction(event -> {
            String gaDenDaChon = comboBoxGaDen.getSelectionModel().getSelectedItem();
            if (gaDenDaChon != null) {
                System.out.println("GA ĐẾN đã chọn: " + gaDenDaChon);
            }
        });
    }

    private void setupAutoComplete(ComboBox<String> comboBox, ObservableList<String> masterList) {
        final BooleanProperty autoCompleteActive = new SimpleBooleanProperty(true);

        AutoCompletionBinding<String> autoCompletionBinding = TextFields.bindAutoCompletion(comboBox.getEditor(),
                request -> {
                    if (!autoCompleteActive.get()) {
                        return Collections.emptyList();
                    }

                    String userInput = request.getUserText().toLowerCase();
                    if (userInput.isBlank()) {
                        return FXCollections.observableArrayList();
                    }

                    String normalizedInput = removeDiacritics(userInput);
                    return masterList.stream()
                            .filter(item -> removeDiacritics(item.toLowerCase()).contains(normalizedInput))
                            .collect(Collectors.toList());
                });

        autoCompletionBinding.setOnAutoCompleted(event -> {
            comboBox.getSelectionModel().select(event.getCompletion());
        });

        comboBox.showingProperty().addListener((obs, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                comboBox.setItems(masterList);
                autoCompleteActive.set(false);
            } else {
                comboBox.getScene().getRoot().requestFocus();
            }
        });

        comboBox.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                autoCompleteActive.set(true);
            }
        });
        autoCompletionBinding.getAutoCompletionPopup().prefWidthProperty().bind(comboBox.widthProperty());
    }

    private String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    private void checkGaDiVaGaDen() {
        String gaDi = comboBoxGaDi.getEditor().getText();
        String gaDen = comboBoxGaDen.getEditor().getText();

        if (gaDi != null && gaDen != null && !gaDi.trim().isEmpty() && !gaDen.trim().isEmpty()) {
            if (gaDi.equalsIgnoreCase(gaDen)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Ga đi và ga đến không được trùng nhau.");
            }
        }
    }

    public void chonLoaiVe() {
        ToggleGroup group = new ToggleGroup();
        motChieuRadio.setToggleGroup(group);
        khuHoiRadio.setToggleGroup(group);
        motChieuRadio.setSelected(true);

        ngayDiDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (date.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: gray;");
                        }
                    }
                };
            }
        });

        ngayDiDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                ngayVeDatePicker.setValue(newValue);
            }
            ngayVeDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
                @Override
                public DateCell call(DatePicker datePicker) {
                    return new DateCell() {
                        @Override
                        public void updateItem(LocalDate date, boolean empty) {
                            super.updateItem(date, empty);
                            if (newValue != null && (date.isBefore(newValue) || date.isEqual(newValue))) {
                                setDisable(true);
                                setStyle("-fx-background-color: gray;");
                            }
                        }
                    };
                }
            });
        });

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == motChieuRadio) {
                ngayVeDatePicker.setDisable(true);
                ngayVeDatePicker.setValue(null);
            } else if (newValue == khuHoiRadio) {
                ngayVeDatePicker.setDisable(false);
            }
        });

        motChieuRadio.setSelected(true);
        ngayVeDatePicker.setDisable(true);
    }

    private void setupTableView(TableView<LichTrinh> tableView) {
        TableColumn<LichTrinh, String> maTauCol = new TableColumn<>("Mã tàu");
        maTauCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTauByMaTau().getMaTau()));

        TableColumn<LichTrinh, String> gioDiCol = new TableColumn<>("Giờ đi");
        gioDiCol.setCellValueFactory(cellData -> {
            java.sql.Time gioDi = cellData.getValue().getGioDi();
            return new SimpleStringProperty(gioDi != null ? gioDi.toLocalTime().toString() : "");
        });

        TableColumn<LichTrinh, String> gioDenCol = new TableColumn<>("Giờ đến");
        gioDenCol.setCellValueFactory(cellData -> {
            java.sql.Time gioDen = cellData.getValue().getGioDen();
            return new SimpleStringProperty(gioDen != null ? gioDen.toLocalTime().toString() : "");
        });

        TableColumn<LichTrinh, String> gaKhoiHanhCol = new TableColumn<>("Ga khởi hành");
        gaKhoiHanhCol.setCellValueFactory(cellData -> {
            String gaKhoiHanh = cellData.getValue().getGaKhoiHanh();
            return new SimpleStringProperty(gaKhoiHanh != null ? gaKhoiHanh : "");
        });

        TableColumn<LichTrinh, String> gaKetThucCol = new TableColumn<>("Ga kết thúc");
        gaKetThucCol.setCellValueFactory(cellData -> {
            String gaKetThuc = cellData.getValue().getGaKetThuc();
            return new SimpleStringProperty(gaKetThuc != null ? gaKetThuc : "");
        });

        tableView.getColumns().clear();
        tableView.getColumns().addAll(maTauCol, gioDiCol, gioDenCol, gaKhoiHanhCol, gaKetThucCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleTimKiem() {
        gaDi = comboBoxGaDi.getValue();
        gaDen = comboBoxGaDen.getValue();
        ngayDiLD = ngayDiDatePicker.getValue();
        ngayVeLD = ngayVeDatePicker.getValue();
        isMotChieu = motChieuRadio.isSelected();

        toaTauGridPane.getChildren().clear();

        if (gaDi == null || gaDi.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập ga đi.");
            comboBoxGaDi.requestFocus();
            lamMoi();
            return;
        }
        if (gaDen == null || gaDen.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập ga đến.");
            comboBoxGaDen.requestFocus();
            lamMoi();
            return;
        }
        if (ngayDiLD == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn ngày đi.");
            ngayDiDatePicker.requestFocus();
            lamMoi();
            return;
        }
        if (!isMotChieu && ngayVeLD == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn ngày về cho vé khứ hồi.");
            ngayVeDatePicker.requestFocus();
            lamMoi();
            return;
        }

        Date ngayDi = Date.valueOf(ngayDiLD);
        Date ngayVe = !isMotChieu && ngayVeLD != null
                ? Date.valueOf(ngayVeLD)
                : null;

        try {
            List<LichTrinh> lichTrinhDiList = timVeService.timLichTrinh(gaDi, gaDen, ngayDi);
            VBox parentVBox = (VBox) anchorPaneMain.getParent();

            if (isMotChieu) {
                if (parentVBox.getChildren().contains(anchorPaneMain1)) {
                    parentVBox.getChildren().remove(anchorPaneMain1);
                }

                if (lichTrinhDiList == null || lichTrinhDiList.isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không tìm thấy lịch trình phù hợp cho lượt đi.");
                    lamMoi();
                } else {
                    luotDilb.setText("LƯỢT ĐI");
                    dataLichTrinhDi.setAll(lichTrinhDiList);
                    tableViewDi.setItems(dataLichTrinhDi);
                }

                VBox.setVgrow(anchorPaneMain, Priority.ALWAYS);
            } else {
                List<LichTrinh> lichTrinhVeList = timVeService.timLichTrinh(gaDen, gaDi, ngayVe);

                if (!parentVBox.getChildren().contains(anchorPaneMain1)) {
                    parentVBox.getChildren().add(anchorPaneMain1);
                }

                if ((lichTrinhDiList == null || lichTrinhDiList.isEmpty()) ||
                        (lichTrinhVeList == null || lichTrinhVeList.isEmpty())) {
                    showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                            "Không tìm thấy lịch trình phù hợp cho " +
                                    (lichTrinhDiList == null || lichTrinhDiList.isEmpty() ? "lượt đi." : "lượt về."));
                    lamMoi();
                } else {
                    luotDilb.setText("LƯỢT ĐI");
                    dataLichTrinhDi.setAll(lichTrinhDiList);
                    tableViewDi.setItems(dataLichTrinhDi);
                    dataLichTrinhVe.setAll(lichTrinhVeList);
                    tableViewVe.setItems(dataLichTrinhVe);
                }

                VBox.setVgrow(anchorPaneMain, Priority.NEVER);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm lịch trình: " + e.getMessage());
        }
    }

    private void lamMoi() {
        dataLichTrinhDi.clear();
        dataLichTrinhVe.clear();
        tableViewDi.setItems(dataLichTrinhDi);
        tableViewVe.setItems(dataLichTrinhVe);
        lamMoiGiaoDien();

        VBox parentVBox = (VBox) anchorPaneMain.getParent();
        if (parentVBox.getChildren().contains(anchorPaneMain1)) {
            parentVBox.getChildren().remove(anchorPaneMain1);
        }
        VBox.setVgrow(anchorPaneMain, Priority.ALWAYS);
        anchorPaneMain.setMaxHeight(Double.MAX_VALUE);
        loaiToaGhe.setText("");
        luotDilb.setText("LƯỢT ĐI");
        toaTaulb.setText("TOA TÀU");
        dsGhelb.setText("Danh Sách Ghế");
    }

    public void lamMoiGiaoDien() {
        toaTauGridPane.getChildren().clear();
        gridPaneGheTrai.getChildren().clear();
        anchorPaneMain.getChildren().clear();
        toaTauPane.getChildren().clear();
        dsGheContaner.getChildren().clear();

        toaTauGridPane.getChildren().addAll(toaTauChildren);
        gridPaneGheTrai.getChildren().addAll(gheTraiChildren);
        anchorPaneMain.getChildren().addAll(tableLuotDiContainer);
        dsGheContaner.getChildren().addAll(dsgContainer);
        toaTauPane.getChildren().addAll(ttContainer);
    }

    @FXML
    private void chuyenSangBanVe() {
        if (veList.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Bạn chưa chọn vé nào để mua.");
            return;
        }
        gaDi = comboBoxGaDi.getValue();
        gaDen = comboBoxGaDen.getValue();
        ngayDiLD = ngayDiDatePicker.getValue();
        ngayVeLD = ngayVeDatePicker.getValue();
        isMotChieu = motChieuRadio.isSelected();
        if (gaDi == null || gaDen == null || ngayDiLD == null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Ve ve = veList.get(veList.size() - 1);
            gaDi = ve.getLichtrinhByMaLt().getGaKhoiHanh();
            gaDen = ve.getLichtrinhByMaLt().getGaKetThuc();
            ngayDiLD = ve.getLichtrinhByMaLt().getNgayKhoiHanh().toLocalDate();
            ngayVeLD = ve.getLichtrinhByMaLt().getNgayDen().toLocalDate();
            isMotChieu = true;
            motChieuRadio.setSelected(isMotChieu);
        }
        FXMLLoader loader = MenuNhanVienController.instance.readyUI("BanVe/BanVe");
        BanVeController banVeController = loader.getController();
        banVeController.setVeList(new ArrayList<>(veList), gheDaChonMapDi, gheDaChonMapVe, gaDi, gaDen, ngayDiLD,
                ngayVeLD, isMotChieu);
        banVeController.setNhanVien(this.nhanVien);
    }

    @FXML
    private void lamMoiGD() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn làm mới?");
        confirmAlert.setContentText("Mọi thay đổi chưa lưu sẽ bị mất.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Unlock all selected seats before refreshing
            for (Map.Entry<ToaTau, Set<Ghe>> entry : gheDaChonMapDi.entrySet()) {
                for (Ghe ghe : entry.getValue()) {
                    unlockSeatAsync(ghe.getMaGhe(), null);
                }
            }
            for (Map.Entry<ToaTau, Set<Ghe>> entry : gheDaChonMapVe.entrySet()) {
                for (Ghe ghe : entry.getValue()) {
                    unlockSeatAsync(ghe.getMaGhe(), null);
                }
            }
            veList.clear();
            gheDaChonMapDi.clear();
            gheDaChonMapVe.clear();
            FXMLLoader loader = MenuNhanVienController.instance.readyUI("BanVe/TimVe");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    public void setTroLai(List<Ve> veListParam,
                          Map<ToaTau, Set<Ghe>> gheDaChonMapDiParam,
                          Map<ToaTau, Set<Ghe>> gheDaChonMapVeParam,
                          String gaDiParam,
                          String gaDenParam,
                          LocalDate ngayDiLDParam,
                          LocalDate ngayVeLDParam,
                          boolean isMotChieuParam) {
        // Gán lại dữ liệu
        this.veList.setAll(veListParam);
        this.gheDaChonMapDi.clear();
        this.gheDaChonMapDi.putAll(gheDaChonMapDiParam);
        this.gheDaChonMapVe.clear();
        this.gheDaChonMapVe.putAll(gheDaChonMapVeParam);

        this.gaDi = gaDiParam;
        this.gaDen = gaDenParam;
        this.ngayDiLD = ngayDiLDParam;
        this.ngayVeLD = ngayVeLDParam;
        this.isMotChieu = isMotChieuParam;

        // Gán lại giao diện
        comboBoxGaDi.setValue(gaDiParam);
        comboBoxGaDen.setValue(gaDenParam);
        ngayDiDatePicker.setValue(ngayDiLDParam);
        ngayVeDatePicker.setValue(ngayVeLDParam);
        motChieuRadio.setSelected(isMotChieuParam);
        if (!isMotChieuParam) {
            khuHoiRadio.setSelected(true);
        }

        // Xóa tất cả khóa ghế trên server
        Task<Void> unlockAllSeatsTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Ve ve : veList) {
                    try {
                        timVeService.unlockSeat(ve.getGheByMaGhe().getMaGhe());
                    } catch (Exception e) {
                        System.err.println("Failed to unlock seat: " + ve.getGheByMaGhe().getMaGhe() + ", error: " + e.getMessage());
                    }
                }
                return null;
            }
        };
        unlockAllSeatsTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể xóa khóa ghế cho một số ghế.");
            });
        });
        new Thread(unlockAllSeatsTask).start();

        // Tìm lại lịch trình và set
        handleTimKiem();

        // Hiển thị toa tàu và ghế đã chọn
        if (!dataLichTrinhDi.isEmpty()) {
            selectedLichTrinhDi = dataLichTrinhDi.get(0);
            tableViewDi.getSelectionModel().select(0);
            hienThiToaTau(selectedLichTrinhDi.getTauByMaTau().getMaTau(), ngayDiLD, true);
        }

        if (!isMotChieu && !dataLichTrinhVe.isEmpty()) {
            selectedLichTrinhVe = dataLichTrinhVe.get(0);
            tableViewVe.getSelectionModel().select(0);
            hienThiToaTau(selectedLichTrinhVe.getTauByMaTau().getMaTau(), ngayVeLD, false);
        }
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }
}