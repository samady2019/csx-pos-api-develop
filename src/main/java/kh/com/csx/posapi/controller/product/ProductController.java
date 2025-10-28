package kh.com.csx.posapi.controller.product;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kh.com.csx.posapi.dto.product.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-PRODUCTS-CREATE')")
    public BaseResponse createProduct(@Valid @RequestBody ProductRequest request) {
        try {
            ProductResponse createdProduct = productService.createProduct(request);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(createdProduct);
            baseResponse.setMessage("Product created successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-PRODUCTS-UPDATE')")
    public BaseResponse updateProduct(@Valid @RequestBody ProductRequest request) {
        if (request.getProductId() == null) {
            throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            ProductResponse updatedProduct = productService.updateProduct(request);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(updatedProduct);
            baseResponse.setMessage("Product updated successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-PRODUCTS-DELETE')")
    public BaseResponse deleteProduct(@Valid @RequestBody ProductDeleteDTO productRequest) {
        try {
            return productService.deleteProduct(productRequest);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-PRODUCTS-RETRIEVE')")
    public BaseResponse getProductId(ProductRetrieveDTO request) {
        if (request.getProductId() == null) {
            throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            ProductResponse productResponse = productService.getProductByID(request.getProductId(), request.getWarehouseId());
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(productResponse);
            baseResponse.setMessage("Product retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieveByCode")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-PRODUCTS-RETRIEVE')")
    public BaseResponse getProductCode(ProductRetrieveDTO request) {
        if (request.getProductCode() == null || request.getProductCode().trim().isEmpty()) {
            throw new ApiException("Product Code is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            ProductResponse productResponse = productService.getProductByCode(request.getProductCode(), request.getWarehouseId());
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(productResponse);
            baseResponse.setMessage("Product retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-PRODUCTS-RETRIEVE')")
    public BaseResponse getAllProducts(ProductRetrieveDTO request) {
        try {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(productService.getAllProducts(request));
            baseResponse.setMessage("Products retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search")
    public BaseResponse getProductsTerm(ProductRetrieveDTO request) {
        try {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(productService.getProductsTerm(request));
            baseResponse.setMessage("Products retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/searchByBrandCategory")
    public BaseResponse getProductsByBrandCategory(ProductRetrieveDTO request) {
        try {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(productService.getProductsByBrandCategory(request));
            baseResponse.setMessage("Products retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/count")
    public BaseResponse countProduct() {
        try {
            long count = productService.countProducts();
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(count);
            baseResponse.setMessage("Total number of product retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-PRODUCTS-CREATE')")
    public BaseResponse importProduct(@Valid @ModelAttribute ProductImportRequest request, HttpServletRequest servletRequest) {
        Integer rows = productService.importProduct(request, servletRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Total " + rows + " product(s) imported successfully.");
        return baseResponse;
    }
}