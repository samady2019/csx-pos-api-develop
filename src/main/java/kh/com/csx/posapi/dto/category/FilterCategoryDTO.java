package kh.com.csx.posapi.dto.category;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilterCategoryDTO extends FilterDTO {
    private String name;
    private String shortName;
    private Long parentId;
    private Integer status;
}
