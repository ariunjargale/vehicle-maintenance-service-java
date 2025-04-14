package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.Appointment;
import ca.humber.model.Customer;
import ca.humber.model.Mechanic;
import ca.humber.model.Service;
import ca.humber.model.Vehicle;
import ca.humber.util.HibernateUtil;
import ca.humber.util.SessionManager;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TemporalType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oracle.jdbc.OracleTypes;

public class AppointmentDAO {

    // Retrieve all appointments
    public static List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            appointments = session.createQuery("FROM Appointment WHERE isActive = true ORDER BY appointmentDate", Appointment.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return appointments;
    }

    // Retrieve an appointment by ID
    public static Appointment getAppointmentById(int id) {
        try (Session session = SessionManager.getSession()) {
            return session.get(Appointment.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create a new appointment using the stored procedure
    public static boolean createAppointment(Appointment appointment) {
        try {
            return HibernateUtil.callFunction(conn -> {
                try (CallableStatement stmt = conn.prepareCall(
                        "{call appointment_pkg.sp_create_appointment(?, ?, ?, ?, ?, ?)}")) {
                    
                    stmt.setInt(1, appointment.getCustomer().getCustomerId());
                    stmt.setInt(2, appointment.getVehicle().getVehicleId());
                    stmt.setInt(3, appointment.getService().getServiceId());
                    
                    if (appointment.getMechanic() != null) {
                        stmt.setInt(4, appointment.getMechanic().getMechanicId());
                    } else {
                        stmt.setNull(4, Types.INTEGER);
                    }
                    
                    stmt.setTimestamp(5, new Timestamp(appointment.getAppointmentDate().getTime()));
                    stmt.setString(6, appointment.getStatusId());
                    
                    stmt.execute();
                    return true;
                } catch (SQLException e) {
                    handlePackageError(e, "sp_create_appointment");
                    return false;
                }
            });
        } catch (Exception e) {
            // Handle SQL errors
            Throwable cause = e;
            while (cause != null) {
                if (cause.getMessage() != null && cause.getMessage().contains("ORA-02291")) {
                    throw new RuntimeException("Foreign key constraint error: Referenced record does not exist");
                }
                else if (cause.getMessage() != null && cause.getMessage().contains("ORA-20001")) {
                    throw new RuntimeException("Low Stock Alert: Not enough inventory for the service");
                }
                cause = cause.getCause();
            }

            e.printStackTrace();
            return false;
        }
    }

    // Update appointment status using the stored procedure
    public static boolean updateAppointmentStatus(int appointmentId, String statusId) {
        try {
            return HibernateUtil.callFunction(conn -> {
                try (CallableStatement stmt = conn.prepareCall(
                        "{call appointment_pkg.sp_update_appointment_status(?, ?)}")) {
                    
                    stmt.setInt(1, appointmentId);
                    stmt.setString(2, statusId);
                    
                    stmt.execute();
                    return true;
                } catch (SQLException e) {
                    handlePackageError(e, "sp_update_appointment_status");
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Assign a mechanic to an appointment using the stored procedure
    public static boolean assignMechanic(int appointmentId, int mechanicId) {
        try {
            return HibernateUtil.callFunction(conn -> {
                try (CallableStatement stmt = conn.prepareCall(
                        "{call appointment_pkg.sp_assign_mechanic(?, ?)}")) {
                    
                    stmt.setInt(1, appointmentId);
                    stmt.setInt(2, mechanicId);
                    
                    stmt.execute();
                    return true;
                } catch (SQLException e) {
                    handlePackageError(e, "sp_assign_mechanic");
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an appointment (soft delete) using the stored procedure
    public static boolean deleteAppointment(int appointmentId) throws ConstraintException {
        try {
            return HibernateUtil.callFunction(conn -> {
                try (CallableStatement stmt = conn.prepareCall(
                        "{call appointment_pkg.sp_delete_appointment(?)}")) {
                    
                    stmt.setInt(1, appointmentId);
                    
                    stmt.execute();
                    return true;
                } catch (SQLException e) {
                    handlePackageError(e, "sp_delete_appointment");
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConstraintException("Unable to delete appointment, there may be associated records.");
        }
    }

    // Get available appointment slots using the stored procedure
    public static Map<String, String> getAvailableSlots(Date date) {
        Map<String, String> availableSlots = new HashMap<>();

        try {
            List<Map.Entry<String, String>> results = HibernateUtil.callResultListFunction(conn -> {
                List<Map.Entry<String, String>> slots = new ArrayList<>();
                
                try (CallableStatement stmt = conn.prepareCall(
                        "{? = call appointment_pkg.sp_get_available_slots(?)}")) {
                    
                    stmt.registerOutParameter(1, OracleTypes.CURSOR);
                    stmt.setDate(2, new java.sql.Date(date.getTime()));
                    
                    stmt.execute();
                    
                    try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                        while (rs.next()) {
                            String timeSlot = rs.getString("TIME_SLOT");
                            String status = rs.getString("STATUS");
                            slots.add(Map.entry(timeSlot, status));
                        }
                    }
                } catch (SQLException e) {
                    handlePackageError(e, "sp_get_available_slots");
                }
                
                return slots;
            });
            
            // Convert the list of entries to a map
            for (Map.Entry<String, String> entry : results) {
                availableSlots.put(entry.getKey(), entry.getValue());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
            // Fallback to the original implementation if there's an error
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
                String hql = "SELECT EXTRACT(HOUR FROM a.appointmentDate), COUNT(a) " +
                        "FROM Appointment a " +
                        "WHERE a.appointmentDate BETWEEN :startDate AND :endDate " +
                        "AND a.isActive = true " +
                        "GROUP BY EXTRACT(HOUR FROM a.appointmentDate)";
    
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return availableSlots;
    }

    // Search appointments - keep using Hibernate for complex search
    public static List<Appointment> searchAppointments(String searchTerm) {
        List<Appointment> results = new ArrayList<>();
        try (Session session = SessionManager.getSession()) {
            // Use Criteria API for multi-field fuzzy search
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Appointment> criteriaQuery = builder.createQuery(Appointment.class);
            Root<Appointment> root = criteriaQuery.from(Appointment.class);

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            
            // Create search conditions for all relevant fields
            List<Predicate> predicates = new ArrayList<>();
            
            // Customer information
            predicates.add(builder.like(builder.lower(root.get("customer").get("name")), pattern));
            predicates.add(builder.like(builder.lower(root.get("customer").get("phone")), pattern));
            predicates.add(builder.like(builder.lower(root.get("customer").get("email")), pattern));
            
            // Vehicle information
            predicates.add(builder.like(builder.lower(root.get("vehicle").get("make")), pattern));
            predicates.add(builder.like(builder.lower(root.get("vehicle").get("model")), pattern));
            predicates.add(builder.like(builder.lower(root.get("vehicle").get("vin")), pattern));
            predicates.add(builder.like(builder.function("to_char", String.class, root.get("vehicle").get("year")), pattern));
            
            // Service information
            predicates.add(builder.like(builder.lower(root.get("service").get("serviceName")), pattern));
            predicates.add(builder.like(builder.lower(root.get("service").get("serviceTypeId")), pattern));
            
            // Mechanic information
            predicates.add(builder.like(builder.lower(root.get("mechanic").get("name")), pattern));
            predicates.add(builder.like(builder.lower(root.get("mechanic").get("specialization")), pattern));
            
            // Appointment status
            predicates.add(builder.like(builder.lower(root.get("statusId")), pattern));
            
            // Appointment ID
            predicates.add(builder.like(builder.function("to_char", String.class, root.get("appointmentId")), pattern));
            
            // Date - try to match both full date format and partial formats like year or month
            predicates.add(builder.like(builder.function("to_char", String.class, root.get("appointmentDate"), 
                    builder.literal("YYYY-MM-DD HH24:MI")), pattern));
            predicates.add(builder.like(builder.function("to_char", String.class, root.get("appointmentDate"), 
                    builder.literal("YYYY-MM-DD")), pattern));
            predicates.add(builder.like(builder.function("to_char", String.class, root.get("appointmentDate"), 
                    builder.literal("YYYY-MM")), pattern));
            predicates.add(builder.like(builder.function("to_char", String.class, root.get("appointmentDate"), 
                    builder.literal("YYYY")), pattern));
            predicates.add(builder.like(builder.function("to_char", String.class, root.get("appointmentDate"), 
                    builder.literal("HH24:MI")), pattern));
            
            // Combine all search conditions with OR
            Predicate finalPredicate = builder.or(predicates.toArray(new Predicate[0]));

            // Only search active appointments
            Predicate isActivePredicate = builder.equal(root.get("isActive"), true);
            finalPredicate = builder.and(finalPredicate, isActivePredicate);

            // Complete the query with ordering by appointment date (newest first)
            criteriaQuery.where(finalPredicate);
            criteriaQuery.orderBy(builder.desc(root.get("appointmentDate")));
            
            results = session.createQuery(criteriaQuery).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    // Update an appointment - combine multiple calls since there's no direct equivalent
    public static boolean updateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentId() == null) {
            return false;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            try {
                // First get the existing appointment
                Appointment existingAppointment = session.get(Appointment.class, appointment.getAppointmentId());
                if (existingAppointment == null) {
                    return false;
                }

                // Update the individual fields
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

                // Update mechanic using the stored procedure if it changed
                if ((existingAppointment.getMechanic() == null && appointment.getMechanic() != null) ||
                    (existingAppointment.getMechanic() != null && appointment.getMechanic() == null) ||
                    (existingAppointment.getMechanic() != null && appointment.getMechanic() != null && 
                     !existingAppointment.getMechanic().getMechanicId().equals(appointment.getMechanic().getMechanicId()))) {
                    
                    Integer mechanicId = appointment.getMechanic() != null ? 
                                        appointment.getMechanic().getMechanicId() : null;
                                        
                    session.doWork(conn -> {
                        if (mechanicId != null) {
                            try (CallableStatement stmt = conn.prepareCall(
                                    "{call appointment_pkg.sp_assign_mechanic(?, ?)}")) {
                                stmt.setInt(1, appointment.getAppointmentId());
                                stmt.setInt(2, mechanicId);
                                stmt.execute();
                            } catch (SQLException e) {
                                handlePackageError(e, "sp_assign_mechanic");
                                throw new RuntimeException(e);
                            }
                        } else {
                            // Handle null mechanic - this would need a new stored procedure
                            // For now, update directly
                            existingAppointment.setMechanic(null);
                        }
                    });
                }

                // 確保這個條件正確評估
                if (!existingAppointment.getStatusId().equals(appointment.getStatusId())) {
                    session.doWork(conn -> {
                        try (CallableStatement stmt = conn.prepareCall(
                                "{call appointment_pkg.sp_update_appointment_status(?, ?)}")) {
                            stmt.setInt(1, appointment.getAppointmentId());
                            stmt.setString(2, appointment.getStatusId());
                            stmt.execute();
                        } catch (SQLException e) {
                            handlePackageError(e, "sp_update_appointment_status");
                            throw new RuntimeException(e);
                        }
                    });
                }

                // Update the remaining fields (date)
                existingAppointment.setAppointmentDate(appointment.getAppointmentDate());

                // Save the updated appointment
                session.update(existingAppointment);
                transaction.commit();
                return true;
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if the specified appointment time conflicts with existing appointments.
     * This is not available in the package, so keep the original implementation
     */
    public static boolean isTimeColliding(Date appointmentDate, Integer currentAppointmentId) {
        try (Session session = SessionManager.getSession()) {
            // Set the allowed appointment duration (in minutes)
            final int APPOINTMENT_DURATION_MINUTES = 60;

            // Create the time range to check (30 minutes before and after the appointment time)
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(appointmentDate);
            startCal.add(Calendar.MINUTE, -APPOINTMENT_DURATION_MINUTES / 2);
            Date startTime = startCal.getTime();

            Calendar endCal = Calendar.getInstance();
            endCal.setTime(appointmentDate);
            endCal.add(Calendar.MINUTE, APPOINTMENT_DURATION_MINUTES / 2);
            Date endTime = endCal.getTime();

            // Create the query
            String hql = "SELECT COUNT(a) FROM Appointment a " +
                    "WHERE a.isActive = true " +
                    "AND a.appointmentDate BETWEEN :startTime AND :endTime";

            // Exclude the current appointment if editing an existing one
            if (currentAppointmentId != null) {
                hql += " AND a.appointmentId != :appointmentId";
            }

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);

            if (currentAppointmentId != null) {
                query.setParameter("appointmentId", currentAppointmentId);
            }

            Long count = query.uniqueResult();

            // Check if the time slot has reached the maximum number of appointments (assume max 3 per slot)
            return count >= 3;
        } catch (Exception e) {
            System.err.println("Error checking time collision: " + e.getMessage());
            e.printStackTrace();
            return false; // Default to no conflict on error, but log the error
        }
    }

    // Helper method to handle package errors
    private static void handlePackageError(SQLException e, String procedureName) throws RuntimeException {
        if (e.getErrorCode() == -6550 || e.getMessage().contains("not declared")) {
            System.err.println("Warning: Database procedure 'appointment_pkg." + procedureName + "' not found.");
            throw new RuntimeException("Required database procedure not found. Contact your system administrator.");
        } else {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }
}
