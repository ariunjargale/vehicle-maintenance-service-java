-- vehicle_pkg_20250323.sql
-- Created by: Samuel Law
-- Date: 2025-03-23
-- Description: Package for Vehicle Management
-- Includes: CRUD operations for vehicles

CREATE OR REPLACE PACKAGE vehicle_pkg AS
    -- Add a new vehicle
    PROCEDURE sp_add_vehicle(
        p_customer_id IN NUMBER,
        p_make IN VARCHAR2,
        p_model IN VARCHAR2,
        p_year IN NUMBER,
        p_vin IN VARCHAR2,
        p_vehicle_id OUT NUMBER
    );
    
    -- Update vehicle information
    PROCEDURE sp_update_vehicle(
        p_vehicle_id IN NUMBER,
        p_make IN VARCHAR2,
        p_model IN VARCHAR2,
        p_year IN NUMBER,
        p_vin IN VARCHAR2
    );
    
    -- Change vehicle owner
    PROCEDURE sp_change_vehicle_owner(
        p_vehicle_id IN NUMBER,
        p_new_customer_id IN NUMBER
    );
    
    -- Soft delete vehicle (mark as inactive)
    PROCEDURE sp_deactivate_vehicle(
        p_vehicle_id IN NUMBER
    );
    
    -- Reactivate vehicle
    PROCEDURE sp_reactivate_vehicle(
        p_vehicle_id IN NUMBER
    );
    
    -- Hard delete vehicle (permanently remove from database)
    PROCEDURE sp_delete_vehicle(
        p_vehicle_id IN NUMBER
    );
    
    -- Query vehicles by customer ID
    FUNCTION fn_get_vehicles_by_customer(
        p_customer_id IN NUMBER
    ) RETURN SYS_REFCURSOR;
    
    -- Query vehicle by VIN
    FUNCTION fn_get_vehicle_by_vin(
        p_vin IN VARCHAR2
    ) RETURN SYS_REFCURSOR;
    
    -- Query vehicle by ID
    FUNCTION fn_get_vehicle_by_id(
        p_vehicle_id IN NUMBER
    ) RETURN SYS_REFCURSOR;
    
    -- Search vehicles (by multiple criteria)
    FUNCTION fn_search_vehicles(
        p_search_term IN VARCHAR2
    ) RETURN SYS_REFCURSOR;
    
    -- Get all active vehicles
    FUNCTION fn_get_all_active_vehicles RETURN SYS_REFCURSOR;
END vehicle_pkg;
/
