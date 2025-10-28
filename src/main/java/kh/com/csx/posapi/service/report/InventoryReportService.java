package kh.com.csx.posapi.service.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.repository.WarehouseRepository;
import kh.com.csx.posapi.repository.report.InventoryReportRepository;
import kh.com.csx.posapi.dto.report.inventoryReport.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.entity.WarehouseEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.utility.Utility;
import org.springframework.data.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryReportService {
    private final InventoryReportRepository inventoryReportRepository;
    private final WarehouseRepository warehouseRepository;
    private final Utility utility;

    public Page<ProductQuantityAlertResponse> productQuantityAlerts(ProductRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("productCode");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findProductQuantityAlerts(request, pageable);
            return result.map(ProductQuantityAlertResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<ProductExpiryAlertResponse> productExpiryAlerts(ProductRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("productCode, expiry");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findProductExpiryAlerts(request, pageable);
            return result.map(ProductExpiryAlertResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<CategoryResponse> categories(CategoryRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("code");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findCategories(request, pageable);
            return result.map(CategoryResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<BrandResponse> brands(BrandRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("code");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findBrands(request, pageable);
            return result.map(BrandResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<WarehouseResponse> warehouses(ProductRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("productCode, expiry");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findProductsExpires(request, pageable);
            List<WarehouseEntity> warehouses = warehouseRepository.findAllWarehouses(user.getUserId());

            return result.map(item -> {
                List<Tuple> warehousesProduct = warehouses.stream()
                        .filter(warehouse -> request.getWarehouseId() == null || warehouse.getId().equals(request.getWarehouseId()))
                        .map(warehouse -> inventoryReportRepository.findProductExp(
                                warehouse.getId(),
                                (Long) item.get("productId"),
                                (LocalDate) item.get("expiry")))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                return new WarehouseResponse(item, warehousesProduct);
            });
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<AdjustmentResponse> adjustments(AdjustmentRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("referenceNo, date");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.DESC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findAdjustments(request, pageable);
            return result.map(AdjustmentResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<TransferResponse> transfers(TransferRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("referenceNo, date");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.DESC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findTransfers(request, pageable);
            return result.map(TransferResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<ProductInOutResponse> products(ProductRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("productCode");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            } else {
                request.setStartDate(LocalDate.now() + " 00:00:00");
                request.setEndDate(LocalDate.now() + " 23:59:59");
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Page<Tuple> result = inventoryReportRepository.findProducts(request, pageable);
            return result.map(ProductInOutResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
