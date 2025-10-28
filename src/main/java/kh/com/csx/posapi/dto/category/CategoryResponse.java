package kh.com.csx.posapi.dto.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long categoryId;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Long createdBy;
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime updatedAt;

    private ParentCategoryResponse parentCategory;
    private List<CategoryResponse> subCategories;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ParentCategoryResponse {
        private Long categoryId;
        private String code;
        private String name;
        private String description;
        private Integer status;
        private Long createdBy;
        private Long updatedBy;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
        private LocalDateTime createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
        private LocalDateTime updatedAt;

    }
}
