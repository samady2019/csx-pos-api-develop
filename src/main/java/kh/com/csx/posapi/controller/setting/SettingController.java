package kh.com.csx.posapi.controller.setting;

import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.dto.setting.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.SettingService;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/setting")
@RequiredArgsConstructor
public class SettingController {
    private final SettingService settingService;
    private final Utility utility;

    @GetMapping("/system")
    public BaseResponse getSystem() {
        SettingResponse settingResponse = settingService.retrieveSystem();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(settingResponse);
        baseResponse.setMessage("System settings retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/system")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-SYSTEM_SETTINGS')")
    public BaseResponse updateSystem(@Valid @RequestBody SettingRequest request) {
        SettingResponse settingResponse = settingService.updateSystem(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(settingResponse);
        baseResponse.setMessage("System settings updated successfully.");
        return baseResponse;
    }

    @GetMapping("/pos")
    public BaseResponse getPos() {
        PosSettingResponse posSettingResponse = settingService.retrievePos();
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(posSettingResponse);
        baseResponse.setMessage("Pos settings retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/pos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('GENERAL-SETTINGS-POS_SETTINGS')")
    public BaseResponse updatePos(@Valid @RequestBody PosSettingRequest request) {
        PosSettingResponse posSettingResponse = settingService.updatePos(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(posSettingResponse);
        baseResponse.setMessage("Pos settings updated successfully.");
        return baseResponse;
    }

    @PostMapping("/upload")
    public BaseResponse uploadFile(@RequestPart("type") String type, @RequestPart("file") MultipartFile file, HttpServletRequest servletRequest) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(utility.uploadFile(type, file, servletRequest));
        baseResponse.setMessage("File uploaded successfully.");
        return baseResponse;
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@RequestParam("type") String type, @RequestParam(value = "quality", required = false) String quality, @PathVariable String fileName, HttpServletRequest servletRequest) {
        try {
            Resource file = utility.downloadFile(type, fileName, quality);
            FileInfoResponse fileInfo = utility.getFileInfo(type, fileName, quality, servletRequest);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileInfo.getFileType()));
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
            return ResponseEntity.ok().headers(headers).body(file);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/download/system/{fileName}")
    public ResponseEntity<Resource> downloadSystemFile(@RequestParam("type") String type, @RequestParam(value = "quality", required = false) String quality, @PathVariable String fileName, HttpServletRequest servletRequest) {
        try {
            Resource file = utility.downloadFile(type, fileName, quality, true);
            FileInfoResponse fileInfo = utility.getSystemFileInfo(type, fileName, quality, servletRequest);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileInfo.getFileType()));
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
            return ResponseEntity.ok().headers(headers).body(file);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/file/{fileName}")
    public BaseResponse fileInfo(@RequestParam("type") String type, @RequestParam(value = "quality", required = false) String quality, @PathVariable String fileName, HttpServletRequest servletRequest) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(utility.getFileInfo(type, fileName, quality, servletRequest));
        baseResponse.setMessage("File information.");
        return baseResponse;
    }

    @PostMapping("/upload-ftp")
    public BaseResponse receiveFtp(@RequestPart("file") MultipartFile file, HttpServletRequest servletRequest) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(utility.uploadFtp(file, servletRequest));
        baseResponse.setMessage("File FTP uploaded successfully.");
        return baseResponse;
    }

    @GetMapping("/download-ftp/{fileName}")
    public ResponseEntity<Resource> downloadFtpFile(@PathVariable String fileName, HttpServletRequest servletRequest) {
        try {
            Resource file = utility.downloadFileFtp(fileName);
            FileInfoResponse fileInfo = utility.getFileFtpInfo(fileName, servletRequest);
            HttpHeaders headers = new HttpHeaders();

            // headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentType(MediaType.parseMediaType(fileInfo.getFileType()));
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
            return ResponseEntity.ok().headers(headers).body(file);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/file-ftp/{fileName}")
    public BaseResponse fileFtpInfo(@PathVariable String fileName, HttpServletRequest servletRequest) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(utility.getFileFtpInfo(fileName, servletRequest));
        baseResponse.setMessage("File FTP information.");
        return baseResponse;
    }
}
