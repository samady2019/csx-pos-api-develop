package kh.com.csx.posapi.dto.employee;

import kh.com.csx.posapi.entity.EmployeeEntity;
import kh.com.csx.posapi.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponse {
    private EmployeeEntity employee;
    private List<UserResponse> users;
}
