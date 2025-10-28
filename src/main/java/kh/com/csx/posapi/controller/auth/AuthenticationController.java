package kh.com.csx.posapi.controller.auth;

import kh.com.csx.posapi.dto.auth.*;
import kh.com.csx.posapi.dto.user.UserResponse;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/login")
    public BaseResponse login(@RequestBody AuthenticationRequestDTO request) {
        return new BaseResponse(authService.login(request),"Login successfully.");
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-CREATE')")
    public BaseResponse register(@RequestBody RegisterRequestDTO request) {
        return new BaseResponse(authService.register(request),"User created successfully.");
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-UPDATE')")
    public BaseResponse update(@RequestBody UserUpdateRequestDTO request) {
        return new BaseResponse(authService.update(request),"User updated successfully.");
    }

    @PostMapping("/updatePassword")
    public BaseResponse updatePassword(@RequestBody UserUpdateRequestDTO request) {
        authService.updatePassword(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Password updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-DELETE')")
    public BaseResponse delete(@RequestBody UserDeleteRequestDTO request) {
        if (request.getUserId() == null) {
            throw new ApiException("User ID is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getUserType() != null && !request.getUserType().trim().isEmpty()) {
            authService.deleteUser(request.getUserId(), request.getUserType().trim());
        } else {
            authService.deleteUser(request.getUserId());
        }
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("User deleted successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-USERS-RETRIEVE')")
    public BaseResponse retrieveAllUsers(UserRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(authService.getAllUsers(request));
        baseResponse.setMessage("User retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    public BaseResponse retrieveUser(UserRetrieveRequest request) {
        if (request.getUserId() == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            request.setUserId(user.getUserId());
        }
        UserResponse userResponse =
                (request.getUserType() != null && !request.getUserType().trim().isEmpty()) ?
                        authService.getUserByID(request.getUserId(), request.getUserType().trim()) :
                        authService.getUserByID(request.getUserId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(userResponse);
        baseResponse.setMessage("User retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveByUsername")
    public BaseResponse retrieveUserByUsername(UserRetrieveRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ApiException("Username is required", HttpStatus.BAD_REQUEST);
        }
        UserResponse userResponse =
                (request.getUserType() != null && !request.getUserType().trim().isEmpty()) ?
                        authService.getUserByUsername(request.getUsername().trim(), request.getUserType().trim()) :
                        authService.getUserByUsername(request.getUsername().trim());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(userResponse);
        baseResponse.setMessage("User retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAllActive")
    public BaseResponse retrieveAllUsersActive(UserRetrieveRequest request) {
        List<UserResponse> userResponses =
                (request.getUserType() != null && !request.getUserType().trim().isEmpty()) ?
                        authService.getAllActiveUsers(request.getUserType().trim()) :
                        authService.getAllActiveUsers();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(userResponses);
        baseResponse.setMessage("User retrieved successfully.");
        return baseResponse;
    }
}
