
package kh.com.csx.posapi.dto.auth;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialPermissionsRequest {
    private Set<Integer> specialPermissions;
}
