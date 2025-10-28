package kh.com.csx.posapi.dto.permission;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class PermissionCreateRequest {
    @NotBlank(message = "Permission name cannot be empty")
    @Size(max = 255, message = "Permission name cannot exceed 255 characters")
    private String name;

}
