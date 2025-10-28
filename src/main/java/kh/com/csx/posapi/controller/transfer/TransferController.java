package kh.com.csx.posapi.controller.transfer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.setting.OrderRefRequest;
import kh.com.csx.posapi.dto.setting.OrderRefResponse;
import kh.com.csx.posapi.dto.transfer.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.service.TransferService;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;
    private final Utility utility;

    @GetMapping("/sequenceNo")
    public BaseResponse sequenceNo(@Valid OrderRefRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(new OrderRefResponse(utility.getReferenceNo(request.getBillerId(), Constant.ReferenceKey.TR)));
        baseResponse.setMessage("Sequence number retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-TRANSFERS-RETRIEVE')")
    public BaseResponse getTransfer(TransferRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Transfer ID is required.", HttpStatus.BAD_REQUEST);
        }
        BaseResponse baseResponse = new BaseResponse();
        TransferResponse transferResponse = transferService.getTransferById(request.getId());
        baseResponse.setData(transferResponse);
        baseResponse.setMessage("Transfer retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-TRANSFERS-RETRIEVE')")
    public BaseResponse getAllTransfers(TransferRetrieveRequest filter) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(transferService.getAllTransfers(filter));
        baseResponse.setMessage("Transfers retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-TRANSFERS-CREATE')")
    public BaseResponse createTransfer(@Valid @RequestBody TransferCreateRequest request) {
        TransferResponse transferResponse = transferService.createTransfer(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(transferResponse);
        baseResponse.setMessage("Transfer created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-TRANSFERS-UPDATE')")
    public BaseResponse updateTransfer(@Valid @RequestBody TransferUpdateRequest request) {
        TransferResponse transferResponse = transferService.updateTransfer(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(transferResponse);
        baseResponse.setMessage("Transfer updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-TRANSFERS-DELETE')")
    public BaseResponse deleteTransfer(@Valid @RequestBody TransferDeleteRequest request) {
        return transferService.deleteTransfer(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('INVENTORY-TRANSFERS-CREATE')")
    public BaseResponse importTransfer(@Valid @ModelAttribute TransferImportRequest request, HttpServletRequest servletRequest) {
        transferService.importTransfer(request, servletRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Transfer imported successfully.");
        return baseResponse;
    }
}
