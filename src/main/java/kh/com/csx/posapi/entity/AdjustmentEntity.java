package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "adjustments")
public class AdjustmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long countId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime date;

    private String referenceNo;

    @Column(name = "biller_id")
    private Long billerId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    private String attachment;
    private String note;
    private Long createdBy;
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "biller_id", referencedColumnName = "id", insertable = false, updatable = false)
    private BillerEntity biller;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", referencedColumnName = "id", insertable = false, updatable = false)
    private WarehouseEntity warehouse;

    @OneToMany(mappedBy = "adjustment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<AdjustmentItemEntity> items;
}


