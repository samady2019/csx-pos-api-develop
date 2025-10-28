package kh.com.csx.posapi.dto.adjustment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentUpdateRequest {
    @NotNull(message = "Adjustment ID is required.")
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters.")
    private String referenceNo;

    @NotNull(message = "Biller ID is required.")
    private Long billerId;

    @NotNull(message = "Warehouse ID is requried.")
    private Long warehouseId;

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;
    private String note;

    @Valid
    @NotEmpty(message = "Adjustment must contain at least one item.")
    private List<AdjustmentItemRequest> items;
}
