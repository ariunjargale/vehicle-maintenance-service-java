-- customer_pkg_20250330.sql
-- Updated by Scarlett Jet
-- Date: 2025-03-30
-- Description: Enhanced Package for Customer Management
-- Includes: customer_pkg (CRUD for customers only) with improved error handling and row count returns

-- Check if timestamp columns already exist (to prevent errors if rerunning script)
DECLARE
  created_at_exists NUMBER;
  updated_at_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO created_at_exists
  FROM USER_TAB_COLUMNS
  WHERE TABLE_NAME = 'CUSTOMER' AND COLUMN_NAME = 'CREATED_AT';

  SELECT COUNT(*) INTO updated_at_exists
  FROM USER_TAB_COLUMNS
  WHERE TABLE_NAME = 'CUSTOMER' AND COLUMN_NAME = 'UPDATED_AT';

  IF created_at_exists = 0 AND updated_at_exists = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE CUSTOMER
    ADD (
      CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      UPDATED_AT TIMESTAMP
    )';
    DBMS_OUTPUT.PUT_LINE('Timestamp columns added to CUSTOMER table.');
  ELSE
    DBMS_OUTPUT.PUT_LINE('Timestamp columns already exist in CUSTOMER table.');
  END IF;
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error checking or adding timestamp columns: ' || SQLERRM);
END;
/

-- Updated package specification with OUT parameters
CREATE OR REPLACE PACKAGE customer_pkg AS
  PROCEDURE sp_create_customer(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2);

  PROCEDURE sp_update_customer(
    p_customer_id IN NUMBER,
    p_name IN VARCHAR2,
    p_phone IN VARCHAR2,
    p_email IN VARCHAR2,
    p_rows_updated OUT NUMBER
  );

  PROCEDURE sp_delete_customer(
    p_customer_id IN NUMBER,
    p_rows_deleted OUT NUMBER
  );
END customer_pkg;
/

-- Updated package body with validation and error handling
CREATE OR REPLACE PACKAGE BODY customer_pkg AS
  PROCEDURE sp_create_customer(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2) IS
  BEGIN
    -- Input validation
    IF p_name IS NULL OR p_name = '' THEN
      RAISE_APPLICATION_ERROR(-20001, 'Customer name is required');
    END IF;

    INSERT INTO CUSTOMER (NAME, PHONE, EMAIL, IS_ACTIVE, CREATED_AT)
    VALUES (p_name, p_phone, p_email, 1, CURRENT_TIMESTAMP);

    DBMS_OUTPUT.PUT_LINE('Customer created successfully.');

  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Error in sp_create_customer: ' || SQLERRM);
      RAISE;
  END;

  PROCEDURE sp_update_customer(
    p_customer_id IN NUMBER,
    p_name IN VARCHAR2,
    p_phone IN VARCHAR2,
    p_email IN VARCHAR2,
    p_rows_updated OUT NUMBER
  ) IS
  BEGIN
    -- Input validation
    IF p_customer_id IS NULL OR p_customer_id <= 0 THEN
      RAISE_APPLICATION_ERROR(-20002, 'Valid customer ID is required');
    END IF;

    IF p_name IS NULL OR p_name = '' THEN
      RAISE_APPLICATION_ERROR(-20001, 'Customer name is required');
    END IF;

    UPDATE CUSTOMER
    SET NAME = p_name,
        PHONE = p_phone,
        EMAIL = p_email,
        UPDATED_AT = CURRENT_TIMESTAMP
    WHERE CUSTOMER_ID = p_customer_id AND IS_ACTIVE = 1;

    p_rows_updated := SQL%ROWCOUNT;

    DBMS_OUTPUT.PUT_LINE(p_rows_updated || ' customer(s) updated.');

  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Error in sp_update_customer: ' || SQLERRM);
      RAISE;
  END;

  PROCEDURE sp_delete_customer(
    p_customer_id IN NUMBER,
    p_rows_deleted OUT NUMBER
  ) IS
  BEGIN
    -- Input validation
    IF p_customer_id IS NULL OR p_customer_id <= 0 THEN
      RAISE_APPLICATION_ERROR(-20002, 'Valid customer ID is required');
    END IF;

    UPDATE CUSTOMER
    SET IS_ACTIVE = 0,
        UPDATED_AT = CURRENT_TIMESTAMP
    WHERE CUSTOMER_ID = p_customer_id AND IS_ACTIVE = 1;

    p_rows_deleted := SQL%ROWCOUNT;

    DBMS_OUTPUT.PUT_LINE(p_rows_deleted || ' customer(s) soft-deleted.');

  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Error in sp_delete_customer: ' || SQLERRM);
      RAISE;
  END;
END customer_pkg;
/

-- Test the updated procedures
DECLARE
  v_rows_updated NUMBER;
  v_rows_deleted NUMBER;
BEGIN
  -- Test creating a customer
  BEGIN
    customer_pkg.sp_create_customer('Test Customer', '4379876543', 'test@example.com');
    DBMS_OUTPUT.PUT_LINE('Create customer test successful.');
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Create customer test failed: ' || SQLERRM);
  END;

  -- Test updating a customer (assuming customer with ID 1 exists)
  BEGIN
    customer_pkg.sp_update_customer(1, 'Updated Customer', '4370000000', 'updated@example.com', v_rows_updated);
    DBMS_OUTPUT.PUT_LINE('Update customer test result: ' || v_rows_updated || ' rows affected.');
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Update customer test failed: ' || SQLERRM);
  END;

  -- Test deleting a customer (assuming customer with ID 2 exists)
  BEGIN
    customer_pkg.sp_delete_customer(2, v_rows_deleted);
    DBMS_OUTPUT.PUT_LINE('Delete customer test result: ' || v_rows_deleted || ' rows affected.');
  EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('Delete customer test failed: ' || SQLERRM);
  END;
END;
/

-- View customers
SELECT CUSTOMER_ID, NAME, PHONE, EMAIL, IS_ACTIVE,
       TO_CHAR(CREATED_AT, 'YYYY-MM-DD HH24:MI:SS') AS CREATED_AT,
       TO_CHAR(UPDATED_AT, 'YYYY-MM-DD HH24:MI:SS') AS UPDATED_AT
FROM CUSTOMER;