package kh.com.csx.posapi.controller.vendor;

import kh.com.csx.posapi.dto.vendor.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.VendorService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveVendor(VendorRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Vendor ID is required", HttpStatus.BAD_REQUEST);
        }
        VendorResponse vendorResponse = vendorService.getVendorById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(vendorResponse);
        baseResponse.setMessage("Vendor retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListVendors(VendorRetrieveRequest request) {
        List<VendorResponse> vendorsResponse = vendorService.getListVendors(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(vendorsResponse);
        baseResponse.setMessage("Vendors retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    public BaseResponse retrieveAllVendors(VendorRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(vendorService.getAllVendors(request));
        baseResponse.setMessage("Vendors retrieved successfully");
        return baseResponse;
    }

    @PostMapping("/create")
    public BaseResponse createVendor(@Valid @RequestBody VendorCreateRequest request) {
        VendorResponse vendorResponse = vendorService.createVendor(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(vendorResponse);
        baseResponse.setMessage("Vendor created successfully");
        return baseResponse;
    }

    @PostMapping("/update")
    public BaseResponse updateVendor(@Valid @RequestBody VendorUpdateRequest request) {
        VendorResponse vendorResponse = vendorService.updateVendor(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(vendorResponse);
        baseResponse.setMessage("Vendor updated successfully");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    public BaseResponse deleteVendor(@Valid @RequestBody VendorDeleteRequest request) {
        return vendorService.deleteVendor(request);
    }

}
