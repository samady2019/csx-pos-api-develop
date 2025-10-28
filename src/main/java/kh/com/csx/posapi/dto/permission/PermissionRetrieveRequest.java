package kh.com.csx.posapi.dto.permission;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class PermissionRetrieveRequest {
    @NotNull(message = "Permission ID is required.")
    private Integer id;
}
