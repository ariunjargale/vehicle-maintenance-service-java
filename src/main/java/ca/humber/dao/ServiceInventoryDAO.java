package ca.humber.dao;

import ca.humber.model.ServiceInventory;
import ca.humber.util.HibernateUtil;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceInventoryDAO {

    public static List<ServiceInventory> getAll() {
        return HibernateUtil.callResultListFunction(conn -> {
            List<ServiceInventory> list = new ArrayList<>();
            String sql = "{ ? = call service_inventory_pkg.fn_get_all_service_inventory() }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.execute();
                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    while (rs.next()) {
                        list.add(mapResultSetToServiceInventory(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error fetching service inventory list", e);
            }
            return list;
        });
    }

    public static List<ServiceInventory> getAllDetailed() {
        return HibernateUtil.callResultListFunction(conn -> {
            List<ServiceInventory> list = new ArrayList<>();
            String sql = "{ ? = call service_inventory_pkg.fn_get_detailed_service_inventory() }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.execute();
                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    while (rs.next()) {
                        list.add(mapDetailedResultSetToServiceInventory(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error fetching detailed service inventory list", e);
            }
            return list;
        });
    }

    public static ServiceInventory getById(int serviceId, int itemId) {
        return HibernateUtil.callFunction(conn -> {
            String sql = "{ ? = call service_inventory_pkg.fn_get_service_inventory(?, ?) }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.setInt(2, serviceId);
                stmt.setInt(3, itemId);
                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    if (rs.next()) {
                        return mapDetailedResultSetToServiceInventory(rs);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error fetching service inventory", e);
            }
            return null;
        });
    }

    public static List<ServiceInventory> search(String searchTerm) {
        return HibernateUtil.callResultListFunction(conn -> {
            List<ServiceInventory> list = new ArrayList<>();
            String sql = "{ ? = call service_inventory_pkg.fn_search_service_inventory(?) }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.setString(2, searchTerm);
                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    while (rs.next()) {
                        list.add(mapDetailedResultSetToServiceInventory(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error searching service inventory", e);
            }
            return list;
        });
    }

    public static boolean save(ServiceInventory serviceInventory) {
        return HibernateUtil.callFunction(conn -> {
            String sql = "{ call service_inventory_pkg.sp_create_service_inventory(?, ?, ?) }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, serviceInventory.getServiceId());
                stmt.setInt(2, serviceInventory.getItemId());
                stmt.setInt(3, serviceInventory.getQuantityRequired());
                stmt.execute();
                return true;
            } catch (SQLException e) {
                if (e.getErrorCode() == 20001) {
                    // Service inventory item already exists
                    return false;
                } else if (e.getErrorCode() == 20002) {
                    // Service ID does not exist
                    throw new RuntimeException("Service ID " + serviceInventory.getServiceId() + " does not exist");
                } else if (e.getErrorCode() == 20003) {
                    // Item ID does not exist
                    throw new RuntimeException("Item ID " + serviceInventory.getItemId() + " does not exist");
                } else {
                    throw new RuntimeException("Error saving service inventory: " + e.getMessage(), e);
                }
            }
        });
    }

    public static boolean update(ServiceInventory serviceInventory) {
        return HibernateUtil.callFunction(conn -> {
            String sql = "{ call service_inventory_pkg.sp_update_service_inventory(?, ?, ?) }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, serviceInventory.getServiceId());
                stmt.setInt(2, serviceInventory.getItemId());
                stmt.setInt(3, serviceInventory.getQuantityRequired());
                stmt.execute();
                return true;
            } catch (SQLException e) {
                if (e.getErrorCode() == 20004) {
                    // Service inventory item does not exist
                    return false;
                } else {
                    throw new RuntimeException("Error updating service inventory: " + e.getMessage(), e);
                }
            }
        });
    }

    public static boolean delete(int serviceId, int itemId) {
        return HibernateUtil.callFunction(conn -> {
            String sql = "{ call service_inventory_pkg.sp_delete_service_inventory(?, ?) }";
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setInt(1, serviceId);
                stmt.setInt(2, itemId);
                stmt.execute();
                return true;
            } catch (SQLException e) {
                if (e.getErrorCode() == 20004) {
                    // Service inventory item does not exist
                    return false;
                } else {
                    throw new RuntimeException("Error deleting service inventory: " + e.getMessage(), e);
                }
            }
        });
    }

    private static ServiceInventory mapResultSetToServiceInventory(ResultSet rs) throws SQLException {
        return new ServiceInventory(
                rs.getInt("SERVICE_ID"),
                rs.getInt("ITEM_ID"),
                rs.getInt("QUANTITY_REQUIRED")
        );
    }

    private static ServiceInventory mapDetailedResultSetToServiceInventory(ResultSet rs) throws SQLException {
        return new ServiceInventory(
                rs.getInt("SERVICE_ID"),
                rs.getInt("ITEM_ID"),
                rs.getInt("QUANTITY_REQUIRED"),
                rs.getString("SERVICE_NAME"),
                rs.getString("ITEM_NAME")
        );
    }
}