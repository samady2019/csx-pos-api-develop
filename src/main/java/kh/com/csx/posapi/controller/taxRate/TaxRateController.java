package kh.com.csx.posapi.controller.taxRate;

import kh.com.csx.posapi.dto.taxRate.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.TaxRateService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/taxRate")
@RequiredArgsConstructor
public class TaxRateController {

    private final TaxRateService taxRateService;
    private final Utility utility;

    @GetMapping("/retrieve")
    public BaseResponse retrieveTaxRate(TaxRateRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Tax rate ID is required.", HttpStatus.BAD_REQUEST);
        }
        TaxRateResponse taxRateResponse = taxRateService.getTaxRateById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateResponse);
        baseResponse.setMessage("Tax rate retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveByCode")
    public BaseResponse retrieveTaxRateByCode(TaxRateRetrieveRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new ApiException("Tax rate code is required.", HttpStatus.BAD_REQUEST);
        }
        TaxRateResponse taxRateResponse = taxRateService.getTaxRateByCode(request.getCode());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateResponse);
        baseResponse.setMessage("Tax rate retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListTaxRates(TaxRateRetrieveRequest request) {
        List<TaxRateResponse> taxRatesResponse = taxRateService.getListTaxRates(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRatesResponse);
        baseResponse.setMessage("Tax rates retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-TAX_RATE')")
    public BaseResponse retrieveAllTaxRates(TaxRateRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.getAllTaxRates(request));
        baseResponse.setMessage("Tax rates retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-TAX_RATE')")
    public BaseResponse createTaxRate(@Valid @RequestBody TaxRateCreateRequest request) {
        TaxRateResponse taxRateResponse = taxRateService.createTaxRate(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateResponse);
        baseResponse.setMessage("Tax rate created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-TAX_RATE')")
    public BaseResponse updateTaxRate(@Valid @RequestBody TaxRateUpdateRequest request) {
        TaxRateResponse taxRateResponse = taxRateService.updateTaxRate(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateResponse);
        baseResponse.setMessage("Tax rate updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-TAX_RATE')")
    public BaseResponse deleteTaxRate(@Valid @RequestBody TaxRateDeleteRequest request) {
        return taxRateService.deleteTaxRate(request);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/productById")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-PRODUCT')")
    public BaseResponse productById(TaxRateProductRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.retrieveTaxRateProduct(request));
        baseResponse.setMessage("Tax rate product retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-PRODUCT')")
    public BaseResponse product(TaxRateProductRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.retrieveTaxRateProducts(request));
        baseResponse.setMessage("Tax rate products retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-PRODUCT')")
    public BaseResponse product(@Valid @RequestBody TaxRateProductUpdateRequest request) {
        taxRateService.updateTaxRateProducts(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Tax rate products updated successfully.");
        return baseResponse;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping("/taxDeclare/retrieve-tran")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-RETRIEVE')")
    public BaseResponse taxDeclareRetrieveTran(TaxDeclareRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.getTaxDeclarationByTran(request.getType(), request.getTranId()));
        baseResponse.setMessage("Tax declaration retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/taxDeclare/retrieve-trans")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-RETRIEVE')")
    public BaseResponse taxDeclareRetrieveTrans(TaxDeclareRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.getTaxDeclarationByTrans(request.getType(), request.getTranIds()));
        baseResponse.setMessage("Tax declaration retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/taxDeclare/retrieve-tran-all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-RETRIEVE')")
    public BaseResponse taxDeclareRetrieveTranAll(TaxDeclareRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.getTaxDeclarationByTranAll(request));
        baseResponse.setMessage("Tax declaration retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/taxDeclare/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-RETRIEVE')")
    public BaseResponse taxDeclareRetrieve(TaxDeclareRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.getTaxDeclarationById(request.getId()));
        baseResponse.setMessage("Tax declaration retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/taxDeclare/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-RETRIEVE')")
    public BaseResponse taxDeclareRetrieveAll(TaxDeclareRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(taxRateService.getAllTaxDeclarations(request));
        baseResponse.setMessage("Tax declarations retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/taxDeclare/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-CREATE')")
    public BaseResponse taxDeclareCreate(@Valid @RequestBody TaxDeclareCreateRequest request) {
        taxRateService.createTaxDeclaration(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Tax declaration created successfully.");
        return baseResponse;
    }

    @DeleteMapping("/taxDeclare/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('TAX-TAX_DECLARE-DELETE')")
    public BaseResponse taxDeclareDelete(@Valid @RequestBody TaxDeclareDeleteRequest request) {
        return taxRateService.deleteTaxDeclaration(request);
    }

    @GetMapping("/taxDeclare/tax-invoice")
    public ResponseEntity<byte[]> taxInvoice(@Valid TaxDeclareInvoiceRequest request) {
        byte[] pdfBytes = taxRateService.taxInvoice(request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("TAX_INVOICE_" + utility.convertDateTimeFormat(LocalDateTime.now().toString(), "yyyyMMddHHmmss") + ".pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}
