CREATE OR REPLACE TRIGGER trg_check_permission_service
BEFORE INSERT OR UPDATE OR DELETE ON service
FOR EACH ROW
DECLARE
    v_user_id     NUMBER;
    v_role_id     NUMBER;
    v_table_name  VARCHAR2(100) := 'SERVICE';
    v_has_access  NUMBER;
    v_is_insert   BOOLEAN := INSERTING;
    v_is_update   BOOLEAN := UPDATING;
    v_is_delete   BOOLEAN := DELETING;
BEGIN
    v_user_id := NVL(SYS_CONTEXT('LOGIN_CTX', 'USER_ID'), NULL);
    IF v_user_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'User ID not found in session.');
    END IF;

    SELECT role_id INTO v_role_id FROM users WHERE user_id = v_user_id;

    IF v_is_insert OR v_is_update OR v_is_delete THEN
        SELECT COUNT(*) INTO v_has_access
        FROM role_permission
        WHERE role_id = v_role_id
          AND UPPER(table_name) = v_table_name
          AND is_read_only = 0;
    ELSE
        v_has_access := 0;
    END IF;

    IF v_has_access = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Permission denied for ' || v_table_name || '.');
    END IF;
END;
/
