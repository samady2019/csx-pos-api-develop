package kh.com.csx.posapi.service;

import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.warehouse.*;
import kh.com.csx.posapi.entity.SettingEntity;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.entity.WarehouseEntity;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.utility.Utility;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final Utility utility;

    public WarehouseResponse getWarehouseById(Long id) {
        WarehouseEntity warehouseEntity = warehouseRepository.findById(id).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        return WarehouseResponse.builder().warehouse(warehouseEntity).build();
    }

    public WarehouseResponse getWarehouseByCode(String code) {
        WarehouseEntity warehouseEntity = warehouseRepository.findFirstByCode(code).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        return WarehouseResponse.builder().warehouse(warehouseEntity).build();
    }

    public List<WarehouseResponse> getListWarehouses(WarehouseRetrieveRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }
        List<WarehouseEntity> warehouseEntities = warehouseRepository.findListByFilter(request);
        List<WarehouseResponse> warehouseResponses = new ArrayList<>();
        for (WarehouseEntity warehouseEntity : warehouseEntities) {
            warehouseResponses.add(WarehouseResponse.builder().warehouse(warehouseEntity).build());
        }
        return warehouseResponses;
    }

    public Page<WarehouseResponse> getAllWarehouses(WarehouseRetrieveRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
            request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
        }
        if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
            request.setSortBy("id");
        }
        if (request.getOrderBy() == null|| request.getOrderBy().trim().isEmpty()) {
            request.setOrderBy(Constant.OrderBy.ASC);
        }
        Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
        Page<WarehouseEntity> warehouseEntities = warehouseRepository.findAllByFilter(request, pageable);
        return warehouseEntities.map(warehouseEntity -> WarehouseResponse.builder().warehouse(warehouseEntity).build());
    }

    public WarehouseResponse createWarehouse(WarehouseCreateRequest request) {
        SettingEntity setting = utility.getSettings();
        if (setting.getMultiWarehouse().equals(Constant.NO) && warehouseRepository.existsBy()) {
            throw new ApiException("Multiple warehouses are not allowed.", HttpStatus.BAD_REQUEST);
        }
        if (warehouseRepository.existsByCode(request.getCode().trim())) {
            throw new ApiException("Warehouse code already exists.", HttpStatus.BAD_REQUEST);
        }
        if (warehouseRepository.existsByName(request.getName().trim())) {
            throw new ApiException("Warehouse name already exists.", HttpStatus.BAD_REQUEST);
        }
        if (request.getOverselling() == null) {
            request.setOverselling(Constant.Overselling.DEFAULT);
        } else if (!Constant.Overselling.VALID.contains(request.getOverselling())) {
            throw new ApiException("Invalid overselling value. " + Constant.Overselling.NOTE, HttpStatus.BAD_REQUEST);
        }
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setCode(request.getCode().trim());
        warehouse.setName(request.getName().trim());
        warehouse.setFax(request.getFax().trim());
        warehouse.setPhone(request.getPhone().trim());
        warehouse.setEmail(request.getEmail().trim());
        warehouse.setAddress(request.getAddress().trim());
        warehouse.setMap(request.getMap().trim());
        warehouse.setOverselling(request.getOverselling());
        try {
            WarehouseEntity savedWarehouse = warehouseRepository.save(warehouse);
            return getWarehouseById(savedWarehouse.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public WarehouseResponse updateWarehouse(WarehouseUpdateRequest request) {
        WarehouseEntity warehouse = warehouseRepository.findById(request.getId()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        if (warehouseRepository.existsByCodeAndIdNot(request.getCode().trim(), warehouse.getId())) {
            throw new ApiException("Warehouse code already exists.", HttpStatus.BAD_REQUEST);
        }
        if (warehouseRepository.existsByNameAndIdNot(request.getName().trim(), warehouse.getId())) {
            throw new ApiException("Warehouse name already exists.", HttpStatus.BAD_REQUEST);
        }
        if (request.getOverselling() != null) {
            if (!Constant.Overselling.VALID.contains(request.getOverselling())) {
                throw new ApiException("Invalid overselling value. " + Constant.Overselling.NOTE, HttpStatus.BAD_REQUEST);
            }
            warehouse.setOverselling(request.getOverselling());
        }
        warehouse.setCode(request.getCode().trim());
        warehouse.setName(request.getName().trim());
        warehouse.setFax(request.getFax().trim());
        warehouse.setPhone(request.getPhone().trim());
        warehouse.setEmail(request.getEmail().trim());
        warehouse.setAddress(request.getAddress().trim());
        warehouse.setMap(request.getMap().trim());
        warehouse.setOverselling(request.getOverselling());
        try {
            warehouseRepository.save(warehouse);
            return getWarehouseById(request.getId());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public BaseResponse deleteWarehouse(WarehouseDeleteRequest request) {
        ID rs = ID.id(request.getId());
        try {
            if (rs.isSingle()) {
                return deleteWarehouse(rs.id());
            } else {
                int success = 0, fail = 0;
                for (Long id : rs.ids()) {
                    try {
                        deleteWarehouse(id);
                        success++;
                    } catch (Exception e) {
                        fail++;
                    }
                }
                return new BaseResponse(String.format("Warehouse deletion completed. Successfully deleted: %d, Failed: %d.", success, fail));
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BaseResponse deleteWarehouse(Long id) {
        WarehouseEntity warehouse = warehouseRepository.findById(id).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        if (warehouseRepository.countReferences(id) > 0) {
            throw new ApiException("Cannot delete warehouse '" + warehouse.getName() + " (" + warehouse.getCode() + ")'. Warehouse is referenced in other records.", HttpStatus.BAD_REQUEST);
        }
        try {
            warehouseRepository.delete(warehouse);
            return new BaseResponse("Warehouse '" + warehouse.getName() + " (" + warehouse.getCode() + ")' deleted successfully.");
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
