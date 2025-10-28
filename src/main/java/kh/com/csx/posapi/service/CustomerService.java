package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.customer.*;
import kh.com.csx.posapi.entity.CustomerEntity;
import kh.com.csx.posapi.entity.CustomerGroupEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.CustomerGroupRepository;
import kh.com.csx.posapi.repository.CustomerRepository;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final Utility utility;

    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        customerGroupRepository.findById(request.getCustomerGroupId()).orElseThrow(() -> new ApiException("Customer group not found.", HttpStatus.BAD_REQUEST));
        if ((request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) && customerRepository.existsByContactPerson(request.getContactPerson().trim())) {
            throw new ApiException("Contact person already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getPhone() != null && !request.getPhone().trim().isEmpty()) && customerRepository.existsByPhone(request.getPhone().trim())) {
            throw new ApiException("Phone number already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getEmail() != null && !request.getEmail().trim().isEmpty()) && customerRepository.existsByEmail(request.getEmail().trim())) {
            throw new ApiException("Email already exists.", HttpStatus.BAD_REQUEST);
        }
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            request.setGender(Constant.Gender.FEMALE);
        }
        CustomerEntity customer = new CustomerEntity();
        customer.setCustomerGroupId(request.getCustomerGroupId());
        customer.setCompanyEn(request.getCompanyEn());
        customer.setCompanyKh(request.getCompanyKh());
        customer.setNameEn(request.getNameEn());
        customer.setNameKh(request.getNameKh());
        customer.setVatNo(request.getVatNo());
        customer.setGender(determineGender(request.getGender()));
        customer.setContactPerson(request.getContactPerson() != null ? request.getContactPerson().trim() : null);
        customer.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        customer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPostalCode(request.getPostalCode());
        customer.setAddressEn(request.getAddressEn());
        customer.setAddressKh(request.getAddressKh());
        customer.setCountry(request.getCountry());
        customer.setDescription(request.getDescription());
        setUserInfo(customer);
        try {
            CustomerEntity savedCustomer = customerRepository.save(customer);
            return buildResponseCustomerDTO(savedCustomer);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public CustomerResponse updateCustomer(CustomerUpdateRequest request) {
        CustomerEntity existingCustomer = customerRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));
        customerGroupRepository.findById(request.getCustomerGroupId()).orElseThrow(() -> new ApiException("Customer group not found.", HttpStatus.BAD_REQUEST));
        if ((request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) && !existingCustomer.getContactPerson().equals(request.getContactPerson().trim()) && customerRepository.existsByContactPerson(request.getContactPerson().trim())) {
            throw new ApiException("Contact person already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getPhone() != null && !request.getPhone().trim().isEmpty()) && !existingCustomer.getPhone().equals(request.getPhone().trim()) && customerRepository.existsByPhone(request.getPhone().trim())) {
            throw new ApiException("Phone number already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getEmail() != null && !request.getEmail().trim().isEmpty()) && !existingCustomer.getEmail().equals(request.getEmail().trim()) && customerRepository.existsByEmail(request.getEmail().trim())) {
            throw new ApiException("Email already exists.", HttpStatus.BAD_REQUEST);
        }
        existingCustomer.setCustomerGroupId(request.getCustomerGroupId());
        existingCustomer.setCompanyEn(request.getCompanyEn());
        existingCustomer.setCompanyKh(request.getCompanyKh());
        existingCustomer.setNameEn(request.getNameEn());
        existingCustomer.setNameKh(request.getNameKh());
        existingCustomer.setVatNo(request.getVatNo());
        existingCustomer.setGender(determineGender(request.getGender()));
        existingCustomer.setContactPerson(request.getContactPerson() != null ? request.getContactPerson().trim() : null);
        existingCustomer.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        existingCustomer.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        existingCustomer.setCity(request.getCity());
        existingCustomer.setState(request.getState());
        existingCustomer.setPostalCode(request.getPostalCode());
        existingCustomer.setAddressEn(request.getAddressEn());
        existingCustomer.setAddressKh(request.getAddressKh());
        existingCustomer.setCountry(request.getCountry());
        existingCustomer.setDescription(request.getDescription());

        setUserInfo(existingCustomer);
        try {
            CustomerEntity updatedCustomer = customerRepository.save(existingCustomer);
            return buildResponseCustomerDTO(updatedCustomer);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteCustomer(CustomerDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteCustomer(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteCustomer(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Customer deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteCustomer(Long id) {
        CustomerEntity customer = customerRepository.findById(id).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));
        if (customerRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete customer '" + ((customer.getCompanyEn() != null && !customer.getCompanyEn().trim().isEmpty()) ? customer.getCompanyEn() : customer.getNameEn()) + "'. Customer is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            customerRepository.delete(customer);
            return new BaseResponse("Customer '" + ((customer.getCompanyEn() != null && !customer.getCompanyEn().trim().isEmpty()) ? customer.getCompanyEn() : customer.getNameEn()) + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public CustomerResponse getCustomerById(CustomerRetrieveRequest customerRetrieveRequest) {
        CustomerEntity customer = customerRepository.findById(customerRetrieveRequest.getId()).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));
        return buildResponseCustomerDTO(customer);
    }

    public List<CustomerResponse> getCustomersTerm(CustomerRetrieveRequest request) {
        if (request.getTerm() == null || request.getTerm().trim().isEmpty()) {
            throw new ApiException("No customers found.", HttpStatus.BAD_REQUEST);
        }
        List<CustomerEntity> customers = customerRepository.findByTerm(request.getTerm().trim());
        return customers.stream().map(this::buildResponseCustomerDTO).collect(Collectors.toList());
    }

    public List<CustomerResponse> getListCustomers(CustomerRetrieveRequest filter) {
        List<CustomerEntity> customers = customerRepository.findListByFilter(filter);
        return customers.stream().map(this::buildResponseCustomerDTO).collect(Collectors.toList());
    }

    public Page<CustomerResponse> getAllCustomers(CustomerRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id, companyEn");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<CustomerEntity> customers = customerRepository.findAllByFilter(request, pageable);
        return customers.map(this::buildResponseCustomerDTO);
    }

    private void setUserInfo(CustomerEntity customer) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        if (customer.getCreatedAt() == null) {
            customer.setCreatedAt(LocalDateTime.now());
            customer.setCreatedBy(userEntity.getUserId());
        } else {
            customer.setUpdatedAt(LocalDateTime.now());
            customer.setUpdatedBy(userEntity.getUserId());
        }
    }

    private CustomerResponse buildResponseCustomerDTO(CustomerEntity customer) {
        LocalDateTime updatedAt = (customer.getUpdatedBy() == null) ? null : customer.getUpdatedAt();
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerGroupId(customer.getCustomerGroupId())
                .companyEn(customer.getCompanyEn())
                .companyKh(customer.getCompanyKh())
                .nameEn(customer.getNameEn())
                .nameKh(customer.getNameKh())
                .vatNo(customer.getVatNo())
                .gender(customer.getGender())
                .contactPerson(customer.getContactPerson())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .city(customer.getCity())
                .state(customer.getState())
                .postalCode(customer.getPostalCode())
                .addressEn(customer.getAddressEn())
                .addressKh(customer.getAddressKh())
                .country(customer.getCountry())
                .description(customer.getDescription())
                .createdBy(customer.getCreatedBy())
                .updatedBy(customer.getUpdatedBy())
                .createdAt(customer.getCreatedAt())
                .updatedAt(updatedAt)
                .customerGroup(customerGroupRepository.findById(customer.getCustomerGroupId()).orElse(null))
                .build();
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
            throw new ApiException("Gender is required", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public Integer importCustomer(CustomerImportRequest request) {
        Integer r = null;
        try {
            MultipartFile file = request.getFile();
            if (file == null || file.isEmpty()) {
                throw new ApiException("File is required.", HttpStatus.BAD_REQUEST);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
                throw new ApiException("Invalid file type. Only excel (.xlsx) file are allowed.", HttpStatus.BAD_REQUEST);
            }
            try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    r = row.getRowNum();
                    String  customerGroupName = utility.getCellValue(row.getCell(0), String.class);
                    Long    customerGroupId   = null;
                    String  companyEn         = utility.getCellValue(row.getCell(1), String.class);
                    String  companyKh         = utility.getCellValue(row.getCell(2), String.class);
                    String  nameEn            = utility.getCellValue(row.getCell(3), String.class);
                    String  nameKh            = utility.getCellValue(row.getCell(4), String.class);
                    String  vatNo             = utility.getCellValue(row.getCell(5), String.class);
                    String  gender            = utility.getCellValue(row.getCell(6), String.class);
                    String  contactPerson     = utility.getCellValue(row.getCell(7), String.class);
                    String  phone             = utility.getCellValue(row.getCell(8), String.class);
                    String  email             = utility.getCellValue(row.getCell(9), String.class);
                    String  city              = utility.getCellValue(row.getCell(10), String.class);
                    String  state             = utility.getCellValue(row.getCell(11), String.class);
                    String  postalCode        = utility.getCellValue(row.getCell(12), String.class);
                    String  addressEn         = utility.getCellValue(row.getCell(13), String.class);
                    String  addressKh         = utility.getCellValue(row.getCell(14), String.class);
                    String  country           = utility.getCellValue(row.getCell(15), String.class);
                    String  description       = utility.getCellValue(row.getCell(16), String.class);
                    if (customerGroupName == null || customerGroupName.isEmpty()) {
                        throw new ApiException("Customer group is required.", HttpStatus.BAD_REQUEST);
                    } else {
                        CustomerGroupEntity customerGroup = customerGroupRepository.findByNameIgnoreCase(customerGroupName).orElse(null);
                        if (customerGroup == null) {
                            throw new ApiException("Customer group not found.", HttpStatus.BAD_REQUEST);
                        }
                        customerGroupId = customerGroup.getId();
                    }
                    if (nameEn == null || nameEn.isEmpty()) {
                        throw new ApiException("Name (EN) is required.", HttpStatus.BAD_REQUEST);
                    }
                    CustomerCreateRequest customer = new CustomerCreateRequest();
                    customer.setCustomerGroupId(customerGroupId);
                    customer.setCompanyEn(companyEn);
                    customer.setCompanyKh(companyKh);
                    customer.setNameEn(nameEn);
                    customer.setNameKh(nameKh);
                    customer.setVatNo(vatNo);
                    customer.setGender(gender);
                    customer.setContactPerson(contactPerson);
                    customer.setPhone(phone);
                    customer.setEmail(email);
                    customer.setCity(city);
                    customer.setState(state);
                    customer.setPostalCode(postalCode);
                    customer.setAddressEn(addressEn);
                    customer.setAddressKh(addressKh);
                    customer.setCountry(country);
                    customer.setDescription(description);
                    createCustomer(customer);
                }
            }
            if (r == null || r == 0) {
                throw new ApiException("Customer must contain at least one item.", HttpStatus.BAD_REQUEST);
            }
            return r;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (r != null && r != 0) {
                msg = "Row #" + (r + 1) + ": " + msg;
            }
            throw new ApiException(msg, HttpStatus.BAD_REQUEST);
        }
    }
}


