package kh.com.csx.posapi.dto.user;

import kh.com.csx.posapi.entity.PermissionEntity;
import kh.com.csx.posapi.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRolePermissionResponse {
    private Long userId;
    private String username;
    private String user_type;
    private String status;
    private List<RoleEntity> roles;
    private List<PermissionEntity> permissions;
}
