CREATE OR REPLACE TRIGGER trg_audit_service
AFTER INSERT OR UPDATE OR DELETE ON service
FOR EACH ROW
DECLARE
    v_audit_id    AUDIT_LOG.AUDIT_ID%TYPE;
    v_user_id     NUMBER;
    v_operation   VARCHAR2(10);
    v_primary_key VARCHAR2(255);
BEGIN
    -- Get user ID from session context
    v_user_id := NVL(SYS_CONTEXT('LOGIN_CTX', 'USER_ID'), 1);

    -- Determine operation type and key
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_primary_key := TO_CHAR(:NEW.service_id);
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_primary_key := TO_CHAR(:OLD.service_id);
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_primary_key := TO_CHAR(:OLD.service_id);
    END IF;

    -- Insert into AUDIT_LOG
    INSERT INTO audit_log (
        table_name, operation_type, primary_key_value, performed_by, performed_at
    ) VALUES (
        'SERVICE',
        v_operation,
        v_primary_key,
        v_user_id,
        SYSDATE
    )
    RETURNING audit_id INTO v_audit_id;

    -- INSERT
    IF INSERTING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'SERVICE_NAME', :NEW.service_name);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'SERVICE_TYPE_ID', :NEW.service_type_id);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'PRICE', TO_CHAR(:NEW.price));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:NEW.is_active));
    END IF;

    -- UPDATE
    IF UPDATING THEN
        IF :OLD.service_name != :NEW.service_name THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'SERVICE_NAME', :OLD.service_name, :NEW.service_name);
        END IF;

        IF :OLD.service_type_id != :NEW.service_type_id THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'SERVICE_TYPE_ID', :OLD.service_type_id, :NEW.service_type_id);
        END IF;

        IF :OLD.price != :NEW.price THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'PRICE', TO_CHAR(:OLD.price), TO_CHAR(:NEW.price));
        END IF;

        IF :OLD.is_active != :NEW.is_active THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active), TO_CHAR(:NEW.is_active));
        END IF;
    END IF;

    -- DELETE
    IF DELETING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'SERVICE_NAME', :OLD.service_name);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'SERVICE_TYPE_ID', :OLD.service_type_id);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'PRICE', TO_CHAR(:OLD.price));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active));
    END IF;
END;
/
