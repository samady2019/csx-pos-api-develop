package kh.com.csx.posapi.service;

import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.brand.*;
import kh.com.csx.posapi.dto.product.*;
import kh.com.csx.posapi.dto.setting.FileInfoResponse;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitRepository unitRepository;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final Utility utility;

    public ProductResponse getProductByID(Long productId, Long warehouseId) {
        ProductEntity product = productRepository.findByProductId(productId).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
        List<ProductResponse.ProductUnit> units = product.getProductUnits().stream()
                .map(unitEntity -> ProductResponse.ProductUnit.builder()
                        .productId(product.getProductId())
                        .unitId(unitEntity.getId().getUnitId())
                        .cost(unitEntity.getCost())
                        .price(unitEntity.getPrice())
                        .defaultUnit(unitEntity.getDefaultUnit())
                        .defaultSale(unitEntity.getDefaultSale())
                        .defaultPurchase(unitEntity.getDefaultPurchase())
                        .unit(unitRepository.findByUnitId(unitEntity.getId().getUnitId()).orElse(null))
                        .build())
                .collect(Collectors.toList());

        ProductResponse.Brand brandDetail = null;
        if (product.getBrandId() != null) {
            RetrieveBrandDTO retrieveBrandDTO = new RetrieveBrandDTO();
            retrieveBrandDTO.setBrandId(product.getBrandId());
            ResponseBrandDTO responseBrand = brandService.getBrandById(retrieveBrandDTO);
            brandDetail = ProductResponse.Brand.builder()
                    .brandId(responseBrand.getBrandId())
                    .name(responseBrand.getName())
                    .code(responseBrand.getCode())
                    .build();
        }
        ProductResponse.Category categoryDetail = null;
        if (product.getCategoryId() != null) {
            categoryDetail = categoryService.getCategoryDetails(product.getCategoryId());
        }
        return ProductResponse.builder()
                .productId(product.getProductId())
                .image(product.getImage())
                .productCode(product.getProductCode())
                .barCode(product.getBarCode())
                .productNameEn(product.getProductNameEn())
                .productNameKh(product.getProductNameKh())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .currency(product.getCurrency())
                .type(product.getType().toLowerCase())
                .status(product.getStatus())
                .taxMethod(product.getTaxMethod())
                .taxRateDeclare(product.getTaxRateDeclare())
                .stockType(product.getStockType())
                .alertQuantity(product.getAlertQuantity())
                .expiryAlertDays(product.getExpiryAlertDays())
                .description(product.getDescription())
                .productDetails(product.getProductDetails())
                .attachment(product.getAttachment())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .productUnits(units)
                .brand(brandDetail)
                .category(categoryDetail)
                .stock(utility.getStockDetailsByPID(productId, warehouseId))
                .build();
    }

    public ProductResponse getProductByID(Long productId) {
        return getProductByID(productId, null);
    }

    public ProductResponse getProductByCode(String productCode, Long warehouseId) {
        ProductEntity product = productRepository.findByProductCode(productCode.trim()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
        return getProductByID(product.getProductId(), warehouseId);
    }

    public ProductResponse getProductByCode(String productCode) {
        return getProductByCode(productCode, null);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        SettingEntity setting = utility.getSettings();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        if (productRepository.existsByProductCode(request.getProductCode().trim())) {
            throw new ApiException("Product code '" + request.getProductCode() + "' already exists.", HttpStatus.BAD_REQUEST);
        }
        if (productRepository.existsByBarCode(request.getBarCode().trim())) {
            throw new ApiException("Barcode '" + request.getBarCode() + "' already exists.", HttpStatus.BAD_REQUEST);
        }
        CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId()).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
        if (Constant.ActiveStatus.INACTIVE.equals(category.getStatus())) {
            throw new ApiException("Category is inactive.", HttpStatus.BAD_REQUEST);
        }
        if (request.getBrandId() != null) {
            BrandEntity brand = brandRepository.findByBrandId(request.getBrandId()).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
            if (Constant.ActiveStatus.INACTIVE.equals(brand.getStatus())) {
                throw new ApiException("Brand is inactive.", HttpStatus.BAD_REQUEST);
            }
        }
        ProductEntity product = new ProductEntity();
        String image = (request.getImage() != null && !request.getImage().trim().isEmpty()) ? request.getImage().trim() : Constant.PRODUCT_NO_IMAGE;
        product.setImage(image);
        product.setProductCode(request.getProductCode().trim());
        product.setBarCode(request.getBarCode().trim());
        product.setProductNameEn(request.getProductNameEn().trim());
        product.setProductNameKh(request.getProductNameKh() != null ? request.getProductNameKh().trim() : null);
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setCurrency(setting.getDefaultCurrency());
        product.setType(request.getType());
        product.setStatus(request.getStatus() != null ? request.getStatus() : Constant.ActiveStatus.DEFAULT);
        product.setTaxMethod(request.getTaxMethod() != null ? request.getTaxMethod() : Constant.TaxMethod.DEFAULT);
        product.setStockType(request.getStockType());
        product.setAlertQuantity(request.getAlertQuantity() != null ? request.getAlertQuantity() : 0);
        product.setExpiryAlertDays(request.getExpiryAlertDays() != null ? request.getExpiryAlertDays() : 0);
        product.setDescription(request.getDescription());
        product.setProductDetails(request.getProductDetails());
        product.setAttachment(request.getAttachment());
        product.setCreatedBy(userEntity.getUserId());
        product.setCreatedAt(LocalDateTime.now());
        ProductEntity productInserted = productRepository.save(product);

        List<ProductRequest.ProductUnit> units = request.getProductUnits();
        Set<ProductUnitEntity> productUnits = units.stream()
                .map(unit -> {
                    ProductUnitId productUnitId = new ProductUnitId(productInserted.getProductId(), unit.getUnitId());
                    return ProductUnitEntity.builder()
                            .id(productUnitId)
                            .cost(utility.formatDecimal(unit.getCost()))
                            .price(utility.formatDecimal(unit.getPrice()))
                            .defaultUnit(unit.getDefaultUnit())
                            .defaultSale(unit.getDefaultSale())
                            .defaultPurchase(unit.getDefaultPurchase())
                            .build();
                })
                .collect(Collectors.toSet());

        productInserted.setProductUnits(productUnits);
        productRepository.save(productInserted);

        return getProductByID(productInserted.getProductId());
    }

    @Transactional
    public ProductResponse updateProduct(ProductRequest request) {
        SettingEntity setting = utility.getSettings();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        ProductEntity product = productRepository.findById(request.getProductId()).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
        if (!product.getProductCode().equals(request.getProductCode().trim()) && productRepository.existsByProductCode(request.getProductCode().trim())) {
            throw new ApiException("Product code '" + request.getProductCode() + "' already exists.", HttpStatus.BAD_REQUEST);
        }
        if (!product.getBarCode().equals(request.getBarCode().trim()) && productRepository.existsByBarCode(request.getBarCode().trim())) {
            throw new ApiException("Barcode '" + request.getBarCode() + "' already exists.", HttpStatus.BAD_REQUEST);
        }
        CategoryEntity category = categoryRepository.findByCategoryId(request.getCategoryId()).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
        if (Constant.ActiveStatus.INACTIVE.equals(category.getStatus())) {
            throw new ApiException("Category is inactive.", HttpStatus.BAD_REQUEST);
        }
        if (request.getBrandId() != null) {
            BrandEntity brand = brandRepository.findByBrandId(request.getBrandId()).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
            if (Constant.ActiveStatus.INACTIVE.equals(brand.getStatus())) {
                throw new ApiException("Brand is inactive.", HttpStatus.BAD_REQUEST);
            }
        }
        String image = (request.getImage() != null && !request.getImage().trim().isEmpty()) ? request.getImage().trim() : Constant.PRODUCT_NO_IMAGE;
        product.setImage(image);
        product.setProductCode(request.getProductCode().trim());
        product.setBarCode(request.getBarCode().trim());
        product.setProductNameEn(request.getProductNameEn().trim());
        product.setProductNameKh(request.getProductNameKh() != null ? request.getProductNameKh().trim() : null);
        product.setCategoryId(request.getCategoryId());
        product.setBrandId(request.getBrandId());
        product.setCurrency(setting.getDefaultCurrency());
        product.setType(request.getType());
        product.setStatus(request.getStatus() != null ? request.getStatus() : product.getStatus());
        product.setTaxMethod(request.getTaxMethod() != null ? request.getTaxMethod() : product.getTaxMethod());
        product.setStockType(request.getStockType());
        product.setAlertQuantity(request.getAlertQuantity() != null ? request.getAlertQuantity() : 0);
        product.setExpiryAlertDays(request.getExpiryAlertDays() != null ? request.getExpiryAlertDays() : 0);
        product.setDescription(request.getDescription());
        product.setProductDetails(request.getProductDetails());
        product.setAttachment(request.getAttachment());
        product.setUpdatedBy(userEntity.getUserId());
        product.setUpdatedAt(LocalDateTime.now());

        List<ProductRequest.ProductUnit> units = request.getProductUnits();
        Set<ProductUnitEntity> productUnits = units.stream()
                .map(unit -> {
                    ProductUnitId productUnitId = new ProductUnitId(request.getProductId(), unit.getUnitId());
                    return ProductUnitEntity.builder()
                            .id(productUnitId)
                            .cost(utility.formatDecimal(unit.getCost()))
                            .price(utility.formatDecimal(unit.getPrice()))
                            .defaultUnit(unit.getDefaultUnit())
                            .defaultSale(unit.getDefaultSale())
                            .defaultPurchase(unit.getDefaultPurchase())
                            .build();
                })
                .collect(Collectors.toSet());

        UnitEntity baseUnit = utility.getProductBaseUnit(product.getProductId());
        for (ProductUnitEntity pu : productUnits) {
            UnitEntity u = unitRepository.findByUnitId(pu.getId().getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
            if (u.getPunitId() == null && !u.getUnitId().equals(baseUnit.getUnitId())) {
                if (productRepository.countReferences(product.getProductId()) > 0) {
                    throw new ApiException("Unable to change the unit for product '" + product.getProductNameEn() + "' because it is referenced in other records.", HttpStatus.BAD_REQUEST);
                }
            }
        }
        product.getProductUnits().clear();
        product.getProductUnits().addAll(productUnits);
        productRepository.save(product);

        return getProductByID(product.getProductId());
    }

    @Transactional
    public BaseResponse deleteProduct(ProductDeleteDTO request) {
        ID rs = ID.id(request.getProductId());
        try {
            if (rs.isSingle()) {
                return deleteProduct(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteProduct(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Product deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteProduct(Long id) {
        ProductEntity product = productRepository.findByProductId(id).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
        if (productRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete product '" + product.getProductNameEn() + " (" + product.getProductCode() + ")'. Product is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        productRepository.deleteById(id);
        return new BaseResponse("Product '" + product.getProductNameEn() + " (" + product.getProductCode() + ")' deleted successfully.");
    }

    public long countProducts() {
        return productRepository.count();
    }

    public List<ProductResponse> getListProducts() {
        List<ProductEntity> products = productRepository.findAll();
        return products.stream().map(product -> {
            List<ProductResponse.ProductUnit> units = product.getProductUnits().stream()
                    .map(unitEntity -> ProductResponse.ProductUnit.builder()
                            .productId(product.getProductId())
                            .unitId(unitEntity.getId().getUnitId())
                            .cost(unitEntity.getCost())
                            .price(unitEntity.getPrice())
                            .defaultUnit(unitEntity.getDefaultUnit())
                            .defaultSale(unitEntity.getDefaultSale())
                            .defaultPurchase(unitEntity.getDefaultPurchase())
                            .unit(unitRepository.findByUnitId(unitEntity.getId().getUnitId()).orElse(null))
                            .build())
                    .collect(Collectors.toList());

            ProductResponse.Brand brandDetail = null;
            if (product.getBrandId() != null) {
                try {
                    RetrieveBrandDTO retrieveBrandDTO = new RetrieveBrandDTO();
                    retrieveBrandDTO.setBrandId(product.getBrandId());
                    ResponseBrandDTO brandDetailDTO = brandService.getBrandById(retrieveBrandDTO);
                    if (brandDetailDTO != null) {
                        brandDetail = ProductResponse.Brand.builder()
                                .brandId(brandDetailDTO.getBrandId())
                                .name(brandDetailDTO.getName())
                                .code(brandDetailDTO.getCode())
                                .build();
                    }
                } catch (ApiException e) {
                    System.err.println("Error fetching brand details: " + e.getMessage());
                }
            }
            ProductResponse.Category categoryDetail = null;
            if (product.getCategoryId() != null) {
                try {
                    categoryDetail = categoryService.getCategoryDetails(product.getCategoryId());
                } catch (Exception e) {
                    System.err.println("Error fetching category details: " + e.getMessage());
                }
            }
            return ProductResponse.builder()
                    .productId(product.getProductId())
                    .image(product.getImage())
                    .productCode(product.getProductCode())
                    .barCode(product.getBarCode())
                    .productNameEn(product.getProductNameEn())
                    .productNameKh(product.getProductNameKh())
                    .categoryId(product.getCategoryId())
                    .brandId(product.getBrandId())
                    .currency(product.getCurrency())
                    .type(product.getType().toLowerCase())
                    .status(product.getStatus())
                    .taxMethod(product.getTaxMethod())
                    .stockType(product.getStockType())
                    .alertQuantity(product.getAlertQuantity())
                    .expiryAlertDays(product.getExpiryAlertDays())
                    .description(product.getDescription())
                    .productDetails(product.getProductDetails())
                    .attachment(product.getAttachment())
                    .createdBy(product.getCreatedBy())
                    .updatedBy(product.getUpdatedBy())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .productUnits(units)
                    .brand(brandDetail)
                    .category(categoryDetail)
                    .stock(utility.getStockDetailsByPID(product.getProductId()))
                    .build();
        }).collect(Collectors.toList());
    }

    public List<ProductResponse> getProductsTerm(ProductRetrieveDTO request) {
        if (request.getTerm() == null || request.getTerm().trim().isEmpty()) {
            throw new ApiException("No products found.", HttpStatus.BAD_REQUEST);
        }
        if (request.getInStockOnly() != null && request.getInStockOnly().equals(true) && request.getWarehouseId() == null) {
            throw new ApiException("Warehouse ID is required.", HttpStatus.BAD_REQUEST);
        }
        List<ProductEntity> products = productRepository.findByTerm(request.getTerm().trim(), request.getStandard(), request.getWarehouseId(), request.getInStockOnly());
        return products.stream().map(product -> {
            List<ProductResponse.ProductUnit> units = product.getProductUnits().stream()
                    .map(unitEntity -> ProductResponse.ProductUnit.builder()
                            .productId(product.getProductId())
                            .unitId(unitEntity.getId().getUnitId())
                            .cost(unitEntity.getCost())
                            .price(unitEntity.getPrice())
                            .defaultUnit(unitEntity.getDefaultUnit())
                            .defaultSale(unitEntity.getDefaultSale())
                            .defaultPurchase(unitEntity.getDefaultPurchase())
                            .unit(unitRepository.findByUnitId(unitEntity.getId().getUnitId()).orElse(null))
                            .build())
                    .collect(Collectors.toList());

            ProductResponse.Brand brandDetail = null;
            if (product.getBrandId() != null) {
                try {
                    RetrieveBrandDTO retrieveBrandDTO = new RetrieveBrandDTO();
                    retrieveBrandDTO.setBrandId(product.getBrandId());
                    ResponseBrandDTO brandDetailDTO = brandService.getBrandById(retrieveBrandDTO);
                    if (brandDetailDTO != null) {
                        brandDetail = ProductResponse.Brand.builder()
                                .brandId(brandDetailDTO.getBrandId())
                                .name(brandDetailDTO.getName())
                                .code(brandDetailDTO.getCode())
                                .build();
                    }
                } catch (ApiException e) {
                    System.err.println("Error fetching brand details: " + e.getMessage());
                }
            }
            ProductResponse.Category categoryDetail = null;
            if (product.getCategoryId() != null) {
                try {
                    categoryDetail = categoryService.getCategoryDetails(product.getCategoryId());
                } catch (Exception e) {
                    System.err.println("Error fetching category details: " + e.getMessage());
                }
            }
            return ProductResponse.builder()
                    .productId(product.getProductId())
                    .image(product.getImage())
                    .productCode(product.getProductCode())
                    .barCode(product.getBarCode())
                    .productNameEn(product.getProductNameEn())
                    .productNameKh(product.getProductNameKh())
                    .categoryId(product.getCategoryId())
                    .brandId(product.getBrandId())
                    .currency(product.getCurrency())
                    .type(product.getType().toLowerCase())
                    .status(product.getStatus())
                    .taxMethod(product.getTaxMethod())
                    .taxRateDeclare(product.getTaxRateDeclare())
                    .stockType(product.getStockType())
                    .alertQuantity(product.getAlertQuantity())
                    .expiryAlertDays(product.getExpiryAlertDays())
                    .description(product.getDescription())
                    .productDetails(product.getProductDetails())
                    .attachment(product.getAttachment())
                    .createdBy(product.getCreatedBy())
                    .updatedBy(product.getUpdatedBy())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .productUnits(units)
                    .brand(brandDetail)
                    .category(categoryDetail)
                    .stock(utility.getStockDetailsByPID(product.getProductId(), request.getWarehouseId()))
                    .build();
        }).collect(Collectors.toList());
    }

    public Page<ProductResponse> getProductsByBrandCategory(ProductRetrieveDTO request) {
        if (request.getInStockOnly() != null && request.getInStockOnly().equals(true) && request.getWarehouseId() == null) {
            throw new ApiException("Warehouse ID is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("productCode");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        PosSettingEntity posSetting = utility.getPosSettings();
        request.setSize(posSetting.getProductLimit());
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<ProductEntity> products = productRepository.findByBrandCategory(request.getCategoryId(), request.getBrandId(), request.getStandard(), request.getWarehouseId(), request.getInStockOnly(), pageable);
        return products.map(product -> {
            List<ProductResponse.ProductUnit> units = product.getProductUnits().stream()
                    .map(unitEntity -> ProductResponse.ProductUnit.builder()
                            .productId(product.getProductId())
                            .unitId(unitEntity.getId().getUnitId())
                            .cost(unitEntity.getCost())
                            .price(unitEntity.getPrice())
                            .defaultUnit(unitEntity.getDefaultUnit())
                            .defaultSale(unitEntity.getDefaultSale())
                            .defaultPurchase(unitEntity.getDefaultPurchase())
                            .unit(unitRepository.findByUnitId(unitEntity.getId().getUnitId()).orElse(null))
                            .build())
                    .collect(Collectors.toList());

            ProductResponse.Brand brandDetail = null;
            if (product.getBrandId() != null) {
                try {
                    RetrieveBrandDTO retrieveBrandDTO = new RetrieveBrandDTO();
                    retrieveBrandDTO.setBrandId(product.getBrandId());
                    ResponseBrandDTO brandDetailDTO = brandService.getBrandById(retrieveBrandDTO);
                    if (brandDetailDTO != null) {
                        brandDetail = ProductResponse.Brand.builder()
                                .brandId(brandDetailDTO.getBrandId())
                                .name(brandDetailDTO.getName())
                                .code(brandDetailDTO.getCode())
                                .build();
                    }
                } catch (ApiException e) {
                    System.err.println("Error fetching brand details: " + e.getMessage());
                }
            }
            ProductResponse.Category categoryDetail = null;
            if (product.getCategoryId() != null) {
                try {
                    categoryDetail = categoryService.getCategoryDetails(product.getCategoryId());
                } catch (Exception e) {
                    System.err.println("Error fetching category details: " + e.getMessage());
                }
            }
            return ProductResponse.builder()
                    .productId(product.getProductId())
                    .image(product.getImage())
                    .productCode(product.getProductCode())
                    .barCode(product.getBarCode())
                    .productNameEn(product.getProductNameEn())
                    .productNameKh(product.getProductNameKh())
                    .categoryId(product.getCategoryId())
                    .brandId(product.getBrandId())
                    .currency(product.getCurrency())
                    .type(product.getType().toLowerCase())
                    .status(product.getStatus())
                    .taxMethod(product.getTaxMethod())
                    .taxRateDeclare(product.getTaxRateDeclare())
                    .stockType(product.getStockType())
                    .alertQuantity(product.getAlertQuantity())
                    .expiryAlertDays(product.getExpiryAlertDays())
                    .description(product.getDescription())
                    .productDetails(product.getProductDetails())
                    .attachment(product.getAttachment())
                    .createdBy(product.getCreatedBy())
                    .updatedBy(product.getUpdatedBy())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .productUnits(units)
                    .brand(brandDetail)
                    .category(categoryDetail)
                    .stock(utility.getStockDetailsByPID(product.getProductId(), request.getWarehouseId()))
                    .build();
        });
    }

    public Page<ProductResponse> getAllProducts(ProductRetrieveDTO request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("productCode");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<ProductEntity> products = productRepository.findAllByFilter(request, pageable);
        return products.map(product -> {
            List<ProductResponse.ProductUnit> units = product.getProductUnits().stream()
                    .map(unitEntity -> ProductResponse.ProductUnit.builder()
                            .productId(product.getProductId())
                            .unitId(unitEntity.getId().getUnitId())
                            .cost(unitEntity.getCost())
                            .price(unitEntity.getPrice())
                            .defaultUnit(unitEntity.getDefaultUnit())
                            .defaultSale(unitEntity.getDefaultSale())
                            .defaultPurchase(unitEntity.getDefaultPurchase())
                            .unit(unitRepository.findByUnitId(unitEntity.getId().getUnitId()).orElse(null))
                            .build())
                    .collect(Collectors.toList());

            ProductResponse.Brand brandDetail = null;
            if (product.getBrandId() != null) {
                try {
                    RetrieveBrandDTO retrieveBrandDTO = new RetrieveBrandDTO();
                    retrieveBrandDTO.setBrandId(product.getBrandId());
                    ResponseBrandDTO brandDetailDTO = brandService.getBrandById(retrieveBrandDTO);
                    if (brandDetailDTO != null) {
                        brandDetail = ProductResponse.Brand.builder()
                                .brandId(brandDetailDTO.getBrandId())
                                .name(brandDetailDTO.getName())
                                .code(brandDetailDTO.getCode())
                                .build();
                    }
                } catch (ApiException e) {
                    System.err.println("Error fetching brand details: " + e.getMessage());
                }
            }
            ProductResponse.Category categoryDetail = null;
            if (product.getCategoryId() != null) {
                try {
                    categoryDetail = categoryService.getCategoryDetails(product.getCategoryId());
                } catch (Exception e) {
                    System.err.println("Error fetching category details: " + e.getMessage());
                }
            }
            return ProductResponse.builder()
                    .productId(product.getProductId())
                    .image(product.getImage())
                    .productCode(product.getProductCode())
                    .barCode(product.getBarCode())
                    .productNameEn(product.getProductNameEn())
                    .productNameKh(product.getProductNameKh())
                    .categoryId(product.getCategoryId())
                    .brandId(product.getBrandId())
                    .currency(product.getCurrency())
                    .type(product.getType().toLowerCase())
                    .status(product.getStatus())
                    .taxMethod(product.getTaxMethod())
                    .taxRateDeclare(product.getTaxRateDeclare())
                    .stockType(product.getStockType())
                    .alertQuantity(product.getAlertQuantity())
                    .expiryAlertDays(product.getExpiryAlertDays())
                    .description(product.getDescription())
                    .productDetails(product.getProductDetails())
                    .attachment(product.getAttachment())
                    .createdBy(product.getCreatedBy())
                    .updatedBy(product.getUpdatedBy())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .productUnits(units)
                    .brand(brandDetail)
                    .category(categoryDetail)
                    .stock(utility.getStockDetailsByPID(product.getProductId(), request.getWarehouseId()))
                    .build();
        });
    }

    @Transactional
    public Integer importProduct(ProductImportRequest request, HttpServletRequest servletRequest) {
        Integer r = null;
        List<FileInfoResponse> savedFiles = new ArrayList<>();
        try {
            MultipartFile file = request.getFile();
            if (file == null || file.isEmpty()) {
                throw new ApiException("File is required.", HttpStatus.BAD_REQUEST);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                throw new ApiException("Invalid file type. Only excel (.xlsx) file are allowed.", HttpStatus.BAD_REQUEST);
            }
            SettingEntity setting = utility.getSettings();
            try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    r = row.getRowNum();
                    String   image           = Constant.PRODUCT_NO_IMAGE;
                    String   type            = utility.getCellValue(row.getCell(1), String.class);
                    String   productCode     = utility.getCellValue(row.getCell(2), String.class);
                    String   barCode         = utility.getCellValue(row.getCell(3), String.class);
                    String   productNameEn   = utility.getCellValue(row.getCell(4), String.class);
                    String   productNameKh   = utility.getCellValue(row.getCell(5), String.class);
                    String  _categoryCode    = utility.getCellValue(row.getCell(6), String.class);
                    String  _brandCode       = utility.getCellValue(row.getCell(7), String.class);
                    String  _unitCode        = utility.getCellValue(row.getCell(8), String.class);
                    Double   cost            = utility.formatDecimal(utility.getCellValue(row.getCell(9), Double.class));
                    Double   price           = utility.formatDecimal(utility.getCellValue(row.getCell(10), Double.class));
                    String   currency        = setting.getDefaultCurrency();
                    String  _taxMethod       = utility.getCellValue(row.getCell(11), String.class);
                    String   stockType       = utility.getCellValue(row.getCell(12), String.class);
                    Double   alertQuantity   = utility.formatQuantity(utility.getCellValue(row.getCell(13), Double.class));
                    Integer  expiryAlertDays = utility.getCellValue(row.getCell(14), Integer.class);
                    String   description     = utility.getCellValue(row.getCell(15), String.class);
                    String   productDetails  = utility.getCellValue(row.getCell(16), String.class);
                    String   attachment      = null;
                    String  _status          = utility.getCellValue(row.getCell(18), String.class);

                    Long     categoryId      = null;
                    Long     brandId         = null;
                    Long     baseUnitId      = null;
                    Integer  taxMethod       = null;
                    Integer  status          = null;
                    List<ProductRequest.ProductUnit> productUnits = new ArrayList<>();
                    if (type != null && !type.isEmpty()) {
                        type = type.toLowerCase();
                    }
                    if (productCode == null || productCode.isEmpty()) {
                        throw new ApiException("Product code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (_categoryCode != null && !_categoryCode.isEmpty()) {
                        CategoryEntity category = categoryRepository.findByCode(_categoryCode).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
                        categoryId = category.getCategoryId();
                    }
                    if (_brandCode != null && !_brandCode.isEmpty()) {
                        BrandEntity brand = brandRepository.findByCode(_brandCode).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
                        brandId = brand.getBrandId();
                    }
                    if (_unitCode != null && !_unitCode.isEmpty()) {
                        UnitEntity unit = unitRepository.findByUnitCode(_unitCode).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
                        if (unit.getPunitId() != null) {
                            throw new ApiException("Invalid base unit '" + unit.getUnitCode() + "'.", HttpStatus.BAD_REQUEST);
                        }
                        baseUnitId = unit.getUnitId();
                        List<UnitEntity> unitEntities = unitRepository.findByPunitId(baseUnitId);
                        unitEntities.add(unit);
                        for (UnitEntity unitEntity : unitEntities) {
                            ProductRequest.ProductUnit productUnit = new ProductRequest.ProductUnit();
                            productUnit.setUnitId(unitEntity.getUnitId());
                            productUnit.setCost(utility.convertFromBaseUnitPrice(unitEntity.getUnitId(), cost));
                            productUnit.setPrice(utility.convertFromBaseUnitPrice(unitEntity.getUnitId(), price));
                            if (unitEntity.getUnitId().equals(baseUnitId)) {
                                productUnit.setDefaultUnit(Constant.YES);
                                productUnit.setDefaultSale(Constant.YES);
                                productUnit.setDefaultPurchase(Constant.YES);
                            }
                            productUnits.add(productUnit);
                        }
                        productUnits.sort(Comparator.comparing(ProductRequest.ProductUnit::getCost));
                    }
                    if (_taxMethod == null || _taxMethod.isEmpty()) {
                        taxMethod = Constant.TaxMethod.DEFAULT;
                    } else {
                        _taxMethod = _taxMethod.toLowerCase();
                        if (Constant.TaxMethod.VALID_KEY.containsKey(_taxMethod)) {
                            taxMethod = Constant.TaxMethod.VALID_KEY.get(_taxMethod);
                        } else {
                            throw new ApiException(Constant.TaxMethod.NOTE, HttpStatus.BAD_REQUEST);
                        }
                    }
                    if (_status == null || _status.isEmpty()) {
                        status = Constant.ActiveStatus.DEFAULT;
                    } else {
                        _status = _status.toLowerCase();
                        if (Constant.ActiveStatus.VALID_KEY.containsKey(_status)) {
                            status = Constant.ActiveStatus.VALID_KEY.get(_status);
                        } else {
                            throw new ApiException(Constant.ActiveStatus.NOTE, HttpStatus.BAD_REQUEST);
                        }
                    }
                    byte[] imageBytes = utility.getExcelImageByRow(sheet, r, 0);
                    String fileNameImage = productCode + ".png";
                    MultipartFile imageFile = (imageBytes != null) ? utility.convertBytesToMultipartFile(imageBytes, fileNameImage) : null;
                    if (imageFile != null) {
                        List<FileInfoResponse> fileInfoImage = utility.uploadFile(Constant.Directory.INVENTORY, imageFile, servletRequest);
                        image = fileInfoImage.get(0).getFileName();
                        savedFiles.addAll(fileInfoImage);
                    }
                    byte[] attachmentBytes = utility.getExcelImageByRow(sheet, r, 17);
                    String fileNameAttachment = productCode + ".png";
                    MultipartFile attachmentFile = (attachmentBytes != null) ? utility.convertBytesToMultipartFile(attachmentBytes, fileNameAttachment) : null;
                    if (attachmentFile != null) {
                        List<FileInfoResponse> fileInfoAttachment = utility.uploadFile(Constant.Directory.INVENTORY, attachmentFile, servletRequest);
                        attachment = fileInfoAttachment.get(0).getFileName();
                        savedFiles.addAll(fileInfoAttachment);
                    }
                    ProductRequest product = new ProductRequest();
                    product.setImage(image);
                    product.setProductCode(productCode);
                    product.setBarCode(barCode);
                    product.setProductNameEn(productNameEn);
                    product.setProductNameKh(productNameKh);
                    product.setCategoryId(categoryId);
                    product.setBrandId(brandId);
                    product.setCurrency(currency);
                    product.setType(type);
                    product.setStatus(status);
                    product.setTaxMethod(taxMethod);
                    product.setStockType(stockType);
                    product.setAlertQuantity(alertQuantity);
                    product.setExpiryAlertDays(expiryAlertDays);
                    product.setDescription(description);
                    product.setProductDetails(productDetails);
                    product.setAttachment(attachment);
                    product.setProductUnits(productUnits);
                    utility.validateRequest(product);

                    createProduct(product);
                }
            }
            if (r == null || r == 0) {
                throw new ApiException("Employee must contain at least one item.", HttpStatus.BAD_REQUEST);
            }
            return r;
        } catch (Exception e) {
            for (FileInfoResponse savedFile : savedFiles) {
                try {
                    Path path = Paths.get(savedFile.getFilePath()).toAbsolutePath().normalize();
                    Files.delete(path);
                } catch (IOException ioException) {
                    System.out.println("Failed to rollback file: " + savedFile + ", " + ioException.getMessage());
                }
            }
            String msg = e.getMessage();
            if (r != null && r != 0) {
                msg = "Row #" + (r + 1) + ": " + msg;
            }
            throw new ApiException(msg, HttpStatus.BAD_REQUEST);
        }
    }
}
