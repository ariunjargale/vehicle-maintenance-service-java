CREATE OR REPLACE TRIGGER trg_check_permission_users
FOR INSERT OR UPDATE OR DELETE ON users
COMPOUND TRIGGER
    v_user_id     NUMBER;
    v_role_id     NUMBER;
    v_table_name  CONSTANT VARCHAR2(100) := 'USERS';
    v_has_access  NUMBER;
BEFORE STATEMENT IS
BEGIN
    -- Get performing user id
    v_user_id := SYS_CONTEXT('LOGIN_CTX', 'USER_ID');
    IF v_user_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'User ID not found in session.');
    END IF;

    -- Performing user_role
    SELECT role_id INTO v_role_id FROM users WHERE user_id = v_user_id;

    -- Checking permission
    SELECT COUNT(*) INTO v_has_access
    FROM role_permission
    WHERE role_id = v_role_id
      AND UPPER(table_name) = v_table_name
      AND is_read_only = 0;

    IF v_has_access = 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Permission denied for ' || v_table_name || '.');
    END IF;
END BEFORE STATEMENT;

END trg_check_permission_users;