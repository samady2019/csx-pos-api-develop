package kh.com.csx.posapi.controller.auth;

import kh.com.csx.posapi.dto.role.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveRole(RoleRetrieveRequest request) {
        if (request.getRoleId() == null) {
            throw new ApiException("Role ID is required.", HttpStatus.BAD_REQUEST);
        }
        RoleResponse roleResponse = roleService.getRole(request.getRoleId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(roleResponse);
        baseResponse.setMessage("Role retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListRolesPermission(RoleRetrieveRequest request) {
        List<RoleResponse> roleResponses = roleService.getListRolesPermissions(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(roleResponses);
        baseResponse.setMessage("Roles retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-ROLES-RETRIEVE')")
    public BaseResponse retrieveAllRolesPermission(RoleRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(roleService.getAllRolesPermissions(request));
        baseResponse.setMessage("Roles retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-ROLES-CREATE')")
    public BaseResponse createRole(@RequestBody RoleCreateRequest request) {
        RoleResponse roleResponse = roleService.createRole(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Role created successfully.");
        baseResponse.setData(roleResponse);
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-ROLES-UPDATE')")
    public BaseResponse updateRole(@RequestBody RoleUpdateRequest request) {
        RoleResponse roleResponse = roleService.updateRole(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(roleResponse);
        baseResponse.setMessage("Role updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-ROLES-DELETE')")
    public BaseResponse deleteRole(@RequestBody RoleDeleteRequest request) {
        roleService.deleteRole(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Role deleted successfully.");
        return baseResponse;
    }

    @PostMapping("/assignPermission")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-ROLES-UPDATE') or hasAuthority('USER-ROLES-ASSIGN-PERMISSION')")
    public BaseResponse assignPermissionsToRole(@RequestBody RolePermissionsRequest request) {
        RoleResponse roleResponse = roleService.assignPermissionsToRole(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(roleResponse);
        baseResponse.setMessage("Permissions assigned to role successfully.");
        return baseResponse;
    }
}