package kh.com.csx.posapi.entity;

import kh.com.csx.posapi.dto.stockMovement.StockMovement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import static kh.com.csx.posapi.constant.Constant.DateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_movement")
public class StockMovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "transaction", length = 255)
    private String transaction;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "product_id")
    private Long productId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
    @Column(name = "expiry")
    private LocalDate expiry;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "unit_quantity")
    private Double unitQuantity;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "cost")
    private Double cost;

    public StockMovementEntity(StockMovement stock) {
        this.id = stock.getId();
        this.date = stock.getDate();
        this.transaction = stock.getTransaction();
        this.transactionId = stock.getTransactionId();
        this.warehouseId = stock.getWarehouseId();
        this.productId = stock.getProductId();
        this.expiry = stock.getExpiry();
        this.unitId = stock.getUnitId();
        this.unitQuantity = stock.getUnitQuantity();
        this.quantity = stock.getQuantity();
        this.cost = stock.getCost();
    }

    public StockMovementEntity(StockMovementEntity stock) {
        this.id = stock.getId();
        this.date = stock.getDate();
        this.transaction = stock.getTransaction();
        this.transactionId = stock.getTransactionId();
        this.warehouseId = stock.getWarehouseId();
        this.productId = stock.getProductId();
        this.expiry = stock.getExpiry();
        this.unitId = stock.getUnitId();
        this.unitQuantity = stock.getUnitQuantity();
        this.quantity = stock.getQuantity();
        this.cost = stock.getCost();
    }
}
