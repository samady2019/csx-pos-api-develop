package kh.com.csx.posapi.dto.taxRate;

import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxRateProductResponse {
    public Long productId;
    public String image;
    public String type;
    public String productCode;
    public String barCode;
    public String productNameEn;
    public String productNameKh;
    public String category;
    public String brand;
    public String currency;
    public String unitCode;
    public String unitNameEn;
    public String unitNameKh;
    private Integer taxMethod;
    private Double taxRateDeclare;
    public Double cost;
    public Double price;
    public Double taxCost;
    public Double taxPrice;

    public TaxRateProductResponse(Tuple data) {
        this.productId      = ((Number) data.get("productId")).longValue();
        this.image          = (String) data.get("image");
        this.type           = (String) data.get("type");
        this.productCode    = (String) data.get("productCode");
        this.barCode        = (String) data.get("barCode");
        this.productNameEn  = (String) data.get("productNameEn");
        this.productNameKh  = (String) data.get("productNameKh");
        this.category       = (String) data.get("category");
        this.brand          = (String) data.get("brand");
        this.currency       = (String) data.get("currency");
        this.unitCode       = (String) data.get("unitCode");
        this.unitNameEn     = (String) data.get("unitNameEn");
        this.unitNameKh     = (String) data.get("unitNameKh");
        this.taxMethod      = ((Number) data.get("taxMethod")).intValue();
        this.taxRateDeclare = data.get("taxRateDeclare") != null ? ((Number) data.get("taxRateDeclare")).doubleValue() : null;
        this.cost           = data.get("cost")     != null ? ((Number) data.get("cost")).doubleValue()     : 0.0;
        this.price          = data.get("price")    != null ? ((Number) data.get("price")).doubleValue()    : 0.0;
        this.taxCost        = data.get("taxCost")  != null ? ((Number) data.get("taxCost")).doubleValue()  : 0.0;
        this.taxPrice       = data.get("taxPrice") != null ? ((Number) data.get("taxPrice")).doubleValue() : 0.0;
    }
}
