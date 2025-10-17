module com.acadia.acadiastudyplanner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.xerial.sqlitejdbc;
    requires java.net.http;

    opens com.acadia.acadiastudyplanner to javafx.fxml;
    opens com.acadia.acadiastudyplanner.controller to javafx.fxml;

    exports com.acadia.acadiastudyplanner;
    exports com.acadia.acadiastudyplanner.model;
}
