module ca.humber.vehicle_maintenance_service {
	requires javafx.controls;
	requires transitive javafx.graphics;
	requires org.hibernate.orm.core;
	requires jakarta.persistence;
	requires java.sql;
	requires java.naming;

	exports ca.humber.main;
	exports ca.humber.dao;
	exports ca.humber.model;
	
	opens ca.humber.model to org.hibernate.orm.core;
}
