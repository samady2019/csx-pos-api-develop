package kh.com.csx.posapi.dto.auth;
import java.util.Set;

public class SpecialPermissionRequest {
    private Set<Integer> specialPermissions;

    public Set<Integer> getSpecialPermissions() {
        return specialPermissions;
    }

    public void setSpecialPermissions(Set<Integer> specialPermissions) {
        this.specialPermissions = specialPermissions;
    }
}
