CREATE OR REPLACE TRIGGER trg_audit_user_role
AFTER INSERT OR UPDATE OR DELETE ON user_role
FOR EACH ROW
DECLARE
    v_audit_id      AUDIT_LOG.AUDIT_ID%TYPE;
    v_user_id       NUMBER;
    v_operation     VARCHAR2(10);
    v_primary_key   VARCHAR2(255);
BEGIN
    -- Get user ID from session context
    v_user_id := NVL(SYS_CONTEXT('LOGIN_CTX', 'USER_ID'), 1);

    -- Determine operation type and primary key
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_primary_key := TO_CHAR(:NEW.role_id);
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_primary_key := TO_CHAR(:OLD.role_id);
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_primary_key := TO_CHAR(:OLD.role_id);
    END IF;

    -- Insert audit log header
    INSERT INTO audit_log (
        table_name, operation_type, primary_key_value, performed_by, performed_at
    ) VALUES (
        'USER_ROLE',
        v_operation,
        v_primary_key,
        v_user_id,
        SYSDATE
    )
    RETURNING audit_id INTO v_audit_id;

    -- INSERT details
    IF INSERTING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'ROLE_NAME', :NEW.role_name);
    END IF;

    -- UPDATE details
    IF UPDATING THEN
        IF :OLD.role_name != :NEW.role_name THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'ROLE_NAME', :OLD.role_name, :NEW.role_name);
        END IF;
    END IF;

    -- DELETE details
    IF DELETING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'ROLE_NAME', :OLD.role_name);
    END IF;
END;
/
