package kh.com.csx.posapi.dto.brand;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilterBrandDTO extends FilterDTO {
    private String name;
    private String status;
}
