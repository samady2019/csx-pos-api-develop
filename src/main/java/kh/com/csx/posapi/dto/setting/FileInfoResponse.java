package kh.com.csx.posapi.dto.setting;

import com.fasterxml.jackson.annotation.JsonFormat;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoResponse {
    private String directory;
    private String originalFilename;
    private String fileName;
    private String fileType;
    private String fileExtension;
    private String quality;
    private Long size;
    private String fileSizeInKB;
    private String filePath;
    private String downloadUrl;
    private String checksum;
    private String uploadedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    private LocalDateTime lastModifiedDate;
}
