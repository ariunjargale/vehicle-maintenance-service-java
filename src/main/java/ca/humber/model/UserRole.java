package ca.humber.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "USER_ROLE")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROLE_ID")
    private Long roleId;

    @Column(name = "ROLE_NAME", nullable = false, unique = true)
    private String roleName;

    @OneToMany(mappedBy = "userRole", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<Users> users;

    @OneToMany(mappedBy = "userRole", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<RolePermission> rolePermissions;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }

    public List<RolePermission> getRolePermissions() {
        return rolePermissions;
    }

    public void setRolePermissions(List<RolePermission> rolePermissions) {
        this.rolePermissions = rolePermissions;
    }
}


