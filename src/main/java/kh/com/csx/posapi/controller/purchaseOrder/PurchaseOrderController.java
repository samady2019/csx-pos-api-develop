package kh.com.csx.posapi.controller.purchaseOrder;

import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.ReferenceKey;
import kh.com.csx.posapi.dto.payment.*;
import kh.com.csx.posapi.dto.purchaseOrder.*;
import kh.com.csx.posapi.dto.setting.OrderRefRequest;
import kh.com.csx.posapi.dto.setting.OrderRefResponse;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PaymentService;
import kh.com.csx.posapi.service.PurchaseOrderService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/purchaseOrder")
@RequiredArgsConstructor
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;
    private final PaymentService paymentService;
    private final Utility utility;

    @GetMapping("/sequenceNo")
    public BaseResponse sequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), ReferenceKey.PO)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES_ORDER-RETRIEVE')")
    public BaseResponse retrievePurchaseOrder(PurchaseOrderRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Purchase order ID is required", HttpStatus.BAD_REQUEST);
        }
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.getPurchaseOrderById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseOrderResponse);
        baseResponse.setMessage("Purchase order retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES_ORDER-RETRIEVE')")
    public BaseResponse retrieveAllPurchasesOrder(PurchaseOrderRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseOrderService.getAllPurchasesOrder(request));
        baseResponse.setMessage("Purchases order retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES_ORDER-CREATE')")
    public BaseResponse createPurchaseOrder(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.createPurchaseOrder(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseOrderResponse);
        baseResponse.setMessage("Purchase order created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES_ORDER-UPDATE')")
    public BaseResponse updatePurchaseOrder(@Valid @RequestBody PurchaseOrderUpdateRequest request) {
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.updatePurchaseOrder(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(purchaseOrderResponse);
        baseResponse.setMessage("Purchase order updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES_ORDER-DELETE')")
    public BaseResponse deletePayment(@Valid @RequestBody PurchaseOrderDeleteRequest request) {
        return purchaseOrderService.deletePurchaseOrder(request);
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

    @GetMapping("/retrievePaymentsByPurchaseOrderId")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-RETRIEVE')")
    public BaseResponse retrievePaymentsByPurchaseOrderId(PaymentRetrieveRequest request) {
        if (request.getPurchaseOrderId() == null) {
            throw new ApiException("Purchase order ID is required.", HttpStatus.BAD_REQUEST);
        }
        List<PaymentResponse> paymentResponse = paymentService.getPaymentsByPurchaseOrderId(request.getPurchaseOrderId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payments retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/createPayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-CREATE')")
    public BaseResponse createPayment(@Valid @RequestBody List<PaymentCreateRequest> request) {
        List<PaymentResponse> paymentResponses = paymentService.createPayment(Constant.PaymentTransactionType.PURCHASE_ORDER, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponses);
        baseResponse.setMessage("Payment created successfully.");
        return baseResponse;
    }

    @PostMapping("/updatePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-UPDATE')")
    public BaseResponse updatePayment(@Valid @RequestBody PaymentUpdateRequest request) {
        PaymentResponse paymentResponse = paymentService.updatePayment(Constant.PaymentTransactionType.PURCHASE_ORDER, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payment updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/deletePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PAYMENTS-DELETE')")
    public BaseResponse deletePayment(@Valid @RequestBody PaymentDeleteRequest request) {
        paymentService.deletePayment(Constant.PaymentTransactionType.PURCHASE_ORDER, request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Payment deleted successfully.");
        return baseResponse;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-PURCHASES_ORDER-CREATE')")
    public BaseResponse importPurchaseOrder(@Valid @ModelAttribute PurchaseOrderImportRequest request, HttpServletRequest servletRequest) {
        purchaseOrderService.importPurchaseOrder(request, servletRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Purchase order imported successfully.");
        return baseResponse;
    }
}
