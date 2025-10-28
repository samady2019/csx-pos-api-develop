package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.category.*;
import kh.com.csx.posapi.dto.product.ProductResponse;
import kh.com.csx.posapi.entity.CategoryEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.CategoryRepository;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final Utility utility;

    public CategoryResponse createCategory(CategoryCreateRequest request) {
        CategoryEntity category = new CategoryEntity();
        if (categoryRepository.existsByCode(request.getCode().trim())) {
            throw new ApiException("Category code already exists.", HttpStatus.BAD_REQUEST);
        }
        if (request.getParentId() != null) {
            CategoryEntity parentCategory = categoryRepository.findByCategoryId(request.getParentId()).orElseThrow(() -> new ApiException("Parent category not found.", HttpStatus.BAD_REQUEST));
            if (Constant.ActiveStatus.INACTIVE.equals(parentCategory.getStatus())) {
                throw new ApiException("Parent category is inactive.", HttpStatus.BAD_REQUEST);
            }
            category.setParentCategory(parentCategory);
            category.setPCategoryId(parentCategory.getCategoryId());
        }
        if (request.getStatus() == null) {
            category.setStatus(Constant.ActiveStatus.DEFAULT);
        } else {
            if (!Constant.ActiveStatus.VALID_STATUSES.contains(request.getStatus())) {
                throw new ApiException(Constant.ActiveStatus.NOTE, HttpStatus.BAD_REQUEST);
            }
            category.setStatus(request.getStatus());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        category.setCode(request.getCode().trim());
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setCreatedBy(userEntity.getUserId());
        category.setCreatedAt(LocalDateTime.now());
        try {
            CategoryEntity savedCategory = categoryRepository.saveAndFlush(category);
            CategoryResponse.ParentCategoryResponse parentCategoryResponse = null;
            if (savedCategory.getParentCategory() != null) {
                CategoryEntity parentCategory = savedCategory.getParentCategory();
                parentCategoryResponse = CategoryResponse.ParentCategoryResponse.builder()
                        .categoryId(parentCategory.getCategoryId())
                        .code(parentCategory.getCode())
                        .name(parentCategory.getName())
                        .description(parentCategory.getDescription())
                        .status(parentCategory.getStatus())
                        .createdBy(parentCategory.getCreatedBy())
                        .updatedBy(parentCategory.getUpdatedBy())
                        .createdAt(parentCategory.getCreatedAt())
                        .updatedAt(parentCategory.getUpdatedAt())
                        .build();
            }
            return CategoryResponse.builder()
                    .categoryId(savedCategory.getCategoryId())
                    .code(savedCategory.getCode())
                    .name(savedCategory.getName())
                    .description(savedCategory.getDescription())
                    .status(savedCategory.getStatus())
                    .createdBy(savedCategory.getCreatedBy())
                    .createdAt(savedCategory.getCreatedAt())
                    .parentCategory(parentCategoryResponse)
                    .build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public CategoryResponse updateCategory(CategoryUpdateRequest request) {
        CategoryEntity existingCategory = categoryRepository.findByCategoryId(request.getCategoryId()).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
        if (categoryRepository.existsByCodeAndCategoryIdNot(request.getCode().trim(), existingCategory.getCategoryId())) {
            throw new ApiException("Category code already exists.", HttpStatus.BAD_REQUEST);
        }
        if (request.getParentId() != null) {
            CategoryEntity parentCategory = categoryRepository.findByCategoryId(request.getParentId()).orElseThrow(() -> new ApiException("Parent category not found.", HttpStatus.BAD_REQUEST));
            if (Constant.ActiveStatus.INACTIVE.equals(parentCategory.getStatus())) {
                throw new ApiException("Parent category is inactive.", HttpStatus.BAD_REQUEST);
            }
            existingCategory.setParentCategory(parentCategory);
            existingCategory.setPCategoryId(parentCategory.getCategoryId());
        } else {
            existingCategory.setParentCategory(null);
            existingCategory.setPCategoryId(null);
        }
        if (request.getStatus() != null) {
            if (!Constant.ActiveStatus.VALID_STATUSES.contains(request.getStatus())) {
                throw new ApiException(Constant.ActiveStatus.NOTE, HttpStatus.BAD_REQUEST);
            }
            existingCategory.setStatus(request.getStatus());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        existingCategory.setCode(request.getCode().trim());
        existingCategory.setName(request.getName().trim());
        existingCategory.setDescription(request.getDescription());
        existingCategory.setUpdatedBy(userEntity.getUserId());
        existingCategory.setUpdatedAt(LocalDateTime.now());
        try {
            CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
            CategoryResponse.ParentCategoryResponse parentCategoryResponse = null;
            if (updatedCategory.getParentCategory() != null) {
                CategoryEntity parentCategory = updatedCategory.getParentCategory();
                parentCategoryResponse = CategoryResponse.ParentCategoryResponse.builder()
                        .categoryId(parentCategory.getCategoryId())
                        .code(parentCategory.getCode())
                        .name(parentCategory.getName())
                        .description(parentCategory.getDescription())
                        .status(parentCategory.getStatus())
                        .createdBy(parentCategory.getCreatedBy())
                        .updatedBy(parentCategory.getUpdatedBy())
                        .createdAt(parentCategory.getCreatedAt())
                        .updatedAt(parentCategory.getUpdatedAt())
                        .build();
            }
            return CategoryResponse.builder()
                    .categoryId(updatedCategory.getCategoryId())
                    .code(updatedCategory.getCode())
                    .name(updatedCategory.getName())
                    .description(updatedCategory.getDescription())
                    .status(updatedCategory.getStatus())
                    .createdBy(updatedCategory.getCreatedBy())
                    .updatedBy(updatedCategory.getUpdatedBy())
                    .createdAt(updatedCategory.getCreatedAt())
                    .updatedAt(updatedCategory.getUpdatedAt())
                    .parentCategory(parentCategoryResponse)
                    .build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteCategory(CategoryDeleteRequest request) {
        ID rs = ID.id(request.getCategoryId());
        try {
            if (rs.isSingle()) {
                return deleteCategory(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteCategory(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Category deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findByCategoryId(id).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
        if (categoryRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete category '" + category.getName() + "'. Category is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            categoryRepository.delete(category);
            return new BaseResponse("Category '" + category.getName() + " (" + category.getCode() + ")' deleted successfully.");
        } catch (Exception e){
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public CategoryResponse getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findByCategoryId(id).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
        CategoryResponse.ParentCategoryResponse parentCategoryResponse = null;
        if (category.getParentCategory() != null) {
            CategoryEntity parentCategory = category.getParentCategory();
            parentCategoryResponse = CategoryResponse.ParentCategoryResponse.builder()
                    .categoryId(parentCategory.getCategoryId())
                    .code(parentCategory.getCode())
                    .name(parentCategory.getName())
                    .description(parentCategory.getDescription())
                    .status(parentCategory.getStatus())
                    .createdBy(parentCategory.getCreatedBy())
                    .updatedBy(parentCategory.getUpdatedBy())
                    .createdAt(parentCategory.getCreatedAt())
                    .updatedAt(parentCategory.getUpdatedAt())
                    .build();
        }
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .status(category.getStatus())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .parentCategory(parentCategoryResponse)
                .build();
    }

    public List<CategoryEntity> getCategoriesByParentId(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ApiException("Parent category not found.", HttpStatus.BAD_REQUEST);
        }
        return categoryRepository.findByParentCategoryCategoryId(parentId);
    }

    public List<CategoryResponse> getListCategories(CategoryRetrieveRequest request) {
        List<CategoryEntity> categories = categoryRepository.findListByFilter(request);
        return categories.stream()
                .map(category -> {
                    CategoryResponse.ParentCategoryResponse parentCategoryResponse = null;
                    if (category.getParentCategory() != null) {
                        CategoryEntity parentCategory = category.getParentCategory();
                        parentCategoryResponse = CategoryResponse.ParentCategoryResponse.builder()
                                .categoryId(parentCategory.getCategoryId())
                                .code(parentCategory.getCode())
                                .name(parentCategory.getName())
                                .description(parentCategory.getDescription())
                                .status(parentCategory.getStatus())
                                .createdBy(parentCategory.getCreatedBy())
                                .updatedBy(parentCategory.getUpdatedBy())
                                .createdAt(parentCategory.getCreatedAt())
                                .updatedAt(parentCategory.getUpdatedAt())
                                .build();
                    }
                    return CategoryResponse.builder()
                            .categoryId(category.getCategoryId())
                            .code(category.getCode())
                            .name(category.getName())
                            .description(category.getDescription())
                            .status(category.getStatus())
                            .createdBy(category.getCreatedBy())
                            .updatedBy(category.getUpdatedBy())
                            .createdAt(category.getCreatedAt())
                            .updatedAt(category.getUpdatedAt())
                            .parentCategory(parentCategoryResponse)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public Page<CategoryResponse> getAllCategories(CategoryRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("code");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<CategoryEntity> categories = categoryRepository.findAllByFilter(request, pageable);
        return categories.map(category -> {
                    CategoryResponse.ParentCategoryResponse parentCategoryResponse = null;
                    if (category.getParentCategory() != null) {
                        CategoryEntity parentCategory = category.getParentCategory();
                        parentCategoryResponse = CategoryResponse.ParentCategoryResponse.builder()
                                .categoryId(parentCategory.getCategoryId())
                                .code(parentCategory.getCode())
                                .name(parentCategory.getName())
                                .description(parentCategory.getDescription())
                                .status(parentCategory.getStatus())
                                .createdBy(parentCategory.getCreatedBy())
                                .updatedBy(parentCategory.getUpdatedBy())
                                .createdAt(parentCategory.getCreatedAt())
                                .updatedAt(parentCategory.getUpdatedAt())
                                .build();
                    }
                    return CategoryResponse.builder()
                            .categoryId(category.getCategoryId())
                            .code(category.getCode())
                            .name(category.getName())
                            .description(category.getDescription())
                            .status(category.getStatus())
                            .createdBy(category.getCreatedBy())
                            .updatedBy(category.getUpdatedBy())
                            .createdAt(category.getCreatedAt())
                            .updatedAt(category.getUpdatedAt())
                            .parentCategory(parentCategoryResponse)
                            .build();
                });
    }

    public List<CategoryResponse> getAllParentCategories(CategoryRetrieveRequest request) {
        List<CategoryEntity> parentCategories = categoryRepository.findAllParentByFilter(request);
        return parentCategories.stream()
                .map(category -> CategoryResponse.builder()
                        .categoryId(category.getCategoryId())
                        .code(category.getCode())
                        .name(category.getName())
                        .description(category.getDescription())
                        .status(category.getStatus())
                        .createdBy(category.getCreatedBy())
                        .updatedBy(category.getUpdatedBy())
                        .createdAt(category.getCreatedAt())
                        .updatedAt(category.getUpdatedAt())
                        .parentCategory(category.getParentCategory() != null ?
                                CategoryResponse.ParentCategoryResponse.builder()
                                        .categoryId(category.getParentCategory().getCategoryId())
                                        .code(category.getParentCategory().getCode())
                                        .name(category.getParentCategory().getName())
                                        .description(category.getParentCategory().getDescription())
                                        .status(category.getParentCategory().getStatus())
                                        .createdBy(category.getParentCategory().getCreatedBy())
                                        .updatedBy(category.getParentCategory().getUpdatedBy())
                                        .createdAt(category.getParentCategory().getCreatedAt())
                                        .updatedAt(category.getParentCategory().getUpdatedAt())
                                        .build() : null)
                        .subCategories(mapSubCategories(category.getSubCategories()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<CategoryResponse> mapSubCategories(List<CategoryEntity> subCategories) {
        if (subCategories == null || subCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return subCategories.stream()
                .map(subCategory -> CategoryResponse.builder()
                        .categoryId(subCategory.getCategoryId())
                        .code(subCategory.getCode())
                        .name(subCategory.getName())
                        .description(subCategory.getDescription())
                        .status(subCategory.getStatus())
                        .createdBy(subCategory.getCreatedBy())
                        .updatedBy(subCategory.getUpdatedBy())
                        .createdAt(subCategory.getCreatedAt())
                        .updatedAt(subCategory.getUpdatedAt())
                        .parentCategory(subCategory.getParentCategory() != null ?
                                CategoryResponse.ParentCategoryResponse.builder()
                                        .categoryId(subCategory.getParentCategory().getCategoryId())
                                        .code(subCategory.getParentCategory().getCode())
                                        .name(subCategory.getParentCategory().getName())
                                        .description(subCategory.getParentCategory().getDescription())
                                        .status(subCategory.getParentCategory().getStatus())
                                        .createdBy(subCategory.getParentCategory().getCreatedBy())
                                        .updatedBy(subCategory.getParentCategory().getUpdatedBy())
                                        .createdAt(subCategory.getParentCategory().getCreatedAt())
                                        .updatedAt(subCategory.getParentCategory().getUpdatedAt())
                                        .build() : null)
                        .parentCategory(null)
                        .subCategories(mapSubCategories(subCategory.getSubCategories()))
                        .build())
                .collect(Collectors.toList());
    }

    public ProductResponse.Category getCategoryDetails(Long categoryId) {
        CategoryEntity category = categoryRepository.findByCategoryId(categoryId).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));
        return ProductResponse.Category.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .code(category.getCode())
                .build();
    }

    public boolean categoryExists(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    @Transactional
    public Integer importCategory(CategoryImportRequest request) {
        Integer r = null;
        try {
            MultipartFile file = request.getFile();
            if (file == null || file.isEmpty()) {
                throw new ApiException("File is required.", HttpStatus.BAD_REQUEST);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                throw new ApiException("Invalid file type. Only excel (.xlsx) file are allowed.", HttpStatus.BAD_REQUEST);
            }
            try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    r = row.getRowNum();
                    String  code        = utility.getCellValue(row.getCell(0), String.class);
                    String  name        = utility.getCellValue(row.getCell(1), String.class);
                    String  description = utility.getCellValue(row.getCell(2), String.class);
                    Integer status      = utility.getCellValue(row.getCell(3), Integer.class);
                    if (code == null || code.isEmpty()) {
                        throw new ApiException("Category code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (name == null || name.isEmpty()) {
                        throw new ApiException("Category name is required.", HttpStatus.BAD_REQUEST);
                    }
                    CategoryCreateRequest category = new CategoryCreateRequest();
                    category.setCode(code);
                    category.setName(name);
                    category.setDescription(description);
                    category.setStatus(status);
                    createCategory(category);
                }
            }
            if (r == null || r == 0) {
                throw new ApiException("Category must contain at least one item.", HttpStatus.BAD_REQUEST);
            }
            return r;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (r != null && r != 0) {
                msg = "Row #" + (r + 1) + ": " + msg;
            }
            throw new ApiException(msg, HttpStatus.BAD_REQUEST);
        }
    }
}
