package ca.humber.dao;

import ca.humber.exceptions.ConstraintException;
import ca.humber.model.RolePermission;
import ca.humber.model.UserRole;
import ca.humber.model.Users;
import ca.humber.util.HibernateUtil;
import oracle.jdbc.OracleTypes;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsersDao {

    public static Users login(String username, String hashedPassword, Session session) {
        Users res = session.doReturningWork(conn -> {
            try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.login_user(?, ?) }")) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.setString(2, username);
                stmt.setString(3, hashedPassword);
                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    if (rs != null && rs.next()) {
                        Users result = new Users();
                        result.setUserId(rs.getInt("user_id"));
                        result.setUsername(rs.getString("username"));

                        UserRole role = new UserRole();
                        role.setRoleId(rs.getLong("role_id"));

                        result.setUserRole(role);

                        return result;
                    } else {
                        return null; // login failed
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to call login_user function", e);
            }
        });

        return res;
    }

    public static List<RolePermission> getRolePermissions(Long roleId, Session session) {
        return session.doReturningWork(conn -> {
            List<RolePermission> permissions = new ArrayList<>();

            try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.get_role_permissions(?) }")) {
                stmt.registerOutParameter(1, OracleTypes.CURSOR);
                stmt.setLong(2, roleId);
                stmt.execute();

                try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                    while (rs.next()) {
                        RolePermission rp = new RolePermission();
                        rp.setTableName(rs.getString("table_name"));
                        rp.setIsReadOnly(rs.getInt("is_read_only")); // 1 бол true

                        permissions.add(rp);
                    }
                }

            } catch (SQLException e) {
                throw new RuntimeException("Failed to call get_role_permissions", e);
            }

            return permissions;
        });
    }


    public static List<Users> getUserList() {
        return HibernateUtil.executeWithResult(session -> {
            Query<Users> query = session.createQuery("FROM Users WHERE isActive = 1", Users.class);
            return query.list();
        });
    }

    public static List<Users> search(String searchTerm) {
        return HibernateUtil.executeWithResult(session -> {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            Query<Users> query = session.createQuery(
                    "FROM Users u WHERE u.isActive = 1 AND (LOWER(u.username) LIKE :searchPattern)",
                    Users.class);
            query.setParameter("searchPattern", searchPattern);
            return query.list();
        });
    }

    public static List<UserRole> getRoleList() {
        return HibernateUtil.executeWithResult(session -> {
            Query<UserRole> query = session.createQuery("FROM UserRole", UserRole.class);
            return query.list();
        });
    }

    public static void insertUser(Users user) {
        HibernateUtil.executeInsideTransaction(session -> session.save(user));
        System.out.println("User added successfully.");
    }

    public static boolean updateUser(Users user) {
        try {
            HibernateUtil.executeInsideTransaction(session -> session.update(user));
            System.out.println("User updated successfully.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteUser(int userId) throws ConstraintException {
        Users users = HibernateUtil.executeWithResult(session -> session.get(Users.class, userId));

        if (users == null) {
            System.out.println("User with ID " + userId + " not found.");
            return false;
        }

        try {
            // Soft delete - only mark the status as inactive
            users.setIsActive(0);
            HibernateUtil.executeInsideTransaction(session -> session.update(users));
            System.out.println("User deactivated successfully.");
            return true;
        } catch (ConstraintViolationException e) {
            throw new ConstraintException("Cannot delete user. There are record for this user.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}



