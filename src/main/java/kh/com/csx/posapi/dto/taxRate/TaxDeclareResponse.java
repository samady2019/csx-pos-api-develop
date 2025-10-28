package kh.com.csx.posapi.dto.taxRate;

import kh.com.csx.posapi.entity.TaxDeclarationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareResponse {
    private TaxDeclarationEntity taxDeclaration;
}
