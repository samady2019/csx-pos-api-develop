package kh.com.csx.posapi.controller.currencyCalender;

import kh.com.csx.posapi.dto.currencyCalender.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.CurrencyCalenderService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/currencyCalender")
@RequiredArgsConstructor
public class CurrencyCalenderController {

    private final CurrencyCalenderService currencyCalenderService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveCurrencyCalender(CurrencyCalenderRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Currency rate ID is required.", HttpStatus.BAD_REQUEST);
        }
        CurrencyCalenderResponse currencyCalenderResponse = currencyCalenderService.getCurrencyCalenderById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyCalenderResponse);
        baseResponse.setMessage("Currency rate retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveByCurrencyId")
    public BaseResponse retrieveCurrencyCalenderByCurrencyId(CurrencyCalenderRetrieveRequest request) {
        if (request.getCurrencyId() == null) {
            throw new ApiException("Currency ID is required.", HttpStatus.BAD_REQUEST);
        }
        CurrencyCalenderResponse currencyCalenderResponse = currencyCalenderService.getCurrencyCalenderByCurrencyId(request.getCurrencyId(), request.getDate());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyCalenderResponse);
        baseResponse.setMessage("Currency rate retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveByCurrencyCode")
    public BaseResponse retrieveCurrencyCalenderByCurrencyCode(CurrencyCalenderRetrieveRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new ApiException("Currency code is required.", HttpStatus.BAD_REQUEST);
        }
        CurrencyCalenderResponse currencyCalenderResponse = currencyCalenderService.getCurrencyCalenderByCurrencyCode(request.getCode(), request.getDate());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyCalenderResponse);
        baseResponse.setMessage("Currency rate retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListCurrencyCalenders(CurrencyCalenderRetrieveRequest request) {
        List<CurrencyCalenderResponse> currencyCalendersResponse = currencyCalenderService.getListCurrencyCalenders(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyCalendersResponse);
        baseResponse.setMessage("Currencies rate retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-EXCHANGE_RATE')")
    public BaseResponse retrieveAllCurrencyCalenders(CurrencyCalenderRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyCalenderService.getAllCurrencyCalenders(request));
        baseResponse.setMessage("Currencies rate retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-EXCHANGE_RATE')")
    public BaseResponse createCurrencyCalender(@Valid @RequestBody CurrencyCalenderCreateRequest request) {
        CurrencyCalenderResponse currencyCalenderResponse = currencyCalenderService.createCurrencyCalender(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyCalenderResponse);
        baseResponse.setMessage("Currency rate created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-EXCHANGE_RATE')")
    public BaseResponse updateCurrencyCalender(@Valid @RequestBody CurrencyCalenderUpdateRequest request) {
        CurrencyCalenderResponse currencyCalenderResponse = currencyCalenderService.updateCurrencyCalender(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(currencyCalenderResponse);
        baseResponse.setMessage("Currency rate updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-EXCHANGE_RATE')")
    public BaseResponse deleteCurrencyCalender(@Valid @RequestBody CurrencyCalenderDeleteRequest request) {
        return currencyCalenderService.deleteCurrencyCalender(request);
    }

}
