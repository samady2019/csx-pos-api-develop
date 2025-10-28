package kh.com.csx.posapi.dto.posRegister;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosRegisterRetrieveRequest {
    private Long id;
    private Long userId;
    private Long closedBy;
    private String status;
    private String startDate;
    private String endDate;
}
