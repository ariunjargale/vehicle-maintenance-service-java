package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Mechanic;
import ca.humber.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class MechanicDAO {
    
    // 獲取所有活躍技師
    public static List<Mechanic> getActiveMechanics() {
        List<Mechanic> mechanics = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // 使用 HQL 查詢所有活躍的技師
            mechanics = session.createQuery("FROM Mechanic WHERE isActive = true ORDER BY name", Mechanic.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mechanics;
    }
    
    // 獲取所有技師（包括非活躍）
    public static List<Mechanic> getAllMechanics() {
        List<Mechanic> mechanics = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            mechanics = session.createQuery("FROM Mechanic ORDER BY name", Mechanic.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mechanics;
    }
    
    // 根據 ID 獲取技師
    public static Mechanic getMechanicById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Mechanic.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 根據專業領域搜索技師
    public static List<Mechanic> getMechanicsBySpecialization(String specialization) {
        List<Mechanic> mechanics = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Mechanic> query = session.createQuery(
                "FROM Mechanic WHERE isActive = true AND specialization = :spec ORDER BY name", 
                Mechanic.class);
            query.setParameter("spec", specialization);
            mechanics = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mechanics;
    }
    
    // 搜索技師
    public static List<Mechanic> searchMechanics(String searchTerm) {
        List<Mechanic> results = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Query<Mechanic> query = session.createQuery(
                "FROM Mechanic m WHERE m.isActive = true AND (LOWER(m.name) LIKE :searchPattern " +
                "OR LOWER(m.phone) LIKE :searchPattern " +
                "OR LOWER(m.specialization) LIKE :searchPattern)", 
                Mechanic.class);
            query.setParameter("searchPattern", searchPattern);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
    
    // 添加新技師
    public static boolean createMechanic(Mechanic mechanic) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // 設定為活躍狀態
            mechanic.setIsActive(true);
            
            session.save(mechanic);
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
    
    // 更新技師信息
    public static boolean updateMechanic(Mechanic mechanic) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Mechanic existingMechanic = session.get(Mechanic.class, mechanic.getMechanicId());
            if (existingMechanic != null) {
                existingMechanic.setName(mechanic.getName());
                existingMechanic.setPhone(mechanic.getPhone());
                existingMechanic.setSpecialization(mechanic.getSpecialization());
                session.update(existingMechanic);
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
    
    // 軟刪除技師（設為非活躍）
    public static boolean deleteMechanic(int mechanicId) throws ConstraintException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Mechanic mechanic = session.get(Mechanic.class, mechanicId);
            if (mechanic != null) {
                // 檢查是否有相關的預約
                Query<Long> query = session.createQuery(
                    "SELECT COUNT(a) FROM Appointment a WHERE a.mechanic.id = :mechanicId AND a.isActive = true", 
                    Long.class);
                query.setParameter("mechanicId", mechanicId);
                Long count = query.getSingleResult();
                
                if (count > 0) {
                    throw new ConstraintException("無法刪除技師。該技師有 " + count + " 個相關預約。");
                }
                
                // 軟刪除 - 僅設置為非活躍狀態
                mechanic.setIsActive(false);
                session.update(mechanic);
                transaction.commit();
                return true;
            } else {
                return false;
            }
        } catch (ConstraintViolationException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new ConstraintException("無法刪除技師。存在相關依賴。");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    // 重新啟用技師
    public static boolean reactivateMechanic(int mechanicId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Mechanic mechanic = session.get(Mechanic.class, mechanicId);
            if (mechanic != null) {
                mechanic.setIsActive(true);
                session.update(mechanic);
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
}
