package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.role.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.PermissionEntity;
import kh.com.csx.posapi.entity.RoleEntity;
import kh.com.csx.posapi.repository.UserRepository;
import kh.com.csx.posapi.repository.RoleRepository;
import kh.com.csx.posapi.repository.PermissionRepository;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private final Utility utility;

    public RoleResponse getRole(Integer roleId) {
        RoleEntity roleEntity = roleRepository.findById(roleId).orElseThrow(() -> new ApiException("Role not found.", HttpStatus.BAD_REQUEST));
        List<PermissionEntity> permissionList = new ArrayList<>(roleEntity.getPermissions());
        return RoleResponse.builder()
                .roleId(roleEntity.getRoleId())
                .name(roleEntity.getName())
                .permissions(permissionList)
                .build();
    }

    public Page<RoleResponse> getAllRolesPermissions(RoleRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("roleId");
        }
        if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<RoleEntity> roleEntities = roleRepository.findAllByFilter(request, pageable);
        List<RoleResponse> roleResponses = new ArrayList<>();
        for (RoleEntity roleEntity : roleEntities) {
            List<PermissionEntity> permissionList = new ArrayList<>(roleEntity.getPermissions());
            RoleResponse roleResponse = RoleResponse.builder()
                    .roleId(roleEntity.getRoleId())
                    .name(roleEntity.getName())
                    .permissions(permissionList)
                    .build();
            roleResponses.add(roleResponse);
        }
        return new PageImpl<>(roleResponses, pageable, roleEntities.getTotalElements());
    }

    public List<RoleResponse> getListRolesPermissions(RoleRetrieveRequest request) {
        List<RoleEntity> roleEntities = roleRepository.findListByFilter(request);
        List<RoleResponse> roleResponses = new ArrayList<>();
        for (RoleEntity roleEntity : roleEntities) {
            List<PermissionEntity> permissionList = new ArrayList<>(roleEntity.getPermissions());
            RoleResponse roleResponse = RoleResponse.builder()
                    .roleId(roleEntity.getRoleId())
                    .name(roleEntity.getName())
                    .permissions(permissionList)
                    .build();
            roleResponses.add(roleResponse);
        }

        return roleResponses;
    }

    public RoleResponse createRole(RoleCreateRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
             throw new ApiException("Invalid role name.", HttpStatus.CONFLICT);
        }
        RoleEntity existedRole = roleRepository.findByName(request.getName()).orElse(null);
        if (existedRole != null) {
             throw new ApiException("Role already exists.", HttpStatus.CONFLICT);
        }
        RoleEntity role = new RoleEntity();
        role.setName(request.getName());
        Set<PermissionEntity> permissions = new HashSet<>();
        if (request.getPermissions() != null) {
            for (PermissionEntity permissionRequest : request.getPermissions()) {
                if (permissionRequest.getPermissionId() == null) {
                    throw new ApiException("Permission ID cannot be null.", HttpStatus.BAD_REQUEST);
                }
                PermissionEntity permission = permissionRepository.findById(permissionRequest.getPermissionId()).orElseThrow(() -> new ApiException("Permission not found.", HttpStatus.BAD_REQUEST));
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);
        try {
            RoleEntity savedRole = roleRepository.save(role);
            return this.getRole(savedRole.getRoleId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public RoleResponse updateRole(RoleUpdateRequest request) {
        RoleEntity role = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new ApiException("Role not found.", HttpStatus.BAD_REQUEST));
        if (role.getName().equals("ADMIN") || role.getName().equals("OWNER")) {
            throw new ApiException("You cannot edit the ADMIN or OWNER role, as they are essential system roles.", HttpStatus.BAD_REQUEST);
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ApiException("Invalid role name.", HttpStatus.CONFLICT);
        }
        boolean existedRole = roleRepository.findByNameAndRoleIdNot(request.getName(), request.getRoleId()).isPresent();
        if (existedRole) {
            throw new ApiException("Role already exists.", HttpStatus.CONFLICT);
        }
        role.setName(request.getName());
        Set<PermissionEntity> permissions = new HashSet<>();
        if (request.getPermissions() != null) {
            for (PermissionEntity permissionRequest : request.getPermissions()) {
                if (permissionRequest.getPermissionId() == null) {
                    throw new ApiException("Permission ID cannot be null.", HttpStatus.BAD_REQUEST);
                }
                PermissionEntity permission = permissionRepository.findById(permissionRequest.getPermissionId()).orElseThrow(() -> new ApiException("Permission not found.", HttpStatus.BAD_REQUEST));
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);
        try {
            RoleEntity updatedRole = roleRepository.save(role);
            return this.getRole(updatedRole.getRoleId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void deleteRole(RoleDeleteRequest request) {
        RoleEntity role = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new ApiException("Role not found.", HttpStatus.BAD_REQUEST));
        if (role.getName().equals("ADMIN") || role.getName().equals("OWNER")) {
            throw new ApiException("You cannot delete the ADMIN or OWNER role, as they are essential system roles.", HttpStatus.BAD_REQUEST);
        }
        boolean isRoleInUse = userRepository.existsByRoles_RoleId(request.getRoleId());
        if (isRoleInUse) {
            throw new ApiException("Role cannot be deleted because it is currently in use by one or more users.", HttpStatus.BAD_REQUEST);
        }
        try {
            roleRepository.delete(role);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public RoleResponse assignPermissionsToRole(RolePermissionsRequest request) {
        RoleEntity role = roleRepository.findById(request.getRoleId()).orElseThrow(() -> new ApiException("Role not found.", HttpStatus.BAD_REQUEST));
        Set<PermissionEntity> permissions = new HashSet<>();
        if (request.getPermissions() != null) {
            for (PermissionEntity permissionRequest : request.getPermissions()) {
                if (permissionRequest.getPermissionId() == null) {
                    throw new ApiException("Permission ID cannot be null.", HttpStatus.BAD_REQUEST);
                }
                PermissionEntity permission = permissionRepository.findById(permissionRequest.getPermissionId()).orElseThrow(() -> new ApiException("Permission not found.", HttpStatus.BAD_REQUEST));
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);
        try {
            RoleEntity assignRole = roleRepository.save(role);
            return this.getRole(assignRole.getRoleId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
