package kh.com.csx.posapi.entity;

import kh.com.csx.posapi.constant.Constant.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "settings")
public class SettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "logo", length = 255)
    private String logo;

    @Column(name = "small_logo", length = 255)
    private String smallLogo;

    @Column(name = "big_logo", length = 255)
    private String bigLogo;

    @Column(name = "site_name", length = 255)
    private String siteName;

    @Column(name = "date_format", length = 100)
    private String dateFormat;

    @Column(name = "datetime_format", length = 100)
    private String dateTimeFormat;

    @Column(name = "reference_format", length = 100)
    private String referenceFormat;

    @Column(name = "reference_reset")
    private Integer referenceReset;

    @Column(name = "alert_day")
    private Integer alertDay;

    @Column(name = "expiry_alert_days")
    private Integer expiryAlertDays;

    @Column(name = "product_expiry")
    private Integer productExpiry;

    @Column(name = "decimals")
    private Integer decimals;

    @Column(name = "quantity_decimals")
    private Integer quantityDecimals;

    @Column(name = "update_cost")
    private Integer updateCost;

    @Column(name = "multi_warehouse")
    private Integer multiWarehouse;

    @Column(name = "multi_biller")
    private Integer multiBiller;

    @Column(name = "default_currency", length = 20)
    private String defaultCurrency;

    @Column(name = "default_tax_rate")
    private Long defaultTaxRate;

    @Column(name = "default_tax_rate_declare")
    private Double defaultTaxRateDeclare;

    @Column(name = "default_biller")
    private Long defaultBiller;

    @Column(name = "default_warehouse")
    private Long defaultWarehouse;

    @Column(name = "default_supplier")
    private Long defaultSupplier;

    @Column(name = "default_customer")
    private Long defaultCustomer;

    @Column(name = "purchase_order_prefix", length = 20)
    private String purchaseOrderPrefix;

    @Column(name = "purchase_prefix", length = 20)
    private String purchasePrefix;

    @Column(name = "purchase_return_prefix", length = 20)
    private String purchaseReturnPrefix;

    @Column(name = "sale_order_prefix", length = 20)
    private String saleOrderPrefix;

    @Column(name = "sale_prefix", length = 20)
    private String salePrefix;

    @Column(name = "sale_return_prefix", length = 20)
    private String saleReturnPrefix;

    @Column(name = "pos_prefix", length = 20)
    private String posPrefix;

    @Column(name = "bill_prefix", length = 20)
    private String billPrefix;

    @Column(name = "transfer_prefix", length = 20)
    private String transferPrefix;

    @Column(name = "adjustment_prefix", length = 20)
    private String adjustmentPrefix;

    @Column(name = "stock_count_prefix", length = 20)
    private String stockCountPrefix;

    @Column(name = "delivery_prefix", length = 20)
    private String deliveryPrefix;

    @Column(name = "expense_prefix", length = 20)
    private String expensePrefix;

    @Column(name = "payment_prefix", length = 20)
    private String paymentPrefix;

    @Column(name = "accounting_method")
    private Integer accountingMethod;

    @Column(name = "accounting")
    private Integer accounting;

    @Column(name = "overselling")
    private Integer overselling;

    @Column(name = "module_tax_declare")
    private Integer moduleTaxDeclare;

    @Column(name = "rows_per_page")
    private Integer rowsPerPage;

    @Column(name = "language", length = 20)
    private String language;

    @Column(name = "timezone", length = 100)
    private String timezone;

    @Column(name = "theme", length = 100)
    private String theme;

    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "developed_by", length = 255)
    private String developedBy;

    @Column(name = "license_name", length = 255)
    private String licenseName;

    @Column(name = "license_key", length = 255)
    private String licenseKey;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "last_modified")
    private LocalDateTime lastModified;
}
