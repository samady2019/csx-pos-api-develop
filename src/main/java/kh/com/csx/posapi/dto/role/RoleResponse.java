package kh.com.csx.posapi.dto.role;

import kh.com.csx.posapi.entity.PermissionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    private int roleId;
    private String name;
    private List<PermissionEntity> permissions;
}
