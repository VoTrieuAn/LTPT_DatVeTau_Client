package controller.BanVe;

import common.LoaiToa;
import controller.Menu.MenuNhanVienController;
import entity.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import service.banVe.TimVeService;

import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TimVeController implements Initializable {
    @FXML
    private ComboBox<String> comboBoxGaDi;
    @FXML
    private ComboBox<String> comboBoxGaDen;
    @FXML
    private RadioButton motChieuRadio;
    @FXML
    private RadioButton khuHoiRadio;
    @FXML
    private DatePicker ngayDiDatePicker;
    @FXML
    private DatePicker ngayVeDatePicker;
    @FXML
    private Button btnTimKiem;
    @FXML
    private AnchorPane anchorPaneMain; // Lượt đi
    @FXML
    private AnchorPane anchorPaneMain1; // Lượt về
    @FXML
    private TableView<LichTrinh> tableViewDi; // table luot di
    @FXML
    private TableView<LichTrinh> tableViewVe;
    @FXML
    private Label loaiToaGhe;
    @FXML
    private GridPane toaTauGridPane;
    @FXML
    private GridPane gridPaneGheTrai;
    @FXML
    private Label luotDilb;
    @FXML
    private Label dsGhelb;
    @FXML
    private Label toaTaulb;
    @FXML
    private AnchorPane toaTauPane;
    @FXML
    private AnchorPane dsGheContaner;

    private Button selectedToaButton;
    private TimVeService timVeService; // Sử dụng service thay vì DAO

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
        toaTauChildren = new ArrayList<>(toaTauGridPane.getChildren());
        gheTraiChildren = new ArrayList<>(gridPaneGheTrai.getChildren());
        tableLuotDiContainer = new ArrayList<>(anchorPaneMain.getChildren());
        ttContainer = new ArrayList<>(toaTauPane.getChildren());
        dsgContainer = new ArrayList<>(dsGheContaner.getChildren());

        VBox parentVBox = (VBox) anchorPaneMain.getParent();
        parentVBox.getChildren().remove(anchorPaneMain1);
        VBox.setVgrow(anchorPaneMain, Priority.ALWAYS);

        // Khởi tạo service
        timVeService = new TimVeService();

        // Tải danh sách ga đi và ga đến
        timVeService.loadGaDiGaDen(masterGaDiList, masterGaDenList);

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

        // Tải danh sách lịch trình mặc định
        tableViewDi.setItems(timVeService.layDanhSachLichTrinhMacDinh());

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

        if (!tableViewDi.getItems().isEmpty()) {
            tableViewDi.getSelectionModel().select(0);
        }
    }

    private void hienThiToaTau(String maTau, LocalDate lichTrinh, boolean isDi) {
        List<ToaTau> toaTauList = timVeService.timToaTauTheoMaTau(maTau);

        if (toaTauList == null || toaTauList.isEmpty()) {
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
            List<Ghe> gheDaMua = timVeService.layDanhSachGheDaMua(toaTau.getTauByMaTau().getMaTau(), lichTrinh);

            long soGheDaMuaTrongToa = gheDaMua.stream()
                    .filter(ghe -> ghe.getToatauByMaTt().getMaTt().equals(toaTau.getMaTt()))
                    .count();

            boolean tatCaGheDaMua = danhSachGhe.size() > 0 && danhSachGhe.size() == soGheDaMuaTrongToa;

            toaButton.setText("Toa " + toaTau.getSoToa());
            Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/view/images/train_11088660.png")));
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
        List<Ghe> danhSachGhe = timVeService.layDanhSachGheTrongToa(toaTau.getMaTt());
        int totalSeats = danhSachGhe.size();
        List<Ghe> gheDaMua = timVeService.layDanhSachGheDaMua(toaTau.getTauByMaTau().getMaTau(), lichTrinh);
        Set<String> gheDaMuaSet = gheDaMua.stream().map(Ghe::getMaGhe).collect(Collectors.toSet());

        gheDaChonMap.putIfAbsent(toaTau, new HashSet<>());

        for (int i = 0; i < totalSeats; i++) {
            Ghe ghe = danhSachGhe.get(i);
            String maGhe = ghe.getMaGhe();
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

            if (gheDaMuaSet.contains(maGhe)) {
                gheButton.setDisable(true);
                gheButton.setStyle("-fx-border-color: red;");
            } else if (gheDaChonMap.get(toaTau).contains(ghe)) {
                gheButton.setSelected(true);
                gheButton.setStyle("-fx-border-color: #2bbaba; -fx-border-width: 3; -fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");
            } else {
                gheButton.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
            }

            gheButton.setOnAction(event -> {
                if (!gheDaMuaSet.contains(maGhe)) {
                    if (gheButton.isSelected()) {
                        gheButton.setStyle("-fx-border-color: #2bbaba; -fx-border-width: 3; -fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10;");
                        gheDaChonMap.get(toaTau).add(ghe);
                        timVeService.themVe(veList, ghe, toaTau, isDi ? selectedLichTrinhDi : selectedLichTrinhVe);
                    } else {
                        gheButton.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-border-radius: 10; -fx-background-radius: 10;");
                        gheDaChonMap.get(toaTau).remove(ghe);
                        timVeService.xoaVe(veList, ghe);
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
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Ga đi và ga đến không được trùng nhau.");
                alert.showAndWait();
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

        List<LichTrinh> lichTrinhDiList = timVeService.timLichTrinh(gaDi, gaDen, ngayDiLD);
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
            List<LichTrinh> lichTrinhVeList = timVeService.timLichTrinh(gaDen, gaDi, ngayVeLD);

            if (!parentVBox.getChildren().contains(anchorPaneMain1)) {
                parentVBox.getChildren().add(anchorPaneMain1);
            }

            if ((lichTrinhDiList == null || lichTrinhDiList.isEmpty()) ||
                    (lichTrinhVeList == null || lichTrinhVeList.isEmpty())) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                        "Không tìm thấy lịch trình phù hợp cho " +
                                (lichTrinhDiList.isEmpty() ? "lượt đi." : "lượt về."));
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
            FXMLLoader loader = MenuNhanVienController.instance.readyUI("BanVe/TimVe");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public void setTroLai(List<Ve> veListParam,
                          Map<ToaTau, Set<Ghe>> gheDaChonMapDiParam,
                          Map<ToaTau, Set<Ghe>> gheDaChonMapVeParam,
                          String gaDiParam,
                          String gaDenParam,
                          LocalDate ngayDiLDParam,
                          LocalDate ngayVeLDParam,
                          boolean isMotChieuParam) {
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

        comboBoxGaDi.setValue(gaDiParam);
        comboBoxGaDen.setValue(gaDenParam);
        ngayDiDatePicker.setValue(ngayDiLDParam);
        ngayVeDatePicker.setValue(ngayVeLDParam);
        motChieuRadio.setSelected(true);
        if (isMotChieuParam) {
            motChieuRadio.setSelected(true);
        } else {
            khuHoiRadio.setSelected(true);
        }
        handleTimKiem();
        tableViewDi.getSelectionModel().select(0);
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }
}