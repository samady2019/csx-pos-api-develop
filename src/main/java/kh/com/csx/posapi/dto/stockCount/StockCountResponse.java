package kh.com.csx.posapi.dto.stockCount;

import kh.com.csx.posapi.entity.StockCountEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCountResponse {
    private StockCountEntity stockCount;
}
