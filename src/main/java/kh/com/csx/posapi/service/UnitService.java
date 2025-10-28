package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.entity.UnitEntity;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.repository.UnitRepository;
import kh.com.csx.posapi.dto.unit.*;
import kh.com.csx.posapi.utility.Utility;
import org.springframework.data.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final Utility utility;

    public UnitResponse getUnitByID(Long id) {
        UnitEntity unitEntity = unitRepository.findByUnitId(id).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        return UnitResponse.builder().unit(unitEntity).build();
    }

    public List<UnitResponse> getUnitsByParentID(Long id) {
        List<UnitEntity> unitEntities = unitRepository.findByPunitId(id);
        List<UnitResponse> unitResponses = new ArrayList<>();
        for (UnitEntity unitEntity : unitEntities) {
            unitResponses.add(UnitResponse.builder().unit(unitEntity).build());
        }
        return unitResponses;
    }

    public List<UnitResponse> getListUnits() {
        List<UnitEntity> unitEntities = unitRepository.findAll();
        List<UnitResponse> unitResponses = new ArrayList<>();
        for (UnitEntity unitEntity : unitEntities) {
            unitResponses.add(UnitResponse.builder().unit(unitEntity).build());
        }
        return unitResponses;
    }

    public Page<UnitResponse> getAllUnits(UnitRetrieveRequest request) {
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("unitId");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<UnitEntity> unitEntities = unitRepository.findAllByFilter(request, pageable);
        return unitEntities.map(unitEntity -> UnitResponse.builder().unit(unitEntity).build());
    }

    public List<UnitResponse> getAllParentUnits() {
        List<UnitEntity> unitEntities = unitRepository.findByPunitIdIsNull();
        List<UnitResponse> unitResponses = new ArrayList<>();
        for (UnitEntity unitEntity : unitEntities) {
            unitResponses.add(UnitResponse.builder().unit(unitEntity).build());
        }
        return unitResponses;
    }

    public UnitConversionResponse convertToBaseUnit(UnitConversionRequest request) {
        UnitEntity unit = unitRepository.findByUnitId(request.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        if (request.getValue() == 0) {
            throw new ApiException("Value cannot be null and must be not equal to zero.", HttpStatus.BAD_REQUEST);
        }
        UnitConversionResponse.UnitInfo unitInfo = UnitConversionResponse.UnitInfo.builder()
                .unitId(unit.getUnitId())
                .unitCode(unit.getUnitCode())
                .unitNameEn(unit.getUnitNameEn())
                .unitNameKh(unit.getUnitNameKh())
                .value(unit.getValue())
                .description(unit.getDescription())
                .build();
        UnitConversionResponse.UnitInfo baseUnitInfo = null;
        double convertedValue = request.getValue();
        if (unit.getPunitId() != null) {
            UnitEntity baseUnit = unitRepository.findByUnitId(unit.getPunitId()).orElseThrow(() -> new ApiException("Base unit not found.", HttpStatus.BAD_REQUEST));
            baseUnitInfo = UnitConversionResponse.UnitInfo.builder()
                    .unitId(baseUnit.getUnitId())
                    .unitCode(baseUnit.getUnitCode())
                    .unitNameEn(baseUnit.getUnitNameEn())
                    .unitNameKh(baseUnit.getUnitNameKh())
                    .value(baseUnit.getValue())
                    .description(baseUnit.getDescription())
                    .build();

            convertedValue = request.getValue() * unit.getValue();
        }
        return UnitConversionResponse.builder()
                .unit(unitInfo)
                .baseUnit(baseUnitInfo)
                .value(request.getValue())
                .converted(convertedValue)
                .build();
    }

    public UnitConversionResponse convertFromBaseUnit(UnitConversionRequest request) {
        UnitEntity unit = unitRepository.findByUnitId(request.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        if (request.getValue() == 0) {
            throw new ApiException("Value cannot be null and must be not equal to zero.", HttpStatus.CONFLICT);
        }
        UnitConversionResponse.UnitInfo unitInfo = UnitConversionResponse.UnitInfo.builder()
                .unitId(unit.getUnitId())
                .unitCode(unit.getUnitCode())
                .unitNameEn(unit.getUnitNameEn())
                .unitNameKh(unit.getUnitNameKh())
                .value(unit.getValue())
                .description(unit.getDescription())
                .build();
        UnitConversionResponse.UnitInfo baseUnitInfo = null;
        double convertedValue = request.getValue();
        if (unit.getPunitId() != null) {
            UnitEntity baseUnit = unitRepository.findByUnitId(unit.getPunitId()).orElseThrow(() -> new ApiException("Base unit not found.", HttpStatus.BAD_REQUEST));
            baseUnitInfo = UnitConversionResponse.UnitInfo.builder()
                    .unitId(baseUnit.getUnitId())
                    .unitCode(baseUnit.getUnitCode())
                    .unitNameEn(baseUnit.getUnitNameEn())
                    .unitNameKh(baseUnit.getUnitNameKh())
                    .value(baseUnit.getValue())
                    .description(baseUnit.getDescription())
                    .build();

            convertedValue = request.getValue() / (unit.getValue() != 0 ? unit.getValue() : 1);
        }
        return UnitConversionResponse.builder()
                .unit(unitInfo)
                .baseUnit(baseUnitInfo)
                .value(request.getValue())
                .converted(convertedValue)
                .build();
    }

    public UnitResponse createUnit(UnitCreateRequest request) {
        if (unitRepository.findFirstByUnitCode(request.getUnitCode().trim()).isPresent()) {
            throw new ApiException("Unit code already exists.", HttpStatus.CONFLICT);
        }
        if (request.getPunitId() != null) {
            if (unitRepository.findByUnitId(request.getPunitId()).isEmpty()) {
                throw new ApiException("Parent unit ID not found.", HttpStatus.CONFLICT);
            }
        }
        UnitEntity parentUnit = null;
        if (request.getPunitId() != null) {
            parentUnit = unitRepository.findByUnitId(request.getPunitId()).orElseThrow(() -> new ApiException("Parent unit ID not found.", HttpStatus.CONFLICT));
        }
        UnitEntity unit = new UnitEntity();
        unit.setPunitId(request.getPunitId());
        unit.setParentUnit(parentUnit);
        unit.setUnitCode(request.getUnitCode().trim());
        unit.setUnitNameEn(request.getUnitNameEn().trim());
        unit.setUnitNameKh(request.getUnitNameKh().trim());
        unit.setValue(request.getValue());
        unit.setDescription(request.getDescription());
        try {
            UnitEntity savedUnit = unitRepository.save(unit);
            return this.getUnitByID(savedUnit.getUnitId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public UnitResponse updateUnit(UnitUpdateRequest request) {
        UnitEntity unit = unitRepository.findByUnitId(request.getUnitId()).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        if (unitRepository.findFirstByUnitCodeAndUnitIdNot(request.getUnitCode(),request.getUnitId()).isPresent()) {
            throw new ApiException("Unit code already exists.", HttpStatus.CONFLICT);
        }
        if (request.getPunitId() != null) {
            if (unitRepository.findByUnitId(request.getPunitId()).isEmpty()) {
                throw new ApiException("Parent unit ID not found.", HttpStatus.CONFLICT);
            }
        }
        UnitEntity parentUnit = null;
        if (request.getPunitId() != null) {
            parentUnit = unitRepository.findByUnitId(request.getPunitId()).orElseThrow(() -> new ApiException("Parent unit ID not found.", HttpStatus.CONFLICT));
        }
        unit.setPunitId(request.getPunitId());
        unit.setParentUnit(parentUnit);
        unit.setUnitCode(request.getUnitCode().trim());
        unit.setUnitNameEn(request.getUnitNameEn().trim());
        unit.setUnitNameKh(request.getUnitNameKh().trim());
        unit.setValue(request.getValue());
        unit.setDescription(request.getDescription());
        try {
            unitRepository.save(unit);
            return this.getUnitByID(request.getUnitId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteUnit(UnitDeleteRequest request) {
        ID rs = ID.id(request.getUnitId());
        try {
            if (rs.isSingle()) {
                return deleteUnit(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteUnit(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Unit deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteUnit(Long id) {
        UnitEntity unit = unitRepository.findById(id).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        if (unitRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete unit '" + unit.getUnitNameEn() + " (" + unit.getUnitCode() + ")'. Unit is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            unitRepository.delete(unit);
            return new BaseResponse("Unit '" + unit.getUnitNameEn() + " (" + unit.getUnitCode() + ")' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
