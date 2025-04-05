CREATE OR REPLACE TRIGGER trg_audit_vehicle
AFTER INSERT OR UPDATE OR DELETE ON vehicle
FOR EACH ROW
DECLARE
    v_audit_id    AUDIT_LOG.AUDIT_ID%TYPE;
    v_user_id     NUMBER;
    v_operation   VARCHAR2(10);
    v_primary_key VARCHAR2(255);
BEGIN
    -- Get user ID from session context
    v_user_id := NVL(SYS_CONTEXT('LOGIN_CTX', 'USER_ID'), 1);

    -- Determine operation type
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_primary_key := TO_CHAR(:NEW.vehicle_id);
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_primary_key := TO_CHAR(:OLD.vehicle_id);
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_primary_key := TO_CHAR(:OLD.vehicle_id);
    END IF;

    -- Insert audit header
    INSERT INTO audit_log (
        table_name, operation_type, primary_key_value, performed_by, performed_at
    ) VALUES (
        'VEHICLE',
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
        VALUES (v_audit_id, 'MAKE', :NEW.make);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'MODEL', :NEW.model);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'YEAR', TO_CHAR(:NEW.year));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'VIN', :NEW.vin);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:NEW.is_active));
    END IF;

    -- UPDATE
    IF UPDATING THEN
        IF :OLD.customer_id != :NEW.customer_id THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'CUSTOMER_ID', TO_CHAR(:OLD.customer_id), TO_CHAR(:NEW.customer_id));
        END IF;

        IF :OLD.make != :NEW.make THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'MAKE', :OLD.make, :NEW.make);
        END IF;

        IF :OLD.model != :NEW.model THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'MODEL', :OLD.model, :NEW.model);
        END IF;

        IF :OLD.year != :NEW.year THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'YEAR', TO_CHAR(:OLD.year), TO_CHAR(:NEW.year));
        END IF;

        IF :OLD.vin != :NEW.vin THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'VIN', :OLD.vin, :NEW.vin);
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
        VALUES (v_audit_id, 'MAKE', :OLD.make);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'MODEL', :OLD.model);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'YEAR', TO_CHAR(:OLD.year));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'VIN', :OLD.vin);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active));
    END IF;
END;
/
