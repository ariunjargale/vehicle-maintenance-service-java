CREATE OR REPLACE TRIGGER trg_check_inventory_on_update
BEFORE UPDATE ON appointment
FOR EACH ROW
DECLARE
    v_old_item_id   inventory.item_id%TYPE;
    v_new_item_id   inventory.item_id%TYPE;
    v_qty           service_inventory.quantity_required%TYPE;
BEGIN

    IF :OLD.service_id != :NEW.service_id THEN
        -- Restore inventory from the old service
        FOR old_inv IN (
            SELECT item_id, quantity_required
            FROM service_inventory
            WHERE service_id = :OLD.service_id
        ) LOOP
            UPDATE inventory
            SET quantity = quantity + old_inv.quantity_required
            WHERE item_id = old_inv.item_id;
        END LOOP;

        -- Check and deduct inventory for the new service
        FOR new_inv IN (
            SELECT si.item_id, si.quantity_required, i.quantity AS available_qty
            FROM service_inventory si
            JOIN inventory i ON si.item_id = i.item_id
            WHERE si.service_id = :NEW.service_id
        ) LOOP
            IF new_inv.available_qty < new_inv.quantity_required THEN
                RAISE_APPLICATION_ERROR(-20002, 'Not enough inventory for item ID: ' || new_inv.item_id);
            END IF;

            UPDATE inventory
            SET quantity = quantity - new_inv.quantity_required
            WHERE item_id = new_inv.item_id;
        END LOOP;
    END IF;

    IF :OLD.is_active = 1 AND :NEW.is_active = 0 THEN
        FOR inv IN (
            SELECT item_id, quantity_required
            FROM service_inventory
            WHERE service_id = :OLD.service_id
        ) LOOP
            UPDATE inventory
            SET quantity = quantity + inv.quantity_required
            WHERE item_id = inv.item_id;
        END LOOP;
    END IF;
END;
/
