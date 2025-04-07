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
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();
			SessionManager.setSession(session);
			User user = UsersDao.login(username, hashedPassword);
			if (user == null) {
				throw new RuntimeException("Invalid credentials.");
			}

			List<RolePermission> rolePermissions = UsersDao.getRolePermissions(user.getRoleId());

			SessionManager.login(user, rolePermissions);

		} catch (Exception ex) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw ex;
		}
	}

	public void logout() {
		SessionManager.logout();
	}

}