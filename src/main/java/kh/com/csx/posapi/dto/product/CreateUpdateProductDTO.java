package kh.com.csx.posapi.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUpdateProductDTO {


    private String brandId;

    @NotNull(message = "categoryId is required")
    @Min(value = 0, message = "categoryId must be a number")
    private String categoryId;

    @NotNull(message = "productCode is required")
    @Min(value = 0, message = "productCode must be a number")
    private String productCode;

    @NotNull(message = "barCode is required")
    @NotBlank(message = "barCode cannot be blanked")
    private String barCode;

    @NotNull(message = "productNameEn is required")
    @NotBlank(message = "productNameEn cannot be blanked")
    private String productNameEn;

    @NotNull(message = "productNameKh is required")
    @NotBlank(message = "productNameKh cannot be blanked")
    private String productNameKh;

    @NotNull(message = "type is required")
    @NotBlank(message = "type cannot be blanked")
    private String type;

    @NotNull(message = "price is required")
    @DecimalMin(value = "0.0", message = "price must be a number")
    private String price;

    @NotNull(message = "supplyPrice is required")
    @DecimalMin(value = "0.0", message = "supplyPrice must be a number")
    private String supplyPrice;

    @NotNull(message = "quantityAvailable is required")
    @Min(value = 0, message = "quantityAvailable must be a number")
    private String quantityAvailable;

    @NotNull(message = "storeAt is required")
    @NotBlank(message = "storeAt cannot be blanked")
    private String storeAt;

    @NotNull(message = "status is required")
    @NotBlank(message = "status cannot be blanked")
    private String status;
}
