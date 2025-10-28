package kh.com.csx.posapi.controller.report;

import kh.com.csx.posapi.service.report.PeopleReportService;
import kh.com.csx.posapi.dto.report.peopleReport.*;
import kh.com.csx.posapi.model.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report/people")
@RequiredArgsConstructor
public class PeopleReportController {
    private final PeopleReportService peopleReportService;

    @GetMapping("suppliers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PURCHASE-REPORTS-SUPPLIERS')")
    public BaseResponse suppliers(PeopleRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(peopleReportService.suppliers(request));
        baseResponse.setMessage("Suppliers report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("customers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-CUSTOMERS')")
    public BaseResponse customers(PeopleRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(peopleReportService.customers(request));
        baseResponse.setMessage("Customers report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("salesman")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('SALE-REPORTS-SALESMAN')")
    public BaseResponse salesman(UserRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(peopleReportService.salesman(request));
        baseResponse.setMessage("Salesman report retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-REPORTS-USERS')")
    public BaseResponse users(UserRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(peopleReportService.users(request));
        baseResponse.setMessage("Users report retrieved successfully.");
        return baseResponse;
    }
}
