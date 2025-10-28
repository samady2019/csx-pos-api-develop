package kh.com.csx.posapi.service;

import jakarta.persistence.EntityManager;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.ReferenceKey;
import kh.com.csx.posapi.constant.Constant.PosRegister;
import kh.com.csx.posapi.constant.Constant.SuspendStatus;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.taxRate.*;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.dto.payment.PaymentCreateRequest;
import kh.com.csx.posapi.dto.posRegister.*;
import kh.com.csx.posapi.dto.suspendedBill.*;
import kh.com.csx.posapi.dto.sale.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {
    @Autowired
    private EntityManager entityManager;

    private final BillerRepository billerRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final TaxRateRepository taxRateRepository;
    // private final SaleOrderRepository saleOrderRepository;
    // private final QuoteRepository quoteRepository;
    private final SaleRepository saleRepository;
    private final PosRegisterRepository posRegisterRepository;
    private final SuspendedBillRepository suspendedBillRepository;
    private final SuspendedItemRepository suspendedItemRepository;
    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final SaleItemRepository saleItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final TaxRateService taxRateService;

    private final ProductService productService;
    private final Utility utility;

    public SaleResponse getSaleById(Long id) {
        SaleEntity data = saleRepository.findById(id).orElseThrow(() -> new ApiException("Sale not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);
        data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
        return SaleResponse.builder().sale(data).build();
    }

    public Page<SaleResponse> getAllSales(SaleRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("referenceNo, date");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
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
        Page<SaleEntity> saleEntities = saleRepository.findAllByFilter(request, pageable);
        return saleEntities.map(
            data -> {
                data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
                return SaleResponse.builder().sale(data).build();
            }
        );
    }

    public PosRegisterStatusResponse checkUserRegister(Long userId) {
        if (userId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            userId = user.getUserId();
        }
        String result = posRegisterRepository.checkUserRegister(userId);
        return PosRegisterStatusResponse.builder().status(result).build();
    }

    public PosRegisterResponse getRegisterById(Long id) {
        PosRegisterEntity data = posRegisterRepository.findById(id).orElseThrow(() -> new ApiException("POS register not found.", HttpStatus.BAD_REQUEST));
        return PosRegisterResponse.builder().posRegister(data).build();
    }

    public SuspendedBillResponse getSuspendedById(Long id) {
        SuspendedBillEntity data = suspendedBillRepository.findById(id).orElseThrow(() -> new ApiException("Suspended bill not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);
        data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
        return SuspendedBillResponse.builder().suspendedBill(data).build();
    }

    public Page<SuspendedBillResponse> getAllSuspended(SuspendedBillRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("referenceNo, date");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (request.getStatus() == null) {
            request.setStatus(SuspendStatus.SUSPENDED);
        }
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
        Page<SuspendedBillEntity> suspendedBillEntities = suspendedBillRepository.findAllByFilter(request, pageable);
        return suspendedBillEntities.map(
            data -> {
                data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
                return SuspendedBillResponse.builder().suspendedBill(data).build();
            }
        );
    }

    @Transactional
    public PosRegisterResponse openRegister(PosRegisterOpenRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (posRegisterRepository.existRegister(user.getUserId())) {
            throw new ApiException("User: '" + user.getUsername() + "' already opened register.", HttpStatus.BAD_REQUEST);
        }
        PosRegisterEntity posRegister = new PosRegisterEntity();
        posRegister.setDate(LocalDateTime.now());
        posRegister.setUserId(user.getUserId());
        posRegister.setCashInHand(utility.formatDecimal(request.getCashInHand()));
        posRegister.setTotalCash(0.0);
        posRegister.setTotalCheques(0);
        posRegister.setTotalCcSlips(0);
        posRegister.setTotalCashSubmitted(0.0);
        posRegister.setTotalChequesSubmitted(0);
        posRegister.setTotalCcSlipsSubmitted(0);
        posRegister.setStatus(PosRegister.OPEN);
        try {
            PosRegisterEntity data = posRegisterRepository.save(posRegister);
            return getRegisterById(data.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PosRegisterResponse closeRegister(PosRegisterCloseRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        PosRegisterEntity posRegister = posRegisterRepository.findById(request.getId()).orElseThrow(() -> new ApiException("POS register not found.", HttpStatus.BAD_REQUEST));
        if (posRegister.getStatus().equals(PosRegister.CLOSE)) {
            throw new ApiException("This POS register has been closed.", HttpStatus.BAD_REQUEST);
        }
        List<Map<String, Object>> payments = posRegisterRepository.getPosPaymentsByUserBill(user.getUserId());
        List<Map<String, Object>> expenses = posRegisterRepository.getExpensePaymentsByUserBill(user.getUserId());
        List<Map<String, Object>> trans    = posRegisterRepository.getPosTranPaymentsByUserBill(user.getUserId());
        Double total_changes               = posRegisterRepository.getPosPaymentChangesByUserBill(user.getUserId());
        double total_cash = posRegister.getCashInHand() - (total_changes != null ? total_changes : 0);
        int total_cheques = 0;
        int total_CcSlips = 0;
        if (!payments.isEmpty()) {
            for (Map<String, Object> payment : payments) {
                if (payment.get("type").equals(Constant.PaymentMethodType.CASH)) {
                    total_cash += (payment.get("total_amount") != null ? ((Number) payment.get("total_amount")).doubleValue() : 0);
                }
            }
        }
        if (!expenses.isEmpty()) {
            for (Map<String, Object> expense : expenses) {
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
        posRegister.setTotalCash(utility.formatDecimal(total_cash > 0 ? total_cash : 0));
        posRegister.setTotalCheques(total_cheques);
        posRegister.setTotalCcSlips(total_CcSlips);
        posRegister.setTotalCashSubmitted(utility.formatDecimal(request.getTotalCashSubmitted()));
        posRegister.setTotalChequesSubmitted(request.getTotalChequesSubmitted());
        posRegister.setTotalCcSlipsSubmitted(request.getTotalCcSlipsSubmitted());
        posRegister.setNote(request.getNote());
        posRegister.setStatus(PosRegister.CLOSE);
        posRegister.setClosedBy(user.getUserId());
        posRegister.setClosedAt(LocalDateTime.now());
        try {
            posRegisterRepository.save(posRegister);
            return getRegisterById(posRegister.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public SaleResponse createPOS(SaleCreateRequest request) {
        SettingEntity settings = utility.getSettings();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (!posRegisterRepository.existRegister(user.getUserId())) {
            throw new ApiException("User '" + user.getUsername() + "' must open a register with cash in hand.", HttpStatus.BAD_REQUEST);
        }
        SuspendedBillEntity suspended = null;
        if (request.getSuspendId() != null) {
            suspended = suspendedBillRepository.findById(request.getSuspendId()).orElseThrow(() -> new ApiException("Suspended bill not found.", HttpStatus.BAD_REQUEST));
            if (suspended.getStatus().equals(SuspendStatus.COMPLETED)) {
                throw new ApiException("Unable to create sale: The suspend bill '" + suspended.getReferenceNo() + "' has already been processed.", HttpStatus.BAD_REQUEST);
            }
        }
        if (request.getDeliveryStatus() == null || request.getDeliveryStatus().trim().isEmpty()) {
            request.setDeliveryStatus(Constant.DeliveryStatus.DEFAULT);
        } else if (!Constant.DeliveryStatus.VALID.contains(request.getDeliveryStatus().trim())) {
            throw new ApiException("Invalid delivery status. " + Constant.DeliveryStatus.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (request.getPayments() == null || request.getPayments().isEmpty()) {
            throw new ApiException("Payment is required.", HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        if (request.getSalesmanBy() != null) {
            UserEntity salesman = userRepository.findBySalesmanId(request.getSalesmanBy()).orElseThrow(() -> new ApiException("Salesman not found.", HttpStatus.BAD_REQUEST));
            if (salesman.getStatus().equals(Constant.User.Status.INACTIVE)) {
                throw new ApiException("Salesman is inactive.", HttpStatus.BAD_REQUEST);
            }
        }
        utility.verifyAccess(request);

        LocalDateTime date       = request.getDate();
        LocalDateTime order_date = request.getOrderDate();
        Long   biller_id         = request.getBillerId();
        Long   warehouse_id      = request.getWarehouseId();
        Long   customer_id       = request.getCustomerId();
        Long   user_id           = user.getUserId();
        Long   salesman_id       = request.getSalesmanBy();
        String reference_no      = utility.getReferenceNo(biller_id, ReferenceKey.POS);
        double product_discount  = 0;
        double product_tax       = 0;
        double total             = 0;
        String discount_id       = request.getOrderDiscountId();
        double r_discount        = 0;
        double f_discount        = 0;
        double discount          = 0;
        Long   tax_id            = request.getOrderTaxId();
        double tax               = 0;
        double shipping          = utility.formatDecimal(request.getShipping());
        double grand_total       = 0;
        double paid              = 0;
        double changes           = 0;
        int    total_items       = 0;
        String status            = Constant.SaleStatus.COMPLETED;
        String payment_status    = Constant.PaymentStatus.DEFAULT;
        String delivery_status   = request.getDeliveryStatus().trim();
        SaleEntity sale          = new SaleEntity();
        sale.setDate(date);
        sale.setOrderDate(order_date);
        sale.setReferenceNo(reference_no);
        sale.setWaitNumber(saleRepository.getWaitNumber(biller_id));
        sale.setBillerId(biller_id);
        sale.setWarehouseId(warehouse_id);
        sale.setCustomerId(customer_id);
        sale.setCurrencies(request.getCurrencies());
        sale.setAttachment(request.getAttachment());
        sale.setSuspendId(suspended != null ? suspended.getId() : null);
        sale.setSuspendNote(suspended != null ? suspended.getSuspendNote() : null);
        sale.setStaffNote(request.getStaffNote());
        sale.setNote(request.getNote());
        sale.setPos(Constant.YES);
        sale.setSalesmanBy(salesman_id);
        sale.setCreatedBy(user_id);
        sale.setCreatedAt(LocalDateTime.now());
        List<StockMovementEntity> stocks   = new ArrayList<>();
        List<SaleItemEntity>      products = new ArrayList<>();
        for (SaleItemRequest item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitId() == null) {
                throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitPrice() == null || item.getUnitPrice() < 0) {
                throw new ApiException("Unit price is required and must be a positive value.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitQuantity() == null || item.getUnitQuantity() <= 0) {
                throw new ApiException("Unit quantity is required and must be greater than zero.", HttpStatus.BAD_REQUEST);
            }
            if (item.getTaxRateId() == null) {
                throw new ApiException("Product Tax ID is required.", HttpStatus.BAD_REQUEST);
            }
            ProductEntity product_details = productRepository.findByProductId(item.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
            if (product_details.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(item.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(item.getProductId(), item.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            taxRateRepository.findById(item.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
            Long      item_product_id         = item.getProductId();
            Long      item_unit_id            = item.getUnitId();
            double    item_quantity           = utility.formatQuantity(item.getUnitQuantity());
            double    item_price              = utility.formatDecimal(item.getUnitPrice());
            String    item_discount_id        = item.getDiscount();
            Long      item_tax_id             = item.getTaxRateId();

            double    item_base_unit_cost     = utility.formatDecimal(utility.getAvgCost(item_product_id));
            double    item_r_discount         = utility.calculateDiscount(item_price, item_discount_id);
            double    item_f_discount         = utility.formatDecimal(item_price - item_r_discount);
            double    item_discount           = utility.formatDecimal(item_price - item_f_discount);
            double    item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product_details.getTaxMethod()));
            double    item_net_unit_price     = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product_details.getTaxMethod()));
            double    item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_price, 0, item_tax, 0));
            double    item_subtotal           = utility.formatDecimal(item_total * item_quantity);

            double    item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
            double    item_base_unit_price    = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));

            SaleItemEntity product = new SaleItemEntity();
            product.setWarehouseId(warehouse_id);
            product.setProductId(item_product_id);
            product.setUnitId(item_unit_id);
            product.setUnitQuantity(item_quantity);
            product.setQuantity(item_base_unit_quantity);
            product.setUnitPrice(item_price);
            product.setBaseUnitPrice(item_base_unit_price);
            product.setNetUnitPrice(item_net_unit_price);
            product.setBaseUnitCost(item_base_unit_cost);
            product.setDiscount(item_discount_id);
            product.setItemDiscount(item_discount);
            product.setTaxRateId(item_tax_id);
            product.setItemTax(item_tax);
            product.setSubtotal(item_subtotal);
            product.setDescription(item.getDescription());
            products.add(product);
            if (!product_details.getType().equals(Constant.ProductType.SERVICE)) {
                StockMovementEntity stock = new StockMovementEntity();
                stock.setDate(date);
                stock.setTransaction(Constant.StockTransactionType.SALE);
                stock.setWarehouseId(warehouse_id);
                stock.setProductId(item_product_id);
                stock.setUnitId(item_unit_id);
                stock.setUnitQuantity(-1 * item_quantity);
                stock.setQuantity(-1 * item_base_unit_quantity);
                stock.setCost(item_base_unit_cost);
                stocks.add(stock);
            }
            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
            total_items      += 1;
        }
        if (settings.getProductExpiry().equals(Constant.YES) && !stocks.isEmpty()) {
            stocks = utility.checkPOSExpiry(stocks).stream().map(StockMovementEntity::new).toList();
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping));
        sale.setTotal(total);
        sale.setShipping(shipping);
        sale.setProductDiscount(product_discount);
        sale.setOrderDiscountId(discount_id);
        sale.setOrderDiscount(discount);
        sale.setTotalDiscount(product_discount + discount);
        sale.setProductTax(product_tax);
        sale.setOrderTaxId(tax_id);
        sale.setOrderTax(tax);
        sale.setTotalTax(product_tax + tax);
        sale.setGrandTotal(grand_total);
        sale.setPaid(paid);
        sale.setChanges(changes);
        sale.setTotalItems(total_items);
        sale.setStatus(status);
        sale.setPaymentStatus(payment_status);
        sale.setDeliveryStatus(delivery_status);
        try {
            SaleEntity data = saleRepository.save(sale);
            Long sale_id    = data.getId();
            products.forEach(product -> product.setSaleId(sale_id));
            stocks.forEach(stock -> stock.setTransactionId(sale_id));
            saleItemRepository.saveAll(products);
            stockMovementRepository.saveAll(stocks);
            utility.checkOverstock(warehouse_id, stocks);
            if (suspended != null) {
                suspendedBillRepository.updateStatus(suspended.getId());
            }
            utility.updateReferenceNo(biller_id, ReferenceKey.POS, reference_no);
            entityManager.flush();
            entityManager.clear();
            double total_paid = 0;
            List<PaymentEntity> payments = new ArrayList<>();
            String pay_reference_no = utility.checkReferenceNo(biller_id, ReferenceKey.PAY, request.getPayments().get(0).getReferenceNo().trim());
            for (PaymentCreateRequest req_payment : request.getPayments()) {
                PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(req_payment.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
                String currenciesJson = req_payment.getCurrencies();
                double paid_amount    = utility.formatDecimal(utility.calculateTotalBaseCurrency(currenciesJson));
                if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
                    throw new ApiException("Payment method type '" + Constant.PaymentMethodType.DEPOSIT + "' is not allowed for this transaction.", HttpStatus.BAD_REQUEST);
                }
                if (paid_amount <= 0) {
                    throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
                }
                PaymentEntity payment = new PaymentEntity();
                payment.setBillerId(biller_id);
                payment.setSaleId(sale_id);
                payment.setDate(date);
                payment.setReferenceNo(pay_reference_no);
                payment.setPaymentMethodId(req_payment.getPaymentMethodId());
                payment.setAccountNumber(req_payment.getAccountNumber());
                payment.setAccountName(req_payment.getAccountName());
                payment.setBankName(req_payment.getBankName());
                payment.setChequeNo(req_payment.getChequeNo());
                payment.setChequeDate(req_payment.getChequeDate());
                payment.setChequeNumber(req_payment.getChequeNumber());
                payment.setCcNo(req_payment.getCcNo());
                payment.setCcCvv2(req_payment.getCcCvv2());
                payment.setCcHolder(req_payment.getCcHolder());
                payment.setCcMonth(req_payment.getCcMonth());
                payment.setCcYear(req_payment.getCcYear());
                payment.setCcType(req_payment.getCcType());
                payment.setCurrencies(currenciesJson);
                payment.setAmount(paid_amount);
                payment.setNote(req_payment.getNote());
                payment.setAttachment(req_payment.getAttachment());
                payment.setType(Constant.PaymentTransactionStatus.RECEIVED);
                payment.setCreatedBy(user_id);
                payment.setCreatedAt(LocalDateTime.now());
                payments.add(payment);
                total_paid += paid_amount;
            }
            if (total_paid < grand_total) {
                throw new ApiException("Payment is less than the grand total due. Please complete the full payment.", HttpStatus.BAD_REQUEST);
            }
            paymentRepository.saveAll(payments);
            paymentRepository.updatePaymentStatus(null, null, null, sale_id, null);
            utility.updateReferenceNo(biller_id, ReferenceKey.PAY, pay_reference_no);
            entityManager.flush();
            entityManager.clear();

            return getSaleById(sale_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public SuspendedBillResponse createSuspend(SuspendedBillCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (!posRegisterRepository.existRegister(user.getUserId())) {
            throw new ApiException("User '" + user.getUsername() + "' must open a register with cash in hand.", HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        if (request.getSalesmanBy() != null) {
            UserEntity salesman = userRepository.findBySalesmanId(request.getSalesmanBy()).orElseThrow(() -> new ApiException("Salesman not found.", HttpStatus.BAD_REQUEST));
            if (salesman.getStatus().equals(Constant.User.Status.INACTIVE)) {
                throw new ApiException("Salesman is inactive.", HttpStatus.BAD_REQUEST);
            }
        }
        utility.verifyAccess(request);

        LocalDateTime date          = LocalDateTime.now();
        Long   biller_id            = request.getBillerId();
        Long   warehouse_id         = request.getWarehouseId();
        Long   customer_id          = request.getCustomerId();
        Long   user_id              = user.getUserId();
        Long   salesman_id          = request.getSalesmanBy();
        String reference_no         = utility.getReferenceNo(biller_id, ReferenceKey.BILL);
        double product_discount     = 0;
        double product_tax          = 0;
        double total                = 0;
        String discount_id          = request.getOrderDiscountId();
        double r_discount           = 0;
        double f_discount           = 0;
        double discount             = 0;
        Long   tax_id               = request.getOrderTaxId();
        double tax                  = 0;
        double shipping             = utility.formatDecimal(request.getShipping());
        double grand_total          = 0;
        int    total_items          = 0;
        Integer status              = SuspendStatus.SUSPENDED;
        SuspendedBillEntity suspend = new SuspendedBillEntity();
        suspend.setDate(date);
        suspend.setReferenceNo(reference_no);
        suspend.setBillerId(biller_id);
        suspend.setWarehouseId(warehouse_id);
        suspend.setCustomerId(customer_id);
        suspend.setAttachment(request.getAttachment());
        suspend.setSuspendNote(request.getSuspendNote());
        suspend.setStaffNote(request.getStaffNote());
        suspend.setNote(request.getNote());
        suspend.setSalesmanBy(salesman_id);
        suspend.setCreatedBy(user_id);
        suspend.setCreatedAt(LocalDateTime.now());
        List<SuspendedItemEntity> products = new ArrayList<>();
        for (SuspendedItemRequest item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitId() == null) {
                throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitPrice() == null || item.getUnitPrice() < 0) {
                throw new ApiException("Unit price is required and must be a positive value.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitQuantity() == null || item.getUnitQuantity() <= 0) {
                throw new ApiException("Unit quantity is required and must be greater than zero.", HttpStatus.BAD_REQUEST);
            }
            if (item.getTaxRateId() == null) {
                throw new ApiException("Product Tax ID is required.", HttpStatus.BAD_REQUEST);
            }
            ProductEntity product_details = productRepository.findByProductId(item.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
            if (product_details.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(item.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(item.getProductId(), item.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            taxRateRepository.findById(item.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
            Long      item_product_id         = item.getProductId();
            Long      item_unit_id            = item.getUnitId();
            double    item_quantity           = utility.formatQuantity(item.getUnitQuantity());
            double    item_price              = utility.formatDecimal(item.getUnitPrice());
            String    item_discount_id        = item.getDiscount();
            Long      item_tax_id             = item.getTaxRateId();

            double    item_r_discount         = utility.calculateDiscount(item_price, item_discount_id);
            double    item_f_discount         = utility.formatDecimal(item_price - item_r_discount);
            double    item_discount           = utility.formatDecimal(item_price - item_f_discount);
            double    item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product_details.getTaxMethod()));
            double    item_net_unit_price     = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product_details.getTaxMethod()));
            double    item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_price, 0, item_tax, 0));
            double    item_subtotal           = utility.formatDecimal(item_total * item_quantity);

            double    item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
            double    item_base_unit_price    = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));

            SuspendedItemEntity product = new SuspendedItemEntity();
            product.setWarehouseId(warehouse_id);
            product.setProductId(item_product_id);
            product.setUnitId(item_unit_id);
            product.setUnitQuantity(item_quantity);
            product.setQuantity(item_base_unit_quantity);
            product.setUnitPrice(item_price);
            product.setBaseUnitPrice(item_base_unit_price);
            product.setNetUnitPrice(item_net_unit_price);
            product.setDiscount(item_discount_id);
            product.setItemDiscount(item_discount);
            product.setTaxRateId(item_tax_id);
            product.setItemTax(item_tax);
            product.setSubtotal(item_subtotal);
            product.setDescription(item.getDescription());
            products.add(product);

            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
            total_items      += 1;
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping));
        suspend.setTotal(total);
        suspend.setShipping(shipping);
        suspend.setProductDiscount(product_discount);
        suspend.setOrderDiscountId(discount_id);
        suspend.setOrderDiscount(discount);
        suspend.setTotalDiscount(product_discount + discount);
        suspend.setProductTax(product_tax);
        suspend.setOrderTaxId(tax_id);
        suspend.setOrderTax(tax);
        suspend.setTotalTax(product_tax + tax);
        suspend.setGrandTotal(grand_total);
        suspend.setTotalItems(total_items);
        suspend.setStatus(status);
        try {
            SuspendedBillEntity data = suspendedBillRepository.save(suspend);
            Long suspend_id = data.getId();
            products.forEach(product -> product.setSuspendId(suspend_id));
            suspendedItemRepository.saveAll(products);
            utility.updateReferenceNo(biller_id, ReferenceKey.BILL, reference_no);
            entityManager.flush();
            entityManager.clear();

            return getSuspendedById(suspend_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public SuspendedBillResponse updateSuspend(SuspendedBillUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (!posRegisterRepository.existRegister(user.getUserId())) {
            throw new ApiException("User '" + user.getUsername() + "' must open a register with cash in hand.", HttpStatus.BAD_REQUEST);
        }
        SuspendedBillEntity suspend = suspendedBillRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Suspend bill not found.", HttpStatus.BAD_REQUEST));
        if (suspend.getStatus().equals(SuspendStatus.COMPLETED)) {
            throw new ApiException("Unable to update suspend: The suspend bill '" + suspend.getReferenceNo() + "' has already been processed.", HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        if (request.getSalesmanBy() != null) {
            UserEntity salesman = userRepository.findBySalesmanId(request.getSalesmanBy()).orElseThrow(() -> new ApiException("Salesman not found.", HttpStatus.BAD_REQUEST));
            if (salesman.getStatus().equals(Constant.User.Status.INACTIVE)) {
                throw new ApiException("Salesman is inactive.", HttpStatus.BAD_REQUEST);
            }
        }
        utility.verifyAccess(request);

        Long   suspend_id           = suspend.getId();
        Long   biller_id            = suspend.getBillerId();
        Long   warehouse_id         = request.getWarehouseId();
        Long   customer_id          = request.getCustomerId();
        Long   user_id              = user.getUserId();
        Long   salesman_id          = request.getSalesmanBy();
        double product_discount     = 0;
        double product_tax          = 0;
        double total                = 0;
        String discount_id          = request.getOrderDiscountId();
        double r_discount           = 0;
        double f_discount           = 0;
        double discount             = 0;
        Long   tax_id               = request.getOrderTaxId();
        double tax                  = 0;
        double shipping             = utility.formatDecimal(request.getShipping());
        double grand_total          = 0;
        int    total_items          = 0;

        suspend.setBillerId(biller_id);
        suspend.setWarehouseId(warehouse_id);
        suspend.setCustomerId(customer_id);
        suspend.setAttachment(request.getAttachment());
        suspend.setSuspendNote(request.getSuspendNote());
        suspend.setStaffNote(request.getStaffNote());
        suspend.setNote(request.getNote());
        suspend.setSalesmanBy(salesman_id);
        suspend.setUpdatedBy(user_id);
        suspend.setUpdatedAt(LocalDateTime.now());
        List<SuspendedItemEntity> products = new ArrayList<>();
        for (SuspendedItemRequest item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitId() == null) {
                throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitPrice() == null || item.getUnitPrice() < 0) {
                throw new ApiException("Unit price is required and must be a positive value.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitQuantity() == null || item.getUnitQuantity() <= 0) {
                throw new ApiException("Unit quantity is required and must be greater than zero.", HttpStatus.BAD_REQUEST);
            }
            if (item.getTaxRateId() == null) {
                throw new ApiException("Product Tax ID is required.", HttpStatus.BAD_REQUEST);
            }
            ProductEntity product_details = productRepository.findByProductId(item.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
            if (product_details.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(item.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(item.getProductId(), item.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            taxRateRepository.findById(item.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
            Long      item_product_id         = item.getProductId();
            Long      item_unit_id            = item.getUnitId();
            double    item_quantity           = utility.formatQuantity(item.getUnitQuantity());
            double    item_price              = utility.formatDecimal(item.getUnitPrice());
            String    item_discount_id        = item.getDiscount();
            Long      item_tax_id             = item.getTaxRateId();

            double    item_r_discount         = utility.calculateDiscount(item_price, item_discount_id);
            double    item_f_discount         = utility.formatDecimal(item_price - item_r_discount);
            double    item_discount           = utility.formatDecimal(item_price - item_f_discount);
            double    item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product_details.getTaxMethod()));
            double    item_net_unit_price     = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product_details.getTaxMethod()));
            double    item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_price, 0, item_tax, 0));
            double    item_subtotal           = utility.formatDecimal(item_total * item_quantity);

            double    item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
            double    item_base_unit_price    = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));

            SuspendedItemEntity product = new SuspendedItemEntity();
            product.setSuspendId(suspend_id);
            product.setWarehouseId(warehouse_id);
            product.setProductId(item_product_id);
            product.setUnitId(item_unit_id);
            product.setUnitQuantity(item_quantity);
            product.setQuantity(item_base_unit_quantity);
            product.setUnitPrice(item_price);
            product.setBaseUnitPrice(item_base_unit_price);
            product.setNetUnitPrice(item_net_unit_price);
            product.setDiscount(item_discount_id);
            product.setItemDiscount(item_discount);
            product.setTaxRateId(item_tax_id);
            product.setItemTax(item_tax);
            product.setSubtotal(item_subtotal);
            product.setDescription(item.getDescription());
            products.add(product);

            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
            total_items      += 1;
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping));
        suspend.setTotal(total);
        suspend.setShipping(shipping);
        suspend.setProductDiscount(product_discount);
        suspend.setOrderDiscountId(discount_id);
        suspend.setOrderDiscount(discount);
        suspend.setTotalDiscount(product_discount + discount);
        suspend.setProductTax(product_tax);
        suspend.setOrderTaxId(tax_id);
        suspend.setOrderTax(tax);
        suspend.setTotalTax(product_tax + tax);
        suspend.setGrandTotal(grand_total);
        suspend.setTotalItems(total_items);
        try {
            suspendedBillRepository.save(suspend);
            suspendedItemRepository.deleteBySuspendId(suspend_id);
            suspendedItemRepository.saveAll(products);
            entityManager.flush();
            entityManager.clear();

            return getSuspendedById(suspend_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteSuspend(SuspendedBillDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteSuspend(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteSuspend(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Suspended bill. deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteSuspend(Long id) {
        SuspendedBillEntity suspend = suspendedBillRepository.findById(id).orElseThrow(() -> new ApiException("Suspend bill not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(suspend);
        if (suspend.getStatus().equals(SuspendStatus.COMPLETED)) {
            throw new ApiException("Unable to delete suspend bill. '" + suspend.getReferenceNo() + "'. The suspend has already been processed.", HttpStatus.BAD_REQUEST);
        }
        try {
            suspendedItemRepository.deleteBySuspendId(id);
            suspendedBillRepository.delete(suspend);
            return new BaseResponse("Suspended bill. '" + suspend.getReferenceNo() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public byte[] taxInvoice(SaleRetrieveRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty()) {
            throw new ApiException("Transaction IDs is required.", HttpStatus.BAD_REQUEST);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Path path = Paths.get("data", "templates", "fop.xconf").toAbsolutePath();
            File file = path.toFile();
            FopFactory fopFactory  = FopFactory.newInstance(file);
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            StringBuilder allPageSequences = new StringBuilder();
            for (Long id : request.getIds()) {
                SettingEntity setting      = utility.getSettings();
                SaleEntity    sale         = saleRepository.findById(id).orElseThrow(() -> new ApiException("Transaction not found.", HttpStatus.BAD_REQUEST));
                TaxRateEntity orderTax     = taxRateRepository.findById(sale.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
                UserEntity    createdBy    = sale.getCreatedBy() != null ? userRepository.findByUserId(sale.getCreatedBy()).orElse(null) : null;
                UserEntity    updatedBy    = sale.getUpdatedBy() != null ? userRepository.findByUserId(sale.getUpdatedBy()).orElse(null) : null;
                Double        exchangeRate = null;
                if (sale.getCurrencies() != null && !sale.getCurrencies().trim().isEmpty()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<Map<String, Map<String, Object>>> currencies = objectMapper.readValue(sale.getCurrencies(), new TypeReference<List<Map<String, Map<String, Object>>>>() {});
                    for (Map<String, Map<String, Object>> item : currencies) {
                        Map<String, Object> currency = item.get("currencyCalender");
                        if ("KHR".equals(currency.get("code"))) {
                            exchangeRate = ((Number) currency.get("rate")).doubleValue();
                            if (setting.getDefaultCurrency().equals("KHR")) {
                                exchangeRate = (exchangeRate * 4000);
                            }
                        }
                    }
                }
                TaxDeclareTransaction tx = TaxDeclareTransaction.builder()
                        .id(null)
                        .taxDeclarationId(null)
                        .transactionId(sale.getId())
                        .transaction(Constant.TaxDeclarationType.SALE)
                        .date(sale.getDate())
                        .dueDate(sale.getDueDate())
                        .referenceNo(sale.getReferenceNo())
                        .taxReferenceNo(sale.getReferenceNo())
                        .billerId(sale.getBillerId())
                        .warehouseId(sale.getWarehouseId())
                        .companyId(sale.getCustomer().getId())
                        .companyEn(sale.getCustomer().getCompanyEn())
                        .companyKh(sale.getCustomer().getCompanyKh())
                        .nameEn(sale.getCustomer().getNameEn())
                        .nameKh(sale.getCustomer().getNameKh())
                        .phone(sale.getCustomer().getPhone())
                        .email(sale.getCustomer().getEmail())
                        .vatNo(sale.getCustomer().getVatNo())
                        .quantity(utility.formatQuantity(saleRepository.getSaleTotalQuantity(sale.getId())))
                        .total(sale.getTotal())
                        .shipping(sale.getShipping())
                        .productDiscount(sale.getProductDiscount())
                        .orderDiscountId(sale.getOrderDiscountId())
                        .orderDiscount(sale.getOrderDiscount())
                        .totalDiscount(sale.getTotalDiscount())
                        .productTax(sale.getProductTax())
                        .orderTaxId(orderTax.getId())
                        .orderTaxName(orderTax.getName())
                        .orderTaxValue(orderTax.getRate())
                        .orderTax(sale.getOrderTax())
                        .totalTax(sale.getTotalTax())
                        .grandTotal(sale.getGrandTotal())
                        .totalItems(sale.getTotalItems())
                        .exchangeRate(exchangeRate)
                        .note(sale.getNote())
                        .createdBy(sale.getCreatedBy())
                        .updatedBy(sale.getUpdatedBy())
                        .createdAt(sale.getCreatedAt())
                        .updatedAt(sale.getUpdatedAt())
                        .createdByName(createdBy != null ? (createdBy.getEmployee() != null ? (createdBy.getEmployee().getFirstName() + " " + createdBy.getEmployee().getLastName()) : createdBy.getUsername()) : "")
                        .updatedByName(updatedBy != null ? (updatedBy.getEmployee() != null ? (updatedBy.getEmployee().getFirstName() + " " + updatedBy.getEmployee().getLastName()) : updatedBy.getUsername()) : "")
                        .biller(sale.getBiller())
                        .warehouse(sale.getWarehouse())
                        .company(sale.getCustomer())
                        .transactionItems(
                            sale.getItems().stream().map((item) -> {
                                ProductEntity product = productRepository.findByProductId(item.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
                                UnitEntity    unit    = unitRepository.findByUnitId(item.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
                                TaxRateEntity itemTax = taxRateRepository.findById(item.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
                                return TaxDeclareTransactionItem.builder()
                                        .id(null)
                                        .transactionId(sale.getId())
                                        .transaction(Constant.TaxDeclarationType.SALE)
                                        .itemId(item.getId())
                                        .taxRateDeclare(null)
                                        .warehouseId(item.getWarehouseId())
                                        .productId(product.getProductId())
                                        .productCode(product.getProductCode())
                                        .productBarCode(product.getBarCode())
                                        .productNameEn(product.getProductNameEn())
                                        .productNameKh(product.getProductNameKh())
                                        .expiry(item.getExpiry())
                                        .unitId(unit.getUnitId())
                                        .unitCode(unit.getUnitCode())
                                        .unitNameEn(unit.getUnitNameEn())
                                        .unitNameKh(unit.getUnitNameKh())
                                        .unitQuantity(item.getUnitQuantity())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .baseUnitPrice(item.getBaseUnitPrice())
                                        .discount(item.getDiscount())
                                        .itemDiscount(item.getItemDiscount())
                                        .taxRateId(itemTax.getId())
                                        .taxRateName(itemTax.getName())
                                        .taxRateValue(itemTax.getRate())
                                        .itemTax(item.getItemTax())
                                        .subtotal(item.getSubtotal())
                                        .description(item.getDescription())
                                    .build();
                            }).collect(Collectors.toList())
                        )
                    .build();
                allPageSequences.append(taxRateService.buildPageSequence(tx));
            }
            String fullFoContent = """
                <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
                    <fo:layout-master-set>
                        <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm" margin-top="1.0cm" margin-bottom="3.5cm" margin-left="0.8cm" margin-right="0.8cm">
                            <fo:region-body margin-top="9.5cm" margin-bottom="2.5cm"/>
                            <fo:region-before extent="3.5cm"/>
                            <fo:region-after extent="3.2cm"/>
                        </fo:simple-page-master>
                    </fo:layout-master-set>
                """ + allPageSequences.toString() + """
                </fo:root>
            """;
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Source src = new StreamSource(new StringReader(fullFoContent));
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        } catch (Exception e) {
            throw new ApiException("PDF generation failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return out.toByteArray();
    }
}
