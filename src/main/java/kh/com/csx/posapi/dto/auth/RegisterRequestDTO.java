package kh.com.csx.posapi.dto.auth;

import kh.com.csx.posapi.dto.user.UserRolePermissionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private Long employeeId;
    private String userType;
    private String username;
    private String password;
    private String language;
    private String billers;
    private String warehouses;
    private Integer viewRight;
    private Double commissionRate;
    private String status;
    private UserRolePermissionRequest rolePermission;
}
