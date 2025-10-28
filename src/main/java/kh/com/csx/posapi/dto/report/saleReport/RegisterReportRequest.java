package kh.com.csx.posapi.dto.report.saleReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterReportRequest extends FilterDTO {
    private Long id;
    private Long userId;
    private String name;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
