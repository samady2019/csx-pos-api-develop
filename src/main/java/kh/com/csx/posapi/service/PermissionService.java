package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant.Modules;
import kh.com.csx.posapi.entity.PermissionEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.repository.PermissionRepository;
import kh.com.csx.posapi.repository.RoleRepository;
import kh.com.csx.posapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public PermissionEntity createPermission(String name) {
        if (permissionRepository.existsByName(name)) {
            throw new ApiException("Permission already exists.", HttpStatus.BAD_REQUEST);
        }
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setName(name);
        return permissionRepository.save(permissionEntity);
    }

    public PermissionEntity getPermissionById(Integer id) {
        return permissionRepository.findById(id).orElseThrow(() -> new ApiException("Permission not found.", HttpStatus.BAD_REQUEST));
    }

    public Map<String, Object> getAllPermissions() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> moduleList = new ArrayList<>();
        for (Modules module : Modules.values()) {
            Map<String, Object> moduleMap = new HashMap<>();
            List<Map<String, Object>> submoduleList = new ArrayList<>();
            Set<String> addedPermissions = new HashSet<>();
            for (String submodule : module.getSubmodules()) {
                String moduleSubmodule = module.name() + "-" + submodule;
                List<PermissionEntity> permissions = permissionRepository.findByModule(moduleSubmodule + "%");
                List<PermissionEntity> filteredPermissions = new ArrayList<>();
                for (PermissionEntity permission : permissions) {
                    if (!addedPermissions.contains(permission.getName())) {
                        filteredPermissions.add(permission);
                        addedPermissions.add(permission.getName());
                    }
                }
                Map<String, Object> submoduleMap = new HashMap<>();
                submoduleMap.put(submodule, filteredPermissions);
                submoduleList.add(submoduleMap);
            }
            moduleMap.put(module.name(), submoduleList);
            moduleList.add(moduleMap);
        }
        response.put("modules", moduleList);
        return response;
    }

    public PermissionEntity updatePermission(Integer id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new ApiException("New permission name cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        String normalName = newName.trim();
        PermissionEntity existingPermission = permissionRepository.findById(id).orElseThrow(() -> new ApiException("Permission not found.", HttpStatus.BAD_REQUEST));
        boolean isNameTaken = permissionRepository.existsByName(normalName);
        if (isNameTaken && !existingPermission.getName().equals(normalName)) {
            throw new ApiException("Permission with the given name already exists.", HttpStatus.BAD_REQUEST);
        }
        existingPermission.setName(normalName);
        return permissionRepository.save(existingPermission);
    }

    public void deletePermission(Integer id) {
        if (roleRepository.existsByPermissions_PermissionId(id)) {
            throw new ApiException("Permission cannot be deleted because it is assigned to roles.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByPermissions_PermissionId(id)) {
            throw new ApiException("Permission cannot be deleted because it is assigned to users.", HttpStatus.BAD_REQUEST);
        }
        if (!permissionRepository.existsById(id)) {
            throw new ApiException("Permission not found.", HttpStatus.BAD_REQUEST);
        }
        permissionRepository.deleteById(id);
    }
}
