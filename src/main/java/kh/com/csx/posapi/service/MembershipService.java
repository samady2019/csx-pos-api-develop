package kh.com.csx.posapi.service;

import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.membership.*;
import kh.com.csx.posapi.entity.MembershipEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.CustomerRepository;
import kh.com.csx.posapi.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final CustomerRepository customerRepository;


    public MembershipResponse createMembership(MembershipCreateRequest request) {
        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new ApiException("Customer does not exist", HttpStatus.NOT_FOUND);
        }
        List<MembershipEntity> existingMemberships = membershipRepository.findByCustomerId(request.getCustomerId());
        if (existingMemberships.stream().anyMatch(existingMembership -> existingMembership.getExpiredDate().isAfter(LocalDate.now()))) {
            throw new ApiException("Customer already has an active membership", HttpStatus.CONFLICT);
        }
        MembershipEntity membership = new MembershipEntity();
        membership.setCustomerId(request.getCustomerId());
        membership.setPoint(request.getPoint());
        membership.setExpiredDate(request.getExpiredDate());
        setUserInfo(membership);
        try {
            MembershipEntity savedMembership = membershipRepository.save(membership);
            return buildResponseMembershipDTO(savedMembership);
        } catch (Exception e) {
            throw new ApiException("Error creating membership: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public MembershipResponse updateMembership(MembershipUpdateRequest request) {
        MembershipEntity existingMembership = membershipRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Membership not found", HttpStatus.NOT_FOUND));
        existingMembership.setCustomerId(request.getCustomerId());
        existingMembership.setPoint(request.getPoint());
        existingMembership.setExpiredDate(request.getExpiredDate());
        setUserInfo(existingMembership);
        try {
            MembershipEntity updatedMembership = membershipRepository.save(existingMembership);
            return buildResponseMembershipDTO(updatedMembership);
        } catch (Exception e) {
            throw new ApiException("Error updating membership: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteMembership(MembershipDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteMembership(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteMembership(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Membership deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteMembership(Long id) {
        MembershipEntity membership = membershipRepository.findById(id).orElseThrow(() -> new ApiException("Membership not found", HttpStatus.NOT_FOUND));
        try {
            membershipRepository.delete(membership);
            return new BaseResponse("Membership deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public MembershipResponse getMembershipById(MembershipRetrieveRequest membershipRetrieveRequest) {
        MembershipEntity membership = membershipRepository.findById(membershipRetrieveRequest.getId()).orElseThrow(() -> new ApiException("Membership not found", HttpStatus.NOT_FOUND));
        return buildResponseMembershipDTO(membership);
    }

    public List<MembershipResponse> getAllMemberships() {
        List<MembershipEntity> memberships = membershipRepository.findAll();
        return memberships.stream().map(this::buildResponseMembershipDTO).collect(Collectors.toList());
    }

    private void setUserInfo(MembershipEntity membership) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        if (membership.getCreatedAt() == null) {
            membership.setCreatedAt(LocalDateTime.now());
            membership.setCreatedBy(userEntity.getUserId());
        } else {
            membership.setUpdatedAt(LocalDateTime.now());
            membership.setUpdatedBy(userEntity.getUserId());
        }
    }

    private MembershipResponse buildResponseMembershipDTO(MembershipEntity membership) {
        LocalDateTime updatedAt = (membership.getUpdatedBy() == null) ? null : membership.getUpdatedAt();
        return MembershipResponse.builder()
                .id(membership.getId())
                .customerId(membership.getCustomerId())
                .point(membership.getPoint())
                .expiredDate(membership.getExpiredDate())
                .createdBy(membership.getCreatedBy())
                .updatedBy(membership.getUpdatedBy())
                .createdAt(membership.getCreatedAt())
                .updatedAt(updatedAt)
                .build();
    }
}

