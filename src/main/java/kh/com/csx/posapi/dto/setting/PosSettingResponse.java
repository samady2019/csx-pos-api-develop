package kh.com.csx.posapi.dto.setting;

import kh.com.csx.posapi.entity.PosSettingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PosSettingResponse {
    private PosSettingEntity posSetting;
}
