package kh.com.csx.posapi.dto.permission;

import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class PermissionUpdateRequest {
    @NotNull(message = "Permission ID is required")
    private Integer id;

    @NotNull(message = "Permission name cannot be empty")
    @Size(max = 255, message = "Permission name cannot exceed 255 characters")
    private String name;

}
