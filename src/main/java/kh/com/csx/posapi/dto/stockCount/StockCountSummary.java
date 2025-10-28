package kh.com.csx.posapi.dto.stockCount;

public interface StockCountSummary {
    Long getId();
    Integer getMissing();
    Integer getMatches();
    Integer getDifferences();
}
