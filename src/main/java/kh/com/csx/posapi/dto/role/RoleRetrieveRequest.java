package kh.com.csx.posapi.dto.role;

import kh.com.csx.posapi.dto.FilterDTO;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleRetrieveRequest extends FilterDTO {
    private Integer roleId;
    private String name;
}
