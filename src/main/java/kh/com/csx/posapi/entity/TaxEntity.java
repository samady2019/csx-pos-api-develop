package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "taxs")
public class TaxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tax_declaration_id")
    private Long taxDeclarationId;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction", length = 50)
    private String transaction;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "reference_no", length = 50)
    private String referenceNo;

    @Column(name = "tax_reference_no", length = 50)
    private String taxReferenceNo;

    @Column(name = "company_en", length = 255)
    private String companyEn;

    @Column(name = "company_kh", length = 255)
    private String companyKh;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "name_kh", length = 255)
    private String nameKh;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "vat_no", length = 255)
    private String vatNo;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "total")
    private Double total;

    @Column(name = "order_discount")
    private Double orderDiscount;

    @Column(name = "order_tax")
    private Double orderTax;

    @Column(name = "shipping")
    private Double shipping;

    @Column(name = "grand_total")
    private Double grandTotal;

    @Column(name = "exchange_rate")
    private Double exchangeRate;

    @Column(name = "note")
    private String note;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"),
        @JoinColumn(name = "transaction",    referencedColumnName = "transaction")
    })
    private List<TaxItemEntity> transactionItems;
}
