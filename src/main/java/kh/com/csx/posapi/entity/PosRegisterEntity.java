package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import static kh.com.csx.posapi.constant.Constant.DateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pos_register")
public class PosRegisterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "cash_in_hand")
    private Double cashInHand;

    @Column(name = "total_cash")
    private Double totalCash;

    @Column(name = "total_cheques")
    private Integer totalCheques;

    @Column(name = "total_cc_slips")
    private Integer totalCcSlips;

    @Column(name = "total_cash_submitted")
    private Double totalCashSubmitted;

    @Column(name = "total_cheques_submitted")
    private Integer totalChequesSubmitted;

    @Column(name = "total_cc_slips_submitted")
    private Integer totalCcSlipsSubmitted;

     @Column(name = "transfer_opened_bills")
     private String transferOpenedBills;

    @Column(name = "note")
    private String note;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "closed_by")
    private Long closedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
