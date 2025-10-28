package kh.com.csx.posapi.controller.report;

import kh.com.csx.posapi.service.report.FinancialReportService;
import kh.com.csx.posapi.dto.report.financialReport.*;
import kh.com.csx.posapi.model.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report/financial")
@RequiredArgsConstructor
public class FinancialReportController {
    private final FinancialReportService financialReportService;

    @GetMapping("expense")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-REPORTS-EXPENSES')")
    public BaseResponse expenses(ExpenseRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(financialReportService.expenses(request));
        baseResponse.setMessage("Expenses report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("expense-monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-REPORTS-EXPENSES')")
    public BaseResponse expensesMonthly(ExpenseMonthlyRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(financialReportService.expensesMonthly(request));
        baseResponse.setMessage("Expenses monthly report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("payment-alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-PAYMENTS') or hasAuthority('SALE-REPORTS-PAYMENTS') or hasAuthority('EXPENSE-REPORTS-PAYMENTS')")
    public BaseResponse purchasePaymentAlerts(PaymentRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(financialReportService.purchasePaymentAlerts(request));
        baseResponse.setMessage("Payment alerts report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("payment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-PAYMENTS') or hasAuthority('SALE-REPORTS-PAYMENTS') or hasAuthority('EXPENSE-REPORTS-PAYMENTS')")
    public BaseResponse payments(PaymentRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(financialReportService.payments(request));
        baseResponse.setMessage("Payments report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("cash-management")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-PAYMENTS') or hasAuthority('SALE-REPORTS-PAYMENTS') or hasAuthority('EXPENSE-REPORTS-PAYMENTS')")
    public BaseResponse cashManagement(PaymentRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(financialReportService.cashManagement(request));
        baseResponse.setMessage("Cash management report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("profit-and-or-loss")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-PAYMENTS') or hasAuthority('SALE-REPORTS-PAYMENTS') or hasAuthority('EXPENSE-REPORTS-PAYMENTS')")
    public BaseResponse profitAndOrLoss(PaymentRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(financialReportService.profitAndOrLoss(request));
        baseResponse.setMessage("Profit and/or loss report retrieved successfully.");
        return baseResponse;
    }
}
