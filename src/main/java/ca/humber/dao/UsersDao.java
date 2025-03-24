package ca.humber.dao;

import ca.humber.model.Users;
import org.hibernate.Session;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class UsersDao {

    public static Users login(String username, String hashedPassword, Session session) {
        Integer userId = session.doReturningWork(conn -> {
            try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.login_user(?, ?) }")) {
                stmt.registerOutParameter(1, Types.INTEGER);
                stmt.setString(2, username);
                stmt.setString(3, hashedPassword);
                stmt.execute();
                return stmt.getInt(1);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to call login_user function", e);
            }
        });

        if (userId == null || userId <= 0) {
            return null;
        }

        return session.get(Users.class, userId.longValue());
    }


}



