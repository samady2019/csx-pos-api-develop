package kh.com.csx.posapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.biller.BillerDeleteRequest;
import kh.com.csx.posapi.dto.language.*;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final Utility utility;

    @Autowired
    private ObjectMapper objectMapper;

    public LanguageResponse getLanguageById(Long id) {
        try {
            LanguageEntity languageEntity = languageRepository.findById(id).orElseThrow(() -> new ApiException("Language not found.", HttpStatus.BAD_REQUEST));
            return LanguageResponse.builder().language(languageEntity).build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<LanguageResponse> getAllLanguages(LanguageRetrieveRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("code");
            }
            if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Page<LanguageEntity> languageEntities = languageRepository.findAllByFilter(request, pageable);
            return languageEntities.map(languageEntity -> LanguageResponse.builder().language(languageEntity).build());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public List<Map<String, Object>> getListLanguages(LanguageRetrieveRequest request) {
        try {
            String langCode = request.getLangCode() != null ? request.getLangCode().trim().toLowerCase() : null;
            if (langCode != null && !Constant.Language.VALID.contains(langCode)) {
                throw new ApiException("Invalid language code: '" + langCode + "'", HttpStatus.BAD_REQUEST);
            }
            List<LanguageEntity> languages = languageRepository.findAll();
            List<Map<String, Object>> result = objectMapper.convertValue(languages, new TypeReference<>() {});
            if (langCode != null && !langCode.isEmpty()) {
                String columnName = Constant.Language.KEY.get(langCode);
                result.forEach(map -> map.keySet().removeIf(key -> !key.equals("id") && !key.equals("code") && !key.equals(columnName)));
            }
            return result;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public LanguageResponse createLanguage(LanguageCreateRequest request) {
        if (languageRepository.existsByCode(request.getCode().trim())) {
            throw new ApiException("Language code: '" + request.getCode().trim() + "' already exists.", HttpStatus.BAD_REQUEST);
        }
        try {
            LanguageEntity language = new LanguageEntity();
            language.setCode(request.getCode() != null ? request.getCode().trim() : null);
            language.setKhmer(request.getKhmer() != null ? request.getKhmer().trim() : null);
            language.setEnglish(request.getEnglish() != null ? request.getEnglish().trim() : null);
            language.setChinese(request.getChinese() != null ? request.getChinese().trim() : null);
            language.setThai(request.getThai() != null ? request.getThai().trim() : null);
            language.setVietnamese(request.getVietnamese() != null ? request.getVietnamese().trim() : null);
            LanguageEntity savedLanguage = languageRepository.save(language);
            return getLanguageById(savedLanguage.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public LanguageResponse updateLanguage(LanguageUpdateRequest request) {
        LanguageEntity language = languageRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Language not found.", HttpStatus.BAD_REQUEST));
        if (languageRepository.existsByCodeAndIdNot(request.getCode().trim(), language.getId())) {
            throw new ApiException("Language code already exists.", HttpStatus.BAD_REQUEST);
        }
        try {
            language.setCode(request.getCode() != null ? request.getCode().trim() : null);
            language.setKhmer(request.getKhmer() != null ? request.getKhmer().trim() : null);
            language.setEnglish(request.getEnglish() != null ? request.getEnglish().trim() : null);
            language.setChinese(request.getChinese() != null ? request.getChinese().trim() : null);
            language.setThai(request.getThai() != null ? request.getThai().trim() : null);
            language.setVietnamese(request.getVietnamese() != null ? request.getVietnamese().trim() : null);
            languageRepository.save(language);
            return getLanguageById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteLanguage(LanguageDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteLanguage(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteLanguage(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Language deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteLanguage(Long id) {
        LanguageEntity language = languageRepository.findById(id).orElseThrow(() -> new ApiException("Language not found.", HttpStatus.BAD_REQUEST));
        try {
            languageRepository.delete(language);
            return new BaseResponse("Language deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public Integer importLanguage(LanguageImportRequest request) {
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
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    throw new ApiException("The Excel file is empty.", HttpStatus.BAD_REQUEST);
                }
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    throw new ApiException("Invalid file format: Missing header row.", HttpStatus.BAD_REQUEST);
                }
                Map<String, Integer> columnIndexMap = new HashMap<>();
                for (Cell cell : headerRow) {
                    String cellValue = utility.getCellValue(cell, String.class);
                    if (cellValue != null && Constant.Language.KEY.containsKey(cellValue.toLowerCase())) {
                        columnIndexMap.put(Constant.Language.KEY.get(cellValue.toLowerCase()), cell.getColumnIndex());
                    }
                }
                if (columnIndexMap.isEmpty()) {
                    throw new ApiException("No valid language columns found in the file.", HttpStatus.BAD_REQUEST);
                }
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    r = row.getRowNum();
                    String code = utility.getCellValue(row.getCell(0), String.class);
                    if (code == null || code.isEmpty()) {
                        throw new ApiException("Language key code is required.", HttpStatus.BAD_REQUEST);
                    }
                    LanguageEntity language = languageRepository.findFirstByCode(code).orElse(new LanguageEntity());
                    language.setCode(code);
                    for (Map.Entry<String, Integer> entry : columnIndexMap.entrySet()) {
                        String languageKey = entry.getKey();
                        Integer columnIndex = entry.getValue();
                        String languageValue = utility.getCellValue(row.getCell(columnIndex), String.class);
                        if (languageValue != null && !languageValue.isEmpty()) {
                            switch (languageKey) {
                                case "khmer"      -> language.setKhmer(languageValue);
                                case "english"    -> language.setEnglish(languageValue);
                                case "chinese"    -> language.setChinese(languageValue);
                                case "thai"       -> language.setThai(languageValue);
                                case "vietnamese" -> language.setVietnamese(languageValue);
                            }
                        }
                    }
                    languageRepository.save(language);
                }
            }
            if (r == null || r == 0) {
                throw new ApiException("Category must contain at least one item.", HttpStatus.BAD_REQUEST);
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
