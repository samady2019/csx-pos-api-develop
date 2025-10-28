package kh.com.csx.posapi.dto.vendor;

import kh.com.csx.posapi.entity.VendorEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorResponse {
    private VendorEntity vendor;
}
