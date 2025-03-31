CREATE OR REPLACE PACKAGE appointment_pkg AS
    -- Create a new appointment
    PROCEDURE sp_create_appointment(
        p_customer_id IN NUMBER, 
        p_vehicle_id IN NUMBER, 
        p_service_id IN NUMBER, 
        p_mechanic_id IN NUMBER, 
        p_appointment_date IN DATE, 
        p_status_id IN VARCHAR2
    );
    
    -- Update appointment status
    PROCEDURE sp_update_appointment_status(
        p_appointment_id IN NUMBER, 
        p_status_id IN VARCHAR2
    );
    
    -- Assign mechanic to appointment
    PROCEDURE sp_assign_mechanic(
        p_appointment_id IN NUMBER, 
        p_mechanic_id IN NUMBER
    );
    
    -- Soft delete an appointment
    PROCEDURE sp_delete_appointment(
        p_appointment_id IN NUMBER
    );
    
    -- Get available appointment slots for a specific date
    FUNCTION sp_get_available_slots(
        p_date IN DATE
    ) RETURN SYS_REFCURSOR;
    
END appointment_pkg;
/

CREATE OR REPLACE PACKAGE BODY appointment_pkg AS
    -- Create a new appointment
    PROCEDURE sp_create_appointment(
        p_customer_id IN NUMBER, 
        p_vehicle_id IN NUMBER, 
        p_service_id IN NUMBER, 
        p_mechanic_id IN NUMBER, 
        p_appointment_date IN DATE, 
        p_status_id IN VARCHAR2
    ) IS
    BEGIN
        INSERT INTO APPOINTMENT (
            CUSTOMER_ID, 
            VEHICLE_ID, 
            SERVICE_ID, 
            MECHANIC_ID, 
            APPOINTMENT_DATE, 
            STATUS_ID,
            IS_ACTIVE
        ) VALUES (
            p_customer_id, 
            p_vehicle_id, 
            p_service_id, 
            p_mechanic_id, 
            p_appointment_date, 
            p_status_id,
            1
        );
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_create_appointment;
    
    -- Update appointment status
    PROCEDURE sp_update_appointment_status(
        p_appointment_id IN NUMBER, 
        p_status_id IN VARCHAR2
    ) IS
    BEGIN
        UPDATE APPOINTMENT
        SET STATUS_ID = p_status_id
        WHERE APPOINTMENT_ID = p_appointment_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_update_appointment_status;
    
    -- Assign mechanic to appointment
    PROCEDURE sp_assign_mechanic(
        p_appointment_id IN NUMBER, 
        p_mechanic_id IN NUMBER
    ) IS
    BEGIN
        UPDATE APPOINTMENT
        SET MECHANIC_ID = p_mechanic_id
        WHERE APPOINTMENT_ID = p_appointment_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_assign_mechanic;
    
    -- Soft delete an appointment
    PROCEDURE sp_delete_appointment(
        p_appointment_id IN NUMBER
    ) IS
    BEGIN
        UPDATE APPOINTMENT
        SET IS_ACTIVE = 0
        WHERE APPOINTMENT_ID = p_appointment_id;
        
        COMMIT;
        
        EXCEPTION
            WHEN OTHERS THEN
                ROLLBACK;
                RAISE;
    END sp_delete_appointment;
    
    -- Get available appointment slots for a specific date
    FUNCTION sp_get_available_slots(
        p_date IN DATE
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
        v_day_of_week VARCHAR2(10);
    BEGIN
        -- Determine day of week (1=Sunday, 2=Monday, ..., 7=Saturday)
        SELECT TO_CHAR(p_date, 'D') INTO v_day_of_week FROM DUAL;
        
        -- Exclude Sundays (assuming garage closed on Sundays)
        IF v_day_of_week = 1 THEN
            OPEN v_result FOR
                SELECT 'CLOSED' AS STATUS FROM DUAL;
        ELSE
            -- Return available time slots (9AM to 5PM, hourly slots)
            -- Excluding slots that already have appointments
            OPEN v_result FOR
                WITH HOURS AS (
                    SELECT TO_DATE('09:00', 'HH24:MI') + (LEVEL-1)/24 AS SLOT_TIME
                    FROM DUAL
                    CONNECT BY LEVEL <= 9  -- 9 slots from 9AM to 5PM
                ),
                BUSY_SLOTS AS (
                    SELECT TRUNC(APPOINTMENT_DATE) AS APPT_DATE, 
                           TO_CHAR(APPOINTMENT_DATE, 'HH24:MI') AS APPT_TIME,
                           COUNT(*) AS NUM_APPOINTMENTS
                    FROM APPOINTMENT
                    WHERE TRUNC(APPOINTMENT_DATE) = TRUNC(p_date)
                    AND IS_ACTIVE = 1
                    GROUP BY TRUNC(APPOINTMENT_DATE), TO_CHAR(APPOINTMENT_DATE, 'HH24:MI')
                )
                SELECT 
                    TO_CHAR(SLOT_TIME, 'HH24:MI') AS TIME_SLOT,
                    CASE 
                        WHEN B.NUM_APPOINTMENTS >= 3 THEN 'BOOKED'  -- Assuming max 3 concurrent appointments
                        ELSE 'AVAILABLE' 
                    END AS STATUS
                FROM HOURS H
                LEFT JOIN BUSY_SLOTS B ON TO_CHAR(H.SLOT_TIME, 'HH24:MI') = B.APPT_TIME
                ORDER BY H.SLOT_TIME;
        END IF;
        
        RETURN v_result;
        
        EXCEPTION
            WHEN OTHERS THEN
                RAISE;
    END sp_get_available_slots;
    
END appointment_pkg;
/

-- Grant execute permission to the application user
GRANT EXECUTE ON appointment_pkg TO APP_USER;

-- Test the package (optional)
-- DECLARE
--     v_cursor SYS_REFCURSOR;
--     v_time VARCHAR2(10);
--     v_status VARCHAR2(20);
-- BEGIN
--     v_cursor := appointment_pkg.sp_get_available_slots(SYSDATE);
--     LOOP
--         FETCH v_cursor INTO v_time, v_status;
--         EXIT WHEN v_cursor%NOTFOUND;
--         DBMS_OUTPUT.PUT_LINE('Time: ' || v_time || ' - Status: ' || v_status);
--     END LOOP;
--     CLOSE v_cursor;
-- END;
-- /

-- Example: Create test appointment
-- BEGIN
--     appointment_pkg.sp_create_appointment(1, 1, 1, 1, SYSDATE, 'P');
--     DBMS_OUTPUT.PUT_LINE('Appointment created successfully');
-- END;
-- /