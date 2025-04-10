/* Inventory Row Type*/
CREATE OR REPLACE TYPE inventory_row_type AS OBJECT (
        item_id   NUMBER,
        item_name VARCHAR2(255),
        quantity  NUMBER,
        price     NUMBER(10, 2),
        is_active NUMBER(1)
);
/

/* Inventory Table Type (collection of rows)*/
CREATE OR REPLACE TYPE inventory_table_type AS
    TABLE OF inventory_row_type;
/

CREATE OR REPLACE PACKAGE inventory_pkg AS

    /* Function to get one item by ID*/
    FUNCTION fn_get_inventory (
        p_item_id IN NUMBER
    ) RETURN inventory_table_type;

    /* Function to get all items*/
    FUNCTION fn_get_all_inventory RETURN inventory_table_type;

    /* Function to search by filter*/
    FUNCTION fn_search_inventory (
        p_filter_value IN VARCHAR2
    ) RETURN inventory_table_type;

    /* Function to get total count of items*/
    FUNCTION fn_get_inventory_count RETURN NUMBER;

    /* Procedure to create an item (full fields)*/
    PROCEDURE sp_create_inventory (
        p_item_name IN VARCHAR2,
        p_quantity  IN NUMBER,
        p_price     IN NUMBER
    );

    /* Procedure to update an item*/
    PROCEDURE sp_update_inventory (
        p_item_id   IN NUMBER,
        p_item_name IN VARCHAR2,
        p_quantity  IN NUMBER,
        p_price     IN NUMBER
    );

    /* Soft delete (is_active = 0)*/
    PROCEDURE sp_delete_inventory (
        p_item_id IN NUMBER
    );
    
    FUNCTION get_low_stocks (
        p_threshold IN NUMBER
    ) RETURN inventory_table_type;

END inventory_pkg;
/

CREATE OR REPLACE PACKAGE BODY inventory_pkg AS

    FUNCTION fn_get_inventory (
        p_item_id IN NUMBER
    ) RETURN inventory_table_type IS
        v_result inventory_table_type := inventory_table_type();
    BEGIN
        SELECT
            inventory_row_type(item_id, item_name, quantity, price, is_active)
        BULK COLLECT
        INTO v_result
        FROM
            inventory
        WHERE
                item_id = p_item_id
            AND is_active = 1;

        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN inventory_table_type();
    END fn_get_inventory;

    FUNCTION fn_get_all_inventory RETURN inventory_table_type IS
        v_result inventory_table_type := inventory_table_type();
    BEGIN
        SELECT
            inventory_row_type(item_id, item_name, quantity, price, is_active)
        BULK COLLECT
        INTO v_result
        FROM
            inventory
        WHERE
            is_active = 1;

        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN inventory_table_type();
    END fn_get_all_inventory;

    FUNCTION fn_search_inventory (
        p_filter_value IN VARCHAR2
    ) RETURN inventory_table_type IS
        v_result inventory_table_type := inventory_table_type();
    BEGIN
        SELECT
            inventory_row_type(item_id, item_name, quantity, price, is_active)
        BULK COLLECT
        INTO v_result
        FROM
            inventory
        WHERE
                is_active = 1
            AND ( lower(item_name) LIKE lower('%' || p_filter_value || '%')
                  OR to_char(item_id) LIKE '%' || p_filter_value || '%'
                  OR to_char(quantity) LIKE '%' || p_filter_value || '%'
                  OR to_char(price) LIKE '%' || p_filter_value || '%' );

        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN inventory_table_type();
    END fn_search_inventory;

    FUNCTION fn_get_inventory_count RETURN NUMBER IS
        v_count NUMBER := 0;
    BEGIN
        SELECT
            COUNT(*)
        INTO v_count
        FROM
            inventory
        WHERE
            is_active = 1;

        RETURN v_count;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN 0;
    END fn_get_inventory_count;

    PROCEDURE sp_create_inventory (
        p_item_name IN VARCHAR2,
        p_quantity  IN NUMBER,
        p_price     IN NUMBER
    ) IS
    BEGIN
        INSERT INTO inventory (
            item_name,
            quantity,
            price
        ) VALUES (
            p_item_name,
            p_quantity,
            p_price
        );

    EXCEPTION
        WHEN OTHERS THEN
            NULL;
    END sp_create_inventory;

    PROCEDURE sp_update_inventory (
        p_item_id   IN NUMBER,
        p_item_name IN VARCHAR2,
        p_quantity  IN NUMBER,
        p_price     IN NUMBER
    ) IS
    BEGIN
        UPDATE inventory
        SET
            item_name = p_item_name,
            quantity = p_quantity,
            price = p_price
        WHERE
            item_id = p_item_id;

    EXCEPTION
        WHEN OTHERS THEN
            NULL;
    END sp_update_inventory;

    PROCEDURE sp_delete_inventory (
        p_item_id IN NUMBER
    ) IS
    BEGIN
        UPDATE inventory
        SET
            is_active = 0
        WHERE
            item_id = p_item_id;

    EXCEPTION
        WHEN OTHERS THEN
            NULL;
    END sp_delete_inventory;
    
    FUNCTION get_low_stocks (
        p_threshold IN NUMBER
    ) RETURN inventory_table_type IS
        v_result inventory_table_type := inventory_table_type();
    BEGIN
        SELECT
            inventory_row_type(item_id, item_name, quantity, price, is_active)
        BULK COLLECT
        INTO v_result
        FROM
            inventory
        WHERE is_active = 1
            AND quantity <= p_threshold;

        RETURN v_result;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN inventory_table_type();
    END get_low_stocks;

END inventory_pkg;
/