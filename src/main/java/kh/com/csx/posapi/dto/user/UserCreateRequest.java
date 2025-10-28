package kh.com.csx.posapi.dto.user;

import java.util.Set;

public class UserCreateRequest {
    private String username;
    private String password;
    private String userType;
    private String status;
    private Set<Integer> roles;
    private Set<Integer> roleIds; //Optional
    private Set<Integer> specialPermissions; //Optional

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Integer> getRoles() {
        return roles;
    }

    public void setRoles(Set<Integer> roles) {
        this.roles = roles;
    }

    public Set<Integer> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Integer> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<Integer> getSpecialPermissions(){
        return specialPermissions;
    }

    public void setSpecialPermissions(Set<Integer> specialPermissions){
        this.specialPermissions = specialPermissions;
    }
}
