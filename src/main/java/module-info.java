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
    requires java.desktop;

    exports ca.humber;
    exports ca.humber.controller;
    exports ca.humber.model;
    exports ca.humber.util;
    exports ca.humber.service;

    opens ca.humber.controller to javafx.fxml;
    opens ca.humber to javafx.fxml;
    opens ca.humber.model to javafx.base, javafx.fxml, org.hibernate.orm.core;
}
