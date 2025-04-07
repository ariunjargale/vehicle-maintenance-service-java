package ca.humber.dao;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ca.humber.model.RolePermission;
import ca.humber.model.User;
import ca.humber.model.UserRole;
import ca.humber.util.HibernateUtil;
import oracle.jdbc.OracleTypes;

public class UsersDao {

	public static User login(String username, String hashedPassword) {
		return HibernateUtil.callFunction(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.login(?, ?) }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.setString(2, username);
				stmt.setString(3, hashedPassword);
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					if (rs != null && rs.next()) {
						User result = new User();
						result.setUserId(rs.getInt("user_id"));
						result.setUsername(rs.getString("username"));
						result.setRoleId(rs.getInt("role_id"));
						return result;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}

			return null;
		});
	}

	public static List<RolePermission> getRolePermissions(int roleId) {
		return HibernateUtil.callResultListFunction(conn -> {
			List<RolePermission> permissions = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.get_role_permissions(?) }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.setLong(2, roleId);
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					while (rs.next()) {
						RolePermission rp = new RolePermission();
						rp.setTableName(rs.getString("table_name"));
						rp.setIsReadOnly(rs.getInt("is_read_only"));

						permissions.add(rp);
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}

			return permissions;
		});
	}

	public static List<UserRole> getUserRoles() {
		return HibernateUtil.callResultListFunction(conn -> {
			List<UserRole> roles = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.get_user_roles }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					while (rs.next()) {
						UserRole role = new UserRole();
						role.setRoleId(rs.getInt("role_id"));
						role.setRoleName(rs.getString("role_name"));
						roles.add(role);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return roles;
		});
	}

	public static List<User> getUsers() {
		return HibernateUtil.callResultListFunction(conn -> {
			List<User> users = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.get_users }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					while (rs.next()) {
						User user = new User();
						user.setUserId(rs.getInt("user_id"));
						user.setUsername(rs.getString("username"));
						user.setRoleId(rs.getInt("role_id"));
						user.setIsActive(rs.getInt("is_active"));
						users.add(user);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return users;
		});
	}

	public static List<User> getUserByUsername(String username) {
		return HibernateUtil.callResultListFunction(conn -> {
			List<User> users = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call user_pkg.get_user_by_username(?) }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.setString(2, username.concat("%"));
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					while (rs.next()) {
						User user = new User();
						user.setUserId(rs.getInt("user_id"));
						user.setUsername(rs.getString("username"));
						user.setRoleId(rs.getInt("role_id"));
						user.setIsActive(rs.getInt("is_active"));
						users.add(user);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return users;
		});
	}

	public static void insertUser(User user) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call user_pkg.insert_user(?, ?, ?) }")) {
				stmt.setString(1, user.getUsername());
				stmt.setString(2, user.getPassword());
				stmt.setInt(3, user.getRoleId());
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void updateUser(User user) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call user_pkg.update_user(?, ?, ?) }")) {
				stmt.setInt(1, user.getUserId());
				stmt.setString(2, user.getUsername());
				stmt.setInt(3, user.getRoleId());
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void resetPassword(int userId, String newPassword) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call user_pkg.reset_password(?, ?) }")) {
				stmt.setInt(1, userId);
				stmt.setString(2, newPassword);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace(); // optionally log
				throw new RuntimeException("❌ Failed to reset password", e);
			}
		});
	}

	public static void deactivateUser(int userId) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call user_pkg.deactivate_user(?) }")) {
				stmt.setInt(1, userId);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
}
