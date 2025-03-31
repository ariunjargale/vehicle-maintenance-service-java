package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Customer;
import ca.humber.model.Vehicle;
import ca.humber.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.util.List;

public class CustomerDAO {
    
    // Get all active customers
    public static List<Customer> getActiveCustomers() {
        // 從 executeWithResult 改為 executeWithResultTemp
        return HibernateUtil.executeWithResult(session -> {
            Query<Customer> query = session.createQuery("FROM Customer WHERE isActive = true ORDER BY name", Customer.class);
            return query.list();
        });
    }
    
    // Get all customers (including inactive ones)
    public static List<Customer> getAllCustomers() {
        return HibernateUtil.executeWithResult(session -> {
            Query<Customer> query = session.createQuery("FROM Customer", Customer.class);
            return query.list();
        });
    }
    
    // Get customer by ID
    public static Customer getCustomerById(int id) {
        return HibernateUtil.executeWithResult(session -> session.get(Customer.class, id));
    }
    
    // Search customers by search term
    public static List<Customer> searchCustomers(String searchTerm) {
        return HibernateUtil.executeWithResult(session -> {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Query<Customer> query = session.createQuery(
                "FROM Customer c WHERE c.isActive = true AND (LOWER(c.name) LIKE :searchPattern " +
                "OR LOWER(c.phone) LIKE :searchPattern OR LOWER(c.email) LIKE :searchPattern)", 
                Customer.class);
            query.setParameter("searchPattern", searchPattern);
            return query.list();
        });
    }
    
    // Add a new customer
    public static void insertCustomer(Customer customer) {
        HibernateUtil.executeInsideTransaction(session -> session.save(customer));
        System.out.println("Customer added successfully.");
    }
    
    // Update customer
    public static boolean updateCustomer(Customer customer) {
        try {
            HibernateUtil.executeInsideTransaction(session -> session.update(customer));
            System.out.println("Customer updated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Soft delete customer (set status to inactive)
    public static boolean deleteCustomer(int customerId) throws ConstraintException {
        Customer customer = HibernateUtil.executeWithResult(session -> session.get(Customer.class, customerId));
        
        if (customer == null) {
            System.out.println("Customer with ID " + customerId + " not found.");
            return false;
        }
        
        // Check if the customer has associated vehicles
        List<Vehicle> vehicles = VehicleDAO.getVehiclesByCustomerId(customerId);
        if (!vehicles.isEmpty()) {
            throw new ConstraintException("Cannot delete customer. Customer has " + vehicles.size() + " vehicles registered. Please delete or transfer vehicles first.");
        }
        
        try {
            // Soft delete - only set status to inactive
            customer.setIsActive(false);
            HibernateUtil.executeInsideTransaction(session -> session.update(customer));
            System.out.println("Customer deactivated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Hard delete customer (permanently remove from database) - use with caution
    public static boolean hardDeleteCustomer(int customerId) throws ConstraintException {
        Customer customer = HibernateUtil.executeWithResult(session -> session.get(Customer.class, customerId));
        
        if (customer == null) {
            System.out.println("Customer with ID " + customerId + " not found.");
            return false;
        }
        
        // Check if the customer has associated vehicles
        List<Vehicle> vehicles = VehicleDAO.getVehiclesByCustomerId(customerId);
        if (!vehicles.isEmpty()) {
            throw new ConstraintException("Cannot delete customer. Customer has " + vehicles.size() + " vehicles registered. Please delete or transfer vehicles first.");
        }
        
        try {
            HibernateUtil.executeInsideTransaction(session -> session.delete(customer));
            System.out.println("Customer permanently deleted successfully.");
            return true;
        } catch (ConstraintViolationException e) {
            throw new ConstraintException("Cannot delete customer due to database constraints.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Create test data - for testing purposes only
    public static void createTestCustomers() {
        try {
            // Check if customer data already exists
            List<Customer> existingCustomers = getAllCustomers();
            if (!existingCustomers.isEmpty()) {
                System.out.println("Test customers already exist, skipping creation.");
                return;
            }
            
            // Create some test customers
            Customer c1 = new Customer("Wang Daming", "0912345678", "wang.daming@example.com");
            Customer c2 = new Customer("Chen Xiaofang", "0923456789", "chen.xiaofang@example.com");
            Customer c3 = new Customer("Lin Zhiqiang", "0934567890", "lin.zhiqiang@example.com");
            Customer c4 = new Customer("Zhang Meiling", "0945678901", "zhang.meiling@example.com");
            Customer c5 = new Customer("Li Jianguo", "0956789012", "li.jianguo@example.com");
            
            HibernateUtil.executeInsideTransaction(session -> {
                session.save(c1);
                session.save(c2);
                session.save(c3);
                session.save(c4);
                session.save(c5);
            });
            
            System.out.println("Test customers created successfully.");
        } catch (Exception e) {
            System.err.println("Error creating test customers:");
            e.printStackTrace();
        }
    }
}