package kh.com.csx.posapi.controller.employee;

import jakarta.servlet.http.HttpServletRequest;
import kh.com.csx.posapi.dto.employee.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/retrieve")
    public BaseResponse retrieveEmployee(EmployeeRetrieveRequest request) {
        if (request.getId() == null) {
            throw new ApiException("Employee ID is required.", HttpStatus.BAD_REQUEST);
        }
        EmployeeResponse employeeResponse = employeeService.getEmployeeByID(request.getId());
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(employeeResponse);
        baseResponse.setMessage("Employee retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/list")
    public BaseResponse retrieveListEmployees(EmployeeRetrieveRequest request) {
        List<EmployeeResponse> employeesResponse = employeeService.getListEmployees(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(employeesResponse);
        baseResponse.setMessage("Employees retrieved successfully.");
        return baseResponse;
    }

    @GetMapping("/retrieveAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-EMPLOYEES-RETRIEVE')")
    public BaseResponse retrieveAllEmployees(EmployeeRetrieveRequest request) {
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(employeeService.getAllEmployees(request));
        baseResponse.setMessage("Employees retrieved successfully.");
        return baseResponse;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-EMPLOYEES-CREATE')")
    public BaseResponse createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse employeeResponse = employeeService.createEmployee(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(employeeResponse);
        baseResponse.setMessage("Employee created successfully.");
        return baseResponse;
    }

    @PostMapping("/update")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-EMPLOYEES-UPDATE')")
    public BaseResponse updateEmployee(@Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse employeeResponse = employeeService.updateEmployee(request);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(employeeResponse);
        baseResponse.setMessage("Employee updated successfully.");
        return baseResponse;
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-EMPLOYEES-DELETE')")
    public BaseResponse deleteEmployee(@Valid @RequestBody EmployeeDeleteRequest request) {
        return employeeService.deleteEmployee(request);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('USER-EMPLOYEES-CREATE')")
    public BaseResponse importEmployee(@Valid @ModelAttribute EmployeeImportRequest request, HttpServletRequest servletRequest) {
        Integer rows = employeeService.importEmployee(request, servletRequest);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage("Total " + rows + " employee(s) imported successfully.");
        return baseResponse;
    }

}
