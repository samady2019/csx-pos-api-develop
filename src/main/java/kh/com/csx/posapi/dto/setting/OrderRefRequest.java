package kh.com.csx.posapi.dto.setting;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRefRequest {
    @NotNull(message = "Biller ID is required.")
    private Long billerId;
}
