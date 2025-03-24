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
                    .addAnnotatedClass(Course.class)
                    .addAnnotatedClass(Users.class)
                    .addAnnotatedClass(UserRole.class)
                    .addAnnotatedClass(RolePermission.class)
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

}