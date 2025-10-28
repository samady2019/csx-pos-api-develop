package kh.com.csx.posapi.dto.biller;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillerCreateRequest {
    @NotBlank(message = "Biller code is required")
    @Size(max = 50, message = "Biller code should not exceed {max} characters.")
    private String code;

    @NotBlank(message = "Company name (EN) is required")
    @Size(max = 255, message = "Company name (EN) should not exceed {max} characters.")
    private String companyEn;

    @Size(max = 255, message = "Company name (KH) should not exceed {max} characters.")
    private String companyKh;

    @Size(max = 255, message = "Name (EN) should not exceed {max} characters.")
    private String nameEn;

    @Size(max = 255, message = "Name (KH) should not exceed {max} characters.")
    private String nameKh;

    @Size(max = 255, message = "VAT number should not exceed {max} characters.")
    private String vatNo;

    @Size(max = 50, message = "Contact person should not exceed {max} characters.")
    private String contactPerson;

    @Size(max = 50, message = "Phone should not exceed {max} characters.")
    private String phone;

    @Email(message = "Email is not valid")
    @Size(max = 50, message = "Email should not exceed {max} characters.")
    private String email;

    @Size(max = 255, message = "City should not exceed {max} characters.")
    private String city;

    @Size(max = 255, message = "State should not exceed {max} characters.")
    private String state;

    @Size(max = 50, message = "Postal code should not exceed {max} characters.")
    private String postalCode;

    @Size(max = 255, message = "Address (EN) should not exceed {max} characters.")
    private String addressEn;

    @Size(max = 255, message = "Address (KH) should not exceed {max} characters.")
    private String addressKh;

    @Size(max = 255, message = "Country should not exceed {max} characters.")
    private String country;

    private String description;
}
