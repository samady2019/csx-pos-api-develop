package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_ref")
public class OrderRefEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "biller_id")
    private Long billerId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "po")
    private Integer po = 1;

    @Column(name = "pr")
    private Integer pr = 1;

    @Column(name = "p")
    private Integer p = 1;

    @Column(name = "so")
    private Integer so = 1;

    @Column(name = "sr")
    private Integer sr = 1;

    @Column(name = "s")
    private Integer s = 1;

    @Column(name = "pos")
    private Integer pos = 1;

    @Column(name = "bill")
    private Integer bill = 1;

    @Column(name = "sc")
    private Integer sc = 1;

    @Column(name = "aj")
    private Integer aj = 1;

    @Column(name = "tr")
    private Integer tr = 1;

    @Column(name = "ex")
    private Integer ex = 1;

    @Column(name = "pay")
    private Integer pay = 1;
}
