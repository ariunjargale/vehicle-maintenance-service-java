-- vehicle_pkg_20250323.sql
-- Created by: Samuel Law
-- Date: 2025-03-23
-- Description: Package for Vehicle Management
-- Includes: CRUD operations for vehicles

-- CREATE OR REPLACE PACKAGE vehicle_pkg AS
--     -- Add a new vehicle
--     PROCEDURE sp_add_vehicle(
--         p_customer_id IN NUMBER,
--         p_make IN VARCHAR2,
--         p_model IN VARCHAR2,
--         p_year IN NUMBER,
--         p_vin IN VARCHAR2,
--         p_vehicle_id OUT NUMBER
--     );
    
--     -- Update vehicle information
--     PROCEDURE sp_update_vehicle(
--         p_vehicle_id IN NUMBER,
--         p_make IN VARCHAR2,
--         p_model IN VARCHAR2,
--         p_year IN NUMBER,
--         p_vin IN VARCHAR2
--     );
    
--     -- Change vehicle owner
--     PROCEDURE sp_change_vehicle_owner(
--         p_vehicle_id IN NUMBER,
--         p_new_customer_id IN NUMBER
--     );
    
--     -- Soft delete vehicle (mark as inactive)
--     PROCEDURE sp_deactivate_vehicle(
--         p_vehicle_id IN NUMBER
--     );
    
--     -- Reactivate vehicle
--     PROCEDURE sp_reactivate_vehicle(
--         p_vehicle_id IN NUMBER
--     );
    
--     -- Hard delete vehicle (permanently remove from database)
--     PROCEDURE sp_delete_vehicle(
--         p_vehicle_id IN NUMBER
--     );
    
--     -- Query vehicles by customer ID
--     FUNCTION fn_get_vehicles_by_customer(
--         p_customer_id IN NUMBER
--     ) RETURN SYS_REFCURSOR;
    
--     -- Query vehicle by VIN
--     FUNCTION fn_get_vehicle_by_vin(
--         p_vin IN VARCHAR2
--     ) RETURN SYS_REFCURSOR;
    
--     -- Query vehicle by ID
--     FUNCTION fn_get_vehicle_by_id(
--         p_vehicle_id IN NUMBER
--     ) RETURN SYS_REFCURSOR;
    
--     -- Search vehicles (by multiple criteria)
--     FUNCTION fn_search_vehicles(
--         p_search_term IN VARCHAR2
--     ) RETURN SYS_REFCURSOR;
    
--     -- Get all active vehicles
--     FUNCTION fn_get_all_active_vehicles RETURN SYS_REFCURSOR;
-- END vehicle_pkg;
-- /

CREATE OR REPLACE PACKAGE BODY vehicle_pkg AS
    -- Add a new vehicle
    PROCEDURE sp_add_vehicle(
        p_customer_id IN NUMBER,
        p_make IN VARCHAR2,
        p_model IN VARCHAR2,
        p_year IN NUMBER,
        p_vin IN VARCHAR2,
        p_vehicle_id OUT NUMBER
    ) IS
    BEGIN
        INSERT INTO VEHICLE (
            CUSTOMER_ID,
            MAKE,
            MODEL,
            YEAR,
            VIN,
            IS_ACTIVE
        ) VALUES (
            p_customer_id,
            p_make,
            p_model,
            p_year,
            p_vin,
            1
        ) RETURNING VEHICLE_ID INTO p_vehicle_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_add_vehicle;
    
    -- Update vehicle information
    PROCEDURE sp_update_vehicle(
        p_vehicle_id IN NUMBER,
        p_make IN VARCHAR2,
        p_model IN VARCHAR2,
        p_year IN NUMBER,
        p_vin IN VARCHAR2
    ) IS
    BEGIN
        UPDATE VEHICLE
        SET MAKE = p_make,
            MODEL = p_model,
            YEAR = p_year,
            VIN = p_vin
        WHERE VEHICLE_ID = p_vehicle_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_update_vehicle;
    
    -- Change vehicle owner
    PROCEDURE sp_change_vehicle_owner(
        p_vehicle_id IN NUMBER,
        p_new_customer_id IN NUMBER
    ) IS
    BEGIN
        UPDATE VEHICLE
        SET CUSTOMER_ID = p_new_customer_id
        WHERE VEHICLE_ID = p_vehicle_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_change_vehicle_owner;
    
    -- Soft delete vehicle (mark as inactive)
    PROCEDURE sp_deactivate_vehicle(
        p_vehicle_id IN NUMBER
    ) IS
    BEGIN
        UPDATE VEHICLE
        SET IS_ACTIVE = 0
        WHERE VEHICLE_ID = p_vehicle_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_deactivate_vehicle;
    
    -- Reactivate vehicle
    PROCEDURE sp_reactivate_vehicle(
        p_vehicle_id IN NUMBER
    ) IS
    BEGIN
        UPDATE VEHICLE
        SET IS_ACTIVE = 1
        WHERE VEHICLE_ID = p_vehicle_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_reactivate_vehicle;
    
    -- Hard delete vehicle (permanently remove from database)
    PROCEDURE sp_delete_vehicle(
        p_vehicle_id IN NUMBER
    ) IS
    BEGIN
        DELETE FROM VEHICLE
        WHERE VEHICLE_ID = p_vehicle_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_delete_vehicle;
    
    -- Query vehicles by customer ID
    FUNCTION fn_get_vehicles_by_customer(
        p_customer_id IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                v.VEHICLE_ID, 
                v.CUSTOMER_ID,
                c.NAME as CUSTOMER_NAME,
                v.MAKE, 
                v.MODEL, 
                v.YEAR, 
                v.VIN, 
                v.IS_ACTIVE
            FROM VEHICLE v
            JOIN CUSTOMER c ON v.CUSTOMER_ID = c.CUSTOMER_ID
            WHERE v.CUSTOMER_ID = p_customer_id
            AND v.IS_ACTIVE = 1;
            
        RETURN v_cursor;
        
        EXCEPTION
            WHEN OTHERS THEN
                IF v_cursor%ISOPEN THEN
                    CLOSE v_cursor;
                END IF;
                RAISE;
    END fn_get_vehicles_by_customer;
    
    -- Query vehicle by VIN
    FUNCTION fn_get_vehicle_by_vin(
        p_vin IN VARCHAR2
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                v.VEHICLE_ID, 
                v.CUSTOMER_ID,
                c.NAME as CUSTOMER_NAME,
                v.MAKE, 
                v.MODEL, 
                v.YEAR, 
                v.VIN, 
                v.IS_ACTIVE
            FROM VEHICLE v
            JOIN CUSTOMER c ON v.CUSTOMER_ID = c.CUSTOMER_ID
            WHERE v.VIN = p_vin;
            
        RETURN v_cursor;
        
        EXCEPTION
            WHEN OTHERS THEN
                IF v_cursor%ISOPEN THEN
                    CLOSE v_cursor;
                END IF;
                RAISE;
    END fn_get_vehicle_by_vin;
    
    -- Query vehicle by ID
    FUNCTION fn_get_vehicle_by_id(
        p_vehicle_id IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                v.VEHICLE_ID, 
                v.CUSTOMER_ID,
                c.NAME as CUSTOMER_NAME,
                v.MAKE, 
                v.MODEL, 
                v.YEAR, 
                v.VIN, 
                v.IS_ACTIVE
            FROM VEHICLE v
            JOIN CUSTOMER c ON v.CUSTOMER_ID = c.CUSTOMER_ID
            WHERE v.VEHICLE_ID = p_vehicle_id;
            
        RETURN v_cursor;
        
        EXCEPTION
            WHEN OTHERS THEN
                IF v_cursor%ISOPEN THEN
                    CLOSE v_cursor;
                END IF;
                RAISE;
    END fn_get_vehicle_by_id;
    
    -- Search vehicles (by multiple criteria)
    FUNCTION fn_search_vehicles(
        p_search_term IN VARCHAR2
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
        v_search_pattern VARCHAR2(255);
    BEGIN
        v_search_pattern := '%' || UPPER(p_search_term) || '%';
        
        OPEN v_cursor FOR
            SELECT 
                v.VEHICLE_ID, 
                v.CUSTOMER_ID,
                c.NAME as CUSTOMER_NAME,
                v.MAKE, 
                v.MODEL, 
                v.YEAR, 
                v.VIN, 
                v.IS_ACTIVE
            FROM VEHICLE v
            JOIN CUSTOMER c ON v.CUSTOMER_ID = c.CUSTOMER_ID
            WHERE (
                UPPER(v.MAKE) LIKE v_search_pattern OR
                UPPER(v.MODEL) LIKE v_search_pattern OR
                UPPER(v.VIN) LIKE v_search_pattern OR
                TO_CHAR(v.YEAR) LIKE v_search_pattern OR
                UPPER(c.NAME) LIKE v_search_pattern
            )
            AND v.IS_ACTIVE = 1
            ORDER BY v.MAKE, v.MODEL;
            
        RETURN v_cursor;
        
        EXCEPTION
            WHEN OTHERS THEN
                IF v_cursor%ISOPEN THEN
                    CLOSE v_cursor;
                END IF;
                RAISE;
    END fn_search_vehicles;
    
    -- Get all active vehicles
    FUNCTION fn_get_all_active_vehicles RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT 
                v.VEHICLE_ID, 
                v.CUSTOMER_ID,
                c.NAME as CUSTOMER_NAME,
                v.MAKE, 
                v.MODEL, 
                v.YEAR, 
                v.VIN, 
                v.IS_ACTIVE
            FROM VEHICLE v
            JOIN CUSTOMER c ON v.CUSTOMER_ID = c.CUSTOMER_ID
            WHERE v.IS_ACTIVE = 1
            ORDER BY v.MAKE, v.MODEL;
            
        RETURN v_cursor;
        
        EXCEPTION
            WHEN OTHERS THEN
                IF v_cursor%ISOPEN THEN
                    CLOSE v_cursor;
                END IF;
                RAISE;
    END fn_get_all_active_vehicles;
    
END vehicle_pkg;
/

-- Grant execute permission to the application user
GRANT EXECUTE ON vehicle_pkg TO APP_USER;

-- Optional: Test package procedures
-- BEGIN
--     vehicle_pkg.sp_reactivate_vehicle(1);
--     DBMS_OUTPUT.PUT_LINE('Vehicle reactivated successfully.');
-- EXCEPTION
--     WHEN OTHERS THEN
--         DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
-- END;
-- /
