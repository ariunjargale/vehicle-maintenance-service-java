CREATE OR REPLACE TRIGGER trg_audit_inventory
AFTER INSERT OR UPDATE OR DELETE ON inventory
FOR EACH ROW
DECLARE
    v_audit_id    AUDIT_LOG.AUDIT_ID%TYPE;
    v_user_id     NUMBER;
    v_operation   VARCHAR2(10);
    v_primary_key VARCHAR2(255);
BEGIN
    -- Get user ID from session context
    v_user_id := NVL(SYS_CONTEXT('LOGIN_CTX', 'USER_ID'), 1);

    -- Determine operation type and primary key value
    IF INSERTING THEN
        v_operation := 'INSERT';
        v_primary_key := TO_CHAR(:NEW.item_id);
    ELSIF UPDATING THEN
        v_operation := 'UPDATE';
        v_primary_key := TO_CHAR(:OLD.item_id);
    ELSIF DELETING THEN
        v_operation := 'DELETE';
        v_primary_key := TO_CHAR(:OLD.item_id);
    END IF;

    -- Insert into AUDIT_LOG
    INSERT INTO audit_log (
        table_name, operation_type, primary_key_value, performed_by, performed_at
    ) VALUES (
        'INVENTORY',
        v_operation,
        v_primary_key,
        v_user_id,
        SYSDATE
    )
    RETURNING audit_id INTO v_audit_id;

    -- INSERT
    IF INSERTING THEN
        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'ITEM_NAME', :NEW.item_name);

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'QUANTITY', TO_CHAR(:NEW.quantity));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'PRICE', TO_CHAR(:NEW.price));

        INSERT INTO audit_log_detail (audit_id, column_name, new_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:NEW.is_active));
    END IF;

    -- UPDATE
    IF UPDATING THEN
        IF :OLD.item_name != :NEW.item_name THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'ITEM_NAME', :OLD.item_name, :NEW.item_name);
        END IF;

        IF :OLD.quantity != :NEW.quantity THEN
            INSERT INTO audit_log_detail (audit_id, column_name, old_value, new_value)
            VALUES (v_audit_id, 'QUANTITY', TO_CHAR(:OLD.quantity), TO_CHAR(:NEW.quantity));
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
        VALUES (v_audit_id, 'ITEM_NAME', :OLD.item_name);

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'QUANTITY', TO_CHAR(:OLD.quantity));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'PRICE', TO_CHAR(:OLD.price));

        INSERT INTO audit_log_detail (audit_id, column_name, old_value)
        VALUES (v_audit_id, 'IS_ACTIVE', TO_CHAR(:OLD.is_active));
    END IF;
END;
/
