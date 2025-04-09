CREATE OR REPLACE PACKAGE role_pkg AS

    -- Get all roles
    FUNCTION get_roles RETURN SYS_REFCURSOR;
    
    -- Get roles by name
	FUNCTION get_roles_by_name (
	    p_rolename IN VARCHAR2
	) RETURN SYS_REFCURSOR;

    -- Get role by ID
    FUNCTION get_role_by_id(p_role_id IN NUMBER) RETURN SYS_REFCURSOR;
	

    -- Insert role
    PROCEDURE insert_role(p_role_name IN VARCHAR2);

    -- Update role
    PROCEDURE update_role(p_role_id IN NUMBER, p_role_name IN VARCHAR2);

    -- Delete role
    PROCEDURE delete_role(p_role_id IN NUMBER);

    -- Get permissions of a role
    FUNCTION get_permissions_by_role(p_role_id IN NUMBER) RETURN SYS_REFCURSOR;
    
    -- Get permissions by name
	FUNCTION get_permissions_by_name (
		p_role_id IN NUMBER,
	    p_tablename IN VARCHAR2
	) RETURN SYS_REFCURSOR;


    -- Insert permission for a role
    PROCEDURE insert_permission(
        p_role_id      IN NUMBER,
        p_table_name   IN VARCHAR2,
        p_is_read_only IN NUMBER
    );

    -- Update permission
    PROCEDURE update_permission(
        p_permission_id IN NUMBER,
        p_table_name    IN VARCHAR2,
        p_is_read_only  IN NUMBER
    );

    -- Delete permission
    PROCEDURE delete_permission(p_permission_id IN NUMBER);

END role_pkg;
/

CREATE OR REPLACE PACKAGE BODY role_pkg AS

    ----------------------------------------------------------------
    -- Get all roles
    ----------------------------------------------------------------
    FUNCTION get_roles RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT role_id, role_name FROM user_role ORDER BY role_id;
        RETURN v_cursor;
    END;
    
    ----------------------------------------------------------------
	-- Get roles by name
	----------------------------------------------------------------
	FUNCTION get_roles_by_name (
	    p_rolename IN VARCHAR2
	) RETURN SYS_REFCURSOR IS
	    v_cursor SYS_REFCURSOR;
	BEGIN
	    OPEN v_cursor FOR
	        SELECT role_id, role_name
	        FROM user_role
	        WHERE LOWER(role_name) LIKE LOWER(p_rolename);
	
	    RETURN v_cursor;
	END;

    ----------------------------------------------------------------
    -- Get role by ID
    ----------------------------------------------------------------
    FUNCTION get_role_by_id(p_role_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT role_id, role_name FROM user_role WHERE role_id = p_role_id;
        RETURN v_cursor;
    END;

    ----------------------------------------------------------------
    -- Insert new role
    ----------------------------------------------------------------
    PROCEDURE insert_role(p_role_name IN VARCHAR2) IS
    BEGIN
        INSERT INTO user_role(role_name)
        VALUES (p_role_name);
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Update role name
    ----------------------------------------------------------------
    PROCEDURE update_role(p_role_id IN NUMBER, p_role_name IN VARCHAR2) IS
    BEGIN
        UPDATE user_role
        SET role_name = p_role_name
        WHERE role_id = p_role_id;
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Delete role (Only if no users are assigned)
    ----------------------------------------------------------------
    PROCEDURE delete_role(p_role_id IN NUMBER) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM users WHERE role_id = p_role_id;

        IF v_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20010, 'Cannot delete role: assigned to users.');
        END IF;

        DELETE FROM user_role WHERE role_id = p_role_id;
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Get role permissions
    ----------------------------------------------------------------
    FUNCTION get_permissions_by_role(p_role_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT permission_id, table_name, is_read_only
            FROM role_permission
            WHERE role_id = p_role_id
            ORDER BY permission_id;
        RETURN v_cursor;
    END;
    
    ----------------------------------------------------------------
	-- Get role permissions by table name
	----------------------------------------------------------------
	FUNCTION get_permissions_by_name (
		p_role_id IN NUMBER,
	    p_tablename IN VARCHAR2
	) RETURN SYS_REFCURSOR IS
	    v_cursor SYS_REFCURSOR;
	BEGIN
	    OPEN v_cursor FOR
	        SELECT permission_id, table_name, is_read_only
	        FROM role_permission
	        WHERE role_id = p_role_id AND LOWER(table_name) LIKE LOWER(p_tablename)
			ORDER BY permission_id;
			
	    RETURN v_cursor;
	END;

    ----------------------------------------------------------------
    -- Insert permission
    ----------------------------------------------------------------
    PROCEDURE insert_permission(
        p_role_id      IN NUMBER,
        p_table_name   IN VARCHAR2,
        p_is_read_only IN NUMBER
    ) IS
    BEGIN
        INSERT INTO role_permission(role_id, table_name, is_read_only)
        VALUES (p_role_id, p_table_name, p_is_read_only);
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Update permission
    ----------------------------------------------------------------
    PROCEDURE update_permission(
        p_permission_id IN NUMBER,
        p_table_name    IN VARCHAR2,
        p_is_read_only  IN NUMBER
    ) IS
    BEGIN
        UPDATE role_permission
        SET table_name = p_table_name,
            is_read_only = p_is_read_only
        WHERE permission_id = p_permission_id;
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Delete permission
    ----------------------------------------------------------------
    PROCEDURE delete_permission(p_permission_id IN NUMBER) IS
    BEGIN
        DELETE FROM role_permission WHERE permission_id = p_permission_id;
        COMMIT;
    END;

END role_pkg;
/



