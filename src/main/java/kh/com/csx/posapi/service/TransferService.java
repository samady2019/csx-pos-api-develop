package kh.com.csx.posapi.service;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.TransferStatus;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.setting.FileInfoResponse;
import kh.com.csx.posapi.dto.transfer.*;
import kh.com.csx.posapi.dto.user.BillerWarehouseRequest;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
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
import java.util.*;

@RequiredArgsConstructor
@Service
public class TransferService {
    private final EntityManager entityManager;
    private final TransferRepository transferRepository;
    private final TransferItemRepository transferItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final BillerRepository billerRepository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;

    private final ProductService productService;
    private final Utility utility;

    public TransferResponse getTransferById(Long id) {
        TransferEntity data = transferRepository.findById(id).orElseThrow(() -> new ApiException("Transfer not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(new BillerWarehouseRequest(data.getFromBiller(), data.getFromWarehouse()));
        data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getFromWarehouse())));
        return TransferResponse.builder().transfer(data).build();
    }

    public Page<TransferResponse> getAllTransfers(TransferRetrieveRequest request) {
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
            Page<TransferEntity> transferEntities = transferRepository.findAllByFilter(request, pageable);
            return transferEntities.map(
                data -> {
                    data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getFromWarehouse())));
                    return TransferResponse.builder().transfer(data).build();
                }
            );
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public TransferResponse createTransfer(TransferCreateRequest request) {
        if (!warehouseRepository.existsById(request.getFromWarehouse())) {
            throw new ApiException("From warehouse not found.", HttpStatus.BAD_REQUEST);
        }
        if (!warehouseRepository.existsById(request.getToWarehouse())) {
            throw new ApiException("To warehouse not found.", HttpStatus.BAD_REQUEST);
        }
        if (!billerRepository.existsById(request.getFromBiller())) {
            throw new ApiException("From biller not found.", HttpStatus.BAD_REQUEST);
        }
        if (!billerRepository.existsById(request.getToBiller())) {
            throw new ApiException("To biller not found.", HttpStatus.BAD_REQUEST);
        }
        utility.verifyAccess(new BillerWarehouseRequest(request.getFromBiller(), request.getFromWarehouse()));

        if (request.getFromWarehouse().equals(request.getToWarehouse())) {
            throw new ApiException("Cannot transfer between the same warehouse.", HttpStatus.BAD_REQUEST);
        }
        String status = request.getStatus().trim();
        if (!TransferStatus.VALID.contains(status)) {
            throw new ApiException("Invalid type. " + TransferStatus.NOTE, HttpStatus.BAD_REQUEST);
        }
        double total        = 0.0;
        double total_tax    = 0.0;
        double grand_total  = 0.0;
        Long   biller_id    = request.getFromBiller();
        String reference_no = utility.checkReferenceNo(biller_id, Constant.ReferenceKey.TR, request.getReferenceNo().trim());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        TransferEntity transfer = TransferEntity.builder()
                .referenceNo(reference_no)
                .date(request.getDate())
                .fromBiller(request.getFromBiller())
                .toBiller(request.getToBiller())
                .fromWarehouse(request.getFromWarehouse())
                .toWarehouse(request.getToWarehouse())
                .total(total)
                .totalTax(total_tax)
                .grandTotal(grand_total)
                .status(status)
                .attachment(request.getAttachment())
                .note(request.getNote())
                .createdBy(userEntity.getUserId())
                .createdAt(LocalDateTime.now())
                .build();

        List<TransferItemEntity>  transferItems      = new ArrayList<>();
        List<StockMovementEntity> fromStockMovements = new ArrayList<>();
        List<StockMovementEntity> toStockMovements   = new ArrayList<>();
        for (TransferItemRequest itemRequest : request.getItems()) {
            ProductEntity product_details = productRepository.findById(itemRequest.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
            if (product_details.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
            }
            if (!product_details.getType().equals(Constant.ProductType.STANDARD)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") with type '" + product_details.getType() + "' is not allowed.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(itemRequest.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(itemRequest.getProductId(), itemRequest.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            double unitQuantity     = utility.formatQuantity(itemRequest.getUnitQuantity());
            double baseUnitQuantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(itemRequest.getUnitId(), unitQuantity));
            double baseUnitCost     = utility.formatDecimal(utility.getAvgCost(itemRequest.getProductId()));
            double unitCost         = utility.formatDecimal(utility.convertToBaseUnitPrice(itemRequest.getUnitId(), baseUnitCost));
            TransferItemEntity item = TransferItemEntity.builder()
                    .fromWarehouse(transfer.getFromWarehouse())
                    .toWarehouse(transfer.getToWarehouse())
                    .productId(itemRequest.getProductId())
                    .unitId(itemRequest.getUnitId())
                    .unitQuantity(unitQuantity)
                    .quantity(baseUnitQuantity)
                    .unitCost(unitCost)
                    .baseUnitCost(baseUnitCost)
                    .expiry(itemRequest.getExpiry())
                    .build();

            StockMovementEntity fromStockMovement = new StockMovementEntity();
            fromStockMovement.setDate(request.getDate());
            fromStockMovement.setTransaction(Constant.StockTransactionType.TRANSFER);
            fromStockMovement.setWarehouseId(request.getFromWarehouse());
            fromStockMovement.setProductId(itemRequest.getProductId());
            fromStockMovement.setUnitId(itemRequest.getUnitId());
            fromStockMovement.setExpiry(itemRequest.getExpiry());
            fromStockMovement.setUnitQuantity(-1 * unitQuantity);
            fromStockMovement.setQuantity(-1 * baseUnitQuantity);
            fromStockMovement.setCost(baseUnitCost);

            StockMovementEntity toStockMovement = new StockMovementEntity();
            toStockMovement.setDate(request.getDate());
            toStockMovement.setTransaction(Constant.StockTransactionType.TRANSFER);
            toStockMovement.setWarehouseId(request.getToWarehouse());
            toStockMovement.setProductId(itemRequest.getProductId());
            toStockMovement.setUnitId(itemRequest.getUnitId());
            toStockMovement.setExpiry(itemRequest.getExpiry());
            toStockMovement.setUnitQuantity(unitQuantity);
            toStockMovement.setQuantity(baseUnitQuantity);
            toStockMovement.setCost(baseUnitCost);

            transferItems.add(item);
            fromStockMovements.add(fromStockMovement);
            toStockMovements.add(toStockMovement);

            total += (unitQuantity * unitCost);
        }
        transfer.setTotal(utility.formatDecimal(total));
        transfer.setTotalTax(utility.formatDecimal(total_tax));
        transfer.setGrandTotal(utility.formatDecimal(total + total_tax));
        try {
            transfer = transferRepository.save(transfer);
            Long id = transfer.getId();
            transferItems.forEach(product -> product.setTransferId(id));
            fromStockMovements.forEach(stock -> stock.setTransactionId(id));
            toStockMovements.forEach(stock -> stock.setTransactionId(id));
            transferItemRepository.saveAll(transferItems);
            if (status.equals(TransferStatus.COMPLETED)) {
                stockMovementRepository.saveAll(fromStockMovements);
                stockMovementRepository.saveAll(toStockMovements);
                utility.checkOverstock(request.getFromWarehouse(), fromStockMovements);
            }
            utility.updateReferenceNo(biller_id, Constant.ReferenceKey.TR, reference_no);
            entityManager.flush();
            entityManager.clear();

            return getTransferById(id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public TransferResponse updateTransfer(TransferUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        TransferEntity transfer = transferRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Transfer not found.", HttpStatus.BAD_REQUEST));
        if (!warehouseRepository.existsById(request.getFromWarehouse())) {
            throw new ApiException("From warehouse not found.", HttpStatus.BAD_REQUEST);
        }
        if (!warehouseRepository.existsById(request.getToWarehouse())) {
            throw new ApiException("To warehouse not found.", HttpStatus.BAD_REQUEST);
        }
        if (!billerRepository.existsById(request.getFromBiller())) {
            throw new ApiException("From biller not found.", HttpStatus.BAD_REQUEST);
        }
        if (!billerRepository.existsById(request.getToBiller())) {
            throw new ApiException("To biller not found.", HttpStatus.BAD_REQUEST);
        }
        utility.verifyAccess(new BillerWarehouseRequest(request.getFromBiller(), request.getFromWarehouse()));

        if (request.getFromWarehouse().equals(request.getToWarehouse())) {
            throw new ApiException("Cannot transfer between the same warehouse.", HttpStatus.BAD_REQUEST);
        }
        String status = request.getStatus().trim();
        if (!TransferStatus.VALID.contains(status)) {
            throw new ApiException("Invalid type. " + TransferStatus.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (transferRepository.existsByReferenceNoAndIdNot(request.getReferenceNo(), transfer.getId())) {
            throw new ApiException("Reference number already exists.", HttpStatus.BAD_REQUEST);
        }
        String reference_no     = request.getReferenceNo();
        String old_reference_no = transfer.getReferenceNo();
        double total            = 0.0;
        double total_tax        = 0.0;
        double grand_total      = 0.0;
        transfer.setReferenceNo(request.getReferenceNo());
        transfer.setFromBiller(request.getFromBiller());
        transfer.setToBiller(request.getToBiller());
        transfer.setFromWarehouse(request.getFromWarehouse());
        transfer.setToWarehouse(request.getToWarehouse());
        transfer.setTotal(total);
        transfer.setTotalTax(total_tax);
        transfer.setGrandTotal(grand_total);
        transfer.setAttachment(request.getAttachment());
        transfer.setNote(request.getNote());
        transfer.setUpdatedBy(userEntity.getUserId());
        transfer.setUpdatedAt(LocalDateTime.now());

        List<TransferItemEntity>  transferItems      = new ArrayList<>();
        List<StockMovementEntity> fromStockMovements = new ArrayList<>();
        List<StockMovementEntity> toStockMovements   = new ArrayList<>();
        for (TransferItemRequest itemRequest : request.getItems()) {
            ProductEntity product_details = productRepository.findById(itemRequest.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
            if (product_details.getStatus().equals(Constant.ActiveStatus.INACTIVE)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is not active.", HttpStatus.BAD_REQUEST);
            }
            if (!product_details.getType().equals(Constant.ProductType.STANDARD)) {
                throw new ApiException("The product: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") with type '" + product_details.getType() + "' is not allowed.", HttpStatus.BAD_REQUEST);
            }
            UnitEntity unit = unitRepository.findByUnitId(itemRequest.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (!utility.validProductUnit(itemRequest.getProductId(), itemRequest.getUnitId())) {
                throw new ApiException("Unit code '" + unit.getUnitCode() + "' of product '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is invalid.", HttpStatus.BAD_REQUEST);
            }
            double unitQuantity     = utility.formatQuantity(itemRequest.getUnitQuantity());
            double baseUnitQuantity = utility.formatQuantity(utility.convertToBaseUnitQuantity(itemRequest.getUnitId(), unitQuantity));
            double baseUnitCost     = utility.formatDecimal(utility.getAvgCost(itemRequest.getProductId()));
            double unitCost         = utility.formatDecimal(utility.convertToBaseUnitPrice(itemRequest.getUnitId(), baseUnitCost));
            TransferItemEntity item = TransferItemEntity.builder()
                    .fromWarehouse(transfer.getFromWarehouse())
                    .toWarehouse(transfer.getToWarehouse())
                    .productId(itemRequest.getProductId())
                    .unitId(itemRequest.getUnitId())
                    .unitQuantity(unitQuantity)
                    .quantity(baseUnitQuantity)
                    .unitCost(unitCost)
                    .baseUnitCost(baseUnitCost)
                    .expiry(itemRequest.getExpiry())
                    .build();

            StockMovementEntity fromStockMovement = new StockMovementEntity();
            fromStockMovement.setDate(request.getDate());
            fromStockMovement.setTransaction(Constant.StockTransactionType.TRANSFER);
            fromStockMovement.setWarehouseId(request.getFromWarehouse());
            fromStockMovement.setProductId(itemRequest.getProductId());
            fromStockMovement.setUnitId(itemRequest.getUnitId());
            fromStockMovement.setExpiry(itemRequest.getExpiry());
            fromStockMovement.setUnitQuantity(-1 * unitQuantity);
            fromStockMovement.setQuantity(-1 * baseUnitQuantity);
            fromStockMovement.setCost(baseUnitCost);

            StockMovementEntity toStockMovement = new StockMovementEntity();
            toStockMovement.setDate(request.getDate());
            toStockMovement.setTransaction(Constant.StockTransactionType.TRANSFER);
            toStockMovement.setWarehouseId(request.getToWarehouse());
            toStockMovement.setProductId(itemRequest.getProductId());
            toStockMovement.setUnitId(itemRequest.getUnitId());
            toStockMovement.setExpiry(itemRequest.getExpiry());
            toStockMovement.setUnitQuantity(unitQuantity);
            toStockMovement.setQuantity(baseUnitQuantity);
            toStockMovement.setCost(baseUnitCost);

            transferItems.add(item);
            fromStockMovements.add(fromStockMovement);
            toStockMovements.add(toStockMovement);

            total += (unitQuantity * unitCost);
        }
        transfer.setTotal(utility.formatDecimal(total));
        transfer.setTotalTax(utility.formatDecimal(total_tax));
        transfer.setGrandTotal(utility.formatDecimal(total + total_tax));
        if (status.equals(TransferStatus.COMPLETED)) {

        }
        try {
            Long id = transfer.getId();
            TransferEntity oldTransfer = transferRepository.findById(id).orElseThrow(() -> new ApiException("Transfer not found.", HttpStatus.BAD_REQUEST));
            List<StockMovementEntity> oldStockMovements = stockMovementRepository.findByTransactionAndTransactionId(Constant.StockTransactionType.TRANSFER, id);
            transferItemRepository.deleteByTransferId(id);
            stockMovementRepository.deleteByTransactionAndTransactionId(Constant.StockTransactionType.TRANSFER, id);
            transferRepository.save(transfer);
            transferItems.forEach(product -> product.setTransferId(id));
            fromStockMovements.forEach(stock -> stock.setTransactionId(id));
            toStockMovements.forEach(stock -> stock.setTransactionId(id));
            transferItemRepository.saveAll(transferItems);
            if (status.equals(TransferStatus.COMPLETED)) {
                stockMovementRepository.saveAll(fromStockMovements);
                stockMovementRepository.saveAll(toStockMovements);
                utility.checkOverstock(request.getFromWarehouse(), fromStockMovements);
            }
            if (oldTransfer.getStatus().equals(TransferStatus.COMPLETED)) {
                utility.checkOverstock(oldTransfer.getToWarehouse(), oldStockMovements);
            }
            if (!old_reference_no.equals(reference_no)) {
                utility.updateReferenceNo(request.getFromBiller(), Constant.ReferenceKey.TR, reference_no);
            }
            entityManager.flush();
            entityManager.clear();

            return getTransferById(id);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteTransfer(TransferDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteTransfer(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteTransfer(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Transfer deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteTransfer(Long id) {
        try {
            TransferEntity transfer = transferRepository.findById(id).orElseThrow(() -> new ApiException("Transfer not found.", HttpStatus.BAD_REQUEST));
            List<StockMovementEntity> stockMovements = stockMovementRepository.findByTransactionAndTransactionId(Constant.StockTransactionType.TRANSFER, id);
            utility.verifyAccess(new BillerWarehouseRequest(transfer.getFromBiller(), transfer.getFromWarehouse()));

            transferRepository.deleteById(id);
            transferItemRepository.deleteByTransferId(id);
            stockMovementRepository.deleteByTransactionAndTransactionId(Constant.StockTransactionType.TRANSFER, id);
            utility.checkOverstock(transfer.getToWarehouse(), stockMovements);
            return new BaseResponse("Transfer reference no. '" + transfer.getReferenceNo() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public TransferResponse importTransfer(TransferImportRequest request, HttpServletRequest servletRequest) {
        List<FileInfoResponse> savedFiles = new ArrayList<>();
        try {
            LocalDateTime date           = utility.convertToLocalDateTime(request.getDate());
            String referenceNo           = request.getReferenceNo();
            Long fromBiller              = request.getFromBiller();
            Long toBiller                = request.getToBiller();
            Long fromWarehouse           = request.getFromWarehouse();
            Long toWarehouse             = request.getToWarehouse();
            String attachment            = null;
            String note                  = request.getNote();
            String status                = request.getStatus();
            MultipartFile fileAttachment = request.getAttachment();
            MultipartFile fileItems      = request.getFile();
            List<TransferItemRequest> items = new ArrayList<>();
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
                    String unitCode     = utility.getCellValue(row.getCell(1), String.class);
                    Double unitQuantity = utility.getCellValue(row.getCell(2), Double.class);
                    LocalDate expiry    = utility.getCellValue(row.getCell(3), LocalDate.class);
                    if (productCode == null || productCode.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Product code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (unitCode == null || unitCode.isEmpty()) {
                        throw new ApiException("Row #" + (r + 1) + ": Unit code is required.", HttpStatus.BAD_REQUEST);
                    }
                    ProductEntity product = productRepository.findByProductCode(productCode).orElseThrow(() -> new ApiException("Row #" + (r + 1) + ": Product code '" + productCode + "' not found.", HttpStatus.BAD_REQUEST));
                    UnitEntity    unit    = unitRepository.findByUnitCode(unitCode).orElseThrow(() -> new ApiException("Row #" + (r + 1) + ": Unit code '" + unitCode + "' not found.", HttpStatus.BAD_REQUEST));
                    TransferItemRequest item = new TransferItemRequest();
                    item.setProductId(product.getProductId());
                    item.setUnitId(unit.getUnitId());
                    item.setUnitQuantity(unitQuantity);
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
            TransferCreateRequest transferCreateRequest = new TransferCreateRequest();
            transferCreateRequest.setDate(date);
            transferCreateRequest.setReferenceNo(referenceNo);
            transferCreateRequest.setFromBiller(fromBiller);
            transferCreateRequest.setToBiller(toBiller);
            transferCreateRequest.setFromWarehouse(fromWarehouse);
            transferCreateRequest.setToWarehouse(toWarehouse);
            transferCreateRequest.setAttachment(attachment);
            transferCreateRequest.setNote(note);
            transferCreateRequest.setStatus(status);
            transferCreateRequest.setItems(items);
            utility.validateRequest(transferCreateRequest);

            return createTransfer(transferCreateRequest);
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
