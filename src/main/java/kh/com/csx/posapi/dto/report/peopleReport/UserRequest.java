package kh.com.csx.posapi.dto.report.peopleReport;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserRequest extends PeopleRequest {
    private String status;
}
