package ca.humber.util;

import ca.humber.model.Users;
import org.hibernate.Session;

public class SessionManager {

    private static Users currentUser;
    private static Session session;

    private SessionManager() {
    }

    public static void login(Session activeSession, Users user) {
        session = activeSession;
        currentUser = user;
    }

    public static void logout() {
        if (session != null && session.isOpen()) {
            session.close();
        }
        session = null;
        currentUser = null;
    }

    public static Users getCurrentUser() {
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

    public static boolean isLoggedIn() {
        return currentUser != null && session != null && session.isOpen();
    }
}