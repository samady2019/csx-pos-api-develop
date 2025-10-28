package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import static kh.com.csx.posapi.constant.Constant.DateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_counts")
public class StockCountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "biller_id")
    private Long billerId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "initial_file", length = 255)
    private String initialFile;

    @Column(name = "final_file", length = 255)
    private String finalFile;

    @Column(name = "brands", length = 255)
    private String brands;

    @Column(name = "categories", length = 255)
    private String categories;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "differences")
    private Integer differences;

    @Column(name = "matches")
    private Integer matches;

    @Column(name = "missing")
    private Integer missing;

    @Column(name = "attachment", length = 255)
    private String attachment;

    @Lob
    @Column(name = "note")
    private String note;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "biller_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BillerEntity biller;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id", insertable = false, updatable = false)
    private WarehouseEntity warehouse;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_count_id", referencedColumnName = "id")
    private List<StockCountItemEntity> items;
}
