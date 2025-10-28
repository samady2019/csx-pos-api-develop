package kh.com.csx.posapi.controller.auth;

import jakarta.validation.Valid;
import kh.com.csx.posapi.dto.permission.*;
import kh.com.csx.posapi.entity.PermissionEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/permission")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public BaseResponse createPermission(@Valid @RequestBody PermissionCreateRequest permissionRequest) {
        try {
            PermissionEntity permissionEntity = permissionService.createPermission(permissionRequest.getName());
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Permission created successfully.");
            baseResponse.setData(permissionEntity);
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-PERMISSIONS-RETRIEVE') or hasAuthority('USER-ROLES-UPDATE') or hasAuthority('USER-ROLES-ASSIGN-PERMISSION')")
    public BaseResponse getPermissionById(@Valid PermissionRetrieveRequest permissionRequest) {
        try {
            PermissionEntity permissionEntity = permissionService.getPermissionById(permissionRequest.getId());
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Permission retrieved successfully.");
            baseResponse.setData(permissionEntity);
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-PERMISSIONS-RETRIEVE') or hasAuthority('USER-ROLES-UPDATE') or hasAuthority('USER-ROLES-ASSIGN-PERMISSION')")
    public BaseResponse getAllPermissions() {
        try {
            Map<String, Object> permissions = permissionService.getAllPermissions();
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Permissions retrieved successfully.");
            baseResponse.setData(permissions);
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public BaseResponse updatePermission(@Valid @RequestBody PermissionUpdateRequest permissionRequest) {
        try {
            PermissionEntity permissionEntity = permissionService.updatePermission(permissionRequest.getId(), permissionRequest.getName());
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Permission updated successfully.");
            baseResponse.setData(permissionEntity);
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public BaseResponse deletePermission(@Valid @RequestBody PermissionDeleteRequest permissionRequest) {
        try {
            permissionService.deletePermission(permissionRequest.getId());
            BaseResponse baseResponse = new BaseResponse();
            baseResponse.setMessage("Permission deleted successfully.");
            return baseResponse;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
