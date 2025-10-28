package kh.com.csx.posapi.dto.posRegister;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosRegisterOpenRequest {
    @NotNull(message = "Cash in hand is required.")
    @PositiveOrZero(message = "Cash in hand must be zero or positive")
    private Double cashInHand;
}
