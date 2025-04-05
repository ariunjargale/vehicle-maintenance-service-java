CREATE OR REPLACE TRIGGER trg_audit_customer
AFTER INSERT OR UPDATE OR DELETE ON customer
FOR EACH ROW
DECLARE
    v_audit_id    AUDIT_LOG.AUDIT_ID%TYPE;
    v_user_id     NUMBER;
    v_operation   VARCHAR2(10);
    v_primary_key VARCHAR2(255);
BEGIN
    -- Get current user ID from session context
    v_user_id := NVL(SYS_CONTEXT('LOGIN_CTX', 'USER_ID'), 1);

    -- Determine operation type and primary key
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_primary_key := TO_CHAR(:NEW.customer_id);
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_primary_key := TO_CHAR(:OLD.customer_id);
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_primary_key := TO_CHAR(:OLD.customer_id);
    END IF;

    -- Insert into AUDIT_LOG
    INSERT INTO audit_log (
        table_name, operation_type, primary_key_value, performed_by, performed_at
    ) VALUES (
        'CUSTOMER',
        v_operation,
        v_primary_key,
        v_user_id,
        SYSDATE  -- Using SYSDATE as requested
    )
    RETURNING audit_id INTO v_audit_id;

    -- INSERT
    IF INSERTING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'NAME', :NEW.name);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'PHONE', :NEW.phone);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'EMAIL', :NEW.email);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:NEW.is_active));
    END IF;

    -- UPDATE
    IF UPDATING THEN
        IF :OLD.name != :NEW.name THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'NAME', :OLD.name, :NEW.name);
        END IF;

        IF :OLD.phone != :NEW.phone THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'PHONE', :OLD.phone, :NEW.phone);
        END IF;

        IF :OLD.email != :NEW.email THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'EMAIL', :OLD.email, :NEW.email);
        END IF;

        IF :OLD.is_active != :NEW.is_active THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active), TO_CHAR(:NEW.is_active));
        END IF;
    END IF;

    -- DELETE
    IF DELETING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'NAME', :OLD.name);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'PHONE', :OLD.phone);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'EMAIL', :OLD.email);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active));
    END IF;
END;
/
