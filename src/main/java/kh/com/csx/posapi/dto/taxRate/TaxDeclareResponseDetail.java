package kh.com.csx.posapi.dto.taxRate;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.entity.BillerEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class TaxDeclareResponseDetail {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    private Long billerId;

    private String type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    private LocalDate fromDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    private LocalDate toDate;

    private Integer totalItems;

    private Double total;

    private Double discount;

    private Double tax;

    private Double shipping;

    private Double grandTotal;

    private Long createdBy;

    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    private LocalDateTime updatedAt;

    private BillerEntity biller;

    private List<TaxDeclareTransaction> transactions;
}
