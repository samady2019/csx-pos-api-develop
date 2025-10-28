package kh.com.csx.posapi.dto.sale;

import kh.com.csx.posapi.entity.SaleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleResponse {
    private SaleEntity sale;
}
