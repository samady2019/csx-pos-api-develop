package kh.com.csx.posapi.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FilterDTO {

    private Long user;
    private List<Long> bIds;
    private List<Long> whIds;
    private Integer page = 1;
    private Integer size;
    private String sortBy;
    private String orderBy;
    private LocalDateTime start;
    private LocalDateTime end;
    private String term;

}
