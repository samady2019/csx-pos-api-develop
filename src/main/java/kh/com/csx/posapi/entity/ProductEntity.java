package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "product_code", nullable = false, unique = true, length = 255)
    private String productCode;

    @Column(name = "bar_code", nullable = false, unique = true, length = 255)
    private String barCode;

    @Column(name = "product_name_en", length = 255)
    private String productNameEn;

    @Column(name = "product_name_kh", length = 255)
    private String productNameKh;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "currency", nullable = false, length = 50)
    private String currency;

    @Column(name = "type", nullable = false, length = 255)
    private String type;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "tax_method", nullable = false)
    private Integer taxMethod;

    @Column(name = "tax_rate_declare")
    private Double taxRateDeclare;

    @Column(name = "stock_type", length = 255)
    private String stockType;

    @Column(name = "alert_quantity")
    private Double alertQuantity;

    @Column(name = "expiry_alert_days")
    private Integer expiryAlertDays;

    @Lob
    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "product_details")
    private String productDetails;

    @Column(name = "attachment", length = 255)
    private String attachment;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", insertable = false, updatable = false)
    private CategoryEntity category;

    @ManyToOne
    @JoinColumn(name = "brand_id", referencedColumnName = "brand_id", insertable = false, updatable = false)
    private BrandEntity brand;

    @OneToMany(mappedBy = "id.productId", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("price ASC")
    private Set<ProductUnitEntity> productUnits = new HashSet<>();
}
