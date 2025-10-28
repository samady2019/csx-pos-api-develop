package kh.com.csx.posapi.controller.adjustment;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.adjustment.*;
import kh.com.csx.posapi.dto.setting.OrderRefRequest;
import kh.com.csx.posapi.dto.setting.OrderRefResponse;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.service.AdjustmentService;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/adjustment")
@RequiredArgsConstructor
public class AdjustmentController {
    private final AdjustmentService adjustmentService;
    private final Utility utility;

    @GetMapping("/sequenceNo")
    public BaseResponse sequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), Constant.ReferenceKey.AJ)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-ADJUSTMENTS-RETRIEVE')")
    public BaseResponse getAdjustmentById(AdjustmentRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Adjustment ID is required.", HttpStatus.BAD_REQUEST);
        }
        AdjustmentResponse adjustmentResponse = adjustmentService.getAdjustmentById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(adjustmentResponse);
        baseResponse.setMessage("Adjustment retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-ADJUSTMENTS-RETRIEVE')")
    public BaseResponse getAllAdjustments(AdjustmentRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(adjustmentService.getAllAdjustments(request));
        baseResponse.setMessage("Adjustments retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-ADJUSTMENTS-CREATE')")
    public BaseResponse createAdjustment(@Valid @RequestBody AdjustmentCreateRequest request) {
        AdjustmentResponse adjustmentResponse = adjustmentService.createAdjustment(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(adjustmentResponse);
        baseResponse.setMessage("Adjustment created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-ADJUSTMENTS-UPDATE')")
    public BaseResponse updateAdjustment(@Valid @RequestBody AdjustmentUpdateRequest request) {
        AdjustmentResponse adjustmentResponse = adjustmentService.updateAdjustment(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(adjustmentResponse);
        baseResponse.setMessage("Adjustment updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-ADJUSTMENTS-DELETE')")
    public BaseResponse deleteAdjustment(@Valid @RequestBody AdjustmentDeleteRequest request) {
        return adjustmentService.deleteAdjustment(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-ADJUSTMENTS-CREATE')")
    public BaseResponse importAdjustment(@Valid @ModelAttribute AdjustmentImportRequest request, HttpServletRequest servletRequest) {
        adjustmentService.importAdjustment(request, servletRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Adjustment imported successfully.");
        return baseResponse;
    }
}
