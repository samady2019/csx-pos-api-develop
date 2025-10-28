package kh.com.csx.posapi.dto.setting;

import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant.PosType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosSettingRequest {
    @NotNull(message = "Category limit is required.")
    @PositiveOrZero(message = "Category limit be zero or positive")
    private Integer categoryLimit;

    @NotNull(message = "Brand limit is required.")
    @PositiveOrZero(message = "Brand limit be zero or positive")
    private Integer brandLimit;

    @NotNull(message = "Product limit is required.")
    @PositiveOrZero(message = "Product limit be zero or positive")
    private Integer productLimit;

    @NotNull(message = "Default category is required.")
    private Long defaultCategory;

    @NotNull(message = "Default brand is required.")
    private Long defaultBrand;

    @NotNull(message = "Show category is required.")
    @ZeroOrOne(message = "Show category must be either 0 (No) or 1 (Yes).")
    private Integer showCategory;

    @NotNull(message = "Show quantity is required.")
    @ZeroOrOne(message = "Show quantity must be either 0 (No) or 1 (Yes).")
    private Integer showQuantity;

    @NotNull(message = "Display time is required.")
    @ZeroOrOne(message = "Display time must be either 0 (No) or 1 (Yes).")
    private Integer displayTime;

    @NotNull(message = "Coupon card is required.")
    @ZeroOrOne(message = "Coupon card must be either 0 (No) or 1 (Yes).")
    private Integer couponCard;

    @NotNull(message = "Sale due is required.")
    @ZeroOrOne(message = "Sale due must be either 0 (No) or 1 (Yes).")
    private Integer saleDue;

    @NotBlank(message = "Pin code is required.")
    @Size(max = 20, message = "Pin code should not exceed {max} characters.")
    private String pinCode;

    @NotBlank(message = "Pos type is required.")
    @Size(max = 20, message = "Pos type should not exceed {max} characters.")
    @Pattern(regexp = PosType.regexp, message = "Pos type must be either " + PosType.NOTE + ".")
    private String posType;
}
