package kh.com.csx.posapi.dto.product;

import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.setting.ZeroOrOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private Long productId;

    @Size(max = 255, message = "Image should not exceed {max} characters.")
    private String image;

    @NotBlank(message = "Product code is required.")
    @Size(max = 255, message = "Product code should not exceed {max} characters.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Product code can only contain letters, numbers, underscores, or hyphens.")
    private String productCode;

    @NotBlank(message = "Barcode is required.")
    @Size(max = 255, message = "Barcode should not exceed {max} characters.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Barcode can only contain letters, numbers, underscores, or hyphens.")
    private String barCode;

    @NotBlank(message = "Product name (EN) is required.")
    @Size(max = 255, message = "Product name (EN) should not exceed {max} characters.")
    private String productNameEn;

    @Size(max = 255, message = "Product name (KH) should not exceed {max} characters.")
    private String productNameKh;

    @NotNull(message = "Category ID is required.")
    private Long categoryId;

    private Long brandId;

    @NotBlank(message = "Currency is required.")
    @Size(max = 50, message = "Currency should not exceed {max} characters.")
    private String currency;

    @NotBlank(message = "Product type is required.")
    @Size(max = 255, message = "Product type should not exceed {max} characters.")
    @Pattern(regexp = Constant.ProductType.regexp, message = "Product type must match one of the predefined, " + Constant.ProductType.NOTE + ".")
    private String type;

    @ZeroOrOne(message = "Invalid status. " + Constant.ActiveStatus.NOTE)
    private Integer status;

    @ZeroOrOne(message = "Invalid tax method. " + Constant.TaxMethod.NOTE)
    private Integer taxMethod;

    @Size(max = 255, message = "Stock type should not exceed {max} characters.")
    private String stockType;

    @PositiveOrZero(message = "Alert quantity must be zero or positive")
    private Double alertQuantity;

    @PositiveOrZero(message = "Expiry alert days must be zero or positive")
    private Integer expiryAlertDays;

    private String description;
    private String productDetails;

    @Size(max = 255, message = "Attachment should not exceed {max} characters.")
    private String attachment;

    @NotEmpty(message = "Product unit is required.")
    private List<ProductUnit> productUnits;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductUnit {
        private Long productId;
        private Long unitId;
        private double cost;
        private double price;
        private int defaultUnit;
        private int defaultSale;
        private int defaultPurchase;
    }
}
