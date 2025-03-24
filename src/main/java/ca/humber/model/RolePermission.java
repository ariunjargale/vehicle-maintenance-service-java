package ca.humber.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ROLE_PERMISSION")
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERMISSION_ID")
    private Long permissionId;

    @ManyToOne
    @JoinColumn(name = "ROLE_ID", nullable = false)
    private UserRole userRole;

    @Column(name = "TABLE_NAME", nullable = false)
    private String tableName;

    @Column(name = "IS_READ_ONLY", columnDefinition = "NUMBER(1) DEFAULT 1")
    private Integer isReadOnly;

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(Integer isReadOnly) {
        this.isReadOnly = isReadOnly;
    }
}