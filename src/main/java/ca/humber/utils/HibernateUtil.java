/**
 *
 * @author Ariunjargal Erdenebaatar
 * @created Mar 6, 2025
 */
package ca.humber.utils;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static SessionFactory buildSessionFactory() {
		try {
			return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("SessionFactory creation failed: " + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static void shutdown() {
		getSessionFactory().close();
	}
}
