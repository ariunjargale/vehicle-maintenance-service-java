module ca.humber {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.sql;
    requires java.naming;

    opens ca.humber to javafx.fxml;
    opens ca.humber.model to org.hibernate.orm.core;
    exports ca.humber;
    exports ca.humber.controller;
    opens ca.humber.controller to javafx.fxml;
}
