package kh.com.csx.posapi.controller.promotion;

import jakarta.validation.Valid;
import kh.com.csx.posapi.dto.promotion.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promotion")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-PROMOTIONS-CREATE')")
    public BaseResponse createPromotion(@Valid @RequestBody PromotionCreateRequest request) {
        try {
            PromotionResponse createdPromotion = promotionService.createPromotion(request);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Promotion created successfully.");
            baseResponse.setData(createdPromotion);
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-PROMOTIONS-UPDATE')")
    public BaseResponse updatePromotion(@Valid @RequestBody PromotionUpdateRequest updateRequest) {
        try {
            PromotionResponse updatedPromotion = promotionService.updatePromotion(updateRequest);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Promotion updated successfully.");
            baseResponse.setData(updatedPromotion);
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-PROMOTIONS-DELETE')")
    public BaseResponse deletePromotion(@Valid @RequestBody PromotionDeleteRequest request) {
        try {
            return promotionService.deletePromotion(request);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-PROMOTIONS-RETRIEVE')")
    public BaseResponse getAllPromotions(PromotionRetrieveRequest request) {
        try {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(promotionService.getAllPromotions(request));
            baseResponse.setMessage("Promotions retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/product")
    public BaseResponse getProductPromotion(PromotionRetrieveRequest request) {
        if (request.getBillerId() == null) {
            throw new ApiException("Biller ID is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getProductId() == null) {
            throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
        }
        PromotionItemResponse promotion = promotionService.getProductPromotion(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(promotion);
        baseResponse.setMessage("Product promotion retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    public BaseResponse getPromotionById(PromotionRetrieveRequest request) {
        if (request.getId() ==  null) {
            throw new ApiException("Promotion ID is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            PromotionResponse promotion = promotionService.getPromotionById(request.getId());
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(promotion);
            baseResponse.setMessage("Promotion retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
