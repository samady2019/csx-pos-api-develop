package kh.com.csx.posapi.dto.report.inventoryReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductExpiryAlertResponse extends ProductResponse {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    private LocalDate expiry;

    private Double quantity;

    public ProductExpiryAlertResponse(Tuple data) {
        this.productId     = ((Number) data.get("productId")).longValue();
        this.productCode   = (String) data.get("productCode");
        this.barCode       = (String) data.get("barCode");
        this.productNameEn = (String) data.get("productNameEn");
        this.productNameKh = (String) data.get("productNameKh");
        this.category      = (String) data.get("category");
        this.brand         = (String) data.get("brand");
        this.unitCode      = (String) data.get("unitCode");
        this.unitNameEn    = (String) data.get("unitNameEn");
        this.unitNameKh    = (String) data.get("unitNameKh");
        this.expiry        = (LocalDate) data.get("expiry");
        this.quantity      = data.get("quantity") != null ? ((Number) data.get("quantity")).doubleValue() : 0.0;
    }
}
