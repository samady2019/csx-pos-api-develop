package kh.com.csx.posapi.dto.taxRate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxDeclareCreateRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @NotNull(message = "Date is required.")
    private LocalDateTime date;

    @NotNull(message = "Biller ID is required.")
    private Long billerId;

    @NotBlank(message = "Type is required.")
    @Size(max = 50, message = "Type should not exceed {max} characters.")
    @Pattern(regexp = Constant.TaxDeclarationType.REGEXP, message = "Tax declaration type must match one of the predefined, " + Constant.TaxDeclarationType.NOTE + ".")
    private String type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    @NotNull(message = "From date is required.")
    private LocalDate fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    @NotNull(message = "To date is required.")
    private LocalDate toDate;

    @Valid
    @NotEmpty(message = "Transaction must contain at least one item.")
    List<TaxDeclareItemRequest> transactions;
}
