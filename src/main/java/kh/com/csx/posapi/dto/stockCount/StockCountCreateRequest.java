package kh.com.csx.posapi.dto.stockCount;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCountCreateRequest {
    @NotBlank(message = "Reference number is required.")
    @Size(max = 50, message = "Reference number should not exceed {max} characters.")
    private String referenceNo;

    @NotNull(message = "Biller ID is required.")
    private Long billerId;

    @NotNull(message = "Warehouse ID is required.")
    private Long warehouseId;

    @NotBlank(message = "Stock count type is required.")
    @Size(max = 50, message = "Type should not exceed {max} characters.")
    private String type;

    @Size(max = 255, message = "Brands ID should not exceed {max} characters.")
    private String brands;

    @Size(max = 255, message = "Categories ID should not exceed {max} characters.")
    private String categories;

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;

    private String note;
}
