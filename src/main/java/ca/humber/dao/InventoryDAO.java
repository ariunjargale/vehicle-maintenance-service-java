package ca.humber.dao;

import ca.humber.model.Inventory;
import ca.humber.util.HibernateUtil;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public static void createInventory(Inventory inventory) {
        HibernateUtil.callProcedure(conn -> {
            try (CallableStatement stmt = conn.prepareCall("{ call inventory_pkg.sp_create_inventory( ?, ?, ?) }")) {
                stmt.setString(1, inventory.getItemName());
                stmt.setInt(2, inventory.getQuantity());
                stmt.setBigDecimal(3, inventory.getPrice());
                stmt.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to create inventory", e);
            }
        });
    }

    public static void updateInventory(Inventory inventory) {
        HibernateUtil.callProcedure(conn -> {
            try (CallableStatement stmt = conn.prepareCall("{ call inventory_pkg.sp_update_inventory(?, ?, ?, ?) }")) {
                stmt.setLong(1, inventory.getItemId());
                stmt.setString(2, inventory.getItemName());
                stmt.setInt(3, inventory.getQuantity());
                stmt.setBigDecimal(4, inventory.getPrice());
                stmt.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update inventory", e);
            }
        });
    }

    public static void deleteInventory(long itemId) {
        HibernateUtil.callProcedure(conn -> {
            try (CallableStatement stmt = conn.prepareCall("{ call inventory_pkg.sp_delete_inventory(?) }")) {
                stmt.setLong(1, itemId);
                stmt.execute();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete inventory", e);
            }
        });
    }

    public static Inventory getInventory(long itemId) {
        return HibernateUtil.callResultListFunction(conn -> {
            List<Inventory> list = new ArrayList<>();
            String sql = "SELECT * FROM TABLE(inventory_pkg.fn_get_inventory(?))";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, itemId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(fromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch inventory", e);
            }
            return list;
        }).stream().findFirst().orElse(null);
    }

    public static List<Inventory> getAllInventory() {
        return HibernateUtil.callResultListFunction(conn -> {
            List<Inventory> list = new ArrayList<>();
            String sql = "SELECT * FROM TABLE(inventory_pkg.fn_get_all_inventory())";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(fromResultSet(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to fetch all inventory", e);
            }
            return list;
        });
    }

    public static List<Inventory> searchInventory(String filter) {
        return HibernateUtil.callResultListFunction(conn -> {
            List<Inventory> list = new ArrayList<>();
            String sql = "SELECT * FROM TABLE(inventory_pkg.fn_search_inventory(?))";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, filter);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(fromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to search inventory", e);
            }
            return list;
        });
    }

    public static int getInventoryCount() {
        return HibernateUtil.callSingleResultFunction(conn -> {
            try (CallableStatement stmt = conn.prepareCall("{ ? = call inventory_pkg.fn_get_inventory_count() }")) {
                stmt.registerOutParameter(1, Types.INTEGER);
                stmt.execute();
                return stmt.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get inventory count", e);
            }
        });
    }

    private static Inventory fromResultSet(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setItemId(rs.getLong("item_id"));
        inv.setItemName(rs.getString("item_name"));
        inv.setQuantity(rs.getInt("quantity"));
        inv.setPrice(rs.getBigDecimal("price"));
        inv.setIsActive(rs.getInt("is_active"));
        return inv;
    }
}
