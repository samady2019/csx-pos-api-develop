package kh.com.csx.posapi.service;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.ReferenceKey;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.payment.PaymentCreateRequest;
import kh.com.csx.posapi.dto.setting.FileInfoResponse;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.dto.purchase.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    @Autowired
    private EntityManager entityManager;

    private final BillerRepository billerRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final TaxRateRepository taxRateRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRepository purchaseRepository;
    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;

    private final ProductService productService;
    private final Utility utility;

    public PurchaseResponse getPurchaseById(Long id) {
        PurchaseEntity data = purchaseRepository.findById(id).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);
        data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
        return PurchaseResponse.builder().purchase(data).build();
    }

    public Page<PurchaseResponse> getAllPurchases(PurchaseRetrieveRequest request) {
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
        Page<PurchaseEntity> purchaseEntities = purchaseRepository.findAllByFilter(request, pageable);
        return purchaseEntities.map(
            data -> {
                data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
                return PurchaseResponse.builder().purchase(data).build();
            }
        );
    }

    @Transactional
    public PurchaseResponse createPurchase(PurchaseCreateRequest request) {
        SettingEntity settings = utility.getSettings();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (request.getPurchaseOrderId() != null) {
            PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(request.getPurchaseOrderId()).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
            if (purchaseOrder.getStatus().equals(Constant.PurchaseStatus.COMPLETED)) {
                throw new ApiException("Unable to create purchase: The purchase order '" + purchaseOrder.getReferenceNo() + "' has already been processed.", HttpStatus.BAD_REQUEST);
            }
        }
        if (!Constant.PurchaseStatus.VALID.contains(request.getStatus().trim())) {
            throw new ApiException("Invalid status. " + Constant.PurchaseStatus.NOTE, HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        supplierRepository.findById(request.getSupplierId()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(request);

        LocalDateTime date       = request.getDate();
        LocalDate due_date       = request.getPaymentTerm() != null ? date.toLocalDate().plusDays(request.getPaymentTerm()) : null;
        Long   purchase_order_id = request.getPurchaseOrderId();
        Long   biller_id         = request.getBillerId();
        Long   warehouse_id      = request.getWarehouseId();
        Long   supplierId        = request.getSupplierId();
        Long   user_id           = user.getUserId();
        String reference_no      = utility.checkReferenceNo(biller_id, ReferenceKey.P, request.getReferenceNo().trim());
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
        String status            = request.getStatus().trim();
        PurchaseEntity purchase  = new PurchaseEntity();
        purchase.setDate(date);
        purchase.setDueDate(due_date);
        purchase.setPurchaseOrderId(purchase_order_id);
        purchase.setReferenceNo(reference_no);
        purchase.setBillerId(biller_id);
        purchase.setWarehouseId(warehouse_id);
        purchase.setSupplierId(supplierId);
        purchase.setPaymentTerm(request.getPaymentTerm());
        purchase.setCurrencies(request.getCurrencies());
        purchase.setAttachment(request.getAttachment());
        purchase.setStaffNote(request.getStaffNote());
        purchase.setNote(request.getNote());
        purchase.setCreatedBy(user_id);
        purchase.setCreatedAt(LocalDateTime.now());
        List<StockMovementEntity> stocks  = new ArrayList<>();
        List<PurchaseItemEntity> products = new ArrayList<>();
        for (PurchaseItemRequest item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitId() == null) {
                throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitCost() == null || item.getUnitCost() < 0) {
                throw new ApiException("Unit cost is required and must be a positive value.", HttpStatus.BAD_REQUEST);
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
            if (!product_details.getType().equals(Constant.ProductType.STANDARD)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") with type '" + product_details.getType() + "' is not allowed.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(item.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(item.getProductId(), item.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            taxRateRepository.findById(item.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
            if (settings.getProductExpiry().equals(Constant.NO)) {
                item.setExpiry(null);
            }
            if (item.getExpiry() != null && item.getExpiry().isBefore(LocalDate.now())) {
                throw new ApiException("Expiry date cannot be earlier than the current date.", HttpStatus.BAD_REQUEST);
            }
            Long      item_product_id         = item.getProductId();
            LocalDate item_expiry             = item.getExpiry();
            Long      item_unit_id            = item.getUnitId();
            double    item_quantity           = utility.formatQuantity(item.getUnitQuantity());
            double    item_cost               = utility.formatDecimal(item.getUnitCost());
            String    item_discount_id        = item.getDiscount();
            Long      item_tax_id             = item.getTaxRateId();

            double    item_r_discount         = utility.calculateDiscount(item_cost, item_discount_id);
            double    item_f_discount         = utility.formatDecimal(item_cost - item_r_discount);
            double    item_discount           = utility.formatDecimal(item_cost - item_f_discount);
            double    item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product_details.getTaxMethod()));
            double    item_net_unit_cost      = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product_details.getTaxMethod()));
            double    item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_cost, 0, item_tax, 0));
            double    item_subtotal           = utility.formatDecimal(item_total * item_quantity);

            double    item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
            double    item_base_unit_cost     = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));

            PurchaseItemEntity product = new PurchaseItemEntity();
            product.setWarehouseId(warehouse_id);
            product.setProductId(item_product_id);
            product.setExpiry(item_expiry);
            product.setUnitId(item_unit_id);
            product.setUnitQuantity(item_quantity);
            product.setQuantity(item_base_unit_quantity);
            product.setUnitCost(item_cost);
            product.setBaseUnitCost(item_base_unit_cost);
            product.setNetUnitCost(item_net_unit_cost);
            product.setDiscount(item_discount_id);
            product.setItemDiscount(item_discount);
            product.setTaxRateId(item_tax_id);
            product.setItemTax(item_tax);
            product.setSubtotal(item_subtotal);
            product.setDescription(item.getDescription());
            products.add(product);

            StockMovementEntity stock = new StockMovementEntity();
            stock.setDate(date);
            stock.setTransaction(Constant.StockTransactionType.PURCHASE);
            stock.setWarehouseId(warehouse_id);
            stock.setProductId(item_product_id);
            stock.setExpiry(item_expiry);
            stock.setUnitId(item_unit_id);
            stock.setUnitQuantity(item_quantity);
            stock.setQuantity(item_base_unit_quantity);
            stock.setCost(item_base_unit_cost);
            stocks.add(stock);

            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping));
        purchase.setTotal(total);
        purchase.setSurcharge(0.0);
        purchase.setShipping(shipping);
        purchase.setProductDiscount(product_discount);
        purchase.setOrderDiscountId(discount_id);
        purchase.setOrderDiscount(discount);
        purchase.setTotalDiscount(product_discount + discount);
        purchase.setProductTax(product_tax);
        purchase.setOrderTaxId(tax_id);
        purchase.setOrderTax(tax);
        purchase.setTotalTax(product_tax + tax);
        purchase.setGrandTotal(grand_total);
        purchase.setPaid(paid);
        purchase.setStatus(status);
        purchase.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
        try {
            PurchaseEntity data = purchaseRepository.save(purchase);
            Long purchase_id    = data.getId();
            products.forEach(product -> product.setPurchaseId(purchase_id));
            stocks.forEach(stock -> stock.setTransactionId(purchase_id));
            purchaseItemRepository.saveAll(products);
            if (status.equals(Constant.PurchaseStatus.PARTIAL) || status.equals(Constant.PurchaseStatus.COMPLETED)) {
                stockMovementRepository.saveAll(stocks);
            }
            if (purchase_order_id != null) {
                purchaseRepository.updatePurchaseOrderStatus(purchase_order_id);
            }
            utility.updateReferenceNo(biller_id, ReferenceKey.P, reference_no);
            entityManager.flush();
            entityManager.clear();
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
                double deposit = 0;
                List<PaymentEntity> payments = new ArrayList<>();
                String pay_reference_no = utility.checkReferenceNo(biller_id, ReferenceKey.PAY, request.getPayments().get(0).getReferenceNo().trim());
                for (PaymentCreateRequest req_payment : request.getPayments()) {
                    PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(req_payment.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
                    String currenciesJson = req_payment.getCurrencies();
                    double paid_amount    = utility.formatDecimal(utility.calculateTotalBaseCurrency(currenciesJson));
                    if (paid_amount <= 0) {
                        throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
                    }
                    PaymentEntity payment = new PaymentEntity();
                    payment.setBillerId(biller_id);
                    payment.setPurchaseId(purchase_id);
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
                    payment.setType(Constant.PaymentTransactionStatus.SENT);
                    payment.setCreatedBy(user_id);
                    payment.setCreatedAt(LocalDateTime.now());
                    if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
                        deposit += paid_amount;
                    }
                    payments.add(payment);
                }
                if (deposit > 0) {
                    if (purchase_order_id == null) {
                        throw new ApiException("Deposit payment require an associated purchase order. Please provide a valid purchase order ID.", HttpStatus.BAD_REQUEST);
                    } else if (deposit > paymentRepository.getTotalDepositByPurchaseOrderId(purchase_order_id)) {
                        throw new ApiException("The payment cannot be over the available deposit amount.", HttpStatus.BAD_REQUEST);
                    }
                }
                paymentRepository.saveAll(payments);
                paymentRepository.updatePaymentStatus(null, purchase_id, null, null, null);
                utility.updateReferenceNo(biller_id, ReferenceKey.PAY, pay_reference_no);
                entityManager.flush();
                entityManager.clear();
            }
            return getPurchaseById(purchase_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PurchaseResponse updatePurchase(PurchaseUpdateRequest request) {
        SettingEntity settings = utility.getSettings();
        PurchaseEntity purchase = purchaseRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (!purchase.getStatus().equals(Constant.PurchaseStatus.PENDING) || !purchase.getPaymentStatus().equals(Constant.PaymentStatus.PENDING)) {
            throw new ApiException("Unable to edit: The purchase has already been processed.", HttpStatus.BAD_REQUEST);
        }
        if (!Constant.PurchaseStatus.VALID.contains(request.getStatus().trim())) {
            throw new ApiException("Invalid status. " + Constant.PurchaseStatus.NOTE, HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        supplierRepository.findById(request.getSupplierId()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        if (purchaseRepository.existsByReferenceNoAndIdNot(request.getReferenceNo(), purchase.getId())) {
            throw new ApiException("Reference number already exists.", HttpStatus.BAD_REQUEST);
        }
        utility.verifyAccess(request);

        LocalDateTime date       = request.getDate();
        LocalDate due_date       = request.getPaymentTerm() != null ? date.toLocalDate().plusDays(request.getPaymentTerm()) : null;
        String reference_no      = request.getReferenceNo();
        String old_reference_no  = purchase.getReferenceNo();
        Long   purchase_order_id = purchase.getPurchaseOrderId();
        Long   biller_id         = request.getBillerId();
        Long   warehouse_id      = request.getWarehouseId();
        Long   supplierId        = request.getSupplierId();
        Long   user_id           = user.getUserId();
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
        String status            = request.getStatus().trim();
        purchase.setDate(date);
        purchase.setDueDate(due_date);
        purchase.setReferenceNo(reference_no);
        purchase.setBillerId(biller_id);
        purchase.setWarehouseId(warehouse_id);
        purchase.setSupplierId(supplierId);
        purchase.setPaymentTerm(request.getPaymentTerm());
        purchase.setCurrencies(request.getCurrencies());
        purchase.setAttachment(request.getAttachment());
        purchase.setStaffNote(request.getStaffNote());
        purchase.setNote(request.getNote());
        purchase.setUpdatedBy(user_id);
        purchase.setUpdatedAt(LocalDateTime.now());
        List<StockMovementEntity> stocks  = new ArrayList<>();
        List<PurchaseItemEntity> products = new ArrayList<>();
        for (PurchaseItemRequest item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitId() == null) {
                throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitCost() == null || item.getUnitCost() < 0) {
                throw new ApiException("Unit cost is required and must be a positive value.", HttpStatus.BAD_REQUEST);
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
            if (!product_details.getType().equals(Constant.ProductType.STANDARD)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") with type '" + product_details.getType() + "' is not allowed.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(item.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(item.getProductId(), item.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            taxRateRepository.findById(item.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
            if (settings.getProductExpiry().equals(Constant.NO)) {
                item.setExpiry(null);
            }
            if (item.getExpiry() != null && item.getExpiry().isBefore(LocalDate.now())) {
                throw new ApiException("Expiry date cannot be earlier than the current date.", HttpStatus.BAD_REQUEST);
            }
            Long      item_product_id         = item.getProductId();
            LocalDate item_expiry             = item.getExpiry();
            Long      item_unit_id            = item.getUnitId();
            double    item_quantity           = utility.formatQuantity(item.getUnitQuantity());
            double    item_cost               = utility.formatDecimal(item.getUnitCost());
            String    item_discount_id        = item.getDiscount();
            Long      item_tax_id             = item.getTaxRateId();

            double    item_r_discount         = utility.calculateDiscount(item_cost, item_discount_id);
            double    item_f_discount         = utility.formatDecimal(item_cost - item_r_discount);
            double    item_discount           = utility.formatDecimal(item_cost - item_f_discount);
            double    item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product_details.getTaxMethod()));
            double    item_net_unit_cost      = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product_details.getTaxMethod()));
            double    item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_cost, 0, item_tax, 0));
            double    item_subtotal           = utility.formatDecimal(item_total * item_quantity);

            double    item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
            double    item_base_unit_cost     = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));

            PurchaseItemEntity product = new PurchaseItemEntity();
            product.setWarehouseId(warehouse_id);
            product.setProductId(item_product_id);
            product.setExpiry(item_expiry);
            product.setUnitId(item_unit_id);
            product.setUnitQuantity(item_quantity);
            product.setQuantity(item_base_unit_quantity);
            product.setUnitCost(item_cost);
            product.setBaseUnitCost(item_base_unit_cost);
            product.setNetUnitCost(item_net_unit_cost);
            product.setDiscount(item_discount_id);
            product.setItemDiscount(item_discount);
            product.setTaxRateId(item_tax_id);
            product.setItemTax(item_tax);
            product.setSubtotal(item_subtotal);
            product.setDescription(item.getDescription());
            products.add(product);

            StockMovementEntity stock = new StockMovementEntity();
            stock.setDate(date);
            stock.setTransaction(Constant.StockTransactionType.PURCHASE);
            stock.setWarehouseId(warehouse_id);
            stock.setProductId(item_product_id);
            stock.setExpiry(item_expiry);
            stock.setUnitId(item_unit_id);
            stock.setUnitQuantity(item_quantity);
            stock.setQuantity(item_base_unit_quantity);
            stock.setCost(item_base_unit_cost);
            stocks.add(stock);

            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping));
        purchase.setTotal(total);
        purchase.setSurcharge(0.0);
        purchase.setShipping(shipping);
        purchase.setProductDiscount(product_discount);
        purchase.setOrderDiscountId(discount_id);
        purchase.setOrderDiscount(discount);
        purchase.setTotalDiscount(product_discount + discount);
        purchase.setProductTax(product_tax);
        purchase.setOrderTaxId(tax_id);
        purchase.setOrderTax(tax);
        purchase.setTotalTax(product_tax + tax);
        purchase.setGrandTotal(grand_total);
        purchase.setPaid(paid);
        purchase.setStatus(status);
        purchase.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
        try {
            Long purchase_id = purchase.getId();
            purchaseItemRepository.deleteByPurchaseId(purchase_id);
            stockMovementRepository.deleteByTransactionAndTransactionId(Constant.StockTransactionType.PURCHASE, purchase_id);
            paymentRepository.deletePaymentsByPurchaseId(purchase_id);

            purchaseRepository.save(purchase);
            products.forEach(product -> product.setPurchaseId(purchase_id));
            stocks.forEach(stock -> stock.setTransactionId(purchase_id));
            purchaseItemRepository.saveAll(products);
            if (status.equals(Constant.PurchaseStatus.PARTIAL) || status.equals(Constant.PurchaseStatus.COMPLETED)) {
                stockMovementRepository.saveAll(stocks);
            }
            if (purchase_order_id != null) {
                purchaseRepository.updatePurchaseOrderStatus(purchase_order_id);
            }
            if (!old_reference_no.equals(reference_no)) {
                utility.updateReferenceNo(biller_id, ReferenceKey.P, reference_no);
            }
            entityManager.flush();
            entityManager.clear();
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
                double deposit = 0;
                List<PaymentEntity> payments = new ArrayList<>();
                String pay_reference_no = utility.checkReferenceNo(biller_id, ReferenceKey.PAY, request.getPayments().get(0).getReferenceNo().trim());
                for (PaymentCreateRequest req_payment : request.getPayments()) {
                    PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(req_payment.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
                    String currenciesJson = req_payment.getCurrencies();
                    double paid_amount    = utility.formatDecimal(utility.calculateTotalBaseCurrency(currenciesJson));
                    if (paid_amount <= 0) {
                        throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
                    }
                    PaymentEntity payment = new PaymentEntity();
                    payment.setBillerId(biller_id);
                    payment.setPurchaseId(purchase_id);
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
                    payment.setType(Constant.PaymentTransactionStatus.SENT);
                    payment.setCreatedBy(user_id);
                    payment.setCreatedAt(LocalDateTime.now());
                    if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
                        deposit += paid_amount;
                    }
                    payments.add(payment);
                }
                if (deposit > 0) {
                    if (purchase_order_id == null) {
                        throw new ApiException("Deposit payment require an associated purchase order. Please provide a valid purchase order ID.", HttpStatus.BAD_REQUEST);
                    } else if (deposit > paymentRepository.getTotalDepositByPurchaseOrderId(purchase_order_id)) {
                        throw new ApiException("The payment cannot be over the available deposit amount.", HttpStatus.BAD_REQUEST);
                    }
                }
                paymentRepository.saveAll(payments);
                paymentRepository.updatePaymentStatus(null, purchase_id, null, null, null);
                utility.updateReferenceNo(biller_id, ReferenceKey.PAY, pay_reference_no);
                entityManager.flush();
                entityManager.clear();
            }
            return getPurchaseById(purchase_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deletePurchase(PurchaseDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deletePurchase(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deletePurchase(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Purchase deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deletePurchase(Long id) {
        PurchaseEntity purchase = purchaseRepository.findById(id).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(purchase);
        if (!purchase.getStatus().equals(Constant.PurchaseStatus.PENDING) || !purchase.getPaymentStatus().equals(Constant.PaymentStatus.PENDING)) {
            throw new ApiException("Unable to delete purchase reference no. '" + purchase.getReferenceNo() + "'. The purchase has already been processed.", HttpStatus.BAD_REQUEST);
        }
        try {
            Long purchase_order_id = purchase.getPurchaseOrderId();
            purchaseItemRepository.deleteByPurchaseId(id);
            stockMovementRepository.deleteByTransactionAndTransactionId(Constant.StockTransactionType.PURCHASE, id);
            paymentRepository.deletePaymentsByPurchaseId(id);
            purchaseRepository.delete(purchase);
            if (purchase_order_id != null) {
                purchaseRepository.updatePurchaseOrderStatus(purchase_order_id);
            }
            return new BaseResponse("Purchase reference no. '" + purchase.getReferenceNo() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PurchaseResponse returnPurchase(PurchaseReturnRequest request) {
        SettingEntity settings = utility.getSettings();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        PurchaseEntity old_purchase = purchaseRepository.findById(request.getPurchaseId()).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
        if (old_purchase.getStatus().equals(Constant.PurchaseStatus.PENDING) || old_purchase.getStatus().equals(Constant.PurchaseStatus.RETURNED)) {
            throw new ApiException("Return not allowed. Purchase '" + old_purchase.getReferenceNo() + "' is either not completed or returned.", HttpStatus.BAD_REQUEST);
        }
        utility.printLog(purchaseRepository.checkReturn(old_purchase.getId()));
        if (purchaseRepository.checkReturn(old_purchase.getId()) != 1) {
            throw new ApiException("Return not allowed. Purchase '" + old_purchase.getReferenceNo() + "' has already been returned.", HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(old_purchase.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(old_purchase.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        supplierRepository.findById(old_purchase.getSupplierId()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(old_purchase);

        LocalDateTime date       = request.getDate();
        Long   old_purchase_id   = old_purchase.getId();
        Long   biller_id         = old_purchase.getBillerId();
        Long   warehouse_id      = old_purchase.getWarehouseId();
        Long   supplierId        = old_purchase.getSupplierId();
        Long   user_id           = user.getUserId();
        String reference_no      = utility.checkReferenceNo(biller_id, ReferenceKey.PR, request.getReferenceNo().trim());
        double product_discount  = 0;
        double product_tax       = 0;
        double total             = 0;
        String discount_id       = request.getOrderDiscountId();
        double r_discount        = 0;
        double f_discount        = 0;
        double discount          = 0;
        Long   tax_id            = request.getOrderTaxId();
        double tax               = 0;
        double surcharge         = utility.formatDecimal(request.getSurcharge());
        double shipping          = utility.formatDecimal(request.getShipping());
        double grand_total       = 0;
        double paid              = 0;
        String status            = Constant.PurchaseStatus.RETURNED;

        PurchaseEntity purchase  = new PurchaseEntity();
        purchase.setDate(date);
        purchase.setPurchaseId(old_purchase_id);
        purchase.setReferenceNo(reference_no);
        purchase.setBillerId(biller_id);
        purchase.setWarehouseId(warehouse_id);
        purchase.setSupplierId(supplierId);
        purchase.setCurrencies(request.getCurrencies());
        purchase.setAttachment(request.getAttachment());
        purchase.setStaffNote(request.getStaffNote());
        purchase.setNote(request.getNote());
        purchase.setCreatedBy(user_id);
        purchase.setCreatedAt(LocalDateTime.now());
        List<StockMovementEntity> stocks  = new ArrayList<>();
        List<PurchaseItemEntity> products = new ArrayList<>();
        for (PurchaseItemRequest item : request.getItems()) {
            if (item.getPurchaseItemId() ==  null) {
                throw new ApiException("Purchase item ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitId() == null) {
                throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getUnitCost() == null || item.getUnitCost() < 0) {
                throw new ApiException("Unit cost is required and must be a positive value.", HttpStatus.BAD_REQUEST);
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
            if (!product_details.getType().equals(Constant.ProductType.STANDARD)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") with type '" + product_details.getType() + "' is not allowed.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(item.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(item.getProductId(), item.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            taxRateRepository.findById(item.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));

            Long      item_purchase_item_id   = item.getPurchaseItemId();
            Long      item_product_id         = item.getProductId();
            LocalDate item_expiry             = item.getExpiry();
            Long      item_unit_id            = item.getUnitId();
            double    item_quantity           = utility.formatQuantity(item.getUnitQuantity());
            double    item_cost               = utility.formatDecimal(item.getUnitCost());
            String    item_discount_id        = item.getDiscount();
            Long      item_tax_id             = item.getTaxRateId();

            double    item_r_discount         = utility.calculateDiscount(item_cost, item_discount_id);
            double    item_f_discount         = utility.formatDecimal(item_cost - item_r_discount);
            double    item_discount           = utility.formatDecimal(item_cost - item_f_discount);
            double    item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product_details.getTaxMethod()));
            double    item_net_unit_cost      = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product_details.getTaxMethod()));
            double    item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_cost, 0, item_tax, 0));
            double    item_subtotal           = utility.formatDecimal(item_total * item_quantity);

            double    item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
            double    item_base_unit_cost     = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));

            Double balanceQuantity = purchaseRepository.returnBalanceQuantity(item_purchase_item_id, item_product_id, item_expiry, item_base_unit_quantity);
            if (balanceQuantity == null) {
                throw new ApiException("The purchase item '" + product_details.getProductNameEn() + " (" + product_details.getProductCode() + ")' could not be matched. Please check the product details.", HttpStatus.BAD_REQUEST);
            } else if (balanceQuantity < 0) {
                throw new ApiException("Cannot return '" + product_details.getProductNameEn() + " (" + product_details.getProductCode() + ")'. The return quantity exceeds the available balance.", HttpStatus.BAD_REQUEST);
            }
            PurchaseItemEntity product = new PurchaseItemEntity();
            product.setPurchaseItemId(item_purchase_item_id);
            product.setWarehouseId(warehouse_id);
            product.setProductId(item_product_id);
            product.setExpiry(item_expiry);
            product.setUnitId(item_unit_id);
            product.setUnitQuantity(-1 * item_quantity);
            product.setQuantity(-1 * item_base_unit_quantity);
            product.setUnitCost(item_cost);
            product.setBaseUnitCost(item_base_unit_cost);
            product.setNetUnitCost(item_net_unit_cost);
            product.setDiscount(item_discount_id);
            product.setItemDiscount(-1 * item_discount);
            product.setTaxRateId(item_tax_id);
            product.setItemTax(-1 * item_tax);
            product.setSubtotal(-1 * item_subtotal);
            product.setDescription(item.getDescription());
            products.add(product);

            StockMovementEntity stock = new StockMovementEntity();
            stock.setDate(date);
            stock.setTransaction(Constant.StockTransactionType.PURCHASE_RETURN);
            stock.setWarehouseId(warehouse_id);
            stock.setProductId(item_product_id);
            stock.setExpiry(item_expiry);
            stock.setUnitId(item_unit_id);
            stock.setUnitQuantity(-1 * item_quantity);
            stock.setQuantity(-1 * item_base_unit_quantity);
            stock.setCost(item_base_unit_cost);
            stocks.add(stock);

            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping, surcharge));
        purchase.setTotal(-1 * total);
        purchase.setSurcharge(surcharge);
        purchase.setShipping(-1 * shipping);
        purchase.setProductDiscount(-1 * product_discount);
        purchase.setOrderDiscountId(discount_id);
        purchase.setOrderDiscount(-1 * discount);
        purchase.setTotalDiscount(-1 * (product_discount + discount));
        purchase.setProductTax(-1 * product_tax);
        purchase.setOrderTaxId(tax_id);
        purchase.setOrderTax(-1 * tax);
        purchase.setTotalTax(-1 * (product_tax + tax));
        purchase.setGrandTotal(-1 * grand_total);
        purchase.setPaid(paid);
        purchase.setStatus(status);
        purchase.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
        try {
            PurchaseEntity data = purchaseRepository.save(purchase);
            Long purchase_id    = data.getId();
            products.forEach(product -> product.setPurchaseId(purchase_id));
            stocks.forEach(stock -> stock.setTransactionId(purchase_id));
            purchaseItemRepository.saveAll(products);
            stockMovementRepository.saveAll(stocks);
            utility.checkOverstock(warehouse_id, stocks);
            utility.updateReferenceNo(biller_id, ReferenceKey.PR, reference_no);
            entityManager.flush();
            entityManager.clear();
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
                double deposit = 0;
                List<PaymentEntity> payments = new ArrayList<>();
                String pay_reference_no = utility.checkReferenceNo(biller_id, ReferenceKey.PAY, request.getPayments().get(0).getReferenceNo().trim());
                for (PaymentCreateRequest req_payment : request.getPayments()) {
                    PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(req_payment.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
                    String currenciesJson = req_payment.getCurrencies();
                    double paid_amount    = utility.formatDecimal(utility.calculateTotalBaseCurrency(currenciesJson));
                    if (paid_amount >= 0) {
                        throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
                    }
                    PaymentEntity payment = new PaymentEntity();
                    payment.setBillerId(biller_id);
                    payment.setPurchaseId(purchase_id);
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
                    payment.setType(Constant.PaymentTransactionStatus.RETURNED);
                    payment.setCreatedBy(user_id);
                    payment.setCreatedAt(LocalDateTime.now());
                    if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
                        deposit += paid_amount;
                    }
                    payments.add(payment);
                }
                if (deposit < 0) {
                    if (deposit < (-1 * paymentRepository.getTotalDepositByPurchaseId(old_purchase_id))) {
                        throw new ApiException("The payment cannot be over the available deposit amount.", HttpStatus.BAD_REQUEST);
                    }
                }
                paymentRepository.saveAll(payments);
                paymentRepository.updatePaymentStatus(null, purchase_id, null, null, null);
                utility.updateReferenceNo(biller_id, ReferenceKey.PAY, pay_reference_no);
                entityManager.flush();
                entityManager.clear();
            }
            return getPurchaseById(purchase_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PurchaseResponse importPurchase(PurchaseImportRequest request, HttpServletRequest servletRequest) {
        List<FileInfoResponse> savedFiles = new ArrayList<>();
        try {
            LocalDateTime date             = utility.convertToLocalDateTime(request.getDate());
            String        referenceNo      = request.getReferenceNo();
            Long          billerId         = request.getBillerId();
            Long          warehouseId      = request.getWarehouseId();
            Long          supplierId       = request.getSupplierId();
            String        orderDiscountId  = request.getOrderDiscountId();
            Long          orderTaxId       = request.getOrderTaxId();
            double        shipping         = utility.formatDecimal(request.getShipping());
            Long          paymentTerm      = request.getPaymentTerm();
            String        currencies       = request.getCurrencies();
            MultipartFile fileAttachment   = request.getAttachment();
            String        note             = request.getNote();
            String        staffNote        = request.getStaffNote();
            String        status           = request.getStatus();
            MultipartFile fileItems        = request.getFile();
            String        attachment       = null;
            List<PurchaseItemRequest> items = new ArrayList<>();
            if (fileItems == null || fileItems.isEmpty()) {
                throw new ApiException("File is required.", HttpStatus.BAD_REQUEST);
            }
            String originalFilename = fileItems.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                throw new ApiException("Invalid file type. Only excel (.xlsx) file are allowed.", HttpStatus.BAD_REQUEST);
            }
            try (InputStream inputStream = fileItems.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    int r               = row.getRowNum();
                    String productCode  = utility.getCellValue(row.getCell(0), String.class);
                    String unitCode     = utility.getCellValue(row.getCell(1), String.class);
                    Double unitQuantity = utility.getCellValue(row.getCell(2), Double.class);
                    Double unitCost     = utility.getCellValue(row.getCell(3), Double.class);
                    String discount     = utility.getCellValue(row.getCell(4), String.class);
                    String tax          = utility.getCellValue(row.getCell(5), String.class);
                    LocalDate expiry    = utility.getCellValue(row.getCell(6), LocalDate.class);
                    String description  = utility.getCellValue(row.getCell(7), String.class);

                    if (productCode == null || productCode.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Product code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (unitCode == null || unitCode.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Unit code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (tax == null || tax.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Product Tax is required.", HttpStatus.BAD_REQUEST);
                    }
                    ProductEntity product = productRepository.findByProductCode(productCode).orElseThrow(() -> new ApiException("Row #" + (r + 1) + ": Product code '" + productCode + "' not found.", HttpStatus.BAD_REQUEST));
                    UnitEntity    unit    = unitRepository.findByUnitCode(unitCode).orElseThrow(() -> new ApiException("Row #" + (r + 1) + ": Unit code '" + unitCode + "' not found.", HttpStatus.BAD_REQUEST));
                    TaxRateEntity taxRate = taxRateRepository.findFirstByName(tax).orElseThrow(() -> new ApiException("Row #" + (r + 1) + ": Tax rate not found.", HttpStatus.BAD_REQUEST));

                    PurchaseItemRequest item = new PurchaseItemRequest();
                    item.setProductId(product.getProductId());
                    item.setExpiry(expiry);
                    item.setUnitId(unit.getUnitId());
                    item.setUnitQuantity(unitQuantity);
                    item.setUnitCost(unitCost);
                    item.setDiscount(discount);
                    item.setTaxRateId(taxRate.getId());
                    item.setDescription(description);
                    items.add(item);
                }
            }
            if (items.isEmpty()) {
                throw new ApiException("Purchase must contain at least one item.", HttpStatus.BAD_REQUEST);
            }
            if (fileAttachment != null && !fileAttachment.isEmpty()) {
                List<FileInfoResponse> fileInfoAttachment = utility.uploadFile(Constant.Directory.PURCHASE, fileAttachment, servletRequest);
                attachment = fileInfoAttachment.get(0).getFileName();
                savedFiles.addAll(fileInfoAttachment);
            }
            PurchaseCreateRequest purchaseCreateRequest = new PurchaseCreateRequest();
            purchaseCreateRequest.setDate(date);
            purchaseCreateRequest.setReferenceNo(referenceNo);
            purchaseCreateRequest.setBillerId(billerId);
            purchaseCreateRequest.setWarehouseId(warehouseId);
            purchaseCreateRequest.setSupplierId(supplierId);
            purchaseCreateRequest.setShipping(shipping);
            purchaseCreateRequest.setOrderDiscountId(orderDiscountId);
            purchaseCreateRequest.setOrderTaxId(orderTaxId);
            purchaseCreateRequest.setPaymentTerm(paymentTerm);
            purchaseCreateRequest.setCurrencies(currencies);
            purchaseCreateRequest.setAttachment(attachment);
            purchaseCreateRequest.setStaffNote(staffNote);
            purchaseCreateRequest.setNote(note);
            purchaseCreateRequest.setStatus(status);
            purchaseCreateRequest.setItems(items);
            utility.validateRequest(purchaseCreateRequest);

            return createPurchase(purchaseCreateRequest);
        } catch (Exception e) {
            for (FileInfoResponse savedFile : savedFiles) {
                try {
                    Path path = Paths.get(savedFile.getFilePath()).toAbsolutePath().normalize();
                    Files.delete(path);
                } catch (IOException ioException) {
                    System.out.println("Failed to rollback file: " + savedFile + ", " + ioException.getMessage());
                }
            }
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
