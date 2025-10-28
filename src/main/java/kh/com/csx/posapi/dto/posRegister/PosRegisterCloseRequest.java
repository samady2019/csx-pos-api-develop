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
public class PosRegisterCloseRequest {
    @NotNull(message = "POS register ID is required.")
    private Long id;

    @NotNull(message = "Total cash submitted is required.")
    @PositiveOrZero(message = "Total cash submitted must be zero or positive")
    private Double totalCashSubmitted;

    @NotNull(message = "Total cheques submitted is required.")
    @PositiveOrZero(message = "Total cheques submitted must be zero or positive")
    private Integer totalChequesSubmitted;

    @NotNull(message = "Total cheques submitted is required.")
    @PositiveOrZero(message = "Total cheques submitted must be zero or positive")
    private Integer totalCcSlipsSubmitted;

    // private String transferOpenedBills;

    private String note;
}
