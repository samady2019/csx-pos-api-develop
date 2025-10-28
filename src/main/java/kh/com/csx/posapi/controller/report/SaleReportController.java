package kh.com.csx.posapi.controller.report;

import kh.com.csx.posapi.dto.report.saleReport.*;
import kh.com.csx.posapi.service.report.SaleReportService;
import kh.com.csx.posapi.model.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/report/sale")
@RequiredArgsConstructor
public class SaleReportController {

    private final SaleReportService saleReportService;

    @GetMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-REGISTERS')")
    public BaseResponse getRegisterReport(RegisterReportRequest request) {
        Page<RegisterReportResponse> registerReports = saleReportService.registerReport(request);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("registerReports", registerReports);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(responseData);
        baseResponse.setMessage("Register report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/registerDetail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-REGISTERS')")
    public BaseResponse getRegisterDetailReport(RegisterReportRequest request) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("registerDetailReport", saleReportService.registerDetailReport(request));
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(responseData);
        baseResponse.setMessage("Register detail report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-SALES') or hasAuthority('SALE-REPORTS-POS')")
    public BaseResponse getDailySalesReport(DailySalesReportRequest request) {
        List<DailySalesReportResponse> reportResponseList = saleReportService.dailySalesReport(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(reportResponseList);
        baseResponse.setMessage("Daily sales reports retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-SALES') or hasAuthority('SALE-REPORTS-POS')")
    public BaseResponse getMonthlySalesReport(MonthlySalesReportRequest request) {
        Integer year = request.getYear() != null ? request.getYear() : LocalDate.now().getYear();
        List<MonthlySalesReportResponse> monthlyReports = saleReportService.monthlySalesReport(year, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(monthlyReports);
        baseResponse.setMessage("Monthly sales report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-SALES') or hasAuthority('SALE-REPORTS-POS')")
    public BaseResponse getGeneralSalesReport(GeneralSalesReportRequest request) {
        Page<GeneralSalesReportResponse> reportPage = saleReportService.salesReport(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(reportPage);
        baseResponse.setMessage("General sales report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/discount")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-SALES') or hasAuthority('SALE-REPORTS-POS')")
    public BaseResponse getSalesDiscountReport(SalesDiscountReportRequest request) {
        Page<SalesDiscountReportResponse> reportPage = saleReportService.salesDiscountReport(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(reportPage);
        baseResponse.setMessage("Sales discount report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-SALES') or hasAuthority('SALE-REPORTS-POS')")
    public BaseResponse getSalesDetailReport(GeneralSalesReportRequest request) {
        Page<SalesDetailReportResponse> reportPage = saleReportService.salesDetailReport(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(reportPage);
        baseResponse.setMessage("Sales detail report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/topSelling")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS') or hasAuthority('SALE-REPORTS-SALES') or hasAuthority('SALE-REPORTS-POS')")
    public BaseResponse getTopSellingProducts( TopSellingProductReportRequest request) {
        Page<TopSellingProductReportResponse> topSellingProducts = saleReportService.getTopSellingProducts(request);
        BaseResponse response = new BaseResponse();
        response.setData(topSellingProducts);
        response.setMessage("Top selling products retrieved successfully.");
        return response;
    }
}
