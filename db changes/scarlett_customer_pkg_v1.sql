-- scarlett_customer_pkg_v1.sql
-- Created by Scarlett Jet
-- Date: 2025-03-23
-- Description: Package for Customer Management
-- Includes: customer_pkg (CRUD for customers only)

--Alter Table customer to include timestamp
ALTER TABLE CUSTOMER
ADD (
  CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED_AT TIMESTAMP
);


CREATE OR REPLACE PACKAGE customer_pkg AS
  PROCEDURE sp_create_customer(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2);
  PROCEDURE sp_update_customer(p_customer_id IN NUMBER, p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2);
  PROCEDURE sp_delete_customer(p_customer_id IN NUMBER);
END customer_pkg;
/




CREATE OR REPLACE PACKAGE BODY customer_pkg AS

  PROCEDURE sp_create_customer(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2) IS
  BEGIN
    INSERT INTO CUSTOMER (NAME, PHONE, EMAIL, IS_ACTIVE, CREATED_AT)
    VALUES (p_name, p_phone, p_email, 1, CURRENT_TIMESTAMP);
  END;

  PROCEDURE sp_update_customer(p_customer_id IN NUMBER, p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2) IS
  BEGIN
    UPDATE CUSTOMER
    SET NAME = p_name,
        PHONE = p_phone,
        EMAIL = p_email,
        UPDATED_AT = CURRENT_TIMESTAMP
    WHERE CUSTOMER_ID = p_customer_id AND IS_ACTIVE = 1;
  END;

  PROCEDURE sp_delete_customer(p_customer_id IN NUMBER) IS
  BEGIN
    UPDATE CUSTOMER
    SET IS_ACTIVE = 0,
        UPDATED_AT = CURRENT_TIMESTAMP
    WHERE CUSTOMER_ID = p_customer_id;
  END;

END customer_pkg;
/



--Adding test customer
BEGIN
  customer_pkg.sp_create_customer('Scarlett Jet', '4371234567', 'scarlett@example.com');
END;
/


--Update the same customer
BEGIN
  customer_pkg.sp_update_customer(1, 'Scarlett Blaze', '4370000000', 'blaze@example.com');
END;
/


-- Soft delete the customer
BEGIN
  customer_pkg.sp_delete_customer(1);
END;
/


---- View customers
SELECT * FROM CUSTOMER;


-- View active customers only
SELECT * FROM CUSTOMER WHERE IS_ACTIVE = 1;

