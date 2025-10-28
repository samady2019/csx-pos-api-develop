package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tax_items")
public class TaxItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction", length = 50)
    private String transaction;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "tax_rate_declare")
    private Double taxRateDeclare;
}
