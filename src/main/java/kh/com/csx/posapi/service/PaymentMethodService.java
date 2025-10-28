package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.paymentMethod.*;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.PaymentMethodEntity;
import kh.com.csx.posapi.repository.PaymentMethodRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final Utility utility;

    public PaymentMethodResponse getPaymentMethodById(Long id) {
        PaymentMethodEntity paymentMethodEntity = paymentMethodRepository.findById(id).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
        return PaymentMethodResponse.builder().paymentMethod(paymentMethodEntity).build();
    }

    public List<PaymentMethodResponse> getListPaymentMethods(PaymentMethodRetrieveRequest request) {
        List<PaymentMethodEntity> paymentMethodEntities = paymentMethodRepository.findListByFilter(request);
        List<PaymentMethodResponse> paymentMethodResponses = new ArrayList<>();
        for (PaymentMethodEntity paymentMethodEntity : paymentMethodEntities) {
            paymentMethodResponses.add(PaymentMethodResponse.builder().paymentMethod(paymentMethodEntity).build());
        }
        return paymentMethodResponses;
    }

    public Page<PaymentMethodResponse> getAllPaymentMethods(PaymentMethodRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<PaymentMethodEntity> paymentMethodEntities = paymentMethodRepository.findAllByFilter(request, pageable);
        return paymentMethodEntities.map(paymentMethodEntity -> PaymentMethodResponse.builder().paymentMethod(paymentMethodEntity).build());
    }

    public PaymentMethodResponse createPaymentMethod(PaymentMethodCreateRequest request) {
        if (!Constant.PaymentMethodType.VALID.contains(request.getType().trim())) {
            throw new ApiException("Invalid payment method type. " + Constant.PaymentMethodType.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (request.getStatus() == null) {
            request.setStatus(Constant.ActiveStatus.DEFAULT);
        } else if (!Constant.ActiveStatus.VALID_STATUSES.contains(request.getStatus())) {
            throw new ApiException("Invalid payment method status. " + Constant.ActiveStatus.NOTE, HttpStatus.BAD_REQUEST);
        }
        PaymentMethodEntity paymentMethod = new PaymentMethodEntity();
        paymentMethod.setName(request.getName().trim());
        paymentMethod.setDescription(request.getDescription());
        paymentMethod.setType(request.getType().trim());
        paymentMethod.setStatus(request.getStatus());
        try {
            PaymentMethodEntity savedPaymentMethod = paymentMethodRepository.save(paymentMethod);
            return getPaymentMethodById(savedPaymentMethod.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public PaymentMethodResponse updatePaymentMethod(PaymentMethodUpdateRequest request) {
        PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
        if (!Constant.PaymentMethodType.VALID.contains(request.getType().trim())) {
            throw new ApiException("Invalid payment method type. " + Constant.PaymentMethodType.NOTE, HttpStatus.BAD_REQUEST);
        }
        if (request.getStatus() != null) {
            if (!Constant.ActiveStatus.VALID_STATUSES.contains(request.getStatus())) {
                throw new ApiException("Invalid payment method status. " + Constant.ActiveStatus.NOTE, HttpStatus.BAD_REQUEST);
            }
            paymentMethod.setStatus(request.getStatus());
        }
        paymentMethod.setName(request.getName().trim());
        paymentMethod.setDescription(request.getDescription());
        paymentMethod.setType(request.getType().trim());
        try {
            paymentMethodRepository.save(paymentMethod);
            return getPaymentMethodById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deletePaymentMethod(PaymentMethodDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deletePaymentMethod(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deletePaymentMethod(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Payment method deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deletePaymentMethod(Long id) {
        PaymentMethodEntity paymentMethod = paymentMethodRepository.findById(id).orElseThrow(() -> new ApiException("Payment method not found.", HttpStatus.BAD_REQUEST));
        if (paymentMethodRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete payment method '" + paymentMethod.getName() + "'. Payment method is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            paymentMethodRepository.delete(paymentMethod);
            return new BaseResponse("Payment method '" + paymentMethod.getName() + "' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
