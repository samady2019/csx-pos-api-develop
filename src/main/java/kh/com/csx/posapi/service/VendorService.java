package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.VendorEntity;
import kh.com.csx.posapi.repository.VendorRepository;
import kh.com.csx.posapi.dto.vendor.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final Utility utility;

    public VendorResponse getVendorById(Long id) {
        VendorEntity vendorEntity = vendorRepository.findById(id).orElseThrow(() -> new ApiException("Vendor not found", HttpStatus.NOT_FOUND));
        return VendorResponse.builder().vendor(vendorEntity).build();
    }

    public List<VendorResponse> getListVendors(VendorRetrieveRequest request) {
        List<VendorEntity> vendorEntities = vendorRepository.findListByFilter(request);
        List<VendorResponse> vendorResponses = new ArrayList<>();
        for (VendorEntity vendorEntity : vendorEntities) {
            vendorResponses.add(VendorResponse.builder().vendor(vendorEntity).build());
        }
        return vendorResponses;
    }

    public Page<VendorResponse> getAllVendors(VendorRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<VendorEntity> vendorEntities = vendorRepository.findAllByFilter(request, pageable);
        return vendorEntities.map(vendorEntity -> VendorResponse.builder().vendor(vendorEntity).build());
    }

    public VendorResponse createVendor(VendorCreateRequest request) {
        VendorEntity vendor = new VendorEntity();
        vendor.setUserId(request.getUserId());
        vendor.setFirstName(request.getFirstName().trim());
        vendor.setLastName(request.getLastName().trim());
        vendor.setShopNameEn(request.getShopNameEn().trim());
        vendor.setShopNameKh(request.getShopNameKh().trim());
        vendor.setAddress(request.getAddress().trim());
        vendor.setCreatedAt(LocalDateTime.now());
        try {
            VendorEntity savedVendor = vendorRepository.save(vendor);
            return getVendorById(savedVendor.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public VendorResponse updateVendor(VendorUpdateRequest request) {
        VendorEntity vendor = vendorRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Vendor not found", HttpStatus.NOT_FOUND));
        vendor.setUserId(request.getUserId());
        vendor.setFirstName(request.getFirstName().trim());
        vendor.setLastName(request.getLastName().trim());
        vendor.setShopNameEn(request.getShopNameEn().trim());
        vendor.setShopNameKh(request.getShopNameKh().trim());
        vendor.setAddress(request.getAddress().trim());
        vendor.setUpdatedAt(LocalDateTime.now());
        try {
            vendorRepository.save(vendor);
            return getVendorById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteVendor(VendorDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteVendor(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteVendor(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Vendor deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteVendor(Long id) {
        VendorEntity vendor = vendorRepository.findById(id).orElseThrow(() -> new ApiException("Vendor not found.", HttpStatus.NOT_FOUND));
        try {
            vendorRepository.delete(vendor);
            return new BaseResponse("Vendor deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
