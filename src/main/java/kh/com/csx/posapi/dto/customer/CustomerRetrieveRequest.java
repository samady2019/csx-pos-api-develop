package kh.com.csx.posapi.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRetrieveRequest extends FilterDTO {
    private Long id;

    @Size(max = 255, message = "Company (EN) must be at most 255 characters")
    private String companyEn;

    @Size(max = 255, message = "Company (KH) must be at most 255 characters")
    private String companyKh;

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
}
