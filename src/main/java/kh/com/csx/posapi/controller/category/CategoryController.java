package kh.com.csx.posapi.controller.category;

import kh.com.csx.posapi.dto.category.*;
import kh.com.csx.posapi.entity.CategoryEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.CategoryService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-CATEGORIES-CREATE')")
    public BaseResponse createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse categoryResponse = categoryService.createCategory(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(categoryResponse);
        baseResponse.setMessage("Category created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-CATEGORIES-UPDATE')")
    public BaseResponse updateCategory(@Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse categoryResponse = categoryService.updateCategory(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(categoryResponse);
        baseResponse.setMessage("Category updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-CATEGORIES-DELETE')")
    public BaseResponse deleteCategory(@Valid @RequestBody CategoryDeleteRequest request) {
        return categoryService.deleteCategory(request);
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-CATEGORIES-RETRIEVE')")
    public BaseResponse retrieveAllCategories(CategoryRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(categoryService.getAllCategories(request));
        baseResponse.setMessage("All categories retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    public BaseResponse retrieveCategory(CategoryRetrieveRequest request) {
        if (request.getCategoryId() == null) {
            throw new ApiException("Category ID is required.", HttpStatus.BAD_REQUEST);
        }
        BaseResponse baseResponse = new BaseResponse();
        CategoryResponse categoryResponse = categoryService.getCategoryById(request.getCategoryId());
        baseResponse.setData(categoryResponse);
        baseResponse.setMessage("Category retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveByParent")
    public BaseResponse retrieveCategoriesByParent(CategoryRetrieveRequest request) {
        if (request.getCategoryId() == null) {
            throw new ApiException("Category ID is required.", HttpStatus.BAD_REQUEST);
        }
        BaseResponse baseResponse = new BaseResponse();
        List<CategoryEntity> categories = categoryService.getCategoriesByParentId(request.getCategoryId());
        baseResponse.setData(categories);
        baseResponse.setMessage("Categories retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListCategories(CategoryRetrieveRequest request) {
        List<CategoryResponse> categories = categoryService.getListCategories(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(categories);
        baseResponse.setMessage("All categories retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAllParents")
    public BaseResponse retrieveAllParentCategories(CategoryRetrieveRequest request) {
        List<CategoryResponse> parentCategories = categoryService.getAllParentCategories(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(parentCategories);
        baseResponse.setMessage("All parent categories retrieved successfully.");
        return baseResponse;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-CATEGORIES-CREATE')")
    public BaseResponse importCategory(@Valid @ModelAttribute CategoryImportRequest request) {
        Integer rows = categoryService.importCategory(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Total " + rows + " categories imported successfully.");
        return baseResponse;
    }
}
