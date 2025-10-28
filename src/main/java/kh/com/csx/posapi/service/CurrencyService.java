package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.CurrencyEntity;
import kh.com.csx.posapi.repository.CurrencyRepository;
import org.springframework.data.domain.*;
import kh.com.csx.posapi.dto.currency.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final Utility utility;

    public CurrencyResponse getCurrencyById(Long id) {
        CurrencyEntity currencyEntity = currencyRepository.findById(id).orElseThrow(() -> new ApiException("Currency not found.", HttpStatus.BAD_REQUEST));
        return CurrencyResponse.builder().currency(currencyEntity).build();
    }

    public CurrencyResponse getCurrencyByCode(String code) {
        CurrencyEntity currencyEntity = currencyRepository.findFirstByCode(code).orElseThrow(() -> new ApiException("Currency not found.", HttpStatus.BAD_REQUEST));
        return CurrencyResponse.builder().currency(currencyEntity).build();
    }

    public List<CurrencyResponse> getListCurrencies(CurrencyRetrieveRequest request) {
        List<CurrencyEntity> currencyEntities = currencyRepository.findListByFilter(request);
        List<CurrencyResponse> currencyResponses = new ArrayList<>();
        for (CurrencyEntity currencyEntity : currencyEntities) {
            currencyResponses.add(CurrencyResponse.builder().currency(currencyEntity).build());
        }
        return currencyResponses;
    }

    public Page<CurrencyResponse> getAllCurrencies(CurrencyRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<CurrencyEntity> currencyEntities = currencyRepository.findAllByFilter(request, pageable);
        return currencyEntities.map(currencyEntity -> CurrencyResponse.builder().currency(currencyEntity).build());
    }

    public CurrencyResponse createCurrency(CurrencyCreateRequest request) {
        if (currencyRepository.existsByCode(request.getCode().trim())) {
            throw new ApiException("Currency code already exists.", HttpStatus.BAD_REQUEST);
        }
        if (currencyRepository.existsByName(request.getName().trim())) {
            throw new ApiException("Currency name already exists.", HttpStatus.BAD_REQUEST);
        }
        if (currencyRepository.existsBySymbol(request.getSymbol().trim())) {
            throw new ApiException("Currency symbol already exists.", HttpStatus.BAD_REQUEST);
        }
        CurrencyEntity currency = new CurrencyEntity();
        currency.setCode(request.getCode().trim());
        currency.setName(request.getName().trim());
        currency.setSymbol(request.getSymbol().trim());
        currency.setRate(request.getRate());
        try {
            CurrencyEntity savedCurrency = currencyRepository.save(currency);
            return getCurrencyById(savedCurrency.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public CurrencyResponse updateCurrency(CurrencyUpdateRequest request) {
        CurrencyEntity currency = currencyRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Currency not found.", HttpStatus.BAD_REQUEST));
        if (currencyRepository.existsByCodeAndIdNot(request.getCode().trim(), currency.getId())) {
            throw new ApiException("Currency code already exists.", HttpStatus.BAD_REQUEST);
        }
        if (currencyRepository.existsByNameAndIdNot(request.getName().trim(), currency.getId())) {
            throw new ApiException("Currency name already exists.", HttpStatus.BAD_REQUEST);
        }
        if (currencyRepository.existsBySymbolAndIdNot(request.getSymbol().trim(), currency.getId())) {
            throw new ApiException("Currency symbol already exists.", HttpStatus.BAD_REQUEST);
        }
        currency.setCode(request.getCode().trim());
        currency.setName(request.getName().trim());
        currency.setSymbol(request.getSymbol().trim());
        currency.setRate(request.getRate());
        try {
            currencyRepository.save(currency);
            return getCurrencyById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteCurrency(CurrencyDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteCurrency(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteCurrency(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Currency deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteCurrency(Long id) {
        CurrencyEntity currency = currencyRepository.findById(id).orElseThrow(() -> new ApiException("Currency not found.", HttpStatus.BAD_REQUEST));
        if (currencyRepository.countReferences(currency.getId(), currency.getCode()) > 0) {
            throw new ApiException("Cannot delete currency '" + currency.getName() + " (" + currency.getCode() + ")'. Currency is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            currencyRepository.delete(currency);
            return new BaseResponse("Brand '" + currency.getName() + " (" + currency.getCode() + ")' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
