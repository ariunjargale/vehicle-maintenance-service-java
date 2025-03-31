CREATE OR REPLACE PACKAGE user_pkg AS
    FUNCTION login_user (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2
    ) RETURN SYS_REFCURSOR;

    PROCEDURE reset_password (
        p_user_id      IN NUMBER,
        p_new_password IN VARCHAR2
    );

    PROCEDURE create_user (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2,
        p_role_id  IN NUMBER
    );

    FUNCTION get_user_by_id (
        p_user_id IN NUMBER
    ) RETURN SYS_REFCURSOR;

    PROCEDURE deactivate_user (
        p_user_id IN NUMBER
    );

    FUNCTION get_role_permissions (
        p_role_id IN NUMBER
    ) RETURN SYS_REFCURSOR;

END user_pkg;
/

CREATE OR REPLACE PACKAGE BODY user_pkg AS

    FUNCTION login_user (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2
    ) RETURN SYS_REFCURSOR IS
        v_cursor  SYS_REFCURSOR;
        v_user_id NUMBER;
    BEGIN
        SELECT
            u.user_id
        INTO v_user_id
        FROM
            users u
        WHERE
                u.username = p_username
            AND u.password = p_password
            AND u.is_active = 1;

        dbms_session.set_context('LOGIN_CTX', 'USER_ID', v_user_id);
        OPEN v_cursor FOR SELECT
                                                u.user_id,
                                                u.username,
                                                u.role_id
                                            FROM
                                                users u
                          WHERE
                              u.user_id = v_user_id;

        RETURN v_cursor;
    EXCEPTION
        WHEN no_data_found THEN
            RETURN NULL;
    END;

    PROCEDURE reset_password (
        p_user_id      IN NUMBER,
        p_new_password IN VARCHAR2
    ) IS
    BEGIN
        UPDATE users
        SET
            password = p_new_password
        WHERE
            user_id = p_user_id;

        COMMIT;
    END;

    PROCEDURE create_user (
        p_username IN VARCHAR2,
        p_password IN VARCHAR2,
        p_role_id  IN NUMBER
    ) IS
    BEGIN
        INSERT INTO users (
            username,
            password,
            role_id,
            is_active
        ) VALUES (
            p_username,
            p_password,
            p_role_id,
            1
        );

        COMMIT;
    END;

    FUNCTION get_user_by_id (
        p_user_id IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR SELECT
                                                user_id,
                                                username,
                                                role_id,
                                                is_active
                                            FROM
                                                users
                          WHERE
                              user_id = p_user_id;

        RETURN v_cursor;
    END;

    PROCEDURE deactivate_user (
        p_user_id IN NUMBER
    ) IS
    BEGIN
        UPDATE users
        SET
            is_active = 0
        WHERE
            user_id = p_user_id;

        COMMIT;
    END;

    FUNCTION get_role_permissions (
        p_role_id IN NUMBER
    ) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR SELECT
                                                table_name,
                                                is_read_only
                                            FROM
                                                role_permission
                          WHERE
                              role_id = p_role_id;

        RETURN v_cursor;
    END;

END user_pkg;