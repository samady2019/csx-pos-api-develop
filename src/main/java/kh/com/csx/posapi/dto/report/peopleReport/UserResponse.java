package kh.com.csx.posapi.dto.report.peopleReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long userId;
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    private LocalDate dob;

    private String phone;
    private String email;
    private String username;
    private String userType;
    private String status;

    public UserResponse(Tuple data) {
        this.userId     = ((Number) data.get("userId")).longValue();
        this.employeeId = data.get("employeeId") != null ? ((Number) data.get("employeeId")).longValue() : null;
        this.firstName  = (String) data.get("firstName");
        this.lastName   = (String) data.get("lastName");
        this.gender     = (String) data.get("gender");
        this.dob        = (LocalDate) data.get("dob");
        this.phone      = (String) data.get("phone");
        this.email      = (String) data.get("email");
        this.username   = (String) data.get("username");
        this.userType   = (String) data.get("userType");
        this.status     = (String) data.get("status");
    }
}
