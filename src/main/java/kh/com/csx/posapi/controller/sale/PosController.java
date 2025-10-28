package kh.com.csx.posapi.controller.sale;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.payment.*;
import kh.com.csx.posapi.dto.posRegister.*;
import kh.com.csx.posapi.dto.sale.*;
import kh.com.csx.posapi.dto.suspendedBill.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PaymentService;
import kh.com.csx.posapi.service.SaleService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
public class PosController {
    private final SaleService saleService;
    private final PaymentService paymentService;
    private final Utility utility;

    @GetMapping("/checkUserRegister")
    public BaseResponse checkUserRegister(PosRegisterRetrieveRequest request) {
        PosRegisterStatusResponse posRegisterResponse = saleService.checkUserRegister(request.getUserId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(posRegisterResponse);
        baseResponse.setMessage("POS Register Status.");
        return baseResponse;
    }

    @GetMapping("/retrieveRegister")
    public BaseResponse retrieveRegister(PosRegisterRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("POS register ID is required.", HttpStatus.BAD_REQUEST);
        }
        PosRegisterResponse posRegisterResponse = saleService.getRegisterById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(posRegisterResponse);
        baseResponse.setMessage("Register retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-REGISTER')")
    public BaseResponse register(@Valid @RequestBody PosRegisterOpenRequest request) {
        PosRegisterResponse posRegisterResponse = saleService.openRegister(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(posRegisterResponse);
        baseResponse.setMessage("Register opened successfully.");
        return baseResponse;
    }

    @PostMapping("/close")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-CLOSE')")
    public BaseResponse close(@Valid @RequestBody PosRegisterCloseRequest request) {
        PosRegisterResponse posRegisterResponse = saleService.closeRegister(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(posRegisterResponse);
        baseResponse.setMessage("Register closed successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-RETRIEVE')")
    public BaseResponse retrieveSale(SaleRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Sale ID is required.", HttpStatus.BAD_REQUEST);
        }
        SaleResponse saleResponse = saleService.getSaleById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(saleResponse);
        baseResponse.setMessage("Sale retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-RETRIEVE')")
    public BaseResponse retrieveAllSales(SaleRetrieveRequest request) {
        request.setPos(Constant.YES);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(saleService.getAllSales(request));
        baseResponse.setMessage("Sales retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-CREATE')")
    public BaseResponse createSale(@Valid @RequestBody SaleCreateRequest request) {
        SaleResponse saleResponse = saleService.createPOS(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(saleResponse);
        baseResponse.setMessage("Sale created successfully.");
        return baseResponse;
    }

    @GetMapping("/retrievePayment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-PAYMENTS-RETRIEVE')")
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

    @GetMapping("/retrievePaymentsBySaleId")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-PAYMENTS-RETRIEVE')")
    public BaseResponse retrievePaymentsBySaleId(PaymentRetrieveRequest request) {
        if (request.getSaleId() == null) {
            throw new ApiException("Sale ID is required.", HttpStatus.BAD_REQUEST);
        }
        List<PaymentResponse> paymentResponse = paymentService.getPaymentsBySaleId(request.getSaleId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentResponse);
        baseResponse.setMessage("Payments retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveSuspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-SUSPEND-RETRIEVE')")
    public BaseResponse retrieveSuspend(SuspendedBillRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Suspend ID is required.", HttpStatus.BAD_REQUEST);
        }
        SuspendedBillResponse suspendedBillResponse = saleService.getSuspendedById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(suspendedBillResponse);
        baseResponse.setMessage("Suspended bill retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAllSuspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-SUSPEND-RETRIEVE')")
    public BaseResponse retrieveAllSuspend(SuspendedBillRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(saleService.getAllSuspended(request));
        baseResponse.setMessage("Suspended bills retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/createSuspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-SUSPEND-CREATE')")
    public BaseResponse createSuspend(@Valid @RequestBody SuspendedBillCreateRequest request) {
        SuspendedBillResponse suspendedBillResponse = saleService.createSuspend(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(suspendedBillResponse);
        baseResponse.setMessage("Bill suspended successfully.");
        return baseResponse;
    }

    @PostMapping("/updateSuspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-SUSPEND-UPDATE')")
    public BaseResponse updateSuspend(@Valid @RequestBody SuspendedBillUpdateRequest request) {
        SuspendedBillResponse suspendedBillResponse = saleService.updateSuspend(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(suspendedBillResponse);
        baseResponse.setMessage("Suspended bill updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/deleteSuspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-SUSPEND-DELETE')")
    public BaseResponse deleteSuspend(@Valid @RequestBody SuspendedBillDeleteRequest request) {
        return saleService.deleteSuspend(request);
    }

    @GetMapping("/tax-invoice")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-POS-RETRIEVE')")
    public ResponseEntity<byte[]> taxInvoice(SaleRetrieveRequest request) {
        byte[] pdfBytes = saleService.taxInvoice(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("TAX_INVOICE_" + utility.convertDateTimeFormat(LocalDateTime.now().toString(), "yyyyMMddHHmmss") + ".pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
