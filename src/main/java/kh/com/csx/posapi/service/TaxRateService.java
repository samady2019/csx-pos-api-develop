package kh.com.csx.posapi.service;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.Features;
import kh.com.csx.posapi.constant.Constant.TaxDeclarationType;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.product.ProductResponse;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.dto.taxRate.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.fop.apps.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

@Service
@RequiredArgsConstructor
public class TaxRateService {

    private final TaxRateRepository        taxRateRepository;
    private final ProductRepository        productRepository;
    private final UnitRepository           unitRepository;
    private final BillerRepository         billerRepository;
    private final WarehouseRepository      warehouseRepository;
    private final PurchaseRepository       purchaseRepository;
    private final PurchaseItemRepository   purchaseItemRepository;
    private final SaleRepository           saleRepository;
    private final SaleItemRepository       saleItemRepository;
    private final ExpenseRepository        expenseRepository;
    private final SupplierRepository       supplierRepository;
    private final CustomerRepository       customerRepository;
    private final UserRepository           userRepository;

    private final TaxDeclarationRepository taxDeclarationRepository;
    private final TaxRepository            taxRepository;
    private final TaxItemRepository        taxItemRepository;

    private final Utility                  utility;
    private final ProductService           productService;

    public TaxRateResponse getTaxRateById(Long id) {
        TaxRateEntity taxRateEntity = taxRateRepository.findById(id).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        return TaxRateResponse.builder().taxRate(taxRateEntity).build();
    }

    public TaxRateResponse getTaxRateByCode(String code) {
        TaxRateEntity taxRateEntity = taxRateRepository.findFirstByCode(code).orElseThrow(() -> new ApiException("Tax rate not found", HttpStatus.BAD_REQUEST));
        return TaxRateResponse.builder().taxRate(taxRateEntity).build();
    }

    public List<TaxRateResponse> getListTaxRates(TaxRateRetrieveRequest request) {
        List<TaxRateEntity> taxRateEntities = taxRateRepository.findListByFilter(request);
        List<TaxRateResponse> taxRateResponses = new ArrayList<>();
        for (TaxRateEntity taxRateEntity : taxRateEntities) {
            taxRateResponses.add(TaxRateResponse.builder().taxRate(taxRateEntity).build());
        }
        return taxRateResponses;
    }

    public Page<TaxRateResponse> getAllTaxRates(TaxRateRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<TaxRateEntity> taxRateEntities = taxRateRepository.findAllByFilter(request, pageable);
        return taxRateEntities.map(taxRateEntity -> TaxRateResponse.builder().taxRate(taxRateEntity).build());
    }

    public TaxRateResponse createTaxRate(TaxRateCreateRequest request) {
        if (taxRateRepository.existsByCode(request.getCode().trim())) {
            throw new ApiException("Tax rate code already exists", HttpStatus.BAD_REQUEST);
        }
        if (taxRateRepository.existsByName(request.getName().trim())) {
            throw new ApiException("Tax rate name already exists", HttpStatus.BAD_REQUEST);
        }
        TaxRateEntity taxRate = new TaxRateEntity();
        taxRate.setCode(request.getCode().trim());
        taxRate.setName(request.getName().trim());
        taxRate.setRate(request.getRate());
        try {
            TaxRateEntity savedTaxRate = taxRateRepository.save(taxRate);
            return getTaxRateById(savedTaxRate.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public TaxRateResponse updateTaxRate(TaxRateUpdateRequest request) {
        TaxRateEntity taxRate = taxRateRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Tax rate not found", HttpStatus.BAD_REQUEST));
        if (taxRateRepository.existsByCodeAndIdNot(request.getCode().trim(), taxRate.getId())) {
            throw new ApiException("Tax rate code already exists", HttpStatus.BAD_REQUEST);
        }
        if (taxRateRepository.existsByNameAndIdNot(request.getName().trim(), taxRate.getId())) {
            throw new ApiException("Tax rate name already exists", HttpStatus.BAD_REQUEST);
        }
        taxRate.setCode(request.getCode().trim());
        taxRate.setName(request.getName().trim());
        taxRate.setRate(request.getRate());
        try {
            taxRateRepository.save(taxRate);
            return getTaxRateById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteTaxRate(TaxRateDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteTaxRate(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteTaxRate(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Tax rate deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteTaxRate(Long id) {
        TaxRateEntity taxRate = taxRateRepository.findById(id).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        if (taxRateRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete tax rate '" + taxRate.getName() + "'. Tax rate is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            taxRateRepository.delete(taxRate);
            return new BaseResponse("Tax rate '" + taxRate.getName() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public TaxRateProductResponse retrieveTaxRateProduct(TaxRateProductRetrieveRequest request) {
        utility.checkModule(Features.TAX_DECLARE);

        try {
            if (request.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            Tuple result = productRepository.findTaxRateProduct(request.getProductId());
            if (result == null) {
                throw new ApiException("Product not found.", HttpStatus.BAD_REQUEST);
            }
            return new TaxRateProductResponse(result);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<TaxRateProductResponse> retrieveTaxRateProducts(TaxRateProductRetrieveRequest request) {
        utility.checkModule(Features.TAX_DECLARE);

        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("productCode");
            }
            if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Page<Tuple> result = productRepository.findTaxRateProducts(request, pageable);
            return result.map(TaxRateProductResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void updateTaxRateProducts(TaxRateProductUpdateRequest request) {
        utility.checkModule(Features.TAX_DECLARE);
        try {
            for (ProductTax productTax : request.getProductsTax()) {
                ProductEntity product = productRepository.findByProductId(productTax.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
                product.setTaxRateDeclare(productTax.getTaxRateDeclare());
                productRepository.save(product);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public TaxDeclareTransaction getTaxDeclarationByTran(String tran, Long tranId) {
        utility.checkModule(Features.TAX_DECLARE);

        tran = tran != null ? tran.trim() : null;
        if (tran == null || !TaxDeclarationType.VALID.contains(tran)) {
            throw new ApiException("Invalid tax declare type.", HttpStatus.BAD_REQUEST);
        }
        if (tranId == null) {
            throw new ApiException("Transaction ID is required.", HttpStatus.BAD_REQUEST);
        }
        utility.verifyAccess(getTransaction(tran, tranId));
        return convertTaxTransaction(tran, tranId);
    }

    public List<TaxDeclareTransaction> getTaxDeclarationByTrans(String tran, List<Long> tranIds) {
        if (tranIds == null || tranIds.isEmpty()) {
            throw new ApiException("Transaction IDs is required.", HttpStatus.BAD_REQUEST);
        }
        List<TaxDeclareTransaction> taxDeclareTransactions = new ArrayList<>();
        for (Long tranId : tranIds) {
            TaxDeclareTransaction transaction = getTaxDeclarationByTran(tran, tranId);
            taxDeclareTransactions.add(transaction);
        }
        return taxDeclareTransactions;
    }

    public Page<TaxEntity> getTaxDeclarationByTranAll(TaxDeclareRetrieveRequest request) {
        utility.checkModule(Features.TAX_DECLARE);

        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.DESC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        // if (request.getMonth() == null || request.getMonth().trim().isEmpty()) {
        //     request.setMonth(String.valueOf(LocalDate.now().getMonthValue()));
        // }
        // if (request.getYear() == null || request.getYear().trim().isEmpty()) {
        //     request.setYear(String.valueOf(LocalDate.now().getYear()));
        // }
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            request.setType(request.getType().trim());
            if (!Constant.TaxDeclarationType.VALID.contains(request.getType())) {
                throw new ApiException("Invalid type. " + TaxDeclarationType.NOTE, HttpStatus.BAD_REQUEST);
            }
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
        return taxRepository.findAllByFilter(request, pageable);
    }

    public TaxDeclareResponseDetail getTaxDeclarationById(Long id) {
        utility.checkModule(Features.TAX_DECLARE);

        if (id == null) {
            throw new ApiException("Tax declare ID is required.", HttpStatus.BAD_REQUEST);
        }
        TaxDeclarationEntity data = taxDeclarationRepository.findById(id).orElseThrow(() -> new ApiException("Tax declare not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);
        TaxDeclareResponseDetail response = new TaxDeclareResponseDetail();
        response.setId(data.getId());
        response.setDate(data.getDate());
        response.setBillerId(data.getBillerId());
        response.setType(data.getType());
        response.setFromDate(data.getFromDate());
        response.setToDate(data.getToDate());
        response.setTotalItems(data.getTotalItems());
        response.setTotal(data.getTotal());
        response.setDiscount(data.getDiscount());
        response.setTax(data.getTax());
        response.setShipping(data.getShipping());
        response.setGrandTotal(data.getGrandTotal());
        response.setCreatedBy(data.getCreatedBy());
        response.setCreatedAt(data.getCreatedAt());
        response.setUpdatedBy(data.getUpdatedBy());
        response.setUpdatedAt(data.getUpdatedAt());
        response.setBiller(data.getBiller());
        List<TaxDeclareTransaction> transactions = new ArrayList<>();
        data.getTransactions().forEach (data_transaction -> {
            transactions.add(convertTaxTransaction(data_transaction.getTransaction(), data_transaction.getTransactionId(), data_transaction.getId()));
        });
        response.setTransactions(transactions);
        return response;
    }

    public Page<TaxDeclareResponse> getAllTaxDeclarations(TaxDeclareRetrieveRequest request) {
        utility.checkModule(Features.TAX_DECLARE);
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.DESC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        // if (request.getMonth() == null || request.getMonth().trim().isEmpty()) {
        //     request.setMonth(String.valueOf(LocalDate.now().getMonthValue()));
        // }
        // if (request.getYear() == null || request.getYear().trim().isEmpty()) {
        //     request.setYear(String.valueOf(LocalDate.now().getYear()));
        // }
        if (request.getType() != null && !request.getType().trim().isEmpty()) {
            request.setType(request.getType().trim());
            if (!Constant.TaxDeclarationType.VALID.contains(request.getType())) {
                throw new ApiException("Invalid type. " + TaxDeclarationType.NOTE, HttpStatus.BAD_REQUEST);
            }
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
        Page<TaxDeclarationEntity> taxDeclarationEntities = taxDeclarationRepository.findAllByFilter(request, pageable);
        return taxDeclarationEntities.map(taxDeclarationEntity -> TaxDeclareResponse.builder().taxDeclaration(taxDeclarationEntity).build());
    }

    @Transactional
    public void createTaxDeclaration(TaxDeclareCreateRequest request) {
        utility.checkModule(Features.TAX_DECLARE);

        try {
            SettingEntity  settings = utility.getSettings();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
            utility.verifyAccess(request);

            TaxDeclarationEntity taxDeclarationEntity = new TaxDeclarationEntity();
            List<TaxEntity>      taxEntities          = new ArrayList<>();
            List<TaxItemEntity>  taxItemEntities      = new ArrayList<>();
            LocalDateTime        date                 = request.getDate();
            Long                 billerId             = request.getBillerId();
            String               type                 = request.getType();
            LocalDate            fromDate             = request.getFromDate();
            LocalDate            toDate               = request.getToDate();
            Integer              totalItems           = 0;
            Double               total                = 0.0;
            Double               discount             = 0.0;
            Double               tax                  = 0.0;
            Double               shipping             = 0.0;
            Double               grandTotal           = 0.0;
            int i = 0;
            for (TaxDeclareItemRequest item : request.getTransactions()) {
                if (!taxEntities.isEmpty() && taxEntities.stream().anyMatch(t -> Objects.equals(t.getTransactionId(), item.getTransactionId()))) {
                    continue;
                }
                if (!validTransaction(type, item.getTransactionId(), null)) {
                    throw new ApiException("Transaction already declared.", HttpStatus.BAD_REQUEST);
                }
                TaxDeclareTransaction taxTransaction = convertTaxTransaction(type, item.getTransactionId());
                TaxEntity             taxEntity      = new TaxEntity();
                String                prefixYear     = (type.equals(TaxDeclarationType.PURCHASE) ? settings.getPurchasePrefix() : settings.getSalePrefix()) + date.format(DateTimeFormatter.ofPattern("yy"));
                int                   countTrans     = taxDeclarationRepository.getLastTransactionRef(type, prefixYear);
                for (TaxDeclareTransactionItem taxTransactionItem : taxTransaction.getTransactionItems()) {
                    TaxItemEntity taxItem = new TaxItemEntity();
                    taxItem.setTransaction(taxTransactionItem.getTransaction());
                    taxItem.setTransactionId(taxTransactionItem.getTransactionId());
                    taxItem.setItemId(taxTransactionItem.getItemId());
                    taxItem.setTaxRateDeclare(taxTransactionItem.getTaxRateDeclare());
                    taxItemEntities.add(taxItem);
                }
                taxEntity.setTransactionId(taxTransaction.getTransactionId());
                taxEntity.setTransaction(taxTransaction.getTransaction());
                taxEntity.setDate(taxTransaction.getDate());
                taxEntity.setReferenceNo(taxTransaction.getReferenceNo());
                taxEntity.setTaxReferenceNo(prefixYear + "/" + String.format("%06d", ((Number) (countTrans + ++i)).longValue()));
                taxEntity.setCompanyEn(taxTransaction.getCompanyEn());
                taxEntity.setCompanyKh(taxTransaction.getCompanyKh());
                taxEntity.setNameEn(taxTransaction.getNameEn());
                taxEntity.setNameKh(taxTransaction.getNameKh());
                taxEntity.setPhone(taxTransaction.getPhone());
                taxEntity.setEmail(taxTransaction.getEmail());
                taxEntity.setVatNo(taxTransaction.getVatNo());
                taxEntity.setQuantity(utility.formatQuantity(taxTransaction.getQuantity()));
                taxEntity.setTotal(utility.formatDecimal(taxTransaction.getTotal()));
                taxEntity.setOrderDiscount(utility.formatDecimal(taxTransaction.getOrderDiscount()));
                taxEntity.setOrderTax(utility.formatDecimal(taxTransaction.getOrderTax()));
                taxEntity.setShipping(utility.formatDecimal(taxTransaction.getShipping()));
                taxEntity.setGrandTotal(utility.formatDecimal(taxTransaction.getGrandTotal()));
                taxEntity.setExchangeRate(utility.formatDecimal(item.getExchangeRate()));
                taxEntity.setNote(taxTransaction.getNote());

                taxEntities.add(taxEntity);
                totalItems  = i;
                total      += utility.formatDecimal(taxTransaction.getTotal());
                discount   += utility.formatDecimal(taxTransaction.getOrderDiscount());
                tax        += utility.formatDecimal(taxTransaction.getOrderTax());
                shipping   += utility.formatDecimal(taxTransaction.getShipping());
                grandTotal += utility.formatDecimal(taxTransaction.getGrandTotal());
            }
            taxDeclarationEntity.setDate(date);
            taxDeclarationEntity.setBillerId(billerId);
            taxDeclarationEntity.setType(type);
            taxDeclarationEntity.setFromDate(fromDate);
            taxDeclarationEntity.setToDate(toDate);
            taxDeclarationEntity.setTotalItems(totalItems);
            taxDeclarationEntity.setTotal(total);
            taxDeclarationEntity.setDiscount(discount);
            taxDeclarationEntity.setTax(tax);
            taxDeclarationEntity.setShipping(shipping);
            taxDeclarationEntity.setGrandTotal(grandTotal);
            taxDeclarationEntity.setCreatedBy(user.getUserId());
            taxDeclarationEntity.setCreatedAt(LocalDateTime.now());

            TaxDeclarationEntity data = taxDeclarationRepository.save(taxDeclarationEntity);
            taxEntities.forEach(t -> t.setTaxDeclarationId(data.getId()));
            taxRepository.saveAll(taxEntities);
            taxItemRepository.saveAll(taxItemEntities);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteTaxDeclaration(TaxDeclareDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteTaxDeclaration(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteTaxDeclaration(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Tax declare deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteTaxDeclaration(Long id) {
        utility.checkModule(Features.TAX_DECLARE);

        TaxDeclarationEntity taxDeclaration = taxDeclarationRepository.findById(id).orElseThrow(() -> new ApiException("Tax declare not found.", HttpStatus.BAD_REQUEST));
        try {
            List<TaxEntity> taxs = taxRepository.findByTaxDeclarationId(taxDeclaration.getId());
            for (TaxEntity tax : taxs) {
                taxItemRepository.deleteByTransactionAndTransactionId(tax.getTransaction(), tax.getTransactionId());
            }
            taxRepository.deleteByTaxDeclarationId(taxDeclaration.getId());
            taxDeclarationRepository.delete(taxDeclaration);
            return new BaseResponse("Tax declaration deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public boolean validTransaction(String type, Long transactionId, Long taxDeclareId) {
        return !taxDeclarationRepository.checkTransaction(type, transactionId, taxDeclareId);
    }

    public <T> T getTransaction(String type, Long transactionId) {
        T transaction = switch (type) {
            case TaxDeclarationType.PURCHASE -> (T) purchaseRepository.findById(transactionId).orElseThrow(() -> new ApiException("Transaction not found.", HttpStatus.BAD_REQUEST));
            case TaxDeclarationType.SALE     -> (T) saleRepository.findById(transactionId).orElseThrow(() -> new ApiException("Transaction not found.", HttpStatus.BAD_REQUEST));
            case TaxDeclarationType.EXPENSE  -> (T) expenseRepository.findById(transactionId).orElseThrow(() -> new ApiException("Transaction not found.", HttpStatus.BAD_REQUEST));
            default -> throw new ApiException("Invalid tax declaration type.", HttpStatus.BAD_REQUEST);
        };
        return transaction;
    }

    public TaxDeclareTransaction convertTaxTransaction(String type, Long transactionId) {
        return convertTaxTransaction(type, transactionId, null);
    }

    public TaxDeclareTransaction convertTaxTransaction(String type, Long transactionId, Long taxDeclareId) {
        SettingEntity       settings     = utility.getSettings();
        TaxEntity           dataTax      = taxRepository.findByTransactionAndTransactionId(type, transactionId);
        List<TaxItemEntity> dataTaxItems = taxItemRepository.findByTransactionAndTransactionId(type, transactionId);
        Long                taxId;
        Long                taxDeclarationId;
        Long                taxTransactionId;
        String              taxTransaction;
        LocalDateTime       taxDate;
        LocalDate           taxDueDate;
        String              taxReferenceNo;
        String              taxTaxReferenceNo;
        Long                taxBillerId;
        Long                taxWarehouseId;
        Long                taxCompanyId;
        String              taxCompanyEn;
        String              taxCompanyKh;
        String              taxNameEn;
        String              taxNameKh;
        String              taxPhone;
        String              taxEmail;
        String              taxVatNo;
        Double              taxQuantity;
        Double              taxTotal;
        Double              taxShipping;
        Double              taxProductDiscount;
        String              taxOrderDiscountId;
        Double              taxOrderDiscount;
        Double              taxTotalDiscount;
        Double              taxProductTax;
        Long                taxOrderTaxId;
        String              taxOrderTaxName;
        Double              taxOrderTaxValue;
        Double              taxOrderTax;
        Double              taxTotalTax;
        Double              taxGrandTotal;
        Integer             taxTotalItems;
        Double              taxExchangeRate;
        String              taxNote;
        Long                taxCreatedBy;
        Long                taxUpdatedBy;
        LocalDateTime       taxCreatedAt;
        LocalDateTime       taxUpdatedAt;
        String              taxCreatedByName;
        String              taxUpdatedByName;
        BillerEntity        taxBiller;
        WarehouseEntity     taxWarehouse;
        Object              taxCompany;
        List<TaxDeclareTransactionItem> taxItems = new ArrayList<>();
        if (type.equals(TaxDeclarationType.PURCHASE)) {
            PurchaseEntity           purchase      = getTransaction(type, transactionId);
            List<PurchaseItemEntity> purchaseItems = purchaseItemRepository.findByPurchaseId(purchase.getId());
            BillerEntity             biller        = billerRepository.findById(purchase.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
            WarehouseEntity          warehouse     = warehouseRepository.findById(purchase.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
            SupplierEntity           supplier      = supplierRepository.findById(purchase.getSupplierId()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
            UserEntity               createdBy     = purchase.getCreatedBy() != null ? userRepository.findByUserId(purchase.getCreatedBy()).orElse(null) : null;
            UserEntity               updatedBy     = purchase.getUpdatedBy() != null ? userRepository.findByUserId(purchase.getUpdatedBy()).orElse(null) : null;
            TaxRateEntity            orderTaxRate  = taxRateRepository.findById(purchase.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
            double product_discount  = 0;
            double product_tax       = 0;
            double total             = 0;
            String discount_id       = purchase.getOrderDiscountId();
            double r_discount        = 0;
            double f_discount        = 0;
            double discount          = 0;
            Long   tax_id            = orderTaxRate.getId();
            String tax_name          = orderTaxRate.getName();
            double tax_value         = orderTaxRate.getRate();
            double tax               = 0;
            double shipping          = utility.formatDecimal(purchase.getShipping());
            double grand_total       = 0;
            int    total_items       = 0;
            for (PurchaseItemEntity purchaseItem : purchaseItems) {
                if (!dataTaxItems.isEmpty() && dataTaxItems.stream().noneMatch(item -> item.getItemId().equals(purchaseItem.getId()))) continue;
                TaxItemEntity   taxItemEntity = dataTaxItems.stream().filter(item -> item.getItemId().equals(purchaseItem.getId())).findFirst().orElse(null);
                TaxRateEntity   taxRateEntity = taxRateRepository.findById(purchaseItem.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
                UnitEntity      unitEntity    = unitRepository.findByUnitId(purchaseItem.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
                ProductResponse product       = productService.getProductByID(purchaseItem.getProductId());

                double          item_tax_rate_declare   = taxItemEntity != null ? taxItemEntity.getTaxRateDeclare() : (product.getTaxRateDeclare() != null ? product.getTaxRateDeclare() : settings.getDefaultTaxRateDeclare());
                Long            item_product_id         = product.getProductId();
                String          item_product_code       = product.getProductCode();
                String          item_product_bar_code   = product.getBarCode();
                String          item_product_name_en    = product.getProductNameEn();
                String          item_product_name_kh    = product.getProductNameKh();
                LocalDate       item_expiry             = purchaseItem.getExpiry();
                Long            item_unit_id            = unitEntity.getUnitId();
                String          item_unit_code          = unitEntity.getUnitCode();
                String          item_unit_name_en       = unitEntity.getUnitNameEn();
                String          item_unit_name_kh       = unitEntity.getUnitNameKh();
                double          item_quantity           = utility.formatQuantity(purchaseItem.getUnitQuantity());
                double          item_price              = utility.formatDecimal((item_tax_rate_declare * purchaseItem.getUnitCost()) / 100);
                String          item_discount_id        = purchaseItem.getDiscount();
                Long            item_tax_id             = taxRateEntity.getId();
                String          item_tax_name           = taxRateEntity.getName();
                double          item_tax_value          = taxRateEntity.getRate();
                double          item_r_discount         = utility.calculateDiscount(item_price, item_discount_id);
                double          item_f_discount         = utility.formatDecimal(item_price - item_r_discount);
                double          item_discount           = utility.formatDecimal(item_price - item_f_discount);
                double          item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product.getTaxMethod()));
                double          item_net_unit_price     = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product.getTaxMethod()));
                double          item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_price, 0, item_tax, 0));
                double          item_subtotal           = utility.formatDecimal(item_total * item_quantity);
                double          item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
                double          item_base_unit_price    = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));
                Long            taxItemId               = taxItemEntity != null ? taxItemEntity.getId()             : null;
                Long            taxItemTransactionId    = taxItemEntity != null ? taxItemEntity.getTransactionId()  : purchaseItem.getPurchaseId();
                String          taxItemTransaction      = type;
                Long            taxItemItemId           = purchaseItem.getId();
                Double          taxItemTaxRateDeclare   = item_tax_rate_declare;
                Long            taxItemWarehouseId      = purchaseItem.getWarehouseId();
                Long            taxItemProductId        = item_product_id;
                String          taxItemProductCode      = item_product_code;
                String          taxItemProductBarCode   = item_product_bar_code;
                String          taxItemProductNameEn    = item_product_name_en;
                String          taxItemProductNameKh    = item_product_name_kh;
                LocalDate       taxItemExpiry           = item_expiry;
                Long            taxItemUnitId           = item_unit_id;
                String          taxItemUnitCode         = item_unit_code;
                String          taxItemUnitNameEn       = item_unit_name_en;
                String          taxItemUnitNameKh       = item_unit_name_kh;
                Double          taxItemUnitQuantity     = item_quantity;
                Double          taxItemQuantity         = item_base_unit_quantity;
                Double          taxItemUnitPrice        = item_price;
                Double          taxItemBaseUnitPrice    = item_base_unit_price;
                String          taxItemDiscount         = item_discount_id;
                Double          taxItemItemDiscount     = item_discount;
                Long            taxItemTaxRateId        = item_tax_id;
                String          taxItemTaxRateName      = item_tax_name;
                Double          taxItemTaxRateValue     = item_tax_value;
                Double          taxItemItemTax          = item_tax;
                Double          taxItemSubtotal         = item_subtotal;
                String          taxItemDescription      = purchaseItem.getDescription();
                ProductResponse taxItemProduct          = product;

                TaxDeclareTransactionItem taxItem = new TaxDeclareTransactionItem();
                taxItem.setId(taxItemId);
                taxItem.setTransactionId(taxItemTransactionId);
                taxItem.setTransaction(taxItemTransaction);
                taxItem.setItemId(taxItemItemId);
                taxItem.setTaxRateDeclare(taxItemTaxRateDeclare);
                taxItem.setWarehouseId(taxItemWarehouseId);
                taxItem.setProductId(taxItemProductId);
                taxItem.setProductCode(taxItemProductCode);
                taxItem.setProductBarCode(taxItemProductBarCode);
                taxItem.setProductNameEn(taxItemProductNameEn);
                taxItem.setProductNameKh(taxItemProductNameKh);
                taxItem.setExpiry(taxItemExpiry);
                taxItem.setUnitId(taxItemUnitId);
                taxItem.setUnitCode(taxItemUnitCode);
                taxItem.setUnitNameEn(taxItemUnitNameEn);
                taxItem.setUnitNameKh(taxItemUnitNameKh);
                taxItem.setUnitQuantity(taxItemUnitQuantity);
                taxItem.setQuantity(taxItemQuantity);
                taxItem.setUnitPrice(taxItemUnitPrice);
                taxItem.setBaseUnitPrice(taxItemBaseUnitPrice);
                taxItem.setDiscount(taxItemDiscount);
                taxItem.setItemDiscount(taxItemItemDiscount);
                taxItem.setTaxRateId(taxItemTaxRateId);
                taxItem.setTaxRateName(taxItemTaxRateName);
                taxItem.setTaxRateValue(taxItemTaxRateValue);
                taxItem.setItemTax(taxItemItemTax);
                taxItem.setSubtotal(taxItemSubtotal);
                taxItem.setDescription(taxItemDescription);
                taxItems.add(taxItem);

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

            taxId              = dataTax != null ? dataTax.getId()             : null;
            taxDeclarationId   = taxDeclareId;
            taxTransactionId   = transactionId;
            taxTransaction     = type;
            taxDate            = dataTax != null ? dataTax.getDate()           : purchase.getDate();
            taxDueDate         = purchase.getDueDate();
            taxReferenceNo     = dataTax != null ? dataTax.getReferenceNo()    : purchase.getReferenceNo();
            taxTaxReferenceNo  = dataTax != null ? dataTax.getTaxReferenceNo() : null;
            taxBillerId        = purchase.getBillerId();
            taxWarehouseId     = purchase.getWarehouseId();
            taxCompanyId       = purchase.getSupplierId();
            taxCompanyEn       = dataTax != null ? dataTax.getCompanyEn()      : supplier.getCompanyEn();
            taxCompanyKh       = dataTax != null ? dataTax.getCompanyKh()      : supplier.getCompanyKh();
            taxNameEn          = dataTax != null ? dataTax.getNameEn()         : supplier.getNameEn();
            taxNameKh          = dataTax != null ? dataTax.getNameKh()         : supplier.getNameKh();
            taxPhone           = dataTax != null ? dataTax.getPhone()          : supplier.getPhone();
            taxEmail           = dataTax != null ? dataTax.getEmail()          : supplier.getEmail();
            taxVatNo           = dataTax != null ? dataTax.getVatNo()          : supplier.getVatNo();
            taxQuantity        = dataTax != null ? dataTax.getQuantity()       : utility.formatQuantity(purchaseRepository.getPurchaseTotalQuantity(purchase.getId()));
            taxTotal           = total;
            taxShipping        = shipping;
            taxProductDiscount = product_discount;
            taxOrderDiscountId = discount_id;
            taxOrderDiscount   = discount;
            taxTotalDiscount   = product_discount + discount;
            taxProductTax      = product_tax;
            taxOrderTaxId      = tax_id;
            taxOrderTaxName    = tax_name;
            taxOrderTaxValue   = tax_value;
            taxOrderTax        = tax;
            taxTotalTax        = product_tax + tax;
            taxGrandTotal      = grand_total;
            taxTotalItems      = total_items;
            taxExchangeRate    = dataTax != null ? dataTax.getExchangeRate()   : null;
            taxNote            = purchase.getNote();
            taxCreatedBy       = purchase.getCreatedBy();
            taxCreatedAt       = purchase.getCreatedAt();
            taxUpdatedBy       = purchase.getUpdatedBy();
            taxUpdatedAt       = purchase.getUpdatedAt();
            taxCreatedByName   = (createdBy != null ? (createdBy.getEmployee() != null ? (createdBy.getEmployee().getFirstName() + " " + createdBy.getEmployee().getLastName()) : createdBy.getUsername()) : "");
            taxUpdatedByName   = (updatedBy != null ? (updatedBy.getEmployee() != null ? (updatedBy.getEmployee().getFirstName() + " " + updatedBy.getEmployee().getLastName()) : updatedBy.getUsername()) : "");
            taxBiller          = biller;
            taxWarehouse       = warehouse;
            taxCompany         = supplier;
        } else {
            SaleEntity               sale          = getTransaction(type, transactionId);
            List<SaleItemEntity>     saleItems     = saleItemRepository.findBySaleId(sale.getId());
            BillerEntity             biller        = billerRepository.findById(sale.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
            WarehouseEntity          warehouse     = warehouseRepository.findById(sale.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
            CustomerEntity           customer      = customerRepository.findById(sale.getCustomerId()).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));
            UserEntity               createdBy     = sale.getCreatedBy() != null ? userRepository.findByUserId(sale.getCreatedBy()).orElse(null) : null;
            UserEntity               updatedBy     = sale.getUpdatedBy() != null ? userRepository.findByUserId(sale.getUpdatedBy()).orElse(null) : null;
            TaxRateEntity            orderTaxRate  = taxRateRepository.findById(sale.getOrderTaxId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
            double product_discount  = 0;
            double product_tax       = 0;
            double total             = 0;
            String discount_id       = sale.getOrderDiscountId();
            double r_discount        = 0;
            double f_discount        = 0;
            double discount          = 0;
            Long   tax_id            = orderTaxRate.getId();
            String tax_name          = orderTaxRate.getName();
            double tax_value         = orderTaxRate.getRate();
            double tax               = 0;
            double shipping          = utility.formatDecimal(sale.getShipping());
            double grand_total       = 0;
            int    total_items       = 0;
            for (SaleItemEntity saleItem : saleItems) {
                if (!dataTaxItems.isEmpty() && dataTaxItems.stream().noneMatch(item -> item.getItemId().equals(saleItem.getId()))) continue;
                TaxItemEntity   taxItemEntity = dataTaxItems.stream().filter(item -> item.getItemId().equals(saleItem.getId())).findFirst().orElse(null);
                TaxRateEntity   taxRateEntity = taxRateRepository.findById(saleItem.getTaxRateId()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
                UnitEntity      unitEntity    = unitRepository.findByUnitId(saleItem.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
                ProductResponse product       = productService.getProductByID(saleItem.getProductId());

                double          item_tax_rate_declare   = taxItemEntity != null ? taxItemEntity.getTaxRateDeclare() : (product.getTaxRateDeclare() != null ? product.getTaxRateDeclare() : settings.getDefaultTaxRateDeclare());
                Long            item_product_id         = product.getProductId();
                String          item_product_code       = product.getProductCode();
                String          item_product_bar_code   = product.getBarCode();
                String          item_product_name_en    = product.getProductNameEn();
                String          item_product_name_kh    = product.getProductNameKh();
                LocalDate       item_expiry             = saleItem.getExpiry();
                Long            item_unit_id            = unitEntity.getUnitId();
                String          item_unit_code          = unitEntity.getUnitCode();
                String          item_unit_name_en       = unitEntity.getUnitNameEn();
                String          item_unit_name_kh       = unitEntity.getUnitNameKh();
                double          item_quantity           = utility.formatQuantity(saleItem.getUnitQuantity());
                double          item_price              = utility.formatDecimal((item_tax_rate_declare * saleItem.getUnitPrice()) / 100);
                String          item_discount_id        = saleItem.getDiscount();
                Long            item_tax_id             = taxRateEntity.getId();
                String          item_tax_name           = taxRateEntity.getName();
                double          item_tax_value          = taxRateEntity.getRate();
                double          item_r_discount         = utility.calculateDiscount(item_price, item_discount_id);
                double          item_f_discount         = utility.formatDecimal(item_price - item_r_discount);
                double          item_discount           = utility.formatDecimal(item_price - item_f_discount);
                double          item_tax                = utility.formatDecimal(utility.calculateTax(item_f_discount, item_tax_id, product.getTaxMethod()));
                double          item_net_unit_price     = utility.formatDecimal(utility.calculateNetAmount(item_f_discount, item_tax, product.getTaxMethod()));
                double          item_total              = utility.formatDecimal(utility.calculateTotal(item_net_unit_price, 0, item_tax, 0));
                double          item_subtotal           = utility.formatDecimal(item_total * item_quantity);
                double          item_base_unit_quantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(item_unit_id, item_quantity));
                double          item_base_unit_price    = utility.formatDecimal(utility.convertToBaseUnitPrice(item_unit_id, item_total));
                Long            taxItemId               = taxItemEntity != null ? taxItemEntity.getId()             : null;
                Long            taxItemTransactionId    = taxItemEntity != null ? taxItemEntity.getTransactionId()  : saleItem.getSaleId();
                String          taxItemTransaction      = type;
                Long            taxItemItemId           = saleItem.getId();
                Double          taxItemTaxRateDeclare   = item_tax_rate_declare;
                Long            taxItemWarehouseId      = saleItem.getWarehouseId();
                Long            taxItemProductId        = item_product_id;
                String          taxItemProductCode      = item_product_code;
                String          taxItemProductBarCode   = item_product_bar_code;
                String          taxItemProductNameEn    = item_product_name_en;
                String          taxItemProductNameKh    = item_product_name_kh;
                LocalDate       taxItemExpiry           = item_expiry;
                Long            taxItemUnitId           = item_unit_id;
                String          taxItemUnitCode         = item_unit_code;
                String          taxItemUnitNameEn       = item_unit_name_en;
                String          taxItemUnitNameKh       = item_unit_name_kh;
                Double          taxItemUnitQuantity     = item_quantity;
                Double          taxItemQuantity         = item_base_unit_quantity;
                Double          taxItemUnitPrice        = item_price;
                Double          taxItemBaseUnitPrice    = item_base_unit_price;
                String          taxItemDiscount         = item_discount_id;
                Double          taxItemItemDiscount     = item_discount;
                Long            taxItemTaxRateId        = item_tax_id;
                String          taxItemTaxRateName      = item_tax_name;
                Double          taxItemTaxRateValue     = item_tax_value;
                Double          taxItemItemTax          = item_tax;
                Double          taxItemSubtotal         = item_subtotal;
                String          taxItemDescription      = saleItem.getDescription();
                ProductResponse taxItemProduct          = product;

                TaxDeclareTransactionItem taxItem = new TaxDeclareTransactionItem();
                taxItem.setId(taxItemId);
                taxItem.setTransactionId(taxItemTransactionId);
                taxItem.setTransaction(taxItemTransaction);
                taxItem.setItemId(taxItemItemId);
                taxItem.setTaxRateDeclare(taxItemTaxRateDeclare);
                taxItem.setWarehouseId(taxItemWarehouseId);
                taxItem.setProductId(taxItemProductId);
                taxItem.setProductCode(taxItemProductCode);
                taxItem.setProductBarCode(taxItemProductBarCode);
                taxItem.setProductNameEn(taxItemProductNameEn);
                taxItem.setProductNameKh(taxItemProductNameKh);
                taxItem.setExpiry(taxItemExpiry);
                taxItem.setUnitId(taxItemUnitId);
                taxItem.setUnitCode(taxItemUnitCode);
                taxItem.setUnitNameEn(taxItemUnitNameEn);
                taxItem.setUnitNameKh(taxItemUnitNameKh);
                taxItem.setUnitQuantity(taxItemUnitQuantity);
                taxItem.setQuantity(taxItemQuantity);
                taxItem.setUnitPrice(taxItemUnitPrice);
                taxItem.setBaseUnitPrice(taxItemBaseUnitPrice);
                taxItem.setDiscount(taxItemDiscount);
                taxItem.setItemDiscount(taxItemItemDiscount);
                taxItem.setTaxRateId(taxItemTaxRateId);
                taxItem.setTaxRateName(taxItemTaxRateName);
                taxItem.setTaxRateValue(taxItemTaxRateValue);
                taxItem.setItemTax(taxItemItemTax);
                taxItem.setSubtotal(taxItemSubtotal);
                taxItem.setDescription(taxItemDescription);
                taxItems.add(taxItem);

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

            taxId              = dataTax != null ? dataTax.getId()             : null;
            taxDeclarationId   = taxDeclareId;
            taxTransactionId   = transactionId;
            taxTransaction     = type;
            taxDate            = dataTax != null ? dataTax.getDate()           : sale.getDate();
            taxDueDate         = sale.getDueDate();
            taxReferenceNo     = dataTax != null ? dataTax.getReferenceNo()    : sale.getReferenceNo();
            taxTaxReferenceNo  = dataTax != null ? dataTax.getTaxReferenceNo() : null;
            taxBillerId        = sale.getBillerId();
            taxWarehouseId     = sale.getWarehouseId();
            taxCompanyId       = sale.getCustomerId();
            taxCompanyEn       = dataTax != null ? dataTax.getCompanyEn()      : customer.getCompanyEn();
            taxCompanyKh       = dataTax != null ? dataTax.getCompanyKh()      : customer.getCompanyKh();
            taxNameEn          = dataTax != null ? dataTax.getNameEn()         : customer.getNameEn();
            taxNameKh          = dataTax != null ? dataTax.getNameKh()         : customer.getNameKh();
            taxPhone           = dataTax != null ? dataTax.getPhone()          : customer.getPhone();
            taxEmail           = dataTax != null ? dataTax.getEmail()          : customer.getEmail();
            taxVatNo           = dataTax != null ? dataTax.getVatNo()          : customer.getVatNo();
            taxQuantity        = dataTax != null ? dataTax.getQuantity()       : utility.formatQuantity(saleRepository.getSaleTotalQuantity(sale.getId()));
            taxTotal           = total;
            taxShipping        = shipping;
            taxProductDiscount = product_discount;
            taxOrderDiscountId = discount_id;
            taxOrderDiscount   = discount;
            taxTotalDiscount   = product_discount + discount;
            taxProductTax      = product_tax;
            taxOrderTaxId      = tax_id;
            taxOrderTaxName    = tax_name;
            taxOrderTaxValue   = tax_value;
            taxOrderTax        = tax;
            taxTotalTax        = product_tax + tax;
            taxGrandTotal      = grand_total;
            taxTotalItems      = total_items;
            taxExchangeRate    = dataTax != null ? dataTax.getExchangeRate()   : null;
            taxNote            = sale.getNote();
            taxCreatedBy       = sale.getCreatedBy();
            taxCreatedAt       = sale.getCreatedAt();
            taxUpdatedBy       = sale.getUpdatedBy();
            taxUpdatedAt       = sale.getUpdatedAt();
            taxCreatedByName   = (createdBy != null ? (createdBy.getEmployee() != null ? (createdBy.getEmployee().getFirstName() + " " + createdBy.getEmployee().getLastName()) : createdBy.getUsername()) : "");
            taxUpdatedByName   = (updatedBy != null ? (updatedBy.getEmployee() != null ? (updatedBy.getEmployee().getFirstName() + " " + updatedBy.getEmployee().getLastName()) : updatedBy.getUsername()) : "");
            taxBiller          = biller;
            taxWarehouse       = warehouse;
            taxCompany         = customer;
        }
        TaxDeclareTransaction tax = new TaxDeclareTransaction();
        tax.setId(taxId);
        tax.setTaxDeclarationId(taxDeclarationId);
        tax.setTransactionId(taxTransactionId);
        tax.setTransaction(taxTransaction);
        tax.setDate(taxDate);
        tax.setDueDate(taxDueDate);
        tax.setReferenceNo(taxReferenceNo);
        tax.setTaxReferenceNo(taxTaxReferenceNo);
        tax.setBillerId(taxBillerId);
        tax.setWarehouseId(taxWarehouseId);
        tax.setCompanyId(taxCompanyId);
        tax.setCompanyEn(taxCompanyEn);
        tax.setCompanyKh(taxCompanyKh);
        tax.setNameEn(taxNameEn);
        tax.setNameKh(taxNameKh);
        tax.setPhone(taxPhone);
        tax.setEmail(taxEmail);
        tax.setVatNo(taxVatNo);
        tax.setQuantity(taxQuantity);
        tax.setTotal(taxTotal);
        tax.setShipping(taxShipping);
        tax.setProductDiscount(taxProductDiscount);
        tax.setOrderDiscountId(taxOrderDiscountId);
        tax.setOrderDiscount(taxOrderDiscount);
        tax.setTotalDiscount(taxTotalDiscount);
        tax.setProductTax(taxProductTax);
        tax.setOrderTaxId(taxOrderTaxId);
        tax.setOrderTaxName(taxOrderTaxName);
        tax.setOrderTaxValue(taxOrderTaxValue);
        tax.setOrderTax(taxOrderTax);
        tax.setTotalTax(taxTotalTax);
        tax.setGrandTotal(taxGrandTotal);
        tax.setTotalItems(taxTotalItems);
        tax.setExchangeRate(taxExchangeRate);
        tax.setNote(taxNote);
        tax.setCreatedBy(taxCreatedBy);
        tax.setUpdatedBy(taxUpdatedBy);
        tax.setCreatedAt(taxCreatedAt);
        tax.setUpdatedAt(taxUpdatedAt);
        tax.setCreatedByName(taxCreatedByName);
        tax.setUpdatedByName(taxUpdatedByName);
        tax.setBiller(taxBiller);
        tax.setWarehouse(taxWarehouse);
        tax.setCompany(taxCompany);
        tax.setTransactionItems(taxItems);

        return tax;
    }

    public byte[] taxInvoice(TaxDeclareInvoiceRequest request) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Path path = Paths.get("data", "templates", "fop.xconf").toAbsolutePath();
            File file = path.toFile();
            FopFactory  fopFactory  = FopFactory.newInstance(file);
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            StringBuilder allPageSequences = new StringBuilder();
            for (Long id : request.getIds()) {
                TaxDeclareTransaction tx = getTaxDeclarationByTran(request.getType(), id);
                allPageSequences.append(buildPageSequence(tx));
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

    public String buildPageSequence(TaxDeclareTransaction tx) {
        Font   font             = utility.loadFont(System.getProperty("user.dir") + "/data/fonts/KhmerOSbattambang.ttf", 10);
        String companyNameKh    = (String) ((utility.getPropertyObjectValue(tx.getCompany(), "companyKh") != null && !utility.getPropertyObjectValue(tx.getCompany(), "companyKh").toString().trim().isEmpty()) ? utility.getPropertyObjectValue(tx.getCompany(), "companyKh") : utility.getPropertyObjectValue(tx.getCompany(), "nameKh"));
        String companyNameEn    = (String) ((utility.getPropertyObjectValue(tx.getCompany(), "companyEn") != null && !utility.getPropertyObjectValue(tx.getCompany(), "companyEn").toString().trim().isEmpty()) ? utility.getPropertyObjectValue(tx.getCompany(), "companyEn") : utility.getPropertyObjectValue(tx.getCompany(), "nameEn"));
        String xmlItemsTbl      = "";
        String xmlFooterTbl     = "";
        String xmlFooterEtyTbl  = "";

        int rows_spanned = 4;
        if (tx.getOrderDiscount() > 0) {
            rows_spanned++;
        }
        if (tx.getShipping() > 0) {
            rows_spanned++;
        }
        xmlFooterTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell 
                        number-rows-spanned="   
                          """ + rows_spanned + """ 
                        " 
                        number-columns-spanned="3" padding-left="0px" padding-right="5px" padding-top="15px" padding-bottom="5px" text-align="left">
                        <fo:block font-family="KhmerOSbattambang">
                          """ + tx.getBiller().getDescription() + """
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">តម្លៃសរុប</fo:block>
                        <fo:block>Sub Total</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                          """ + utility.formatMoney(tx.getTotal(), "USD") + """
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        if (tx.getOrderDiscount() > 0) {
            xmlFooterTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">
                          """ + "បញ្ចុះតម្លៃ (" + tx.getOrderDiscountId() + ")" + """
                        </fo:block>
                        <fo:block>
                        """ + "Discount (" + tx.getOrderDiscountId() + ")" + """
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                          """ + utility.formatMoney(tx.getOrderDiscount(), "USD") + """
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        }
        xmlFooterTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">
                          """ + "អាករលើតម្លៃបន្ថែម (" + utility.noZeroDecimal(tx.getOrderTaxValue()) + "%)" + """
                        </fo:block>
                        <fo:block>
                        """ + tx.getOrderTaxName() + """
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                          """ + utility.formatMoney(tx.getOrderTax(), "USD") + """
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        if (tx.getShipping() > 0) {
            xmlFooterTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">ថ្លៃដឹកជញ្ជូន</fo:block>
                        <fo:block>Shipping</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                          """ + utility.formatMoney(tx.getShipping(), "USD") + """
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        }
        xmlFooterTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">សរុបរួមជាប្រាក់ដុល្លា</fo:block>
                        <fo:block>Grand Total in USD</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                          """ + utility.formatMoney(tx.getGrandTotal(), "USD") + """
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        xmlFooterTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">សរុបរួមជាប្រាក់រៀល</fo:block>
                        <fo:block>Grand Total in Riel</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">
                          """ + utility.formatMoney((tx.getGrandTotal() * (tx.getExchangeRate() != null ? tx.getExchangeRate() : 1)), "KHR", 0) + """
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;

        xmlFooterEtyTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell 
                        number-rows-spanned="   
                          """ + rows_spanned + """ 
                        " 
                        number-columns-spanned="3" padding-left="0px" padding-right="5px" padding-top="15px" padding-bottom="5px" text-align="left">
                        <fo:block font-family="KhmerOSbattambang">
                          """ + tx.getBiller().getDescription() + """
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">តម្លៃសរុប</fo:block>
                        <fo:block>Sub Total</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        if (tx.getOrderDiscount() > 0) {
            xmlFooterEtyTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">
                          """ + "បញ្ចុះតម្លៃ" + """
                        </fo:block>
                        <fo:block>
                        """ + "Discount" + """
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        }
        xmlFooterEtyTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">
                          """ + "អាករលើតម្លៃបន្ថែម" + """
                        </fo:block>
                        <fo:block>
                          VAT
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        if (tx.getShipping() > 0) {
            xmlFooterEtyTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">ថ្លៃដឹកជញ្ជូន</fo:block>
                        <fo:block>Shipping</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        }
        xmlFooterEtyTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">សរុបរួមជាប្រាក់ដុល្លា</fo:block>
                        <fo:block>Grand Total in USD</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block>
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;
        xmlFooterEtyTbl += """
                    <fo:table-row line-height="14pt">
                      <fo:table-cell display-align="center" border="1px solid black" number-columns-spanned="2" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">សរុបរួមជាប្រាក់រៀល</fo:block>
                        <fo:block>Grand Total in Riel</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="5px" padding-bottom="1px" text-align="right">
                        <fo:block font-family="KhmerOSbattambang">
                        </fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                """;

        int total_rows = tx.getTransactionItems().size();
        total_rows = (total_rows % 5 != 0 ? (total_rows + (5 - (total_rows % 5))) : total_rows);
        if (!tx.getTransactionItems().isEmpty()) {
            for(int i = 0; i < total_rows; i++) {
                if (i < tx.getTransactionItems().size()) {
                    TaxDeclareTransactionItem txi = tx.getTransactionItems().get(i);
                    int txtWidth = utility.getTextWidth(txi.getProductNameEn(), font);
                    xmlItemsTbl += """
                        <fo:table-row>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="center">
                            <fo:block>
                              """ + (i +1) + """
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="left">
                            <fo:block-container wrap-option="no-wrap" overflow="hidden">
                              <fo:block>
                                """ + txi.getProductNameEn() + """
                              </fo:block>
                            </fo:block-container>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="center">
                            <fo:block>
                              """ + utility.formatQuantity(txi.getUnitQuantity()) + """
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="center">
                            <fo:block>
                              """ + txi.getUnitNameEn() + """
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="right">
                            <fo:block>
                              """ + utility.formatMoney(txi.getUnitPrice(), "USD") + """
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="right">
                            <fo:block>
                              """ + utility.formatMoney(txi.getSubtotal(), "USD") + """
                            </fo:block>
                          </fo:table-cell>
                        </fo:table-row>
                    """;
                } else {
                    xmlItemsTbl += """
                        <fo:table-row>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="center">
                            <fo:block>
                              """ + (i +1) + """
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="left">
                            <fo:block>
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="center">
                            <fo:block>
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="center">
                            <fo:block>
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="right">
                            <fo:block>
                            </fo:block>
                          </fo:table-cell>
                          <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="3.5px" padding-bottom="3.5px" text-align="right">
                            <fo:block>
                            </fo:block>
                          </fo:table-cell>
                        </fo:table-row>
                    """;
                }
                if ((i +1) % 5 == 0) {
                    if ((i +1) == total_rows) {
                        xmlItemsTbl += xmlFooterTbl;
                    } else {
                        xmlItemsTbl += xmlFooterEtyTbl;
                        xmlItemsTbl += "<fo:table-row break-after=\"page\"><fo:table-cell><fo:block></fo:block></fo:table-cell></fo:table-row>";
                    }
                }
            }
        } else {
            xmlItemsTbl = "<fo:table-row><fo:table-cell number-columns-spanned=\"6\" border=\"1px solid black\" padding=\"5px\" text-align=\"center\"><fo:block>No data available</fo:block></fo:table-cell></fo:table-row>";
        }
        return """
            <fo:page-sequence master-reference="A4">
              <fo:static-content flow-name="xsl-region-before" font-family="KhmerOSbattambang, sans-serif" font-size="10pt" line-height="16pt">
                <fo:table table-layout="fixed" width="100%">
                  <fo:table-column column-width="51%"/>
                  <fo:table-column column-width="49%"/>
                  <fo:table-body>
                    <fo:table-row>
                      <fo:table-cell>
                        <fo:block font-size="16pt" font-weight="bold" text-align="left" space-after="8pt" font-family="KhmerOSmuollight">
                          """ + tx.getBiller().getCompanyKh() + """
                        </fo:block>
                        <fo:block font-size="16pt" font-weight="bold" text-align="left" space-after="8pt" font-family="HelveticaWorld">
                          """ + tx.getBiller().getCompanyEn().toUpperCase() + """
                        </fo:block>
                      </fo:table-cell>
                      <fo:table-cell>
                        <fo:block font-size="14pt" font-weight="bold" text-align="center" space-after="8pt" font-family="KhmerOSmuollight">វិក្កយបត្រអាករ</fo:block>
                        <fo:block font-size="14pt" font-weight="bold" text-align="center" space-after="8pt" font-family="HelveticaWorld">TAX INVOICE</fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                    <fo:table-row><fo:table-cell><fo:block padding-bottom="15px"></fo:block></fo:table-cell></fo:table-row>
                    <fo:table-row line-height="16pt">
                      <fo:table-cell>
                        <fo:table table-layout="fixed" width="100%" margin-top="16px">
                          <fo:table-column column-width="27%"/>
                          <fo:table-column column-width="2%"/>
                          <fo:table-column column-width="65%"/>
                          <fo:table-body>
                            <fo:table-row>
                              <fo:table-cell number-columns-spanned="3">
                                <fo:block>
                                  <fo:inline font-family="KhmerOSbattambang" font-weight="bold">លេខអត្តសញ្ញាណកម្ម អតប </fo:inline>
                                  <fo:inline font-family="HelveticaWorld" font-weight="bold">(VAT-TIN)&#xA0;:&#xA0;""" + tx.getBiller().getVatNo().toUpperCase() + """
                                  </fo:inline>
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row> 
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">អាសយដ្ឋាន</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">
                                  """ + tx.getBiller().getAddressKh() + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block>Address</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + tx.getBiller().getAddressEn() + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">លេខទូរស័ព្ទ / Tel</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + tx.getBiller().getPhone() + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell number-columns-spanned="3">
                                <fo:block font-family="KhmerOSbattambang">
                                  អ្នកលក់ / Sales Person :  
                                  """ + tx.getCreatedByName() + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                          </fo:table-body>
                        </fo:table>
                      </fo:table-cell>
                      <fo:table-cell>
                        <fo:table table-layout="fixed" width="100%">
                          <fo:table-column column-width="60%"/>
                          <fo:table-column column-width="2%"/>
                          <fo:table-column column-width="35%"/>
                          <fo:table-body>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">លេខវិក្កយបត្រ / Invoice Nº</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + tx.getTaxReferenceNo() + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">កាលបរិច្ឆេទ / Issue Date</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + tx.getDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">ថ្ងៃផុតកំណត់ / Due Date</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + (tx.getDueDate() != null ? tx.getDueDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "") + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">អត្រាប្តូរប្រាក់ / Exchange Rate</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">
                                  """ + utility.formatMoney(tx.getExchangeRate(), "KHR", 0) + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                          </fo:table-body>
                        </fo:table>
                        <fo:block border-top="1pt solid black" margin-top="6pt" margin-bottom="15pt"/>
                        <fo:table table-layout="fixed" width="100%">
                          <fo:table-column column-width="35%"/>
                          <fo:table-column column-width="2%"/>
                          <fo:table-column column-width="60%"/>
                          <fo:table-body>
                            <fo:table-row line-height="10pt">
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang" font-weight="bold">ឈ្មោះអតិថិជន</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang" font-weight="bold">:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang" font-weight="bold">
                                  """ + companyNameKh + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block>Customer Name</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + companyNameEn + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">លេខទូរស័ព្ទ / Tel</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + utility.getPropertyObjectValue(tx.getCompany(), "phone") + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">អាសយដ្ឋាន</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block font-family="KhmerOSbattambang">
                                  """ + utility.getPropertyObjectValue(tx.getCompany(), "addressKh") + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell>
                                <fo:block>Address</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>:</fo:block>
                              </fo:table-cell>
                              <fo:table-cell>
                                <fo:block>
                                  """ + utility.getPropertyObjectValue(tx.getCompany(), "addressEn") + """
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                              <fo:table-cell number-columns-spanned="3">
                                <fo:block>
                                  <fo:inline font-family="KhmerOSbattambang" font-weight="bold">លេខអត្តសញ្ញាណកម្ម អតប </fo:inline>
                                  <fo:inline font-family="HelveticaWorld" font-weight="bold">(VAT-TIN)&#xA0;:&#xA0;""" + utility.getPropertyObjectValue(tx.getCompany(), "vatNo") + """
                                  </fo:inline>
                                </fo:block>
                              </fo:table-cell>
                            </fo:table-row>  
                          </fo:table-body>
                        </fo:table>
                      </fo:table-cell>
                    </fo:table-row>
                  </fo:table-body>
                </fo:table>
              </fo:static-content>
            
              <fo:static-content flow-name="xsl-region-after" font-family="KhmerOSbattambang, sans-serif" font-size="10pt" line-height="16pt">
                <fo:block space-before="1cm">
                  <fo:table table-layout="fixed" width="100%">
                    <fo:table-column column-width="60%"/>
                    <fo:table-column column-width="40%"/>
                    <fo:table-body>
                      <fo:table-row>
                        <fo:table-cell>
                          <fo:block>ហត្ថលេខាអ្នកទិញ</fo:block>
                          <fo:block>Buyer's Signature:</fo:block>
                          <fo:block margin-top="38px">_______________________________</fo:block>
                          <fo:block>ឈ្មោះ / Name: </fo:block>
                          <fo:block>តួនាទី / Title: </fo:block>
                          <fo:block>កាលបរិច្ឆេទ / Date: </fo:block>
                        </fo:table-cell>
                        <fo:table-cell>
                          <fo:block>ហត្ថលេខាអ្នកលក់</fo:block>
                          <fo:block>Seller's Signature:</fo:block>
                          <fo:block margin-top="38px">_______________________________</fo:block>
                          <fo:block>ឈ្មោះ / Name: </fo:block>
                          <fo:block>តួនាទី / Title: </fo:block>
                          <fo:block>កាលបរិច្ឆេទ / Date: </fo:block>
                        </fo:table-cell>
                      </fo:table-row>
                    </fo:table-body>
                  </fo:table>
                  <fo:block font-size="8.2pt" text-align="left" space-before="10pt">
                    សម្គាល់៖ ច្បាប់ដើមសម្រាប់អ្នកទិញ និងច្បាប់ចម្លងសម្រាប់អ្នកលក់
                  </fo:block>
                  <fo:block font-size="8.2pt" text-align="left">
                    Note: Original Invoice for Customer and Copied for Seller
                  </fo:block>
                </fo:block>
              </fo:static-content>
       
              <fo:flow flow-name="xsl-region-body" font-family="KhmerOSbattambang, sans-serif" font-size="10pt">
                <fo:table table-layout="fixed" width="100%">
                  <fo:table-column column-width="5%" />
                  <fo:table-column column-width="44%"/>
                  <fo:table-column column-width="11%"/>
                  <fo:table-column column-width="11%"/>
                  <fo:table-column column-width="12%"/>
                  <fo:table-column column-width="17%"/>
                  <fo:table-header>
                    <fo:table-row background-color="silver">
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="8px" padding-bottom="8px" line-height="8px" text-align="center" font-weight="bold">
                        <fo:block font-family="KhmerOSbattambang">ល.រ</fo:block>
                        <fo:block>Nº</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="8px" padding-bottom="8px" line-height="8px" text-align="center" font-weight="bold">
                        <fo:block font-family="KhmerOSbattambang">បរិយាយមុខទំនិញ</fo:block>
                        <fo:block>Description of Product</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="8px" padding-bottom="8px" line-height="8px" text-align="center" font-weight="bold">
                        <fo:block font-family="KhmerOSbattambang">បរិមាណ</fo:block>
                        <fo:block>Quantity</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="8px" padding-bottom="8px" line-height="8px" text-align="center" font-weight="bold">
                        <fo:block font-family="KhmerOSbattambang">ឯកតា</fo:block>
                        <fo:block>UOM</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="8px" padding-bottom="8px" line-height="8px" text-align="center" font-weight="bold">
                        <fo:block font-family="KhmerOSbattambang">តម្លៃឯកតា</fo:block>
                        <fo:block>Unit Price</fo:block>
                      </fo:table-cell>
                      <fo:table-cell display-align="center" border="1px solid black" padding="5px" padding-top="8px" padding-bottom="8px" line-height="8px" text-align="center" font-weight="bold">
                        <fo:block font-family="KhmerOSbattambang">តម្លៃសរុប</fo:block>
                        <fo:block>Amount</fo:block>
                      </fo:table-cell>
                    </fo:table-row>
                  </fo:table-header>
                  <fo:table-body>
                    """ + xmlItemsTbl + """
                  </fo:table-body>
                </fo:table>
              </fo:flow>
            </fo:page-sequence>
        """;
    }
}
