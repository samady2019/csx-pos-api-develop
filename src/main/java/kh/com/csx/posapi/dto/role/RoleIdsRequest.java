package kh.com.csx.posapi.dto.role;
import java.util.List;

public class RoleIdsRequest {
    private List<Integer> roleIds;

    public List<Integer> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Integer> roleIds) {
        this.roleIds = roleIds;
    }
}
