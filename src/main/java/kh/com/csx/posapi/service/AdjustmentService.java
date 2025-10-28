package kh.com.csx.posapi.service;

import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.ReferenceKey;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.adjustment.*;
import kh.com.csx.posapi.dto.setting.FileInfoResponse;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdjustmentService {
    private final EntityManager entityManager;
    private final AdjustmentRepository adjustmentRepository;
    private final AdjustmentItemRepository adjustmentItemRepository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockCountRepository stockCountRepository;
    private final BillerRepository billerRepository;

    private final ProductService productService;
    private final Utility utility;

    public AdjustmentResponse getAdjustmentById(Long id) {
        AdjustmentEntity data = adjustmentRepository.findById(id).orElseThrow(() -> new ApiException("Adjustment not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);
        data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
        return AdjustmentResponse.builder().adjustment(data).build();
    }

    public Page<AdjustmentResponse> getAllAdjustments(AdjustmentRetrieveRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("referenceNo, date");
            }
            if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
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
            Page<AdjustmentEntity> adjustmentEntities = adjustmentRepository.findAllByFilter(request, pageable);
            return adjustmentEntities.map(
                data -> {
                    data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
                    return AdjustmentResponse.builder().adjustment(data).build();
                }
            );
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public AdjustmentResponse createAdjustment(AdjustmentCreateRequest request) {
        try {
            utility.verifyAccess(request);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user     = (UserEntity) authentication.getPrincipal();
            Long user_id        = user.getUserId();
            LocalDateTime date  = request.getDate();
            Long biller_id      = request.getBillerId();
            Long warehouse_id   = request.getWarehouseId();
            String reference_no = utility.checkReferenceNo(biller_id, ReferenceKey.AJ, request.getReferenceNo().trim());
            String attachment   = request.getAttachment();
            String note         = request.getNote();
            warehouseRepository.findById(warehouse_id).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
            billerRepository.findById(biller_id).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
            Long count_id = null;
            if (request.getCountId() != null) {
                StockCountEntity stockCount = stockCountRepository.findById(request.getCountId()).orElseThrow(() -> new ApiException("Stock count not found.", HttpStatus.BAD_REQUEST));
                count_id = stockCount.getId();
            }
            AdjustmentEntity adjustment = new AdjustmentEntity();
            adjustment.setCountId(count_id);
            adjustment.setDate(date);
            adjustment.setReferenceNo(reference_no);
            adjustment.setBillerId(biller_id);
            adjustment.setWarehouseId(warehouse_id);
            adjustment.setAttachment(attachment);
            adjustment.setNote(note);
            adjustment.setCreatedBy(user_id);
            adjustment.setCreatedAt(LocalDateTime.now());
            adjustment = adjustmentRepository.save(adjustment);
            List<AdjustmentItemEntity> adjustmentItems = new ArrayList<>();
            List<StockMovementEntity> stockMovements = new ArrayList<>();
            Long adjustmentId = adjustment.getId();
            for (AdjustmentItemRequest itemRequest : request.getItems()) {
                Long productId = itemRequest.getProductId();
                ProductEntity product = productRepository.findById(productId).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
                if (product.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                    throw new ApiException("The product name: '" + product.getProductNameEn() + "' (" + product.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
                }
                if (!product.getType().equals(Constant.ProductType.STANDARD)) {
                    throw new ApiException("The product name: '" + product.getProductNameEn() + "' (" + product.getProductCode() + ") with type '" + product.getType() + "' is not allowed.", HttpStatus.BAD_REQUEST);
                }
                Long unitId = itemRequest.getUnitId();
                UnitEntity unit = unitRepository.findById(unitId).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
                if (!utility.validProductUnit(productId, unitId)) {
                    throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product.getProductNameEn() + "' (" + product.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
                }
                String type = itemRequest.getType().toLowerCase().trim();
                if (!Constant.AdjustmentType.VALID.contains(type)) {
                    throw new ApiException("Invalid type. " + Constant.AdjustmentType.NOTE, HttpStatus.BAD_REQUEST);
                }
                LocalDate expiry           = itemRequest.getExpiry();
                double    unitQuantity     = utility.formatQuantity(itemRequest.getUnitQuantity());
                double    baseUnitQuantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(unitId, unitQuantity));
                double    baseUnitCost     = utility.formatDecimal(utility.getAvgCost(productId));
                double    unitCost         = utility.formatDecimal(utility.convertToBaseUnitPrice(unitId, baseUnitCost));

                AdjustmentItemEntity adjustmentItem = AdjustmentItemEntity.builder()
                        .warehouseId(warehouse_id)
                        .type(type)
                        .productId(productId)
                        .expiry(expiry)
                        .unitId(unitId)
                        .unitQuantity(unitQuantity)
                        .quantity(baseUnitQuantity)
                        .unitCost(unitCost)
                        .baseUnitCost(baseUnitCost)
                        .adjustment(adjustment)
                        .build();
                adjustmentItems.add(adjustmentItem);

                StockMovementEntity stockMovement = new StockMovementEntity();
                stockMovement.setDate(date);
                stockMovement.setTransactionId(adjustmentId);
                stockMovement.setTransaction(Constant.StockTransactionType.ADJUSTMENT);
                stockMovement.setWarehouseId(warehouse_id);
                stockMovement.setProductId(productId);
                stockMovement.setUnitId(unitId);
                stockMovement.setExpiry(expiry);
                stockMovement.setUnitQuantity(type.equals(Constant.AdjustmentType.SUBTRACTION) ? (-1 * unitQuantity) : unitQuantity);
                stockMovement.setQuantity(type.equals(Constant.AdjustmentType.SUBTRACTION) ? (-1 * baseUnitQuantity) : baseUnitQuantity);
                stockMovement.setCost(baseUnitCost);
                stockMovements.add(stockMovement);
            }
            adjustmentItemRepository.saveAll(adjustmentItems);
            stockMovementRepository.saveAll(stockMovements);
            utility.checkOverstock(warehouse_id, stockMovements);
            utility.updateReferenceNo(biller_id, ReferenceKey.AJ, reference_no);
            entityManager.flush();
            entityManager.clear();

            return getAdjustmentById(adjustmentId);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public AdjustmentResponse updateAdjustment(AdjustmentUpdateRequest request) {
        try {
            utility.verifyAccess(request);

            AdjustmentEntity adjustment = adjustmentRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Adjustment not found.", HttpStatus.BAD_REQUEST));
            warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
            billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
            if (adjustmentRepository.existsByReferenceNoAndIdNot(request.getReferenceNo(), adjustment.getId())) {
                throw new ApiException("Reference number already exists.", HttpStatus.BAD_REQUEST);
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity   = (UserEntity) authentication.getPrincipal();
            String reference_no     = request.getReferenceNo();
            String old_reference_no = adjustment.getReferenceNo();

            adjustment.setDate(request.getDate());
            adjustment.setReferenceNo(request.getReferenceNo());
            adjustment.setWarehouseId(request.getWarehouseId());
            adjustment.setBillerId(request.getBillerId());
            adjustment.setNote(request.getNote());
            adjustment.setAttachment(request.getAttachment());
            adjustment.setUpdatedBy(userEntity.getUserId());
            adjustment.setUpdatedAt(LocalDateTime.now());
            List<AdjustmentItemEntity> adjustmentItems = new ArrayList<>();
            List<StockMovementEntity> stockMovements = new ArrayList<>();
            for (AdjustmentItemRequest itemRequest : request.getItems()) {
                Long productId = itemRequest.getProductId();
                ProductEntity product = productRepository.findById(productId).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
                if (product.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                    throw new ApiException("The product name: '" + product.getProductNameEn() + "' (" + product.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
                }
                if (!product.getType().equals(Constant.ProductType.STANDARD)) {
                    throw new ApiException("The product name: '" + product.getProductNameEn() + "' (" + product.getProductCode() + ") with type '" + product.getType() + "' is not allowed.", HttpStatus.BAD_REQUEST);
                }
                Long unitId = itemRequest.getUnitId();
                UnitEntity unit = unitRepository.findById(unitId).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
                if (!utility.validProductUnit(productId, unitId)) {
                    throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product.getProductNameEn() + "' (" + product.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
                }
                String type = itemRequest.getType().toLowerCase().trim();
                if (!Constant.AdjustmentType.VALID.contains(type)) {
                    throw new ApiException("Invalid type. " + Constant.AdjustmentType.NOTE, HttpStatus.BAD_REQUEST);
                }
                LocalDate expiry           = itemRequest.getExpiry();
                double    unitQuantity     = utility.formatQuantity(itemRequest.getUnitQuantity());
                double    baseUnitQuantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(unitId, unitQuantity));
                double    baseUnitCost     = utility.formatDecimal(utility.getAvgCost(itemRequest.getProductId()));
                double    unitCost         = utility.formatDecimal(utility.convertToBaseUnitPrice(unitId, baseUnitCost));

                AdjustmentItemEntity item = new AdjustmentItemEntity();
                item.setAdjustment(adjustment);
                item.setWarehouseId(request.getWarehouseId());
                item.setType(type);
                item.setProductId(productId);
                item.setExpiry(expiry);
                item.setUnitId(unitId);
                item.setUnitQuantity(unitQuantity);
                item.setQuantity(baseUnitQuantity);
                item.setUnitCost(unitCost);
                item.setBaseUnitCost(baseUnitCost);
                adjustmentItems.add(item);

                StockMovementEntity stockMovement = new StockMovementEntity();
                stockMovement.setDate(request.getDate());
                stockMovement.setTransactionId(adjustment.getId());
                stockMovement.setTransaction(Constant.StockTransactionType.ADJUSTMENT);
                stockMovement.setWarehouseId(request.getWarehouseId());
                stockMovement.setProductId(productId);
                stockMovement.setUnitId(unitId);
                stockMovement.setExpiry(expiry);
                stockMovement.setUnitQuantity(type.equals(Constant.AdjustmentType.SUBTRACTION) ? (-1 * unitQuantity) : unitQuantity);
                stockMovement.setQuantity(type.equals(Constant.AdjustmentType.SUBTRACTION) ? (-1 * baseUnitQuantity) : baseUnitQuantity);
                stockMovement.setCost(baseUnitCost);
                stockMovements.add(stockMovement);
            }
            AdjustmentEntity oldAdjustment = adjustmentRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Adjustment not found.", HttpStatus.BAD_REQUEST));
            List<StockMovementEntity> oldStockMovements = stockMovementRepository.findByTransactionAndTransactionId(Constant.StockTransactionType.ADJUSTMENT, adjustment.getId());
            adjustmentItemRepository.deleteByAdjustmentId(adjustment.getId());
            stockMovementRepository.deleteByTransactionAndTransactionId(Constant.StockTransactionType.ADJUSTMENT, adjustment.getId());
            adjustment.setItems(adjustmentItems);
            adjustment = adjustmentRepository.save(adjustment);
            adjustmentItemRepository.saveAll(adjustmentItems);
            stockMovementRepository.saveAll(stockMovements);
            utility.checkOverstock(oldAdjustment.getWarehouseId(), oldStockMovements);
            utility.checkOverstock(request.getWarehouseId(), stockMovements);
            if (!old_reference_no.equals(reference_no)) {
                utility.updateReferenceNo(request.getBillerId(), ReferenceKey.AJ, reference_no);
            }
            entityManager.flush();
            entityManager.clear();

            return getAdjustmentById(adjustment.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteAdjustment(AdjustmentDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteAdjustment(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteAdjustment(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Adjustment deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteAdjustment(Long id) {
        try {
            AdjustmentEntity adjustment = adjustmentRepository.findById(id).orElseThrow(() -> new ApiException("Adjustment not found.", HttpStatus.BAD_REQUEST));
            List<StockMovementEntity> stockMovements = stockMovementRepository.findByTransactionAndTransactionId(Constant.StockTransactionType.ADJUSTMENT, adjustment.getId());
            utility.verifyAccess(adjustment);

            adjustmentRepository.deleteById(id);
            adjustmentItemRepository.deleteByAdjustmentId(id);
            stockMovementRepository.deleteByTransactionAndTransactionId(Constant.StockTransactionType.ADJUSTMENT, id);
            utility.checkOverstock(adjustment.getWarehouseId(), stockMovements);

            return new BaseResponse("Adjustment reference no. '" + adjustment.getReferenceNo() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public AdjustmentResponse importAdjustment(AdjustmentImportRequest request, HttpServletRequest servletRequest) {
        List<FileInfoResponse> savedFiles = new ArrayList<>();
        try {
            LocalDateTime date           = utility.convertToLocalDateTime(request.getDate());
            String referenceNo           = request.getReferenceNo();
            Long billerId                = request.getBillerId();
            Long warehouseId             = request.getWarehouseId();
            String attachment            = null;
            String note                  = request.getNote();
            MultipartFile fileAttachment = request.getAttachment();
            MultipartFile fileItems      = request.getFile();
            List<AdjustmentItemRequest> items = new ArrayList<>();
            if (fileItems == null || fileItems.isEmpty()) {
                throw new ApiException("File is required.", HttpStatus.BAD_REQUEST);
            }
            String originalFilename = fileItems.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                throw new ApiException("Invalid file type. Only excel (.xlsx) file are allowed.", HttpStatus.BAD_REQUEST);
            }
            try (InputStream inputStream = fileItems.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    int r               = row.getRowNum();
                    String productCode  = utility.getCellValue(row.getCell(0), String.class);
                    String type         = utility.getCellValue(row.getCell(1), String.class);
                    String unitCode     = utility.getCellValue(row.getCell(2), String.class);
                    Double unitQuantity = utility.getCellValue(row.getCell(3), Double.class);
                    LocalDate expiry    = utility.getCellValue(row.getCell(4), LocalDate.class);
                    if (productCode == null || productCode.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Product code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (unitCode == null || unitCode.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Unit code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (unitQuantity == null || unitQuantity <= 0) {
                        throw new ApiException("Row #" + (r + 1) + ": Unit quantity must be positive.", HttpStatus.BAD_REQUEST);
                    }
                    if (type == null || type.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Type is required.", HttpStatus.BAD_REQUEST);
                    }
                    ProductEntity product = productRepository.findByProductCode(productCode).orElseThrow(() -> new ApiException("Row #" + (r + 1) + ": Product code '" + productCode + "' not found.", HttpStatus.BAD_REQUEST));
                    UnitEntity    unit    = unitRepository.findByUnitCode(unitCode).orElseThrow(() -> new ApiException("Row #" + (r + 1) + ": Unit code '" + unitCode + "' not found.", HttpStatus.BAD_REQUEST));
                    AdjustmentItemRequest item = new AdjustmentItemRequest();
                    item.setProductId(product.getProductId());
                    item.setUnitId(unit.getUnitId());
                    item.setUnitQuantity(unitQuantity);
                    item.setType(type);
                    item.setExpiry(expiry);
                    items.add(item);
                }
            }
            if (items.isEmpty()) {
                throw new ApiException("Adjustment must contain at least one item.", HttpStatus.BAD_REQUEST);
            }
            if (fileAttachment != null && !fileAttachment.isEmpty()) {
                List<FileInfoResponse> fileInfoAttachment = utility.uploadFile(Constant.Directory.INVENTORY, fileAttachment, servletRequest);
                attachment = fileInfoAttachment.get(0).getFileName();
                savedFiles.addAll(fileInfoAttachment);
            }
            AdjustmentCreateRequest adjustmentCreateRequest = new AdjustmentCreateRequest();
            adjustmentCreateRequest.setDate(date);
            adjustmentCreateRequest.setReferenceNo(referenceNo);
            adjustmentCreateRequest.setBillerId(billerId);
            adjustmentCreateRequest.setWarehouseId(warehouseId);
            adjustmentCreateRequest.setAttachment(attachment);
            adjustmentCreateRequest.setNote(note);
            adjustmentCreateRequest.setItems(items);

            return createAdjustment(adjustmentCreateRequest);
        } catch (Exception e) {
            for (FileInfoResponse savedFile : savedFiles) {
                try {
                    Path path = Paths.get(savedFile.getFilePath()).toAbsolutePath().normalize();
                    Files.delete(path);
                } catch (IOException ioException) {
                    System.out.println("Failed to rollback file: " + savedFile + ", " + ioException.getMessage());
                }
            }
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
