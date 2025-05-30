package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Customer;
import ca.humber.model.Vehicle;
import ca.humber.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    // Get a list of all active vehicles
    public static List<Vehicle> getVehiclesList() {
        try {
            return HibernateUtil.executeWithResult(session -> {
                // 首先嘗試使用包函數
                try {
                    List<Vehicle> vehicles = new ArrayList<>();
                    session.doWork(connection -> {
                        try (CallableStatement stmt = connection
                                .prepareCall("{ ? = call vehicle_pkg.fn_get_all_active_vehicles() }")) {
                            stmt.registerOutParameter(1, Types.REF_CURSOR);
                            stmt.execute();

                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                while (rs.next()) {
                                    vehicles.add(createVehicleFromResultSet(rs));
                                }
                            }
                        } catch (SQLException e) {
                            System.err.println("Error calling package: " + e.getMessage());
                            throw new RuntimeException("Failed to get vehicles list", e);
                        }
                    });
                    return vehicles;
                } catch (Exception e) {
                    // If the package call fails, fall back to a direct HQL query
                    System.err.println("Package call failed, using direct query: " + e.getMessage());
                    return session.createQuery("FROM Vehicle v LEFT JOIN FETCH v.customer WHERE v.isActive = true",
                            Vehicle.class).list();
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to get vehicles list: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get a single vehicle by ID
    public static Vehicle getVehicleById(int id) {
        try {
            return HibernateUtil.executeWithResult(session -> {
                // First, attempt to use the package function
                try {
                    final Vehicle[] vehicle = { null };
                    session.doWork(connection -> {
                        try (CallableStatement stmt = connection
                                .prepareCall("{ ? = call vehicle_pkg.fn_get_vehicle_by_id(?) }")) {
                            stmt.registerOutParameter(1, Types.REF_CURSOR);
                            stmt.setInt(2, id);
                            stmt.execute();

                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                if (rs.next()) {
                                    vehicle[0] = createVehicleFromResultSet(rs);
                                }
                            }
                        } catch (SQLException e) {
                            System.err.println("Error calling package: " + e.getMessage());
                            throw new RuntimeException("Failed to get vehicle by ID", e);
                        }
                    });
                    return vehicle[0];
                } catch (Exception e) {
                    // If the package call fails, fall back to a direct query
                    System.err.println("Package call failed, using direct query: " + e.getMessage());
                    return session.get(Vehicle.class, id);
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to get vehicle by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get a list of vehicles by customer ID
    public static List<Vehicle> getVehiclesByCustomerId(int customerId) {
        try {
            return HibernateUtil.executeWithResult(session -> {
                // 首先嘗試使用包函數
                try {
                    List<Vehicle> vehicles = new ArrayList<>();
                    session.doWork(connection -> {
                        try (CallableStatement stmt = connection
                                .prepareCall("{ ? = call vehicle_pkg.fn_get_vehicles_by_customer(?) }")) {
                            stmt.registerOutParameter(1, Types.REF_CURSOR);
                            stmt.setInt(2, customerId);
                            stmt.execute();

                            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                                while (rs.next()) {
                                    vehicles.add(createVehicleFromResultSet(rs));
                                }
                            }
                        } catch (SQLException e) {
                            System.err.println("Error calling package: " + e.getMessage());
                            throw new RuntimeException("Failed to get vehicles by customer ID", e);
                        }
                    });
                    return vehicles;
                } catch (Exception e) {
                    // 如果包調用失敗，回退到直接 HQL 查詢
                    System.err.println("Package call failed, using direct query: " + e.getMessage());
                    return session.createQuery(
                            "FROM Vehicle v WHERE v.customer.customerId = :customerId AND v.isActive = true",
                            Vehicle.class)
                            .setParameter("customerId", customerId)
                            .list();
                }
            });
        } catch (Exception e) {
            System.err.println("Failed to get vehicles by customer ID: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Search for vehicles
    public static List<Vehicle> searchVehicles(String searchTerm) {
        return HibernateUtil.callFunction(conn -> {
            List<Vehicle> vehicles = new ArrayList<>();
            try (CallableStatement stmt = conn.prepareCall("{ ? = call vehicle_pkg.fn_search_vehicles(?) }")) {
                stmt.registerOutParameter(1, Types.REF_CURSOR);
                stmt.setString(2, searchTerm);
                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    while (rs.next()) {
                        vehicles.add(createVehicleFromResultSet(rs));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error searching vehicles: " + e.getMessage(), e);
            }
            return vehicles;
        });
    }

    // Add a new vehicle
    public static void insertVehicle(Vehicle vehicle) {
        HibernateUtil.callProcedure(conn -> {
            try (CallableStatement stmt = conn.prepareCall("{ call vehicle_pkg.sp_add_vehicle(?, ?, ?, ?, ?, ?) }")) {
                stmt.setInt(1, vehicle.getCustomer().getCustomerId());
                stmt.setString(2, vehicle.getMake());
                stmt.setString(3, vehicle.getModel());
                stmt.setInt(4, vehicle.getYear());
                stmt.setString(5, vehicle.getVin());
                stmt.registerOutParameter(6, Types.INTEGER);
                stmt.execute();

                vehicle.setVehicleId(stmt.getInt(6));
                System.out.println("Vehicle added successfully. ID: " + vehicle.getVehicleId());
            } catch (SQLException e) {
                throw new RuntimeException("Error adding vehicle: " + e.getMessage(), e);
            }
        });
    }

    // Update a vehicle
    public static boolean updateVehicle(Vehicle vehicle) {
        try {
            HibernateUtil.callProcedure(conn -> {
                try (CallableStatement stmt = conn
                        .prepareCall("{ call vehicle_pkg.sp_update_vehicle(?, ?, ?, ?, ?) }")) {
                    stmt.setInt(1, vehicle.getVehicleId());
                    stmt.setString(2, vehicle.getMake());
                    stmt.setString(3, vehicle.getModel());
                    stmt.setInt(4, vehicle.getYear());
                    stmt.setString(5, vehicle.getVin());
                    stmt.execute();

                    if (vehicle.getCustomer() != null) {
                        try (CallableStatement ownerStmt = conn
                                .prepareCall("{ call vehicle_pkg.sp_change_vehicle_owner(?, ?) }")) {
                            ownerStmt.setInt(1, vehicle.getVehicleId());
                            ownerStmt.setInt(2, vehicle.getCustomer().getCustomerId());
                            ownerStmt.execute();
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Error updating vehicle: " + e.getMessage(), e);
                }
            });
            System.out.println("Vehicle updated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Soft delete a vehicle (set as inactive)
    public static boolean deleteVehicle(int vehicleId) throws ConstraintException {
        try {
            HibernateUtil.callProcedure(conn -> {
                try (CallableStatement stmt = conn.prepareCall("{ call vehicle_pkg.sp_deactivate_vehicle(?) }")) {
                    stmt.setInt(1, vehicleId);
                    stmt.execute();
                } catch (SQLException e) {
                    if (e.getErrorCode() == 20008 || e.getMessage().contains("active appointments")) {
                        throw new RuntimeException(new ConstraintException(
                                "Cannot deactivate vehicle. There are active appointments scheduled for this vehicle."));
                    } else if (e.getErrorCode() == -6550 || e.getMessage().contains("not declared")) {
                        System.err
                                .println("Warning: Database procedure 'vehicle_pkg.sp_deactivate_vehicle' not found.");
                        throw new RuntimeException(
                                "Required database procedure not found. Contact your system administrator.");
                    } else {
                        throw new RuntimeException("Error deactivating vehicle: " + e.getMessage(), e);
                    }
                }
            });
            System.out.println("Vehicle deactivated successfully.");
            return true;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ConstraintException) {
                throw (ConstraintException) e.getCause();
            }
            System.err.println("Failed to deactivate vehicle: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Hard delete a vehicle (permanently remove from the database)
    public static boolean hardDeleteVehicle(int vehicleId) throws ConstraintException {
        try {
            HibernateUtil.callProcedure(conn -> {
                try (CallableStatement stmt = conn.prepareCall("{ call vehicle_pkg.sp_delete_vehicle(?) }")) {
                    stmt.setInt(1, vehicleId);
                    stmt.execute();
                } catch (SQLException e) {
                    if (e.getErrorCode() == 20010 || e.getMessage().contains("appointments associated")) {
                        throw new RuntimeException(new ConstraintException(
                                "Cannot permanently delete vehicle. There are appointments associated with this vehicle."));
                    } else if (e.getErrorCode() == -6550 || e.getMessage().contains("not declared")) {
                        throw new RuntimeException(
                                "Database procedure not found. Please ensure the database schema is up to date.");
                    } else {
                        throw new RuntimeException("Error deleting vehicle: " + e.getMessage(), e);
                    }
                }
            });
            System.out.println("Vehicle permanently deleted successfully.");
            return true;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ConstraintException) {
                throw (ConstraintException) e.getCause();
            }
            e.printStackTrace();
            return false;
        }
    }

    // Reactivate a vehicle
    public static boolean reactivateVehicle(int vehicleId) {
        try {
            HibernateUtil.callProcedure(conn -> {
                try (CallableStatement stmt = conn.prepareCall("{ call vehicle_pkg.sp_reactivate_vehicle(?) }")) {
                    stmt.setInt(1, vehicleId);
                    stmt.execute();
                } catch (SQLException e) {
                    throw new RuntimeException("Error reactivating vehicle: " + e.getMessage(), e);
                }
            });
            System.out.println("Vehicle reactivated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to create a Vehicle object from a result set
    private static Vehicle createVehicleFromResultSet(ResultSet rs) throws SQLException {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setVehicleId(rs.getInt("VEHICLE_ID"));
            vehicle.setMake(rs.getString("MAKE"));
            vehicle.setModel(rs.getString("MODEL"));
            vehicle.setYear(rs.getInt("YEAR"));
            vehicle.setVin(rs.getString("VIN"));
            vehicle.setIsActive(rs.getInt("IS_ACTIVE") == 1);

            Customer customer = new Customer();
            customer.setCustomerId(rs.getInt("CUSTOMER_ID"));
            customer.setName(rs.getString("CUSTOMER_NAME"));
            vehicle.setCustomer(customer);

            return vehicle;
        } catch (SQLException e) {
            System.err.println("Error processing result set: " + e.getMessage());
            throw e;
        }
    }

    // Helper method to handle package errors
    private static void handlePackageError(SQLException e, String procedureName) throws RuntimeException {
        if (e.getErrorCode() == -6550 || e.getMessage().contains("not declared")) {
            System.err.println("Warning: Database procedure 'vehicle_pkg." + procedureName + "' not found.");
            throw new RuntimeException("Required database procedure not found. Contact your system administrator.");
        } else {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}
