package kh.com.csx.posapi.dto.transfer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDeleteRequest {
    @NotNull(message = "Transfer ID is required.")
    private Object id;
}
