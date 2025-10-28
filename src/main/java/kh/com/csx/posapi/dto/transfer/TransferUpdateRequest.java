package kh.com.csx.posapi.dto.transfer;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferUpdateRequest {
    @NotNull(message = "Transfer ID is required.")
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters.")
    private String referenceNo;

    @NotNull(message = "From biller ID is required.")
    private Long fromBiller;

    @NotNull(message = "To biller ID is required.")
    private Long toBiller;

    @NotNull(message = "From warehouse ID is required.")
    private Long fromWarehouse;

    @NotNull(message = "To warehouse ID is required.")
    private Long toWarehouse;

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;

    private String note;

    @NotBlank(message = "Status is required.")
    @Size(max = 50, message = "Status should not exceed {max} characters.")
    private String status;

    @Valid
    @NotEmpty(message = "Transfer must contain at least one item")
    private List<TransferItemRequest> items;
}
