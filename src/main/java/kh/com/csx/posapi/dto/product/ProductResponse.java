package kh.com.csx.posapi.dto.product;

import static kh.com.csx.posapi.constant.Constant.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.dto.stockMovement.StockResponse;
import kh.com.csx.posapi.entity.UnitEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long productId;
    private String image;
    private String productCode;
    private String barCode;
    private String productNameEn;
    private String productNameKh;
    private Long categoryId;
    private Long brandId;
    private String currency;
    private String type;
    private Integer status;
    private Integer taxMethod;
    private Double taxRateDeclare;
    private String stockType;
    private Double alertQuantity;
    private Integer expiryAlertDays;
    private String description;
    private String productDetails;
    private String attachment;
    private Long createdBy;
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime updatedAt;

    private List<ProductUnit> productUnits;
    private Brand brand;
    private Category category;

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
        private UnitEntity unit;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Brand {
        private Long brandId;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Category {
        private Long categoryId;
        private String name;
        private String code;
    }

    private StockResponse stock;
}
