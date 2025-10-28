package kh.com.csx.posapi.dto.permission;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class PermissionDeleteRequest {
    @NotNull(message = "ID is require for deletion")
    private Integer id;
}
