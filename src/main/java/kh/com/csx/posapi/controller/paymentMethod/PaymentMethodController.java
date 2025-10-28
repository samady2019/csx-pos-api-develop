package kh.com.csx.posapi.controller.paymentMethod;

import kh.com.csx.posapi.dto.paymentMethod.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/paymentMethod")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping("/retrieve")
    public BaseResponse retrievePaymentMethod(PaymentMethodRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Payment method ID is required", HttpStatus.BAD_REQUEST);
        }
        PaymentMethodResponse paymentMethodResponse = paymentMethodService.getPaymentMethodById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentMethodResponse);
        baseResponse.setMessage("Payment method retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListPaymentMethods(PaymentMethodRetrieveRequest request) {
        List<PaymentMethodResponse> paymentMethodsResponse = paymentMethodService.getListPaymentMethods(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentMethodsResponse);
        baseResponse.setMessage("Payment methods retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-PAYMENT_METHOD')")
    public BaseResponse retrieveAllPaymentMethods(PaymentMethodRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentMethodService.getAllPaymentMethods(request));
        baseResponse.setMessage("Payment methods retrieved successfully");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-PAYMENT_METHOD')")
    public BaseResponse createPaymentMethod(@Valid @RequestBody PaymentMethodCreateRequest request) {
        PaymentMethodResponse paymentMethodResponse = paymentMethodService.createPaymentMethod(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentMethodResponse);
        baseResponse.setMessage("Payment method created successfully");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-PAYMENT_METHOD')")
    public BaseResponse updatePaymentMethod(@Valid @RequestBody PaymentMethodUpdateRequest request) {
        PaymentMethodResponse paymentMethodResponse = paymentMethodService.updatePaymentMethod(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(paymentMethodResponse);
        baseResponse.setMessage("Payment method updated successfully");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-PAYMENT_METHOD')")
    public BaseResponse deletePaymentMethod(@Valid @RequestBody PaymentMethodDeleteRequest request) {
        return paymentMethodService.deletePaymentMethod(request);
    }

}
