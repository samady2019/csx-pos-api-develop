package kh.com.csx.posapi.dto.report.peopleReport;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class PeopleRequest extends FilterDTO {
    private Long billerId;
    private Long warehouseId;
    private Long id;
    private String name;
    private String gender;
    private String contactPerson;
    private String phone;
    private String email;
    private Long createdBy;
    private Long updatedBy;
    private String startDate;
    private String endDate;
}
