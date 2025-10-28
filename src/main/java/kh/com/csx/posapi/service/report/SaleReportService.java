package kh.com.csx.posapi.service.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.report.saleReport.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.repository.PosRegisterRepository;
import kh.com.csx.posapi.repository.report.SaleReportRepository;
import kh.com.csx.posapi.utility.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SaleReportService {
    @Autowired private SaleReportRepository saleReportRepository;
    @Autowired private Utility utility;
    @Autowired private PosRegisterRepository posRegisterRepository;

    public Page<RegisterReportResponse> registerReport(RegisterReportRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("openedAt");
        }
        if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.DESC);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if ((request.getStartDate() != null && request.getEndDate() != null) && (request.getStartDate().equals(request.getEndDate()))) {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
            }
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
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        LocalDateTime startDateTime = request.getStartDate() != null ? request.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = request.getEndDate() != null ? request.getEndDate().atTime(23, 59, 59) : null;
        return saleReportRepository.findRegisterReports(request.getUser(), request.getId(), request.getUserId(), request.getStatus(), request.getName(), startDateTime, endDateTime, request, pageable)
                .map(tuple -> {
                    Long id = tuple.get("id", Long.class);
                    Long userId = tuple.get("userId", Long.class);
                    String name = tuple.get("name", String.class);
                    String status = tuple.get("status", String.class);
                    Double cashInHand = tuple.get("cashInHand", Double.class);
                    Double totalCash = tuple.get("totalCash", Double.class);
                    Integer totalCheques = tuple.get("totalCheques", Integer.class);
                    Integer totalCcSlips = tuple.get("totalCcSlips", Integer.class);
                    Double totalCashSubmitted = tuple.get("totalCashSubmitted", Double.class);
                    Integer totalChequesSubmitted = tuple.get("totalChequesSubmitted", Integer.class);
                    Integer totalCcSlipsSubmitted = tuple.get("totalCcSlipsSubmitted", Integer.class);
                    String note = tuple.get("note", String.class);
                    LocalDateTime openedAt = tuple.get("openedAt", LocalDateTime.class);
                    LocalDateTime closedAt = tuple.get("closedAt", LocalDateTime.class);
                    return RegisterReportResponse.builder()
                            .id(id)
                            .userId(userId)
                            .name(name)
                            .status(status)
                            .cashInHand(cashInHand)
                            .totalCash(totalCash)
                            .totalCheques(totalCheques)
                            .totalCcSlips(totalCcSlips)
                            .totalCashSubmitted(totalCashSubmitted)
                            .totalChequesSubmitted(totalChequesSubmitted)
                            .totalCcSlipsSubmitted(totalCcSlipsSubmitted)
                            .note(note)
                            .openedAt(openedAt)
                            .closedAt(closedAt)
                            .build();
                });
    }

    public RegisterDetailReportResponse registerDetailReport(RegisterReportRequest request) {
        if (request.getId() == null && request.getUserId() == null) {
            throw new ApiException("Pos register ID or User ID is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            Tuple register = saleReportRepository.findRegisterDetails(request);
            if (register == null) {
                throw new ApiException("POS register not found.", HttpStatus.BAD_REQUEST);
            }
            Long posRegisterId     = ((Number) register.get("id")).longValue();
            Long userId            = ((Number) register.get("userId")).longValue();
            String status          = (String) register.get("status");
            List<Tuple> payments   = saleReportRepository.findRegisterPayments(posRegisterId);
            Tuple sale             = saleReportRepository.findRegisterSaleDetails(posRegisterId);
            List<Tuple> categories = saleReportRepository.findRegisterSaleCategories(posRegisterId);
            List<RegisterDetailReportResponse.Sale.SaleItem> saleItems = categories.stream().map(category -> {
                Long categoryId = ((Number) category.get("categoryId")).longValue();
                List<Tuple> items = saleReportRepository.findRegisterSaleItems(posRegisterId, categoryId);
                return new RegisterDetailReportResponse.Sale.SaleItem(category, items);
            }).toList();
            List<Tuple> customers = saleReportRepository.findRegisterCustomers(posRegisterId);

            RegisterDetailReportResponse.PosRegister posRegister = new RegisterDetailReportResponse.PosRegister(register);
            if (status.equals(Constant.PosRegister.OPEN)) {
                List<Map<String, Object>> paymentsTrans = posRegisterRepository.getPosPaymentsByUserBill(userId);
                List<Map<String, Object>> expensesTrans = posRegisterRepository.getExpensePaymentsByUserBill(userId);
                List<Map<String, Object>> trans         = posRegisterRepository.getPosTranPaymentsByUserBill(userId);
                Double total_changes                    = posRegisterRepository.getPosPaymentChangesByUserBill(userId);
                double cashInHand = register.get("cashInHand") != null ? ((Number) register.get("cashInHand")).doubleValue() : 0.0;
                double total_cash = cashInHand - (total_changes != null ? total_changes : 0);
                int total_cheques = 0;
                int total_CcSlips = 0;
                if (!paymentsTrans.isEmpty()) {
                    for (Map<String, Object> payment : paymentsTrans) {
                        if (payment.get("type").equals(Constant.PaymentMethodType.CASH)) {
                            total_cash += (payment.get("total_amount") != null ? ((Number) payment.get("total_amount")).doubleValue() : 0);
                        }
                    }
                }
                if (!expensesTrans.isEmpty()) {
                    for (Map<String, Object> expense : expensesTrans) {
                        if (expense.get("type").equals(Constant.PaymentMethodType.CASH)) {
                            total_cash -= (expense.get("total_amount") != null ? ((Number) expense.get("total_amount")).doubleValue() : 0);
                        }
                    }
                }
                if (!trans.isEmpty()) {
                    for (Map<String, Object> tran : trans) {
                        if (tran.get("type").equals(Constant.PaymentMethodType.CHEQUE)) {
                            total_cheques += (tran.get("total") != null ? ((Number) tran.get("total")).intValue() : 0);
                        } else if (tran.get("type").equals(Constant.PaymentMethodType.CARD)) {
                            total_CcSlips += (tran.get("total") != null ? ((Number) tran.get("total")).intValue() : 0);
                        }
                    }
                }
                posRegister.setTotalCash(total_cash > 0 ? total_cash : 0);
                posRegister.setTotalCheques(total_cheques);
                posRegister.setTotalCcSlips(total_CcSlips);
            }
            List<RegisterDetailReportResponse.Payment>  paymentRegister  = payments.stream().map(RegisterDetailReportResponse.Payment::new).toList();
            RegisterDetailReportResponse.Sale           saleRegister     = new RegisterDetailReportResponse.Sale(sale, saleItems);
            List<RegisterDetailReportResponse.Customer> customerRegister = customers.stream().map(RegisterDetailReportResponse.Customer::new).toList();

            return new RegisterDetailReportResponse(posRegister, paymentRegister, saleRegister, customerRegister);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public List<DailySalesReportResponse> dailySalesReport(DailySalesReportRequest request) {

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

        LocalDate currentDate = LocalDate.now();
        Integer year = request.getYear() != null ? request.getYear() : currentDate.getYear();
        Integer month = request.getMonth() != null ? request.getMonth() : currentDate.getMonthValue();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<Tuple> result = saleReportRepository.findDailySalesReport(
                startDate.atStartOfDay(), endDate.atTime(23, 59, 59), request
        );
        List<DailySalesReportResponse> reportList = new ArrayList<>();
        for (Tuple tuple : result) {
            LocalDate date = tuple.get("date", LocalDate.class);
            Double total = tuple.get("total", Double.class);
            Double discount = tuple.get("discount", Double.class);
            Double productTax = tuple.get("productTax", Double.class);
            Double orderTax = tuple.get("orderTax", Double.class);
            Double shipping = tuple.get("shipping", Double.class);
            Double grandTotal = tuple.get("grandTotal", Double.class);
            Double paid = tuple.get("paid", Double.class);
            Long totalSales = tuple.get("totalSales", Long.class);
            DailySalesReportResponse response = new DailySalesReportResponse(
                    date,
                    total != null ? total : 0.0,
                    discount != null ? discount : 0.0,
                    productTax != null ? productTax : 0.0,
                    orderTax != null ? orderTax : 0.0,
                    shipping != null ? shipping : 0.0,
                    grandTotal != null ? grandTotal : 0.0,
                    paid != null ? paid : 0.0,
                    totalSales != null ? totalSales.intValue() : 0
            );
            reportList.add(response);
        }
        Map<LocalDate, DailySalesReportResponse> reportMap = new HashMap<>();
        for (DailySalesReportResponse response : reportList) {
            reportMap.put(response.getDate(), response);
        }
        List<DailySalesReportResponse> finalReport = new ArrayList<>();
        for (LocalDate currentDateIter = startDate; !currentDateIter.isAfter(endDate); currentDateIter = currentDateIter.plusDays(1)) {
            DailySalesReportResponse report = reportMap.getOrDefault(currentDateIter,
                    new DailySalesReportResponse(
                            currentDateIter, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0
                    )
            );
            finalReport.add(report);
        }
        return finalReport;
    }

    public List<MonthlySalesReportResponse> monthlySalesReport(Integer year, MonthlySalesReportRequest filter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();

        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            filter.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            filter.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            filter.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }

        if (year == null) {
            year = LocalDate.now().getYear();
        }
        List<Tuple> result = saleReportRepository.findMonthlySalesReport(year, filter);
        Map<Integer, MonthlySalesReportResponse> reportMap = new HashMap<>();

        for (Tuple tuple : result) {
            Integer reportYear = tuple.get("year", Integer.class);
            Integer month = tuple.get("month", Integer.class);
            Double total = tuple.get("total", Double.class);
            Double discount = tuple.get("discount", Double.class);
            Double productTax = tuple.get("productTax", Double.class);
            Double orderTax = tuple.get("orderTax", Double.class);
            Double shipping = tuple.get("shipping", Double.class);
            Double grandTotal = tuple.get("grandTotal", Double.class);
            Double paid = tuple.get("paid", Double.class);
            Long totalSales = tuple.get("totalSales", Long.class);

            MonthlySalesReportResponse response = new MonthlySalesReportResponse(
                    year, month,
                    total != null ? total : 0.0,
                    discount != null ? discount : 0.0,
                    productTax != null ? productTax : 0.0,
                    orderTax != null ? orderTax : 0.0,
                    shipping != null ? shipping : 0.0,
                    grandTotal != null ? grandTotal : 0.0,
                    paid != null ? paid : 0.0,
                    totalSales != null ? totalSales.intValue() : 0
            );

            reportMap.put(month, response);
        }

        List<MonthlySalesReportResponse> finalReport = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            MonthlySalesReportResponse report = reportMap.getOrDefault(month,
                    new MonthlySalesReportResponse(
                            year, month,
                            0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0
                    )
            );
            finalReport.add(report);
        }
        return finalReport;
    }

    public Page<GeneralSalesReportResponse> salesReport(GeneralSalesReportRequest filter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (filter.getSortBy() == null || filter.getSortBy().trim().isEmpty()) {
            filter.setSortBy("referenceNo, date");
        }
        if (filter.getOrderBy() == null || filter.getOrderBy().trim().isEmpty()) {
            filter.setOrderBy(Constant.OrderBy.DESC);
        }
        if ((filter.getStartDate() != null && filter.getEndDate() != null) && (filter.getStartDate().equals(filter.getEndDate()))) {
            if (filter.getStartDate() == null || filter.getEndDate() == null) {
                throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
            }
        }
        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            filter.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            filter.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            filter.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }
        Pageable pageable = utility.initPagination(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getOrderBy());
        LocalDateTime startDateTime = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : null;
        LocalDateTime endDateTime = filter.getEndDate() != null ? filter.getEndDate().atTime(23, 59, 59) : null;
        Page<Tuple> resultPage = saleReportRepository.findGeneralSalesReport(startDateTime, endDateTime, filter, pageable);
        return resultPage.map(tuple -> {
            Long id = tuple.get("id", Long.class);
            Long saleOrderId = tuple.get("saleOrderId", Long.class);
            LocalDateTime date = tuple.get("date", LocalDateTime.class);
            String referenceNo = tuple.get("referenceNo", String.class);
            String soReferenceNo = tuple.get("soReferenceNo", String.class);
            String biller = tuple.get("biller", String.class);
            String warehouse = tuple.get("warehouse", String.class);
            String customer = tuple.get("customer", String.class);
            Double total = tuple.get("total", Double.class);
            Double shipping = tuple.get("shipping", Double.class);
            Double orderDiscount = tuple.get("orderDiscount", Double.class);
            String orderDiscountId = tuple.get("orderDiscountId", String.class);
            Double orderTax = tuple.get("orderTax", Double.class);
            String orderTaxId = tuple.get("orderTaxId", String.class);
            Double grandTotal = tuple.get("grandTotal", Double.class);
            Double paid = tuple.get("paid", Double.class);
            Double balance = tuple.get("balance", Double.class);
            Double changes = tuple.get("changes", Double.class);
            Double cost = tuple.get("cost", Double.class);
            String status = tuple.get("status", String.class);
            String paymentStatus = tuple.get("paymentStatus", String.class);
            String deliveryStatus = tuple.get("deliveryStatus", String.class);
            String note = tuple.get("note", String.class);
            String attachment = tuple.get("attachment", String.class);
            String createdBy = tuple.get("createdBy", String.class);
            return GeneralSalesReportResponse.builder()
                    .id(id)
                    .saleOrderId(saleOrderId)
                    .date(date)
                    .referenceNo(referenceNo)
                    .soReferenceNo(soReferenceNo)
                    .biller(biller)
                    .warehouse(warehouse)
                    .customer(customer)
                    .total(total)
                    .shipping(shipping)
                    .orderDiscount(orderDiscount)
                    .orderDiscountId(orderDiscountId)
                    .orderTax(orderTax)
                    .orderTaxId(orderTaxId)
                    .grandTotal(grandTotal)
                    .paid(paid)
                    .balance(balance)
                    .changes(changes)
                    .cost(cost)
                    .status(status)
                    .paymentStatus(paymentStatus)
                    .deliveryStatus(deliveryStatus)
                    .note(note)
                    .attachment(attachment)
                    .createdBy(createdBy)
                    .build();
        });
    }

    public Page<SalesDiscountReportResponse> salesDiscountReport(SalesDiscountReportRequest filter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            filter.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            filter.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            filter.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }
        if (filter.getSortBy() == null || filter.getSortBy().trim().isEmpty()) {
            filter.setSortBy("referenceNo, date");
        }
        if (filter.getOrderBy() == null || filter.getOrderBy().trim().isEmpty()) {
            filter.setOrderBy(Constant.OrderBy.DESC);
        }
        LocalDateTime startDateTime = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime endDateTime = filter.getEndDate() != null ? filter.getEndDate().atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);
        Pageable pageable = utility.initPagination(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getOrderBy());
        Page<Tuple> resultPage = saleReportRepository.findSalesDiscountReport(startDateTime, endDateTime, filter, pageable);
        return resultPage.map(tuple -> {
            Long productId = tuple.get("productId", Long.class);
            String referenceNo = tuple.get("referenceNo", String.class);
            LocalDateTime date = tuple.get("date", LocalDateTime.class);
            String customerName = tuple.get("customerName", String.class);
            String productCode = tuple.get("productCode", String.class);
            String productNameEn = tuple.get("productNameEn", String.class);
            String productNameKh = tuple.get("productNameKh", String.class);
            Double unitPrice = tuple.get("unitPrice", Double.class);
            Double unitQuantity = tuple.get("unitQuantity", Double.class);
            String discount = String.valueOf(tuple.get("discount"));

            return new SalesDiscountReportResponse(
                    date != null ? date : LocalDateTime.now(),
                    referenceNo != null ? referenceNo : "",
                    customerName != null ? customerName : "",
                    productId != null ? productId : 0,
                    productCode != null ? productCode : "",
                    productNameEn != null ? productNameEn : "",
                    productNameKh != null ? productNameKh : "",
                    unitPrice != null ? unitPrice : 0.0,
                    unitQuantity != null ? unitQuantity : 0.0,
                    discount != null ? discount : ""
            );
        });
    }

    public Page<SalesDetailReportResponse> salesDetailReport(GeneralSalesReportRequest filter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            filter.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            filter.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            filter.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }
        if (filter.getSortBy() == null || filter.getSortBy().trim().isEmpty()) {
            filter.setSortBy("nameEn");
        }
        if (filter.getOrderBy() == null || filter.getOrderBy().trim().isEmpty()) {
            filter.setOrderBy(Constant.OrderBy.ASC);
        }
        LocalDateTime startDateTime = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime endDateTime = filter.getEndDate() != null ? filter.getEndDate().atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);
        Pageable pageable = utility.initPagination(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getOrderBy());

        Page<Tuple> result = saleReportRepository.findCustomersSalesReport(startDateTime, endDateTime, filter, pageable);
        return result.map(customer -> {
            Long customerId = ((Number) customer.get("customerId")).longValue();
            filter.setCustomerId(customerId);
            List<Tuple> salesTuples = saleReportRepository.findSalesDetailReport(startDateTime, endDateTime, filter);
            List<SalesDetailReportResponse.Sale> sales = salesTuples.stream().map(sale -> {
                Long saleId = ((Number) sale.get("id")).longValue();
                List<Tuple> items = saleReportRepository.findSaleItemsDetailReport(saleId);
                return new SalesDetailReportResponse.Sale(sale, items);
            }).toList();
            return new SalesDetailReportResponse(customer, sales);
        });
    }

    public Page<TopSellingProductReportResponse> getTopSellingProducts(TopSellingProductReportRequest filter) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
            filter.setUser(user.getUserId());
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            filter.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            filter.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }
        if (filter.getSortBy() == null || filter.getSortBy().trim().isEmpty()) {
            filter.setSortBy("quantitySold");
        }
        if (filter.getOrderBy() == null || filter.getOrderBy().trim().isEmpty()) {
            filter.setOrderBy(Constant.OrderBy.DESC);
        }
        LocalDateTime startDateTime = filter.getStartDate() != null ? filter.getStartDate().atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime endDateTime = filter.getEndDate() != null ? filter.getEndDate().atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);
        Pageable pageable = utility.initPagination(filter.getPage(), filter.getSize(), filter.getSortBy(), filter.getOrderBy());
        Page<Tuple> resultPage = saleReportRepository.findTopSellingProducts(startDateTime, endDateTime, filter, pageable);
        return resultPage.map(tuple -> {
            Long productId = tuple.get("productId", Long.class);
            String productCode = tuple.get("productCode", String.class);
            String productName = tuple.get("productName", String.class);
            String category = tuple.get("category", String.class);
            String brand = tuple.get("brand", String.class);
            String unitCode = tuple.get("unitCode", String.class);
            String unitNameEn = tuple.get("unitNameEn", String.class);
            String unitNameKh = tuple.get("unitNameKh", String.class);
            Double quantitySold = tuple.get("quantitySold", Double.class);
            Double totalPriceAmount = tuple.get("totalPriceAmount", Double.class);
            Double totalCostAmount = tuple.get("totalCostAmount", Double.class);
            Double totalProfit = tuple.get("totalProfit", Double.class);

            return TopSellingProductReportResponse.builder()
                    .productId(productId)
                    .productCode(productCode)
                    .productName(productName)
                    .category(category)
                    .brand(brand)
                    .unitCode(unitCode)
                    .unitNameEn(unitNameEn)
                    .unitNameKh(unitNameKh)
                    .quantitySold(quantitySold)
                    .totalPriceAmount(totalPriceAmount)
                    .totalCostAmount(totalCostAmount)
                    .totalProfit(totalProfit)
                    .build();
        });
    }
}
