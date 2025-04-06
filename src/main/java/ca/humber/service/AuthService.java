package ca.humber.service;

import ca.humber.dao.UsersDao;
import ca.humber.model.RolePermission;
import ca.humber.model.User;
import ca.humber.util.HibernateUtil;
import ca.humber.util.PasswordUtil;
import ca.humber.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AuthService {

	public void login(String username, String rawPassword) {
		String hashedPassword = PasswordUtil.hashPassword(rawPassword);
		// Create new session - will cache it in app
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();

			SessionManager.setSession(session);
			User user = UsersDao.login(username, hashedPassword);

			if (user == null) {
				session.close();
				throw new RuntimeException("Invalid credentials.");
			}

			List<RolePermission> rolePermissions = UsersDao.getRolePermissions(user.getRoleId());

			// Save to app session
			SessionManager.login(user, rolePermissions);

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			if (session != null && session.isOpen())
				session.close();
			throw new RuntimeException("Login failed: " + e.getMessage(), e);
		}
	}

	public void logout() {
		SessionManager.logout();
	}

}