package ca.humber.service;

import ca.humber.dao.InventoryDAO;
import ca.humber.model.Inventory;

import java.util.List;

public class InventoryService {

    public static void createInventory(Inventory inventory) {
        InventoryDAO.createInventory(inventory);
    }

    public static void updateInventory(Inventory inventory) {
        InventoryDAO.updateInventory(inventory);
    }

    public static void deleteInventory(long itemId) {
        InventoryDAO.deleteInventory(itemId);
    }

    public static Inventory getInventory(long itemId) {
        return InventoryDAO.getInventory(itemId);
    }

    public static List<Inventory> getAllInventory() {
        return InventoryDAO.getAllInventory();
    }

    public static List<Inventory> searchInventory(String keyword) {
        return InventoryDAO.searchInventory(keyword);
    }

    public static int getInventoryCount() {
        return InventoryDAO.getInventoryCount();
    }
}