package kh.com.csx.posapi.dto.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UnitConversionResponse {
    private UnitInfo unit;
    private UnitInfo baseUnit;
    private double value;
    private double converted;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UnitInfo {
        private Long unitId;
        private String unitCode;
        private String unitNameEn;
        private String unitNameKh;
        private double value;
        private String description;
    }
}
