package kh.com.csx.posapi.dto.report.inventoryReport;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrandRequest extends ProductRequest {
    private String code;
    private String name;
}
