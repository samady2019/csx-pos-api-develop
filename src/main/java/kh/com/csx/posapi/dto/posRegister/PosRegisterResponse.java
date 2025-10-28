package kh.com.csx.posapi.dto.posRegister;

import kh.com.csx.posapi.entity.PosRegisterEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosRegisterResponse {
    private PosRegisterEntity posRegister;
}
