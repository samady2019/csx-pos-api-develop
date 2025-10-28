package kh.com.csx.posapi.dto.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.entity.CustomerGroupEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private Long customerGroupId;
    private String companyEn;
    private String companyKh;
    private String nameEn;
    private String nameKh;
    private String vatNo;
    private String gender;
    private String contactPerson;
    private String phone;
    private String email;
    private String city;
    private String state;
    private String postalCode;
    private String addressEn;
    private String addressKh;
    private String country;
    private String description;
    private Long createdBy;
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime updatedAt;

    private CustomerGroupEntity customerGroup;
}
