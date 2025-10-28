package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "units")
public class UnitEntity {
    @Id
    @Column(name = "unit_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unitId;

    @Column(name = "p_unit_id")
    private Long punitId;

    @Column(name = "unit_code", nullable = false, unique = true)
    private String unitCode;

    @Column(name = "unit_name_en", nullable = false)
    private String unitNameEn;

    @Column(name = "unit_name_kh")
    private String unitNameKh;

    @Column(name = "value", nullable = false)
    private double value;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "p_unit_id", referencedColumnName = "unit_id", insertable=false, updatable=false)
    private UnitEntity parentUnit;
}
