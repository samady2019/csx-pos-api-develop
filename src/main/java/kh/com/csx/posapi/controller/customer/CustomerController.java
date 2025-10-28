package kh.com.csx.posapi.controller.customer;

import jakarta.validation.Valid;
import kh.com.csx.posapi.dto.customer.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-CUSTOMERS-CREATE')")
    public BaseResponse createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        try {
            CustomerResponse createdCustomer = customerService.createCustomer(request);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(createdCustomer);
            baseResponse.setMessage("Customer created successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-CUSTOMERS-UPDATE')")
    public BaseResponse updateCustomer(@Valid @RequestBody CustomerUpdateRequest updateRequest) {
        try {
            CustomerResponse updatedCustomer = customerService.updateCustomer(updateRequest);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(updatedCustomer);
            baseResponse.setMessage("Customer updated successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-CUSTOMERS-DELETE')")
    public BaseResponse deleteCustomer(@Valid @RequestBody CustomerDeleteRequest request) {
        try {
            return customerService.deleteCustomer(request);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-CUSTOMERS-RETRIEVE')")
    public BaseResponse getAllCustomers(CustomerRetrieveRequest request) {
        try {
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(customerService.getAllCustomers(request));
            baseResponse.setMessage("Customers retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieve")
    public BaseResponse getCustomerById(CustomerRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Customer ID is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            CustomerResponse customer = customerService.getCustomerById(request);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(customer);
            baseResponse.setMessage("Customer retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/search")
    public BaseResponse getCustomersTerm(CustomerRetrieveRequest request) {
        try {
            List<CustomerResponse> customers = customerService.getCustomersTerm(request);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(customers);
            baseResponse.setMessage("Customers retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list")
    public BaseResponse getListCustomers(CustomerRetrieveRequest request) {
        try {
            List<CustomerResponse> customers = customerService.getListCustomers(request);
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setData(customers);
            baseResponse.setMessage("Customers retrieved successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-CUSTOMERS-CREATE')")
    public BaseResponse importCustomer(@Valid @ModelAttribute CustomerImportRequest request) {
        Integer rows = customerService.importCustomer(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Total " + rows + " customer(s) imported successfully.");
        return baseResponse;
    }
}
