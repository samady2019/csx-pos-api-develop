package kh.com.csx.posapi.controller.auth;

import kh.com.csx.posapi.dto.auth.UserRetrieveRequest;
import kh.com.csx.posapi.dto.user.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.UserService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-RETRIEVE')")
    public BaseResponse createUser(@RequestBody UserCreateRequest request) {
        UserCreateResponse userResponse = userService.createUser(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(userResponse);
        baseResponse.setMessage("User created successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    public BaseResponse getUserById(UserRequest userRequest) {
        if (userRequest.getUserId() == null) {
            throw new ApiException("User ID is required.", HttpStatus.BAD_REQUEST);
        }
        UserResponse response = userService.getUserById(userRequest.getUserId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(response);
        baseResponse.setMessage("User retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-RETRIEVE')")
    public BaseResponse getAllUsers() {
        List<UserResponse> responses = userService.getAllUsers();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(responses);
        baseResponse.setMessage("All users retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/permission")
    public BaseResponse getUserPermissions(UserRequest userRequest) {
        if (userRequest.getUserId() == null) {
            throw new ApiException("User ID is required.", HttpStatus.BAD_REQUEST);
        }
        UserRolePermissionResponse response = userService.getUserPermissions(userRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(response);
        baseResponse.setMessage("User Permissions retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/role_permission")
    public BaseResponse getUserRolesPermissions(UserRequest userRequest) {
        if (userRequest.getUserId() == null) {
            throw new ApiException("User ID is required.", HttpStatus.BAD_REQUEST);
        }
        UserRolePermissionResponse response = userService.getUserRolePermission(userRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(response);
        baseResponse.setMessage("User roles and permissions retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/all_role_permissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-RETRIEVE')")
    public BaseResponse getAllUserRolesPermissions(UserRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(userService.getAllUserRolePermissions(request));
        baseResponse.setMessage("All user's roles and permissions retrieved successfully.");
        return baseResponse;
    }

    @PostMapping(value = "/assignRole", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-UPDATE') or hasAuthority('USER-USERS-ASSIGN-ROLE')")
    public BaseResponse assignRolesToUser(@RequestPart(value = "user", required = false) UserRequest userRequest, @RequestPart(value = "data", required = false) UserRoleRequest userRoleRequest) {
        BaseResponse baseResponse = new BaseResponse();
        if (userRequest == null || userRoleRequest == null) {
            baseResponse.setMessage("Required parameter 'user' or 'data' is not present.");
            return baseResponse;
        }
        UserRolePermissionResponse response = userService.assignRolesToUser(userRequest, userRoleRequest);
        baseResponse.setData(response);
        baseResponse.setMessage("Roles assigned successfully.");
        return baseResponse;
    }

    @PostMapping(value = "/assignPermission", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-UPDATE') or hasAuthority('USER-USERS-ASSIGN-PERMISSION')")
    public BaseResponse assignPermissionsToUser(@RequestPart(value = "user", required = false) UserRequest userRequest, @RequestPart(value = "data", required = false) UserPermissionRequest userPermissionRequest) {
        BaseResponse baseResponse = new BaseResponse();
        if (userRequest == null || userPermissionRequest == null) {
            baseResponse.setMessage("Required parameter 'user' or 'data' is not present.");
            return baseResponse;
        }
        UserRolePermissionResponse response = userService.assignPermissionsToUser(userRequest, userPermissionRequest);
        baseResponse.setData(response);
        baseResponse.setMessage("Permissions assigned successfully.");
        return baseResponse;
    }

    @PostMapping(value = "/assignRolePermission", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-UPDATE') or hasAuthority('USER-USERS-ASSIGN-ROLE') or hasAuthority('USER-USERS-ASSIGN-PERMISSION')")
    public BaseResponse setUserRole(@RequestPart(value = "user", required = false) UserRequest userRequest, @RequestPart(value = "data", required = false) UserRolePermissionRequest userRolePermissionRequest) {
        BaseResponse baseResponse = new BaseResponse();
        if (userRequest == null || userRolePermissionRequest == null) {
            baseResponse.setMessage("Required parameter 'user' or 'data' is not present.");
            return baseResponse;
        }
        UserRolePermissionResponse response = userService.updateUserRolePermission(userRequest, userRolePermissionRequest);
        baseResponse.setData(response);
        baseResponse.setMessage("User role and permission assigned successfully.");
        return baseResponse;
    }
}
