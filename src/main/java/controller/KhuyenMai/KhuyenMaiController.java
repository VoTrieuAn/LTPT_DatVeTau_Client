package controller.KhuyenMai;

import controller.Menu.MenuController;
import entity.KhuyenMai;
import entity.KhuyenMaiKhachHang;
import entity.KhuyenMaiNgay;
import entity.KhuyenMaiVe;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import rmi.RMIServiceLocator;
import service.KhuyenMaiService;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class KhuyenMaiController implements Initializable {

    @FXML
    private AnchorPane anchorPaneMain;
    @FXML
    private Button btnDangHoatDong;
    @FXML
    private Button btnNgungHoatDong;
    @FXML
    private Button btnTatCa;
    @FXML
    private Button btnXoaTatCa;
    @FXML
    private TableView<KhuyenMai> tableKhuyenMai;
    @FXML
    private TableColumn<KhuyenMai, String> trangThaiColumn;
    @FXML
    private HBox hBoxPage;

    private ObservableList<KhuyenMai> danhSachKhuyenMai = FXCollections.observableArrayList();
    private String status = "all";
    private final KhuyenMaiService khuyenMaiService = RMIServiceLocator.getKhuyenMaiService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableViewKhuyenMai(tableKhuyenMai);
        loadData();

        btnNgungHoatDong.setOnAction(event -> {
            KhuyenMai selectedItem = tableKhuyenMai.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                showAlert("Thông báo", "Vui lòng chọn một dòng!", Alert.AlertType.WARNING);
                return;
            }
            boolean currentState = selectedItem.isTrangThai();
            selectedItem.setTrangThai(!currentState);
            try {
                if (khuyenMaiService.updateKhuyenMai(selectedItem)) {
                    tableKhuyenMai.refresh();
                } else {
                    showAlert("Lỗi", "Không thể thay đổi trạng thái!", Alert.AlertType.ERROR);
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        btnDangHoatDong.setOnAction(event -> {
            KhuyenMai selectedItem = tableKhuyenMai.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                showAlert("Thông báo", "Vui lòng chọn một dòng!", Alert.AlertType.WARNING);
                return;
            }
            TableColumn<KhuyenMai, Double> giaTriKMCol = null;
            for (TableColumn<KhuyenMai, ?> col : tableKhuyenMai.getColumns()) {
                if (col.getText().equals("Giá Trị KM %")) {
                    giaTriKMCol = (TableColumn<KhuyenMai, Double>) col;
                    break;
                }
            }
            if (giaTriKMCol == null) {
                showAlert("Lỗi", "Không tìm thấy cột Giá Trị KM!", Alert.AlertType.ERROR);
                return;
            }
            int selectedIndex = tableKhuyenMai.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                tableKhuyenMai.edit(selectedIndex, giaTriKMCol);
            }
        });
    }

    @FXML
    private void controller(ActionEvent event) throws IOException {
        Object source = event.getSource();
        if (source == btnXoaTatCa) {
            xoa();
        } else if (source == btnTatCa) {
            status = "all";
            loadData(khuyenMaiService.getKhuyenMaiByStatus(status));
        } else if (source == btnDangHoatDong) {
            status = "active";
            loadData(khuyenMaiService.getKhuyenMaiByStatus(status));
        } else if (source == btnNgungHoatDong) {
            status = "inactive";
            loadData(khuyenMaiService.getKhuyenMaiByStatus(status));
        }
        hBoxPage.setVisible(true);
    }

    @FXML
    void mouseClicked(MouseEvent event) throws IOException {
        Object source = event.getSource();
        if (source == anchorPaneMain) {
            huyChonDong();
        }
    }

    @FXML
    void keyPressed(KeyEvent event) {
        Object source = event.getSource();
        if (source == anchorPaneMain) {
            if (event.getCode() == KeyCode.ESCAPE) {
                huyChonDong();
            }
        }
    }

    public void showThemKhuyenMai() {
        MenuController.instance.readyUI("KhuyenMai/ThemKhuyenMai");
    }

    private void loadData() {
        List<KhuyenMai> khuyenMais = null;
        try {
            khuyenMais = khuyenMaiService.getDanhSachKhuyenMai();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        danhSachKhuyenMai.addAll(khuyenMais);
        tableKhuyenMai.setItems(danhSachKhuyenMai);
    }

    private void loadData(List<KhuyenMai> khuyenMais) {
        danhSachKhuyenMai.clear();
        danhSachKhuyenMai.addAll(khuyenMais);
        tableKhuyenMai.setItems(danhSachKhuyenMai);
    }

    private void setupTableViewKhuyenMai(TableView<KhuyenMai> tableView) {
        tableView.setEditable(true);

        TableColumn<KhuyenMai, String> maKMCol = new TableColumn<>("Mã KM");
        maKMCol.setCellValueFactory(new PropertyValueFactory<>("maKM"));
        maKMCol.setEditable(false);

        TableColumn<KhuyenMai, String> tenKMCol = new TableColumn<>("Tên KM");
        tenKMCol.setCellValueFactory(new PropertyValueFactory<>("tenKhuyenMai"));
        tenKMCol.setEditable(false);

        TableColumn<KhuyenMai, String> loaiKMCol = new TableColumn<>("Loại KM");
        loaiKMCol.setCellValueFactory(cellData -> {
            KhuyenMai khuyenMai = cellData.getValue();
            String loaiKMStr;
            if (khuyenMai instanceof KhuyenMaiKhachHang) {
                loaiKMStr = "Đối Tượng Hành Khách";
            } else if (khuyenMai instanceof KhuyenMaiNgay) {
                loaiKMStr = "Trước Khởi Hành";
            } else if (khuyenMai instanceof KhuyenMaiVe) {
                loaiKMStr = "Loại Vé";
            } else {
                loaiKMStr = "Không Xác Định";
            }
            return new SimpleStringProperty(loaiKMStr);
        });
        loaiKMCol.setEditable(false);

// Cột Giá Trị Khuyến Mãi (giaTriKhuyenMai)
        TableColumn<KhuyenMai, Double> giaTriKMCol = new TableColumn<>("Giá Trị KM %");
        giaTriKMCol.setCellValueFactory(new PropertyValueFactory<>("giaTriKhuyenMai"));

// Tùy chỉnh CellFactory để theo dõi focus
        giaTriKMCol.setCellFactory(column -> new TableCell<>() {
            private final TextField textField = new TextField();

            // Constructor không cần khai báo ở đây

            {
                // Lắng nghe sự kiện mất focus
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) { // Khi mất focus
                        commitEditFromFocus();
                    }
                });

                // Khi nhấn Enter
                textField.setOnAction(event -> commitEditFromFocus());
            }

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        textField.setText(item != null ? item.toString() : "");
                        setGraphic(textField);
                        setText(null);
                    } else {
                        setText(item != null ? item.toString() : "");
                        setGraphic(null);
                    }
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                Double item = getItem();
                textField.setText(item != null ? item.toString() : "");
                setGraphic(textField);
                setText(null);
                textField.requestFocus();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem() != null ? getItem().toString() : "");
                setGraphic(null);
            }

            @Override
            public void commitEdit(Double newValue) {
                super.commitEdit(newValue);

                // Cập nhật dữ liệu
                KhuyenMai km = getTableView().getItems().get(getIndex());
                km.setGiaTriKhuyenMai(newValue);

                // Gọi service cập nhật
                try {
                    if (khuyenMaiService.updateKhuyenMai(km)) {
                        // Cập nhật lại giao diện
                        getTableView().refresh(); // CẦN THÊM DÒNG NÀY
                    } else {
                        showAlert("Lỗi", "Cập nhật thất bại!", Alert.AlertType.ERROR);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }

            private void commitEditFromFocus() {
                try {
                    String input = textField.getText();
                    if (input != null && !input.isEmpty()) {
                        Double newValue = Double.parseDouble(input);
                        if (newValue >= 0 && newValue <= 100) { // Kiểm tra giá trị hợp lệ
                            commitEdit(newValue);
                        } else {
                            showAlert("Lỗi", "Giá trị khuyến mãi phải từ 0 đến 100!", Alert.AlertType.ERROR);
                            cancelEdit(); // Không lưu nếu không hợp lệ
                        }
                    }
                } catch (NumberFormatException e) {
                    showAlert("Lỗi", "Giá trị nhập không hợp lệ!", Alert.AlertType.ERROR);
                    cancelEdit(); // Không lưu nếu không hợp lệ
                }
            }
        });


        // Cột Trạng Thái (trangThaiColumn)
        TableColumn<KhuyenMai, String> trangThaiCol = new TableColumn<>("Trạng Thái");
        trangThaiCol.setCellValueFactory(cellData -> {
            boolean trangThai = cellData.getValue().isTrangThai();
            String trangThaiStr = trangThai ? "Hoạt động" : "Ngưng hoạt động";
            return new SimpleStringProperty(trangThaiStr);
        });
        trangThaiCol.setCellFactory(ComboBoxTableCell.forTableColumn("Hoạt động", "Ngưng hoạt động"));
        trangThaiCol.setOnEditCommit(event -> {
            KhuyenMai km = event.getRowValue();
            String newTrangThai = event.getNewValue();
            km.setTrangThai(newTrangThai.equals("Hoạt động"));
            try {
                khuyenMaiService.updateKhuyenMai(km);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });


        tableView.getColumns().clear();
        tableView.getColumns().addAll(maKMCol, tenKMCol, loaiKMCol, giaTriKMCol, trangThaiCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void huyChonDong() {
        tableKhuyenMai.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(content);
        alert.show();
    }

    private Optional<ButtonType> showAlertConfirm(String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(content);
        ButtonType buttonCo = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType buttonKhong = new ButtonType("Không", ButtonBar.ButtonData.NO);
        ButtonType buttonHuy = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonCo, buttonKhong, buttonHuy);
        return alert.showAndWait();
    }

    private void xoa() {
        KhuyenMai khuyenMai = tableKhuyenMai.getSelectionModel().getSelectedItem();
        if (khuyenMai != null) {
            Optional<ButtonType> buttonType = showAlertConfirm("Bạn có chắc chắn xóa?");
            if (buttonType.isPresent()) {
                if (buttonType.get().getButtonData() == ButtonBar.ButtonData.NO) {
                    return;
                }
                if (buttonType.get().getButtonData() == ButtonBar.ButtonData.YES) {
                    try {
                        if (khuyenMaiService.deleteKhuyenMai(khuyenMai)) {
                            showAlert("Thông báo", "Xóa thành công!", Alert.AlertType.INFORMATION);
                            danhSachKhuyenMai.removeIf(km -> !km.isTrangThai());
                            tableKhuyenMai.refresh();
                        } else {
                            showAlert("Thông báo", "Xóa thất bại", Alert.AlertType.WARNING);
                        }
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}