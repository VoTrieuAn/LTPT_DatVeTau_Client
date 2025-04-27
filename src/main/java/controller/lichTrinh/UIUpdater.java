package controller.lichTrinh;

import entity.LichTrinh;
import entity.Tau;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import util.maLTGenerator;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Quản lý cập nhật giao diện người dùng, bao gồm combo box, nhãn, và bảng lịch trình.
 */
public class UIUpdater {
    private final ComboBox<String> startStationComboBox;
    private final ComboBox<String> endStationComboBox;
    private final Label intermediateStationsLabel;
    private final TableView<LichTrinh> scheduleTable;

    /**
     * Khởi tạo UIUpdater với các thành phần giao diện.
     * @param startStationComboBox Combo box cho ga khởi hành.
     * @param endStationComboBox Combo box cho ga kết thúc.
     * @param intermediateStationsLabel Nhãn hiển thị danh sách ga trung gian.
     * @param scheduleTable Bảng hiển thị lịch trình.
     */
    public UIUpdater(ComboBox<String> startStationComboBox, ComboBox<String> endStationComboBox,
                     Label intermediateStationsLabel, TableView<LichTrinh> scheduleTable) {
        this.startStationComboBox = startStationComboBox;
        this.endStationComboBox = endStationComboBox;
        this.intermediateStationsLabel = intermediateStationsLabel;
        this.scheduleTable = scheduleTable;
    }

    /**
     * Cập nhật combo box ga.
     * @param comboBox Combo box cần cập nhật.
     * @param station Mã ga, hoặc null để đặt lại.
     */
    public void updateStationComboBox(ComboBox<String> comboBox, String station) {
        if (station == null) {
            comboBox.setValue("--Chọn ga--");
            return;
        }
        String fullName = getFullStationName(station);
        comboBox.setValue(fullName);
    }

    /**
     * Cập nhật nhãn danh sách ga trung gian.
     * @param stations Danh sách mã ga.
     */
    public void updateIntermediateStationsLabel(List<String> stations) {
        if (stations.size() <= 2) {
            intermediateStationsLabel.setText("");
            return;
        }
        List<String> intermediateFullNames = stations.subList(1, stations.size() - 1)
                .stream()
                .map(this::getFullStationName)
                .toList();
        intermediateStationsLabel.setText(String.join(", ", intermediateFullNames));
    }

    /**
     * Cập nhật bảng lịch trình.
     * @param schedules Danh sách lịch trình.
     * @param tableColumns Các cột của bảng.
     */
    public void updateScheduleTable(ObservableList<LichTrinh> schedules, TableColumn<LichTrinh, ?>... tableColumns) {
        scheduleTable.setItems(schedules);
        configureTableColumns(tableColumns);
    }

    /**
     * Cấu hình các cột của bảng lịch trình.
     * @param tableColumns Các cột cần cấu hình.
     */
    private void configureTableColumns(TableColumn<LichTrinh, ?>... tableColumns) {
        for (TableColumn<LichTrinh, ?> column : tableColumns) {
            String property = column.getId();
            switch (property) {
                case "maLTColumn":
                    ((TableColumn<LichTrinh, String>) column).setCellValueFactory(new PropertyValueFactory<>("maLt"));
                    break;
                case "gaKHColumn":
                    ((TableColumn<LichTrinh, String>) column).setCellValueFactory(new PropertyValueFactory<>("gaKhoiHanh"));
                    break;
                case "gaKTColumn":
                    ((TableColumn<LichTrinh, String>) column).setCellValueFactory(new PropertyValueFactory<>("gaKetThuc"));
                    break;
                case "ngayKHColumn":
                    ((TableColumn<LichTrinh, LocalDate>) column).setCellValueFactory(new PropertyValueFactory<>("ngayKhoiHanh"));
                    break;
                case "ngayKTColumn":
                    ((TableColumn<LichTrinh, LocalDate>) column).setCellValueFactory(new PropertyValueFactory<>("ngayDen"));
                    break;
                case "gioKHColumn":
                    ((TableColumn<LichTrinh, Time>) column).setCellValueFactory(new PropertyValueFactory<>("gioDi"));
                    break;
                case "gioKTColumn":
                    ((TableColumn<LichTrinh, Time>) column).setCellValueFactory(new PropertyValueFactory<>("gioDen"));
                    break;
                case "maTauColumn":
                    ((TableColumn<LichTrinh, String>) column).setCellValueFactory(new Callback<>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<LichTrinh, String> cellData) {
                            Tau tau = cellData.getValue().getTauByMaTau();
                            return new SimpleStringProperty(tau != null ? tau.getMaTau() : "");
                        }
                    });
                    break;
                case "trangThaiColumn":
                    ((TableColumn<LichTrinh, String>) column).setCellValueFactory(new Callback<>() {
                        @Override
                        public ObservableValue<String> call(TableColumn.CellDataFeatures<LichTrinh, String> cellData) {
                            int trangThai = cellData.getValue().getTrangThai();
                            String text = switch (trangThai) {
                                case -1 -> "Đã hủy";
                                case 0 -> "Chưa khởi hành";
                                case 1 -> "Đã khởi hành";
                                default -> "Không xác định";
                            };
                            return new SimpleStringProperty(text);
                        }
                    });
                    break;
            }
        }
    }

    /**
     * Lấy tên đầy đủ của ga từ mã ga, sử dụng ánh xạ từ maLTGenerator.
     * @param station Mã ga.
     * @return Tên đầy đủ của ga.
     */
    private String getFullStationName(String station) {
        return maLTGenerator.getStationMap().entrySet().stream()
                .filter(entry -> entry.getValue().equals(station))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ga không hợp lệ: " + station));
    }
}