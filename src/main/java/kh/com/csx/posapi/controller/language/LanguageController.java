package kh.com.csx.posapi.controller.language;

import kh.com.csx.posapi.dto.language.LanguageResponse;
import kh.com.csx.posapi.dto.language.LanguageRetrieveRequest;
import kh.com.csx.posapi.dto.language.LanguageDeleteRequest;
import kh.com.csx.posapi.dto.language.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.LanguageService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/language")
@RequiredArgsConstructor
public class LanguageController {
    private final LanguageService languageService;
    private final Utility utility;

    @GetMapping("/retrieve")
    public BaseResponse retrieveLanguage(LanguageRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Language ID is required.", HttpStatus.BAD_REQUEST);
        }
        LanguageResponse languageResponse = languageService.getLanguageById(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(languageResponse);
        baseResponse.setMessage("Language retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-LANGUAGE')")
    public BaseResponse retrieveAllLanguages(LanguageRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(languageService.getAllLanguages(request));
        baseResponse.setMessage("Languages retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListLanguages(LanguageRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(languageService.getListLanguages(request));
        baseResponse.setMessage("Languages retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-LANGUAGE')")
    public BaseResponse createLanguage(@Valid @RequestBody LanguageCreateRequest request) {
        LanguageResponse languageResponse = languageService.createLanguage(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(languageResponse);
        baseResponse.setMessage("Language created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-LANGUAGE')")
    public BaseResponse updateLanguage(@Valid @RequestBody LanguageUpdateRequest request) {
        LanguageResponse languageResponse = languageService.updateLanguage(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(languageResponse);
        baseResponse.setMessage("Language updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-LANGUAGE')")
    public BaseResponse deleteLanguage(@Valid @RequestBody LanguageDeleteRequest request) {
        return languageService.deleteLanguage(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-LANGUAGE')")
    public BaseResponse importLanguage(@Valid @ModelAttribute LanguageImportRequest request) {
        Integer rows = languageService.importLanguage(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Total " + rows + " keyword languages imported successfully.");
        return baseResponse;
    }
}
