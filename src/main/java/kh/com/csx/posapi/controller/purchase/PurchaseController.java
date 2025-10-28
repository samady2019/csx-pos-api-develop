package kh.com.csx.posapi.controller.purchase;

import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.PaymentTransactionType;
import kh.com.csx.posapi.dto.payment.*;
import kh.com.csx.posapi.dto.purchase.*;
import kh.com.csx.posapi.dto.setting.OrderRefRequest;
import kh.com.csx.posapi.dto.setting.OrderRefResponse;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PaymentService;
import kh.com.csx.posapi.service.PurchaseService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;
    private final PaymentService paymentService;
    private final Utility utility;

    @GetMapping("/sequenceNo")
    public BaseResponse sequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), Constant.ReferenceKey.P)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/return-sequenceNo")
    public BaseResponse returnSequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), Constant.ReferenceKey.PR)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES-RETRIEVE')")
    public BaseResponse retrievePurchase(PurchaseRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Purchase ID is required.", HttpStatus.BAD_REQUEST);
        }
        PurchaseResponse purchaseResponse = purchaseService.getPurchaseById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseResponse);
        baseResponse.setMessage("Purchase retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES-RETRIEVE')")
    public BaseResponse retrieveAllPurchases(PurchaseRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseService.getAllPurchases(request));
        baseResponse.setMessage("Purchases retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES-CREATE')")
    public BaseResponse createPurchase(@Valid @RequestBody PurchaseCreateRequest request) {
        PurchaseResponse purchaseResponse = purchaseService.createPurchase(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseResponse);
        baseResponse.setMessage("Purchase created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES-UPDATE')")
    public BaseResponse updatePurchase(@Valid @RequestBody PurchaseUpdateRequest request) {
        PurchaseResponse purchaseResponse = purchaseService.updatePurchase(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseResponse);
        baseResponse.setMessage("Purchase updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES-DELETE')")
    public BaseResponse deletePayment(@Valid @RequestBody PurchaseDeleteRequest request) {
        return purchaseService.deletePurchase(request);
    }

    @PostMapping("/return")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES-CREATE')")
    public BaseResponse returnPurchase(@Valid @RequestBody PurchaseReturnRequest request) {
        PurchaseResponse purchaseResponse = purchaseService.returnPurchase(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseResponse);
        baseResponse.setMessage("Purchase returned successfully.");
        return baseResponse;
    }

    @GetMapping("/retrievePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-RETRIEVE')")
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

    @GetMapping("/retrievePaymentsByPurchaseId")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-RETRIEVE')")
    public BaseResponse retrievePaymentsByPurchaseId(PaymentRetrieveRequest request) {
        if (request.getPurchaseId() == null) {
            throw new ApiException("Purchase ID is required.", HttpStatus.BAD_REQUEST);
        }
        List<PaymentResponse> paymentResponse = paymentService.getPaymentsByPurchaseId(request.getPurchaseId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payments retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/createPayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-CREATE')")
    public BaseResponse createPayment(@Valid @RequestBody List<PaymentCreateRequest> request) {
        List<PaymentResponse> paymentResponses = paymentService.createPayment(PaymentTransactionType.PURCHASE, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponses);
        baseResponse.setMessage("Payment created successfully.");
        return baseResponse;
    }

    @PostMapping("/updatePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-UPDATE')")
    public BaseResponse updatePayment(@Valid @RequestBody PaymentUpdateRequest request) {
        PaymentResponse paymentResponse = paymentService.updatePayment(PaymentTransactionType.PURCHASE, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payment updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/deletePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-DELETE')")
    public BaseResponse deletePayment(@Valid @RequestBody PaymentDeleteRequest request) {
        paymentService.deletePayment(PaymentTransactionType.PURCHASE, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Payment deleted successfully.");
        return baseResponse;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES-CREATE')")
    public BaseResponse importPurchase(@Valid @ModelAttribute PurchaseImportRequest request, HttpServletRequest servletRequest) {
        purchaseService.importPurchase(request, servletRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Purchase imported successfully.");
        return baseResponse;
    }
}
