package ca.humber.util;

import ca.humber.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.List;
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


    public static void callProcedure(Consumer<Connection> caller) {
        Session session = SessionManager.getSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.doWork(caller::accept);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to call procedure", e);
        }
    }

    public static <T> T callFunction(Function<Connection, T> caller) {
        Session session = SessionManager.getSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            T result = session.doReturningWork(caller::apply);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to call function", e);
        }
    }

    // Call this if you need function with single result
    public static <T> T callSingleResultFunction(Function<Connection, T> caller) {
        Session session = SessionManager.getSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            T result = session.doReturningWork(caller::apply);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to call single-result function", e);
        }
    }

    // Call this if you need function with collection/table result
    public static <T> List<T> callResultListFunction(Function<Connection, List<T>> caller) {
        Session session = SessionManager.getSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            List<T> result = session.doReturningWork(caller::apply);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Failed to call result list function", e);
        }
    }

    // execute without result
    public static void executeInsideTransaction(Consumer<Session> action) {
        Transaction tx = null;
        Session session = SessionManager.getSession(); // Current user's session

        try {
            tx = session.beginTransaction();
            action.accept(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        }
    }

    // execute with result
    public static <T> T executeWithResult(Function<Session, T> function) {
        Transaction tx = null;
        Session session = SessionManager.getSession(); // Current user's session

        try {
            tx = session.beginTransaction();
            T result = function.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }

    // It'll be removed
    public static void executeInsideTransactionTemp(Consumer<Session> action) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            action.accept(session);
            tx.commit();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            e.printStackTrace();
        }
    }
    
    // It'll be removed
    public static <T> T executeWithResultTemp(Function<Session, T> function) {
        Transaction tx = null;
        try (Session session = getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            T result = function.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        }
    }
}