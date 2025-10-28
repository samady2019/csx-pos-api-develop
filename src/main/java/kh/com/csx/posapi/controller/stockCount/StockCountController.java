package kh.com.csx.posapi.controller.stockCount;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.setting.OrderRefRequest;
import kh.com.csx.posapi.dto.setting.OrderRefResponse;
import kh.com.csx.posapi.dto.stockCount.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.StockCountService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stockCount")
@RequiredArgsConstructor
public class StockCountController {
    private final StockCountService stockCountService;
    private final Utility utility;

    @GetMapping("/sequenceNo")
    public BaseResponse sequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), Constant.ReferenceKey.SC)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve-initial")
    public BaseResponse retrieveInitialStockCount(StockCountRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Stock count ID is required.", HttpStatus.BAD_REQUEST);
        }
        StockCountResponse stockCountResponse = stockCountService.getInitStockCountById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(stockCountResponse);
        baseResponse.setMessage("Initial stock count retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-STOCK_COUNTS-RETRIEVE')")
    public BaseResponse retrieveStockCount(StockCountRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Stock count ID is required.", HttpStatus.BAD_REQUEST);
        }
        StockCountResponse stockCountResponse = stockCountService.getStockCountById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(stockCountResponse);
        baseResponse.setMessage("Stock count retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-STOCK_COUNTS-RETRIEVE')")
    public BaseResponse retrieveAllStockCount(StockCountRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(stockCountService.getAllStockCounts(request));
        baseResponse.setMessage("Stock counts retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-STOCK_COUNTS-CREATE')")
    public BaseResponse initializeStockCount(@Valid @RequestBody StockCountCreateRequest request) {
        StockCountResponse stockCountResponse = stockCountService.initializeStockCount(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(stockCountResponse);
        baseResponse.setMessage("Stock count initialized successfully.");
        return baseResponse;
    }

    @PostMapping("/finalize")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-STOCK_COUNTS-UPDATE')")
    public BaseResponse finalizeStockCount(@Valid @RequestBody StockCountFinalRequest request) {
        StockCountResponse stockCountResponse = stockCountService.finalizeStockCount(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(stockCountResponse);
        baseResponse.setMessage("Stock count finalized successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-STOCK_COUNTS-UPDATE')")
    public BaseResponse updateStockCount(@Valid @RequestBody StockCountUpdateRequest request) {
        StockCountResponse stockCountResponse = stockCountService.updateStockCount(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(stockCountResponse);
        baseResponse.setMessage("Stock count updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-STOCK_COUNTS-DELETE')")
    public BaseResponse deleteStockCount(@Valid @RequestBody StockCountDeleteRequest request) {
        return stockCountService.deleteStockCount(request);
    }
}
