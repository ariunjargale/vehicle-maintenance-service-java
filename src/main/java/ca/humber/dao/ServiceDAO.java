package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Service;
import ca.humber.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    /**
     * Get all active services
     */
    public static List<Service> getActiveServices() {
        List<Service> services = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            services = session.createQuery("FROM Service WHERE isActive = true ORDER BY serviceName", Service.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }

    /**
     * Get all services including inactive ones
     */
    public static List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            services = session.createQuery("FROM Service ORDER BY serviceName", Service.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }

    /**
     * Get service by ID
     */
    public static Service getServiceById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Service.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Search for services by name or type
     */
    public static List<Service> searchServices(String searchTerm) {
        List<Service> results = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Query<Service> query = session.createQuery(
                    "FROM Service s WHERE s.isActive = true AND (LOWER(s.serviceName) LIKE :searchPattern " +
                            "OR LOWER(s.serviceTypeId) LIKE :searchPattern " +
                            "OR CAST(s.price AS string) LIKE :searchPattern)",
                    Service.class);
            query.setParameter("searchPattern", searchPattern);
            results = query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Create a new service
     */
    public static boolean createService(Service service) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Set as active by default
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

    /**
     * Update an existing service
     */
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

    /**
     * Soft delete a service (mark as inactive)
     */
    public static boolean deleteService(int serviceId) throws ConstraintException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Service service = session.get(Service.class, serviceId);
            if (service != null) {
                // Check if there are related appointments
                Query<Long> query = session.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.service.id = :serviceId AND a.isActive = true",
                        Long.class);
                query.setParameter("serviceId", serviceId);
                Long count = query.getSingleResult();

                if (count > 0) {
                    throw new ConstraintException("Cannot delete service. This service has " + count + " related appointments.");
                }

                // Soft delete - only mark as inactive
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
            throw new ConstraintException("Cannot delete service. There are related dependencies.");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reactivate a previously deactivated service
     */
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

    /**
     * Save or update a service
     */
    public static boolean saveService(Service service) {
        if (service.getServiceId() == 0) {
            return createService(service);
        } else {
            return updateService(service);
        }
    }
}