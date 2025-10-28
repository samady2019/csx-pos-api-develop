package kh.com.csx.posapi.service;

import jakarta.persistence.EntityManager;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.user.BillerWarehouseRequest;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.dto.payment.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    @Autowired
    private EntityManager entityManager;

    private final PaymentRepository       paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRepository      purchaseRepository;
    private final ExpenseRepository       expenseRepository;

    private final Utility                 utility;

    public PaymentResponse getPaymentById(Long id) {
        PaymentEntity paymentEntity = paymentRepository.findById(id).orElseThrow(() -> new ApiException("Payment not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(paymentEntity);
        return PaymentResponse.builder().payment(paymentEntity).build();
    }

    public List<PaymentResponse> getPaymentsByPurchaseOrderId(Long id) {
        List<PaymentEntity> paymentEntities = paymentRepository.findByPurchaseOrderId(id);
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            paymentResponses.add(PaymentResponse.builder().payment(paymentEntity).build());
        }
        return paymentResponses;
    }

    public List<PaymentResponse> getPaymentsByPurchaseId(Long id) {
        List<PaymentEntity> paymentEntities = paymentRepository.findByPurchaseId(id);
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            paymentResponses.add(PaymentResponse.builder().payment(paymentEntity).build());
        }
        return paymentResponses;
    }

    public List<PaymentResponse> getExpenseId(Long id) {
        List<PaymentEntity> paymentEntities = paymentRepository.findByExpenseId(id);
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            paymentResponses.add(PaymentResponse.builder().payment(paymentEntity).build());
        }
        return paymentResponses;
    }

    public List<PaymentResponse> getPaymentsBySaleOrderId(Long id) {
        List<PaymentEntity> paymentEntities = paymentRepository.findBySaleOrderId(id);
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            paymentResponses.add(PaymentResponse.builder().payment(paymentEntity).build());
        }
        return paymentResponses;
    }

    public List<PaymentResponse> getPaymentsBySaleId(Long id) {
        List<PaymentEntity> paymentEntities = paymentRepository.findBySaleId(id);
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            paymentResponses.add(PaymentResponse.builder().payment(paymentEntity).build());
        }
        return paymentResponses;
    }

    public Page<PaymentResponse> getAllPayments(PaymentRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()){
            request.setSortBy("date, referenceNo");
        }
        if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()){
            request.setOrderBy(Constant.OrderBy.DESC);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
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
        Page<PaymentEntity> paymentEntities = paymentRepository.findAllByFilter(request, pageable);
        List<PaymentResponse> paymentResponses = new ArrayList<>();
        for (PaymentEntity paymentEntity : paymentEntities) {
            paymentResponses.add(PaymentResponse.builder().payment(paymentEntity).build());
        }
        return paymentEntities.map(paymentEntity -> PaymentResponse.builder().payment(paymentEntity).build());
    }

    public List<PaymentResponse> listAllPayments() {
        try {
            List<PaymentEntity> paymentEntities = paymentRepository.findAll();
            return paymentEntities.stream().map(paymentEntity -> PaymentResponse.builder().payment(paymentEntity).build()).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Error retrieving all payments: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public List<PaymentResponse> createPayment(String tran, List<PaymentCreateRequest> requests) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (tran == null || tran.trim().isEmpty()) {
            throw new ApiException("Payment transaction type is required.", HttpStatus.BAD_REQUEST);
        }
        Long id                = null;
        Long purchase_order_id = null;
        Long purchase_id       = null;
        Long sale_order_id     = null;
        Long sale_id           = null;
        Long expense_id        = null;
        Long biller_id         = null;
        String tran_status     = null;
        if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE_ORDER)) {
            if (requests.get(0).getPurchaseOrderId() == null) {
                throw new ApiException("Purchase order ID is required.", HttpStatus.BAD_REQUEST);
            }
            boolean hasDifferent = requests.stream().map(PaymentCreateRequest::getPurchaseOrderId).distinct().count() > 1;
            if (hasDifferent) {
                throw new ApiException("Purchase order ID can't be different.", HttpStatus.BAD_REQUEST);
            }
            PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(requests.get(0).getPurchaseOrderId()).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
            if (purchaseOrder.getPaymentStatus().equals(Constant.PaymentStatus.COMPLETED)) {
                throw new ApiException("Unable to create payment: The purchase order '" + purchaseOrder.getReferenceNo() + "' has already been paid.", HttpStatus.BAD_REQUEST);
            }
            id          = purchase_order_id = purchaseOrder.getId();
            biller_id   = purchaseOrder.getBillerId();
            tran_status = Constant.PaymentTransactionStatus.SENT;
        } else if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE)) {
            if (requests.get(0).getPurchaseId() == null) {
                throw new ApiException("Purchase ID is required.", HttpStatus.BAD_REQUEST);
            }
            boolean hasDifferent = requests.stream().map(PaymentCreateRequest::getPurchaseId).distinct().count() > 1;
            if (hasDifferent) {
                throw new ApiException("Purchase ID can't be different.", HttpStatus.BAD_REQUEST);
            }
            PurchaseEntity purchase = purchaseRepository.findById(requests.get(0).getPurchaseId()).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
            if (purchase.getPaymentStatus().equals(Constant.PaymentStatus.COMPLETED)) {
                throw new ApiException("Unable to create payment: The purchase '" + purchase.getReferenceNo() + "' has already been paid.", HttpStatus.BAD_REQUEST);
            }
            id          = purchase_id = purchase.getId();
            biller_id   = purchase.getBillerId();
            tran_status = (!purchase.getStatus().equals(Constant.PurchaseStatus.RETURNED) ? Constant.PaymentTransactionStatus.SENT : Constant.PaymentTransactionStatus.RETURNED);
        } else if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE_RETURN)) {
            if (requests.get(0).getPurchaseReturnId() == null) {
                throw new ApiException("Purchase return ID is required.", HttpStatus.BAD_REQUEST);
            }
            tran_status = Constant.PaymentTransactionStatus.RETURNED;
        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE_ORDER)) {
            if (requests.get(0).getSaleOrderId() == null) {
                throw new ApiException("Sale order ID is required.", HttpStatus.BAD_REQUEST);
            }
            tran_status = Constant.PaymentTransactionStatus.RECEIVED;
        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE)) {
            if (requests.get(0).getSaleId() == null) {
                throw new ApiException("Sale ID is required.", HttpStatus.BAD_REQUEST);
            }
            tran_status = Constant.PaymentTransactionStatus.RECEIVED;
        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE_RETURN)) {
            if (requests.get(0).getSaleReturnId() == null) {
                throw new ApiException("Sale return ID is required.", HttpStatus.BAD_REQUEST);
            }
            tran_status = Constant.PaymentTransactionStatus.RETURNED;
        } else if (tran.trim().equals(Constant.PaymentTransactionType.EXPENSE)) {
            if (requests.get(0).getExpenseId() == null) {
                throw new ApiException("Expense ID is required.", HttpStatus.BAD_REQUEST);
            }
            boolean hasDifferent = requests.stream().map(PaymentCreateRequest::getExpenseId).distinct().count() > 1;
            if (hasDifferent) {
                throw new ApiException("Expense ID can't be different.", HttpStatus.BAD_REQUEST);
            }
            ExpenseEntity expense = expenseRepository.findById(requests.get(0).getExpenseId()).orElseThrow(() -> new ApiException("Expense not found.", HttpStatus.BAD_REQUEST));
            if (expense.getPaymentStatus().equals(Constant.PaymentStatus.COMPLETED)) {
                throw new ApiException("Unable to create payment: The expense '" + expense.getReferenceNo() + "' has already been paid.", HttpStatus.BAD_REQUEST);
            }
            id          = expense_id = expense.getId();
            biller_id   = expense.getBillerId();
            tran_status = Constant.PaymentTransactionStatus.SENT;
        } else {
            throw new ApiException("Invalid transaction type provided.", HttpStatus.BAD_REQUEST);
        }
        double deposit = 0;
        List<PaymentEntity> payments = new ArrayList<>();
        String reference_no = utility.checkReferenceNo(biller_id, Constant.ReferenceKey.PAY, requests.get(0).getReferenceNo().trim());
        utility.verifyAccess(new BillerWarehouseRequest(biller_id, null));
        for (PaymentCreateRequest request : requests) {
            PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
            String currenciesJson = request.getCurrencies();
            double paid_amount    = utility.formatDecimal(utility.calculateTotalBaseCurrency(currenciesJson));
            if ((!tran_status.equals(Constant.PaymentTransactionStatus.RETURNED) && paid_amount <= 0) || (tran_status.equals(Constant.PaymentTransactionStatus.RETURNED) && paid_amount >= 0)) {
                throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
            }
            PaymentEntity payment = new PaymentEntity();
            payment.setBillerId(biller_id);
            payment.setSaleOrderId(sale_order_id);
            payment.setSaleId(sale_id);
            payment.setPurchaseOrderId(purchase_order_id);
            payment.setPurchaseId(purchase_id);
            payment.setExpenseId(expense_id);
            payment.setDate(request.getDate());
            payment.setReferenceNo(reference_no);
            payment.setPaymentMethodId(request.getPaymentMethodId());
            payment.setAccountNumber(request.getAccountNumber());
            payment.setAccountName(request.getAccountName());
            payment.setBankName(request.getBankName());
            payment.setChequeNo(request.getChequeNo());
            payment.setChequeDate(request.getChequeDate());
            payment.setChequeNumber(request.getChequeNumber());
            payment.setCcNo(request.getCcNo());
            payment.setCcCvv2(request.getCcCvv2());
            payment.setCcHolder(request.getCcHolder());
            payment.setCcMonth(request.getCcMonth());
            payment.setCcYear(request.getCcYear());
            payment.setCcType(request.getCcType());
            payment.setCurrencies(currenciesJson);
            payment.setAmount(paid_amount);
            payment.setNote(request.getNote());
            payment.setAttachment(request.getAttachment());
            payment.setType(tran_status);
            payment.setCreatedBy(user.getUserId());
            payment.setCreatedAt(LocalDateTime.now());
            if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
                deposit += paid_amount;
            }
            payments.add(payment);
        }
        if (deposit != 0) {
            checkDeposit(tran, id, deposit);
        }
        try {
            List<PaymentEntity> savedPayments = paymentRepository.saveAll(payments);
            paymentRepository.updatePaymentStatus(purchase_order_id, purchase_id, sale_order_id, sale_id, expense_id);
            utility.updateReferenceNo(biller_id, Constant.ReferenceKey.PAY, reference_no);
            entityManager.flush();
            entityManager.clear();

            List<PaymentResponse> paymentResponses = new ArrayList<>();
            for (PaymentEntity paymentEntity : savedPayments) {
                paymentResponses.add(PaymentResponse.builder().payment(paymentRepository.findById(paymentEntity.getId()).orElseThrow()).build());
            }
            return paymentResponses;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PaymentResponse updatePayment(String tran, PaymentUpdateRequest request) {
        PaymentEntity payment = paymentRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Payment not found.", HttpStatus.BAD_REQUEST));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        utility.verifyAccess(payment);

        if (tran == null || tran.trim().isEmpty()) {
            throw new ApiException("Payment transaction type is required.", HttpStatus.BAD_REQUEST);
        }
        Long id                = null;
        Long purchase_order_id = null;
        Long purchase_id       = null;
        Long sale_order_id     = null;
        Long sale_id           = null;
        Long expense_id        = null;
        if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE_ORDER)) {
            PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(payment.getPurchaseOrderId()).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
            id = purchase_order_id = purchaseOrder.getId();
        } else if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE)) {
            PurchaseEntity purchase = purchaseRepository.findById(payment.getPurchaseId()).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
            id = purchase_id = purchase.getId();
        } else if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE_RETURN)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE_ORDER)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE_RETURN)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.EXPENSE)) {
            ExpenseEntity expense = expenseRepository.findById(payment.getExpenseId()).orElseThrow(() -> new ApiException("Expense not found.", HttpStatus.BAD_REQUEST));
            id = expense_id = expense.getId();
        } else {
            throw new ApiException("Invalid transaction type provided.", HttpStatus.BAD_REQUEST);
        }
        PaymentMethodEntity oldPaymentMethod = paymentMethodRepository.findById(payment.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
        PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
        String currenciesJson = request.getCurrencies();
        double paid_amount    = utility.formatDecimal(utility.calculateTotalBaseCurrency(currenciesJson));
        if ((!payment.getType().equals(Constant.PaymentTransactionStatus.RETURNED) && paid_amount <= 0) || (payment.getType().equals(Constant.PaymentTransactionStatus.RETURNED) && paid_amount >= 0)) {
            throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
        }
        if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
            double old_amount = oldPaymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT) ? payment.getAmount() : 0;
            checkDeposit(tran, id, paid_amount, old_amount);
        }
        // if (paymentRepository.existsByReferenceNoAndIdNot(request.getReferenceNo(), payment.getId())) {
        //  throw new ApiException("Reference number already exists.", HttpStatus.BAD_REQUEST);
        // }
        payment.setDate(request.getDate());
        payment.setReferenceNo(request.getReferenceNo().trim());
        payment.setPaymentMethodId(request.getPaymentMethodId());
        payment.setAccountNumber(request.getAccountNumber());
        payment.setAccountName(request.getAccountName());
        payment.setBankName(request.getBankName());
        payment.setChequeNo(request.getChequeNo());
        payment.setChequeDate(request.getChequeDate());
        payment.setChequeNumber(request.getChequeNumber());
        payment.setCcNo(request.getCcNo());
        payment.setCcCvv2(request.getCcCvv2());
        payment.setCcHolder(request.getCcHolder());
        payment.setCcMonth(request.getCcMonth());
        payment.setCcYear(request.getCcYear());
        payment.setCcType(request.getCcType());
        payment.setCurrencies(currenciesJson);
        payment.setAmount(paid_amount);
        payment.setNote(request.getNote());
        payment.setAttachment(request.getAttachment());
        payment.setUpdatedBy(user.getUserId());
        payment.setUpdatedAt(LocalDateTime.now());
        try {
            paymentRepository.save(payment);
            paymentRepository.updatePaymentStatus(purchase_order_id, purchase_id, sale_order_id, sale_id, expense_id);
            entityManager.flush();
            entityManager.clear();

            return getPaymentById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void deletePayment(String tran, PaymentDeleteRequest request) {
        PaymentEntity payment = paymentRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Payment not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(payment);

        if (tran == null || tran.trim().isEmpty()) {
            throw new ApiException("Payment transaction type is required.", HttpStatus.BAD_REQUEST);
        }
        Long purchase_order_id = null;
        Long purchase_id       = null;
        Long sale_order_id     = null;
        Long sale_id           = null;
        Long expense_id        = null;
        if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE_ORDER)) {
            PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(payment.getPurchaseOrderId()).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
            purchase_order_id = purchaseOrder.getId();
        } else if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE)) {
            PurchaseEntity purchase = purchaseRepository.findById(payment.getPurchaseId()).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
            purchase_id = purchase.getId();
        } else if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE_RETURN)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE_ORDER)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE_RETURN)) {

        } else if (tran.trim().equals(Constant.PaymentTransactionType.EXPENSE)) {
            ExpenseEntity expense = expenseRepository.findById(payment.getExpenseId()).orElseThrow(() -> new ApiException("Expense not found.", HttpStatus.BAD_REQUEST));
            expense_id = expense.getId();
        } else {
            throw new ApiException("Invalid transaction type provided.", HttpStatus.BAD_REQUEST);
        }
        try {
            paymentRepository.delete(payment);
            paymentRepository.updatePaymentStatus(purchase_order_id, purchase_id, sale_order_id, sale_id, expense_id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Double retrieveDepositBalance(Long purchaseOrderId, Long purchaseId) {
        if (purchaseOrderId != null) {
            purchaseOrderRepository.findById(purchaseOrderId).orElseThrow(() -> new ApiException("Purchase order not found.", HttpStatus.BAD_REQUEST));
            return paymentRepository.getTotalDepositByPurchaseOrderId(purchaseOrderId);
        } else {
            purchaseRepository.findById(purchaseId).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
            return paymentRepository.getTotalDepositByPurchaseId(purchaseId);
        }
    }

    public void checkDeposit(String tran, Long id, double amount, double old_amount) {
        if (tran.trim().equals(Constant.PaymentTransactionType.PURCHASE)) {
            PurchaseEntity purchase = purchaseRepository.findById(id).orElseThrow(() -> new ApiException("Purchase not found.", HttpStatus.BAD_REQUEST));
            if (!purchase.getStatus().equals(Constant.PurchaseStatus.RETURNED)) {
                if (purchase.getPurchaseOrderId() == null) {
                    throw new ApiException("Deposit payment require an associated purchase order.", HttpStatus.BAD_REQUEST);
                }  else if (amount > (paymentRepository.getTotalDepositByPurchaseOrderId(purchase.getPurchaseOrderId()) + old_amount)) {
                    throw new ApiException("The payment cannot be over the available deposit amount.", HttpStatus.BAD_REQUEST);
                }
            } else {
                if (purchase.getPurchaseId() == null) {
                    throw new ApiException("Deposit payment require an associated purchase.", HttpStatus.BAD_REQUEST);
                } else if (amount < ((-1 * paymentRepository.getTotalDepositByPurchaseId(purchase.getPurchaseId())) + old_amount)) {
                    throw new ApiException("The payment cannot be over the available deposit amount.", HttpStatus.BAD_REQUEST);
                }
            }
        } else if (tran.trim().equals(Constant.PaymentTransactionType.SALE)) {

        } else {
            throw new ApiException("Payment method type '" + Constant.PaymentMethodType.DEPOSIT + "' is not allowed for this transaction.", HttpStatus.BAD_REQUEST);
        }
    }

    public void checkDeposit(String tran, Long id, double amount) {
        checkDeposit(tran, id, amount, 0.0);
    }
}
