module ca.humber {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.sql;
    requires java.naming;
    requires ojdbc8;
    requires java.management;
    requires itextpdf; 
    requires itext; 

    exports ca.humber;
    exports ca.humber.model;
    exports ca.humber.controller;

    opens ca.humber.controller to javafx.fxml;
    opens ca.humber to javafx.fxml;
    opens ca.humber.model to javafx.base, javafx.fxml, org.hibernate.orm.core;
}
