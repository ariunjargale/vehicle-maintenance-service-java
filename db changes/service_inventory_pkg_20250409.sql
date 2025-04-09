CREATE OR REPLACE PACKAGE service_inventory_pkg AS
    -- Get all service inventory items
    FUNCTION fn_get_all_service_inventory RETURN SYS_REFCURSOR;

    -- Get detailed service inventory with joined service and item names
    FUNCTION fn_get_detailed_service_inventory RETURN SYS_REFCURSOR;

    -- Get a specific service inventory item
    FUNCTION fn_get_service_inventory(p_service_id IN NUMBER, p_item_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Search service inventory
    FUNCTION fn_search_service_inventory(p_search_term IN VARCHAR2) RETURN SYS_REFCURSOR;

    -- Create a new service inventory item
    PROCEDURE sp_create_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity_required IN NUMBER
    );

    -- Update an existing service inventory item
    PROCEDURE sp_update_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity_required IN NUMBER
    );

    -- Delete a service inventory item
    PROCEDURE sp_delete_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER
    );
END service_inventory_pkg;




CREATE OR REPLACE PACKAGE service_inventory_pkg AS
    -- Get all service inventory items
    FUNCTION fn_get_all_service_inventory RETURN SYS_REFCURSOR;

    -- Get detailed service inventory with joined service and item names
    FUNCTION fn_get_detailed_service_inventory RETURN SYS_REFCURSOR;

    -- Get a specific service inventory item
    FUNCTION fn_get_service_inventory(p_service_id IN NUMBER, p_item_id IN NUMBER) RETURN SYS_REFCURSOR;

    -- Search service inventory
    FUNCTION fn_search_service_inventory(p_search_term IN VARCHAR2) RETURN SYS_REFCURSOR;

    -- Create a new service inventory item
    PROCEDURE sp_create_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity_required IN NUMBER
    );

    -- Update an existing service inventory item
    PROCEDURE sp_update_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity_required IN NUMBER
    );

    -- Delete a service inventory item
    PROCEDURE sp_delete_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER
    );
END service_inventory_pkg;
/




CREATE OR REPLACE PACKAGE BODY service_inventory_pkg AS
    -- Get all service inventory items
    FUNCTION fn_get_all_service_inventory RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT service_id, item_id, quantity_required
            FROM service_inventory
            ORDER BY service_id, item_id;
        RETURN v_cursor;
    END fn_get_all_service_inventory;

    -- Get detailed service inventory with joined service and item names
    FUNCTION fn_get_detailed_service_inventory RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT si.service_id, si.item_id, si.quantity_required,
                   s.service_name, i.item_name
            FROM service_inventory si
            JOIN service s ON si.service_id = s.service_id
            JOIN inventory i ON si.item_id = i.item_id
            ORDER BY si.service_id, si.item_id;
        RETURN v_cursor;
    END fn_get_detailed_service_inventory;

    -- Get a specific service inventory item
    FUNCTION fn_get_service_inventory(p_service_id IN NUMBER, p_item_id IN NUMBER) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
    BEGIN
        OPEN v_cursor FOR
            SELECT si.service_id, si.item_id, si.quantity_required,
                   s.service_name, i.item_name
            FROM service_inventory si
            JOIN service s ON si.service_id = s.service_id
            JOIN inventory i ON si.item_id = i.item_id
            WHERE si.service_id = p_service_id
            AND si.item_id = p_item_id;
        RETURN v_cursor;
    END fn_get_service_inventory;

    -- Search service inventory
    FUNCTION fn_search_service_inventory(p_search_term IN VARCHAR2) RETURN SYS_REFCURSOR IS
        v_cursor SYS_REFCURSOR;
        v_search_pattern VARCHAR2(255) := '%' || LOWER(p_search_term) || '%';
    BEGIN
        OPEN v_cursor FOR
            SELECT si.service_id, si.item_id, si.quantity_required,
                   s.service_name, i.item_name
            FROM service_inventory si
            JOIN service s ON si.service_id = s.service_id
            JOIN inventory i ON si.item_id = i.item_id
            WHERE TO_CHAR(si.service_id) LIKE v_search_pattern
               OR TO_CHAR(si.item_id) LIKE v_search_pattern
               OR LOWER(s.service_name) LIKE v_search_pattern
               OR LOWER(i.item_name) LIKE v_search_pattern;
        RETURN v_cursor;
    END fn_search_service_inventory;

    -- Create a new service inventory item
    PROCEDURE sp_create_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity_required IN NUMBER
    ) IS
        v_existing_count NUMBER;
        v_service_count NUMBER;
        v_item_count NUMBER;
    BEGIN
        -- Check if the entry already exists
        SELECT COUNT(*)
        INTO v_existing_count
        FROM service_inventory
        WHERE service_id = p_service_id
        AND item_id = p_item_id;

        IF v_existing_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Service inventory item already exists');
        END IF;

        -- Verify that service exists
        SELECT COUNT(*)
        INTO v_service_count
        FROM service
        WHERE service_id = p_service_id;

        IF v_service_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20002, 'Service ID does not exist');
        END IF;

        -- Verify that item exists
        SELECT COUNT(*)
        INTO v_item_count
        FROM inventory
        WHERE item_id = p_item_id;

        IF v_item_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20003, 'Item ID does not exist');
        END IF;

        -- Insert the new entry
        INSERT INTO service_inventory (service_id, item_id, quantity_required)
        VALUES (p_service_id, p_item_id, p_quantity_required);

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END sp_create_service_inventory;

    -- Update an existing service inventory item
    PROCEDURE sp_update_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER,
        p_quantity_required IN NUMBER
    ) IS
        v_existing_count NUMBER;
    BEGIN
        -- Check if the entry exists
        SELECT COUNT(*)
        INTO v_existing_count
        FROM service_inventory
        WHERE service_id = p_service_id
        AND item_id = p_item_id;

        IF v_existing_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20004, 'Service inventory item does not exist');
        END IF;

        -- Update the entry
        UPDATE service_inventory
        SET quantity_required = p_quantity_required
        WHERE service_id = p_service_id
        AND item_id = p_item_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END sp_update_service_inventory;

    -- Delete a service inventory item
    PROCEDURE sp_delete_service_inventory(
        p_service_id IN NUMBER,
        p_item_id IN NUMBER
    ) IS
        v_existing_count NUMBER;
    BEGIN
        -- Check if the entry exists
        SELECT COUNT(*)
        INTO v_existing_count
        FROM service_inventory
        WHERE service_id = p_service_id
        AND item_id = p_item_id;

        IF v_existing_count = 0 THEN
            RAISE_APPLICATION_ERROR(-20004, 'Service inventory item does not exist');
        END IF;

        -- Delete the entry
        DELETE FROM service_inventory
        WHERE service_id = p_service_id
        AND item_id = p_item_id;

        COMMIT;
    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            RAISE;
    END sp_delete_service_inventory;
END service_inventory_pkg;
/





--Testing
-- 1. Add a new service inventory item
BEGIN
  service_inventory_pkg.sp_create_service_inventory(
    p_service_id => 1,        -- Replace with an existing service ID
    p_item_id => 6,           -- Replace with an existing item ID that's not already linked to this service
    p_quantity_required => 5
  );
  COMMIT;
  DBMS_OUTPUT.PUT_LINE('Service inventory item added successfully');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    ROLLBACK;
END;
/

-- 2. Verify the item was added
SELECT * FROM service_inventory
WHERE service_id = 1 AND item_id = 6;

-- 3. Update an existing item
BEGIN
  service_inventory_pkg.sp_update_service_inventory(
    p_service_id => 1,        -- Use the same IDs from your test insert
    p_item_id => 6,
    p_quantity_required => 5  -- Change the quantity
  );
  COMMIT;
  DBMS_OUTPUT.PUT_LINE('Service inventory item updated successfully');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    ROLLBACK;
END;
/

-- 4. Test the search function
DECLARE
  v_cursor SYS_REFCURSOR;
  v_service_id NUMBER;
  v_item_id NUMBER;
  v_quantity NUMBER;
  v_service_name VARCHAR2(100);
  v_item_name VARCHAR2(100);
BEGIN
  v_cursor := service_inventory_pkg.fn_search_service_inventory('Oil');

  DBMS_OUTPUT.PUT_LINE('Search results:');
  LOOP
    FETCH v_cursor INTO v_service_id, v_item_id, v_quantity, v_service_name, v_item_name;
    EXIT WHEN v_cursor%NOTFOUND;

    DBMS_OUTPUT.PUT_LINE('Service: ' || v_service_name ||
                         ', Item: ' || v_item_name ||
                         ', Quantity: ' || v_quantity);
  END LOOP;

  CLOSE v_cursor;
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    IF v_cursor%ISOPEN THEN
      CLOSE v_cursor;
    END IF;
END;
/

-- 5. Delete the item
BEGIN
  service_inventory_pkg.sp_delete_service_inventory(
    p_service_id => 1,        -- Use the same IDs from your test insert
    p_item_id => 2
  );
  COMMIT;
  DBMS_OUTPUT.PUT_LINE('Service inventory item deleted successfully');
EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    ROLLBACK;
END;
/