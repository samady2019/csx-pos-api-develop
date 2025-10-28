package kh.com.csx.posapi.dto.purchase;

import kh.com.csx.posapi.entity.PurchaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponse {
     private PurchaseEntity purchase;
}
