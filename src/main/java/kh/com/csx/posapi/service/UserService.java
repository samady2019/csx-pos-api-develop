package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.auth.UserRetrieveRequest;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.dto.user.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.entity.PermissionEntity;
import kh.com.csx.posapi.entity.RoleEntity;
import kh.com.csx.posapi.repository.UserRepository;
import kh.com.csx.posapi.repository.PermissionRepository;
import kh.com.csx.posapi.repository.RoleRepository;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    private final Utility utility;

    public UserCreateResponse createUser(UserCreateRequest userRequest) {
        UserEntity userEntity = UserEntity.builder()
                .username(userRequest.getUsername())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .userType(userRequest.getUserType())
                .status(userRequest.getStatus())
                .roles(new HashSet<>())
                .permissions(new HashSet<>())
                .build();

        if (userRequest.getRoleIds() != null) {
            List<RoleEntity> roleEntitiesList = roleRepository.findAllById(userRequest.getRoleIds());
            Set<RoleEntity> rolesSet = new HashSet<>(roleEntitiesList);
            if (rolesSet.size() != userRequest.getRoleIds().size()) {
                throw new ApiException("One or more roles not found.", HttpStatus.BAD_REQUEST);
            }
            userEntity.setRoles(rolesSet);
            Set<PermissionEntity> rolePermissions = rolesSet.stream().flatMap(role -> role.getPermissions().stream()).collect(Collectors.toSet());
            userEntity.getPermissions().addAll(rolePermissions);
        }
        if (userRequest.getSpecialPermissions() != null && !userRequest.getSpecialPermissions().isEmpty()) {
            List<PermissionEntity> specialPermissionsList = permissionRepository.findAllById(userRequest.getSpecialPermissions());
            Set<PermissionEntity> specialPermissionsSet = new HashSet<>(specialPermissionsList);
            userEntity.getPermissions().addAll(specialPermissionsSet);
        }
        userEntity = userRepository.save(userEntity);
        return new UserCreateResponse(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getUserType(),
                userEntity.getStatus(),
                userEntity.getPermissions().stream().map(PermissionEntity::getName).collect(Collectors.toSet())
        );
    }

    public UserResponse getUserById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
        return UserResponse.builder()
                .userId(userEntity.getUserId())
                .username(userEntity.getUsername())
                .userType(userEntity.getUserType())
                .status(userEntity.getStatus())
                .build();
    }

    public List<UserResponse> getAllUsersByEmployeeId(Long id) {
        List<UserEntity> users = userRepository.findByEmployeeId(id);
        return users.stream().map(user -> UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .userType(user.getUserType())
                .status(user.getStatus())
                .build()).collect(Collectors.toList());
    }

    public List<UserResponse> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream().map(user -> UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .userType(user.getUserType())
                .status(user.getStatus())
                .build()).collect(Collectors.toList());
    }

    public UserRolePermissionResponse getUserPermissions(UserRequest request) {
        UserEntity userEntity = userRepository.findByUserId(request.getUserId()).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
        List<PermissionEntity> permissionsList = new ArrayList<>(userEntity.getPermissions());
        return UserRolePermissionResponse.builder()
                .userId(userEntity.getUserId())
                .username(userEntity.getUsername())
                .user_type(userEntity.getUserType())
                .status(userEntity.getStatus())
                .permissions(permissionsList)
                .build();
    }

    public UserRolePermissionResponse getUserRolePermission(UserRequest request) {
        UserEntity userEntity = userRepository.findByUserId(request.getUserId()).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
        List<RoleEntity> rolesList = new ArrayList<>(userEntity.getRoles());
        List<PermissionEntity> permissionsList = new ArrayList<>(userEntity.getPermissions());
        return UserRolePermissionResponse.builder()
                .userId(userEntity.getUserId())
                .username(userEntity.getUsername())
                .user_type(userEntity.getUserType())
                .status(userEntity.getStatus())
                .roles(rolesList)
                .permissions(permissionsList)
                .build();
    }

    public Page<UserRolePermissionResponse> getAllUserRolePermissions(UserRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<UserEntity> users = userRepository.findAllByFilter(request, pageable);
        List<UserRolePermissionResponse> responses = new ArrayList<>();
        for (UserEntity userEntity : users) {
            List<RoleEntity> rolesList = new ArrayList<>(userEntity.getRoles());
            List<PermissionEntity> permissionsList = new ArrayList<>(userEntity.getPermissions());
            UserRolePermissionResponse response = UserRolePermissionResponse.builder()
                    .userId(userEntity.getUserId())
                    .username(userEntity.getUsername())
                    .user_type(userEntity.getUserType())
                    .status(userEntity.getStatus())
                    .roles(rolesList)
                    .permissions(permissionsList)
                    .build();
            responses.add(response);
        }
        return new PageImpl<>(responses, pageable, users.getTotalElements());
    }

    public UserRolePermissionResponse assignRolesToUser(UserRequest userRequest, UserRoleRequest userRoleRequest) {
        UserEntity userEntity = userRepository.findByUserId(userRequest.getUserId()).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
        Set<RoleEntity> roles = new HashSet<>();
        if (userRoleRequest.getRoles() != null) {
            for (RoleEntity roleRequest : userRoleRequest.getRoles()) {
                RoleEntity role = roleRepository.findById(roleRequest.getRoleId()).orElseThrow(() -> new ApiException("Role not found.", HttpStatus.BAD_REQUEST));
                roles.add(role);
            }
        }
        userEntity.setRoles(roles);
        try {
            userRepository.save(userEntity);
            return this.getUserRolePermission(userRequest);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public UserRolePermissionResponse assignPermissionsToUser(UserRequest userRequest, UserPermissionRequest userPermissionRequest) {
        UserEntity userEntity = userRepository.findByUserId(userRequest.getUserId()).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
        Set<PermissionEntity> permissions = new HashSet<>();
        if (userPermissionRequest.getPermissions() != null) {
            for (PermissionEntity permissionRequest : userPermissionRequest.getPermissions()) {
                if (permissionRequest.getPermissionId() == null) {
                    throw new ApiException("Permission ID cannot be null", HttpStatus.BAD_REQUEST);
                }
                PermissionEntity permission = permissionRepository.findById(permissionRequest.getPermissionId()).orElseThrow(() -> new ApiException("Permission not found.", HttpStatus.BAD_REQUEST));
                permissions.add(permission);
            }
        }
        userEntity.setPermissions(permissions);
        try {
            userRepository.save(userEntity);
            return this.getUserRolePermission(userRequest);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public UserRolePermissionResponse updateUserRolePermission(UserRequest userRequest, UserRolePermissionRequest userRolePermissionRequest) {
        UserEntity userEntity = userRepository.findByUserId(userRequest.getUserId()).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
        Set<RoleEntity> roles = new HashSet<>();
        Set<PermissionEntity> permissions = new HashSet<>();
        if (userRolePermissionRequest.getRoles() != null) {
            for (RoleEntity roleRequest : userRolePermissionRequest.getRoles()) {
                RoleEntity role = roleRepository.findById(roleRequest.getRoleId()).orElseThrow(() -> new ApiException("Role not found.", HttpStatus.BAD_REQUEST));
                roles.add(role);
            }
        }
        if (userRolePermissionRequest.getPermissions() != null) {
            for (PermissionEntity permissionRequest : userRolePermissionRequest.getPermissions()) {
                PermissionEntity permission = permissionRepository.findById(permissionRequest.getPermissionId()).orElseThrow(() -> new ApiException("Permission not found.", HttpStatus.BAD_REQUEST));
                permissions.add(permission);
            }
        }
        userEntity.setRoles(roles);
        userEntity.setPermissions(permissions);
        try {
            userRepository.save(userEntity);
            return this.getUserRolePermission(userRequest);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
