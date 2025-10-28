package kh.com.csx.posapi.service;

import jakarta.persistence.EntityManager;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.promotion.*;
import kh.com.csx.posapi.entity.ProductEntity;
import kh.com.csx.posapi.entity.PromotionEntity;
import kh.com.csx.posapi.entity.PromotionItemEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.BillerRepository;
import kh.com.csx.posapi.repository.ProductRepository;
import kh.com.csx.posapi.repository.PromotionItemRepository;
import kh.com.csx.posapi.repository.PromotionRepository;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {
    @Autowired
    private EntityManager entityManager;

    private final PromotionRepository promotionRepository;
    private final PromotionItemRepository promotionItemRepository;
    private final ProductRepository productRepository;
    private final BillerRepository billerRepository;
    private final Utility utility;

    public PromotionItemResponse getProductPromotion(PromotionRetrieveRequest request) {
        ProductEntity product = productRepository.findByProductId(request.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
        PromotionItemEntity data = promotionItemRepository.findByProduct(request.getBillerId(), request.getProductId(), LocalDate.now()).orElse(null);
        return PromotionItemResponse.builder().item(data).build();
    }

    public PromotionResponse getPromotionById(Long id) {
        PromotionEntity data = promotionRepository.findById(id).orElseThrow(() -> new ApiException("Promotion not found.", HttpStatus.BAD_REQUEST));
        return PromotionResponse.builder().promotion(data).build();
    }

    public Page<PromotionResponse> getAllPromotions(PromotionRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id, startDate");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.DESC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
             request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        Page<PromotionEntity> promotionEntities = promotionRepository.findAllByFilter(request, pageable);
        return promotionEntities.map(promotionEntity -> PromotionResponse.builder().promotion(promotionEntity).build());
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (request.getBillers() != null && !request.getBillers().trim().isEmpty()) {
            try {
                StringBuilder strIds = new StringBuilder();
                List<Long> billerIds = Arrays.stream(request.getBillers().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                for (Long billerId : billerIds) {
                    billerRepository.findById(billerId).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
                    if (!strIds.isEmpty()) {
                        strIds.append(",");
                    }
                    strIds.append(billerId.toString());
                }
                request.setBillers(strIds.toString());
            } catch (Exception e) {
                throw new ApiException("Invalid input biller IDs.", HttpStatus.BAD_REQUEST);
            }
        }
        if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDate.now())) {
            throw new ApiException("Start date cannot be in the past", HttpStatus.BAD_REQUEST);
        }
        if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException("End date cannot be before the start date", HttpStatus.BAD_REQUEST);
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            Set<Long> userBillers = Arrays.stream(user.getBillers().split(",")).map(Long::parseLong).collect(Collectors.toSet());
            Set<Long> promotionBillers;
            if (request.getBillers() != null && !request.getBillers().trim().isEmpty()) {
                promotionBillers = Arrays.stream(request.getBillers().split(",")).map(Long::parseLong).collect(Collectors.toSet());
            } else {
                promotionBillers = new HashSet<>(billerRepository.findAllBillerIds());
            }
            if (!userBillers.containsAll(promotionBillers)) {
                throw new ApiException("You do not have access to the specified billers for this promotion.", HttpStatus.BAD_REQUEST);
            }
        }
        PromotionEntity promotion = new PromotionEntity();
        promotion.setName(request.getName().trim());
        promotion.setBillers(request.getBillers());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setDescription(request.getDescription());
        List<PromotionItemEntity> products = new ArrayList<>();
        for (PromotionItemRequest item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getDiscount() == null || item.getDiscount() <= 0) {
                throw new ApiException("Discount (%) must be provided and greater than zero.", HttpStatus.BAD_REQUEST);
            }
            ProductEntity product_details = productRepository.findByProductId(item.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
            if (product_details.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                throw new ApiException("The product name: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
            }
            PromotionItemEntity product = new PromotionItemEntity();
            product.setProductId(item.getProductId());
            product.setDiscount(item.getDiscount());
            products.add(product);
        }
        try {
            PromotionEntity data = promotionRepository.save(promotion);
            products.forEach(product -> product.setPromotionId(data.getId()));
            promotionItemRepository.saveAll(products);
            entityManager.flush();
            entityManager.clear();

            return getPromotionById(data.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public PromotionResponse updatePromotion(PromotionUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        PromotionEntity promotion = promotionRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Promotion not found.", HttpStatus.BAD_REQUEST));
        if (request.getBillers() != null && !request.getBillers().trim().isEmpty()) {
            try {
                StringBuilder strIds = new StringBuilder();
                List<Long> billerIds = Arrays.stream(request.getBillers().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                for (Long billerId : billerIds) {
                    billerRepository.findById(billerId).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
                    if (!strIds.isEmpty()) {
                        strIds.append(",");
                    }
                    strIds.append(billerId.toString());
                }
                request.setBillers(strIds.toString());
            } catch (Exception e) {
                throw new ApiException("Invalid input biller IDs.", HttpStatus.BAD_REQUEST);
            }
        }
        if (request.getStartDate() != null && request.getStartDate().isBefore(LocalDate.now())) {
            throw new ApiException("Start date cannot be in the past.", HttpStatus.BAD_REQUEST);
        }
        if (request.getEndDate() != null && request.getStartDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException("End date cannot be before the start date.", HttpStatus.BAD_REQUEST);
        }
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            Set<Long> userBillers = Arrays.stream(user.getBillers().split(",")).map(Long::parseLong).collect(Collectors.toSet());
            Set<Long> promotionBillers;
            if (request.getBillers() != null && !request.getBillers().trim().isEmpty()) {
                promotionBillers = Arrays.stream(request.getBillers().split(",")).map(Long::parseLong).collect(Collectors.toSet());
            } else {
                promotionBillers = new HashSet<>(billerRepository.findAllBillerIds());
            }
            if (!userBillers.containsAll(promotionBillers)) {
                throw new ApiException("You do not have access to the specified billers for this promotion.", HttpStatus.BAD_REQUEST);
            }
        }
        promotion.setName(request.getName().trim());
        promotion.setBillers(request.getBillers());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setDescription(request.getDescription());
        List<PromotionItemEntity> products = new ArrayList<>();
        for (PromotionItemRequest item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (item.getDiscount() == null || item.getDiscount() <= 0) {
                throw new ApiException("Discount (%) must be provided and greater than zero.", HttpStatus.BAD_REQUEST);
            }
            ProductEntity product_details = productRepository.findByProductId(item.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
            if (product_details.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                throw new ApiException("The product name: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
            }
            PromotionItemEntity product = new PromotionItemEntity();
            product.setPromotionId(promotion.getId());
            product.setProductId(item.getProductId());
            product.setDiscount(item.getDiscount());
            products.add(product);
        }
        try {
            promotionRepository.save(promotion);
            promotionItemRepository.deleteByPromotionId(promotion.getId());
            promotionItemRepository.saveAll(products);
            entityManager.flush();
            entityManager.clear();

            return getPromotionById(promotion.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deletePromotion(PromotionDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deletePromotion(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deletePromotion(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Promotion deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deletePromotion(Long id) {
        PromotionEntity promotion = promotionRepository.findById(id).orElseThrow(() -> new ApiException("Promotion not found.", HttpStatus.BAD_REQUEST));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            Set<Long> userBillers = Arrays.stream(user.getBillers().split(",")).map(Long::parseLong).collect(Collectors.toSet());
            Set<Long> promotionBillers;
            if (promotion.getBillers() != null && !promotion.getBillers().trim().isEmpty()) {
                promotionBillers = Arrays.stream(promotion.getBillers().split(",")).map(Long::parseLong).collect(Collectors.toSet());
            } else {
                promotionBillers = new HashSet<>(billerRepository.findAllBillerIds());
            }
            if (!userBillers.containsAll(promotionBillers)) {
                throw new ApiException("You do not have access to the specified billers for this promotion.", HttpStatus.BAD_REQUEST);
            }
        }
        try {
            promotionItemRepository.deleteByPromotionId(promotion.getId());
            promotionRepository.delete(promotion);
            return new BaseResponse("Promotion '" + promotion.getName() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
