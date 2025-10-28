package kh.com.csx.posapi.dto.unit;

import kh.com.csx.posapi.entity.UnitEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitResponse {
    private UnitEntity unit;
}
