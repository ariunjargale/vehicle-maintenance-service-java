package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Appointment;
import ca.humber.model.Customer;
import ca.humber.model.Mechanic;
import ca.humber.model.Service;
import ca.humber.model.Vehicle;
import ca.humber.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TemporalType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentDAO {

    // Retrieve all appointments
    public static List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use HQL to query all active appointments
            appointments = session
                    .createQuery("FROM Appointment WHERE isActive = true ORDER BY appointmentDate", Appointment.class)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // Retrieve an appointment by ID
    public static Appointment getAppointmentById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Appointment.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create a new appointment
    public static boolean createAppointment(Appointment appointment) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Ensure associated objects are properly loaded
            if (appointment.getCustomer() != null) {
                Customer customer = session.get(Customer.class, appointment.getCustomer().getCustomerId());
                appointment.setCustomer(customer);
            }

            if (appointment.getVehicle() != null) {
                Vehicle vehicle = session.get(Vehicle.class, appointment.getVehicle().getVehicleId());
                appointment.setVehicle(vehicle);
            }

            if (appointment.getService() != null) {
                Service service = session.get(Service.class, appointment.getService().getServiceId());
                appointment.setService(service);
            }

            if (appointment.getMechanic() != null) {
                Mechanic mechanic = session.get(Mechanic.class, appointment.getMechanic().getMechanicId());
                appointment.setMechanic(mechanic);
            }

            // Set as active by default
            appointment.setIsActive(true);

            session.save(appointment);
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

    // Update appointment status
    public static boolean updateAppointmentStatus(int appointmentId, String statusId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment != null) {
                appointment.setStatusId(statusId);
                session.update(appointment);
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

    // Assign a mechanic to an appointment
    public static boolean assignMechanic(int appointmentId, int mechanicId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Appointment appointment = session.get(Appointment.class, appointmentId);
            Mechanic mechanic = session.get(Mechanic.class, mechanicId);

            if (appointment != null && mechanic != null) {
                appointment.setMechanic(mechanic);
                session.update(appointment);
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

    // Delete an appointment (soft delete)
    public static boolean deleteAppointment(int appointmentId) throws ConstraintException {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Appointment appointment = session.get(Appointment.class, appointmentId);
            if (appointment != null) {
                // Soft delete - only set as inactive
                appointment.setIsActive(false);
                session.update(appointment);
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
            throw new ConstraintException("Unable to delete appointment, there may be associated records.");
        }
    }

    // Get available appointment slots
    public static Map<String, String> getAvailableSlots(Date date) {
        Map<String, String> availableSlots = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Set the start and end time of the date
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date startDate = cal.getTime();

            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endDate = cal.getTime();

            // Query the booked slots and their count for the date
            String hql = "SELECT HOUR(a.appointmentDate), COUNT(a) " +
                    "FROM Appointment a " +
                    "WHERE a.appointmentDate BETWEEN :startDate AND :endDate " +
                    "AND a.isActive = true " +
                    "GROUP BY HOUR(a.appointmentDate)";

            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);

            List<Object[]> results = query.getResultList();

            // Create all available slots (9:00-17:00)
            for (int hour = 9; hour <= 17; hour++) {
                String timeSlot = String.format("%02d:00", hour);
                availableSlots.put(timeSlot, "AVAILABLE");
            }

            // Update booked slots based on query results
            for (Object[] result : results) {
                Integer hour = (Integer) result[0];
                Long count = (Long) result[1];

                if (hour >= 9 && hour <= 17) {
                    String timeSlot = String.format("%02d:00", hour);
                    // Assume a maximum of 3 appointments per slot
                    if (count >= 3) {
                        availableSlots.put(timeSlot, "BOOKED");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return availableSlots;
    }

    // Search appointments
    public static List<Appointment> searchAppointments(String searchTerm) {
        List<Appointment> results = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use Criteria API for multi-field fuzzy search
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Appointment> criteriaQuery = builder.createQuery(Appointment.class);
            Root<Appointment> root = criteriaQuery.from(Appointment.class);

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            // Create search conditions
            Predicate customerFirstNamePredicate = builder.like(builder.lower(root.get("customer").get("firstName")),
                    pattern);
            Predicate customerLastNamePredicate = builder.like(builder.lower(root.get("customer").get("lastName")),
                    pattern);
            Predicate vehicleMakePredicate = builder.like(builder.lower(root.get("vehicle").get("make")), pattern);
            Predicate vehicleModelPredicate = builder.like(builder.lower(root.get("vehicle").get("model")), pattern);
            Predicate serviceNamePredicate = builder.like(builder.lower(root.get("service").get("serviceName")),
                    pattern);

            // Combine search conditions
            Predicate finalPredicate = builder.or(
                    customerFirstNamePredicate,
                    customerLastNamePredicate,
                    vehicleMakePredicate,
                    vehicleModelPredicate,
                    serviceNamePredicate);

            // Only search active appointments
            Predicate isActivePredicate = builder.equal(root.get("isActive"), true);
            finalPredicate = builder.and(finalPredicate, isActivePredicate);

            // Complete the query
            criteriaQuery.where(finalPredicate);
            results = session.createQuery(criteriaQuery).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    // Update an appointment
    public static boolean updateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentId() == null) {
            return false;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Get the existing appointment from the database
            Appointment existingAppointment = session.get(Appointment.class, appointment.getAppointmentId());
            if (existingAppointment == null) {
                return false;
            }

            // Update the appointment with new values
            if (appointment.getCustomer() != null) {
                Customer customer = session.get(Customer.class, appointment.getCustomer().getCustomerId());
                existingAppointment.setCustomer(customer);
            }

            if (appointment.getVehicle() != null) {
                Vehicle vehicle = session.get(Vehicle.class, appointment.getVehicle().getVehicleId());
                existingAppointment.setVehicle(vehicle);
            }

            if (appointment.getService() != null) {
                Service service = session.get(Service.class, appointment.getService().getServiceId());
                existingAppointment.setService(service);
            }

            // The mechanic can be null (unassigned)
            if (appointment.getMechanic() != null) {
                Mechanic mechanic = session.get(Mechanic.class, appointment.getMechanic().getMechanicId());
                existingAppointment.setMechanic(mechanic);
            } else {
                existingAppointment.setMechanic(null);
            }

            // Update date and status
            existingAppointment.setAppointmentDate(appointment.getAppointmentDate());
            existingAppointment.setStatusId(appointment.getStatusId());

            // Save the updated appointment
            session.update(existingAppointment);
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
}
