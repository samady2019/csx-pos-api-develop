package kh.com.csx.posapi.dto.expenseCategory;

import jakarta.validation.constraints.NotNull;
import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCategoryRetrieveRequest extends FilterDTO {

    private Long id;
    private String code;
    private String name;
    private Long parentId;

}

