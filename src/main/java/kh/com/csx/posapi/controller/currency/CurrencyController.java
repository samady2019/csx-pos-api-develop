package kh.com.csx.posapi.controller.currency;

import kh.com.csx.posapi.dto.currency.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveCurrency(CurrencyRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Currency ID is required", HttpStatus.BAD_REQUEST);
        }
        CurrencyResponse currencyResponse = currencyService.getCurrencyById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyResponse);
        baseResponse.setMessage("Currency retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/retrieveByCode")
    public BaseResponse retrieveCurrencyByCode(CurrencyRetrieveRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new ApiException("Currency code is required", HttpStatus.BAD_REQUEST);
        }
        CurrencyResponse currencyResponse = currencyService.getCurrencyByCode(request.getCode());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyResponse);
        baseResponse.setMessage("Currency retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListCurrencies(CurrencyRetrieveRequest request) {
        List<CurrencyResponse> currencysResponse = currencyService.getListCurrencies(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencysResponse);
        baseResponse.setMessage("Currencies retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CURRENCY')")
    public BaseResponse retrieveAllCurrencies(CurrencyRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyService.getAllCurrencies(request));
        baseResponse.setMessage("Currencies retrieved successfully");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CURRENCY')")
    public BaseResponse createCurrency(@Valid @RequestBody CurrencyCreateRequest request) {
        CurrencyResponse currencyResponse = currencyService.createCurrency(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyResponse);
        baseResponse.setMessage("Currency created successfully");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CURRENCY')")
    public BaseResponse updateCurrency(@Valid @RequestBody CurrencyUpdateRequest request) {
        CurrencyResponse currencyResponse = currencyService.updateCurrency(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyResponse);
        baseResponse.setMessage("Currency updated successfully");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CURRENCY')")
    public BaseResponse deleteCurrency(@Valid @RequestBody CurrencyDeleteRequest request) {
        return currencyService.deleteCurrency(request);
    }

}
