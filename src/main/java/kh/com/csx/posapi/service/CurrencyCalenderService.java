package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.currencyCalender.*;
import kh.com.csx.posapi.entity.SettingEntity;
import kh.com.csx.posapi.entity.CurrencyEntity;
import kh.com.csx.posapi.entity.CurrencyCalenderEntity;
import kh.com.csx.posapi.repository.CurrencyRepository;
import kh.com.csx.posapi.repository.CurrencyCalenderRepository;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CurrencyCalenderService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyCalenderRepository currencyCalenderRepository;
    private final Utility utility;

    public CurrencyCalenderResponse getCurrencyCalenderById(Long id) {
        CurrencyCalenderEntity currencyCalenderEntity = currencyCalenderRepository.findById(id).orElseThrow(() -> new ApiException("Currency rate not found.", HttpStatus.BAD_REQUEST));
        return CurrencyCalenderResponse.builder().currencyCalender(currencyCalenderEntity).build();
    }

    public CurrencyCalenderResponse getCurrencyCalenderByCurrencyId(Long currencyId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        CurrencyCalenderEntity currencyCalenderEntity = currencyCalenderRepository.findFirstByCurrencyIdAndDate(currencyId, date).orElseThrow(() -> new ApiException("Currency rate not found.", HttpStatus.BAD_REQUEST));
        return CurrencyCalenderResponse.builder().currencyCalender(currencyCalenderEntity).build();
    }

    public CurrencyCalenderResponse getCurrencyCalenderByCurrencyCode(String code, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        CurrencyCalenderEntity currencyCalenderEntity = currencyCalenderRepository.findFirstByCodeAndDate(code, date).orElseThrow(() -> new ApiException("Currency rate not found.", HttpStatus.BAD_REQUEST));
        return CurrencyCalenderResponse.builder().currencyCalender(currencyCalenderEntity).build();
    }

    public List<CurrencyCalenderResponse> getListCurrencyCalenders(CurrencyCalenderRetrieveRequest request) {
        if (request.getDate() == null) {
            request.setDate(LocalDate.now());
        }
        List<CurrencyCalenderEntity> currencyCalenderEntities = currencyCalenderRepository.findListByFilter(request);
        List<CurrencyCalenderResponse> currencyCalenderResponses = new ArrayList<>();
        for (CurrencyCalenderEntity currencyCalenderEntity : currencyCalenderEntities) {
            currencyCalenderResponses.add(CurrencyCalenderResponse.builder().currencyCalender(currencyCalenderEntity).build());
        }
        return currencyCalenderResponses;
    }

    public Page<CurrencyCalenderResponse> getAllCurrencyCalenders(CurrencyCalenderRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("currencyId, date");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.DESC);
        }
        if (request.getDate() == null) {
            request.setDate(LocalDate.now());
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<CurrencyCalenderEntity> currencyCalenderEntities = currencyCalenderRepository.findAllByFilter(request, pageable);
        return currencyCalenderEntities.map(currencyCalenderEntity -> CurrencyCalenderResponse.builder().currencyCalender(currencyCalenderEntity).build());
    }

    public CurrencyCalenderResponse createCurrencyCalender(CurrencyCalenderCreateRequest request) {
        CurrencyEntity currency = currencyRepository.findById(request.getCurrencyId()).orElseThrow(() -> new ApiException("Currency not found.", HttpStatus.BAD_REQUEST));
        CurrencyEntity baseCurrency = utility.getDefaultCurrency();
        if (baseCurrency.getId().equals(request.getCurrencyId())) {
            throw new ApiException("Exchange currency must be different from the base currency.", HttpStatus.BAD_REQUEST);
        }
        if (currencyCalenderRepository.existsByCurrencyIdAndDate(request.getCurrencyId(), request.getDate())) {
            throw new ApiException("Currency rate already exists.", HttpStatus.BAD_REQUEST);
        }
        CurrencyCalenderEntity currencyCalender = new CurrencyCalenderEntity();
        currencyCalender.setCurrencyId(request.getCurrencyId());
        currencyCalender.setDate(request.getDate());
        currencyCalender.setRate(utility.formatDecimal(request.getRate()));
        currencyCalender.setCurrency(currency);
        try {
            CurrencyCalenderEntity savedCurrencyCalender = currencyCalenderRepository.save(currencyCalender);
            return getCurrencyCalenderById(savedCurrencyCalender.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public CurrencyCalenderResponse updateCurrencyCalender(CurrencyCalenderUpdateRequest request) {
        CurrencyEntity currency = currencyRepository.findById(request.getCurrencyId()).orElseThrow(() -> new ApiException("Currency not found.", HttpStatus.BAD_REQUEST));
        CurrencyCalenderEntity currencyCalender = currencyCalenderRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Currency rate not found.", HttpStatus.BAD_REQUEST));
        CurrencyEntity baseCurrency = utility.getDefaultCurrency();
        if (baseCurrency.getId().equals(request.getCurrencyId())) {
            throw new ApiException("Exchange currency must be different from the base currency.", HttpStatus.BAD_REQUEST);
        }
        if (currencyCalenderRepository.existsByCurrencyIdAndDateAndIdNot(request.getCurrencyId(), request.getDate(), currencyCalender.getId())) {
            throw new ApiException("Currency rate already exists.", HttpStatus.BAD_REQUEST);
        }
        currencyCalender.setCurrencyId(request.getCurrencyId());
        currencyCalender.setDate(request.getDate());
        currencyCalender.setRate(utility.formatDecimal(request.getRate()));
        currencyCalender.setCurrency(currency);
        try {
            currencyCalenderRepository.save(currencyCalender);
            return getCurrencyCalenderById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteCurrencyCalender(CurrencyCalenderDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteCurrencyCalender(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteCurrencyCalender(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Currency rate deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteCurrencyCalender(Long id) {
        SettingEntity setting = utility.getSettings();
        CurrencyCalenderEntity currencyCalender = currencyCalenderRepository.findById(id).orElseThrow(() -> new ApiException("Currency rate not found.", HttpStatus.BAD_REQUEST));
        try {
            currencyCalenderRepository.delete(currencyCalender);
            return new BaseResponse("Currency rate " + setting.getDefaultCurrency() + "/" + currencyCalender.getCurrency().getCode() + " (" + currencyCalender.getDate() + ") deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
