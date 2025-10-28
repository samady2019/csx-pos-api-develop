package kh.com.csx.posapi.dto.report.saleReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterReportResponse {

    private Long id;
    private Long userId;
    private String name;
    private String status;
    private Double cashInHand;
    private Double totalCash;
    private Integer totalCheques;
    private Integer totalCcSlips;
    private Double totalCashSubmitted;
    private Integer totalChequesSubmitted;
    private Integer totalCcSlipsSubmitted;
    private String note;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime openedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime closedAt;
}