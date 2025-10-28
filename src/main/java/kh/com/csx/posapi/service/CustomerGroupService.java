package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.customerGroup.*;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.CustomerGroupEntity;
import kh.com.csx.posapi.repository.CustomerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerGroupService {

    private final CustomerGroupRepository customerGroupRepository;
    private final Utility utility;

    public CustomerGroupResponse getCustomerGroupById(Long id) {
        CustomerGroupEntity customerGroupEntity = customerGroupRepository.findById(id).orElseThrow(() -> new ApiException("Customer group not found.", HttpStatus.BAD_REQUEST));
        return CustomerGroupResponse.builder().customerGroup(customerGroupEntity).build();
    }

    public List<CustomerGroupResponse> getListCustomerGroups(CustomerGroupRetrieveRequest request) {
        List<CustomerGroupEntity> customerGroupEntities = customerGroupRepository.findListByFilter(request);
        List<CustomerGroupResponse> customerGroupResponses = new ArrayList<>();
        for (CustomerGroupEntity customerGroupEntity : customerGroupEntities) {
            customerGroupResponses.add(CustomerGroupResponse.builder().customerGroup(customerGroupEntity).build());
        }
        return customerGroupResponses;
    }

    public Page<CustomerGroupResponse> getAllCustomerGroups(CustomerGroupRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<CustomerGroupEntity> customerGroupEntities = customerGroupRepository.findAllByFilter(request, pageable);
        return customerGroupEntities.map(customerGroupEntity -> CustomerGroupResponse.builder().customerGroup(customerGroupEntity).build());
    }

    public CustomerGroupResponse createCustomerGroup(CustomerGroupCreateRequest request) {
        if (customerGroupRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ApiException("Customer group '" + request.getName().trim() + "' already exists.", HttpStatus.BAD_REQUEST);
        }
        CustomerGroupEntity customerGroup = new CustomerGroupEntity();
        customerGroup.setName(request.getName().trim());
        customerGroup.setPercent(request.getPercent());
        try {
            CustomerGroupEntity savedCustomerGroup = customerGroupRepository.save(customerGroup);
            return getCustomerGroupById(savedCustomerGroup.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public CustomerGroupResponse updateCustomerGroup(CustomerGroupUpdateRequest request) {
        CustomerGroupEntity customerGroup = customerGroupRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Customer group not found.", HttpStatus.BAD_REQUEST));
        if (customerGroupRepository.existsByNameIgnoreCaseAndIdNot(request.getName().trim(), request.getId())) {
            throw new ApiException("Customer group '" + request.getName().trim() + "' already exists.", HttpStatus.BAD_REQUEST);
        }
        customerGroup.setName(request.getName().trim());
        customerGroup.setPercent(request.getPercent());
        try {
            customerGroupRepository.save(customerGroup);
            return getCustomerGroupById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteCustomerGroup(CustomerGroupDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteCustomerGroup(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteCustomerGroup(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Customer group deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteCustomerGroup(Long id) {
        CustomerGroupEntity customerGroup = customerGroupRepository.findById(id).orElseThrow(() -> new ApiException("Customer group not found.", HttpStatus.BAD_REQUEST));
        if (customerGroupRepository.existCustomer(customerGroup.getId())) {
            throw new ApiException("Unable to delete customer group '" + customerGroup.getName() + "' has already used by customer.", HttpStatus.BAD_REQUEST);
        }
        try {
            customerGroupRepository.delete(customerGroup);
            return new BaseResponse("Customer group '" + customerGroup.getName() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
