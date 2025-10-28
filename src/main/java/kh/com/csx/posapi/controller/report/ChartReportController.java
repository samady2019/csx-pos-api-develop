package kh.com.csx.posapi.controller.report;

import kh.com.csx.posapi.service.report.ChartReportService;
import kh.com.csx.posapi.dto.report.chartReport.*;
import kh.com.csx.posapi.model.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report/chart")
@RequiredArgsConstructor
public class ChartReportController {
    private final ChartReportService chartReportService;

    @GetMapping("overview")
    @PreAuthorize("""
        hasRole('ADMIN') or hasRole('OWNER') or
        hasAuthority('INVENTORY-REPORTS-PRODUCTS') or 
        hasAuthority('PURCHASE-REPORTS-PURCHASES') or hasAuthority('PURCHASE-REPORTS-PAYMENTS') or
        hasAuthority('SALE-REPORTS-POS') or hasAuthority('SALE-REPORTS-SALES') or hasAuthority('SALE-REPORTS-RETURNS') or hasAuthority('SALE-REPORTS-PAYMENTS') or
        hasAuthority('EXPENSE-REPORTS-EXPENSES') or hasAuthority('EXPENSE-REPORTS-PAYMENTS')
    """)
    public BaseResponse overview(OverviewRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(chartReportService.overview(request));
        baseResponse.setMessage("Overview report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("stock-value")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse stockValue(OverviewRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(chartReportService.stockValue(request));
        baseResponse.setMessage("Stock value report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("stock-category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse stockCategories(OverviewRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(chartReportService.stockCategories(request));
        baseResponse.setMessage("Stock categories report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("stock-warehouse")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse stockWarehouses(OverviewRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(chartReportService.stockWarehouses(request));
        baseResponse.setMessage("Stock warehouses report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("best-seller")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS') or hasAuthority('SALE-REPORTS-POS') or hasAuthority('SALE-REPORTS-SALES')")
    public BaseResponse bestSellers(OverviewRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(chartReportService.bestSellers(request));
        baseResponse.setMessage("Best sellers report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("expense-category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-REPORTS-EXPENSES')")
    public BaseResponse expenseCategories(OverviewRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(chartReportService.expenseCategories(request));
        baseResponse.setMessage("Expense categories report retrieved successfully.");
        return baseResponse;
    }
}
