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
import kh.com.csx.posapi.dto.purchaseOrder.*;
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
public class PurchaseOrderService {

    @Autowired
    private EntityManager entityManager;

    private final BillerRepository billerRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final TaxRateRepository taxRateRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UnitRepository unitRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;

    private final ProductService productService;
    private final Utility utility;

    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrderEntity data = purchaseOrderRepository.findById(id).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);
        data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
        return PurchaseOrderResponse.builder().purchaseOrder(data).build();
    }

    public Page<PurchaseOrderResponse> getAllPurchasesOrder(PurchaseOrderRetrieveRequest request) {
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
        Page<PurchaseOrderEntity> purchaseOrderEntities = purchaseOrderRepository.findAllByFilter(request, pageable);
        return purchaseOrderEntities.map(
            data -> {
                data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
                return PurchaseOrderResponse.builder().purchaseOrder(data).build();
            }
        );
    }

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreateRequest request) {
        SettingEntity settings = utility.getSettings();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (request.getEstimatedDate() != null && request.getEstimatedDate().isBefore(request.getDate().toLocalDate())) {
            throw new ApiException("Estimated date cannot be earlier than the purchase order date.", HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        supplierRepository.findById(request.getSupplierId()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(request);

        LocalDateTime date      = request.getDate();
        Long   biller_id        = request.getBillerId();
        Long   warehouse_id     = request.getWarehouseId();
        Long   user_id          = user.getUserId();
        String reference_no     = utility.checkReferenceNo(biller_id, ReferenceKey.PO, request.getReferenceNo().trim());
        double product_discount = 0;
        double product_tax      = 0;
        double total            = 0;
        String discount_id      = request.getOrderDiscountId();
        double r_discount       = 0;
        double f_discount       = 0;
        double discount         = 0;
        Long   tax_id           = request.getOrderTaxId();
        double tax              = 0;
        double shipping         = utility.formatDecimal(request.getShipping());
        double grand_total      = 0;
        double paid             = 0;
        PurchaseOrderEntity purchaseOrder = new PurchaseOrderEntity();
        purchaseOrder.setDate(date);
        if (request.getPaymentTerm() != null) {
            purchaseOrder.setDueDate(date.toLocalDate().plusDays(request.getPaymentTerm()));
        }
        purchaseOrder.setEstimatedDate(request.getEstimatedDate());
        purchaseOrder.setReferenceNo(reference_no);
        purchaseOrder.setBillerId(biller_id);
        purchaseOrder.setWarehouseId(warehouse_id);
        purchaseOrder.setSupplierId(request.getSupplierId());
        purchaseOrder.setStatus(Constant.PurchaseStatus.DEFAULT);
        purchaseOrder.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
        purchaseOrder.setPaymentTerm(request.getPaymentTerm());
        purchaseOrder.setCurrencies(request.getCurrencies());
        purchaseOrder.setAttachment(request.getAttachment());
        purchaseOrder.setStaffNote(request.getStaffNote());
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setCreatedBy(user_id);
        purchaseOrder.setCreatedAt(LocalDateTime.now());
        List<PurchaseOrderItemEntity> products = new ArrayList<>();
        for (PurchaseOrderItemRequest item : request.getItems()) {
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

            PurchaseOrderItemEntity product = new PurchaseOrderItemEntity();
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

            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping));
        purchaseOrder.setTotal(total);
        purchaseOrder.setShipping(shipping);
        purchaseOrder.setProductDiscount(product_discount);
        purchaseOrder.setOrderDiscountId(discount_id);
        purchaseOrder.setOrderDiscount(discount);
        purchaseOrder.setTotalDiscount(product_discount + discount);
        purchaseOrder.setProductTax(product_tax);
        purchaseOrder.setOrderTaxId(tax_id);
        purchaseOrder.setOrderTax(tax);
        purchaseOrder.setTotalTax(product_tax + tax);
        purchaseOrder.setGrandTotal(grand_total);
        purchaseOrder.setPaid(paid);
        purchaseOrder.setStatus(Constant.PurchaseStatus.DEFAULT);
        purchaseOrder.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
        try {
            PurchaseOrderEntity data = purchaseOrderRepository.save(purchaseOrder);
            Long purchase_order_id   = data.getId();
            products.forEach(product -> product.setPurchaseOrderId(purchase_order_id));
            purchaseOrderItemRepository.saveAll(products);
            utility.updateReferenceNo(biller_id, ReferenceKey.PO, reference_no);
            entityManager.flush();
            entityManager.clear();
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
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
                    payment.setPurchaseOrderId(purchase_order_id);
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

                    payments.add(payment);
                }
                paymentRepository.saveAll(payments);
                paymentRepository.updatePaymentStatus(purchase_order_id, null, null, null, null);
                utility.updateReferenceNo(biller_id, ReferenceKey.PAY, pay_reference_no);
                entityManager.flush();
                entityManager.clear();
            }
            return getPurchaseOrderById(purchase_order_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(PurchaseOrderUpdateRequest request) {
        SettingEntity settings = utility.getSettings();
        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
        if (!purchaseOrder.getStatus().equals(Constant.PurchaseStatus.PENDING) || !purchaseOrder.getPaymentStatus().equals(Constant.PaymentStatus.PENDING)) {
            throw new ApiException("Unable to edit: The purchase order has already been processed.", HttpStatus.BAD_REQUEST);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (request.getEstimatedDate() != null && request.getEstimatedDate().isBefore(request.getDate().toLocalDate())) {
            throw new ApiException("Estimated date cannot be earlier than the purchase order date.", HttpStatus.BAD_REQUEST);
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        supplierRepository.findById(request.getSupplierId()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        taxRateRepository.findById(request.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        if (purchaseOrderRepository.existsByReferenceNoAndIdNot(request.getReferenceNo(), purchaseOrder.getId())) {
            throw new ApiException("Reference number already exists.", HttpStatus.BAD_REQUEST);
        }
        utility.verifyAccess(request);

        LocalDateTime date      = request.getDate();
        String reference_no     = request.getReferenceNo();
        String old_reference_no = purchaseOrder.getReferenceNo();
        Long   biller_id        = request.getBillerId();
        Long   warehouse_id     = request.getWarehouseId();
        Long   user_id          = user.getUserId();
        double product_discount = 0;
        double product_tax      = 0;
        double total            = 0;
        String discount_id      = request.getOrderDiscountId();
        double r_discount       = 0;
        double f_discount       = 0;
        double discount         = 0;
        Long   tax_id           = request.getOrderTaxId();
        double tax              = 0;
        double shipping         = utility.formatDecimal(request.getShipping());
        double grand_total      = 0;
        double paid             = 0;
        purchaseOrder.setDate(date);
        if (request.getPaymentTerm() != null) {
            purchaseOrder.setDueDate(date.toLocalDate().plusDays(request.getPaymentTerm()));
        }
        purchaseOrder.setEstimatedDate(request.getEstimatedDate());
        purchaseOrder.setReferenceNo(reference_no);
        purchaseOrder.setBillerId(biller_id);
        purchaseOrder.setWarehouseId(warehouse_id);
        purchaseOrder.setSupplierId(request.getSupplierId());
        purchaseOrder.setStatus(Constant.PurchaseStatus.DEFAULT);
        purchaseOrder.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
        purchaseOrder.setPaymentTerm(request.getPaymentTerm());
        purchaseOrder.setCurrencies(request.getCurrencies());
        purchaseOrder.setAttachment(request.getAttachment());
        purchaseOrder.setStaffNote(request.getStaffNote());
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setUpdatedBy(user_id);
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        List<PurchaseOrderItemEntity> products = new ArrayList<>();
        for (PurchaseOrderItemRequest item : request.getItems()) {
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

            PurchaseOrderItemEntity product = new PurchaseOrderItemEntity();
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

            total            += item_subtotal;
            product_discount += (item_discount * item_quantity);
            product_tax      += (item_tax * item_quantity);
        }
        r_discount  = utility.calculateDiscount(total, discount_id);
        f_discount  = utility.formatDecimal(total - r_discount);
        discount    = utility.formatDecimal(total - f_discount);
        tax         = utility.formatDecimal(utility.calculateTax(f_discount, tax_id));
        grand_total = utility.formatDecimal(utility.calculateTotal(f_discount, 0, tax, shipping));
        purchaseOrder.setTotal(total);
        purchaseOrder.setShipping(shipping);
        purchaseOrder.setProductDiscount(product_discount);
        purchaseOrder.setOrderDiscountId(discount_id);
        purchaseOrder.setOrderDiscount(discount);
        purchaseOrder.setTotalDiscount(product_discount + discount);
        purchaseOrder.setProductTax(product_tax);
        purchaseOrder.setOrderTaxId(tax_id);
        purchaseOrder.setOrderTax(tax);
        purchaseOrder.setTotalTax(product_tax + tax);
        purchaseOrder.setGrandTotal(grand_total);
        purchaseOrder.setPaid(paid);
        purchaseOrder.setStatus(Constant.PurchaseStatus.DEFAULT);
        purchaseOrder.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
        try {
            Long purchase_order_id = purchaseOrder.getId();
            purchaseOrderItemRepository.deleteByPurchaseOrderId(purchase_order_id);
            paymentRepository.deletePaymentsByPurchaseOrderId(purchase_order_id);

            purchaseOrderRepository.save(purchaseOrder);
            products.forEach(product -> product.setPurchaseOrderId(purchase_order_id));
            purchaseOrderItemRepository.saveAll(products);
            if (!old_reference_no.equals(reference_no)) {
                utility.updateReferenceNo(biller_id, ReferenceKey.PO, reference_no);
            }
            entityManager.flush();
            entityManager.clear();
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
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
                    payment.setPurchaseOrderId(purchase_order_id);
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

                    payments.add(payment);
                }
                paymentRepository.saveAll(payments);
                paymentRepository.updatePaymentStatus(purchase_order_id, null, null, null, null);
                utility.updateReferenceNo(biller_id, ReferenceKey.PAY, pay_reference_no);
                entityManager.flush();
                entityManager.clear();
            }
            return getPurchaseOrderById(purchase_order_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deletePurchaseOrder(PurchaseOrderDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deletePurchaseOrder(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deletePurchaseOrder(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Purchase order deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deletePurchaseOrder(Long id) {
        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(id).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(purchaseOrder);
        if (!purchaseOrder.getStatus().equals(Constant.PurchaseStatus.PENDING) || !purchaseOrder.getPaymentStatus().equals(Constant.PaymentStatus.PENDING)) {
            throw new ApiException("Unable to delete purchase order reference no. '" + purchaseOrder.getReferenceNo() + "'. The purchase order has already been processed.", HttpStatus.BAD_REQUEST);
        }
        try {
            purchaseOrderItemRepository.deleteByPurchaseOrderId(id);
            paymentRepository.deletePaymentsByPurchaseOrderId(id);
            purchaseOrderRepository.delete(purchaseOrder);
            return new BaseResponse("Purchase order reference no. '" + purchaseOrder.getReferenceNo() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PurchaseOrderResponse importPurchaseOrder(PurchaseOrderImportRequest request, HttpServletRequest servletRequest) {
        List<FileInfoResponse> savedFiles = new ArrayList<>();
        try {
            LocalDateTime date             = utility.convertToLocalDateTime(request.getDate());
            LocalDate     estimatedDate    = (request.getEstimatedDate() != null && !request.getEstimatedDate().isEmpty()) ? utility.convertToLocalDate(request.getEstimatedDate()) : null;
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
            MultipartFile fileItems        = request.getFile();
            String        attachment       = null;
            List<PurchaseOrderItemRequest> items = new ArrayList<>();
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
                    String description  = utility.getCellValue(row.getCell(6), String.class);

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

                    PurchaseOrderItemRequest item = new PurchaseOrderItemRequest();
                    item.setProductId(product.getProductId());
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
                throw new ApiException("Purchase order must contain at least one item.", HttpStatus.BAD_REQUEST);
            }
            if (fileAttachment != null && !fileAttachment.isEmpty()) {
                List<FileInfoResponse> fileInfoAttachment = utility.uploadFile(Constant.Directory.PURCHASE, fileAttachment, servletRequest);
                attachment = fileInfoAttachment.get(0).getFileName();
                savedFiles.addAll(fileInfoAttachment);
            }
            PurchaseOrderCreateRequest purchaseOrderCreateRequest = new PurchaseOrderCreateRequest();
            purchaseOrderCreateRequest.setDate(date);
            purchaseOrderCreateRequest.setEstimatedDate(estimatedDate);
            purchaseOrderCreateRequest.setReferenceNo(referenceNo);
            purchaseOrderCreateRequest.setBillerId(billerId);
            purchaseOrderCreateRequest.setWarehouseId(warehouseId);
            purchaseOrderCreateRequest.setSupplierId(supplierId);
            purchaseOrderCreateRequest.setShipping(shipping);
            purchaseOrderCreateRequest.setOrderDiscountId(orderDiscountId);
            purchaseOrderCreateRequest.setOrderTaxId(orderTaxId);
            purchaseOrderCreateRequest.setPaymentTerm(paymentTerm);
            purchaseOrderCreateRequest.setCurrencies(currencies);
            purchaseOrderCreateRequest.setAttachment(attachment);
            purchaseOrderCreateRequest.setStaffNote(staffNote);
            purchaseOrderCreateRequest.setNote(note);
            purchaseOrderCreateRequest.setItems(items);
            utility.validateRequest(purchaseOrderCreateRequest);

            return createPurchaseOrder(purchaseOrderCreateRequest);
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
