package kh.com.csx.posapi.service;

import jakarta.persistence.EntityManager;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.FileExtension;
import kh.com.csx.posapi.constant.Constant.StockCountType;
import kh.com.csx.posapi.constant.Constant.StockCountStatus;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.stockCount.*;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;

@Service
@RequiredArgsConstructor
public class StockCountService {
    @Autowired
    private EntityManager entityManager;

    private final StockCountRepository stockCountRepository;
    private final StockCountItemRepository stockCountItemRepository;
    private final BillerRepository billerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    private final ProductService productService;
    private final Utility utility;

    public StockCountResponse getStockCountById(Long id) {
        StockCountEntity data = stockCountRepository.findById(id).orElseThrow(() -> new ApiException("Stock count not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);
        data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
        return StockCountResponse.builder().stockCount(data).build();
    }

    public Page<StockCountResponse> getAllStockCounts(StockCountRetrieveRequest request) {
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
        Page<StockCountEntity> stockCountEntities = stockCountRepository.findAllByFilter(request, pageable);
        return stockCountEntities.map(
            data -> {
                data.getItems().forEach(item -> item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId())));
                return StockCountResponse.builder().stockCount(data).build();
            }
        );
    }

    public StockCountResponse getInitStockCountById(Long id) {
        StockCountEntity data = stockCountRepository.findById(id).orElseThrow(() -> new ApiException("Stock count not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(data);

        data.setFinalFile(null);
        data.setDifferences(null);
        data.setMatches(null);
        data.setMissing(null);
        data.setStatus(StockCountStatus.PENDING);
        List<StockCountItemEntity> items = data.getItems();
        items.forEach(
            item -> {
                item.setCounted(0.0);
                item.setStatus(0);
                item.setProduct(productService.getProductByID(item.getProductId(), item.getWarehouseId()));
            }
        );
        data.setItems(items);
        return StockCountResponse.builder().stockCount(data).build();
    }

    @Transactional
    public StockCountResponse initializeStockCount(StockCountCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        utility.verifyAccess(request);

        if (!StockCountType.VALID.contains(request.getType().trim())) {
            throw new ApiException("Invalid type. " + StockCountType.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (request.getType().trim().equals(StockCountType.FULL)) {
            request.setBrands(null);
            request.setCategories(null);
        } else {
            if ((request.getBrands() == null || request.getBrands().trim().isEmpty()) && (request.getCategories() == null || request.getCategories().trim().isEmpty())) {
                throw new ApiException("Brand or Category IDs is required.", HttpStatus.BAD_REQUEST);
            } else {
                if (request.getBrands() != null && !request.getBrands().trim().isEmpty()) {
                    try {
                        StringBuilder strIds = new StringBuilder();
                        List<Long> brandIds = Arrays.stream(request.getBrands().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                        for (Long brandId : brandIds) {
                            brandRepository.findById(brandId).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
                            if (!strIds.isEmpty()) {
                                strIds.append(",");
                            }
                            strIds.append(brandId.toString());
                        }
                        request.setBrands(strIds.toString());
                    } catch (Exception e) {
                        throw new ApiException("Invalid input brand IDs.", HttpStatus.BAD_REQUEST);
                    }
                }
                if (request.getCategories() != null && !request.getCategories().trim().isEmpty()) {
                    try {
                        StringBuilder strIds = new StringBuilder();
                        List<Long> categoryIds = Arrays.stream(request.getCategories().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                        for (Long categoryId : categoryIds) {
                            categoryRepository.findById(categoryId).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
                            if (!strIds.isEmpty()) {
                                strIds.append(",");
                            }
                            strIds.append(categoryId.toString());
                        }
                        request.setCategories(strIds.toString());
                    } catch (Exception e) {
                        throw new ApiException("Invalid input category IDs.", HttpStatus.BAD_REQUEST);
                    }
                }
                if ((request.getBrands() == null || request.getBrands().trim().isEmpty()) && (request.getCategories() == null || request.getCategories().trim().isEmpty())) {
                    throw new ApiException("Brand or Category IDs is required.", HttpStatus.BAD_REQUEST);
                }
            }
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        LocalDateTime date          = LocalDateTime.now();
        Long   biller_id            = request.getBillerId();
        Long   warehouse_id         = request.getWarehouseId();
        Long   user_id              = user.getUserId();
        String reference_no         = utility.checkReferenceNo(biller_id, Constant.ReferenceKey.SC, request.getReferenceNo().trim());
        String brands               = (request.getBrands() != null && !request.getBrands().trim().isEmpty()) ? request.getBrands() : null;
        String categories           = (request.getCategories() != null && !request.getCategories().trim().isEmpty()) ? request.getCategories() : null;
        String type                 = request.getType().trim();
        String status               = StockCountStatus.PENDING;

        StockCountEntity stockCount = new StockCountEntity();
        stockCount.setDate(date);
        stockCount.setReferenceNo(reference_no);
        stockCount.setBillerId(biller_id);
        stockCount.setWarehouseId(warehouse_id);
        stockCount.setType(type);
        stockCount.setBrands(brands);
        stockCount.setCategories(categories);
        stockCount.setAttachment(request.getAttachment());
        stockCount.setNote(request.getNote());
        stockCount.setStatus(status);
        stockCount.setCreatedBy(user_id);
        stockCount.setCreatedAt(LocalDateTime.now());

        List<StockCountItemEntity> stockCountItems = new ArrayList<>();
        List<StockCountItem> stocks = utility.getStockCountGrpExp(warehouse_id, null, brands, categories, null);
        if (!stocks.isEmpty()) {
            File directory = new File(utility.uploadDocPath + File.separator + Constant.Directory.INVENTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filename = utility.generateRandomFilename("", FileExtension.EXCEL);
            File file = new File(directory, filename);
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Stock Count");
                Row header  = sheet.createRow(0);
                header.createCell(utility.colIdx("A")).setCellValue("Product Code");
                header.createCell(utility.colIdx("B")).setCellValue("Product Name");
                header.createCell(utility.colIdx("C")).setCellValue("Expected");
                header.createCell(utility.colIdx("D")).setCellValue("Counted");
                header.createCell(utility.colIdx("E")).setCellValue("Expiry");
                int rowNum = 1;
                for (StockCountItem stock : stocks) {
                    ProductEntity product = utility.getProductDetails(stock.getProductId());
                    StockCountItemEntity item = new StockCountItemEntity();
                    item.setWarehouseId(warehouse_id);
                    item.setProductId(stock.getProductId());
                    item.setExpiry(stock.getExpiry());
                    item.setExpected(utility.formatQuantity(stock.getQuantity()));
                    item.setCounted(0.0);
                    item.setCost(utility.formatDecimal(utility.getAvgCost(stock.getProductId())));
                    item.setStatus(Constant.NO);
                    stockCountItems.add(item);

                    Row row = sheet.createRow(rowNum++);
                    row.createCell(utility.colIdx("A")).setCellValue(product.getProductCode());
                    row.createCell(utility.colIdx("B")).setCellValue(product.getProductNameEn());
                    row.createCell(utility.colIdx("C")).setCellValue(utility.formatQuantity(stock.getQuantity()));
                    row.createCell(utility.colIdx("D")).setCellValue(0);
                    row.createCell(utility.colIdx("E")).setCellValue(stock.getExpiry() != null ? stock.getExpiry().toString() : "");
                }
                sheet.setColumnWidth(utility.colIdx("A"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("B"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("C"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("D"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("E"), 20 * 256);
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                stockCount.setInitialFile(filename);
            } catch (IOException e) {
                throw new ApiException("Failed to generate Excel file.", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new ApiException("Stock count is empty.", HttpStatus.BAD_REQUEST);
        }
        stockCount.setTotalItems(stockCountItems.size());
        try {
            StockCountEntity data = stockCountRepository.save(stockCount);
            stockCountItems.forEach(item -> item.setStockCountId(data.getId()));
            stockCountItemRepository.saveAll(stockCountItems);
            utility.updateReferenceNo(biller_id, Constant.ReferenceKey.SC, reference_no);
            entityManager.flush();
            entityManager.clear();

            return getInitStockCountById(data.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public StockCountResponse finalizeStockCount(StockCountFinalRequest request) {
        Long stock_count_id = request.getId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        StockCountEntity stockCount = stockCountRepository.findById(stock_count_id).orElseThrow(() -> new ApiException("Stock count not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(stockCount);

        if (stockCount.getStatus().equals(StockCountStatus.COMPLETED)) {
            throw new ApiException("Unable to finalize stock count: The stock count '" + stockCount.getReferenceNo() + "' has already been processed.", HttpStatus.BAD_REQUEST);
        }
        for (StockCountItemRequest stock : request.getItems()) {
            if (stock.getProductId() == null) {
                throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
            }
            if (stock.getCounted() == null) {
                throw new ApiException("Counted quantity is required.", HttpStatus.BAD_REQUEST);
            }
            Long product_id  = stock.getProductId();
            LocalDate expiry = stock.getExpiry();
            Double counted   = utility.formatQuantity(stock.getCounted());
            StockCountItemEntity stockCountItem = stockCountItemRepository.findByStockCountIdPIDExp(stock_count_id, product_id, expiry).orElseThrow(() -> new ApiException("Stock count item id: " + product_id + " not found.", HttpStatus.BAD_REQUEST));

            stockCountItem.setCounted(counted);
            stockCountItem.setStatus(1);
            stockCountItemRepository.save(stockCountItem);
        }
        List<StockCountItemEntity> stocks = stockCountItemRepository.findByStockCountId(stock_count_id);
        File directory = new File(utility.uploadDocPath + File.separator + Constant.Directory.INVENTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String filename = utility.generateRandomFilename("", FileExtension.EXCEL);
        File file = new File(directory, filename);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stock Count");
            Row header  = sheet.createRow(0);
            header.createCell(utility.colIdx("A")).setCellValue("Product Code");
            header.createCell(utility.colIdx("B")).setCellValue("Product Name");
            header.createCell(utility.colIdx("C")).setCellValue("Expected");
            header.createCell(utility.colIdx("D")).setCellValue("Counted");
            header.createCell(utility.colIdx("E")).setCellValue("Expiry");
            int rowNum = 1;
            for (StockCountItemEntity stock : stocks) {
                ProductEntity product = utility.getProductDetails(stock.getProductId());
                Row row = sheet.createRow(rowNum++);
                row.createCell(utility.colIdx("A")).setCellValue(product.getProductCode());
                row.createCell(utility.colIdx("B")).setCellValue(product.getProductNameEn());
                row.createCell(utility.colIdx("C")).setCellValue(utility.formatQuantity(stock.getExpected()));
                row.createCell(utility.colIdx("D")).setCellValue(utility.formatQuantity(stock.getCounted()));
                row.createCell(utility.colIdx("E")).setCellValue(stock.getExpiry() != null ? stock.getExpiry().toString() : "");
            }
            sheet.setColumnWidth(utility.colIdx("A"), 20 * 256);
            sheet.setColumnWidth(utility.colIdx("B"), 20 * 256);
            sheet.setColumnWidth(utility.colIdx("C"), 20 * 256);
            sheet.setColumnWidth(utility.colIdx("D"), 20 * 256);
            sheet.setColumnWidth(utility.colIdx("E"), 20 * 256);
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            stockCount.setFinalFile(filename);
        } catch (IOException e) {
            throw new ApiException("Failed to generate Excel file.", HttpStatus.BAD_REQUEST);
        }
        StockCountSummary summary = stockCountItemRepository.getStockCountSummaryById(stock_count_id);
        stockCount.setDifferences(summary.getDifferences());
        stockCount.setMatches(summary.getMatches());
        stockCount.setMissing(summary.getMissing());
        stockCount.setStatus(StockCountStatus.COMPLETED);
        stockCount.setUpdatedBy(user.getUserId());
        stockCount.setUpdatedAt(LocalDateTime.now());
        try {
            stockCountRepository.save(stockCount);
            entityManager.flush();
            entityManager.clear();

            return getStockCountById(stock_count_id);
        }  catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public StockCountResponse updateStockCount(StockCountUpdateRequest request) {
        Long stock_count_id = request.getId();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        StockCountEntity stockCount = stockCountRepository.findById(stock_count_id).orElseThrow(() -> new ApiException("Stock count not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(stockCount);

        if (stockCount.getStatus().equals(StockCountStatus.COMPLETED)) {
            throw new ApiException("Unable to edit stock count: The stock count '" + stockCount.getReferenceNo() + "' has already been processed.", HttpStatus.BAD_REQUEST);
        }
        if (!StockCountType.VALID.contains(request.getType().trim())) {
            throw new ApiException("Invalid type. " + StockCountType.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (request.getType().trim().equals(StockCountType.FULL)) {
            request.setBrands(null);
            request.setCategories(null);
        } else {
            if ((request.getBrands() == null || request.getBrands().trim().isEmpty()) && (request.getCategories() == null || request.getCategories().trim().isEmpty())) {
                throw new ApiException("Brand or Category IDs is required.", HttpStatus.BAD_REQUEST);
            } else {
                if (request.getBrands() != null && !request.getBrands().trim().isEmpty()) {
                    try {
                        StringBuilder strIds = new StringBuilder();
                        List<Long> brandIds = Arrays.stream(request.getBrands().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                        for (Long brandId : brandIds) {
                            brandRepository.findById(brandId).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
                            if (!strIds.isEmpty()) {
                                strIds.append(",");
                            }
                            strIds.append(brandId.toString());
                        }
                        request.setBrands(strIds.toString());
                    } catch (Exception e) {
                        throw new ApiException("Invalid input brand IDs.", HttpStatus.BAD_REQUEST);
                    }
                }
                if (request.getCategories() != null && !request.getCategories().trim().isEmpty()) {
                    try {
                        StringBuilder strIds = new StringBuilder();
                        List<Long> categoryIds = Arrays.stream(request.getCategories().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                        for (Long categoryId : categoryIds) {
                            categoryRepository.findById(categoryId).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
                            if (!strIds.isEmpty()) {
                                strIds.append(",");
                            }
                            strIds.append(categoryId.toString());
                        }
                        request.setCategories(strIds.toString());
                    } catch (Exception e) {
                        throw new ApiException("Invalid input category IDs.", HttpStatus.BAD_REQUEST);
                    }
                }
                if ((request.getBrands() == null || request.getBrands().trim().isEmpty()) && (request.getCategories() == null || request.getCategories().trim().isEmpty())) {
                    throw new ApiException("Brand or Category IDs is required.", HttpStatus.BAD_REQUEST);
                }
            }
        }
        billerRepository.findById(request.getBillerId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getWarehouseId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        if (stockCountRepository.existsByReferenceNoAndIdNot(request.getReferenceNo(), stockCount.getId())) {
            throw new ApiException("Reference number already exists.", HttpStatus.BAD_REQUEST);
        }
        LocalDateTime date          = LocalDateTime.now();
        String reference_no         = request.getReferenceNo();
        String old_reference_no     = stockCount.getReferenceNo();
        Long   biller_id            = request.getBillerId();
        Long   warehouse_id         = request.getWarehouseId();
        Long   user_id              = user.getUserId();
        String brands               = (request.getBrands() != null && !request.getBrands().trim().isEmpty()) ? request.getBrands() : null;
        String categories           = (request.getCategories() != null && !request.getCategories().trim().isEmpty()) ? request.getCategories() : null;
        String type                 = request.getType().trim();

        stockCount.setDate(date);
        stockCount.setReferenceNo(reference_no);
        stockCount.setBillerId(biller_id);
        stockCount.setWarehouseId(warehouse_id);
        stockCount.setType(type);
        stockCount.setBrands(brands);
        stockCount.setCategories(categories);
        stockCount.setAttachment(request.getAttachment());
        stockCount.setNote(request.getNote());
        stockCount.setUpdatedBy(user_id);
        stockCount.setUpdatedAt(LocalDateTime.now());

        List<StockCountItemEntity> stockCountItems = new ArrayList<>();
        List<StockCountItem> stocks = utility.getStockCountGrpExp(warehouse_id, null, brands, categories, null);
        if (!stocks.isEmpty()) {
            File directory = new File(utility.uploadDocPath + File.separator + Constant.Directory.INVENTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filename = utility.generateRandomFilename("", FileExtension.EXCEL);
            File file = new File(directory, filename);
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Stock Count");
                Row header  = sheet.createRow(0);
                header.createCell(utility.colIdx("A")).setCellValue("Product Code");
                header.createCell(utility.colIdx("B")).setCellValue("Product Name");
                header.createCell(utility.colIdx("C")).setCellValue("Expected");
                header.createCell(utility.colIdx("D")).setCellValue("Counted");
                header.createCell(utility.colIdx("E")).setCellValue("Expiry");
                int rowNum = 1;
                for (StockCountItem stock : stocks) {
                    ProductEntity product = utility.getProductDetails(stock.getProductId());
                    StockCountItemEntity item = new StockCountItemEntity();
                    item.setWarehouseId(warehouse_id);
                    item.setProductId(stock.getProductId());
                    item.setExpiry(stock.getExpiry());
                    item.setExpected(utility.formatQuantity(stock.getQuantity()));
                    item.setCounted(0.0);
                    item.setCost(utility.formatDecimal(utility.getAvgCost(stock.getProductId())));
                    item.setStatus(Constant.NO);
                    stockCountItems.add(item);

                    Row row = sheet.createRow(rowNum++);
                    row.createCell(utility.colIdx("A")).setCellValue(product.getProductCode());
                    row.createCell(utility.colIdx("B")).setCellValue(product.getProductNameEn());
                    row.createCell(utility.colIdx("C")).setCellValue(utility.formatQuantity(stock.getQuantity()));
                    row.createCell(utility.colIdx("D")).setCellValue(0);
                    row.createCell(utility.colIdx("E")).setCellValue(stock.getExpiry() != null ? stock.getExpiry().toString() : "");
                }
                sheet.setColumnWidth(utility.colIdx("A"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("B"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("C"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("D"), 20 * 256);
                sheet.setColumnWidth(utility.colIdx("E"), 20 * 256);
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                stockCount.setInitialFile(filename);
            } catch (IOException e) {
                throw new ApiException("Failed to generate Excel file.", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new ApiException("Stock count is empty.", HttpStatus.BAD_REQUEST);
        }
        stockCount.setTotalItems(stockCountItems.size());
        try {
            StockCountEntity data = stockCountRepository.save(stockCount);
            stockCountItemRepository.deleteByStockCountId(stock_count_id);
            stockCountItems.forEach(item -> item.setStockCountId(data.getId()));
            stockCountItemRepository.saveAll(stockCountItems);
            if (!old_reference_no.equals(reference_no)) {
                utility.updateReferenceNo(biller_id, Constant.ReferenceKey.SC, reference_no);
            }
            entityManager.flush();
            entityManager.clear();

            return getInitStockCountById(data.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteStockCount(StockCountDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteStockCount(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteStockCount(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Stock count deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteStockCount(Long id) {
        StockCountEntity stockCount = stockCountRepository.findById(id).orElseThrow(() -> new ApiException("Stock count not found.", HttpStatus.BAD_REQUEST));
        utility.verifyAccess(stockCount);
        if (stockCount.getStatus().equals(StockCountStatus.COMPLETED)) {
            throw new ApiException("Unable to delete stock count reference no. '" + stockCount.getReferenceNo() + "'. The stock count has already been processed.", HttpStatus.BAD_REQUEST);
        }
        try {
            stockCountItemRepository.deleteByStockCountId(id);
            stockCountRepository.delete(stockCount);
            return new BaseResponse("Stock count reference no. '" + stockCount.getReferenceNo() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
