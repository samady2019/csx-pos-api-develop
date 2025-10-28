package kh.com.csx.posapi.dto.adjustment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentImportRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private String date;

    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters")
    private String referenceNo;

    @NotNull(message = "Biller ID is required")
    private Long billerId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private MultipartFile attachment;

    private String note;

    @NotNull(message = "File is required.")
    private MultipartFile file;
}
