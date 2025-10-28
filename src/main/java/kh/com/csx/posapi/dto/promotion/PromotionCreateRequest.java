package kh.com.csx.posapi.dto.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionCreateRequest {
    @NotBlank(message = "Name is required.")
    @Size(max = 255, message = "Name should not exceed {max} characters.")
    private String name;

    @Size(max = 255, message = "Billers should not exceed {max} characters.")
    private String billers;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    @NotNull(message = "Start date is required.")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATE_FORMAT)
    @NotNull(message = "End date is required.")
    private LocalDate endDate;

    private String description;

    @NotEmpty(message = "Promotion must contain at last on item")
    private List<PromotionItemRequest> items;
}
