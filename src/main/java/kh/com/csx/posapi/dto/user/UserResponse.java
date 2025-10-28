package kh.com.csx.posapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import kh.com.csx.posapi.entity.EmployeeEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long userId;
    private String userType;
    private String username;
    private String language;
    private String billers;
    private String warehouses;
    private Integer viewRight;
    private Double commissionRate;
    private String status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EmployeeEntity employee;
}
