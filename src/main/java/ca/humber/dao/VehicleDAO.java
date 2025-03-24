package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Vehicle;
import ca.humber.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.util.List;

public class VehicleDAO {
    
    public static List<Vehicle> getVehiclesList() {
        return HibernateUtil.executeWithResult(session -> {
            Query<Vehicle> query = session.createQuery("FROM Vehicle WHERE isActive = true", Vehicle.class);
            return query.list();
        });
    }
    
    public static Vehicle getVehicleById(int id) {
        return HibernateUtil.executeWithResult(session -> session.get(Vehicle.class, id));
    }
    
    public static List<Vehicle> getVehiclesByCustomerId(int customerId) {
        return HibernateUtil.executeWithResult(session -> {
            Query<Vehicle> query = session.createQuery(
                "FROM Vehicle v WHERE v.customer.customerId = :customerId AND v.isActive = true", 
                Vehicle.class);
            query.setParameter("customerId", customerId);
            return query.list();
        });
    }
    
    public static List<Vehicle> searchVehicles(String searchTerm) {
        return HibernateUtil.executeWithResult(session -> {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Query<Vehicle> query = session.createQuery(
                "FROM Vehicle v WHERE v.isActive = true AND (LOWER(v.make) LIKE :searchPattern " +
                "OR LOWER(v.model) LIKE :searchPattern " +
                "OR LOWER(v.vin) LIKE :searchPattern OR LOWER(v.licensePlate) LIKE :searchPattern " +
                "OR CAST(v.year AS string) LIKE :searchPattern)", 
                Vehicle.class);
            query.setParameter("searchPattern", searchPattern);
            return query.list();
        });
    }
    
    public static void insertVehicle(Vehicle vehicle) {
        HibernateUtil.executeInsideTransaction(session -> session.save(vehicle));
        System.out.println("Vehicle added successfully.");
    }
    
    public static boolean updateVehicle(Vehicle vehicle) {
        try {
            HibernateUtil.executeInsideTransaction(session -> session.update(vehicle));
            System.out.println("Vehicle updated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean deleteVehicle(int vehicleId) throws ConstraintException {
        Vehicle vehicle = HibernateUtil.executeWithResult(session -> session.get(Vehicle.class, vehicleId));
        
        if (vehicle == null) {
            System.out.println("Vehicle with ID " + vehicleId + " not found.");
            return false;
        }
        
        try {
            // Soft delete - only mark the status as inactive
            vehicle.setIsActive(false);
            HibernateUtil.executeInsideTransaction(session -> session.update(vehicle));
            System.out.println("Vehicle deactivated successfully.");
            return true;
        } catch (ConstraintViolationException e) {
            throw new ConstraintException("Cannot delete vehicle. There are appointments scheduled for this vehicle.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        }
        
        // Hard delete - only use when there are no associated appointments
    public static boolean hardDeleteVehicle(int vehicleId) throws ConstraintException {
        Vehicle vehicle = HibernateUtil.executeWithResult(session -> session.get(Vehicle.class, vehicleId));
        
        if (vehicle == null) {
            System.out.println("Vehicle with ID " + vehicleId + " not found.");
            return false;
        }
        
        try {
            HibernateUtil.executeInsideTransaction(session -> session.delete(vehicle));
            System.out.println("Vehicle permanently deleted successfully.");
            return true;
        } catch (ConstraintViolationException e) {
            throw new ConstraintException("Cannot delete vehicle. There are appointments scheduled for this vehicle.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


// public static List<Customer> getActiveCustomers() {
//     return HibernateUtil.executeWithResult(session -> {
//         Query<Customer> query = session.createQuery("FROM Customer WHERE isActive = true", Customer.class);
//         return query.list();
//     });
// }
// add these code to customer DAO for get custeomr