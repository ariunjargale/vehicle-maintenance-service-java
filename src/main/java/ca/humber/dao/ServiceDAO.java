package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Service;
import ca.humber.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {
    
    // 獲取所有活躍服務
    public static List<Service> getActiveServices() {
        List<Service> services = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // 使用 HQL 查詢所有活躍的服務
            services = session.createQuery("FROM Service WHERE isActive = true ORDER BY serviceName", Service.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }
    
    // 獲取所有服務（包括非活躍）
    public static List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            services = session.createQuery("FROM Service ORDER BY serviceName", Service.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }
    
    // 根據 ID 獲取服務
    public static Service getServiceById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Service.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 根據類型獲取服務
    public static List<Service> getServicesByType(String serviceTypeId) {
        List<Service> services = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Service> query = session.createQuery(
                "FROM Service WHERE isActive = true AND serviceTypeId = :serviceTypeId ORDER BY serviceName", 
                Service.class);
            query.setParameter("serviceTypeId", serviceTypeId);
            services = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }
    
    // 搜索服務
    public static List<Service> searchServices(String searchTerm) {
        List<Service> results = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Query<Service> query = session.createQuery(
                "FROM Service s WHERE s.isActive = true AND (LOWER(s.serviceName) LIKE :searchPattern " +
                "OR LOWER(s.serviceTypeId) LIKE :searchPattern)", 
                Service.class);
            query.setParameter("searchPattern", searchPattern);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
    
    // 添加新服務
    public static boolean createService(Service service) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // 設定為活躍狀態
            service.setIsActive(true);
            
            session.save(service);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    // 更新服務信息
    public static boolean updateService(Service service) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Service existingService = session.get(Service.class, service.getServiceId());
            if (existingService != null) {
                existingService.setServiceName(service.getServiceName());
                existingService.setServiceTypeId(service.getServiceTypeId());
                existingService.setPrice(service.getPrice());
                session.update(existingService);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    // 軟刪除服務（設為非活躍）
    public static boolean deleteService(int serviceId) throws ConstraintException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Service service = session.get(Service.class, serviceId);
            if (service != null) {
                // 檢查是否有相關的預約
                Query<Long> query = session.createQuery(
                    "SELECT COUNT(a) FROM Appointment a WHERE a.service.serviceId = :serviceId AND a.isActive = true", 
                    Long.class);
                query.setParameter("serviceId", serviceId);
                Long count = query.getSingleResult();
                
                if (count > 0) {
                    throw new ConstraintException("無法刪除服務。該服務有 " + count + " 個相關預約。");
                }
                
                // 軟刪除 - 僅設置為非活躍狀態
                service.setIsActive(false);
                session.update(service);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (ConstraintViolationException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new ConstraintException("無法刪除服務。存在相關依賴。");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    // 重新啟用服務
    public static boolean reactivateService(int serviceId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Service service = session.get(Service.class, serviceId);
            if (service != null) {
                service.setIsActive(true);
                session.update(service);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    // 更新服務價格
    public static boolean updateServicePrice(int serviceId, BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            return false; // 價格不能為負
        }
        
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Service service = session.get(Service.class, serviceId);
            if (service != null) {
                service.setPrice(newPrice);
                session.update(service);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    // 獲取服務類型列表
    public static List<String> getServiceTypes() {
        List<String> serviceTypes = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<String> query = session.createQuery(
                "SELECT DISTINCT s.serviceTypeId FROM Service s", 
                String.class);
            serviceTypes = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serviceTypes;
    }
}