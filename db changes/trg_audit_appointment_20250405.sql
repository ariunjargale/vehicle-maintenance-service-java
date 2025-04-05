CREATE OR REPLACE TRIGGER trg_audit_appointment
AFTER INSERT OR UPDATE OR DELETE ON appointment
FOR EACH ROW
DECLARE
    v_audit_id      AUDIT_LOG.AUDIT_ID%TYPE;
    v_user_id       NUMBER;
    v_operation     VARCHAR2(10);
    v_primary_key   VARCHAR2(255);
BEGIN
    -- Get current user ID from session context
    v_user_id := NVL(SYS_CONTEXT('LOGIN_CTX', 'USER_ID'), 1);

    -- Determine the operation type and primary key
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_primary_key := TO_CHAR(:NEW.appointment_id);
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_primary_key := TO_CHAR(:OLD.appointment_id);
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_primary_key := TO_CHAR(:OLD.appointment_id);
    END IF;

    -- Insert into AUDIT_LOG
    INSERT INTO audit_log (
        table_name, operation_type, primary_key_value, performed_by, performed_at
    ) VALUES (
        'APPOINTMENT',
        v_operation,
        v_primary_key,
        v_user_id,
        SYSDATE
    )
    RETURNING audit_id INTO v_audit_id;

    -- INSERT
    IF INSERTING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'CUSTOMER_ID', TO_CHAR(:NEW.customer_id));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'VEHICLE_ID', TO_CHAR(:NEW.vehicle_id));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'SERVICE_ID', TO_CHAR(:NEW.service_id));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'MECHANIC_ID', TO_CHAR(:NEW.mechanic_id));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'APPOINTMENT_DATE', TO_CHAR(:NEW.appointment_date, 'YYYY-MM-DD HH24:MI:SS'));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'STATUS_ID', :NEW.status_id);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:NEW.is_active));
    END IF;

    -- UPDATE
    IF UPDATING THEN
        IF :OLD.customer_id != :NEW.customer_id THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'CUSTOMER_ID', TO_CHAR(:OLD.customer_id), TO_CHAR(:NEW.customer_id));
        END IF;

        IF :OLD.vehicle_id != :NEW.vehicle_id THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'VEHICLE_ID', TO_CHAR(:OLD.vehicle_id), TO_CHAR(:NEW.vehicle_id));
        END IF;

        IF :OLD.service_id != :NEW.service_id THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'SERVICE_ID', TO_CHAR(:OLD.service_id), TO_CHAR(:NEW.service_id));
        END IF;

        IF :OLD.mechanic_id != :NEW.mechanic_id THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'MECHANIC_ID', TO_CHAR(:OLD.mechanic_id), TO_CHAR(:NEW.mechanic_id));
        END IF;

        IF :OLD.appointment_date != :NEW.appointment_date THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (
                v_audit_id,
                'APPOINTMENT_DATE',
                TO_CHAR(:OLD.appointment_date, 'YYYY-MM-DD HH24:MI:SS'),
                TO_CHAR(:NEW.appointment_date, 'YYYY-MM-DD HH24:MI:SS')
            );
        END IF;

        IF :OLD.status_id != :NEW.status_id THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'STATUS_ID', :OLD.status_id, :NEW.status_id);
        END IF;

        IF :OLD.is_active != :NEW.is_active THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active), TO_CHAR(:NEW.is_active));
        END IF;
    END IF;

    -- DELETE
    IF DELETING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'CUSTOMER_ID', TO_CHAR(:OLD.customer_id));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'VEHICLE_ID', TO_CHAR(:OLD.vehicle_id));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'SERVICE_ID', TO_CHAR(:OLD.service_id));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'MECHANIC_ID', TO_CHAR(:OLD.mechanic_id));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'APPOINTMENT_DATE', TO_CHAR(:OLD.appointment_date, 'YYYY-MM-DD HH24:MI:SS'));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'STATUS_ID', :OLD.status_id);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active));
    END IF;
END;
/
