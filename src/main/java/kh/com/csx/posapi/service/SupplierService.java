package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.supplier.*;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.SupplierEntity;
import kh.com.csx.posapi.repository.SupplierRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final Utility utility;

    public SupplierResponse getSupplierById(Long id) {
        SupplierEntity supplierEntity = supplierRepository.findById(id).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        return SupplierResponse.builder().supplier(supplierEntity).build();
    }

    public List<SupplierResponse> getSuppliersTerm(SupplierRetrieveRequest request) {
        if (request.getTerm() == null || request.getTerm().trim().isEmpty()) {
            throw new ApiException("No suppliers found.", HttpStatus.BAD_REQUEST);
        }
        List<SupplierEntity> supplierEntities = supplierRepository.findByTerm(request.getTerm().trim());
        List<SupplierResponse> supplierResponses = new ArrayList<>();
        for (SupplierEntity supplierEntity : supplierEntities) {
            supplierResponses.add(SupplierResponse.builder().supplier(supplierEntity).build());
        }
        return supplierResponses;
    }

    public List<SupplierResponse> getListSuppliers(SupplierRetrieveRequest request) {
        if (request.getGender() != null) {
            request.setGender(utility.determineGender(request.getGender()));
        }
        List<SupplierEntity> supplierEntities = supplierRepository.findListByFilter(request);
        List<SupplierResponse> supplierResponses = new ArrayList<>();
        for (SupplierEntity supplierEntity : supplierEntities) {
            supplierResponses.add(SupplierResponse.builder().supplier(supplierEntity).build());
        }
        return supplierResponses;
    }

    public Page<SupplierResponse> getAllSuppliers(SupplierRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id, companyEn");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        if (request.getGender() != null) {
            request.setGender(utility.determineGender(request.getGender()));
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<SupplierEntity> supplierEntities = supplierRepository.findAllByFilter(request, pageable);
        return supplierEntities.map(supplierEntity -> SupplierResponse.builder().supplier(supplierEntity).build());
    }

    public SupplierResponse createSupplier(SupplierCreateRequest request) {
        if ((request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) && supplierRepository.existsByContactPerson(request.getContactPerson().trim())) {
            throw new ApiException("Contact person already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getPhone() != null && !request.getPhone().trim().isEmpty()) && supplierRepository.existsByPhone(request.getPhone().trim())) {
            throw new ApiException("Phone number already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getEmail() != null && !request.getEmail().trim().isEmpty()) && supplierRepository.existsByEmail(request.getEmail().trim())) {
            throw new ApiException("Email already exists.", HttpStatus.BAD_REQUEST);
        }
        if (request.getGender() == null || request.getGender().trim().isEmpty()) {
            request.setGender(Constant.Gender.FEMALE);
        }
        SupplierEntity supplier = new SupplierEntity();
        supplier.setCompanyEn(request.getCompanyEn());
        supplier.setCompanyKh(request.getCompanyKh());
        supplier.setNameEn(request.getNameEn());
        supplier.setNameKh(request.getNameKh());
        supplier.setVatNo(request.getVatNo());
        supplier.setGender(utility.determineGender(request.getGender()));
        supplier.setContactPerson(request.getContactPerson() != null ? request.getContactPerson().trim() : null);
        supplier.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        supplier.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
        supplier.setCity(request.getCity());
        supplier.setState(request.getState());
        supplier.setPostalCode(request.getPostalCode());
        supplier.setAddressEn(request.getAddressEn());
        supplier.setAddressKh(request.getAddressKh());
        supplier.setCountry(request.getCountry());
        supplier.setDescription(request.getDescription());
        try {
            SupplierEntity savedSupplier = supplierRepository.save(supplier);
            return getSupplierById(savedSupplier.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public SupplierResponse updateSupplier(SupplierUpdateRequest request) {
        SupplierEntity supplier = supplierRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        if ((request.getContactPerson() != null && !request.getContactPerson().trim().isEmpty()) && !supplier.getContactPerson().equals(request.getContactPerson().trim()) && supplierRepository.existsByContactPerson(request.getContactPerson().trim())) {
            throw new ApiException("Contact person already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getPhone() != null && !request.getPhone().trim().isEmpty()) && !supplier.getPhone().equals(request.getPhone().trim()) && supplierRepository.existsByPhone(request.getPhone().trim())) {
            throw new ApiException("Phone number already exists.", HttpStatus.BAD_REQUEST);
        }
        if ((request.getEmail() != null && !request.getEmail().trim().isEmpty()) && !supplier.getEmail().equals(request.getEmail().trim()) && supplierRepository.existsByEmail(request.getEmail().trim())) {
            throw new ApiException("Email already exists.", HttpStatus.BAD_REQUEST);
        }
        supplier.setCompanyEn(request.getCompanyEn().trim());
        supplier.setCompanyKh(request.getCompanyKh().trim());
        supplier.setNameEn(request.getNameEn().trim());
        supplier.setNameKh(request.getNameKh().trim());
        supplier.setVatNo(request.getVatNo().trim());
        supplier.setGender(utility.determineGender(request.getGender()));
        supplier.setContactPerson(request.getContactPerson().trim());
        supplier.setPhone(request.getPhone().trim());
        supplier.setEmail(request.getEmail().trim());
        supplier.setCity(request.getCity().trim());
        supplier.setState(request.getState().trim());
        supplier.setPostalCode(request.getPostalCode().trim());
        supplier.setAddressEn(request.getAddressEn().trim());
        supplier.setAddressKh(request.getAddressKh().trim());
        supplier.setCountry(request.getCountry().trim());
        supplier.setDescription(request.getDescription());
        try {
            supplierRepository.save(supplier);
            return getSupplierById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteSupplier(SupplierDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteSupplier(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteSupplier(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Supplier deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteSupplier(Long id) {
        SupplierEntity supplier = supplierRepository.findById(id).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        if (supplierRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete supplier '" + supplier.getCompanyEn() + "'. Supplier is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            supplierRepository.delete(supplier);
            return new BaseResponse("Supplier '" + supplier.getCompanyEn() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public Integer importSupplier(SupplierImportRequest request) {
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
                    String  companyEn         = utility.getCellValue(row.getCell(0), String.class);
                    String  companyKh         = utility.getCellValue(row.getCell(1), String.class);
                    String  nameEn            = utility.getCellValue(row.getCell(2), String.class);
                    String  nameKh            = utility.getCellValue(row.getCell(3), String.class);
                    String  vatNo             = utility.getCellValue(row.getCell(4), String.class);
                    String  gender            = utility.getCellValue(row.getCell(5), String.class);
                    String  contactPerson     = utility.getCellValue(row.getCell(6), String.class);
                    String  phone             = utility.getCellValue(row.getCell(7), String.class);
                    String  email             = utility.getCellValue(row.getCell(8), String.class);
                    String  city              = utility.getCellValue(row.getCell(9), String.class);
                    String  state             = utility.getCellValue(row.getCell(10), String.class);
                    String  postalCode        = utility.getCellValue(row.getCell(11), String.class);
                    String  addressEn         = utility.getCellValue(row.getCell(12), String.class);
                    String  addressKh         = utility.getCellValue(row.getCell(13), String.class);
                    String  country           = utility.getCellValue(row.getCell(14), String.class);
                    String  description       = utility.getCellValue(row.getCell(15), String.class);
                    SupplierCreateRequest supplier = new SupplierCreateRequest();
                    supplier.setCompanyEn(companyEn);
                    supplier.setCompanyKh(companyKh);
                    supplier.setNameEn(nameEn);
                    supplier.setNameKh(nameKh);
                    supplier.setVatNo(vatNo);
                    supplier.setGender(gender);
                    supplier.setContactPerson(contactPerson);
                    supplier.setPhone(phone);
                    supplier.setEmail(email);
                    supplier.setCity(city);
                    supplier.setState(state);
                    supplier.setPostalCode(postalCode);
                    supplier.setAddressEn(addressEn);
                    supplier.setAddressKh(addressKh);
                    supplier.setCountry(country);
                    supplier.setDescription(description);
                    utility.validateRequest(supplier);
                    createSupplier(supplier);
                }
            }
            if (r == null || r == 0) {
                throw new ApiException("Supplier must contain at least one item.", HttpStatus.BAD_REQUEST);
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
