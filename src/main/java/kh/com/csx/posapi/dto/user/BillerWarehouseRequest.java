package kh.com.csx.posapi.dto.user;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillerWarehouseRequest {
    private Long billerId;
    private Long warehouseId;
}
