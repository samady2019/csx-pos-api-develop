package kh.com.csx.posapi.dto.expense;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseRetrieveRequest extends FilterDTO {

    private Long id;
    private LocalDateTime date;
    private String referenceNo;
    private Long billerId;
    private Long expenseCategoryId;
    private double amount;
    private String paymentStatus;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;

}
