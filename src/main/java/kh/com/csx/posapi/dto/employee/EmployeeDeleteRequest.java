package kh.com.csx.posapi.dto.employee;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDeleteRequest {
    @NotNull(message = "Employee ID is required.")
    private Object id;
}
