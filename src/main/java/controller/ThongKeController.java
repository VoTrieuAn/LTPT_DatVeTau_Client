package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class ThongKeController {
    public ScrollPane tab_NhanVien;
    public ScrollPane tab_TongQuan;
    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize(){
        loadTabContent(tab_TongQuan, "/view/fxml/thongKe/TongQuan.fxml");
    }

    private void loadTabContent(ScrollPane tab, String fxmlFile){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node content = loader.load();
            tab.setContent(content);
            tab.setFitToHeight(true);
            tab.setFitToWidth(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
// 772034599555
// NV240045
// 1262wko3