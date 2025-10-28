package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import kh.com.csx.posapi.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vendors")
public class VendorEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "shop_name_en")
    private String shopNameEn;

    @Column(name = "shop_name_kh")
    private String shopNameKh;

    @Column(name = "address")
    private String address;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constant.DateTime.DATETIME_FORMAT)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
