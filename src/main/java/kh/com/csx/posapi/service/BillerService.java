package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.category.CategoryDeleteRequest;
import kh.com.csx.posapi.entity.SettingEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.BillerEntity;
import kh.com.csx.posapi.repository.BillerRepository;
import kh.com.csx.posapi.dto.biller.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BillerService {

    private final BillerRepository billerRepository;
    private final Utility utility;

    public BillerResponse getBillerById(Long id) {
        BillerEntity billerEntity = billerRepository.findById(id).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        return BillerResponse.builder().biller(billerEntity).build();
    }

    public BillerResponse getBillerByCode(String code) {
        BillerEntity billerEntity = billerRepository.findFirstByCode(code).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        return BillerResponse.builder().biller(billerEntity).build();
    }

    public List<BillerResponse> getListBillers(BillerRetrieveRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            request.setIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        List<BillerEntity> billerEntities = billerRepository.findListByFilter(request);
        List<BillerResponse> billerResponses = new ArrayList<>();
        for (BillerEntity billerEntity : billerEntities) {
            billerResponses.add(BillerResponse.builder().biller(billerEntity).build());
        }
        return billerResponses;
    }

    public Page<BillerResponse> getAllBillers(BillerRetrieveRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
            request.setIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
        }
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<BillerEntity> billerEntities = billerRepository.findAllByFilter(request, pageable);
        return billerEntities.map(billerEntity -> BillerResponse.builder().biller(billerEntity).build());
    }

    @Transactional
    public BillerResponse createBiller(BillerCreateRequest request) {
        SettingEntity setting = utility.getSettings();
        if (setting.getMultiBiller().equals(Constant.NO) && billerRepository.existsBy()) {
            throw new ApiException("Multiple billers are not allowed.", HttpStatus.BAD_REQUEST);
        }
        if (billerRepository.existsByCode(request.getCode().trim())) {
            throw new ApiException("Biller code already exists.", HttpStatus.BAD_REQUEST);
        }
        BillerEntity biller = new BillerEntity();
        biller.setCode(request.getCode() != null ? request.getCode().trim() : null);
        biller.setCompanyEn(request.getCompanyEn() != null ? request.getCompanyEn().trim() : null);
        biller.setCompanyKh(request.getCompanyKh() != null ? request.getCompanyKh().trim() : null);
        biller.setNameEn(request.getNameEn() != null ? request.getNameEn().trim() : null);
        biller.setNameKh(request.getNameKh() != null ? request.getNameKh().trim() : null);
        biller.setVatNo(request.getVatNo() != null ? request.getVatNo().trim() : null);
        biller.setContactPerson(request.getContactPerson() != null ? request.getContactPerson().trim() : null);
        biller.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        biller.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        biller.setCity(request.getCity() != null ? request.getCity().trim() : null);
        biller.setState(request.getState() != null ? request.getState().trim() : null);
        biller.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);
        biller.setAddressEn(request.getAddressEn() != null ? request.getAddressEn().trim() : null);
        biller.setAddressKh(request.getAddressKh() != null ? request.getAddressKh().trim() : null);
        biller.setCountry(request.getCountry() != null ? request.getCountry().trim() : null);
        biller.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        try {
            BillerEntity savedBiller = billerRepository.save(biller);
            utility.initOrderRef(savedBiller.getId());
            return getBillerById(savedBiller.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BillerResponse updateBiller(BillerUpdateRequest request) {
        BillerEntity biller = billerRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        if (billerRepository.existsByCodeAndIdNot(request.getCode().trim(), biller.getId())) {
            throw new ApiException("Biller code already exists.", HttpStatus.BAD_REQUEST);
        }
        biller.setCode(request.getCode() != null ? request.getCode().trim() : null);
        biller.setCompanyEn(request.getCompanyEn() != null ? request.getCompanyEn().trim() : null);
        biller.setCompanyKh(request.getCompanyKh() != null ? request.getCompanyKh().trim() : null);
        biller.setNameEn(request.getNameEn() != null ? request.getNameEn().trim() : null);
        biller.setNameKh(request.getNameKh() != null ? request.getNameKh().trim() : null);
        biller.setVatNo(request.getVatNo() != null ? request.getVatNo().trim() : null);
        biller.setContactPerson(request.getContactPerson() != null ? request.getContactPerson().trim() : null);
        biller.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        biller.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        biller.setCity(request.getCity() != null ? request.getCity().trim() : null);
        biller.setState(request.getState() != null ? request.getState().trim() : null);
        biller.setPostalCode(request.getPostalCode() != null ? request.getPostalCode().trim() : null);
        biller.setAddressEn(request.getAddressEn() != null ? request.getAddressEn().trim() : null);
        biller.setAddressKh(request.getAddressKh() != null ? request.getAddressKh().trim() : null);
        biller.setCountry(request.getCountry() != null ? request.getCountry().trim() : null);
        biller.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        try {
            billerRepository.save(biller);
            return getBillerById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteBiller(BillerDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteBiller(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteBiller(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Biller deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteBiller(Long id) {
        BillerEntity biller = billerRepository.findById(id).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        if (billerRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete biller '" + biller.getCompanyEn() + "'. Biller is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            billerRepository.delete(biller);
            return new BaseResponse("Biller '" + biller.getCompanyEn() + " (" + biller.getCode() + ")' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
