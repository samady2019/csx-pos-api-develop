package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.expenseCategory.*;
import kh.com.csx.posapi.entity.ExpenseCategoryEntity;
import kh.com.csx.posapi.entity.ExpenseParentCategoryEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.ExpenseCategoryRepository;
import kh.com.csx.posapi.repository.ExpenseParentCategoryRepository;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    @Autowired
    private final ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private final ExpenseParentCategoryRepository expenseParentCategoryRepository;

    @Autowired private final Utility utility;

    @Transactional
    public ExpenseCategoryResponse createExpenseCategory(ExpenseCategoryCreateRequest categoryDetails) {
        try {
            if (expenseCategoryRepository.existsByCode(categoryDetails.getCode())) {
                throw new ApiException("Category with this code already exists.", HttpStatus.BAD_REQUEST);
            }

            ExpenseCategoryEntity newCategory = new ExpenseCategoryEntity();
            newCategory.setCode(categoryDetails.getCode());
            newCategory.setName(categoryDetails.getName());
            newCategory.setDescription(categoryDetails.getDescription());

            if (categoryDetails.getParentId() != null) {
                ExpenseCategoryEntity parentCategory = expenseCategoryRepository.findById(categoryDetails.getParentId())
                        .orElseThrow(() -> new ApiException("Parent category not found.", HttpStatus.BAD_REQUEST));

                if (parentCategory.getParentCategory() != null) {
                    throw new ApiException("This category cannot be a parent because it already has a parent.", HttpStatus.BAD_REQUEST);
                }
                newCategory.setParentCategory(parentCategory);
            }

            ExpenseCategoryEntity savedCategory = expenseCategoryRepository.save(newCategory);
            return new ExpenseCategoryResponse(savedCategory);
        } catch (Exception e){
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public ExpenseCategoryResponse updateExpenseCategory(Long id, ExpenseCategoryUpdateRequest categoryDetails) {
        try {
            ExpenseCategoryEntity category = expenseCategoryRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Expense category not found.", HttpStatus.BAD_REQUEST));

            if (!category.getCode().equals(categoryDetails.getCode()) && expenseCategoryRepository.existsByCode(categoryDetails.getCode())) {
                throw new ApiException("Category with this code already exists.", HttpStatus.BAD_REQUEST);
            }

            category.setCode(categoryDetails.getCode());
            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());

            if (categoryDetails.getParentId() != null) {
                ExpenseCategoryEntity parentCategory = expenseCategoryRepository.findById(categoryDetails.getParentId())
                        .orElseThrow(() -> new ApiException("Parent category not found.", HttpStatus.BAD_REQUEST));

                if (parentCategory.getParentCategory() != null) {
                    throw new ApiException("This category cannot be a parent because it already has a parent.", HttpStatus.BAD_REQUEST);
                }
                category.setParentCategory(parentCategory);
            } else {
                category.setParentCategory(null);
            }

            ExpenseCategoryEntity updatedCategory = expenseCategoryRepository.save(category);
            return new ExpenseCategoryResponse(updatedCategory);

        } catch (Exception e){
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public List<ExpenseCategoryResponse> listAllExpenseCategory() {
        try {
            List<ExpenseCategoryEntity> categories = expenseCategoryRepository.findAll();
            return categories.stream().map(ExpenseCategoryResponse::new).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<ExpenseCategoryResponse> getAllExpenseCategories(ExpenseCategoryRetrieveRequest request) {
        try {
            if ("code".equalsIgnoreCase(request.getOrderBy())) {
                request.setOrderBy("code");
            } else if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            Page<ExpenseCategoryEntity> categoryEntities = expenseCategoryRepository.findAllByFilter(request, pageable);

            return categoryEntities.map(ExpenseCategoryResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ExpenseCategoryResponse getExpenseCategoryById(Long categoryId) {
        try {
            ExpenseCategoryEntity category = expenseCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ApiException("Expense category not found.", HttpStatus.BAD_REQUEST));
            return new ExpenseCategoryResponse(category);
        }catch (Exception e){
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public List<ExpenseParentCategoryResponse> getAllParentCategories() {
        try {
            List<ExpenseParentCategoryEntity> categories = expenseParentCategoryRepository.findAllWithSubCategories();
            return categories.stream()
                    .map(ExpenseParentCategoryResponse::new)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteExpenseCategory(ExpenseCategoryDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteExpenseCategory(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteExpenseCategory(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Expense category deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteExpenseCategory(Long categoryId) {
        try {
            ExpenseCategoryEntity category = expenseCategoryRepository.findById(categoryId).orElseThrow(() -> new ApiException("Expense category not found.", HttpStatus.BAD_REQUEST));
            if (expenseCategoryRepository.countReferences(categoryId) > 0) {
                throw new ApiException("Cannot delete expense category '" + category.getName() + " (" + category.getCode() + ")'. Expense category is referenced in other records.", HttpStatus.BAD_REQUEST);
            }
            expenseCategoryRepository.delete(category);
            return new BaseResponse("Expense category '" + category.getName() + " (" + category.getCode() + ")' deleted successfully.");
        }catch (Exception e){
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
