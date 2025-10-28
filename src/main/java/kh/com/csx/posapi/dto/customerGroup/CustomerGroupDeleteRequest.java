package kh.com.csx.posapi.dto.customerGroup;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerGroupDeleteRequest {
    @NotNull(message = "Customer group ID is required.")
    private Object id;
}
