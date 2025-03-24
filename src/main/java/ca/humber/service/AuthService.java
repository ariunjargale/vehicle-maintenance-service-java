package ca.humber.service;

import ca.humber.dao.UsersDao;
import ca.humber.model.Users;
import ca.humber.util.HibernateUtil;
import ca.humber.util.PasswordUtil;
import ca.humber.util.SessionManager;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.CallableStatement;

public class AuthService {

    public void login(String username, String rawPassword) {
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        // Create new session - will cache it in app
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Users user = UsersDao.login(username, hashedPassword, session);

            if (user == null) {
                session.close();
                throw new RuntimeException("Invalid credentials.");
            }

        // TODO: Ari
            // Set Oracle DB session context
        /*    CallableStatement stmt = session
                    .doReturningWork(conn -> conn.prepareCall("BEGIN DBMS_SESSION.SET_CONTEXT('APP_CTX', 'USER_ID', ?); END;"));
            stmt.setInt(1, user.getUserId());
            stmt.execute();

            tx.commit();*/

            // Save to app session
            SessionManager.login(session, user);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            if (session != null && session.isOpen()) session.close();
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }

    public void logout() {
        SessionManager.logout();
    }

}