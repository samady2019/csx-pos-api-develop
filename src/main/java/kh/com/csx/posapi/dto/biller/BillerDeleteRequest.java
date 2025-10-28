package kh.com.csx.posapi.dto.biller;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillerDeleteRequest {
    @NotNull(message = "Biller ID is required.")
    private Object id;
}
