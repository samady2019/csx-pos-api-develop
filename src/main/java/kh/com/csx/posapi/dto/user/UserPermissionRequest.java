package kh.com.csx.posapi.dto.user;

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
public class UserPermissionRequest {
    private List<PermissionEntity> permissions;
}
