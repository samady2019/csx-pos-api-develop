package kh.com.csx.posapi.service;

import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.setting.FileInfoResponse;
import kh.com.csx.posapi.dto.user.UserResponse;
import kh.com.csx.posapi.dto.employee.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.entity.EmployeeEntity;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.EmployeeRepository;
import kh.com.csx.posapi.repository.UserRepository;
import kh.com.csx.posapi.utility.Utility;
import kh.com.csx.posapi.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final Utility utility;

    public EmployeeResponse getEmployeeByID(Long id) {
        EmployeeEntity employeeEntity = employeeRepository.findById(id).orElseThrow(() -> new ApiException("Employee not found.", HttpStatus.BAD_REQUEST));
        List<UserEntity> usersEntity = userRepository.findByEmployeeId(employeeEntity.getId());
        List<UserResponse> userResponses = new ArrayList<>();
        if (usersEntity != null) {
            for (UserEntity user : usersEntity) {
                userResponses.add(UserResponse.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .userType(user.getUserType())
                        .status(user.getStatus())
                        .build());

            }
        }
        return EmployeeResponse.builder()
                .employee(employeeEntity)
                .users(userResponses)
                .build();
    }

    public List<EmployeeResponse> getListEmployees(EmployeeRetrieveRequest request) {
        List<EmployeeEntity> employeeEntities = employeeRepository.findListByFilter(request);
        List<EmployeeResponse> employeeResponses = new ArrayList<>();
        for (EmployeeEntity employeeEntity : employeeEntities) {
            List<UserEntity> usersEntity = userRepository.findByEmployeeId(employeeEntity.getId());
            List<UserResponse> userResponses = new ArrayList<>();
            if (usersEntity != null) {
                for (UserEntity user : usersEntity) {
                    userResponses.add(UserResponse.builder()
                            .userId(user.getUserId())
                            .username(user.getUsername())
                            .userType(user.getUserType())
                            .status(user.getStatus())
                            .build());

                }
            }
            employeeResponses.add(EmployeeResponse.builder()
                    .employee(employeeEntity)
                    .users(userResponses)
                    .build());
        }
        return employeeResponses;
    }

    public Page<EmployeeResponse> getAllEmployees(EmployeeRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<EmployeeEntity> employeeEntities = employeeRepository.findAllByFilter(request, pageable);
        return employeeEntities.map(employeeEntity -> {
            List<UserEntity> userEntities = userRepository.findByEmployeeId(employeeEntity.getId());
            List<UserResponse> userResponses = userEntities.stream()
                    .map(user -> UserResponse.builder()
                            .userId(user.getUserId())
                            .username(user.getUsername())
                            .userType(user.getUserType())
                            .status(user.getStatus())
                            .build())
                    .collect(Collectors.toList());

            return EmployeeResponse.builder()
                    .employee(employeeEntity)
                    .users(userResponses)
                    .build();
        });
    }

    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        EmployeeEntity employee = new EmployeeEntity();
        String image = (request.getImage() != null && !request.getImage().trim().isEmpty()) ? request.getImage().trim() : Constant.USER_NO_IMAGE;
        employee.setImage(image);
        employee.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
        employee.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
        employee.setGender(determineGender(request.getGender()));
        employee.setDob(request.getDob());
        employee.setAge(request.getAge());
        employee.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        employee.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        employee.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        employee.setNationality(request.getNationality() != null ? request.getNationality().trim() : null);
        employee.setCreatedAt(LocalDateTime.now());
        try {
            EmployeeEntity savedEmployee = employeeRepository.save(employee);
            return getEmployeeByID(savedEmployee.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public EmployeeResponse updateEmployee(EmployeeUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            if ((authentication.getAuthorities().stream().anyMatch(grantedAuthority ->
                    grantedAuthority.getAuthority().equals("ROLE_ADMIN") ||
                    grantedAuthority.getAuthority().equals("ROLE_OWNER") ||
                    grantedAuthority.getAuthority().equals("USER-EMPLOYEES-UPDATE"))) || userEntity.getEmployeeId().equals(request.getId())) {
                EmployeeEntity employee = employeeRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Employee not found.", HttpStatus.BAD_REQUEST));
                String image = (request.getImage() != null && !request.getImage().trim().isEmpty()) ? request.getImage().trim() : employee.getImage();
                employee.setImage(image);
                employee.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : null);
                employee.setLastName(request.getLastName() != null ? request.getLastName().trim() : null);
                employee.setGender(determineGender(request.getGender()));
                employee.setDob(request.getDob());
                employee.setAge(request.getAge());
                employee.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
                employee.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
                employee.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
                employee.setNationality(request.getNationality() != null ? request.getNationality().trim() : null);
                employee.setUpdatedAt(LocalDateTime.now());
                employeeRepository.save(employee);

                return getEmployeeByID(request.getId());
            } else {
                throw new ApiException("Access denied.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteEmployee(EmployeeDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteEmployee(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteEmployee(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Employee deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteEmployee(Long id) {
        EmployeeEntity employee = employeeRepository.findById(id).orElseThrow(() -> new ApiException("Employee not found.", HttpStatus.BAD_REQUEST));
        if (userRepository.existsByEmployeeId(employee.getId())) {
            throw new ApiException("Cannot delete employee '" + employee.getFirstName() + " " + employee.getLastName() + "'. Employee have an associated user account.", HttpStatus.BAD_REQUEST);
        }
        try {
            employeeRepository.delete(employee);
            return new BaseResponse("Employee '" + employee.getFirstName() + " " + employee.getLastName() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
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
                throw new ApiException("Invalid gender. Accepted values are 'm', 'male', 'f', or 'female'.", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new ApiException("Gender is required.", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public Integer importEmployee(EmployeeImportRequest request, HttpServletRequest servletRequest) {
        Integer r = null;
        List<FileInfoResponse> savedFiles = new ArrayList<>();
        try {
            MultipartFile file = request.getFile();
            if (file == null || file.isEmpty()) {
                throw new ApiException("File is required.", HttpStatus.BAD_REQUEST);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                throw new ApiException("Invalid file type. Only excel (.xlsx) file are allowed.", HttpStatus.BAD_REQUEST);
            }
            try (InputStream inputStream = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    r = row.getRowNum();
                    String    image       = Constant.USER_NO_IMAGE;
                    String    firstName   = utility.getCellValue(row.getCell(1), String.class);
                    String    lastName    = utility.getCellValue(row.getCell(2), String.class);
                    String    gender      = utility.getCellValue(row.getCell(3), String.class);
                    LocalDate dob         = utility.getCellValue(row.getCell(4), LocalDate.class);
                    Integer   age         = utility.getCellValue(row.getCell(5), Integer.class);
                    String    phone       = utility.getCellValue(row.getCell(6), String.class);
                    String    email       = utility.getCellValue(row.getCell(7), String.class);
                    String    address     = utility.getCellValue(row.getCell(8), String.class);
                    String    nationality = utility.getCellValue(row.getCell(9), String.class);
                    if (firstName == null || firstName.isEmpty()) {
                        throw new ApiException("first name is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (gender == null || gender.isEmpty()) {
                        throw new ApiException("Gender is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (phone == null || phone.isEmpty()) {
                        throw new ApiException("Phone is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (email == null || email.isEmpty()) {
                        throw new ApiException("Email is required.", HttpStatus.BAD_REQUEST);
                    }
                    byte[] imageBytes = utility.getExcelImageByRow(sheet, r, 0);
                    String fileName = firstName + (lastName != null && lastName.isEmpty() ? ('_' + lastName) : "") + ".png";
                    MultipartFile imageFile = (imageBytes != null) ? utility.convertBytesToMultipartFile(imageBytes, fileName) : null;
                    if (imageFile != null) {
                        List<FileInfoResponse> fileInfoImage = utility.uploadFile(Constant.Directory.PEOPLE, imageFile, servletRequest);
                        image = fileInfoImage.get(0).getFileName();
                        savedFiles.addAll(fileInfoImage);
                    }
                    EmployeeCreateRequest employee = new EmployeeCreateRequest();
                    employee.setImage(image);
                    employee.setFirstName(firstName);
                    employee.setLastName(lastName);
                    employee.setGender(gender);
                    employee.setDob(dob);
                    employee.setAge(age);
                    employee.setPhone(phone);
                    employee.setEmail(email);
                    employee.setAddress(address);
                    employee.setNationality(nationality);
                    createEmployee(employee);
                }
            }
            if (r == null || r == 0) {
                throw new ApiException("Employee must contain at least one item.", HttpStatus.BAD_REQUEST);
            }
            return r;
        } catch (Exception e) {
            for (FileInfoResponse savedFile : savedFiles) {
                try {
                    Path path = Paths.get(savedFile.getFilePath()).toAbsolutePath().normalize();
                    Files.delete(path);
                } catch (IOException ioException) {
                    System.out.println("Failed to rollback file: " + savedFile + ", " + ioException.getMessage());
                }
            }
            String msg = e.getMessage();
            if (r != null && r != 0) {
                msg = "Row #" + (r + 1) + ": " + msg;
            }
            throw new ApiException(msg, HttpStatus.BAD_REQUEST);
        }
    }
}
