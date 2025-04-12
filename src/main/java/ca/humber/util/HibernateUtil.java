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
	private static final Object sessionLock = new Object();

	private static SessionFactory buildSessionFactory() {
		try {
			return new Configuration().configure("hibernate.cfg.xml")
					.addAnnotatedClass(Vehicle.class).addAnnotatedClass(Customer.class).addAnnotatedClass(User.class)
					.addAnnotatedClass(UserRole.class).addAnnotatedClass(RolePermission.class)
					.addAnnotatedClass(Service.class).addAnnotatedClass(Mechanic.class)
					.addAnnotatedClass(Appointment.class).buildSessionFactory();
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void callProcedure(Consumer<Connection> caller) {
		Connection conn = SessionManager.getDbConnection();
		if (conn != null) {
			try {
				caller.accept(conn);
				return;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		Session session = SessionManager.getSession();
		synchronized (sessionLock) {
			Transaction tx = session.getTransaction();
			try {
				if (tx == null || !tx.isActive()) {
					tx = session.beginTransaction();
				}
				session.doWork(caller::accept);
				tx.commit();
			} catch (Exception e) {
				if (tx != null && tx.isActive())
					tx.rollback();
				throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
			}
		}
	}

	public static <T> T callFunction(Function<Connection, T> caller) {
		Connection conn = SessionManager.getDbConnection();
		if (conn != null) {
			try {
				return caller.apply(conn);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		Session session = SessionManager.getSession();
		synchronized (sessionLock) {
			Transaction tx = session.getTransaction();
			try {
				if (tx == null || !tx.isActive()) {
					tx = session.beginTransaction();
				}
				T result = session.doReturningWork(caller::apply);
				tx.commit();
				return result;
			} catch (Exception e) {
				if (tx != null && tx.isActive())
					tx.rollback();
				throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
			}
		}
	}

	public static <T> List<T> callResultListFunction(Function<Connection, List<T>> caller) {
		Connection conn = SessionManager.getDbConnection();
		if (conn != null) {
			try {
				return caller.apply(conn);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		
		Session session = SessionManager.getSession();
		synchronized (sessionLock) {
			Transaction tx = session.getTransaction();
			try {
				if (tx == null || !tx.isActive()) {
					tx = session.beginTransaction();
				}
				List<T> result = session.doReturningWork(caller::apply);
				tx.commit();
				return result;
			} catch (Exception e) {
				if (tx != null && tx.isActive())
					tx.rollback();
				throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
			}
		}
	}

	public static void executeInsideTransaction(Consumer<Session> action) {
		Session session = SessionManager.getSession();
		synchronized (sessionLock) {
			Transaction tx = session.getTransaction();
			try {
				if (tx == null || !tx.isActive()) {
					tx = session.beginTransaction();
				}
				action.accept(session);
				tx.commit();
			} catch (Exception e) {
				if (tx != null)
					tx.rollback();
				e.printStackTrace();
			}
		}
	}

	public static <T> T executeWithResult(Function<Session, T> function) {
		Session session = SessionManager.getSession();
		synchronized (sessionLock) {
			Transaction tx = session.getTransaction();
			try {
				if (tx == null || !tx.isActive()) {
					tx = session.beginTransaction();
				}
				T result = function.apply(session);
				tx.commit();
				return result;
			} catch (Exception e) {
				if (tx != null)
					tx.rollback();
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String message(Exception e) {
		String defaultMessage = "An unexpected error occurred.";
		Throwable cause = e;

		while (cause != null) {
			String msg = cause.getMessage();
			if (msg != null) {
				if (msg.contains("ORA-00001")) {
					return "Duplicate entry: A unique constraint has been violated.";
				} else if (msg.contains("ORA-02291")) {
					return "Foreign key constraint error: Related record not found.";
				} else if (msg.contains("ORA-02292")) {
					return "Cannot delete or update: Child records exist.";
				} else if (msg.contains("ORA-20001")) {
					// Custom application error
					return msg.split("\n")[0].replace("ORA-20001:", "").trim();
				}
			}
			cause = cause.getCause();
		}

		// Fallback to root message or default
		return (e.getMessage() != null) ? e.getMessage().split("\n")[0] : defaultMessage;
	}
}
