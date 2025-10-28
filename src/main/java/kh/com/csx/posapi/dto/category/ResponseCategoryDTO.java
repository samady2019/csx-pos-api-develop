package kh.com.csx.posapi.dto.category;

import lombok.Data;

@Data
public class ResponseCategoryDTO {
    private String id;
    private ResponseCategoryDTO parentCategory;
    private String shortNameEn;
    private String shortNameKh;
    private String nameEn;
    private String nameKh;
    private String descriptionEn;
    private String descriptionKh;
    private String status;
}
