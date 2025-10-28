package kh.com.csx.posapi.dto.taxRate;

import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareInvoiceRequest {
    @NotBlank(message = "Type is required.")
    @Pattern(regexp = Constant.TaxDeclarationType.REGEXP, message = "Type must match one of the predefined, " + Constant.TaxDeclarationType.NOTE + ".")
    private String type;

    @NotEmpty(message = "Transaction IDs is required.")
    List<Long> ids;
}
