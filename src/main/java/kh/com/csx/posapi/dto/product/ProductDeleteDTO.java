package kh.com.csx.posapi.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductDeleteDTO {
    @NotNull(message = "Product ID is required.")
    private Object productId;
}
