package kh.com.csx.posapi.dto.user;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest extends FilterDTO {
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String gender;
    private String phone;
    private String email;
    private String userType;
    private String status;

    public UserRequest(Long userId) {
        this.userId = userId;
    }
}
