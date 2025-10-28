package kh.com.csx.posapi.dto.stockCount;

import java.time.LocalDate;

public interface StockCountItem {
    Long getProductId();
    LocalDate getExpiry();
    Double getQuantity();
}
