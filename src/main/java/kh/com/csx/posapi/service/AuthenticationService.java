package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.auth.*;
import kh.com.csx.posapi.dto.user.UserRequest;
import kh.com.csx.posapi.dto.user.UserResponse;
import kh.com.csx.posapi.entity.EmployeeEntity;
import kh.com.csx.posapi.entity.SettingEntity;
import kh.com.csx.posapi.entity.SystemUserEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import org.springframework.data.domain.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final SystemUserRepository systemUserRepository;
    private final EmployeeRepository employeeRepository;
    private final BillerRepository billerRepository;
    private final WarehouseRepository warehouseRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    private final Utility utility;

    @Transactional
    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        SettingEntity setting = utility.getSettings();
        EmployeeEntity employeeEntity = null;
        if (request.getEmployeeId() != null) {
            Optional<EmployeeEntity> optionalEmployee = employeeRepository.findById(request.getEmployeeId());
            if (optionalEmployee.isEmpty()) {
                throw new ApiException("Employee not found.", HttpStatus.BAD_REQUEST);
            } else {
                employeeEntity = optionalEmployee.get();
            }
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ApiException("Username is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ApiException("Password is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            request.setLanguage(setting.getLanguage());
        } else if (!Constant.Language.VALID.contains(request.getLanguage().trim())) {
            throw new ApiException("Invalid language. " + Constant.Language.NOTE + ".", HttpStatus.BAD_REQUEST);
        }
        if (request.getBillers() != null && !request.getBillers().trim().isEmpty()) {
            try {
                StringBuilder strIds = new StringBuilder();
                List<Long> billerIds = Arrays.stream(request.getBillers().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                for (Long billerId : billerIds) {
                    billerRepository.findById(billerId).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
                    if (!strIds.isEmpty()) {
                        strIds.append(",");
                    }
                    strIds.append(billerId.toString());
                }
                request.setBillers(strIds.toString());
            } catch (Exception e) {
                throw new ApiException("Invalid input biller IDs.", HttpStatus.BAD_REQUEST);
            }
        }
        if (request.getWarehouses() != null && !request.getWarehouses().trim().isEmpty()) {
            try {
                StringBuilder strIds = new StringBuilder();
                List<Long> warehouseIds = Arrays.stream(request.getWarehouses().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                for (Long warehouseId : warehouseIds) {
                    warehouseRepository.findById(warehouseId).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
                    if (!strIds.isEmpty()) {
                        strIds.append(",");
                    }
                    strIds.append(warehouseId.toString());
                }
                request.setWarehouses(strIds.toString());
            } catch (Exception e) {
                throw new ApiException("Invalid input warehouse IDs.", HttpStatus.BAD_REQUEST);
            }
        }
        if (request.getViewRight() == null) {
            request.setViewRight(Constant.User.ViewRight.DEFAULT);
        } else if (!Constant.User.ViewRight.VALID.contains(request.getViewRight())) {
            throw new ApiException("Invalid view right. " + Constant.User.ViewRight.NOTE + ".", HttpStatus.BAD_REQUEST);
        }
        if (request.getUserType() == null || request.getUserType().trim().isEmpty()) {
            request.setUserType(Constant.User.Type.DEFAULT);
        } else if (!Constant.User.Type.VALID_STATUSES.contains(request.getUserType().trim())) {
            throw new ApiException("Invalid user type. " + Constant.User.Type.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            request.setStatus(Constant.User.Status.DEFAULT);
        } else if (!Constant.User.Status.VALID_STATUSES.contains(request.getStatus().trim())) {
            throw new ApiException("Invalid user status. " + Constant.User.Status.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (request.getUserType().trim().equals(Constant.User.Type.SYSTEM)) {
            SystemUserEntity existedUser = systemUserRepository.findFirstByUsername(request.getUsername()).orElse(null);
            if (existedUser != null) {
                throw new ApiException("Username already exists.", HttpStatus.BAD_REQUEST);
            }
            SystemUserEntity userEntity = SystemUserEntity.builder()
                    .employeeId(null)
                    .username(request.getUsername().trim())
                    .password(passwordEncoder.encode(request.getPassword().trim()))
                    .userType(request.getUserType().trim())
                    .status(request.getStatus().trim())
                    .build();
            try {
                SystemUserEntity savedUser = systemUserRepository.save(userEntity);
                String jwtToken = jwtService.generateToken(userEntity);
                logger.info("Success created user {} ", userEntity);
                return AuthenticationResponseDTO.builder()
                        .user(getUserByID(savedUser.getUserId(), Constant.User.Type.SYSTEM))
                        .token(jwtToken)
                        .build();
            } catch (Exception e) {
                throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            UserEntity existedUser = userRepository.findFirstByUsername(request.getUsername()).orElse(null);
            if (existedUser != null) {
                throw new ApiException("Username already exists.", HttpStatus.BAD_REQUEST);
            }
            if (request.getRolePermission() == null || request.getRolePermission().getRoles() == null || request.getRolePermission().getRoles().isEmpty()) {
                throw new ApiException("User role required.", HttpStatus.BAD_REQUEST);
            }
            request.setCommissionRate((request.getCommissionRate() != null && request.getCommissionRate() > 0) ? request.getCommissionRate() : 0);
            UserEntity userEntity = UserEntity.builder()
                    .employeeId(employeeEntity != null ? employeeEntity.getId() : null)
                    .userType(request.getUserType().trim())
                    .username(request.getUsername().trim())
                    .password(passwordEncoder.encode(request.getPassword().trim()))
                    .language(request.getLanguage().trim())
                    .billers(request.getBillers())
                    .warehouses(request.getWarehouses())
                    .viewRight(request.getViewRight())
                    .commissionRate(request.getCommissionRate())
                    .status(request.getStatus().trim())
                    .employee(employeeEntity)
                    .build();
            try {
                UserEntity savedUser = userRepository.save(userEntity);
                userService.updateUserRolePermission(new UserRequest(savedUser.getUserId()), request.getRolePermission());
                String jwtToken = jwtService.generateToken(userEntity);
                logger.info("Success created user {} ", userEntity);
                return AuthenticationResponseDTO.builder()
                        .user(getUserByID(savedUser.getUserId()))
                        .token(jwtToken)
                        .build();
            } catch (Exception e) {
                throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Transactional
    public AuthenticationResponseDTO update(UserUpdateRequestDTO request) {
        if (request.getUserId() == null) {
            throw new ApiException("User ID is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getUserType() == null || request.getUserType().trim().isEmpty()) {
            request.setUserType(Constant.User.Type.DEFAULT);
        }
        validateUserType(request.getUserType());
        if (request.getUserType().equals(Constant.User.Type.SYSTEM)) {
            SystemUserEntity user = systemUserRepository.findByUserId(request.getUserId()).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            if (request.getUsername() != null) {
                if (request.getUsername().trim().isEmpty()) {
                    throw new ApiException("Username is required.", HttpStatus.BAD_REQUEST);
                }
                if (systemUserRepository.findFirstByUsernameAndUserIdNot(request.getUsername(),request.getUserId()).isPresent()) {
                    throw new ApiException("Username already exists", HttpStatus.BAD_REQUEST);
                }
                user.setUsername(request.getUsername().trim());
            }
            if (request.getOldPassword() != null || request.getNewPassword() != null) {
                if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                    throw new ApiException("Old password is required.", HttpStatus.BAD_REQUEST);
                }
                if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                    throw new ApiException("New password is required.", HttpStatus.BAD_REQUEST);
                }
                if (!passwordEncoder.matches(request.getOldPassword().trim(), user.getPassword())) {
                    throw new ApiException("Old password does not match", HttpStatus.BAD_REQUEST);
                }
                user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
            }
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                if (!Constant.User.Status.VALID_STATUSES.contains(request.getStatus().trim())) {
                    throw new ApiException("Invalid user status. " + Constant.User.Status.NOTE, HttpStatus.BAD_REQUEST);
                }
                user.setStatus(request.getStatus().trim());
            }
            try {
                SystemUserEntity savedUser = systemUserRepository.save(user);
                String jwtToken = jwtService.generateToken(user);
                logger.info("Success updated user {} ", user);
                return AuthenticationResponseDTO.builder()
                        .user(getUserByID(savedUser.getUserId(), request.getUserType()))
                        .token(jwtToken)
                        .build();
            } catch (Exception e) {
                throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            UserEntity user = userRepository.findByUserId(request.getUserId()).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            if (request.getUsername() != null) {
                if (request.getUsername().trim().isEmpty()) {
                    throw new ApiException("Username is required.", HttpStatus.BAD_REQUEST);
                }
                if (userRepository.findFirstByUsernameAndUserIdNot(request.getUsername(),request.getUserId()).isPresent()) {
                    throw new ApiException("Username already exists.", HttpStatus.BAD_REQUEST);
                }
                user.setUsername(request.getUsername().trim());
            }
            if (request.getOldPassword() != null || request.getNewPassword() != null) {
                if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                    throw new ApiException("Old Password is required.", HttpStatus.BAD_REQUEST);
                }
                if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                    throw new ApiException("New Password is required.", HttpStatus.BAD_REQUEST);
                }
                if (!passwordEncoder.matches(request.getOldPassword().trim(), user.getPassword())) {
                    throw new ApiException("Old password does not match.", HttpStatus.BAD_REQUEST);
                }
                user.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
            }
            if (request.getLanguage() != null && !request.getLanguage().trim().isEmpty()) {
                if (!Constant.Language.VALID.contains(request.getLanguage().trim())) {
                    throw new ApiException("Invalid language. " + Constant.Language.NOTE + ".", HttpStatus.BAD_REQUEST);
                }
                user.setLanguage(request.getLanguage().trim());
            }
            if (request.getBillers() != null && !request.getBillers().trim().isEmpty()) {
                try {
                    StringBuilder strIds = new StringBuilder();
                    List<Long> billerIds = Arrays.stream(request.getBillers().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                    for (Long billerId : billerIds) {
                        billerRepository.findById(billerId).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
                        if (!strIds.isEmpty()) {
                            strIds.append(",");
                        }
                        strIds.append(billerId.toString());
                    }
                    user.setBillers(strIds.toString());
                } catch (Exception e) {
                    throw new ApiException("Invalid input biller IDs.", HttpStatus.BAD_REQUEST);
                }
            }
            if (request.getWarehouses() != null && !request.getWarehouses().trim().isEmpty()) {
                try {
                    StringBuilder strIds = new StringBuilder();
                    List<Long> warehouseIds = Arrays.stream(request.getWarehouses().split(",")).map(String::trim).filter(utility::isValidLong).map(Long::parseLong).toList();
                    for (Long warehouseId : warehouseIds) {
                        warehouseRepository.findById(warehouseId).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
                        if (!strIds.isEmpty()) {
                            strIds.append(",");
                        }
                        strIds.append(warehouseId.toString());
                    }
                    user.setWarehouses(strIds.toString());
                } catch (Exception e) {
                    throw new ApiException("Invalid input warehouse IDs.", HttpStatus.BAD_REQUEST);
                }
            }
            if (request.getViewRight() != null) {
                if (!Constant.User.ViewRight.VALID.contains(request.getViewRight())) {
                    throw new ApiException("Invalid view right. " + Constant.User.ViewRight.NOTE + ".", HttpStatus.BAD_REQUEST);
                }
                user.setViewRight(request.getViewRight());
            }
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                if (!Constant.User.Status.VALID_STATUSES.contains(request.getStatus().trim())) {
                    throw new ApiException("Invalid user status. " + Constant.User.Status.NOTE, HttpStatus.BAD_REQUEST);
                }
                user.setStatus(request.getStatus().trim());
            }
            if (request.getCommissionRate() != null) {
                user.setCommissionRate(request.getCommissionRate() > 0 ? request.getCommissionRate() : 0);
            }
            if (request.getRolePermission() != null) {
                if (request.getRolePermission().getRoles() == null || request.getRolePermission().getRoles().isEmpty()) {
                    throw new ApiException("User role required.", HttpStatus.BAD_REQUEST);
                }
            }
            try {
                UserEntity savedUser = userRepository.save(user);
                if (request.getRolePermission() != null) {
                    userService.updateUserRolePermission(new UserRequest(savedUser.getUserId()), request.getRolePermission());
                }
                String jwtToken = jwtService.generateToken(user);
                logger.info("Success updated user {} ", user);
                return AuthenticationResponseDTO.builder()
                        .user(getUserByID(savedUser.getUserId()))
                        .token(jwtToken)
                        .build();
            } catch (Exception e) {
                throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Transactional
    public void updatePassword(UserUpdateRequestDTO request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            if (request.getUserType() == null || request.getUserType().trim().isEmpty()) {
                request.setUserType(Constant.User.Type.DEFAULT);
            }
            if (request.getUserId() == null) {
                throw new ApiException("User ID is required.", HttpStatus.BAD_REQUEST);
            }
            validateUserType(request.getUserType());
            if (((authentication.getAuthorities().stream().anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_ADMIN") ||
                    grantedAuthority.getAuthority().equals("ROLE_OWNER") ||
                    grantedAuthority.getAuthority().equals("USER-USERS-UPDATE"))) || userEntity.getUserId().equals(request.getUserId())) && request.getUserType().equals(Constant.User.Type.VENDER)) {

                if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
                    throw new ApiException("Old password is required.", HttpStatus.BAD_REQUEST);
                }
                if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                    throw new ApiException("New password is required.", HttpStatus.BAD_REQUEST);
                }
                if (!passwordEncoder.matches(request.getOldPassword().trim(), userEntity.getPassword())) {
                    throw new ApiException("Old password does not match", HttpStatus.BAD_REQUEST);
                }
                userEntity.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
                userRepository.save(userEntity);
            } else {
                throw new ApiException("Access denied.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void deleteUser(Long userId) {
        deleteUser(userId, Constant.User.Type.DEFAULT);
    }

    public void deleteUser(Long userId, String userType) {
        validateUserType(userType);
        if (userType.trim().equals(Constant.User.Type.SYSTEM)) {
            SystemUserEntity user = systemUserRepository.findById(userId).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            try {
                systemUserRepository.delete(user);
            } catch (Exception e) {
                throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            if (userRepository.countReferences(userId) > 0) {
                throw new ApiException("Cannot delete user '" + user.getUsername() + "'. User is referenced in other records.", HttpStatus.BAD_REQUEST);
            }
            try {
                userRepository.delete(user);
            } catch (Exception e) {
                throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    public AuthenticationResponseDTO login(AuthenticationRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserEntity userEntity = userRepository.findByUsername(request.getUsername()).orElseThrow();
        if (userEntity.getStatus().equals(Constant.User.Status.INACTIVE)) {
            throw new ApiException("User '" + request.getUsername() + "' is not active account.", HttpStatus.BAD_REQUEST);
        }
        String jwtToken = jwtService.generateToken(userEntity);
        return AuthenticationResponseDTO.builder()
                .user(getUserByID(userEntity.getUserId()))
                .token(jwtToken)
                .build();
    }

    public UserResponse getUserByID(Long userId) {
        return getUserByID(userId, Constant.User.Type.DEFAULT);
    }

    public UserResponse getUserByID(Long userId, String userType) {
        validateUserType(userType);
        if (userType.trim().equals(Constant.User.Type.SYSTEM)) {
            SystemUserEntity userEntity = systemUserRepository.findById(userId).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            return UserResponse.builder()
                    .userId(userEntity.getUserId())
                    .username(userEntity.getUsername())
                    .userType(userEntity.getUserType())
                    .status(userEntity.getStatus())
                    .employee(null)
                    .build();
        } else {
            UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            return UserResponse.builder()
                    .userId(userEntity.getUserId())
                    .userType(userEntity.getUserType())
                    .username(userEntity.getUsername())
                    .language(userEntity.getLanguage())
                    .billers(userEntity.getBillers())
                    .warehouses(userEntity.getWarehouses())
                    .viewRight(userEntity.getViewRight())
                    .commissionRate(userEntity.getCommissionRate())
                    .status(userEntity.getStatus())
                    .employee(userEntity.getEmployee())
                    .build();
        }
    }

    public UserResponse getUserByUsername(String username) {
        return getUserByUsername(username, Constant.User.Type.DEFAULT);
    }

    public UserResponse getUserByUsername(String username, String userType) {
        validateUserType(userType);
        if (userType.trim().equals(Constant.User.Type.SYSTEM)) {
            SystemUserEntity userEntity = systemUserRepository.findByUsername(username).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            return UserResponse.builder()
                    .userId(userEntity.getUserId())
                    .username(userEntity.getUsername())
                    .userType(userEntity.getUserType())
                    .status(userEntity.getStatus())
                    .employee(null)
                    .build();
        } else {
            UserEntity userEntity = userRepository.findByUsername(username).orElseThrow(() -> new ApiException("User not found.", HttpStatus.BAD_REQUEST));
            return UserResponse.builder()
                    .userId(userEntity.getUserId())
                    .userType(userEntity.getUserType())
                    .username(userEntity.getUsername())
                    .language(userEntity.getLanguage())
                    .billers(userEntity.getBillers())
                    .warehouses(userEntity.getWarehouses())
                    .viewRight(userEntity.getViewRight())
                    .commissionRate(userEntity.getCommissionRate())
                    .status(userEntity.getStatus())
                    .employee(userEntity.getEmployee())
                    .build();
        }
    }

    public List<UserResponse> getAllActiveUsers() {
        return getAllActiveUsers(Constant.User.Type.DEFAULT);
    }

    public List<UserResponse> getAllActiveUsers(String userType) {
        validateUserType(userType);
        if (userType.trim().equals(Constant.User.Type.SYSTEM)) {
            List<SystemUserEntity> userEntities = systemUserRepository.findActiveUsers();
            if (userEntities.isEmpty()) {
                throw new ApiException("Users active is empty.", HttpStatus.BAD_REQUEST);
            }
            List<UserResponse> userResponses = new ArrayList<>();
            for (SystemUserEntity userEntity : userEntities) {
                userResponses.add(UserResponse.builder()
                        .userId(userEntity.getUserId())
                        .username(userEntity.getUsername())
                        .userType(userEntity.getUserType())
                        .status(userEntity.getStatus())
                        .employee(null)
                        .build());
            }
            return userResponses;
        } else {
            List<UserEntity> userEntities = userRepository.findActiveUsers(userType.trim());
            if (userEntities.isEmpty()) {
                throw new ApiException("Users active is empty.", HttpStatus.BAD_REQUEST);
            }
            List<UserResponse> userResponses = new ArrayList<>();
            for (UserEntity userEntity : userEntities) {
                userResponses.add(UserResponse.builder()
                        .userId(userEntity.getUserId())
                        .userType(userEntity.getUserType())
                        .username(userEntity.getUsername())
                        .language(userEntity.getLanguage())
                        .billers(userEntity.getBillers())
                        .warehouses(userEntity.getWarehouses())
                        .viewRight(userEntity.getViewRight())
                        .commissionRate(userEntity.getCommissionRate())
                        .status(userEntity.getStatus())
                        .employee(userEntity.getEmployee())
                        .build());
            }
            return userResponses;
        }
    }

    public Page<UserResponse> getAllUsers(UserRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        if (request.getUserType() == null || request.getUserType().trim().isEmpty()) {
            request.setUserType(Constant.User.Type.DEFAULT);
        }
        validateUserType(request.getUserType().trim());
        if (request.getGender() != null) {
            request.setGender(determineGender(request.getGender()));
        }
        if (request.getUserType().equals(Constant.User.Type.SYSTEM)) {
            Page<SystemUserEntity> userEntities = systemUserRepository.findAllByFilter(request, pageable);
            if (userEntities.isEmpty()) {
                throw new ApiException("Users is empty.", HttpStatus.BAD_REQUEST);
            }
            List<UserResponse> userResponses = new ArrayList<>();
            for (SystemUserEntity userEntity : userEntities) {
                userResponses.add(UserResponse.builder()
                        .userId(userEntity.getUserId())
                        .username(userEntity.getUsername())
                        .userType(userEntity.getUserType())
                        .status(userEntity.getStatus())
                        .employee(null)
                        .build());
            }
            return new PageImpl<>(userResponses, pageable, userEntities.getTotalElements());
        } else {
            Page<UserEntity> userEntities = userRepository.findAllByFilter(request, pageable);
            List<UserResponse> userResponses = new ArrayList<>();
            for (UserEntity userEntity : userEntities) {
                userResponses.add(UserResponse.builder()
                        .userId(userEntity.getUserId())
                        .userType(userEntity.getUserType())
                        .username(userEntity.getUsername())
                        .language(userEntity.getLanguage())
                        .billers(userEntity.getBillers())
                        .warehouses(userEntity.getWarehouses())
                        .viewRight(userEntity.getViewRight())
                        .commissionRate(userEntity.getCommissionRate())
                        .status(userEntity.getStatus())
                        .employee(userEntity.getEmployee())
                        .build());
            }
            return new PageImpl<>(userResponses, pageable, userEntities.getTotalElements());
        }
    }

    private void validateUserType(String userType) {
        if (!Constant.User.Type.VALID_STATUSES.contains(userType.trim())) {
            throw new ApiException("Invalid user type. " + Constant.User.Type.NOTE, HttpStatus.BAD_REQUEST);
        }
    }

    private String determineGender(String genderInput) {
        if (genderInput != null && !genderInput.trim().isEmpty()) {
            String gender = genderInput.trim().toLowerCase();
            if (gender.equals("m") || gender.equals("male")) {
                return Constant.Gender.MALE;
            } else if (gender.equals("f") || gender.equals("female")) {
                return Constant.Gender.FEMALE;
            } else {
                throw new ApiException("Invalid gender. " + Constant.Gender.NOTE, HttpStatus.BAD_REQUEST);
            }
        } else {
            return null;
        }
    }
}