package kh.com.csx.posapi.dto.membership;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipDeleteRequest {
    @NotNull(message = "Membership ID is required.")
    private Long id;
}
