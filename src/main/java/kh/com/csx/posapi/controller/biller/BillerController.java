package kh.com.csx.posapi.controller.biller;

import kh.com.csx.posapi.dto.biller.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.BillerService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/biller")
@RequiredArgsConstructor
public class BillerController {

    private final BillerService billerService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveBiller(BillerRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Biller ID is required", HttpStatus.BAD_REQUEST);
        }
        BillerResponse billerResponse = billerService.getBillerById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(billerResponse);
        baseResponse.setMessage("Biller retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListBillers(BillerRetrieveRequest request) {
        List<BillerResponse> billersResponse = billerService.getListBillers(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(billersResponse);
        baseResponse.setMessage("Billers retrieved successfully");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-BILLER')")
    public BaseResponse retrieveAllBillers(BillerRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(billerService.getAllBillers(request));
        baseResponse.setMessage("Billers retrieved successfully");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-BILLER')")
    public BaseResponse createBiller(@Valid @RequestBody BillerCreateRequest request) {
        BillerResponse billerResponse = billerService.createBiller(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(billerResponse);
        baseResponse.setMessage("Biller created successfully");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-BILLER')")
    public BaseResponse updateBiller(@Valid @RequestBody BillerUpdateRequest request) {
        BillerResponse billerResponse = billerService.updateBiller(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(billerResponse);
        baseResponse.setMessage("Biller updated successfully");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-BILLER')")
    public BaseResponse deleteBiller(@Valid @RequestBody BillerDeleteRequest request) {
        return billerService.deleteBiller(request);
    }

}
