package kh.com.csx.posapi.dto.category;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRetrieveRequest extends FilterDTO {
    private Long categoryId;
    private Long pCategoryId;
    private String code;
    private String name;
    private Integer status;
    private Long createdBy;
    private Long updatedBy;
}

