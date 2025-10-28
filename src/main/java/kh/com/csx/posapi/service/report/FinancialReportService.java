package kh.com.csx.posapi.service.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.repository.report.FinancialReportRepository;
import kh.com.csx.posapi.dto.report.financialReport.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.utility.Utility;
import org.springframework.data.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialReportService {
    private final FinancialReportRepository financialReportRepository;
    private final Utility utility;

    public Page<ExpenseResponse> expenses(ExpenseRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("referenceNo, date");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.DESC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = financialReportRepository.findExpenses(request, pageable);
            return result.map(ExpenseResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<ExpenseMonthlyResponse> expensesMonthly(ExpenseMonthlyRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("code");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (request.getYear() == null || request.getYear().trim().isEmpty()) {
                request.setYear(String.valueOf(LocalDate.now().getYear()));
            }
            Page<Tuple>  result = financialReportRepository.findExpensesMonthly(request, pageable);
            List<String> months = utility.months(null);

            return result.map(item -> {
                List<Tuple> expensesMonthly = months.stream()
                    .map(m -> {
                        request.setExpenseCategoryId(((Number) item.get("id")).longValue());
                        request.setMonth(m);
                        request.setYear((String) item.get("year"));

                        return financialReportRepository.findFirstExpenseByMonth(request);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                return new ExpenseMonthlyResponse(item, expensesMonthly);
            });
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<PaymentAlertResponse> purchasePaymentAlerts(PaymentRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("referenceNo, date");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.DESC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = financialReportRepository.findPurchasePaymentAlerts(request, pageable);
            return result.map(PaymentAlertResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<PaymentResponse> payments(PaymentRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("referenceNo, date");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.DESC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            String tran = request.getTransaction();
            if (tran != null && !tran.trim().isEmpty()) {
                switch (tran.trim()) {
                    case Constant.PaymentTransactionType.PURCHASE_ORDER:
                        request.setTranPurchaseOrder(Constant.YES);
                        break;
                    case Constant.PaymentTransactionType.PURCHASE:
                        request.setTranPurchase(Constant.YES);
                        break;
                    case Constant.PaymentTransactionType.PURCHASE_RETURN:
                        request.setTranPurchaseReturn(Constant.YES);
                        break;
                    case Constant.PaymentTransactionType.SALE_ORDER:
                        request.setTranSaleOrder(Constant.YES);
                        break;
                    case Constant.PaymentTransactionType.SALE:
                        request.setTranSale(Constant.YES);
                        break;
                    case Constant.PaymentTransactionType.SALE_RETURN:
                        request.setTranSaleReturn(Constant.YES);
                        break;
                    case Constant.PaymentTransactionType.EXPENSE:
                        request.setTranExpense(Constant.YES);
                        break;
                    default:
                        break;
                }
            }
            Page<Tuple> result = financialReportRepository.findPayments(request, pageable);
            return result.map(PaymentResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<CashManagementResponse> cashManagement(PaymentRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("id");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            } else {
                request.setStartDate(LocalDate.now() + " 00:00:00");
                request.setEndDate(LocalDate.now() + " 23:59:59");
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = financialReportRepository.findCashManagement(request, pageable);
            return result.map(CashManagementResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ProfitAndOrLossResponse profitAndOrLoss(PaymentRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            } else {
                request.setStartDate(LocalDate.now() + " 00:00:00");
                request.setEndDate(LocalDate.now() + " 23:59:59");
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            Tuple profit         = financialReportRepository.findProfitAndOrLoss(request);
            Tuple sale           = financialReportRepository.findSale(request);
            Tuple purchase       = financialReportRepository.findPurchase(request);
            List<Tuple> expenses = financialReportRepository.findListExpensesGroupByCategory(request);
            List<Tuple> revenues = new ArrayList<>();
            revenues.add(sale);
            // utility.printLog(profit, revenues, purchase, expenses);

            return new ProfitAndOrLossResponse(profit, revenues, purchase, expenses);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
