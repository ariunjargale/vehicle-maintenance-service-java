package ca.humber.util;

import ca.humber.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Course.class)
                    .addAnnotatedClass(Vehicle.class)
                    .addAnnotatedClass(Customer.class)
                    .addAnnotatedClass(Users.class)
                    .addAnnotatedClass(UserRole.class)
                    .addAnnotatedClass(RolePermission.class)
                    .addAnnotatedClass(Service.class)
                    .addAnnotatedClass(Mechanic.class)
                    .addAnnotatedClass(Appointment.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Execute a stored procedure without returning a result.
     * @param caller Logic for calling the stored procedure.
     */
    public static void callProcedure(Consumer<Connection> caller) {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.doWork(caller::accept);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to call procedure", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Execute a stored function and return a result.
     * @param <T> The return type.
     * @param caller Logic for calling the stored function.
     * @return The result of the function execution.
     */
    public static <T> T callFunction(Function<Connection, T> caller) {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            T result = session.doReturningWork(caller::apply);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to call function", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Execute an operation inside a transaction without returning a result.
     * @param action The operation to execute.
     */
    public static void executeInsideTransaction(Consumer<Session> action) {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            action.accept(session);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to execute action inside transaction", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Execute an operation inside a transaction and return a result.
     * @param <T> The return type.
     * @param function The function to execute.
     * @return The result of the function execution.
     */
    public static <T> T executeWithResult(Function<Session, T> function) {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            T result = function.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to execute function with result", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}