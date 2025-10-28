package kh.com.csx.posapi.dto.warehouse;

import kh.com.csx.posapi.entity.WarehouseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponse {
    private WarehouseEntity warehouse;
}
