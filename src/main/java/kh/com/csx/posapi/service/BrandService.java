package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.brand.*;
import kh.com.csx.posapi.entity.BrandEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.BrandRepository;
import kh.com.csx.posapi.repository.ProductRepository;
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
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final Utility utility;

    public ResponseBrandDTO createBrand(CreateBrandDTO request) {
        if (brandRepository.existsByCode(request.getCode().trim())) {
            throw new ApiException("Brand code already exists.", HttpStatus.BAD_REQUEST);
        }
        BrandEntity brand = new BrandEntity();
        brand.setCode(request.getCode().trim());
        brand.setName(request.getName().trim());
        if (request.getStatus() == null) {
            brand.setStatus(Constant.ActiveStatus.DEFAULT);
        } else {
            if (!Constant.ActiveStatus.VALID_STATUSES.contains(request.getStatus())) {
                throw new ApiException(Constant.ActiveStatus.NOTE, HttpStatus.BAD_REQUEST);
            }
            brand.setStatus(request.getStatus());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        brand.setCreatedBy(userEntity.getUserId());
        brand.setCreatedAt(LocalDateTime.now());
        try {
            BrandEntity savedBrand = brandRepository.save(brand);
            return ResponseBrandDTO.builder()
                    .brandId(savedBrand.getBrandId())
                    .code(savedBrand.getCode())
                    .name(savedBrand.getName())
                    .status(savedBrand.getStatus())
                    .createdAt(savedBrand.getCreatedAt())
                    .updatedAt(savedBrand.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseBrandDTO updateBrand(UpdateBrandDTO request) {
        BrandEntity existingBrand = brandRepository.findById(request.getBrandId()).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
        if (brandRepository.existsByCodeAndBrandIdNot(request.getCode().trim(), existingBrand.getBrandId())) {
            throw new ApiException("Brand code already exists.", HttpStatus.BAD_REQUEST);
        }
        existingBrand.setCode(request.getCode().trim());
        existingBrand.setName(request.getName().trim());
        if (request.getStatus() != null) {
            if (!Constant.ActiveStatus.VALID_STATUSES.contains(request.getStatus())) {
                throw new ApiException(Constant.ActiveStatus.NOTE, HttpStatus.BAD_REQUEST);
            }
            existingBrand.setStatus(request.getStatus());
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        existingBrand.setUpdatedBy(userEntity.getUserId());
        existingBrand.setUpdatedAt(LocalDateTime.now());
        try {
            BrandEntity updatedBrand = brandRepository.save(existingBrand);
            return ResponseBrandDTO.builder()
                    .brandId(updatedBrand.getBrandId())
                    .code(updatedBrand.getCode())
                    .name(updatedBrand.getName())
                    .status(updatedBrand.getStatus())
                    .createdAt(updatedBrand.getCreatedAt())
                    .updatedAt(updatedBrand.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteBrand(DeleteBrandDTO request) {
        ID rs = ID.id(request.getBrandId());
        try {
            if (rs.isSingle()) {
                return deleteBrand(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteBrand(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Brand deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteBrand(Long id) {
        BrandEntity brand = brandRepository.findById(id).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
        if (productRepository.existsByBrandId(id)) {
            throw new ApiException("Cannot delete brand as it is assigned to one or more products.", HttpStatus.BAD_REQUEST);
        }
        try {
            brandRepository.delete(brand);
            return new BaseResponse("Brand '" + brand.getName() + " (" + brand.getCode() + ")' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseBrandDTO getBrandById(RetrieveBrandDTO retrieveBrandDTO) {
        BrandEntity brand = brandRepository.findById(retrieveBrandDTO.getBrandId()).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
        return ResponseBrandDTO.builder()
                .brandId(brand.getBrandId())
                .code(brand.getCode())
                .name(brand.getName())
                .status(brand.getStatus())
                .createdAt(brand.getCreatedAt())
                .updatedAt(brand.getUpdatedBy() == null ? null : brand.getUpdatedAt())
                .build();
    }

    public List<ResponseBrandDTO> getListBrands(RetrieveBrandDTO request) {
        List<BrandEntity> brands = brandRepository.findListByFilter(request);
        return brands.stream()
                .map(brand -> ResponseBrandDTO.builder()
                        .brandId(brand.getBrandId())
                        .code(brand.getCode())
                        .name(brand.getName())
                        .status(brand.getStatus())
                        .createdAt(brand.getCreatedAt())
                        .updatedAt(brand.getUpdatedAt())
                        .updatedAt(brand.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public Page<ResponseBrandDTO> getAllBrands(RetrieveBrandDTO request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("code");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<BrandEntity> brands = brandRepository.findAllByFilter(request, pageable);
        return brands.map(brand -> ResponseBrandDTO.builder()
                        .brandId(brand.getBrandId())
                        .code(brand.getCode())
                        .name(brand.getName())
                        .status(brand.getStatus())
                        .createdAt(brand.getCreatedAt())
                        .updatedAt(brand.getUpdatedAt())
                        .updatedAt(brand.getUpdatedAt())
                        .build());
    }

    public long countBrands() {
        return brandRepository.count();
    }

    public boolean brandExists(Long brandId) {
        return brandRepository.existsById(brandId);
    }

    @Transactional
    public Integer importBrand(ImportBrandDTO request) {
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
                    String  code   = utility.getCellValue(row.getCell(0), String.class);
                    String  name   = utility.getCellValue(row.getCell(1), String.class);
                    Integer status = utility.getCellValue(row.getCell(2), Integer.class);
                    if (code == null || code.isEmpty()) {
                        throw new ApiException("Brand code is required.", HttpStatus.BAD_REQUEST);
                    }
                    if (name == null || name.isEmpty()) {
                        throw new ApiException("Brand name is required.", HttpStatus.BAD_REQUEST);
                    }
                    CreateBrandDTO brand = new CreateBrandDTO();
                    brand.setCode(code);
                    brand.setName(name);
                    brand.setStatus(status);
                    createBrand(brand);
                }
            }
            if (r == null || r == 0) {
                throw new ApiException("Brand must contain at least one item.", HttpStatus.BAD_REQUEST);
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
