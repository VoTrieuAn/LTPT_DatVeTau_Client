package controller.lichTrinh;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class BanDoLTController {
    @FXML
    private Circle circle_DaNang;

    @FXML
    private Circle circle_DongHoi;

    @FXML
    private Circle circle_HaNoi;

    @FXML
    private Circle circle_HaiPhong;

    @FXML
    private Circle circle_Hue;

    @FXML
    private Circle circle_LaoCai;

    @FXML
    private Circle circle_NhaTrang;

    @FXML
    private Circle circle_PhanThiet;

    @FXML
    private Circle circle_QuyNhon;

    @FXML
    private Circle circle_SaiGon;

    @FXML
    private Circle circle_ThanhHoa;

    @FXML
    private Circle circle_Vinh;

    @FXML
    private Circle circle_VungTau;
    @FXML
    private Line line_DaNang_QuyNhon;

    @FXML
    private Line line_DongHoi_Hue;

    @FXML
    private Line line_HaNoi_HaiPhong;

    @FXML
    private Line line_HaNoi_ThanhHoa;

    @FXML
    private Line line_Hue_DaNang;

    @FXML
    private Line line_LaoCai_HaNoi;

    @FXML
    private Line line_NhaTrang_PhanThiet;

    @FXML
    private Line line_PhanThiet_SaiGon;

    @FXML
    private Line line_QuyNhon_NhaTrang;

    @FXML
    private Line line_SaiGon_VungTau;

    @FXML
    private Line line_ThanhHoa_Vinh;

    @FXML
    private Line line_Vinh_DongHoi;

    private Circle selectedCircle1, selectedCircle2;
    private Line selectedLine;
    public void onCircleClick(MouseEvent mouseEvent) {
        // Lấy đối tượng Circle từ sự kiện
        Circle circle = (Circle) mouseEvent.getSource();
        // Kiểm tra nếu người dùng nhấp lại vào ga đã chọn trước đó (bỏ chọn)
        if (circle == selectedCircle1) {
            // Bỏ chọn circle1
            selectedCircle1.setFill(Color.WHITE); // Đổi màu về màu ban đầu (màu trắng)
            selectedCircle1 = null; // Xóa circle1
            selectedLine = null; // Xóa line
            if (selectedCircle2 != null) {
                // Nếu có selectedCircle2, hãy giữ màu của nó và kiểm tra lại đường line
                selectedCircle2.setFill(Color.WHITE);
                selectedCircle2 = null;
            }
        } else if (circle == selectedCircle2) {
            // Bỏ chọn circle2
            selectedCircle2.setFill(Color.WHITE); // Đổi màu về màu ban đầu (màu trắng)
            selectedCircle2 = null; // Xóa circle2
            selectedLine.setStroke(Color.WHITE); // Đổi lại màu line về màu ban đầu
            selectedLine = null; // Xóa line
        } else {
            // Nếu chưa chọn đủ 2 ga, tiến hành chọn thêm
            if (selectedCircle1 == null) {
                selectedCircle1 = circle;
                circle.setFill(Color.RED); // Đổi màu circle1 thành đỏ
            } else if (selectedCircle2 == null) {
                selectedCircle2 = circle;
                circle.setFill(Color.RED); // Đổi màu circle2 thành đỏ

                // Cập nhật màu đường line giữa hai ga
                updateLine();
            }
        }
    }

    private void updateLine() {
        // Xác định các cặp ga liên quan
        if (selectedCircle1 == circle_SaiGon && selectedCircle2 == circle_VungTau) {
            selectedLine = line_SaiGon_VungTau;
        } else if (selectedCircle1 == circle_PhanThiet && selectedCircle2 == circle_SaiGon) {
            selectedLine = line_PhanThiet_SaiGon;
        } else if (selectedCircle1 == circle_NhaTrang && selectedCircle2 == circle_PhanThiet) {
            selectedLine = line_NhaTrang_PhanThiet;
        } else if (selectedCircle1 == circle_QuyNhon && selectedCircle2 == circle_DaNang) {
            selectedLine = line_DaNang_QuyNhon;
        } else if (selectedCircle1 == circle_DaNang && selectedCircle2 == circle_Hue) {
            selectedLine = line_Hue_DaNang;
        } else if (selectedCircle1 == circle_Hue && selectedCircle2 == circle_DongHoi) {
            selectedLine = line_DongHoi_Hue;
        } else if (selectedCircle1 == circle_Vinh && selectedCircle2 == circle_ThanhHoa) {
            selectedLine = line_ThanhHoa_Vinh;
        } else if (selectedCircle1 == circle_ThanhHoa && selectedCircle2 == circle_HaNoi) {
            selectedLine = line_HaNoi_ThanhHoa;
        } else if (selectedCircle1 == circle_LaoCai && selectedCircle2 == circle_HaNoi) {
            selectedLine = line_LaoCai_HaNoi;
        }

        // Đổi màu đường line nếu có
        if (selectedLine != null) {
            selectedLine.setStroke(Color.RED);
        }
    }
}
