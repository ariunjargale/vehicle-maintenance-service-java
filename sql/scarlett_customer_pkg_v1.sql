-- scarlett_customer_pkg_v1.sql
-- Created by Scarlett Jet
-- Date: 2025-03-23
-- Description: Package for Customer Management
-- Includes: customer_pkg (CRUD for customers only)



CREATE OR REPLACE PACKAGE customer_pkg AS
  PROCEDURE sp_create_customer(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2);
  PROCEDURE sp_update_customer(p_customer_id IN NUMBER, p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2);
  PROCEDURE sp_delete_customer(p_customer_id IN NUMBER); -- soft delete
END customer_pkg;
/


CREATE OR REPLACE PACKAGE BODY customer_pkg AS

PROCEDURE sp_create_customer(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2) IS
BEGIN
  INSERT INTO CUSTOMER (NAME, PHONE, EMAIL, IS_ACTIVE)
  VALUES (p_name, p_phone, p_email, 1);
END;


  PROCEDURE sp_update_customer(p_customer_id IN NUMBER, p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2) IS
  BEGIN
    UPDATE CUSTOMER
    SET NAME = p_name,
        PHONE = p_phone,
        EMAIL = p_email
    WHERE CUSTOMER_ID = p_customer_id AND IS_ACTIVE = 1;
  END;

  PROCEDURE sp_delete_customer(p_customer_id IN NUMBER) IS
  BEGIN
    UPDATE CUSTOMER
    SET IS_ACTIVE = 0
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


---- View active customers
SELECT * FROM CUSTOMER WHERE IS_ACTIVE = 1;

