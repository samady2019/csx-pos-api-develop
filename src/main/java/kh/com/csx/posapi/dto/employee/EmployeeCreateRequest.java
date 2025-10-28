package kh.com.csx.posapi.dto.employee;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCreateRequest {

    @Size(max = 255, message = "Image should not exceed {max} characters.")
    private String image;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name should not exceed {max} characters.")
    private String firstName;

    @Size(max = 50, message = "Last name should not exceed {max} characters.")
    private String lastName;

    @NotBlank(message = "Gender is required")
    @Size(max = 50, message = "Gender should not exceed {max} characters.")
    private String gender;

    private LocalDate dob;

    private Integer age;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone should not exceed {max} characters.")
    private String phone;

    @Email(message = "Email is not valid")
    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email should not exceed {max} characters.")
    private String email;

    @Size(max = 255, message = "Address should not exceed {max} characters.")
    private String address;

    @Size(max = 255, message = "Nationality should not exceed {max} characters.")
    private String nationality;

}
