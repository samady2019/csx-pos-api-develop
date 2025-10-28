package kh.com.csx.posapi.dto.customerGroup;

import kh.com.csx.posapi.entity.CustomerGroupEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerGroupResponse {
    private CustomerGroupEntity customerGroup;
}
