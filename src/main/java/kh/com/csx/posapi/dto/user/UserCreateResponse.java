package kh.com.csx.posapi.dto.user;

import java.util.Set;

public class UserCreateResponse {
    private Long userId;
    private String username;
    private String userType;
    private String status;
    private Set<String> permissions;

    public UserCreateResponse(Long userId, String username, String userType, String status, Set<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.status = status;
        this.permissions = permissions;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}
