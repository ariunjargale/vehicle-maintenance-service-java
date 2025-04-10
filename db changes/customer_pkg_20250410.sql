SET SERVEROUTPUT ON;

-- ============================
-- 1. PACKAGE SPECIFICATION
-- ============================

CREATE OR REPLACE PACKAGE customer_pkg AS
    -- Error codes
    c_err_name_required CONSTANT NUMBER := -20001;
    c_err_phone_required CONSTANT NUMBER := -20002;
    c_err_invalid_phone CONSTANT NUMBER := -20003;
    c_err_invalid_email CONSTANT NUMBER := -20004;
    c_err_customer_not_found CONSTANT NUMBER := -20005;
    c_err_has_appointments CONSTANT NUMBER := -20006;
    c_err_email_exists CONSTANT NUMBER := -20007;

    -- Get all active customers
    FUNCTION fn_get_active_customers RETURN SYS_REFCURSOR;

    -- Get customer by ID
    FUNCTION fn_get_customer_by_id(
        p_customer_id IN NUMBER
    ) RETURN SYS_REFCURSOR;

    -- Search customers
    FUNCTION fn_search_customers(
        p_search_term IN VARCHAR2
    ) RETURN SYS_REFCURSOR;

    -- Create new customer
    PROCEDURE sp_create_customer(
        p_name IN VARCHAR2,
        p_phone IN VARCHAR2,
        p_email IN VARCHAR2,
        p_customer_id OUT NUMBER
    );

    -- Update existing customer
    PROCEDURE sp_update_customer(
        p_customer_id IN NUMBER,
        p_name IN VARCHAR2,
        p_phone IN VARCHAR2,
        p_email IN VARCHAR2
    );

    -- Soft delete customer
    PROCEDURE sp_delete_customer(
        p_customer_id IN NUMBER
    );

    -- Check if customer can be deleted
    FUNCTION fn_can_delete_customer(
        p_customer_id IN NUMBER
    ) RETURN BOOLEAN;

    -- Get customer's vehicle count
    FUNCTION fn_get_customer_vehicle_count(
        p_customer_id IN NUMBER
    ) RETURN NUMBER;

    -- Get customer's appointment count
    FUNCTION fn_get_customer_appointment_count(
        p_customer_id IN NUMBER
    ) RETURN NUMBER;

    -- Validate email format
    FUNCTION fn_validate_email(
        p_email IN VARCHAR2
    ) RETURN BOOLEAN;

    -- Validate phone format
    FUNCTION fn_validate_phone(
        p_phone IN VARCHAR2
    ) RETURN BOOLEAN;
END customer_pkg;
/

-- ============================
-- 2. PACKAGE BODY
-- ============================

CREATE OR REPLACE PACKAGE BODY customer_pkg AS

    FUNCTION fn_get_active_customers RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT c.*,
                   COUNT(DISTINCT v.vehicle_id) as vehicle_count,
                   COUNT(DISTINCT a.appointment_id) as appointment_count
            FROM customer c
            LEFT JOIN vehicle v ON c.customer_id = v.customer_id
                AND v.is_active = 1
            LEFT JOIN appointment a ON c.customer_id = a.customer_id
                AND a.is_active = 1
            WHERE c.is_active = 1
            GROUP BY c.customer_id, c.name, c.phone, c.email, c.is_active
            ORDER BY c.name;
        RETURN v_cursor;
    END;

    FUNCTION fn_get_customer_by_id(p_customer_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT * FROM customer
            WHERE customer_id = p_customer_id
              AND is_active = 1;
        RETURN v_cursor;
    END;

    FUNCTION fn_search_customers(p_search_term IN VARCHAR2) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
        v_pattern VARCHAR2(255) := '%' || LOWER(p_search_term) || '%';
    BEGIN
        OPEN v_cursor FOR
            SELECT * FROM customer
            WHERE is_active = 1
              AND (LOWER(name) LIKE v_pattern OR LOWER(phone) LIKE v_pattern OR LOWER(email) LIKE v_pattern)
            ORDER BY name;
        RETURN v_cursor;
    END;

    FUNCTION fn_validate_email(p_email IN VARCHAR2) RETURN BOOLEAN IS
    BEGIN
        -- Basic email validation (contains @ and at least one .)
        RETURN REGEXP_LIKE(p_email, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');
    END;

    FUNCTION fn_validate_phone(p_phone IN VARCHAR2) RETURN BOOLEAN IS
    BEGIN
        -- Simple phone validation (10 digits, allows some formatting)
        RETURN REGEXP_LIKE(REGEXP_REPLACE(p_phone, '[^0-9]', ''), '^[0-9]{10}$');
    END;

    PROCEDURE sp_create_customer(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2, p_customer_id OUT NUMBER) IS
        v_count NUMBER;
    BEGIN
        -- Validate input parameters
        IF p_name IS NULL OR LENGTH(TRIM(p_name)) = 0 THEN
            RAISE_APPLICATION_ERROR(c_err_name_required, 'Customer name is required');
        END IF;

        IF p_phone IS NULL OR LENGTH(TRIM(p_phone)) = 0 THEN
            RAISE_APPLICATION_ERROR(c_err_phone_required, 'Phone number is required');
        END IF;

        IF NOT fn_validate_phone(p_phone) THEN
            RAISE_APPLICATION_ERROR(c_err_invalid_phone, 'Invalid phone number format. Must be 10 digits.');
        END IF;

        IF p_email IS NOT NULL AND LENGTH(TRIM(p_email)) > 0 AND NOT fn_validate_email(p_email) THEN
            RAISE_APPLICATION_ERROR(c_err_invalid_email, 'Invalid email format');
        END IF;

        -- Check email uniqueness if provided
        IF p_email IS NOT NULL AND LENGTH(TRIM(p_email)) > 0 THEN
            SELECT COUNT(*) INTO v_count
            FROM customer
            WHERE LOWER(email) = LOWER(TRIM(p_email))
            AND is_active = 1;

            IF v_count > 0 THEN
                RAISE_APPLICATION_ERROR(c_err_email_exists, 'Email already exists');
            END IF;
        END IF;

        INSERT INTO customer (name, phone, email, is_active)
        VALUES (p_name, p_phone, p_email, 1)
        RETURNING customer_id INTO p_customer_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END;

    PROCEDURE sp_update_customer(p_customer_id IN NUMBER, p_name IN VARCHAR2, p_phone IN VARCHAR2, p_email IN VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        -- Check if customer exists
        SELECT COUNT(*) INTO v_count
        FROM customer
        WHERE customer_id = p_customer_id
          AND is_active = 1;

        IF v_count = 0 THEN
            RAISE_APPLICATION_ERROR(c_err_customer_not_found, 'Customer does not exist or is inactive');
        END IF;

        -- Validate input parameters
        IF p_name IS NULL OR LENGTH(TRIM(p_name)) = 0 THEN
            RAISE_APPLICATION_ERROR(c_err_name_required, 'Customer name is required');
        END IF;

        IF p_phone IS NULL OR LENGTH(TRIM(p_phone)) = 0 THEN
            RAISE_APPLICATION_ERROR(c_err_phone_required, 'Phone number is required');
        END IF;

        IF NOT fn_validate_phone(p_phone) THEN
            RAISE_APPLICATION_ERROR(c_err_invalid_phone, 'Invalid phone number format. Must be 10 digits.');
        END IF;

        IF p_email IS NOT NULL AND LENGTH(TRIM(p_email)) > 0 AND NOT fn_validate_email(p_email) THEN
            RAISE_APPLICATION_ERROR(c_err_invalid_email, 'Invalid email format');
        END IF;

        -- Check email uniqueness if provided
        IF p_email IS NOT NULL AND LENGTH(TRIM(p_email)) > 0 THEN
            SELECT COUNT(*) INTO v_count
            FROM customer
            WHERE LOWER(email) = LOWER(TRIM(p_email))
            AND customer_id != p_customer_id
            AND is_active = 1;

            IF v_count > 0 THEN
                RAISE_APPLICATION_ERROR(c_err_email_exists, 'Email already exists');
            END IF;
        END IF;

        UPDATE customer
        SET name = p_name,
            phone = p_phone,
            email = p_email
        WHERE customer_id = p_customer_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END;

    PROCEDURE sp_delete_customer(p_customer_id IN NUMBER) IS
        v_count NUMBER;
        v_can_delete BOOLEAN;
    BEGIN
        -- Check if customer exists
        SELECT COUNT(*) INTO v_count
        FROM customer
        WHERE customer_id = p_customer_id
          AND is_active = 1;

        IF v_count = 0 THEN
            RAISE_APPLICATION_ERROR(c_err_customer_not_found, 'Customer does not exist or is inactive');
        END IF;

        -- Check if customer can be deleted
        v_can_delete := fn_can_delete_customer(p_customer_id);

        IF NOT v_can_delete THEN
            RAISE_APPLICATION_ERROR(c_err_has_appointments, 'Cannot delete customer with future appointments');
        END IF;

        -- First deactivate all customer's vehicles
        UPDATE vehicle
        SET is_active = 0
        WHERE customer_id = p_customer_id;

        -- Then deactivate the customer
        UPDATE customer
        SET is_active = 0
        WHERE customer_id = p_customer_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END;

    FUNCTION fn_can_delete_customer(p_customer_id IN NUMBER) RETURN BOOLEAN IS
        v_future_appointments NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO v_future_appointments
        FROM appointment
        WHERE customer_id = p_customer_id
          AND appointment_date > SYSDATE
          AND is_active = 1;

        RETURN v_future_appointments = 0;
    END;

    FUNCTION fn_get_customer_vehicle_count(p_customer_id IN NUMBER) RETURN NUMBER IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM vehicle
        WHERE customer_id = p_customer_id
        AND is_active = 1;
        RETURN v_count;
    END;

    FUNCTION fn_get_customer_appointment_count(p_customer_id IN NUMBER) RETURN NUMBER IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM appointment
        WHERE customer_id = p_customer_id
          AND is_active = 1;
        RETURN v_count;
    END;

END customer_pkg;
/

-- ============================
-- 3. TESTING BLOCKS
-- ============================

-- 1. Create Customer
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test 1: Creating a new customer');
    DECLARE
        v_customer_id NUMBER;
    BEGIN
        customer_pkg.sp_create_customer(
            p_name => 'Timur Dauletov',
            p_phone => '1234567890',
            p_email => 'timur.dauletov@email.com',
            p_customer_id => v_customer_id
        );
        DBMS_OUTPUT.PUT_LINE('Customer created with ID: ' || v_customer_id);
    END;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/


SELECT * FROM customer WHERE name = 'Timur Dauletov';

SELECT customer_id, name, email, is_active
FROM customer
WHERE email = 'timur.dauletov@email.com';


-- 2. Test Invalid Email
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test 1b: Creating a customer with invalid email');
    DECLARE
        v_customer_id NUMBER;
    BEGIN
        customer_pkg.sp_create_customer(
            p_name => 'Jane Smith',
            p_phone => '9876543210',
            p_email => 'invalid-email', -- Invalid email format
            p_customer_id => v_customer_id
        );
        DBMS_OUTPUT.PUT_LINE('Customer created with ID: ' || v_customer_id);
    END;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Expected error: ' || SQLERRM);
END;
/

-- 3. Test Invalid Phone
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test 1c: Creating a customer with invalid phone');
    DECLARE
        v_customer_id NUMBER;
    BEGIN
        customer_pkg.sp_create_customer(
            p_name => 'Jane Smith',
            p_phone => '123', -- Too short
            p_email => 'jane.smith@email.com',
            p_customer_id => v_customer_id
        );
        DBMS_OUTPUT.PUT_LINE('Customer created with ID: ' || v_customer_id);
    END;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Expected error: ' || SQLERRM);
END;
/

-- 4. Test Duplicate Email
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test 1d: Creating a customer with duplicate email');
    DECLARE
        v_customer_id NUMBER;
    BEGIN
        customer_pkg.sp_create_customer(
            p_name => 'Another Person',
            p_phone => '5551234567',
            p_email => 'timur.dauletov@email.com', -- Already used
            p_customer_id => v_customer_id
        );
        DBMS_OUTPUT.PUT_LINE('Customer created with ID: ' || v_customer_id);
    END;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Expected error: ' || SQLERRM);
END;
/

-- 5. Search Customer
DECLARE
    v_cursor SYS_REFCURSOR;
    v_customer_id NUMBER;
    v_name VARCHAR2(255);
    v_phone VARCHAR2(20);
    v_email VARCHAR2(255);
    v_is_active NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test 2: Searching for customer with name "Timur"');
    v_cursor := customer_pkg.fn_search_customers('Timur');

    LOOP
        FETCH v_cursor INTO v_customer_id, v_name, v_phone, v_email, v_is_active;
        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('Found: ' || v_name || ', Phone: ' || v_phone ||
                           ', Email: ' || v_email);
    END LOOP;

    CLOSE v_cursor;
EXCEPTION
    WHEN OTHERS THEN
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/

-- 6. Update Customer
DECLARE
    v_customer_id NUMBER;
BEGIN
    -- Get the customer ID dynamically
    SELECT customer_id INTO v_customer_id
    FROM customer
    WHERE name = 'Timur Dauletov'
    AND is_active = 1
    AND ROWNUM = 1;

    DBMS_OUTPUT.PUT_LINE('Test 3: Updating customer with ID: ' || v_customer_id);
    customer_pkg.sp_update_customer(
        p_customer_id => v_customer_id,
        p_name => 'Timur K. Dauletov',
        p_phone => '1234567890',
        p_email => 'timur.k.dauletov@email.com'
    );
    DBMS_OUTPUT.PUT_LINE('Customer updated successfully');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Test customer not found');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/

-- 7. Get Customer Details
DECLARE
    v_cursor SYS_REFCURSOR;
    v_customer_id NUMBER;
    v_name VARCHAR2(255);
    v_phone VARCHAR2(20);
    v_email VARCHAR2(255);
    v_is_active NUMBER;
BEGIN
    -- Get the customer ID dynamically
    SELECT customer_id INTO v_customer_id
    FROM customer
    WHERE name = 'Timur K. Dauletov'
    AND is_active = 1
    AND ROWNUM = 1;

    DBMS_OUTPUT.PUT_LINE('Test 4: Getting customer details for ID: ' || v_customer_id);
    v_cursor := customer_pkg.fn_get_customer_by_id(v_customer_id);

    FETCH v_cursor INTO v_customer_id, v_name, v_phone, v_email, v_is_active;

    IF v_cursor%FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Found: ' || v_name || ', Phone: ' || v_phone ||
                           ', Email: ' || v_email);
    END IF;

    CLOSE v_cursor;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Test customer not found');
    WHEN OTHERS THEN
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/

-- 8. Delete Customer
DECLARE
    v_customer_id NUMBER;
BEGIN
    -- Get the customer ID dynamically
    SELECT customer_id INTO v_customer_id
    FROM customer
    WHERE name = 'Timur K. Dauletov'
    AND is_active = 1
    AND ROWNUM = 1;

    DBMS_OUTPUT.PUT_LINE('Test 5: Deleting customer with ID: ' || v_customer_id);
    customer_pkg.sp_delete_customer(v_customer_id);
    DBMS_OUTPUT.PUT_LINE('Customer deleted successfully');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Test customer not found');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/

-- 9. Verify Active Customers with Count Information
DECLARE
    v_cursor SYS_REFCURSOR;
    v_customer_id NUMBER;
    v_name VARCHAR2(255);
    v_phone VARCHAR2(20);
    v_email VARCHAR2(255);
    v_is_active NUMBER;
    v_vehicle_count NUMBER;
    v_appointment_count NUMBER;
    v_count NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Test 6: Listing all active customers with counts');
    v_cursor := customer_pkg.fn_get_active_customers;

    LOOP
        FETCH v_cursor INTO v_customer_id, v_name, v_phone, v_email, v_is_active, v_vehicle_count, v_appointment_count;
        EXIT WHEN v_cursor%NOTFOUND;

        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE(v_count || '. ' || v_name || ', Phone: ' || v_phone ||
                           ', Email: ' || v_email ||
                           ', Vehicles: ' || v_vehicle_count ||
                           ', Appointments: ' || v_appointment_count);
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('Total active customers: ' || v_count);
    CLOSE v_cursor;
EXCEPTION
    WHEN OTHERS THEN
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/