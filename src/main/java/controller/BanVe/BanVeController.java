package controller.BanVe;

import common.LoaiHanhKhach;
import common.LoaiVe;
import config.TrainTicketApplication;
import controller.Menu.MenuNhanVienController;
import dao.KhuyenMaiDAO;
import entity.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import rmi.RMIServiceLocator;
import service.HoaDonService;
import service.KhuyenMaiService;
import service.LichTrinhService;
import util.HoadonCodeGeneratorUtil;

import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class BanVeController implements Initializable {

    @FXML private TextField cccdTextField;
    @FXML private TextField hoTenTextField;
    @FXML private Button btnThem1;
    @FXML private TextField emailTextField;
    @FXML private DatePicker ngayTaoDatePicker;
    @FXML private TextField tienGiamTextField;
    @FXML private TextField tienKhachDuaTextField;
    @FXML private Label lblTienThua;
    @FXML private Label lblTongTien;
    @FXML private Button btnNhanDuTien;
    @FXML private TextField maHoaDonTextField;
    @FXML private TableView<Ve> tableViewBanVe;
    @FXML private TableColumn<Ve, Void> colTT;
    @FXML private TableColumn<Ve, String> colHanhTrinh;
    @FXML private TableColumn<Ve, String> colTenKH;
    @FXML private TableColumn<Ve, String> colThongTinKH;
    @FXML private TableColumn<Ve, String> colKhuyenMai;
    @FXML private TableColumn<Ve, String> colThongTinGhe;
    @FXML private TableColumn<Ve, Double> colThue;
    @FXML private TableColumn<Ve, Double> colThanhTien;
    @FXML private Button btnXoa;
    @FXML private Button btnXoaTatCa;
    @FXML private Button btnTroLai;
    @FXML private Button btnThem;
    @FXML private Button btnTraVaIn;
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;
    @FXML private Label lblPageInfo;

    private ObservableList<Ve> veList = FXCollections.observableArrayList();
    private ObservableList<Ve> displayedVeList = FXCollections.observableArrayList();
    private final Map<ToaTau, Set<Ghe>> gheDaChonMapDi = new HashMap<>();
    private final Map<ToaTau, Set<Ghe>> gheDaChonMapVe = new HashMap<>();
    private DecimalFormat decimalFormat;
    private HoaDonService hoaDonService;
    private LichTrinhService lichTrinhService;
    private KhuyenMaiService khuyenMaiService;
    private NhanVien nhanVien;
    private boolean isFormatting = false;
    private String gaDi;
    private String gaDen;
    private LocalDate ngayDiLD;
    private LocalDate ngayVeLD;
    private boolean isMotChieu;
    private int currentPage = 1;
    private final int rowsPerPage = 10;
    private long totalRecords;
    private List<KhuyenMai> cacheDanhSachKhuyenMai = null;

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public void setVeList(List<Ve> veListParam, Map<ToaTau, Set<Ghe>> gheDaChonMapDiParam,
                          Map<ToaTau, Set<Ghe>> gheDaChonMapVeParam, String gaDiParam, String gaDenParam,
                          LocalDate ngayDiLDParam, LocalDate ngayVeLDParam, boolean isMotChieuParam) {
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

        xacDinhLoaiVe(this.veList);
        for (Ve ve : veList) {
            try {
                KhuyenMai khuyenMaiTotNhat = hoaDonService.layKhuyenMaiTotNhat(ve);
                ve.setKhuyenmaiByMaKm(khuyenMaiTotNhat);
                capNhatGiaVe(ve);
            } catch (RemoteException e) {
                showAlert("Lỗi", "Không thể cập nhật khuyến mãi: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
        totalRecords = veList.size();
        currentPage = 1;
        updateDisplayedVeList();
        capNhatTongTienGiam();
        capNhatTongTien();
        updatePageInfo();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        decimalFormat = new DecimalFormat("#,###", symbols);
        decimalFormat.setMaximumFractionDigits(0);

        hoaDonService = RMIServiceLocator.getHoaDonService();
        lichTrinhService = RMIServiceLocator.getLichTrinhService();
        khuyenMaiService = RMIServiceLocator.getKhuyenMaiService();
        setupTienNhapAutoComplete();

        setupTable(tableViewBanVe);
        tableViewBanVe.setItems(displayedVeList);
        tableViewBanVe.setEditable(true);

        maHoaDonTextField.setText(HoadonCodeGeneratorUtil.generateMaHoadon(
                TrainTicketApplication.getInstance().getEntityManagerFactory().createEntityManager()));
        ngayTaoDatePicker.setValue(LocalDate.now());
        ngayTaoDatePicker.addEventFilter(MouseEvent.ANY, event -> event.consume());

        setupListeners();
        updatePageInfo();
    }

    private void setupListeners() {
        veList.addListener((ListChangeListener<Ve>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasUpdated()) {
                    totalRecords = veList.size();
                    if (currentPage > getTotalPages()) {
                        currentPage = Math.toIntExact(Math.max(1, getTotalPages()));
                    }
                    updateDisplayedVeList();
                    capNhatTongTien();
                    capNhatTongTienGiam();
                    updatePageInfo();
                }
            }
        });

        btnNhanDuTien.setOnAction(event -> tienKhachDuaTextField.setText(lblTongTien.getText()));

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            if (!Character.isDigit(newText.charAt(0))) return null;
            for (char c : newText.toCharArray()) {
                if (!Character.isDigit(c) && c != '.' && c != ',') return null;
            }
            return change;
        };
        tienKhachDuaTextField.setTextFormatter(new TextFormatter<>(filter));

        tienKhachDuaTextField.textProperty().addListener((obs, oldVal, newVal) -> capNhatTienThua());

        btnXoa.setOnAction(event -> xoaVe());
        btnXoaTatCa.setOnAction(event -> xoaTatCaVe());

        cccdTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            String cccd = newValue;
            if (!cccd.isEmpty()) {
                HanhKhach khachHang = null;
                try {
                    khachHang = hoaDonService.timKiemByCccd(cccd);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                if (khachHang != null) {
                    hoTenTextField.setText(khachHang.getHoTenDem() + " " + khachHang.getTen());
                    emailTextField.setText(khachHang.getEmail());
                    btnThem1.setDisable(true);
                } else {
                    hoTenTextField.setText("");
                    emailTextField.setText("");
                    btnThem1.setDisable(false);
                }
            } else {
                hoTenTextField.setText("");
                emailTextField.setText("");
                btnThem1.setDisable(false);
            }
        });

        tienKhachDuaTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isFormatting) return;
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) {
                lblTienThua.setText("0");
                return;
            }
            try {
                long amount = Long.parseLong(digits);
                String formatted = decimalFormat.format(amount);
                if (!formatted.equals(newVal)) {
                    isFormatting = true;
                    tienKhachDuaTextField.setText(formatted);
                    tienKhachDuaTextField.positionCaret(tienKhachDuaTextField.getLength());
                    isFormatting = false;
                }
            } catch (NumberFormatException e) {
                lblTienThua.setText("0");
            }
        });

        btnPrevious.setDisable(true);
        btnNext.setDisable(true);
    }

    @FXML
    private void handlePrevious() {
        if (currentPage > 1) {
            currentPage--;
            updateDisplayedVeList();
            updatePageInfo();
        }
    }

    @FXML
    private void handleNext() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            updateDisplayedVeList();
            updatePageInfo();
        }
    }

    private void updateDisplayedVeList() {
        displayedVeList.clear();
        int startIndex = (currentPage - 1) * rowsPerPage;
        int endIndex = Math.min(startIndex + rowsPerPage, veList.size());
        if (startIndex < veList.size()) {
            displayedVeList.addAll(veList.subList(startIndex, endIndex));
        }
        tableViewBanVe.refresh();
    }

    private void updatePageInfo() {
        long totalPages = getTotalPages();
        lblPageInfo.setText(String.format("Page %d of %d", currentPage, totalPages));
        btnPrevious.setDisable(currentPage == 1);
        btnNext.setDisable(currentPage >= totalPages || veList.isEmpty());
    }

    private long getTotalPages() {
        return (totalRecords + rowsPerPage - 1) / rowsPerPage;
    }

    @FXML
    private void controller(ActionEvent event) {
        if (event.getSource() == btnTraVaIn) {
            luuHoaDonVaVe();
        }
    }

    private void setupTable(TableView<Ve> tableView) {
        colTT.setCellFactory(column -> new TableCell<Ve, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf((currentPage - 1) * rowsPerPage + getIndex() + 1));
            }
        });
        colTT.setSortable(false);

        colHanhTrinh.setCellValueFactory(cellData -> {
            LichTrinh lt = cellData.getValue().getLichtrinhByMaLt();
            String hanhTrinh = lt != null ? lt.getGaKhoiHanh() + " - " + lt.getGaKetThuc() + "\n" + lt.getTauByMaTau().getMaTau() : "";
            return new SimpleStringProperty(hanhTrinh);
        });

        colTenKH.setCellValueFactory(cellData -> {
            Ve ve = cellData.getValue();
            if (ve.getHoTen() == null) {
                ve.setHoTen("");
            }
            return new SimpleStringProperty(ve.getHoTen());
        });

        colTenKH.setCellFactory(column -> new TableCell<Ve, String>() {
            private final TextField textField = new TextField();

            {
                textField.setId("textFieldTenKH");
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit(textField.getText());
                    }
                });

                textField.setOnAction(event -> {
                    commitEdit(textField.getText());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    textField.setText(item);
                    setGraphic(textField);
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                textField.requestFocus();
            }

            @Override
            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                Ve ve = getTableView().getItems().get(getIndex());
                int globalIndex = (currentPage - 1) * rowsPerPage + getIndex();
                if (globalIndex < veList.size()) {
                    veList.get(globalIndex).setHoTen(newValue.trim());
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                textField.setText(getItem());
            }

            public TextField getTextField() {
                return textField;
            }
        });

        colThongTinKH.setCellFactory(column -> new TableCell<Ve, String>() {
            private final ComboBox<LoaiHanhKhach> comboBox = new ComboBox<>();
            private final Label label = new Label();
            private final TextField cccdField = new TextField();
            private final DatePicker datePicker = new DatePicker();
            private final VBox vBox = new VBox();
            private boolean isUpdatingDatePicker = false;

            {
                comboBox.setId("comboBoxLoaiKH");
                cccdField.setId("textFieldCCCD");
                datePicker.setId("datePickerNgaySinh");

                comboBox.setCellFactory(cb -> new ListCell<>() {
                    @Override
                    protected void updateItem(LoaiHanhKhach item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                });
                comboBox.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(LoaiHanhKhach item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getName());
                    }
                });
                comboBox.getItems().addAll(LoaiHanhKhach.values());

                comboBox.setOnAction(event -> {
                    LoaiHanhKhach selected = comboBox.getValue();
                    Ve ve = getTableView().getItems().get(getIndex());
                    int globalIndex = (currentPage - 1) * rowsPerPage + getIndex();
                    if (globalIndex < veList.size()) {
                        veList.get(globalIndex).setLoaiKh(selected);
                    }

                    if (selected == LoaiHanhKhach.NGUOI_CAO_TUOI) {
                        label.setText("CCCD:");
                        vBox.getChildren().setAll(comboBox, label, cccdField, new Label("Ngày sinh:"), datePicker);
                        cccdField.setDisable(false);
                        datePicker.setDisable(false);
                    } else if (selected == LoaiHanhKhach.NGUOI_LON || selected == LoaiHanhKhach.HOC_SINH_SINH_VIEN) {
                        label.setText("CCCD:");
                        vBox.getChildren().setAll(comboBox, label, cccdField);
                        cccdField.setDisable(false);
                        datePicker.setDisable(true);
                    } else {
                        label.setText("Ngày sinh:");
                        vBox.getChildren().setAll(comboBox, label, datePicker);
                        cccdField.setDisable(true);
                        datePicker.setDisable(false);
                    }

                    try {
                        KhuyenMai khuyenMaiTotNhat = hoaDonService.layKhuyenMaiTotNhat(ve);
                        if (globalIndex < veList.size()) {
                            veList.get(globalIndex).setKhuyenmaiByMaKm(khuyenMaiTotNhat);
                            capNhatGiaVe(veList.get(globalIndex));
                        }
                    } catch (RemoteException e) {
                        showAlert("Lỗi", "Không thể cập nhật khuyến mãi: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                    capNhatTongTien();
                    capNhatTongTienGiam();
                    tableViewBanVe.refresh();
                });

                cccdField.setDisable(true);
                datePicker.setDisable(true);
                datePicker.setConverter(new StringConverter<LocalDate>() {
                    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    @Override
                    public String toString(LocalDate date) {
                        return date != null ? dateFormatter.format(date) : "";
                    }

                    @Override
                    public LocalDate fromString(String string) {
                        return string != null && !string.isEmpty() ? LocalDate.parse(string, dateFormatter) : null;
                    }
                });
                datePicker.setDayCellFactory(dp -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isAfter(LocalDate.now())) setDisable(true);
                    }
                });

                cccdField.textProperty().addListener((obs, oldVal, newVal) -> {
                    Ve ve = getTableView().getItems().get(getIndex());
                    int globalIndex = (currentPage - 1) * rowsPerPage + getIndex();
                    if (globalIndex < veList.size()) {
                        veList.get(globalIndex).setCccd(newVal);
                    }
                });

                datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (isUpdatingDatePicker) return;
                    Ve ve = getTableView().getItems().get(getIndex());
                    int globalIndex = (currentPage - 1) * rowsPerPage + getIndex();
                    if (globalIndex < veList.size()) {
                        if (newVal != null) {
                            veList.get(globalIndex).setNgaySinhTreEm(Date.valueOf(newVal));
                            try {
                                KhuyenMai khuyenMaiTotNhat = hoaDonService.layKhuyenMaiTotNhat(ve);
                                veList.get(globalIndex).setKhuyenmaiByMaKm(khuyenMaiTotNhat);
                                capNhatGiaVe(veList.get(globalIndex));
                            } catch (RemoteException e) {
                                showAlert("Lỗi", "Không thể cập nhật khuyến mãi: " + e.getMessage(), Alert.AlertType.ERROR);
                            }
                            capNhatTongTien();
                            tableViewBanVe.refresh();
                        } else {
                            veList.get(globalIndex).setNgaySinhTreEm(null);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Ve ve = getTableView().getItems().get(getIndex());
                    if (ve != null && ve.getLoaiKh() != null) {
                        comboBox.setValue(ve.getLoaiKh());
                        if (ve.getLoaiKh() == LoaiHanhKhach.NGUOI_CAO_TUOI) {
                            label.setText("CCCD:");
                            vBox.getChildren().setAll(comboBox, label, cccdField, new Label("Ngày sinh:"), datePicker);
                            cccdField.setDisable(false);
                            datePicker.setDisable(false);
                            cccdField.setText(ve.getCccd());
                            isUpdatingDatePicker = true;
                            datePicker.setValue(ve.getNgaySinhTreEm() != null ? ve.getNgaySinhTreEm().toLocalDate() : null);
                            isUpdatingDatePicker = false;
                        } else if (ve.getLoaiKh() == LoaiHanhKhach.NGUOI_LON || ve.getLoaiKh() == LoaiHanhKhach.HOC_SINH_SINH_VIEN) {
                            label.setText("CCCD:");
                            vBox.getChildren().setAll(comboBox, label, cccdField);
                            cccdField.setDisable(false);
                            datePicker.setDisable(true);
                            cccdField.setText(ve.getCccd());
                        } else {
                            label.setText("Ngày sinh:");
                            vBox.getChildren().setAll(comboBox, label, datePicker);
                            cccdField.setDisable(true);
                            datePicker.setDisable(false);
                            isUpdatingDatePicker = true;
                            datePicker.setValue(ve.getNgaySinhTreEm() != null ? ve.getNgaySinhTreEm().toLocalDate() : null);
                            isUpdatingDatePicker = false;
                        }
                    } else {
                        comboBox.setValue(null);
                        vBox.getChildren().setAll(comboBox);
                        cccdField.setDisable(true);
                        datePicker.setDisable(true);
                    }
                    setGraphic(vBox);
                }
            }
        });

        colKhuyenMai.setCellFactory(column -> new TableCell<Ve, String>() {
            private final Label lblTenKhuyenMai = new Label();
            private final Label lblGiaTriKhuyenMai = new Label("0");

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Ve ve = getTableView().getItems().get(getIndex());
                    LocalDate ngayMuaVe = LocalDate.now();
                    LocalDate ngayKhoiHanh = ve.getLichtrinhByMaLt().getNgayKhoiHanh().toLocalDate();

                    try {
                        // Cache danh sách khuyến mãi nếu lần đầu
                        if (cacheDanhSachKhuyenMai == null) {
                            cacheDanhSachKhuyenMai = khuyenMaiService.getDanhSach("KhuyenMai.findAll", KhuyenMai.class);
                        }

                        List<KhuyenMai> danhSachKhuyenMaiApDung = cacheDanhSachKhuyenMai.stream()
                                .filter(km -> km.isTrangThai()) // Khuyến mãi phải còn hiệu lực
                                .filter(km -> {
                                    if (km instanceof KhuyenMaiKhachHang khuyenMaiKhachHang) {
                                        return ve.getLoaiKh() == khuyenMaiKhachHang.getLoaiKH();
                                    } else if (km instanceof KhuyenMaiNgay khuyenMaiNgay) {
                                        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(ngayMuaVe, ngayKhoiHanh);
                                        return daysBetween >= khuyenMaiNgay.getNgayToiThieuTruocKM();
                                    } else if (km instanceof KhuyenMaiVe khuyenMaiVe) {
                                        return ve.getLoaiVe() == khuyenMaiVe.getLoaiVe();
                                    }
                                    return false;
                                })
                                .collect(Collectors.toList());

                        if (ve.getKhuyenmaiByMaKm() == null && !danhSachKhuyenMaiApDung.isEmpty()) {
                            KhuyenMai khuyenMaiTotNhat = hoaDonService.layKhuyenMaiTotNhat(ve);
                            int globalIndex = (currentPage - 1) * rowsPerPage + getIndex();

                            if (globalIndex < veList.size()) {
                                veList.get(globalIndex).setKhuyenmaiByMaKm(khuyenMaiTotNhat);
                                capNhatGiaVe(veList.get(globalIndex));
                                capNhatTongTien();
                                capNhatTongTienGiam();
                                tableViewBanVe.refresh();
                            }
                        }

                        KhuyenMai khuyenMai = ve.getKhuyenmaiByMaKm();
                        if (khuyenMai != null) {
                            lblTenKhuyenMai.setText(khuyenMai.getTenKhuyenMai());
                            double discount = khuyenMai.getGiaTriKhuyenMai() * ve.getGheByMaGhe().getGiaGhe() / 100;
                            lblGiaTriKhuyenMai.setText(formatCurrency(discount));
                        } else {
                            lblTenKhuyenMai.setText("Không có khuyến mãi");
                            lblGiaTriKhuyenMai.setText("0");
                        }

                    } catch (Exception e) {
                        showAlert("Lỗi", "Không thể cập nhật khuyến mãi: " + e.getMessage(), Alert.AlertType.ERROR);
                    }

                    VBox vbox = new VBox(lblTenKhuyenMai, lblGiaTriKhuyenMai);
                    setGraphic(vbox);
                }
            }
        });


        colThongTinGhe.setCellValueFactory(cellData -> {
            Ghe ghe = cellData.getValue().getGheByMaGhe();
            ToaTau toaTau = ghe != null ? ghe.getToatauByMaTt() : null;
            String giaGheInfo = ghe != null && toaTau != null
                    ? String.format("Ghế %s - Toa %s \n %s", ghe.getSoGhe(), toaTau.getSoToa(), formatCurrency(ghe.getGiaGhe()))
                    : "";
            return new SimpleStringProperty(giaGheInfo);
        });

        colThue.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getGheByMaGhe().getGiaGhe() * cellData.getValue().getThueSuatGtgt()).asObject());
        colThue.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        colThanhTien.setCellValueFactory(cellData -> {
            Ve ve = cellData.getValue();
            double giaGhe = ve.getGheByMaGhe().getGiaGhe();
            double thueAmount = giaGhe * ve.getThueSuatGtgt();
            double giamGia = ve.getKhuyenmaiByMaKm() != null ? ve.getKhuyenmaiByMaKm().getGiaTriKhuyenMai() * giaGhe / 100 : 0;
            return new SimpleDoubleProperty(giaGhe + thueAmount - giamGia).asObject();
        });
        colThanhTien.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        tableView.getColumns().setAll(colTT, colHanhTrinh, colTenKH, colThongTinKH, colKhuyenMai, colThongTinGhe, colThue, colThanhTien);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        capNhatTongTien();
    }

    private void capNhatTongTien() {
        double tongTien = tinhTongTien(veList);
        lblTongTien.setText(formatCurrency(tongTien));
        capNhatTienThua();
    }

    private void capNhatTongTienGiam() {
        double tongTienGiam = tinhTongTienGiam(veList);
        tienGiamTextField.setText(formatCurrency(tongTienGiam));
    }

    private void capNhatTienThua() {
        try {
            double tongTien = parseCurrency(lblTongTien.getText());
            double tienKhachDua = tienKhachDuaTextField.getText().isEmpty() ? 0 : parseCurrency(tienKhachDuaTextField.getText());
            lblTienThua.setText(formatCurrency(tienKhachDua - tongTien));
        } catch (NumberFormatException e) {
            lblTienThua.setText("0");
        }
    }

    private void xoaVe() {
        Ve selectedVe = tableViewBanVe.getSelectionModel().getSelectedItem();
        if (selectedVe != null && confirm("Bạn có chắc chắn muốn xóa vé này?")) {
            // Lấy ghế và toa tàu
            Ghe ghe = selectedVe.getGheByMaGhe();
            ToaTau toaTau = ghe != null ? ghe.getToatauByMaTt() : null;
            LichTrinh lichTrinh = selectedVe.getLichtrinhByMaLt();

            // Xóa vé khỏi veList
            veList.remove(selectedVe);

            // Cập nhật gheDaChonMap
            if (ghe != null && toaTau != null) {
                boolean isDi = lichTrinh.getGaKhoiHanh().equals(gaDi);
                Map<ToaTau, Set<Ghe>> gheDaChonMap = isDi ? gheDaChonMapDi : gheDaChonMapVe;
                Set<Ghe> gheSet = gheDaChonMap.get(toaTau);
                if (gheSet != null) {
                    gheSet.remove(ghe);
                    if (gheSet.isEmpty()) {
                        gheDaChonMap.remove(toaTau);
                    }
                }
            }

            // Mở khóa ghế trên server dựa trên maLt
            if (ghe != null && lichTrinh != null) {
                Task<Void> unlockTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        lichTrinhService.unlockSeat(ghe.getMaGhe(), lichTrinh.getMaLt());
                        return null;
                    }
                };
                unlockTask.setOnFailed(event -> {
                    Platform.runLater(() -> {
                        showAlert("Lỗi", "Không thể mở khóa ghế: " + unlockTask.getException().getMessage(), Alert.AlertType.ERROR);
                    });
                });
                new Thread(unlockTask).start();
            }

            // Cập nhật giao diện
            tableViewBanVe.refresh();
            totalRecords = veList.size();
            if (currentPage > getTotalPages()) {
                currentPage = Math.toIntExact(Math.max(1, getTotalPages()));
            }
            updateDisplayedVeList();
            updatePageInfo();
        } else if (selectedVe == null) {
            showAlert("Thông báo", "Vui lòng chọn một vé để xóa.", Alert.AlertType.INFORMATION);
        }
    }

    private void xoaTatCaVe() {
        if (!veList.isEmpty() && confirm("Bạn có chắc chắn muốn xóa tất cả vé?")) {
            // Mở khóa tất cả ghế trên server dựa trên maLt
            for (Ve ve : veList) {
                Ghe ghe = ve.getGheByMaGhe();
                LichTrinh lichTrinh = ve.getLichtrinhByMaLt();
                if (ghe != null && lichTrinh != null) {
                    Task<Void> unlockTask = new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            lichTrinhService.unlockSeat(ghe.getMaGhe(), lichTrinh.getMaLt());
                            return null;
                        }
                    };
                    unlockTask.setOnFailed(event -> {
                        Platform.runLater(() -> {
                            showAlert("Lỗi", "Không thể mở khóa ghế: " + unlockTask.getException().getMessage(), Alert.AlertType.ERROR);
                        });
                    });
                    new Thread(unlockTask).start();
                }
            }

            // Xóa dữ liệu
            veList.clear();
            gheDaChonMapDi.clear();
            gheDaChonMapVe.clear();

            // Cập nhật giao diện
            totalRecords = 0;
            currentPage = 1;
            updateDisplayedVeList();
            updatePageInfo();
            tableViewBanVe.refresh();
            capNhatTongTien();
            capNhatTongTienGiam();
        } else if (veList.isEmpty()) {
            showAlert("Thông báo", "Không có vé nào để xóa.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void luuHoaDonVaVe() {
        if (veList.isEmpty()) {
            showAlert("Lỗi thanh toán", "Cần thêm vé", Alert.AlertType.ERROR);
            return;
        }

        if (!validateInput()) return;

        for (Ve ve : veList) {
            Ghe ghe = ve.getGheByMaGhe();
            if (ghe != null && ghe.getToatauByMaTt() == null) {
                showAlert("Lỗi", "Ghế " + ghe.getMaGhe() + " không có thông tin toa tàu.", Alert.AlertType.ERROR);
                return;
            }
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    double tienNhan = parseCurrency(tienKhachDuaTextField.getText());
                    HoaDon hoaDon = hoaDonService.luuHoaDonVaVe(new ArrayList<>(veList), cccdTextField.getText(), tienNhan, nhanVien);

                    // Tạo báo cáo vé
                    for (Ve ve : hoaDon.getVeSet()) {
                        JasperPrint veReport = hoaDonService.generateVeReport(ve.getMaVe());
                        Platform.runLater(() -> displayJasperReport(veReport));
                    }

                    // Tạo báo cáo hóa đơn
                    String recipientEmail = hoaDon.getHanhkhachByMaHk().getEmail();
                    JasperPrint hoaDonReport = hoaDonService.generateHoaDonReport(hoaDon.getMaHd(), recipientEmail);
                    Platform.runLater(() -> displayJasperReport(hoaDonReport));

                    // Mở khóa ghế dựa trên maLt
                    for (Ve ve : veList) {
                        Ghe ghe = ve.getGheByMaGhe();
                        LichTrinh lichTrinh = ve.getLichtrinhByMaLt();
                        if (ghe != null && lichTrinh != null) {
                            lichTrinhService.unlockSeat(ghe.getMaGhe(), lichTrinh.getMaLt());
                        }
                    }

                    // Update UI sau khi lưu xong
                    Platform.runLater(() -> {
                        showAlert("Thông báo", "Lưu hóa đơn và vé thành công", Alert.AlertType.INFORMATION);
                        veList.clear();
                        gheDaChonMapDi.clear();
                        gheDaChonMapVe.clear();
                        totalRecords = 0;
                        currentPage = 1;
                        updateDisplayedVeList();
                        updatePageInfo();
                        tableViewBanVe.refresh();
                        try {
                            FXMLLoader loader = MenuNhanVienController.instance.readyUI("BanVe/TimVe");
                            TimVeController controller = loader.getController();
                            controller.setNhanVien(nhanVien);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert("Lỗi", "Không thể load lại giao diện tìm vé: " + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Xảy ra lỗi", "Lưu thất bại! Xảy ra lỗi: " + e.getMessage(), Alert.AlertType.ERROR));
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    private void displayJasperReport(JasperPrint jasperPrint) {
        Platform.runLater(() -> {
            try {
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setTitle(jasperPrint.getName());
                viewer.setVisible(true);
            } catch (Exception e) {
                showAlert("Lỗi", "Không thể hiển thị báo cáo: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private boolean validateInput() {
        for (int i = 0; i < veList.size(); i++) {
            Ve ve = veList.get(i);
            if (ve.getHoTen() == null || ve.getHoTen().trim().isEmpty()) {
                showValidationError(i, colTenKH, "Vui lòng nhập tên khách hàng.");
                return false;
            }
            if (ve.getLoaiKh() == null) {
                showValidationError(i, colThongTinKH, "Vui lòng chọn loại hành khách.");
                return false;
            }
            if (ve.getLoaiKh() == LoaiHanhKhach.NGUOI_LON || ve.getLoaiKh() == LoaiHanhKhach.HOC_SINH_SINH_VIEN) {
                if (ve.getCccd() == null || ve.getCccd().trim().isEmpty() || !ve.getCccd().matches("[0-9]{12}")) {
                    showValidationError(i, colThongTinKH, "Vui lòng nhập CCCD phù hợp.");
                    return false;
                }
            } else if (ve.getNgaySinhTreEm() == null) {
                showValidationError(i, colThongTinKH, "Vui lòng nhập ngày sinh.");
                return false;
            } else {
                LocalDate ngaySinh = ve.getNgaySinhTreEm().toLocalDate();
                Period p = Period.between(ngaySinh, LocalDate.now());
                int tuoi = p.getYears();
                if (ve.getLoaiKh() == LoaiHanhKhach.TRE_EM_DUOI_6 && tuoi >= 6) {
                    showValidationError(i, colThongTinKH, "Trẻ em dưới 6 tuổi phải có tuổi nhỏ hơn 6.");
                    return false;
                }
                if (ve.getLoaiKh() == LoaiHanhKhach.TRE_EM_6_DEN_10 && (tuoi < 6 || tuoi > 14)) {
                    showValidationError(i, colThongTinKH, "Trẻ em từ 6 đến 10 tuổi phải có tuổi từ 6 đến 10.");
                    return false;
                }
                if (ve.getLoaiKh() == LoaiHanhKhach.NGUOI_CAO_TUOI) {
                    if (ve.getCccd() == null || ve.getCccd().trim().isEmpty() || !ve.getCccd().matches("[0-9]{12}")) {
                        showValidationError(i, colThongTinKH, "Vui lòng nhập CCCD phù hợp.");
                        return false;
                    }
                    if (tuoi < 60) {
                        showValidationError(i, colThongTinKH, "Người cao tuổi phải từ 60 tuổi trở lên.");
                        return false;
                    }
                }
            }
        }

        if (cccdTextField.getText() == null || cccdTextField.getText().trim().isEmpty()) {
            showPopOver(cccdTextField, "CCCD không được để trống!");
            return false;
        }

        String tienKhachDuaText = tienKhachDuaTextField.getText();
        if (tienKhachDuaText == null || tienKhachDuaText.trim().isEmpty()) {
            showPopOver(tienKhachDuaTextField, "Tiền khách đưa không được để trống!");
            return false;
        }
        try {
            double tongTien = parseCurrency(lblTongTien.getText());
            double tienKhachDua = parseCurrency(tienKhachDuaText);
            if (tienKhachDua <= 0 || tienKhachDua < tongTien) {
                showPopOver(tienKhachDuaTextField, "Tiền khách đưa phải lớn hơn 0 và tổng tiền!");
                return false;
            }
        } catch (NumberFormatException e) {
            showPopOver(tienKhachDuaTextField, "Tiền khách đưa phải là số hợp lệ!");
            return false;
        }
        return true;
    }

    private void showPopOver(Node targetNode, String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        PopOver popOver = new PopOver(errorLabel);
        popOver.setDetachable(false);
        popOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        popOver.show(targetNode);
        targetNode.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) popOver.hide();
        });
    }

    private void showValidationError(int rowIndex, TableColumn<Ve, ?> column, String message) {
        int globalIndex = rowIndex;
        int displayIndex = globalIndex - (currentPage - 1) * rowsPerPage;
        if (displayIndex >= 0 && displayIndex < displayedVeList.size()) {
            tableViewBanVe.scrollTo(displayIndex);
            tableViewBanVe.getSelectionModel().select(displayIndex, column);

            TableCell<Ve, ?> cell = getCell(displayIndex, column);
            if (cell != null && cell.getGraphic() != null) {
                Node inputNode = null;
                if (column == colTenKH) {
                    inputNode = cell.getGraphic();
                } else if (column == colThongTinKH) {
                    Ve ve = veList.get(globalIndex);
                    if (ve.getLoaiKh() == null) {
                        inputNode = cell.getGraphic().lookup("#comboBoxLoaiKH");
                    } else if (ve.getLoaiKh() == LoaiHanhKhach.NGUOI_LON || ve.getLoaiKh() == LoaiHanhKhach.NGUOI_CAO_TUOI || ve.getLoaiKh() == LoaiHanhKhach.HOC_SINH_SINH_VIEN) {
                        inputNode = cell.getGraphic().lookup("#textFieldCCCD");
                    } else {
                        inputNode = cell.getGraphic().lookup("#datePickerNgaySinh");
                    }
                }
                if (inputNode != null) {
                    PopOver popOver = new PopOver(new Label(message));
                    popOver.setDetachable(false);
                    popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
                    popOver.show(inputNode);
                    inputNode.requestFocus();
                }
            }
        }
    }

    private <T> TableCell<Ve, T> getCell(int rowIndex, TableColumn<Ve, T> column) {
        for (Node node : tableViewBanVe.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow<?> row && row.getIndex() == rowIndex) {
                for (Node cellNode : row.lookupAll(".table-cell")) {
                    if (cellNode instanceof TableCell<?, ?> cell && cell.getTableColumn() == column) {
                        return (TableCell<Ve, T>) cell;
                    }
                }
            }
        }
        return null;
    }

    @FXML
    private void chuyenSangHK() {
        FXMLLoader loader = MenuNhanVienController.instance.readyUI("BanVe/ThemHanhKhach");
        ThemHanhKhachController controller = loader.getController();
        controller.setVeList(new ArrayList<>(veList), gheDaChonMapDi, gheDaChonMapVe, gaDi, gaDen, ngayDiLD, ngayVeLD, isMotChieu);
        controller.setNhanVien(nhanVien);
    }

    public void setHanhKhach(HanhKhach hanhKhach) {
        if (hanhKhach != null) {
            cccdTextField.setText(hanhKhach.getCccd());
            hoTenTextField.setText(hanhKhach.getHoTenDem() + " " + hanhKhach.getTen());
            emailTextField.setText(hanhKhach.getEmail());
            btnThem1.setDisable(true);
        }
    }

    @FXML
    private void chuyenSangTimVe() {
        FXMLLoader loader = MenuNhanVienController.instance.readyUI("BanVe/TimVe");
        TimVeController controller = loader.getController();
        controller.setTroLai(new ArrayList<>(veList), gheDaChonMapDi, gheDaChonMapVe, gaDi, gaDen, ngayDiLD, ngayVeLD, isMotChieu);
        controller.setNhanVien(nhanVien);
    }

    private void setupTienNhapAutoComplete() {
        tienKhachDuaTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isFormatting) return;
            String digits = newVal.replaceAll("[^0-9]", "");
            if (digits.length() > 9) digits = digits.substring(0, 9);
            if (digits.isEmpty()) return;
            try {
                long amount = Long.parseLong(digits);
                String formatted = formatCurrency(amount);
                if (!formatted.equals(newVal)) {
                    isFormatting = true;
                    tienKhachDuaTextField.setText(formatted);
                    tienKhachDuaTextField.positionCaret(tienKhachDuaTextField.getLength());
                    isFormatting = false;
                }
            } catch (NumberFormatException ignored) {
            }
        });

        AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(tienKhachDuaTextField, request -> {
            String userInput = request.getUserText().replaceAll("[^0-9]", "");
            if (userInput.isEmpty() || userInput.length() > 9) return Collections.emptyList();
            return generateSuggestions(userInput).stream().map(this::formatCurrency).collect(java.util.stream.Collectors.toList());
        });

        binding.setOnAutoCompleted(event -> {
            String selected = event.getCompletion();
            String digits = selected.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                long amount = Long.parseLong(digits);
                isFormatting = true;
                tienKhachDuaTextField.setText(formatCurrency(amount));
                tienKhachDuaTextField.positionCaret(tienKhachDuaTextField.getLength());
                isFormatting = false;
            }
        });
    }

    private List<Long> generateSuggestions(String digits) {
        List<Long> suggestions = new ArrayList<>();
        long amount = Long.parseLong(digits);
        int length = digits.length();

        if (length == 1) {
            suggestions.add(amount * 1000);
            suggestions.add(amount * 10000);
            suggestions.add(amount * 100000);
        } else if (length == 2) {
            suggestions.add(amount * 1000);
            suggestions.add(amount * 10000);
            suggestions.add(amount * 100000);
        } else if (length == 3) {
            suggestions.add(amount * 1000);
            suggestions.add(amount * 10000);
            suggestions.add(amount * 100000);
        } else if (length == 4) {
            suggestions.add(amount * 10);
            suggestions.add(amount * 100);
            suggestions.add(amount * 1000);
        } else if (length == 5) {
            suggestions.add(amount * 10);
            suggestions.add(amount * 100);
            suggestions.add(amount * 1000);
        } else if (length == 6) {
            suggestions.add(amount * 10);
            suggestions.add(amount * 100);
        } else if (length == 7) {
            suggestions.add(amount * 10);
        } else {
            suggestions.add(amount);
        }
        return suggestions;
    }

    @FXML
    private void Huy() {
        if (confirm("Bạn có chắc chắn muốn hủy? Mọi thay đổi chưa lưu sẽ bị mất.")) {
            for (Ve ve : veList) {
                Ghe ghe = ve.getGheByMaGhe();
                LichTrinh lichTrinh = ve.getLichtrinhByMaLt();
                if (ghe != null && lichTrinh != null) {
                    try {
                        lichTrinhService.unlockSeat(ghe.getMaGhe(), lichTrinh.getMaLt());
                    } catch (RemoteException e) {
                        showAlert("Lỗi", "Không thể mở khóa ghế: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            }
            veList.clear();
            gheDaChonMapDi.clear();
            gheDaChonMapVe.clear();
            totalRecords = 0;
            currentPage = 1;
            updateDisplayedVeList();
            updatePageInfo();
            MenuNhanVienController.instance.readyUI("BanVe/TimVe");
        }
    }

    @FXML
    private void handleCccdInput(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String cccd = cccdTextField.getText().trim();
            if (!cccd.isEmpty()) {
                hoTenTextField.clear();
                emailTextField.clear();
                btnThem1.setDisable(false);
            }
        }
    }

    private String formatCurrency(double amount) {
        return decimalFormat.format(amount);
    }

    private double parseCurrency(String currencyString) throws NumberFormatException {
        String sanitized = currencyString.replaceAll("[^0-9,]", "").replace(",", ".");
        return sanitized.isEmpty() ? 0.0 : Double.parseDouble(sanitized);
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void xacDinhLoaiVe(ObservableList<Ve> veList) {
        List<Ve> veListSort = new ArrayList<>(veList);
        veListSort.sort(Comparator
                .comparing((Ve v) -> v.getLichtrinhByMaLt() != null ? v.getLichtrinhByMaLt().getGaKhoiHanh() : "")
                .thenComparing(v -> v.getLichtrinhByMaLt() != null ? v.getLichtrinhByMaLt().getGaKetThuc() : ""));

        Set<Ve> veDaXet = new HashSet<>();
        for (int i = 0; i < veListSort.size(); i++) {
            Ve ve1 = veListSort.get(i);
            if (veDaXet.contains(ve1) || ve1.getLichtrinhByMaLt() == null) continue;

            for (int j = i + 1; j < veListSort.size(); j++) {
                Ve ve2 = veListSort.get(j);
                if (veDaXet.contains(ve2) || ve2.getLichtrinhByMaLt() == null) continue;

                String gaDiVe1 = ve1.getLichtrinhByMaLt().getGaKhoiHanh();
                String gaDenVe1 = ve1.getLichtrinhByMaLt().getGaKetThuc();
                String gaDiVe2 = ve2.getLichtrinhByMaLt().getGaKhoiHanh();
                String gaDenVe2 = ve2.getLichtrinhByMaLt().getGaKetThuc();

                if (gaDiVe1 != null && gaDenVe1 != null && gaDiVe2 != null && gaDenVe2 != null &&
                        gaDiVe1.equals(gaDenVe2) && gaDenVe1.equals(gaDiVe2)) {
                    ve1.setLoaiVe(LoaiVe.KHU_HOI);
                    ve2.setLoaiVe(LoaiVe.KHU_HOI);
                    veDaXet.add(ve1);
                    veDaXet.add(ve2);
                    break;
                }
            }
            if (!veDaXet.contains(ve1)) {
                ve1.setLoaiVe(LoaiVe.MOT_CHIEU);
            }
        }
    }

    private double tinhTongTienGiam(ObservableList<Ve> veList) {
        return veList.stream()
                .mapToDouble(ve -> {
                    if (ve.getGheByMaGhe() == null) return 0;
                    KhuyenMai khuyenMai = ve.getKhuyenmaiByMaKm();
                    return (khuyenMai != null) ? khuyenMai.getGiaTriKhuyenMai() * ve.getGheByMaGhe().getGiaGhe() / 100 : 0;
                })
                .sum();
    }

    private double tinhTongTien(ObservableList<Ve> veList) {
        return veList.stream()
                .mapToDouble(ve -> {
                    if (ve.getGheByMaGhe() == null) return 0;
                    double giaGhe = ve.getGheByMaGhe().getGiaGhe();
                    double thueSuat = ve.getThueSuatGtgt();
                    double thueAmount = giaGhe * thueSuat;
                    double giamGia = (ve.getKhuyenmaiByMaKm() != null) ? ve.getKhuyenmaiByMaKm().getGiaTriKhuyenMai() / 100 * giaGhe : 0;
                    return giaGhe + thueAmount - giamGia;
                })
                .sum();
    }

    private void capNhatGiaVe(Ve ve) {
        if (ve.getGheByMaGhe() == null) return;
        double giaGoc = ve.getGheByMaGhe().getGiaGhe();
        KhuyenMai khuyenMai = ve.getKhuyenmaiByMaKm();
        double thueSuat = ve.getThueSuatGtgt();
        double thueAmount = giaGoc * thueSuat;
        double giamGia = (khuyenMai != null) ? khuyenMai.getGiaTriKhuyenMai() * giaGoc / 100 : 0;
        double giaSauKhuyenMai = giaGoc - giamGia + thueAmount;
        ve.setGiaVe(giaSauKhuyenMai);
    }
}