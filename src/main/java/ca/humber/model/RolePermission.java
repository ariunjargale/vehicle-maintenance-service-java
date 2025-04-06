package ca.humber.model;

public class RolePermission {
    private int permissionId;
    private int roleId;
    private String tableName;
    private int isReadOnly;

    public int getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(int isReadOnly) {
        this.isReadOnly = isReadOnly;
    }
}