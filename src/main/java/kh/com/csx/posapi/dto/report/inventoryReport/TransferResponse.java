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
public class TransferResponse {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    private String referenceNo;
    private String fromBiller;
    private String toBiller;
    private String fromWarehouse;
    private String toWarehouse;
    private String attachment;
    private String note;
    private String status;
    private String createdBy;

    public TransferResponse(Tuple data) {
        this.id            = ((Number) data.get("id")).longValue();
        this.date          = (LocalDateTime) data.get("date");
        this.referenceNo   = (String) data.get("referenceNo");
        this.fromBiller    = (String) data.get("fromBiller");
        this.toBiller      = (String) data.get("toBiller");
        this.fromWarehouse = (String) data.get("fromWarehouse");
        this.toWarehouse   = (String) data.get("toWarehouse");
        this.attachment    = (String) data.get("attachment");
        this.note          = (String) data.get("note");
        this.status        = (String) data.get("status");
        this.createdBy     = (String) data.get("createdBy");
    }
}
