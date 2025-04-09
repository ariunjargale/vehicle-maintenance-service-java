package ca.humber.dao;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.management.relation.Role;

import ca.humber.model.RolePermission;
import ca.humber.model.UserRole;
import ca.humber.util.HibernateUtil;
import oracle.jdbc.OracleTypes;

public class RoleDAO {

	RoleDAO() {
	}

	public static List<UserRole> getRoles() {
		return HibernateUtil.callResultListFunction(conn -> {
			List<UserRole> roles = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call role_pkg.get_roles }")) {
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
				throw new RuntimeException(e.getMessage(), e);
			}

			return roles;
		});
	}

	public static List<UserRole> getRolesByName(String roleName) {
		return HibernateUtil.callResultListFunction(conn -> {
			List<UserRole> roles = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call role_pkg.get_roles_by_name(?) }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.setString(2, roleName.concat("%"));
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
				throw new RuntimeException(e.getMessage(), e);
			}

			return roles;
		});
	}

	public static UserRole getRoleById(int roleId) {
		return HibernateUtil.callFunction(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ ? = call role_pkg.get_role_by_id(?) }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.setInt(2, roleId);
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					if (rs.next()) {
						UserRole role = new UserRole();
						role.setRoleId(rs.getInt("role_id"));
						role.setRoleName(rs.getString("role_name"));
						return role;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}

			return null;
		});
	}

	public static void insertRole(String roleName) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call role_pkg.insert_role(?) }")) {
				stmt.setString(1, roleName);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	public static void updateRole(int roleId, String roleName) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call role_pkg.update_role(?, ?) }")) {
				stmt.setInt(1, roleId);
				stmt.setString(2, roleName);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	public static void deleteRole(int roleId) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call role_pkg.delete_role(?) }")) {
				stmt.setInt(1, roleId);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	public static List<RolePermission> getPermissionsByRole(int roleId) {
		return HibernateUtil.callResultListFunction(conn -> {
			List<RolePermission> permissions = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call role_pkg.get_permissions_by_role(?) }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.setInt(2, roleId);
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					while (rs.next()) {
						RolePermission perm = new RolePermission();
						perm.setPermissionId(rs.getInt("permission_id"));
						perm.setTableName(rs.getString("table_name"));
						perm.setIsReadOnly(rs.getInt("is_read_only"));
						permissions.add(perm);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}

			return permissions;
		});
	}

	public static List<RolePermission> getPermissionsByName(int roleId, String tableName) {
		return HibernateUtil.callResultListFunction(conn -> {
			List<RolePermission> permissions = new ArrayList<>();

			try (CallableStatement stmt = conn.prepareCall("{ ? = call role_pkg.get_permissions_by_name(?, ?) }")) {
				stmt.registerOutParameter(1, OracleTypes.CURSOR);
				stmt.setInt(2, roleId);
				stmt.setString(3, tableName.concat("%"));
				stmt.execute();

				try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
					while (rs.next()) {
						RolePermission perm = new RolePermission();
						perm.setPermissionId(rs.getInt("permission_id"));
						perm.setTableName(rs.getString("table_name"));
						perm.setIsReadOnly(rs.getInt("is_read_only"));
						permissions.add(perm);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}

			return permissions;
		});
	}

	public static void insertPermission(int roleId, String tableName, int isReadOnly) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call role_pkg.insert_permission(?, ?, ?) }")) {
				stmt.setInt(1, roleId);
				stmt.setString(2, tableName);
				stmt.setInt(3, isReadOnly);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}

	public static void updatePermission(int permissionId, String tableName, int isReadOnly) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call role_pkg.update_permission(?, ?, ?) }")) {
				stmt.setInt(1, permissionId);
				stmt.setString(2, tableName);
				stmt.setInt(3, isReadOnly);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void deletePermission(int permissionId) {
		HibernateUtil.callProcedure(conn -> {
			try (CallableStatement stmt = conn.prepareCall("{ call role_pkg.delete_permission(?) }")) {
				stmt.setInt(1, permissionId);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}
}
