package kh.com.csx.posapi.controller.expense;

import jakarta.validation.Valid;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.expense.*;
import kh.com.csx.posapi.dto.payment.*;
import kh.com.csx.posapi.dto.setting.OrderRefRequest;
import kh.com.csx.posapi.dto.setting.OrderRefResponse;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.service.ExpenseService;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PaymentService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expense")
@RequiredArgsConstructor
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private final PaymentService paymentService;

    @Autowired
    private final Utility utility;

    @GetMapping("/sequenceNo")
    public BaseResponse sequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), Constant.ReferenceKey.EX)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-EXPENSES-RETRIEVE')")
    public BaseResponse getExpense(ExpenseRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Expense ID is required.", HttpStatus.BAD_REQUEST);
        }
        ExpenseResponse expenseResponse = expenseService.getExpenseById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(expenseResponse);
        baseResponse.setMessage("Expense retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-EXPENSES-RETRIEVE')")
    public BaseResponse getAllExpenses(ExpenseRetrieveRequest filter) {
        Page<ExpenseResponse> expenses = expenseService.getAllExpenses(filter);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(expenses);
        baseResponse.setMessage("Expenses retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-EXPENSES-CREATE')")
    public BaseResponse createExpense(@Valid @RequestBody ExpenseCreateRequest request) {
        ExpenseResponse expenseResponse = expenseService.createExpense(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(expenseResponse);
        baseResponse.setMessage("Expense created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-EXPENSES-UPDATE')")
    public BaseResponse updateExpense(@Valid @RequestBody ExpenseUpdateRequest request) {
        ExpenseResponse expenseResponse = expenseService.updateExpense(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(expenseResponse);
        baseResponse.setMessage("Expense updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-EXPENSES-DELETE')")
    public BaseResponse deleteExpense(@Valid @RequestBody ExpenseDeleteRequest request) {
        return expenseService.deleteExpense(request);
    }

    @GetMapping("/retrievePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-PAYMENTS-RETRIEVE')")
    public BaseResponse retrievePayment(PaymentRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Payment ID is required.", HttpStatus.BAD_REQUEST);
        }
        PaymentResponse paymentResponse = paymentService.getPaymentById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payment retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrievePaymentsByExpenseId")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-PAYMENTS-RETRIEVE')")
    public BaseResponse retrievePaymentsByExpenseId(PaymentRetrieveRequest request) {
        if (request.getExpenseId() == null) {
            throw new ApiException("Expense ID is required.", HttpStatus.BAD_REQUEST);
        }
        List<PaymentResponse> paymentResponse = paymentService.getExpenseId(request.getExpenseId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payments retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/createPayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-PAYMENTS-CREATE')")
    public BaseResponse createPayment(@Valid @RequestBody List<PaymentCreateRequest> request) {
        List<PaymentResponse> paymentResponses = paymentService.createPayment(Constant.PaymentTransactionType.EXPENSE, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponses);
        baseResponse.setMessage("Payment created successfully.");
        return baseResponse;
    }

    @PostMapping("/updatePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-PAYMENTS-UPDATE')")
    public BaseResponse updatePayment(@Valid @RequestBody PaymentUpdateRequest request) {
        PaymentResponse paymentResponse = paymentService.updatePayment(Constant.PaymentTransactionType.EXPENSE, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payment updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/deletePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-PAYMENTS-DELETE')")
    public BaseResponse deletePayment(@Valid @RequestBody PaymentDeleteRequest request) {
        paymentService.deletePayment(Constant.PaymentTransactionType.EXPENSE, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Payment deleted successfully.");
        return baseResponse;
    }
}


