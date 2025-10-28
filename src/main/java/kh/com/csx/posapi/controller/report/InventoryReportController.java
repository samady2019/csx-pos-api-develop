package kh.com.csx.posapi.controller.report;

import kh.com.csx.posapi.service.report.InventoryReportService;
import kh.com.csx.posapi.dto.report.inventoryReport.*;
import kh.com.csx.posapi.model.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report/inventory")
@RequiredArgsConstructor
public class InventoryReportController {
    private final InventoryReportService inventoryReportService;

    @GetMapping("product-quantity-alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse productQuantityAlerts(ProductRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.productQuantityAlerts(request));
        baseResponse.setMessage("Product quantity alerts report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("product-expiry-alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse productExpiryAlerts(ProductRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.productExpiryAlerts(request));
        baseResponse.setMessage("Product expiry alerts report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("category")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse categories(CategoryRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.categories(request));
        baseResponse.setMessage("Categories report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("brand")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse brands(BrandRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.brands(request));
        baseResponse.setMessage("Brands report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("warehouse")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse warehouses(ProductRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.warehouses(request));
        baseResponse.setMessage("Warehouses report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("adjustment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-ADJUSTMENTS')")
    public BaseResponse adjustments(AdjustmentRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.adjustments(request));
        baseResponse.setMessage("Adjustments report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("transfer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-TRANSFERS')")
    public BaseResponse transfers(TransferRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.transfers(request));
        baseResponse.setMessage("Transfers report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-REPORTS-PRODUCTS')")
    public BaseResponse products(ProductRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(inventoryReportService.products(request));
        baseResponse.setMessage("Products report retrieved successfully.");
        return baseResponse;
    }
}
