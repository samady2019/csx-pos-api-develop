package kh.com.csx.posapi.service;

import jakarta.persistence.EntityManager;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.expense.*;
import kh.com.csx.posapi.dto.payment.PaymentUpdateRequest;
import kh.com.csx.posapi.dto.payment.PaymentCreateRequest;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ExpenseService {

    @Autowired
    private EntityManager entityManager;

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final BillerRepository billerRepository;
    private final Utility utility;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;

    public ExpenseResponse getExpenseById(Long id) {
        try {
            ExpenseEntity expense = expenseRepository.findById(id).orElseThrow(() -> new ApiException("Expense not found.", HttpStatus.BAD_REQUEST));
            utility.verifyAccess(expense);
            return new ExpenseResponse(expense);
        }catch (Exception e){
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<ExpenseResponse> getAllExpenses(ExpenseRetrieveRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()){
                request.setSortBy("referenceNo, date");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()){
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
            Page<ExpenseEntity> expenseEntities = expenseRepository.findAllByFilter(request, pageable);
            return expenseEntities.map(ExpenseResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseCreateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();

            ExpenseCategoryEntity expenseCategory = expenseCategoryRepository.findById(request.getExpenseCategoryId()).orElseThrow(() -> new ApiException("Expense category not found.", HttpStatus.BAD_REQUEST));
            BillerEntity biller = billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
            String referenceNo  = utility.checkReferenceNo(request.getBillerId(), Constant.ReferenceKey.EX, request.getReferenceNo().trim());

            ExpenseEntity expense = new ExpenseEntity();
            expense.setDate(request.getDate());
            expense.setReferenceNo(referenceNo);
            expense.setBillerId(request.getBillerId());
            expense.setExpenseCategoryId(request.getExpenseCategoryId());
            expense.setAmount(utility.formatDecimal(request.getAmount()));
            expense.setPaid(0.0);
            expense.setCurrencies(request.getCurrencies());
            expense.setAttachment(request.getAttachment());
            expense.setNote(request.getNote());
            expense.setCreatedBy(userEntity.getUserId());
            expense.setCreatedAt(LocalDateTime.now());
            expense.setBiller(biller);
            expense.setExpenseCategory(expenseCategory);
            expense.setPaymentStatus(Constant.PaymentStatus.DEFAULT);
            expenseRepository.save(expense);
            utility.updateReferenceNo(request.getBillerId(), Constant.ReferenceKey.EX, referenceNo);
            entityManager.flush();
            entityManager.clear();

            List<PaymentEntity> payments = new ArrayList<>();
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
                String paymentReferenceNo = utility.checkReferenceNo(request.getBillerId(), Constant.ReferenceKey.PAY, request.getPayments().get(0).getReferenceNo().trim());
                for (PaymentCreateRequest paymentRequest : request.getPayments()) {
                    PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(paymentRequest.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
                    double paidAmount = utility.formatDecimal(utility.calculateTotalBaseCurrency(paymentRequest.getCurrencies()));
                    if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
                        throw new ApiException("Payment method type '" + Constant.PaymentMethodType.DEPOSIT + "' is not allowed for this transaction.", HttpStatus.BAD_REQUEST);
                    }
                    if (paidAmount <= 0) {
                        throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
                    }
                    PaymentEntity payment = new PaymentEntity();
                    payment.setExpenseId(expense.getId());
                    payment.setDate(expense.getDate());
                    payment.setReferenceNo(paymentReferenceNo);
                    payment.setPaymentMethodId(paymentRequest.getPaymentMethodId());
                    payment.setType(Constant.PaymentTransactionStatus.SENT);
                    payment.setAmount(paidAmount);
                    payment.setCurrencies(paymentRequest.getCurrencies());
                    payment.setCreatedBy(userEntity.getUserId());
                    payment.setCreatedAt(LocalDateTime.now());
                    payments.add(payment);
                }
                paymentRepository.saveAll(payments);
                paymentRepository.updatePaymentStatus(null, null, null, null, expense.getId());
                utility.updateReferenceNo(request.getBillerId(), Constant.ReferenceKey.PAY, payments.get(0).getReferenceNo());
                entityManager.flush();
                entityManager.clear();
            }
            return getExpenseById(expense.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ExpenseResponse updateExpense(ExpenseUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();

            ExpenseEntity expense = expenseRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Expense not found.", HttpStatus.BAD_REQUEST));
            utility.verifyAccess(request);
            if (!expense.getPaymentStatus().equals(Constant.PaymentStatus.PENDING)) {
                throw new ApiException("Unable to edit: The expense has already been processed.", HttpStatus.BAD_REQUEST);
            }
            ExpenseCategoryEntity expenseCategory = expenseCategoryRepository.findById(request.getExpenseCategoryId()).orElseThrow(() -> new ApiException("Expense category not found.", HttpStatus.BAD_REQUEST));
            BillerEntity biller = billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
            if (expenseRepository.existsByReferenceNoAndIdNot(request.getReferenceNo(), expense.getId())) {
                throw new ApiException("Reference number already exists.", HttpStatus.BAD_REQUEST);
            }
            String reference_no     = request.getReferenceNo();
            String old_reference_no = expense.getReferenceNo();
            expense.setDate(request.getDate());
            expense.setReferenceNo(request.getReferenceNo());
            expense.setBillerId(request.getBillerId());
            expense.setExpenseCategoryId(request.getExpenseCategoryId());
            expense.setAmount(utility.formatDecimal(request.getAmount()));
            expense.setCurrencies(request.getCurrencies());
            expense.setAttachment(request.getAttachment());
            expense.setNote(request.getNote());
            expense.setUpdatedBy(userEntity.getUserId());
            expense.setUpdatedAt(LocalDateTime.now());
            expense.setBiller(biller);
            expense.setExpenseCategory(expenseCategory);
            expenseRepository.save(expense);
            if (!old_reference_no.equals(reference_no)) {
                utility.updateReferenceNo(request.getBillerId(), Constant.ReferenceKey.EX, reference_no);
            }
            entityManager.flush();
            entityManager.clear();

            List<PaymentEntity> payments = new ArrayList<>();
            if (request.getPayments() != null && !request.getPayments().isEmpty()) {
                String paymentReferenceNo = utility.checkReferenceNo(request.getBillerId(), Constant.ReferenceKey.PAY, request.getPayments().get(0).getReferenceNo().trim());
                for (PaymentUpdateRequest paymentRequest : request.getPayments()) {
                    PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(paymentRequest.getPaymentMethodId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
                    double paidAmount = utility.formatDecimal(utility.calculateTotalBaseCurrency(paymentRequest.getCurrencies()));
                    if (paymentMethod.getType().equals(Constant.PaymentMethodType.DEPOSIT)) {
                        throw new ApiException("Payment method type '" + Constant.PaymentMethodType.DEPOSIT + "' is not allowed for this transaction.", HttpStatus.BAD_REQUEST);
                    }
                    if (paidAmount <= 0) {
                        throw new ApiException("Payment amount should be greater than zero.", HttpStatus.BAD_REQUEST);
                    }
                    PaymentEntity payment = new PaymentEntity();
                    payment.setExpenseId(expense.getId());
                    payment.setDate(expense.getDate());
                    payment.setReferenceNo(paymentReferenceNo);
                    payment.setPaymentMethodId(paymentRequest.getPaymentMethodId());
                    payment.setType(Constant.PaymentTransactionStatus.SENT);
                    payment.setAmount(paidAmount);
                    payment.setCurrencies(paymentRequest.getCurrencies());
                    payment.setCreatedBy(userEntity.getUserId());
                    payment.setCreatedAt(LocalDateTime.now());
                    payments.add(payment);
                }
                paymentRepository.saveAll(payments);
                paymentRepository.updatePaymentStatus(null, null, null, null, expense.getId());
                utility.updateReferenceNo(request.getBillerId(), Constant.ReferenceKey.PAY, payments.get(0).getReferenceNo());
                entityManager.flush();
                entityManager.clear();
            }
            return getExpenseById(expense.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteExpense(ExpenseDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteExpense(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteExpense(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Expense deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteExpense(Long id) {
        try {
            ExpenseEntity expense = expenseRepository.findById(id).orElseThrow(() -> new ApiException("Expense not found.", HttpStatus.BAD_REQUEST));
            utility.verifyAccess(expense);
            if (!expense.getPaymentStatus().equals(Constant.PaymentStatus.PENDING)) {
                throw new ApiException("Unable to delete expense reference no. '" + expense.getReferenceNo() + "'. The expense has already been processed.", HttpStatus.BAD_REQUEST);
            }
            expenseRepository.deleteById(id);
            return new BaseResponse("Expense reference no. '" + expense.getReferenceNo() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
