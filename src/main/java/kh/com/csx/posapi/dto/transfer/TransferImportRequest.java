package kh.com.csx.posapi.dto.transfer;

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
public class TransferImportRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private String date;

    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters")
    private String referenceNo;

    @NotNull(message = "From biller ID is required.")
    private Long fromBiller;

    @NotNull(message = "To biller ID is required.")
    private Long toBiller;

    @NotNull(message = "From warehouse ID is required.")
    private Long fromWarehouse;

    @NotNull(message = "To warehouse ID is required.")
    private Long toWarehouse;

    private MultipartFile attachment;

    private String note;

    @NotBlank(message = "Status is required.")
    @Size(max = 50, message = "Status should not exceed {max} characters.")
    private String status;

    @NotNull(message = "File is required.")
    private MultipartFile file;
}
