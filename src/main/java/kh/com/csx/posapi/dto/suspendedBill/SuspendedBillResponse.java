package kh.com.csx.posapi.dto.suspendedBill;

import kh.com.csx.posapi.entity.SuspendedBillEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuspendedBillResponse {
    private SuspendedBillEntity suspendedBill;
}
