package ca.humber.service;

import ca.humber.dao.UsersDao;
import ca.humber.model.RolePermission;
import ca.humber.model.Users;
import ca.humber.util.HibernateUtil;
import ca.humber.util.PasswordUtil;
import ca.humber.util.SessionManager;
import oracle.jdbc.OracleTypes;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

            List<RolePermission> rolePermissions = UsersDao.getRolePermissions(user.getUserRole().getRoleId(), session);

            // Save to app session
            SessionManager.login(session, user, rolePermissions);

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