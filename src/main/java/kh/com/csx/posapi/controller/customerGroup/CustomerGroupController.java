package kh.com.csx.posapi.controller.customerGroup;

import kh.com.csx.posapi.dto.customerGroup.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.CustomerGroupService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customerGroup")
@RequiredArgsConstructor
public class CustomerGroupController {

    private final CustomerGroupService customerGroupService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveCustomerGroup(CustomerGroupRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Customer group ID is required.", HttpStatus.BAD_REQUEST);
        }
        CustomerGroupResponse customerGroupResponse = customerGroupService.getCustomerGroupById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(customerGroupResponse);
        baseResponse.setMessage("Customer group retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListCustomerGroups(CustomerGroupRetrieveRequest request) {
        List<CustomerGroupResponse> customerGroupsResponse = customerGroupService.getListCustomerGroups(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(customerGroupsResponse);
        baseResponse.setMessage("Customer groups retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CUSTOMER_GROUP')")
    public BaseResponse retrieveAllCustomerGroups(CustomerGroupRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(customerGroupService.getAllCustomerGroups(request));
        baseResponse.setMessage("Customer groups retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CUSTOMER_GROUP')")
    public BaseResponse createCustomerGroup(@Valid @RequestBody CustomerGroupCreateRequest request) {
        CustomerGroupResponse customerGroupResponse = customerGroupService.createCustomerGroup(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(customerGroupResponse);
        baseResponse.setMessage("Customer group created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CUSTOMER_GROUP')")
    public BaseResponse updateCustomerGroup(@Valid @RequestBody CustomerGroupUpdateRequest request) {
        CustomerGroupResponse customerGroupResponse = customerGroupService.updateCustomerGroup(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(customerGroupResponse);
        baseResponse.setMessage("Customer group updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-CUSTOMER_GROUP')")
    public BaseResponse deleteCustomerGroup(@Valid @RequestBody CustomerGroupDeleteRequest request) {
        return customerGroupService.deleteCustomerGroup(request);
    }

}
