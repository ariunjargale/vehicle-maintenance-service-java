-- Custom types for mechanic data
CREATE OR REPLACE TYPE mechanic_rec AS OBJECT (
    mechanic_id NUMBER,
    name VARCHAR2(255),
    phone VARCHAR2(20),
    specialization VARCHAR2(255),
    is_active NUMBER(1)
);
/

CREATE OR REPLACE TYPE mechanic_tab IS TABLE OF mechanic_rec;
/

-- Package Specification
CREATE OR REPLACE PACKAGE mechanic_pkg AS
    FUNCTION fn_get_active_mechanics RETURN SYS_REFCURSOR;
    FUNCTION fn_get_mechanic_by_id(p_mechanic_id IN NUMBER) RETURN SYS_REFCURSOR;
    FUNCTION fn_search_mechanics(p_search_term IN VARCHAR2) RETURN SYS_REFCURSOR;
    FUNCTION fn_get_mechanics_by_specialization(p_specialization IN VARCHAR2) RETURN SYS_REFCURSOR;
    PROCEDURE sp_create_mechanic(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_specialization IN VARCHAR2);
    PROCEDURE sp_update_mechanic(p_mechanic_id IN NUMBER, p_name IN VARCHAR2, p_phone IN VARCHAR2, p_specialization IN VARCHAR2);
    PROCEDURE sp_delete_mechanic(p_mechanic_id IN NUMBER);
    FUNCTION fn_can_delete_mechanic(p_mechanic_id IN NUMBER) RETURN BOOLEAN;
    FUNCTION fn_get_mechanic_appointment_count(p_mechanic_id IN NUMBER) RETURN NUMBER;
END mechanic_pkg;
/

-- Package Body
CREATE OR REPLACE PACKAGE BODY mechanic_pkg AS
    FUNCTION fn_get_active_mechanics RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT m.mechanic_id, m.name, m.phone, m.specialization, m.is_active,
                   COUNT(a.appointment_id) as appointment_count
            FROM mechanic m
            LEFT JOIN appointment a ON m.mechanic_id = a.mechanic_id
                AND a.is_active = 1
            WHERE m.is_active = 1
            GROUP BY m.mechanic_id, m.name, m.phone, m.specialization, m.is_active
            ORDER BY m.name;
        RETURN v_result;
    END fn_get_active_mechanics;

    FUNCTION fn_get_mechanic_by_id(p_mechanic_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT * FROM mechanic
            WHERE mechanic_id = p_mechanic_id AND is_active = 1;
        RETURN v_result;
    END;

    FUNCTION fn_search_mechanics(p_search_term IN VARCHAR2) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
        v_pattern VARCHAR2(255) := '%' || LOWER(p_search_term) || '%';
    BEGIN
        OPEN v_result FOR
            SELECT m.*, COUNT(a.appointment_id) AS appointment_count
            FROM mechanic m
            LEFT JOIN appointment a ON m.mechanic_id = a.mechanic_id AND a.is_active = 1
            WHERE m.is_active = 1
              AND (
                  LOWER(m.name) LIKE v_pattern
                  OR LOWER(m.phone) LIKE v_pattern
                  OR LOWER(m.specialization) LIKE v_pattern
              )
            GROUP BY m.mechanic_id, m.name, m.phone, m.specialization, m.is_active
            ORDER BY m.name;
        RETURN v_result;
    END;

    FUNCTION fn_get_mechanics_by_specialization(p_specialization IN VARCHAR2) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT m.*, COUNT(a.appointment_id) AS appointment_count
            FROM mechanic m
            LEFT JOIN appointment a ON m.mechanic_id = a.mechanic_id AND a.is_active = 1
            WHERE m.is_active = 1 AND m.specialization = p_specialization
            GROUP BY m.mechanic_id, m.name, m.phone, m.specialization, m.is_active
            ORDER BY m.name;
        RETURN v_result;
    END;

    PROCEDURE sp_create_mechanic(p_name IN VARCHAR2, p_phone IN VARCHAR2, p_specialization IN VARCHAR2) IS
    BEGIN
        IF p_name IS NULL OR LENGTH(TRIM(p_name)) = 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Mechanic name cannot be empty');
        END IF;

        IF p_phone IS NULL OR LENGTH(TRIM(p_phone)) = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Phone number cannot be empty');
        END IF;

        INSERT INTO mechanic (name, phone, specialization, is_active)
        VALUES (p_name, p_phone, p_specialization, 1);

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END;

    PROCEDURE sp_update_mechanic(p_mechanic_id IN NUMBER, p_name IN VARCHAR2, p_phone IN VARCHAR2, p_specialization IN VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM mechanic WHERE mechanic_id = p_mechanic_id AND is_active = 1;
        IF v_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Mechanic not found');
        END IF;

        UPDATE mechanic
        SET name = p_name,
            phone = p_phone,
            specialization = p_specialization
        WHERE mechanic_id = p_mechanic_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END;

    PROCEDURE sp_delete_mechanic(p_mechanic_id IN NUMBER) IS
        v_can_delete BOOLEAN;
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM mechanic WHERE mechanic_id = p_mechanic_id AND is_active = 1;
        IF v_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Mechanic not found or already inactive');
        END IF;

        v_can_delete := fn_can_delete_mechanic(p_mechanic_id);
        IF NOT v_can_delete THEN
            RAISE_APPLICATION_ERROR(-20004, 'Cannot delete mechanic with future appointments');
        END IF;

        UPDATE mechanic
        SET is_active = 0
        WHERE mechanic_id = p_mechanic_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END;

    FUNCTION fn_can_delete_mechanic(p_mechanic_id IN NUMBER) RETURN BOOLEAN IS
        v_appointments NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_appointments
        FROM appointment
        WHERE mechanic_id = p_mechanic_id
          AND appointment_date > SYSDATE
          AND status_id IN ('S', 'I')
          AND is_active = 1;

        RETURN v_appointments = 0;
    END;

    FUNCTION fn_get_mechanic_appointment_count(p_mechanic_id IN NUMBER) RETURN NUMBER IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM appointment
        WHERE mechanic_id = p_mechanic_id AND is_active = 1;
        RETURN v_count;
    END;
END mechanic_pkg;
/


-- === TESTING BLOCKS ===

SET SERVEROUTPUT ON;

-- Test Block 1: Create mechanics
BEGIN
    -- Create first mechanic
    mechanic_pkg.sp_create_mechanic(
        p_name => 'John Smith',
        p_phone => '1234567890',
        p_specialization => 'Engine Repair'
    );
    DBMS_OUTPUT.PUT_LINE('First mechanic created successfully');
END;
/

-- Test Block 2: Search mechanics
DECLARE
    v_cursor SYS_REFCURSOR;
    v_mechanic_id NUMBER;
    v_name VARCHAR2(255);
    v_phone VARCHAR2(20);
    v_specialization VARCHAR2(255);
    v_is_active NUMBER;
    v_appointment_count NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Searching for mechanics with name "Smith":');
    v_cursor := mechanic_pkg.fn_search_mechanics('Smith');

    LOOP
        FETCH v_cursor INTO v_mechanic_id, v_name, v_phone, v_specialization,
                          v_is_active, v_appointment_count;
        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('Found: ' || v_name ||
                           ', Phone: ' || v_phone ||
                           ', Specialization: ' || v_specialization ||
                           ', Appointments: ' || v_appointment_count);
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

-- Test Block 3: Update mechanic
DECLARE
    v_mechanic_id NUMBER;
BEGIN
    -- Get the ID of our test mechanic
    SELECT mechanic_id INTO v_mechanic_id
    FROM mechanic
    WHERE name = 'John Smith'
    AND ROWNUM = 1;

    -- Update the mechanic
    mechanic_pkg.sp_update_mechanic(
        p_mechanic_id => 43,   --make sure to check for correct id
        p_name => 'John Smith Jr',
        p_phone => '1234567890',
        p_specialization => 'Advanced Engine Repair'
    );

    DBMS_OUTPUT.PUT_LINE('Mechanic updated successfully');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Test mechanic not found');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/

-- Test Block 4: Get active mechanics
DECLARE
    v_cursor SYS_REFCURSOR;
    v_mechanic_id NUMBER;
    v_name VARCHAR2(255);
    v_phone VARCHAR2(20);
    v_specialization VARCHAR2(255);
    v_is_active NUMBER;
    v_appointment_count NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Listing all active mechanics:');
    v_cursor := mechanic_pkg.fn_get_active_mechanics;

    LOOP
        FETCH v_cursor INTO v_mechanic_id, v_name, v_phone, v_specialization,
                          v_is_active, v_appointment_count;
        EXIT WHEN v_cursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('ID: ' || v_mechanic_id ||
                           ', Name: ' || v_name ||
                           ', Phone: ' || v_phone ||
                           ', Specialization: ' || v_specialization ||
                           ', Appointments: ' || v_appointment_count);
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

-- Test Block 5: Delete mechanic
DECLARE
    v_mechanic_id NUMBER;
BEGIN
    -- Get the ID of our test mechanic
    SELECT mechanic_id INTO v_mechanic_id
    FROM mechanic
    WHERE name = 'John Smith Jr'
    AND ROWNUM = 1;

    -- Delete the mechanic
    mechanic_pkg.sp_delete_mechanic(v_mechanic_id);
    DBMS_OUTPUT.PUT_LINE('Mechanic deleted successfully');
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Test mechanic not found');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;
/



