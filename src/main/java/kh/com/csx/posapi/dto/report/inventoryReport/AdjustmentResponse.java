package kh.com.csx.posapi.dto.report.inventoryReport;

import kh.com.csx.posapi.constant.Constant.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentResponse {
    private Long id;
    private Long countId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    private String referenceNo;
    private String biller;
    private String warehouse;
    private String attachment;
    private String note;
    private String createdBy;

    public AdjustmentResponse(Tuple data) {
        this.id          = ((Number) data.get("id")).longValue();
        this.countId     = data.get("countId") != null ? ((Number) data.get("countId")).longValue() : null;
        this.date        = (LocalDateTime) data.get("date");
        this.referenceNo = (String) data.get("referenceNo");
        this.biller      = (String) data.get("biller");
        this.warehouse   = (String) data.get("warehouse");
        this.attachment  = (String) data.get("attachment");
        this.note        = (String) data.get("note");
        this.createdBy   = (String) data.get("createdBy");
    }
}
