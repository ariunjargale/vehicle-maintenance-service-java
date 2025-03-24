package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Customer;
import ca.humber.model.Vehicle;
import ca.humber.util.HibernateUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.util.List;

public class CustomerDAO {
    
    // 獲取所有活動客戶
    public static List<Customer> getActiveCustomers() {
        return HibernateUtil.executeWithResult(session -> {
            Query<Customer> query = session.createQuery("FROM Customer WHERE isActive = true", Customer.class);
            return query.list();
        });
    }
    
    // 獲取所有客戶（包括不活動的）
    public static List<Customer> getAllCustomers() {
        return HibernateUtil.executeWithResult(session -> {
            Query<Customer> query = session.createQuery("FROM Customer", Customer.class);
            return query.list();
        });
    }
    
    // 根據ID獲取客戶
    public static Customer getCustomerById(int id) {
        return HibernateUtil.executeWithResult(session -> session.get(Customer.class, id));
    }
    
    // 根據搜索條件查找客戶
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
    
    // 新增客戶
    public static void insertCustomer(Customer customer) {
        HibernateUtil.executeInsideTransaction(session -> session.save(customer));
        System.out.println("Customer added successfully.");
    }
    
    // 更新客戶
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
    
    // 軟刪除客戶（將狀態設置為非活動）
    public static boolean deleteCustomer(int customerId) throws ConstraintException {
        Customer customer = HibernateUtil.executeWithResult(session -> session.get(Customer.class, customerId));
        
        if (customer == null) {
            System.out.println("Customer with ID " + customerId + " not found.");
            return false;
        }
        
        // 檢查客戶是否有關聯的車輛
        List<Vehicle> vehicles = VehicleDAOold.getVehiclesByCustomerId(customerId);
        if (!vehicles.isEmpty()) {
            throw new ConstraintException("Cannot delete customer. Customer has " + vehicles.size() + " vehicles registered. Please delete or transfer vehicles first.");
        }
        
        try {
            // 軟刪除 - 只將狀態設為非活動
            customer.setIsActive(false);
            HibernateUtil.executeInsideTransaction(session -> session.update(customer));
            System.out.println("Customer deactivated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 硬刪除客戶（從資料庫中永久移除）- 謹慎使用
    public static boolean hardDeleteCustomer(int customerId) throws ConstraintException {
        Customer customer = HibernateUtil.executeWithResult(session -> session.get(Customer.class, customerId));
        
        if (customer == null) {
            System.out.println("Customer with ID " + customerId + " not found.");
            return false;
        }
        
        // 檢查客戶是否有關聯的車輛
        List<Vehicle> vehicles = VehicleDAOold.getVehiclesByCustomerId(customerId);
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
    
    // 創建測試數據 - 僅用於測試目的
    public static void createTestCustomers() {
        try {
            // 檢查是否已有客戶數據
            List<Customer> existingCustomers = getAllCustomers();
            if (!existingCustomers.isEmpty()) {
                System.out.println("Test customers already exist, skipping creation.");
                return;
            }
            
            // 創建幾個測試客戶
            Customer c1 = new Customer("王大明", "0912345678", "wang.daming@example.com");
            Customer c2 = new Customer("陳小芳", "0923456789", "chen.xiaofang@example.com");
            Customer c3 = new Customer("林志強", "0934567890", "lin.zhiqiang@example.com");
            Customer c4 = new Customer("張美玲", "0945678901", "zhang.meiling@example.com");
            Customer c5 = new Customer("李建國", "0956789012", "li.jianguo@example.com");
            
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