module inc.cbi {
    
    requires javafx.fxml;
    requires javafx.controls;
    requires com.opencsv;
    requires org.apache.poi.poi;

    //requires transitive javafx.graphics;

    opens inc.cbi to javafx.fxml;
    //opens inc.cbi to javafx.graphics;

    exports inc.cbi;
}
