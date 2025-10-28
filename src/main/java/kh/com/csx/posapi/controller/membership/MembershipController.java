package kh.com.csx.posapi.controller.membership;

import jakarta.validation.Valid;
import kh.com.csx.posapi.dto.membership.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService membershipService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-MEMBERSHIPS-CREATE')")
    public BaseResponse createMember(@Valid @RequestBody MembershipCreateRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        try {
            MembershipResponse createdMembership = membershipService.createMembership(request);
            baseResponse.setMessage("Membership created successfully");
            baseResponse.setData(createdMembership);
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-MEMBERSHIPS-UPDATE')")
    public BaseResponse updateMembership(@Valid @RequestBody MembershipUpdateRequest updateRequest) {
        BaseResponse baseResponse = new BaseResponse();
        try {
            MembershipResponse updatedMembership = membershipService.updateMembership(updateRequest);
            baseResponse.setMessage("Membership updated successfully");
            baseResponse.setData(updatedMembership);
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-MEMBERSHIPS-DELETE')")
    public BaseResponse deleteMembership(@Valid @RequestBody MembershipDeleteRequest request) {
        return membershipService.deleteMembership(request);
    }

    @GetMapping("/retrieve")
    public BaseResponse getMembershipById(@Valid MembershipRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        try {
            MembershipResponse membership = membershipService.getMembershipById(request);
            baseResponse.setData(membership);
            baseResponse.setMessage("Membership retrieved successfully");
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-MEMBERSHIPS-RETRIEVE')")
    public BaseResponse getAllMembership() {
        BaseResponse baseResponse = new BaseResponse();
        try {
            List<MembershipResponse> memberships = membershipService.getAllMemberships();
            baseResponse.setData(memberships);
            baseResponse.setMessage("Memberships retrieved successfully");
        } catch (Exception e) {
            baseResponse.setMessage(e.getMessage());
            throw new ApiException(baseResponse.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return baseResponse;
    }
}
