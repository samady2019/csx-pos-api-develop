package kh.com.csx.posapi.dto.product;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilterProductDTO extends FilterDTO {

    private String name;

    private String productCode;

    private Long brandId;

    private Long categoryId;

    private String status;

    private String orderByPrice = "ASC" ;

}
