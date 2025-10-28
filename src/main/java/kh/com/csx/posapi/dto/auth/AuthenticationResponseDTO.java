package kh.com.csx.posapi.dto.auth;

import kh.com.csx.posapi.dto.user.UserResponse;
import kh.com.csx.posapi.entity.EmployeeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponseDTO {
    private UserResponse user;
    private String token;
}
