package kh.com.csx.posapi.controller.expenseCategory;

import jakarta.validation.Valid;
import kh.com.csx.posapi.dto.expenseCategory.*;
import kh.com.csx.posapi.dto.expenseCategory.ExpenseCategoryUpdateRequest;
import kh.com.csx.posapi.dto.expenseCategory.ExpenseCategoryResponse;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.service.ExpenseCategoryService;
import kh.com.csx.posapi.model.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenseCategory")
public class ExpenseCategoryController {

    @Autowired
    private ExpenseCategoryService expenseCategoryService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-CATEGORIES-CREATE')")
    public BaseResponse createExpenseCategory(@Valid @RequestBody ExpenseCategoryCreateRequest categoryDetails) {
        ExpenseCategoryResponse newCategory = expenseCategoryService.createExpenseCategory(categoryDetails);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(newCategory);
        baseResponse.setMessage("Expense category created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-CATEGORIES-UPDATE')")
    public BaseResponse updateExpenseCategory(@Valid @RequestBody ExpenseCategoryUpdateRequest categoryDetails) {
        ExpenseCategoryResponse updatedCategory = expenseCategoryService.updateExpenseCategory(categoryDetails.getId(), categoryDetails);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(updatedCategory);
        baseResponse.setMessage("Expense category updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-CATEGORIES-DELETE')")
    public BaseResponse deleteExpenseCategory(@Valid @RequestBody ExpenseCategoryDeleteRequest request) {
        return expenseCategoryService.deleteExpenseCategory(request);
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('EXPENSE-CATEGORIES-RETRIEVE')")
    public BaseResponse getAllExpenseCategories(ExpenseCategoryRetrieveRequest filter) {
        Page<ExpenseCategoryResponse> expenseCategories = expenseCategoryService.getAllExpenseCategories(filter);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(expenseCategories);
        baseResponse.setMessage("Expense categories retrieved successfully.");

        return baseResponse;
    }

    @GetMapping("/retrieve")
    public BaseResponse getExpenseCategory(@Valid ExpenseCategoryRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Expense category ID is required.", HttpStatus.BAD_REQUEST);
        }
        ExpenseCategoryResponse expenseCategory = expenseCategoryService.getExpenseCategoryById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(expenseCategory);
        baseResponse.setMessage("Expense category retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse listAllExpenseCategory() {
        List<ExpenseCategoryResponse> expenseCategories = expenseCategoryService.listAllExpenseCategory();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(expenseCategories);
        baseResponse.setMessage("All expense categories retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/parentCategories")
    public BaseResponse getAllParentCategories() {
        List<ExpenseParentCategoryResponse> parentCategories = expenseCategoryService.getAllParentCategories();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(parentCategories);
        baseResponse.setMessage("Parent categories retrieved successfully.");
        return baseResponse;
    }
}
