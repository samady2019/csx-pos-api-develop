package kh.com.csx.posapi.entity;

import kh.com.csx.posapi.constant.Constant.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
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
@Table(name = "pos_settings")
public class PosSettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_limit")
    private Integer categoryLimit;

    @Column(name = "brand_limit")
    private Integer brandLimit;

    @Column(name = "product_limit")
    private Integer productLimit;

    @Column(name = "default_category")
    private Long defaultCategory;

    @Column(name = "default_brand")
    private Long defaultBrand;

    @Column(name = "show_category")
    private Integer showCategory;

    @Column(name = "show_quantity")
    private Integer showQuantity;

    @Column(name = "display_time")
    private Integer displayTime;

    @Column(name = "coupon_card")
    private Integer couponCard;

    @Column(name = "sale_due")
    private Integer saleDue;

    @Column(name = "pin_code", length = 20)
    private String pinCode;

    @Column(name = "pos_type", length = 20)
    private String posType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "last_modified")
    private LocalDateTime lastModified;
}
