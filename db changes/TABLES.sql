DROP TABLE audit_log_detail CASCADE CONSTRAINTS;
DROP TABLE audit_log CASCADE CONSTRAINTS;
DROP TABLE service_inventory CASCADE CONSTRAINTS;
DROP TABLE inventory CASCADE CONSTRAINTS;
DROP TABLE appointment CASCADE CONSTRAINTS;
DROP TABLE appointment_status CASCADE CONSTRAINTS;
DROP TABLE service CASCADE CONSTRAINTS;
DROP TABLE service_type CASCADE CONSTRAINTS;
DROP TABLE mechanic CASCADE CONSTRAINTS;
DROP TABLE vehicle CASCADE CONSTRAINTS;
DROP TABLE customer CASCADE CONSTRAINTS;
DROP TABLE users CASCADE CONSTRAINTS;
DROP TABLE role_permission CASCADE CONSTRAINTS;
DROP TABLE user_role CASCADE CONSTRAINTS;

CREATE TABLE USER_ROLE (
    ROLE_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    ROLE_NAME VARCHAR2(100) UNIQUE NOT NULL
);

CREATE TABLE ROLE_PERMISSION (
    PERMISSION_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    ROLE_ID NUMBER NOT NULL,
    TABLE_NAME VARCHAR2(50) NOT NULL,
    IS_READ_ONLY NUMBER(1) DEFAULT 1,  -- INSERT, UPDATE, DELETE, READ
    FOREIGN KEY (ROLE_ID) REFERENCES USER_ROLE(ROLE_ID)
);

CREATE TABLE USERS(
    USER_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    USERNAME VARCHAR2(255) UNIQUE NOT NULL,
    PASSWORD VARCHAR2(255) NOT NULL,
    ROLE_ID NUMBER NOT NULL,
    IS_ACTIVE NUMBER(1) DEFAULT 1,  -- 1 = ACTIVE, 0 = INACTIVE
    FOREIGN KEY (ROLE_ID) REFERENCES USER_ROLE(ROLE_ID)
);


CREATE TABLE CUSTOMER (
    CUSTOMER_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    NAME VARCHAR2(255) NOT NULL,
    PHONE VARCHAR2(20) NOT NULL,
    EMAIL VARCHAR2(255) UNIQUE,
    IS_ACTIVE NUMBER(1) DEFAULT 1
);

CREATE TABLE VEHICLE (
    VEHICLE_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    CUSTOMER_ID NUMBER NOT NULL,
    MODEL VARCHAR2(255) NOT NULL,
    YEAR NUMBER NOT NULL,
    LICENSE_PLATE VARCHAR2(50) UNIQUE NOT NULL,
    IS_ACTIVE NUMBER(1) DEFAULT 1,
    FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMER(CUSTOMER_ID)
);

CREATE INDEX IDX_VEHICLE_CUSTOMER_ID ON VEHICLE(CUSTOMER_ID);

CREATE TABLE MECHANIC (
    MECHANIC_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    NAME VARCHAR2(255) NOT NULL,
    PHONE VARCHAR2(20) NOT NULL,
    SPECIALIZATION VARCHAR2(255),
    IS_ACTIVE NUMBER(1) DEFAULT 1
);

CREATE TABLE SERVICE_TYPE (
    SERVICE_TYPE_ID VARCHAR2(10) PRIMARY KEY,
    TYPE_NAME VARCHAR2(255) UNIQUE NOT NULL
);

CREATE TABLE SERVICE (
    SERVICE_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    SERVICE_NAME VARCHAR2(255) NOT NULL,
    SERVICE_TYPE_ID VARCHAR2(10) NOT NULL,
    PRICE DECIMAL(10,2) NOT NULL,
    IS_ACTIVE NUMBER(1) DEFAULT 1,
    FOREIGN KEY (SERVICE_TYPE_ID) REFERENCES SERVICE_TYPE(SERVICE_TYPE_ID)
);

CREATE TABLE APPOINTMENT_STATUS (
    STATUS_ID CHAR(1) PRIMARY KEY,  -- 'S' (SCHEDULED), 'I' (IN PROGRESS), 'C' (COMPLETED), 'X' (CANCELED)
    STATUS_NAME VARCHAR2(255) UNIQUE NOT NULL
);

CREATE TABLE APPOINTMENT (
    APPOINTMENT_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    CUSTOMER_ID NUMBER NOT NULL,
    VEHICLE_ID NUMBER NOT NULL,
    SERVICE_ID NUMBER NOT NULL,
    MECHANIC_ID NUMBER,
    APPOINTMENT_DATE DATE NOT NULL,
    STATUS_ID CHAR(1) NOT NULL,
    IS_ACTIVE NUMBER(1) DEFAULT 1,
    FOREIGN KEY (CUSTOMER_ID) REFERENCES CUSTOMER(CUSTOMER_ID),
    FOREIGN KEY (VEHICLE_ID) REFERENCES VEHICLE(VEHICLE_ID),
    FOREIGN KEY (SERVICE_ID) REFERENCES SERVICE(SERVICE_ID),
    FOREIGN KEY (MECHANIC_ID) REFERENCES MECHANIC(MECHANIC_ID),
    FOREIGN KEY (STATUS_ID) REFERENCES APPOINTMENT_STATUS(STATUS_ID)
);

CREATE INDEX IDX_APPOINTMENT_CUSTOMER_ID ON APPOINTMENT(CUSTOMER_ID);
CREATE INDEX IDX_APPOINTMENT_VEHICLE_ID ON APPOINTMENT(VEHICLE_ID);
CREATE INDEX IDX_APPOINTMENT_SERVICE_ID ON APPOINTMENT(SERVICE_ID);
CREATE INDEX IDX_APPOINTMENT_MECHANIC_ID ON APPOINTMENT(MECHANIC_ID);
CREATE INDEX IDX_APPOINTMENT_STATUS_ID ON APPOINTMENT(STATUS_ID);
CREATE INDEX IDX_APPOINTMENT_CUSTOMER_DATE ON APPOINTMENT(CUSTOMER_ID, APPOINTMENT_DATE);

CREATE TABLE INVENTORY (
    ITEM_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    ITEM_NAME VARCHAR2(255) NOT NULL,
    QUANTITY NUMBER DEFAULT 0 NOT NULL,
    PRICE DECIMAL(10,2) NOT NULL,
    IS_ACTIVE NUMBER(1) DEFAULT 1
);

CREATE TABLE SERVICE_INVENTORY (
    SERVICE_ID NUMBER NOT NULL,
    ITEM_ID NUMBER NOT NULL,
    QUANTITY_REQUIRED NUMBER NOT NULL,
    PRIMARY KEY (SERVICE_ID, ITEM_ID),
    FOREIGN KEY (SERVICE_ID) REFERENCES SERVICE(SERVICE_ID),
    FOREIGN KEY (ITEM_ID) REFERENCES INVENTORY(ITEM_ID)
);

CREATE TABLE AUDIT_LOG (
    AUDIT_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    TABLE_NAME VARCHAR2(255) NOT NULL,
    OPERATION_TYPE VARCHAR2(10) NOT NULL,  -- INSERT, UPDATE, DELETE
    PRIMARY_KEY_VALUE VARCHAR2(255) NOT NULL,
    PERFORMED_BY NUMBER NOT NULL,
    PERFORMED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (PERFORMED_BY) REFERENCES USERS(USER_ID)
);

CREATE INDEX IDX_AUDIT_TABLE ON AUDIT_LOG(TABLE_NAME);

CREATE TABLE AUDIT_LOG_DETAIL (
    DETAIL_ID NUMBER GENERATED AS IDENTITY PRIMARY KEY,
    AUDIT_ID NUMBER NOT NULL,
    COLUMN_NAME VARCHAR2(100) NOT NULL,
    OLD_VALUE VARCHAR2(255),
    NEW_VALUE VARCHAR2(255),
    FOREIGN KEY (AUDIT_ID) REFERENCES AUDIT_LOG(AUDIT_ID)
);

COMMIT;