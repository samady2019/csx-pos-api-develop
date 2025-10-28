package kh.com.csx.posapi.dto.suspendedBill;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuspendedBillDeleteRequest {
    @NotNull(message = "Suspend ID is required.")
    private Object id;
}
