package kh.com.csx.posapi.controller.supplier;

import kh.com.csx.posapi.dto.supplier.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.SupplierService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveSupplier(SupplierRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Supplier ID is required", HttpStatus.BAD_REQUEST);
        }
        SupplierResponse supplierResponse = supplierService.getSupplierById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(supplierResponse);
        baseResponse.setMessage("Supplier retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/search")
    public BaseResponse getSuppliersTerm(SupplierRetrieveRequest request) {
        List<SupplierResponse> suppliersResponse = supplierService.getSuppliersTerm(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(suppliersResponse);
        baseResponse.setMessage("Suppliers retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListSuppliers(SupplierRetrieveRequest request) {
        List<SupplierResponse> suppliersResponse = supplierService.getListSuppliers(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(suppliersResponse);
        baseResponse.setMessage("Suppliers retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-SUPPLIERS-RETRIEVE')")
    public BaseResponse retrieveAllSuppliers(SupplierRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(supplierService.getAllSuppliers(request));
        baseResponse.setMessage("Suppliers retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-SUPPLIERS-CREATE')")
    public BaseResponse createSupplier(@Valid @RequestBody SupplierCreateRequest request) {
        SupplierResponse supplierResponse = supplierService.createSupplier(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(supplierResponse);
        baseResponse.setMessage("Supplier created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-SUPPLIERS-UPDATE')")
    public BaseResponse updateSupplier(@Valid @RequestBody SupplierUpdateRequest request) {
        SupplierResponse supplierResponse = supplierService.updateSupplier(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(supplierResponse);
        baseResponse.setMessage("Supplier updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-SUPPLIERS-DELETE')")
    public BaseResponse deleteSupplier(@Valid @RequestBody SupplierDeleteRequest request) {
        return supplierService.deleteSupplier(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-SUPPLIERS-CREATE')")
    public BaseResponse importSupplier(@Valid @ModelAttribute SupplierImportRequest request) {
        Integer rows = supplierService.importSupplier(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Total " + rows + " supplier(s) imported successfully.");
        return baseResponse;
    }

}
