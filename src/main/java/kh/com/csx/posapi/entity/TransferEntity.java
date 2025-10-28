package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import static kh.com.csx.posapi.constant.Constant.DateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfers")
public class TransferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime date;

    private String referenceNo;

    @Column(name = "from_biller")
    private Long fromBiller;

    @Column(name = "to_biller")
    private Long toBiller;

    @Column(name = "from_warehouse")
    private Long fromWarehouse;

    @Column(name = "to_warehouse")
    private Long toWarehouse;

    private Double total;
    private Double totalTax;
    private Double grandTotal;
    private String attachment;
    private String note;
    private String status;
    private Long createdBy;
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "from_biller", referencedColumnName = "id", insertable = false, updatable = false)
    private BillerEntity fbiller;

    @ManyToOne
    @JoinColumn(name = "to_biller", referencedColumnName = "id", insertable = false, updatable = false)
    private BillerEntity tbiller;

    @ManyToOne
    @JoinColumn(name = "from_warehouse", referencedColumnName = "id", insertable = false, updatable = false)
    private WarehouseEntity fwarehouse;

    @ManyToOne
    @JoinColumn(name = "to_warehouse", referencedColumnName = "id", insertable = false, updatable = false)
    private WarehouseEntity twarehouse;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<TransferItemEntity> items;
}
