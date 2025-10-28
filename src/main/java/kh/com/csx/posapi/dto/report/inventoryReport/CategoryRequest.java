package kh.com.csx.posapi.dto.report.inventoryReport;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class CategoryRequest extends ProductRequest {
    private String code;
    private String name;
}
