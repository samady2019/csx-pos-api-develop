package kh.com.csx.posapi.dto.membership;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;


@Data
public class MembershipCreateRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    private Integer point;
    private LocalDate expiredDate;
}
