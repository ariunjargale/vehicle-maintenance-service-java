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

    /**
     * Save or update a mechanic based on whether it has an ID.
     * This method serves as a unified interface for create/update operations.
     *
     * @param mechanic The mechanic to save or update
     * @return true if operation was successful, false otherwise
     * @throws Exception if an error occurs during the operation
     */
    public static boolean saveMechanic(Mechanic mechanic) throws Exception {
        // If mechanic has no ID (or ID is 0), it's a new mechanic
        if (mechanic.getMechanicId() == 0) {
            // Create new mechanic
            return createMechanic(mechanic);
        } else {
            // Update existing mechanic
            return updateMechanic(mechanic);
        }
    }

    /**
     * Get all active mechanics
     */
    public static List<Mechanic> getActiveMechanics() {
        List<Mechanic> mechanics = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Query all active mechanics
            mechanics = session.createQuery("FROM Mechanic WHERE isActive = true ORDER BY name", Mechanic.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mechanics;
    }

    /**
     * Get all mechanics including inactive ones
     */
    public static List<Mechanic> getAllMechanics() {
        List<Mechanic> mechanics = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            mechanics = session.createQuery("FROM Mechanic ORDER BY name", Mechanic.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mechanics;
    }

    /**
     * Get a mechanic by ID
     */
    public static Mechanic getMechanicById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Mechanic.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get mechanics by specialization
     */
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

    /**
     * Search mechanics by name, phone or specialization
     */
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

    /**
     * Add a new mechanic
     */
    public static boolean createMechanic(Mechanic mechanic) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Set as active by default
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

    /**
     * Update an existing mechanic
     */
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

    /**
     * Soft delete a mechanic (mark as inactive)
     *
     * @throws ConstraintException if the mechanic has active appointments
     */
    public static boolean deleteMechanic(int mechanicId) throws ConstraintException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Mechanic mechanic = session.get(Mechanic.class, mechanicId);
            if (mechanic != null) {
                // Check for related appointments
                Query<Long> query = session.createQuery(
                        "SELECT COUNT(a) FROM Appointment a WHERE a.mechanic.id = :mechanicId AND a.isActive = true",
                        Long.class);
                query.setParameter("mechanicId", mechanicId);
                Long count = query.getSingleResult();

                if (count > 0) {
                    throw new ConstraintException("Cannot delete mechanic. This mechanic has " + count + " related appointments.");
                }

                // Soft delete - only mark as inactive
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
            throw new ConstraintException("Cannot delete mechanic. There are related dependencies.");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reactivate a previously deactivated mechanic
     */
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