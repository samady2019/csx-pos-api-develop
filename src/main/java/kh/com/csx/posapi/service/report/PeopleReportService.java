package kh.com.csx.posapi.service.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.repository.report.PeopleReportRepository;
import kh.com.csx.posapi.dto.report.peopleReport.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.utility.Utility;
import org.springframework.data.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PeopleReportService {
    private final PeopleReportRepository peopleReportRepository;
    private final Utility utility;

    public Page<SupplierResponse> suppliers(PeopleRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("id, companyEn");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            if (request.getGender() != null) {
                request.setGender(utility.determineGender(request.getGender()));
            }
            Page<Tuple> result = peopleReportRepository.findSuppliers(request, pageable);
            return result.map(SupplierResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<CustomerResponse> customers(PeopleRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("id, companyEn");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            if (request.getGender() != null) {
                request.setGender(utility.determineGender(request.getGender()));
            }
            Page<Tuple> result = peopleReportRepository.findCustomers(request, pageable);
            return result.map(CustomerResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<SalesmanResponse> salesman(UserRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("id");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if ((request.getStartDate() != null && !request.getStartDate().isEmpty()) || (request.getEndDate() != null && !request.getEndDate().isEmpty())) {
                if ((request.getStartDate() == null || request.getStartDate().isEmpty()) || (request.getEndDate() == null || request.getEndDate().isEmpty())) {
                    throw new ApiException("Please check start date and end date.", HttpStatus.BAD_REQUEST);
                }
                request.setStart(utility.convertToLocalDateTime(request.getStartDate()));
                request.setEnd(utility.convertToLocalDateTime(request.getEndDate()));
            }
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            if (request.getGender() != null) {
                request.setGender(utility.determineGender(request.getGender()));
            }
            Page<Tuple> result = peopleReportRepository.findSalesman(request, pageable);
            return result.map(SalesmanResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Page<UserResponse> users(UserRequest request) {
        try {
            if (request.getSortBy() == null || request.getSortBy().trim().isEmpty()) {
                request.setSortBy("id");
            }
            if (request.getOrderBy() == null || request.getOrderBy().trim().isEmpty()) {
                request.setOrderBy(Constant.OrderBy.ASC);
            }
            Pageable pageable = utility.initPagination(request.getPage(), request.getSize(), request.getSortBy(), request.getOrderBy());
            if (request.getGender() != null) {
                request.setGender(utility.determineGender(request.getGender()));
            }
            Page<Tuple> result = peopleReportRepository.findUsers(request, pageable);
            return result.map(UserResponse::new);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
