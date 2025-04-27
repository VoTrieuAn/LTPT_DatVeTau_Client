package controller.BanVe;

import common.LoaiHanhKhach;
import config.TrainTicketApplication;
import controller.Menu.MenuNhanVienController;
import dao.KhuyenMaiDAO;
import entity.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import rmi.RMIServiceLocator;
import service.BanVeService;
import util.BarcodeUtil;
import util.EmailSenderUlti;
import util.HoadonCodeGeneratorUtil;
import util.QRCodeUtil;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;

public class BanVeController implements Initializable {

    @FXML private TextField cccdTextField;
    @FXML private TextField hoTenTextField;
    @FXML private Button btnThem1;
    @FXML private TextField emailTextField;
    @FXML private DatePicker ngayTaoDatePicker;
    @FXML private TextField tienGiamTextField;
    @FXML private ComboBox<?> phuongThucComboBox;
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
    @FXML private Label cccdErrorLabel;
    @FXML private Label hoTenErrorLabel;
    @FXML private Label tienKhachDuaErrorLabel;

    private ObservableList<Ve> veList = FXCollections.observableArrayList();
    private final Map<ToaTau, Set<Ghe>> gheDaChonMapDi = new HashMap<>();
    private final Map<ToaTau, Set<Ghe>> gheDaChonMapVe = new HashMap<>();
    private DecimalFormat decimalFormat;
    private BanVeService banVeService;
    private NhanVien nhanVien;
    private boolean isFormatting = false;
    private String gaDi;
    private String gaDen;
    private LocalDate ngayDiLD;
    private LocalDate ngayVeLD;
    private boolean isMotChieu;

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

        tableViewBanVe.setItems(this.veList);
        capNhatTongTienGiam();
        try {
            banVeService.xacDinhLoaiVe(veList);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        decimalFormat = new DecimalFormat("#,###", symbols);
        decimalFormat.setMaximumFractionDigits(0);

        banVeService = RMIServiceLocator.getBanVeService();
        setupTienNhapAutoComplete();

        setupTable(tableViewBanVe);
        tableViewBanVe.setItems(veList);
        tableViewBanVe.setEditable(true);

        maHoaDonTextField.setText(HoadonCodeGeneratorUtil.generateMaHoadon(
                TrainTicketApplication.getInstance().getEntityManagerFactory().createEntityManager()));
        ngayTaoDatePicker.setValue(LocalDate.now());
        ngayTaoDatePicker.addEventFilter(MouseEvent.ANY, event -> event.consume());

        setupListeners();
    }

    private void setupListeners() {
        tableViewBanVe.getItems().addListener((ListChangeListener<Ve>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasUpdated()) {
                    capNhatTongTien();
                    capNhatTongTienGiam();
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
            // Thực hiện logic khi text thay đổi
            String cccd = newValue;
            if (!cccd.isEmpty()) {
                HanhKhach khachHang = null;
                try {
                    khachHang = banVeService.timKiemByCccd(cccd);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                if (khachHang != null) {
                    // Nếu khách hàng tồn tại, hiển thị thông tin và vô hiệu hóa nút btnThem1
                    hoTenTextField.setText(khachHang.getHoTenDem() + " " + khachHang.getTen());
                    emailTextField.setText(khachHang.getEmail());
                    btnThem1.setDisable(true); // Vô hiệu hóa nút
                } else {
                    // Nếu khách hàng không tồn tại, xóa thông tin và kích hoạt nút btnThem1
                    hoTenTextField.setText("");
                    emailTextField.setText("");
                    btnThem1.setDisable(false); // Bật lại nút
                }
            } else {
                // Khi không có giá trị trong TextField, xóa thông tin và bật lại nút btnThem1
                hoTenTextField.setText("");
                emailTextField.setText("");
                btnThem1.setDisable(false); // Bật lại nút
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
    }

    @FXML
    void controller(ActionEvent event) {
        if (event.getSource() == btnTraVaIn) {
            luuHoaDonVaVe();
        }
    }

    private void setupTable(TableView<Ve> tableView) {
        colTT.setCellFactory(column -> new TableCell<Ve, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colTT.setSortable(false);

        colHanhTrinh.setCellValueFactory(cellData -> {
            LichTrinh lt = cellData.getValue().getLichtrinhByMaLt();
            String hanhTrinh = lt != null ? lt.getGaKhoiHanh() + " - " + lt.getGaKetThuc() + "\n" + lt.getTauByMaTau().getMaTau() : "";
            return new SimpleStringProperty(hanhTrinh);
        });

        // 3. Cột Tên KH
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
                textField.setId("textFieldTenKH"); // Thiết lập ID
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
                ve.setHoTen(newValue.trim());
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
                    ve.setLoaiKh(selected);

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

                    KhuyenMai khuyenMaiTotNhat = null;
                    try {
                        khuyenMaiTotNhat = banVeService.layKhuyenMaiTotNhat(ve);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    ve.setKhuyenmaiByMaKm(khuyenMaiTotNhat);
                    try {
                        banVeService.capNhatGiaVe(ve);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
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
                    if (ve != null) ve.setCccd(newVal);
                });

                datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (isUpdatingDatePicker) return;
                    Ve ve = getTableView().getItems().get(getIndex());
                    if (ve != null) {
                        if (newVal != null) {
                            ve.setNgaySinhTreEm(Date.valueOf(newVal));
                            KhuyenMai khuyenMaiTotNhat = null;
                            try {
                                khuyenMaiTotNhat = banVeService.layKhuyenMaiTotNhat(ve);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                            ve.setKhuyenmaiByMaKm(khuyenMaiTotNhat);
                            try {
                                banVeService.capNhatGiaVe(ve);
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                            capNhatTongTien();
                            tableViewBanVe.refresh();
                        } else {
                            ve.setNgaySinhTreEm(null);
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
            private final ComboBox<KhuyenMai> comboBox = new ComboBox<>();
            private final Label lblKhuyenMaiValue = new Label("0");

            {
                comboBox.setCellFactory(cb -> new ListCell<>() {
                    @Override
                    protected void updateItem(KhuyenMai item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getTenKhuyenMai());
                    }
                });
                comboBox.setButtonCell(new ListCell<>() {
                    @Override
                    protected void updateItem(KhuyenMai item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.getTenKhuyenMai());
                    }
                });

                comboBox.setOnAction(event -> {
                    Ve ve = getTableView().getItems().get(getIndex());
                    KhuyenMai khuyenMaiChon = comboBox.getValue();
                    ve.setKhuyenmaiByMaKm(khuyenMaiChon);
                    if (khuyenMaiChon != null) {
                        double discount = khuyenMaiChon.getGiaTriKhuyenMai() * ve.getGheByMaGhe().getGiaGhe() / 100;
                        lblKhuyenMaiValue.setText(formatCurrency(discount));
                    } else {
                        lblKhuyenMaiValue.setText("0");
                    }
                    try {
                        banVeService.capNhatGiaVe(ve);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    capNhatTongTien();
                    capNhatTongTienGiam();
                    tableViewBanVe.refresh();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Ve ve = getTableView().getItems().get(getIndex());
                    LocalDate ngayMuaVe = LocalDate.now();
                    LocalDate ngayKhoiHanh = ve.getLichtrinhByMaLt().getNgayKhoiHanh().toLocalDate();

                    List<KhuyenMai> danhSachKhuyenMai = TrainTicketApplication.getInstance()
                            .getDatabaseContext()
                            .newEntityDAO(KhuyenMaiDAO.class)
                            .getDanhSach("KhuyenMai.findAll", KhuyenMai.class);

                    List<KhuyenMai> danhSachKhuyenMaiApDung = danhSachKhuyenMai.stream()
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
                            .collect(java.util.stream.Collectors.toList());

                    comboBox.getItems().setAll(danhSachKhuyenMaiApDung);

                    if (ve.getKhuyenmaiByMaKm() == null && !danhSachKhuyenMaiApDung.isEmpty()) {
                        KhuyenMai khuyenMaiTotNhat = null;
                        try {
                            khuyenMaiTotNhat = banVeService.layKhuyenMaiTotNhat(ve);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                        ve.setKhuyenmaiByMaKm(khuyenMaiTotNhat);
                        comboBox.setValue(khuyenMaiTotNhat);
                        lblKhuyenMaiValue.setText(formatCurrency(khuyenMaiTotNhat.getGiaTriKhuyenMai() * ve.getGheByMaGhe().getGiaGhe() / 100));
                        try {
                            banVeService.capNhatGiaVe(ve);
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                        capNhatTongTien();
                        capNhatTongTienGiam();
                        tableViewBanVe.refresh();
                    } else {
                        comboBox.setValue(ve.getKhuyenmaiByMaKm());
                        KhuyenMai selectedKhuyenMai = ve.getKhuyenmaiByMaKm();
                        lblKhuyenMaiValue.setText(selectedKhuyenMai != null
                                ? formatCurrency(selectedKhuyenMai.getGiaTriKhuyenMai() * ve.getGheByMaGhe().getGiaGhe() / 100)
                                : "0");
                    }

                    setGraphic(new VBox(comboBox, lblKhuyenMaiValue));
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
        double tongTien = 0;
        try {
            tongTien = banVeService.tinhTongTien(veList);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        lblTongTien.setText(formatCurrency(tongTien));
        capNhatTienThua();
    }

    private void capNhatTongTienGiam() {
        double tongTienGiam = 0;
        try {
            tongTienGiam = banVeService.tinhTongTienGiam(veList);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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
            veList.remove(selectedVe);
            gheDaChonMapDi.getOrDefault(selectedVe.getGheByMaGhe().getToatauByMaTt(), new HashSet<>()).remove(selectedVe.getGheByMaGhe());
            gheDaChonMapVe.getOrDefault(selectedVe.getGheByMaGhe().getToatauByMaTt(), new HashSet<>()).remove(selectedVe.getGheByMaGhe());
        } else if (selectedVe == null) {
            showAlert("Thông báo", "Vui lòng chọn một vé để xóa.", Alert.AlertType.INFORMATION);
        }
    }

    private void xoaTatCaVe() {
        if (!veList.isEmpty() && confirm("Bạn có chắc chắn muốn xóa tất cả vé?")) {
            veList.clear();
            gheDaChonMapDi.clear();
            gheDaChonMapVe.clear();
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

        try {
            double tienNhan = parseCurrency(tienKhachDuaTextField.getText());
            HoaDon hoaDon = banVeService.luuHoaDonVaVe(veList, cccdTextField.getText(), tienNhan, nhanVien);

            for (Ve ve : hoaDon.getVeSet()) {
                generateReport(ve);
            }
            generateReport(hoaDon);

            showAlert("Thông báo", "Lưu hóa đơn và vé thành công", Alert.AlertType.INFORMATION);
            veList.clear();
            tableViewBanVe.refresh();
            FXMLLoader loader = MenuNhanVienController.instance.readyUI("BanVe/TimVe");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Xảy ra lỗi", "Lưu thất bại! Xảy ra lỗi: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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

    public void generateReport(Ve ve) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/trainticket", "root", "123456")) {
            HashMap<String, Object> para = new HashMap<>();
            para.put("maVe", ve.getMaVe());
            BufferedImage qrCodeImage = QRCodeUtil.generateQRCodeImage(ve.getMaVe(), 94, 90);
            para.put("qrCodeImage", qrCodeImage);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    JasperCompileManager.compileReport(getClass().getResourceAsStream("/view/report/Blank_Letter.jrxml")),
                    para, connection);
            JasperViewer.viewReport(jasperPrint, false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Xảy ra lỗi khi tạo báo cáo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void generateReport(HoaDon hoaDon) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/trainticket", "root", "123456")) {
            HashMap<String, Object> para = new HashMap<>();
            para.put("maHD", hoaDon.getMaHd());
            BufferedImage qrCodeImage = BarcodeUtil.generateBarcodeImage(hoaDon.getMaHd(), 154, 50);
            para.put("qrCodeImage", qrCodeImage);

            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    JasperCompileManager.compileReport(getClass().getResourceAsStream("/view/report/Invoice.jrxml")),
                    para, connection);

            String recipient = hoaDon.getHanhkhachByMaHk().getEmail();
            EmailSenderUlti.sendJasperReportAsPDF(recipient, "Hóa đơn của bạn", "Xin chào, đây là hóa đơn của bạn.", jasperPrint);
            JasperViewer.viewReport(jasperPrint, false);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Xảy ra lỗi khi tạo báo cáo: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
        tableViewBanVe.scrollTo(rowIndex);
        tableViewBanVe.getSelectionModel().select(rowIndex, column);

        TableCell<Ve, ?> cell = getCell(rowIndex, column);
        if (cell != null && cell.getGraphic() != null) {
            Node inputNode = null;
            if (column == colTenKH) {
                inputNode = cell.getGraphic();
            } else if (column == colThongTinKH) {
                Ve ve = veList.get(rowIndex);
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
            return generateSuggestions(userInput).stream().map(this::formatCurrency).collect(Collectors.toList());
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
            MenuNhanVienController.instance.readyUI("BanVe/TimVe");
        }
    }

    @FXML
    private void handleCccdInput(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String cccd = cccdTextField.getText().trim();
            if (!cccd.isEmpty()) {
                // Gọi service để kiểm tra hành khách nếu cần
                // Hiện tại giữ logic giao diện đơn giản
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
}