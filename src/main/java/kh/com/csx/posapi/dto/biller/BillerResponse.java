package kh.com.csx.posapi.dto.biller;

import kh.com.csx.posapi.entity.BillerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillerResponse {
    private BillerEntity biller;
}
