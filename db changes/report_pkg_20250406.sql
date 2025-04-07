-- Create a view for revenue reports
CREATE OR REPLACE VIEW vw_revenue_report AS
SELECT 
    EXTRACT(YEAR FROM a.APPOINTMENT_DATE) AS year,
    EXTRACT(MONTH FROM a.APPOINTMENT_DATE) AS month,
    COUNT(a.APPOINTMENT_ID) AS total_appointments,
    COUNT(CASE WHEN a.STATUS_ID = 'C' THEN 1 END) AS completed_appointments,
    SUM(CASE WHEN a.STATUS_ID = 'C' THEN s.PRICE ELSE 0 END) AS total_revenue,
    ROUND(AVG(CASE WHEN a.STATUS_ID = 'C' THEN s.PRICE ELSE NULL END), 2) AS avg_service_price,
    st.TYPE_NAME AS service_type,
    COUNT(CASE WHEN a.STATUS_ID = 'C' AND s.SERVICE_TYPE_ID = st.SERVICE_TYPE_ID THEN 1 END) AS service_type_count
FROM 
    APPOINTMENT a
JOIN 
    SERVICE s ON a.SERVICE_ID = s.SERVICE_ID
JOIN 
    SERVICE_TYPE st ON s.SERVICE_TYPE_ID = st.SERVICE_TYPE_ID
GROUP BY 
    EXTRACT(YEAR FROM a.APPOINTMENT_DATE),
    EXTRACT(MONTH FROM a.APPOINTMENT_DATE),
    st.TYPE_NAME;

-- Create a view for invoice reports
CREATE OR REPLACE VIEW vw_invoice_report AS
SELECT 
    a.APPOINTMENT_ID AS invoice_number,
    a.APPOINTMENT_DATE AS invoice_date,
    c.NAME AS customer_name,
    c.PHONE AS customer_phone,
    c.EMAIL AS customer_email,
    v.MAKE || ' ' || v.MODEL || ' (' || v.YEAR || ')' AS vehicle_info,
    v.VIN AS vehicle_vin,
    s.SERVICE_NAME AS service_description,
    st.TYPE_NAME AS service_category,
    s.PRICE AS service_price,
    m.NAME AS mechanic_name,
    a.STATUS_ID AS status_id,
    CASE 
        WHEN a.STATUS_ID = 'C' THEN 'COMPLETED'
        WHEN a.STATUS_ID = 'S' THEN 'SCHEDULED'
        WHEN a.STATUS_ID = 'I' THEN 'IN PROGRESS'
        WHEN a.STATUS_ID = 'X' THEN 'CANCELED'
        ELSE 'UNKNOWN'
    END AS status_name
FROM 
    APPOINTMENT a
JOIN 
    CUSTOMER c ON a.CUSTOMER_ID = c.CUSTOMER_ID
JOIN 
    VEHICLE v ON a.VEHICLE_ID = v.VEHICLE_ID
JOIN 
    SERVICE s ON a.SERVICE_ID = s.SERVICE_ID
JOIN 
    SERVICE_TYPE st ON s.SERVICE_TYPE_ID = st.SERVICE_TYPE_ID
LEFT JOIN 
    MECHANIC m ON a.MECHANIC_ID = m.MECHANIC_ID;

-- Create report package
CREATE OR REPLACE PACKAGE report_pkg AS
    -- Revenue report - by year and month
    FUNCTION fn_revenue_report(
        p_year IN NUMBER,
        p_month IN NUMBER
    ) RETURN SYS_REFCURSOR;
    
    -- Revenue report - by date range
    FUNCTION fn_revenue_report_by_date_range(
        p_start_date IN DATE,
        p_end_date IN DATE
    ) RETURN SYS_REFCURSOR;
    
    -- Revenue report - by service type
    FUNCTION fn_revenue_report_by_service_type(
        p_service_type IN VARCHAR2,
        p_start_date IN DATE DEFAULT ADD_MONTHS(SYSDATE, -1),
        p_end_date IN DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR;
    
    -- Invoice report - by invoice number (appointment ID)
    FUNCTION fn_invoice_by_id(
        p_invoice_number IN NUMBER
    ) RETURN SYS_REFCURSOR;
    
    -- Invoice report - by customer
    FUNCTION fn_invoices_by_customer(
        p_customer_id IN NUMBER,
        p_start_date IN DATE DEFAULT ADD_MONTHS(SYSDATE, -6),
        p_end_date IN DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR;
    
    -- Invoice report - by status (e.g., completed invoices)
    FUNCTION fn_invoices_by_status(
        p_status IN CHAR,
        p_start_date IN DATE DEFAULT ADD_MONTHS(SYSDATE, -1),
        p_end_date IN DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR;
    
    -- Export revenue report to CSV
    PROCEDURE sp_export_revenue_report_to_csv(
        p_year IN NUMBER,
        p_month IN NUMBER,
        p_filename IN VARCHAR2,
        p_directory IN VARCHAR2,
        p_status OUT VARCHAR2
    );
    
    -- Export invoice report to CSV
    PROCEDURE sp_export_invoice_report_to_csv(
        p_start_date IN DATE,
        p_end_date IN DATE,
        p_status IN CHAR DEFAULT NULL,
        p_filename IN VARCHAR2,
        p_directory IN VARCHAR2,
        p_status_out OUT VARCHAR2
    );
END report_pkg;
/

CREATE OR REPLACE PACKAGE BODY report_pkg AS
    -- Revenue report - by year and month
    FUNCTION fn_revenue_report(
        p_year IN NUMBER,
        p_month IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT *
            FROM vw_revenue_report
            WHERE year = p_year AND month = p_month;
        
        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            IF v_result%ISOPEN THEN
                CLOSE v_result;
            END IF;
            RAISE;
    END fn_revenue_report;
    
    -- Revenue report - by date range
    FUNCTION fn_revenue_report_by_date_range(
        p_start_date IN DATE,
        p_end_date IN DATE
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT 
                EXTRACT(YEAR FROM a.APPOINTMENT_DATE) AS year,
                EXTRACT(MONTH FROM a.APPOINTMENT_DATE) AS month,
                COUNT(a.APPOINTMENT_ID) AS total_appointments,
                COUNT(CASE WHEN a.STATUS_ID = 'C' THEN 1 END) AS completed_appointments,
                SUM(CASE WHEN a.STATUS_ID = 'C' THEN s.PRICE ELSE 0 END) AS total_revenue,
                ROUND(AVG(CASE WHEN a.STATUS_ID = 'C' THEN s.PRICE ELSE NULL END), 2) AS avg_service_price,
                st.TYPE_NAME AS service_type,
                COUNT(CASE WHEN a.STATUS_ID = 'C' AND s.SERVICE_TYPE_ID = st.SERVICE_TYPE_ID THEN 1 END) AS service_type_count
            FROM 
                APPOINTMENT a
            JOIN 
                SERVICE s ON a.SERVICE_ID = s.SERVICE_ID
            JOIN 
                SERVICE_TYPE st ON s.SERVICE_TYPE_ID = st.SERVICE_TYPE_ID
            WHERE 
                a.APPOINTMENT_DATE BETWEEN p_start_date AND p_end_date
            GROUP BY 
                EXTRACT(YEAR FROM a.APPOINTMENT_DATE),
                EXTRACT(MONTH FROM a.APPOINTMENT_DATE),
                st.TYPE_NAME;
        
        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            IF v_result%ISOPEN THEN
                CLOSE v_result;
            END IF;
            RAISE;
    END fn_revenue_report_by_date_range;
    
    -- Revenue report - by service type
    FUNCTION fn_revenue_report_by_service_type(
        p_service_type IN VARCHAR2,
        p_start_date IN DATE DEFAULT ADD_MONTHS(SYSDATE, -1),
        p_end_date IN DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT 
                EXTRACT(YEAR FROM a.APPOINTMENT_DATE) AS year,
                EXTRACT(MONTH FROM a.APPOINTMENT_DATE) AS month,
                COUNT(a.APPOINTMENT_ID) AS total_appointments,
                COUNT(CASE WHEN a.STATUS_ID = 'C' THEN 1 END) AS completed_appointments,
                SUM(CASE WHEN a.STATUS_ID = 'C' THEN s.PRICE ELSE 0 END) AS total_revenue,
                ROUND(AVG(CASE WHEN a.STATUS_ID = 'C' THEN s.PRICE ELSE NULL END), 2) AS avg_service_price,
                st.TYPE_NAME AS service_type
            FROM 
                APPOINTMENT a
            JOIN 
                SERVICE s ON a.SERVICE_ID = s.SERVICE_ID
            JOIN 
                SERVICE_TYPE st ON s.SERVICE_TYPE_ID = st.SERVICE_TYPE_ID
            WHERE 
                a.APPOINTMENT_DATE BETWEEN p_start_date AND p_end_date
                AND st.TYPE_NAME = p_service_type
            GROUP BY 
                EXTRACT(YEAR FROM a.APPOINTMENT_DATE),
                EXTRACT(MONTH FROM a.APPOINTMENT_DATE),
                st.TYPE_NAME;
        
        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            IF v_result%ISOPEN THEN
                CLOSE v_result;
            END IF;
            RAISE;
    END fn_revenue_report_by_service_type;
    
    -- Invoice report - by invoice number (appointment ID)
    FUNCTION fn_invoice_by_id(
        p_invoice_number IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT *
            FROM vw_invoice_report
            WHERE invoice_number = p_invoice_number;
        
        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            IF v_result%ISOPEN THEN
                CLOSE v_result;
            END IF;
            RAISE;
    END fn_invoice_by_id;
    
    -- Invoice report - by customer
    FUNCTION fn_invoices_by_customer(
        p_customer_id IN NUMBER,
        p_start_date IN DATE DEFAULT ADD_MONTHS(SYSDATE, -6),
        p_end_date IN DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT ir.*
            FROM vw_invoice_report ir
            JOIN APPOINTMENT a ON ir.invoice_number = a.APPOINTMENT_ID
            WHERE a.CUSTOMER_ID = p_customer_id
            AND ir.invoice_date BETWEEN p_start_date AND p_end_date
            ORDER BY ir.invoice_date DESC;
        
        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            IF v_result%ISOPEN THEN
                CLOSE v_result;
            END IF;
            RAISE;
    END fn_invoices_by_customer;
    
    -- Invoice report - by status (e.g., completed invoices)
    FUNCTION fn_invoices_by_status(
        p_status IN CHAR,
        p_start_date IN DATE DEFAULT ADD_MONTHS(SYSDATE, -1),
        p_end_date IN DATE DEFAULT SYSDATE
    ) RETURN SYS_REFCURSOR IS
        v_result SYS_REFCURSOR;
    BEGIN
        OPEN v_result FOR
            SELECT *
            FROM vw_invoice_report
            WHERE status_id = p_status
            AND invoice_date BETWEEN p_start_date AND p_end_date
            ORDER BY invoice_date DESC;
        
        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            IF v_result%ISOPEN THEN
                CLOSE v_result;
            END IF;
            RAISE;
    END fn_invoices_by_status;
    
    -- Export revenue report to CSV
    PROCEDURE sp_export_revenue_report_to_csv(
        p_year IN NUMBER,
        p_month IN NUMBER,
        p_filename IN VARCHAR2,
        p_directory IN VARCHAR2,
        p_status OUT VARCHAR2
    ) IS
        v_file UTL_FILE.FILE_TYPE;
        v_cursor SYS_REFCURSOR;
        
        -- Cursor variables
        v_year NUMBER;
        v_month NUMBER;
        v_total_appointments NUMBER;
        v_completed_appointments NUMBER;
        v_total_revenue NUMBER;
        v_avg_service_price NUMBER;
        v_service_type VARCHAR2(255);
        v_service_type_count NUMBER;
    BEGIN
        -- Initialize status
        p_status := 'SUCCESS';
        
        -- Open file
        BEGIN
            v_file := UTL_FILE.FOPEN(p_directory, p_filename, 'W', 32767);
        EXCEPTION 
            WHEN OTHERS THEN
                p_status := 'ERROR: Could not open file - ' || SQLERRM;
                RETURN;
        END;
        
        -- Write headers
        BEGIN
            UTL_FILE.PUT_LINE(v_file, 'Year,Month,Total Appointments,Completed Appointments,Total Revenue,Average Service Price,Service Type,Service Type Count');
        EXCEPTION
            WHEN OTHERS THEN
                UTL_FILE.FCLOSE(v_file);
                p_status := 'ERROR: Could not write headers - ' || SQLERRM;
                RETURN;
        END;
        
        -- Get data
        v_cursor := fn_revenue_report(p_year, p_month);
        
        -- Write data
        BEGIN
            LOOP
                FETCH v_cursor INTO v_year, v_month, v_total_appointments, v_completed_appointments, 
                                   v_total_revenue, v_avg_service_price, v_service_type, v_service_type_count;
                EXIT WHEN v_cursor%NOTFOUND;
                
                UTL_FILE.PUT_LINE(v_file, 
                    v_year || ',' || 
                    v_month || ',' || 
                    v_total_appointments || ',' || 
                    v_completed_appointments || ',' || 
                    v_total_revenue || ',' || 
                    v_avg_service_price || ',' || 
                    '"' || v_service_type || '",' || 
                    v_service_type_count);
            END LOOP;
        EXCEPTION
            WHEN OTHERS THEN
                p_status := 'ERROR: Failed to write data - ' || SQLERRM;
        END;
        
        -- Close cursor and file
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;
        
        UTL_FILE.FCLOSE(v_file);
        
    EXCEPTION
        WHEN OTHERS THEN
            IF v_cursor%ISOPEN THEN
                CLOSE v_cursor;
            END IF;
            
            IF UTL_FILE.IS_OPEN(v_file) THEN
                UTL_FILE.FCLOSE(v_file);
            END IF;
            
            p_status := 'ERROR: ' || SQLERRM;
    END sp_export_revenue_report_to_csv;
    
    -- Export invoice report to CSV
    PROCEDURE sp_export_invoice_report_to_csv(
        p_start_date IN DATE,
        p_end_date IN DATE,
        p_status IN CHAR DEFAULT NULL,
        p_filename IN VARCHAR2,
        p_directory IN VARCHAR2,
        p_status_out OUT VARCHAR2
    ) IS
        v_file UTL_FILE.FILE_TYPE;
        v_cursor SYS_REFCURSOR;
        v_sql VARCHAR2(4000);
        
        -- Cursor variables
        v_invoice_number NUMBER;
        v_invoice_date DATE;
        v_customer_name VARCHAR2(255);
        v_customer_phone VARCHAR2(20);
        v_customer_email VARCHAR2(255);
        v_vehicle_info VARCHAR2(255);
        v_vehicle_vin VARCHAR2(17);
        v_service_description VARCHAR2(255);
        v_service_category VARCHAR2(255);
        v_service_price NUMBER;
        v_mechanic_name VARCHAR2(255);
        v_status_id CHAR(1);
        v_status_name VARCHAR2(50);
    BEGIN
        -- Initialize status
        p_status_out := 'SUCCESS';
        
        -- Open file
        BEGIN
            v_file := UTL_FILE.FOPEN(p_directory, p_filename, 'W', 32767);
        EXCEPTION 
            WHEN OTHERS THEN
                p_status_out := 'ERROR: Could not open file - ' || SQLERRM;
                RETURN;
        END;
        
        -- Write headers
        BEGIN
            UTL_FILE.PUT_LINE(v_file, 'Invoice Number,Invoice Date,Customer Name,Customer Phone,Customer Email,Vehicle Info,VIN,Service Description,Service Category,Service Price,Mechanic Name,Status');
        EXCEPTION
            WHEN OTHERS THEN
                UTL_FILE.FCLOSE(v_file);
                p_status_out := 'ERROR: Could not write headers - ' || SQLERRM;
                RETURN;
        END;
        
        -- Get data
        IF p_status IS NULL THEN
            OPEN v_cursor FOR 
                SELECT * 
                FROM vw_invoice_report 
                WHERE invoice_date BETWEEN p_start_date AND p_end_date
                ORDER BY invoice_date DESC;
        ELSE
            OPEN v_cursor FOR 
                SELECT * 
                FROM vw_invoice_report 
                WHERE invoice_date BETWEEN p_start_date AND p_end_date
                  AND status_id = p_status
                ORDER BY invoice_date DESC;
        END IF;
        
        -- Write data
        BEGIN
            LOOP
                FETCH v_cursor INTO 
                    v_invoice_number, v_invoice_date, v_customer_name, v_customer_phone,
                    v_customer_email, v_vehicle_info, v_vehicle_vin, v_service_description,
                    v_service_category, v_service_price, v_mechanic_name, v_status_id, v_status_name;
                EXIT WHEN v_cursor%NOTFOUND;
                
                UTL_FILE.PUT_LINE(v_file, 
                    v_invoice_number || ',' || 
                    TO_CHAR(v_invoice_date, 'YYYY-MM-DD HH24:MI:SS') || ',' || 
                    '"' || v_customer_name || '",' || 
                    '"' || v_customer_phone || '",' || 
                    '"' || v_customer_email || '",' || 
                    '"' || v_vehicle_info || '",' || 
                    '"' || v_vehicle_vin || '",' || 
                    '"' || v_service_description || '",' || 
                    '"' || v_service_category || '",' || 
                    v_service_price || ',' || 
                    '"' || NVL(v_mechanic_name, 'N/A') || '",' || 
                    '"' || v_status_name || '"');
            END LOOP;
        EXCEPTION
            WHEN OTHERS THEN
                p_status_out := 'ERROR: Failed to write data - ' || SQLERRM;
        END;
        
        -- Close cursor and file
        IF v_cursor%ISOPEN THEN
            CLOSE v_cursor;
        END IF;
        
        UTL_FILE.FCLOSE(v_file);
        
    EXCEPTION
        WHEN OTHERS THEN
            IF v_cursor%ISOPEN THEN
                CLOSE v_cursor;
            END IF;
            
            IF UTL_FILE.IS_OPEN(v_file) THEN
                UTL_FILE.FCLOSE(v_file);
            END IF;
            
            p_status_out := 'ERROR: ' || SQLERRM;
    END sp_export_invoice_report_to_csv;
END report_pkg;
/

-- Grant execute permissions
-- GRANT EXECUTE ON report_pkg TO APP_USER;
-- GRANT SELECT ON vw_revenue_report TO APP_USER;
-- GRANT SELECT ON vw_invoice_report TO APP_USER;

-- GRANT EXECUTE ON report_pkg TO PUBLIC;
-- GRANT SELECT ON vw_revenue_report TO PUBLIC;
-- GRANT SELECT ON vw_invoice_report TO PUBLIC;

-- Example usage:
/*
-- Get revenue report for March 2025
SELECT * FROM TABLE(report_pkg.fn_revenue_report(2025, 3));

-- Get revenue report for a specific date range
SELECT * FROM TABLE(report_pkg.fn_revenue_report_by_date_range(TO_DATE('2025-01-01', 'YYYY-MM-DD'), TO_DATE('2025-03-31', 'YYYY-MM-DD')));

-- Get revenue report for maintenance service type
SELECT * FROM TABLE(report_pkg.fn_revenue_report_by_service_type('Maintenance', TO_DATE('2025-01-01', 'YYYY-MM-DD'), TO_DATE('2025-03-31', 'YYYY-MM-DD')));

-- Get invoice by invoice number
SELECT * FROM TABLE(report_pkg.fn_invoice_by_id(123));

-- Export revenue report to CSV
DECLARE
    v_status VARCHAR2(4000);
BEGIN
    report_pkg.sp_export_revenue_report_to_csv(
        2025, 3, 'revenue_202503.csv', 'REPORTS_DIR', v_status
    );
    DBMS_OUTPUT.PUT_LINE(v_status);
END;
/

-- Export invoice report to CSV
DECLARE
    v_status VARCHAR2(4000);
BEGIN
    report_pkg.sp_export_invoice_report_to_csv(
        TO_DATE('2025-01-01', 'YYYY-MM-DD'),
        TO_DATE('2025-03-31', 'YYYY-MM-DD'),
        'C', -- Export only completed invoices
        'invoices_q1_2025.csv',
        'REPORTS_DIR',
        v_status
    );
    DBMS_OUTPUT.PUT_LINE(v_status);
END;
/