package kh.com.csx.posapi.dto.transfer;

import kh.com.csx.posapi.entity.TransferEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private TransferEntity transfer;
}
