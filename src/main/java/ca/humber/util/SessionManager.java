package ca.humber.util;

import ca.humber.model.RolePermission;
import ca.humber.model.User;
import org.hibernate.Session;

import java.util.List;

public class SessionManager {

	private static User currentUser;
	private static Session session;
	private static List<RolePermission> rolePermissions;

	private SessionManager() {
	}

	public static void setSession(Session activeSession) {
		session = activeSession;
	}

	public static void login(User user, List<RolePermission> permissions) {
		currentUser = user;
		rolePermissions = permissions;
	}

	public static void logout() {
		if (session != null && session.isOpen()) {
			session.close();
		}
		session = null;
		currentUser = null;
		rolePermissions = null;
	}

	public static User getCurrentUser() {
		if (currentUser == null) {
			throw new IllegalStateException("No user is currently logged in.");
		}
		return currentUser;
	}

	public static Session getSession() {
		if (session == null || !session.isOpen()) {
			throw new IllegalStateException("Hibernate session is not initialized or already closed.");
		}
		return session;
	}

	public static List<RolePermission> getRolePermissions() {
		if (rolePermissions == null) {
			throw new IllegalStateException("No role permissions loaded for the current user.");
		}
		return rolePermissions;
	}

	public static boolean isLoggedIn() {
		return currentUser != null && session != null && session.isOpen();
	}
}