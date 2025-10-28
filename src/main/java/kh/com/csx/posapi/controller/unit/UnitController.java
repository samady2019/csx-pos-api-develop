package kh.com.csx.posapi.controller.unit;

import kh.com.csx.posapi.dto.unit.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.UnitService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/unit")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveUnit(UnitRetrieveRequest request) {
        if (request.getUnitId() == null) {
            throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
        }
        UnitResponse unitResponse = unitService.getUnitByID(request.getUnitId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(unitResponse);
        baseResponse.setMessage("Unit retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveByParent")
    public BaseResponse retrieveUnitsByParent(UnitRetrieveRequest request) {
        if (request.getUnitId() == null) {
            throw new ApiException("Unit ID is required.", HttpStatus.BAD_REQUEST);
        }
        List<UnitResponse> unitResponses = unitService.getUnitsByParentID(request.getUnitId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(unitResponses);
        baseResponse.setMessage("Units retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListUnits() {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(unitService.getListUnits());
        baseResponse.setMessage("Units retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAllParent")
    public BaseResponse retrieveAllParentUnits() {
        List<UnitResponse> unitsResponse = unitService.getAllParentUnits();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(unitsResponse);
        baseResponse.setMessage("Units retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/convertToBaseUnit")
    public BaseResponse convertToBaseUnit(@Valid UnitConversionRequest request) {
        UnitConversionResponse response = unitService.convertToBaseUnit(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(response);
        baseResponse.setMessage("Unit converted successfully.");
        return baseResponse;
    }

    @GetMapping("/convertFromBaseUnit")
    public BaseResponse convertFromBaseUnit(@Valid UnitConversionRequest request) {
        UnitConversionResponse response = unitService.convertFromBaseUnit(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(response);
        baseResponse.setMessage("Unit converted successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-UNITS-RETRIEVE')")
    public BaseResponse retrieveAllUnits(UnitRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(unitService.getAllUnits(request));
        baseResponse.setMessage("Units retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-UNITS-CREATE')")
    public BaseResponse createUnit(@Valid @RequestBody UnitCreateRequest request) {
        UnitResponse unitResponse = unitService.createUnit(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(unitResponse);
        baseResponse.setMessage("Unit created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-UNITS-UPDATE')")
    public BaseResponse updateUnit(@Valid @RequestBody UnitUpdateRequest request) {
        UnitResponse unitResponse = unitService.updateUnit(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(unitResponse);
        baseResponse.setMessage("Unit updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-UNITS-DELETE')")
    public BaseResponse deleteUnit(@Valid @RequestBody UnitDeleteRequest request) {
        return unitService.deleteUnit(request);
    }
}
