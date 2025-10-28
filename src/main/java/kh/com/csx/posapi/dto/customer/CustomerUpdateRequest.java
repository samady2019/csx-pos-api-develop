package kh.com.csx.posapi.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdateRequest {

    private Long id;

    @NotNull(message = "Customer group ID is required.")
    private Long customerGroupId;

    @Size(max = 255, message = "Company (EN) must be at most 255 characters")
    private String companyEn;

    @Size(max = 255, message = "Company (KH) must be at most 255 characters")
    private String companyKh;

    @NotEmpty(message = "Name (EN) is required")
    @Size(max = 255, message = "Name (EN) must be at most 255 characters")
    private String nameEn;

    @Size(max = 255, message = "Name (KH) must be at most 255 characters")
    private String nameKh;

    @Size(max = 255, message = "VAT number should not exceed {max} characters.")
    private String vatNo;

    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String gender;

    @Size(max = 50, message = "Contact must be at most 50 characters")
    private String contactPerson;

    @Size(max = 50, message = "Phone must be at most 50 characters")
    private String phone;

    @Email(message = "Email must be a valid email address containing '@'")
    @Size(max = 50, message = "Email must be at most 50 characters")
    private String email;

    private String city;
    private String state;
    private String postalCode;
    private String addressEn;
    private String addressKh;
    private String country;
    private String description;
    private Long createdBy;

}
