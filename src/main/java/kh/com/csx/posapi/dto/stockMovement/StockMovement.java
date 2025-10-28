package kh.com.csx.posapi.dto.stockMovement;

import kh.com.csx.posapi.entity.StockMovementEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockMovement {
    private Long id;
    private LocalDateTime date;
    private String transaction;
    private Long transactionId;
    private Long warehouseId;
    private Long productId;
    private LocalDate expiry;
    private Long unitId;
    private Double unitQuantity;
    private Double quantity;
    private Double cost;

    public StockMovement(StockMovement stock) {
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

    public StockMovement(StockMovementEntity stock) {
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
