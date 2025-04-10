CREATE OR REPLACE TRIGGER trg_check_inventory_on_insert
BEFORE INSERT ON appointment
FOR EACH ROW
DECLARE
    v_item_id        inventory.item_id%TYPE;
    v_required_qty   service_inventory.quantity_required%TYPE;
    v_available_qty  inventory.quantity%TYPE;
BEGIN
    -- Loop through all inventory items linked to the selected service
    FOR inv IN (
        SELECT si.item_id, si.quantity_required, i.quantity AS available_qty
        FROM service_inventory si
        JOIN inventory i ON i.item_id = si.item_id
        WHERE si.service_id = :NEW.service_id
    ) LOOP
        IF inv.available_qty < inv.quantity_required THEN
            RAISE_APPLICATION_ERROR(-20001,
                'Not enough inventory for item ID: ' || inv.item_id ||
                '. Required: ' || inv.quantity_required ||
                ', Available: ' || inv.available_qty);
        END IF;
    END LOOP;

    -- Deduct inventory quantities (AFTER quantity check)
    FOR inv IN (
        SELECT si.item_id, si.quantity_required
        FROM service_inventory si
        WHERE si.service_id = :NEW.service_id
    ) LOOP
        UPDATE inventory
        SET quantity = quantity - inv.quantity_required
        WHERE item_id = inv.item_id;
    END LOOP;
END;
/
