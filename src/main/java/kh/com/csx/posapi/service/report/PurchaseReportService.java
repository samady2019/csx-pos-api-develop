package kh.com.csx.posapi.service.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.report.purchaseReport.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.repository.PaymentRepository;
import kh.com.csx.posapi.repository.report.PurchaseReportRepository;
import kh.com.csx.posapi.utility.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PurchaseReportService {
    @Autowired private PurchaseReportRepository purchaseReportRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private Utility utility;

    @Transactional
    public List<DailyPurchaseReportResponse> dailyPurchaseReport(DailyPurchaseReportRequest request) {
        LocalDate currentDate = LocalDate.now();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            request.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }
        Integer year = request.getYear() != null ? request.getYear() : currentDate.getYear();
        Integer month = request.getMonth() != null ? request.getMonth() : currentDate.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<Tuple> result = purchaseReportRepository.findDailyPurchaseReport(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), request);
        Map<LocalDate, DailyPurchaseReportResponse> reportMap = new HashMap<>();
        for (Tuple tuple : result) {
            LocalDate date = tuple.get("date", LocalDate.class);
            Double total = tuple.get("total", Double.class);
            Double discount = tuple.get("discount", Double.class);
            Double productTax = tuple.get("productTax", Double.class);
            Double orderTax = tuple.get("orderTax", Double.class);
            Double shipping = tuple.get("shipping", Double.class);
            Double grandTotal = tuple.get("grandTotal", Double.class);
            Double paid = tuple.get("paid", Double.class);
            Long totalPurchases = tuple.get("totalPurchases", Long.class);
            DailyPurchaseReportResponse response = new DailyPurchaseReportResponse(
                    date,
                    total != null ? total : 0.0,
                    discount != null ? discount : 0.0,
                    productTax != null ? productTax : 0.0,
                    orderTax != null ? orderTax : 0.0,
                    shipping != null ? shipping : 0.0,
                    grandTotal != null ? grandTotal : 0.0,
                    paid != null ? paid : 0.0,
                    totalPurchases != null ? totalPurchases.intValue() : 0
            );
            reportMap.put(date, response);
        }
        List<DailyPurchaseReportResponse> finalReport = new ArrayList<>();
        for (LocalDate currentDateIter = startDate; !currentDateIter.isAfter(endDate); currentDateIter = currentDateIter.plusDays(1)) {
            DailyPurchaseReportResponse report = reportMap.getOrDefault(currentDateIter,
                new DailyPurchaseReportResponse(
                    currentDateIter, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0
                )
            );
            finalReport.add(report);
        }
        return finalReport;
    }

    @Transactional
    public List<MonthlyPurchaseReportResponse> monthlyPurchaseReport(MonthlyPurchaseReportRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();

        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            request.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }

        Integer year = request.getYear();
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<Tuple> result = purchaseReportRepository.findMonthlyPurchaseReport(year, request);

        List<MonthlyPurchaseReportResponse> reportList = new ArrayList<>();
        Map<Integer, MonthlyPurchaseReportResponse> reportMap = new HashMap<>();

        for (Tuple tuple : result) {
            Integer month = tuple.get("month", Integer.class);
            Double total = tuple.get("total", Double.class);
            Double discount = tuple.get("discount", Double.class);
            Double productTax = tuple.get("productTax", Double.class);
            Double orderTax = tuple.get("orderTax", Double.class);
            Double shipping = tuple.get("shipping", Double.class);
            Double grandTotal = tuple.get("grandTotal", Double.class);
            Double paid = tuple.get("paid", Double.class);
            Long totalPurchases = tuple.get("totalPurchases", Long.class);

            MonthlyPurchaseReportResponse response = new MonthlyPurchaseReportResponse(
                    year, month,
                    total != null ? total : 0.0,
                    discount != null ? discount : 0.0,
                    productTax != null ? productTax : 0.0,
                    orderTax != null ? orderTax : 0.0,
                    shipping != null ? shipping : 0.0,
                    grandTotal != null ? grandTotal : 0.0,
                    paid != null ? paid : 0.0,
                    totalPurchases != null ? totalPurchases.intValue() : 0
            );

            reportMap.put(month, response);
        }

        for (int month = 1; month <= 12; month++) {
            MonthlyPurchaseReportResponse report = reportMap.getOrDefault(month,
                    new MonthlyPurchaseReportResponse(
                            year, month,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0
                    )
            );
            reportList.add(report);
        }

        return reportList;
    }

    @Transactional
    public Page<GeneralPurchaseReportResponse> purchaseReport(GeneralPurchaseReportRequest request) {

        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("referenceNo, date");
        }
        if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.DESC);
        }

        if ((request.getStartDate() != null && request.getEndDate() != null) &&
                (request.getStartDate().equals(request.getEndDate()))) {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
            }
        }

        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();

        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            request.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }

        LocalDateTime startDateTime = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null;

        return purchaseReportRepository.findGeneralPurchaseReport(
                        startDateTime, endDateTime, request, pageable)
                .map(tuple -> {
                    Long id = tuple.get("id", Long.class);
                    Long purchaseOrderId = tuple.get("purchaseOrderId", Long.class);
                    LocalDateTime date = tuple.get("date", LocalDateTime.class);
                    String referenceNo = tuple.get("referenceNo", String.class);
                    String poReferenceNo = tuple.get("poReferenceNo", String.class);
                    String biller = tuple.get("biller", String.class);
                    String warehouse = tuple.get("warehouse", String.class);
                    String supplier = tuple.get("supplier", String.class);
                    Double total = tuple.get("total", Double.class);
                    Double shipping = tuple.get("shipping", Double.class);
                    Double orderDiscount = tuple.get("orderDiscount", Double.class);
                    String orderDiscountId = tuple.get("orderDiscountId", String.class);
                    Double orderTax = tuple.get("orderTax", Double.class);
                    String orderTaxId = tuple.get("orderTaxId", String.class);
                    Double grandTotal = tuple.get("grandTotal", Double.class);
                    Double paid = tuple.get("paid", Double.class);
                    Double balance = tuple.get("balance", Double.class);
                    String status = tuple.get("status", String.class);
                    String paymentStatus = tuple.get("paymentStatus", String.class);
                    String note = tuple.get("note", String.class);
                    String attachment = tuple.get("attachment", String.class);
                    String createdBy = tuple.get("createdBy", String.class);

                    return GeneralPurchaseReportResponse.builder()
                            .id(id)
                            .purchaseOrderId(purchaseOrderId)
                            .date(date)
                            .referenceNo(referenceNo)
                            .poReferenceNo(poReferenceNo)
                            .biller(biller)
                            .warehouse(warehouse)
                            .supplier(supplier)
                            .total(total)
                            .shipping(shipping)
                            .orderDiscount(orderDiscount)
                            .orderDiscountId(orderDiscountId)
                            .orderTax(orderTax)
                            .orderTaxId(orderTaxId)
                            .grandTotal(grandTotal)
                            .paid(paid)
                            .balance(balance)
                            .status(status)
                            .paymentStatus(paymentStatus)
                            .note(note)
                            .attachment(attachment)
                            .createdBy(createdBy)
                            .build();
                });
    }

}
