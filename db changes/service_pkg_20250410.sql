-- Custom types for service data
CREATE OR REPLACE TYPE service_rec AS OBJECT (
    service_id NUMBER,
    service_name VARCHAR2(255),
    service_type_id VARCHAR2(255),
    price NUMBER(10,2),
    is_active NUMBER(1)
);
/

CREATE OR REPLACE TYPE service_tab IS TABLE OF service_rec;
/

CREATE OR REPLACE PACKAGE service_pkg AS
    -- Get all active services
    FUNCTION fn_get_active_services RETURN SYS_REFCURSOR;

    -- Get service by ID
    FUNCTION fn_get_service_by_id(
        p_service_id IN NUMBER
    ) RETURN SYS_REFCURSOR;

    -- Search services
    FUNCTION fn_search_services(
        p_search_term IN VARCHAR2
    ) RETURN SYS_REFCURSOR;

    -- Create new service
    PROCEDURE sp_create_service(
        p_service_name IN VARCHAR2,
        p_service_type_id IN VARCHAR2,
        p_price IN NUMBER
    );

    -- Update existing service
    PROCEDURE sp_update_service(
        p_service_id IN NUMBER,
        p_service_name IN VARCHAR2,
        p_service_type_id IN VARCHAR2,
        p_price IN NUMBER
    );

    -- Soft delete service
    PROCEDURE sp_delete_service(
        p_service_id IN NUMBER
    );

    -- Get service type name
    FUNCTION fn_get_service_type_name(
        p_service_type_id IN VARCHAR2
    ) RETURN VARCHAR2;
END service_pkg;
/

CREATE OR REPLACE PACKAGE BODY service_pkg AS
    -- Get all active services
    FUNCTION fn_get_active_services RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT * FROM service
            WHERE is_active = 1
            ORDER BY service_name;
        RETURN v_result;
    END fn_get_active_services;

    -- Get service by ID
    FUNCTION fn_get_service_by_id(
        p_service_id IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT * FROM service
            WHERE service_id = p_service_id
            AND is_active = 1;
        RETURN v_result;
    END fn_get_service_by_id;

    -- Search services
    FUNCTION fn_search_services(
        p_search_term IN VARCHAR2
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
        v_search_pattern VARCHAR2(255) := '%' || LOWER(p_search_term) || '%';
    BEGIN
        OPEN v_result FOR
            SELECT * FROM service
            WHERE is_active = 1
            AND (
                LOWER(service_name) LIKE v_search_pattern
                OR LOWER(service_type_id) LIKE v_search_pattern
                OR TO_CHAR(price, '999999.99') LIKE v_search_pattern
            )
            ORDER BY service_name;
        RETURN v_result;
    END fn_search_services;

    -- Create new service
    PROCEDURE sp_create_service(
        p_service_name IN VARCHAR2,
        p_service_type_id IN VARCHAR2,
        p_price IN NUMBER
    ) IS
        v_existing_count NUMBER;
    BEGIN
        -- Validate service name
        SELECT COUNT(*)
        INTO v_existing_count
        FROM service
        WHERE LOWER(service_name) = LOWER(p_service_name)
        AND is_active = 1;

        IF v_existing_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Service with this name already exists');
        END IF;

        -- Validate price
        IF p_price <= 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Price must be greater than zero');
        END IF;

        -- Insert service
        INSERT INTO service (
            service_name,
            service_type_id,
            price,
            is_active
        ) VALUES (
            p_service_name,
            p_service_type_id,
            p_price,
            1
        );

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END sp_create_service;

    -- Update existing service
    PROCEDURE sp_update_service(
        p_service_id IN NUMBER,
        p_service_name IN VARCHAR2,
        p_service_type_id IN VARCHAR2,
        p_price IN NUMBER
    ) IS
        v_existing_count NUMBER;
    BEGIN
        -- Check if service exists
        SELECT COUNT(*)
        INTO v_existing_count
        FROM service
        WHERE service_id = p_service_id
        AND is_active = 1;

        IF v_existing_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Service does not exist');
        END IF;

        -- Check name uniqueness
        SELECT COUNT(*)
        INTO v_existing_count
        FROM service
        WHERE LOWER(service_name) = LOWER(p_service_name)
        AND service_id != p_service_id
        AND is_active = 1;

        IF v_existing_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Service with this name already exists');
        END IF;

        -- Validate price
        IF p_price <= 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Price must be greater than zero');
        END IF;

        -- Update service
        UPDATE service
        SET service_name = p_service_name,
            service_type_id = p_service_type_id,
            price = p_price
        WHERE service_id = p_service_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END sp_update_service;

    -- Soft delete service
    PROCEDURE sp_delete_service(
        p_service_id IN NUMBER
    ) IS
        v_existing_count NUMBER;
        v_appointment_count NUMBER;
    BEGIN
        -- Check if service exists
        SELECT COUNT(*)
        INTO v_existing_count
        FROM service
        WHERE service_id = p_service_id
        AND is_active = 1;

        IF v_existing_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Service does not exist');
        END IF;

        -- Check for future appointments
        SELECT COUNT(*)
        INTO v_appointment_count
        FROM appointment
        WHERE service_id = p_service_id
        AND appointment_date > SYSDATE
        AND is_active = 1;

        IF v_appointment_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20004,
                'Cannot delete service with ' || v_appointment_count || ' future appointments');
        END IF;

        -- Soft delete
        UPDATE service
        SET is_active = 0
        WHERE service_id = p_service_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END sp_delete_service;

    -- Get service type name
    FUNCTION fn_get_service_type_name(
        p_service_type_id IN VARCHAR2
    ) RETURN VARCHAR2 IS
    BEGIN
        CASE p_service_type_id
            WHEN '1' THEN RETURN 'Regular Maintenance';
            WHEN '2' THEN RETURN 'Engine Repair';
            WHEN '3' THEN RETURN 'Transmission Repair';
            WHEN '4' THEN RETURN 'Brake Service';
            WHEN '5' THEN RETURN 'Electrical Repair';
            WHEN '6' THEN RETURN 'Air Conditioning';
            WHEN '7' THEN RETURN 'Suspension Work';
            WHEN '8' THEN RETURN 'Wheel and Tire Service';
            WHEN '9' THEN RETURN 'Diagnostic Service';
            WHEN '10' THEN RETURN 'Exhaust System Repair';
            ELSE RETURN 'Unknown Service Type (' || p_service_type_id || ')';
        END CASE;
    END fn_get_service_type_name;
END service_pkg;
/

--Testing blocks
-- 1. Create a new service
BEGIN
  service_pkg.sp_create_service(
    p_service_name => 'Test Service',
    p_service_type_id => '1',
    p_price => 79.99
  );
  COMMIT;
  DBMS_OUTPUT.PUT_LINE('Service created successfully');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    ROLLBACK;
END;
/

-- 2. Verify the service was added
SELECT * FROM service
WHERE LOWER(service_name) = LOWER('Test Service'); -- If using  differnt service name, make sure to change for step 2 as well to match step 1

-- 3. Get a service by ID
DECLARE
  v_cursor SYS_REFCURSOR;
  v_service_id NUMBER;
  v_service_name VARCHAR2(255);
  v_service_type_id VARCHAR2(255);
  v_price NUMBER;
  v_is_active NUMBER;
BEGIN
  -- Get the ID of our test service
  SELECT service_id INTO v_service_id
  FROM service
  WHERE LOWER(service_name) = LOWER('Test Service')
  AND ROWNUM = 1;

  v_cursor := service_pkg.fn_get_service_by_id(v_service_id);

  DBMS_OUTPUT.PUT_LINE('Service details:');
  LOOP
    FETCH v_cursor INTO v_service_id, v_service_name, v_service_type_id, v_price, v_is_active;
    EXIT WHEN v_cursor%NOTFOUND;

    DBMS_OUTPUT.PUT_LINE('ID: ' || v_service_id ||
                       ', Name: ' || v_service_name ||
                       ', Type: ' || service_pkg.fn_get_service_type_name(v_service_type_id) ||
                       ', Price: $' || TO_CHAR(v_price, '999999.99'));
  END LOOP;

  CLOSE v_cursor;
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    DBMS_OUTPUT.PUT_LINE('Test service not found');
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    IF v_cursor%ISOPEN THEN
      CLOSE v_cursor;
    END IF;
END;
/

-- 4. Update the service
DECLARE
  v_service_id NUMBER;
BEGIN
  -- Get the ID of our test service
  SELECT service_id INTO v_service_id
  FROM service
  WHERE LOWER(service_name) = LOWER('Test Service') -- If using  differnt service name, make sure to change for step 2 as well to match step 1
  AND ROWNUM = 1;

  service_pkg.sp_update_service(
    p_service_id => v_service_id,
    p_service_name => 'Updated Test Service',
    p_service_type_id => '2',
    p_price => 89.99
  );
  DBMS_OUTPUT.PUT_LINE('Service updated successfully');
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    DBMS_OUTPUT.PUT_LINE('Test service not found');
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    ROLLBACK;
END;
/

-- 5. Verify the update
SELECT * FROM service
WHERE LOWER(service_name) = LOWER('Updated Test Service');

-- 6. Search for services
DECLARE
  v_cursor SYS_REFCURSOR;
  v_service_id NUMBER;
  v_service_name VARCHAR2(255);
  v_service_type_id VARCHAR2(255);
  v_price NUMBER;
  v_is_active NUMBER;
BEGIN
  v_cursor := service_pkg.fn_search_services('Test');

  DBMS_OUTPUT.PUT_LINE('Search results for "Test":');
  LOOP
    FETCH v_cursor INTO v_service_id, v_service_name, v_service_type_id, v_price, v_is_active;
    EXIT WHEN v_cursor%NOTFOUND;

    DBMS_OUTPUT.PUT_LINE('ID: ' || v_service_id ||
                       ', Name: ' || v_service_name ||
                       ', Type: ' || service_pkg.fn_get_service_type_name(v_service_type_id) ||
                       ', Price: $' || TO_CHAR(v_price, '999999.99'));
  END LOOP;

  CLOSE v_cursor;
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    IF v_cursor%ISOPEN THEN
      CLOSE v_cursor;
    END IF;
END;
/

-- 7. Delete the service
DECLARE
  v_service_id NUMBER;
BEGIN
  -- Get the ID of our test service
  SELECT service_id INTO v_service_id
  FROM service
  WHERE LOWER(service_name) = LOWER('Updated Test Service')
  AND ROWNUM = 1;

  service_pkg.sp_delete_service(v_service_id);
  DBMS_OUTPUT.PUT_LINE('Service deleted successfully');
EXCEPTION
  WHEN NO_DATA_FOUND THEN
    DBMS_OUTPUT.PUT_LINE('Test service not found');
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    ROLLBACK;
END;
/

-- 8. Verify the delete (should show is_active = 0)
SELECT * FROM service
WHERE LOWER(service_name) = LOWER('Updated Test Service');