package kh.com.csx.posapi.dto.customer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerDeleteRequest {
    @NotNull(message = "Customer ID is required.")
    private Long id;
}
