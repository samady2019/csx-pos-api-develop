package kh.com.csx.posapi.dto.permission;

import kh.com.csx.posapi.entity.PermissionEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
    private PermissionEntity permission;
}
