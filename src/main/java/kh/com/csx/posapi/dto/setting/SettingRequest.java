package kh.com.csx.posapi.dto.setting;

import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant.ReferenceFormat;
import kh.com.csx.posapi.constant.Constant.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingRequest {
    @Size(max = 255, message = "Logo should not exceed {max} characters.")
    private String logo;

    @Size(max = 255, message = "Small logo should not exceed {max} characters.")
    private String smallLogo;

    @Size(max = 255, message = "Big logo should not exceed {max} characters.")
    private String bigLogo;

    @NotBlank(message = "Site name is required.")
    @Size(max = 255, message = "Site name not exceed {max} characters.")
    private String siteName;

    @NotBlank(message = "Date format is required.")
    @Size(max = 100, message = "Date format should not exceed {max} characters.")
    private String dateFormat;

    @NotBlank(message = "Date time format is required.")
    @Size(max = 100, message = "Date time format should not exceed {max} characters.")
    private String dateTimeFormat;

    @NotBlank(message = "Reference format is required.")
    @Size(max = 100, message = "Reference format should not exceed {max} characters.")
    @Pattern(regexp = ReferenceFormat.regexp, message = "Reference format must match one of the predefined " + ReferenceFormat.NOTE + ".")
    private String referenceFormat;

    @NotNull(message = "Reference reset is required.")
    @Min(value = 0, message = "Reference reset must be either 0 (None) or 1 (Year) or 2 (Month).")
    @Max(value = 2, message = "Reference reset must be either 0 (None) or 1 (Year) or 2 (Month).")
    private Integer referenceReset;

    @NotNull(message = "Alert day is required.")
    @PositiveOrZero(message = "Alert day be zero or positive")
    private Integer alertDay;

    @NotNull(message = "Expiry alert day is required.")
    @PositiveOrZero(message = "Expiry alert day be zero or positive")
    private Integer expiryAlertDays;

    @NotNull(message = "Product expiry is required.")
    @ZeroOrOne(message = "Product expiry must be either 0 (No) or 1 (Yes).")
    private Integer productExpiry;

    @NotNull(message = "Decimals is required.")
    @Min(value = 0, message = "Decimals must be min 0 and max 4.")
    @Max(value = 4, message = "Decimals must be min 0 and max 4.")
    private Integer decimals;

    @NotNull(message = "Quantity decimals is required.")
    @Min(value = 0, message = "Quantity decimals must be min 0 and max 4.")
    @Max(value = 4, message = "Quantity decimals must be min 0 and max 4.")
    private Integer quantityDecimals;

    @NotNull(message = "Update cost is required.")
    @ZeroOrOne(message = "Update cost must be either 0 (No) or 1 (Yes).")
    private Integer updateCost;

    @NotNull(message = "Multiple warehouse is required.")
    @ZeroOrOne(message = "Multiple warehouse must be either 0 (No) or 1 (Yes).")
    private Integer multiWarehouse;

    @NotNull(message = "Multiple biller is required.")
    @ZeroOrOne(message = "Multiple biller must be either 0 (No) or 1 (Yes).")
    private Integer multiBiller;

    @NotBlank(message = "Default currency is required.")
    @Size(max = 20, message = "Default currency should not exceed {max} characters.")
    private String defaultCurrency;

    @NotNull(message = "Default tax rate is required.")
    private Long defaultTaxRate;

    @NotNull(message = "Default tax rate declare is required.")
    @Min(value = 0, message = "Default tax rate declare must be at least 0.")
    @Max(value = 100, message = "Default tax rate declare must not exceed 100.")
    private Double defaultTaxRateDeclare;

    @NotNull(message = "Default biller is required.")
    private Long defaultBiller;

    @NotNull(message = "Default warehouse is required.")
    private Long defaultWarehouse;

    @NotNull(message = "Default supplier is required.")
    private Long defaultSupplier;

    @NotNull(message = "Default customer is required.")
    private Long defaultCustomer;

    @Size(max = 20, message = "Purchase order prefix should not exceed {max} characters.")
    private String purchaseOrderPrefix;

    @Size(max = 20, message = "Purchase prefix should not exceed {max} characters.")
    private String purchasePrefix;

    @Size(max = 20, message = "Purchase return prefix should not exceed {max} characters.")
    private String purchaseReturnPrefix;

    @Size(max = 20, message = "Sale order prefix should not exceed {max} characters.")
    private String saleOrderPrefix;

    @Size(max = 20, message = "Sale prefix should not exceed {max} characters.")
    private String salePrefix;

    @Size(max = 20, message = "Sale return prefix should not exceed {max} characters.")
    private String saleReturnPrefix;

    @Size(max = 20, message = "Pos prefix should not exceed {max} characters.")
    private String posPrefix;

    @Size(max = 20, message = "Pos prefix should not exceed {max} characters.")
    private String billPrefix;

    @Size(max = 20, message = "Transfer prefix should not exceed {max} characters.")
    private String transferPrefix;

    @Size(max = 20, message = "Adjustment prefix should not exceed {max} characters.")
    private String adjustmentPrefix;

    @Size(max = 20, message = "Stock count prefix should not exceed {max} characters.")
    private String stockCountPrefix;

    @Size(max = 20, message = "Delivery prefix should not exceed {max} characters.")
    private String deliveryPrefix;

    @Size(max = 20, message = "Expense prefix should not exceed {max} characters.")
    private String expensePrefix;

    @Size(max = 20, message = "Payment prefix should not exceed {max} characters.")
    private String paymentPrefix;

    @NotNull(message = "Accounting method is required.")
    @Min(value = 1, message = "Accounting method must be 1 (FIFO) or 2 (LIFO) or 3 (AVG).")
    @Max(value = 3, message = "Accounting method must be 1 (FIFO) or 2 (LIFO) or 3 (AVG).")
    private Integer accountingMethod;

    @NotNull(message = "Accounting is required.")
    @ZeroOrOne(message = "Accounting must be either 0 (No) or 1 (Yes).")
    private Integer accounting;

    @NotNull(message = "Overselling is required.")
    @ZeroOrOne(message = "Overselling must be either 0 (No) or 1 (Yes).")
    private Integer overselling;

    @NotNull(message = "Rows per page is required.")
    private Integer rowsPerPage;

    @NotBlank(message = "Language is required.")
    @Size(max = 20, message = "Language should not exceed {max} characters.")
    @Pattern(regexp = Language.regexp, message = "Language must match one of the predefined, " + Language.NOTE + ".")
    private String language;

    @NotBlank(message = "Time zone is required.")
    @Size(max = 100, message = "Time zone should not exceed {max} characters.")
    private String timezone;

    @NotBlank(message = "Theme is required.")
    @Size(max = 100, message = "Theme should not exceed {max} characters.")
    private String theme;

    @NotBlank(message = "Version is required.")
    @Size(max = 20, message = "Version should not exceed {max} characters.")
    private String version;

    @NotBlank(message = "Developed by is required.")
    @Size(max = 255, message = "Developed by should not exceed {max} characters.")
    private String developedBy;

    @NotBlank(message = "License name is required.")
    @Size(max = 255, message = "License name should not exceed {max} characters.")
    private String licenseName;

    @NotBlank(message = "License key by is required.")
    @Size(max = 255, message = "License key by should not exceed {max} characters.")
    private String licenseKey;
}
