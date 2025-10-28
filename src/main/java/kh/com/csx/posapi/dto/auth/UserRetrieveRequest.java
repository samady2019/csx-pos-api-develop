package kh.com.csx.posapi.dto.auth;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRetrieveRequest extends FilterDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String gender;
    private String phone;
    private String email;
    private String userType;
    private String status;
}
