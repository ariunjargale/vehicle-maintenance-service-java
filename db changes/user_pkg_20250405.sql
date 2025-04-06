CREATE OR REPLACE PACKAGE user_pkg AS

    -- Login function
    FUNCTION login (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2
    ) RETURN SYS_REFCURSOR;
    
    -- Get role permissions
    FUNCTION get_role_permissions (
        p_role_id IN NUMBER
    ) RETURN SYS_REFCURSOR;
    
    -- Get all users
    FUNCTION get_users RETURN SYS_REFCURSOR;
    
    -- Get all user roles
    FUNCTION get_user_roles RETURN SYS_REFCURSOR;
    
    -- Get user by username
	FUNCTION get_user_by_username (
	    p_username IN VARCHAR2
	) RETURN SYS_REFCURSOR;
	
    -- Get user by ID
    FUNCTION get_user_by_id (
        p_user_id IN NUMBER
    ) RETURN SYS_REFCURSOR;

    -- Insert user
    PROCEDURE insert_user (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2,
        p_role_id  IN NUMBER
    );

    -- Update user
    PROCEDURE update_user (
        p_user_id     IN NUMBER,
        p_username    IN VARCHAR2,
        p_role_id     IN NUMBER
    );

    -- Reset password
    PROCEDURE reset_password (
        p_user_id      IN NUMBER,
        p_new_password IN VARCHAR2
    );

    -- Deactivate (soft delete)
    PROCEDURE deactivate_user (
        p_user_id IN NUMBER
    );


END user_pkg;
/


CREATE OR REPLACE PACKAGE BODY user_pkg AS

    ----------------------------------------------------------------
    -- Login
    ----------------------------------------------------------------
    FUNCTION login (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2
    ) RETURN SYS_REFCURSOR IS
        v_cursor   SYS_REFCURSOR;
        v_user_id  NUMBER;
    BEGIN
        SELECT u.user_id
        INTO v_user_id
        FROM users u
        WHERE u.username = p_username
          AND u.password = p_password
          AND u.is_active = 1;

        DBMS_SESSION.SET_CONTEXT('LOGIN_CTX', 'USER_ID', v_user_id);

        OPEN v_cursor FOR
            SELECT u.user_id, u.username, u.role_id
            FROM users u
            WHERE u.user_id = v_user_id;

        RETURN v_cursor;

    EXCEPTION
        WHEN no_data_found THEN
            RETURN NULL;
    END;
    
    ----------------------------------------------------------------
    -- Get role permissions
    ----------------------------------------------------------------
    FUNCTION get_role_permissions (
        p_role_id IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT table_name, is_read_only
            FROM role_permission
            WHERE role_id = p_role_id;
        RETURN v_cursor;
    END;
    
    ----------------------------------------------------------------
    -- Get all users
    ----------------------------------------------------------------
    FUNCTION get_users
    RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT user_id, username, role_id, is_active
            FROM users
            WHERE is_active = 1
            ORDER BY user_id;
        RETURN v_cursor;
    END;

    
    ----------------------------------------------------------------
    -- Get all user roles
    ----------------------------------------------------------------
    FUNCTION get_user_roles
    RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT role_id, role_name
            FROM user_role
            ORDER BY role_id;
        RETURN v_cursor;
    END;
    
	----------------------------------------------------------------
	-- Get user by username
	----------------------------------------------------------------
	FUNCTION get_user_by_username (
	    p_username IN VARCHAR2
	) RETURN SYS_REFCURSOR IS
	    v_cursor SYS_REFCURSOR;
	BEGIN
	    OPEN v_cursor FOR
	        SELECT user_id, username, role_id, is_active
	        FROM users
	        WHERE is_active = 1 AND LOWER(username) LIKE LOWER(p_username);
	
	    RETURN v_cursor;
	END;
	
	----------------------------------------------------------------
    -- Get user by ID
    ----------------------------------------------------------------
    FUNCTION get_user_by_id (
        p_user_id IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT user_id, username, role_id, is_active
            FROM users
            WHERE user_id = p_user_id;
        RETURN v_cursor;
    END;

    ----------------------------------------------------------------
    -- Insert User
    ----------------------------------------------------------------
    PROCEDURE insert_user (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2,
        p_role_id  IN NUMBER
    ) IS
    BEGIN
        INSERT INTO users (
            username, password, role_id, is_active
        ) VALUES (
            p_username, p_password, p_role_id, 1
        );
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Update User Info (except password)
    ----------------------------------------------------------------
    PROCEDURE update_user (
        p_user_id  IN NUMBER,
        p_username IN VARCHAR2,
        p_role_id  IN NUMBER
    ) IS
    BEGIN
        UPDATE users
        SET
            username  = p_username,
            role_id   = p_role_id
        WHERE
            user_id = p_user_id;
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Update Password only
    ----------------------------------------------------------------
    PROCEDURE reset_password (
        p_user_id      IN NUMBER,
        p_new_password IN VARCHAR2
    ) IS
    BEGIN
        UPDATE users
        SET password = p_new_password
        WHERE user_id = p_user_id;
        COMMIT;
    END;

    ----------------------------------------------------------------
    -- Deactivate (Soft Delete)
    ----------------------------------------------------------------
    PROCEDURE deactivate_user (
        p_user_id IN NUMBER
    ) IS
    BEGIN
        UPDATE users
        SET is_active = 0
        WHERE user_id = p_user_id;
        COMMIT;
    END;



    

END user_pkg;
/
