package kh.com.csx.posapi.dto.setting;

import kh.com.csx.posapi.entity.SettingEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingResponse {
    private SettingEntity setting;
}
