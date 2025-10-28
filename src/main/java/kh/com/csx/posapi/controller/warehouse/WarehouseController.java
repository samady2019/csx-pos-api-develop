package kh.com.csx.posapi.controller.warehouse;

import kh.com.csx.posapi.dto.warehouse.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveWarehouse(WarehouseRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Warehouse ID is required.", HttpStatus.BAD_REQUEST);
        }
        WarehouseResponse warehouseResponse = warehouseService.getWarehouseById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(warehouseResponse);
        baseResponse.setMessage("Warehouse retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListWarehouses(WarehouseRetrieveRequest request) {
        List<WarehouseResponse> warehousesResponse = warehouseService.getListWarehouses(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(warehousesResponse);
        baseResponse.setMessage("Warehouses retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-WAREHOUSE')")
    public BaseResponse retrieveAllWarehouses(WarehouseRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(warehouseService.getAllWarehouses(request));
        baseResponse.setMessage("Warehouses retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-WAREHOUSE')")
    public BaseResponse createWarehouse(@Valid @RequestBody WarehouseCreateRequest request) {
        WarehouseResponse warehouseResponse = warehouseService.createWarehouse(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(warehouseResponse);
        baseResponse.setMessage("Warehouse created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-WAREHOUSE')")
    public BaseResponse updateWarehouse(@Valid @RequestBody WarehouseUpdateRequest request) {
        WarehouseResponse warehouseResponse = warehouseService.updateWarehouse(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(warehouseResponse);
        baseResponse.setMessage("Warehouse updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-WAREHOUSE')")
    public BaseResponse deleteWarehouse(@Valid @RequestBody WarehouseDeleteRequest request) {
        return warehouseService.deleteWarehouse(request);
    }

}
