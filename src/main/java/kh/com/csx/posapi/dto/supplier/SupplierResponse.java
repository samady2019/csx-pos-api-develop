package kh.com.csx.posapi.dto.supplier;

import kh.com.csx.posapi.entity.SupplierEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierResponse {
    private SupplierEntity supplier;
}
