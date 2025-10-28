package kh.com.csx.posapi.controller.report;

import kh.com.csx.posapi.dto.report.purchaseReport.*;
import kh.com.csx.posapi.service.report.PurchaseReportService;
import kh.com.csx.posapi.model.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/report/purchase")
@RequiredArgsConstructor
public class PurchaseReportController {

    private final PurchaseReportService purchaseReportService;

    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-PURCHASES')")
    public BaseResponse getDailyReport(DailyPurchaseReportRequest request) {
        List<DailyPurchaseReportResponse> reportResponseList = purchaseReportService.dailyPurchaseReport(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(reportResponseList);
        baseResponse.setMessage("Daily purchase reports retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-PURCHASES')")
    public BaseResponse getMonthlyReport(MonthlyPurchaseReportRequest request) {
        List<MonthlyPurchaseReportResponse> monthlyReports = purchaseReportService.monthlyPurchaseReport(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(monthlyReports);
        baseResponse.setMessage("Monthly purchase reports retrieved successfully.");
        return baseResponse;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-PURCHASES')")
    public BaseResponse getGeneralPurchaseReport(GeneralPurchaseReportRequest request) {
        Page<GeneralPurchaseReportResponse> reportList = purchaseReportService.purchaseReport(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(reportList);
        baseResponse.setMessage("General purchase report retrieved successfully.");
        return baseResponse;
    }
}
