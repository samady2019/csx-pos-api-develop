package kh.com.csx.posapi.controller.payment;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.payment.*;
import kh.com.csx.posapi.dto.setting.OrderRefRequest;
import kh.com.csx.posapi.dto.setting.OrderRefResponse;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PaymentService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final Utility utility;

    @GetMapping("/sequenceNo")
    public BaseResponse sequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), Constant.ReferenceKey.PAY)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-RETRIEVE') or hasAuthority('SALE-PAYMENTS-RETRIEVE') or hasAuthority('EXPENSE-PAYMENTS-RETRIEVE')")
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

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-RETRIEVE') or hasAuthority('SALE-PAYMENTS-RETRIEVE') or hasAuthority('EXPENSE-PAYMENTS-RETRIEVE')")
    public BaseResponse retrieveAllPayments(PaymentRetrieveRequest filter) {
        Page<PaymentResponse> paymentsResponse = paymentService.getAllPayments(filter);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentsResponse);
        baseResponse.setMessage("Payments retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/listAll")
    public BaseResponse listAllPayments() {
        List<PaymentResponse> paymentsResponse = paymentService.listAllPayments();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentsResponse);
        baseResponse.setMessage("Payments retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveDepositBalance")
    public BaseResponse retrieveDepositBalance(PaymentRetrieveRequest request) {
        if (request.getPurchaseOrderId() == null && request.getPurchaseId() == null) {
            throw new ApiException("Purchase order ID or purchase ID is required.", HttpStatus.BAD_REQUEST);
        }
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentService.retrieveDepositBalance(request.getPurchaseOrderId(), request.getPurchaseId()));
        baseResponse.setMessage("Deposit balance retrieved successfully.");
        return baseResponse;
    }

}
