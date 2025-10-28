package kh.com.csx.posapi.controller.brand;

import jakarta.validation.Valid;
import kh.com.csx.posapi.dto.brand.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/brand")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-BRANDS-CREATE')")
    public BaseResponse createBrand(@Valid @RequestBody CreateBrandDTO createBrandDTO) {
        BaseResponse baseResponse = new BaseResponse();
        try {
            ResponseBrandDTO createdBrand = brandService.createBrand(createBrandDTO);
            baseResponse.setMessage("Brand created successfully.");
            baseResponse.setData(createdBrand);
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-BRANDS-UPDATE')")
    public BaseResponse updateBrand(@Valid @RequestBody UpdateBrandDTO updateBrandDTO) {
        BaseResponse baseResponse = new BaseResponse();
        try {
            ResponseBrandDTO updatedBrand = brandService.updateBrand(updateBrandDTO);
            baseResponse.setMessage("Brand updated successfully.");
            baseResponse.setData(updatedBrand);
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-BRANDS-DELETE')")
    public BaseResponse deleteBrand(@Valid @RequestBody DeleteBrandDTO request) {
        return brandService.deleteBrand(request);
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-BRANDS-RETRIEVE')")
    public BaseResponse getAllBrands(RetrieveBrandDTO retrieveBrandDTO) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(brandService.getAllBrands(retrieveBrandDTO));
        baseResponse.setMessage("Brands retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    public BaseResponse getBrandById(RetrieveBrandDTO retrieveBrandDTO) {
        if (retrieveBrandDTO.getBrandId() == null) {
            throw new ApiException("Brand ID is required.", HttpStatus.BAD_REQUEST);
        }
        BaseResponse baseResponse = new BaseResponse();
        try {
            ResponseBrandDTO brand = brandService.getBrandById(retrieveBrandDTO);
            baseResponse.setData(brand);
            baseResponse.setMessage("Brand retrieved successfully.");
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse getListBrands(RetrieveBrandDTO retrieveBrandDTO) {
        BaseResponse baseResponse = new BaseResponse();
        List<ResponseBrandDTO> brands = brandService.getListBrands(retrieveBrandDTO);
        baseResponse.setData(brands);
        baseResponse.setMessage("Brands retrieved successfully.");
        return baseResponse;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-BRANDS-CREATE')")
    public BaseResponse importBrand(@Valid @ModelAttribute ImportBrandDTO request) {
        Integer rows = brandService.importBrand(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Total " + rows + " brand(s) imported successfully.");
        return baseResponse;
    }

    @GetMapping("/count")
    public BaseResponse countBrands() {
        BaseResponse baseResponse = new BaseResponse();
        try {
            long count = brandService.countBrands();
            baseResponse.setMessage("Total number of brands retrieved successfully.");
            baseResponse.setData(count);
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }
}
