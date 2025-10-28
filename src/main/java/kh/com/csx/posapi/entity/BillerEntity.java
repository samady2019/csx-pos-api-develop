package kh.com.csx.posapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "billers")
public class BillerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "company_en", length = 255)
    private String companyEn;

    @Column(name = "company_kh", length = 255)
    private String companyKh;

    @Column(name = "name_en", length = 255)
    private String nameEn;

    @Column(name = "name_kh", length = 255)
    private String nameKh;

    @Column(name = "vat_no", length = 255)
    private String vatNo;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "state", length = 255)
    private String state;

    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Column(name = "address_en", length = 255)
    private String addressEn;

    @Column(name = "address_kh", length = 255)
    private String addressKh;

    @Column(name = "country", length = 255)
    private String country;

    @Lob
    @Column(name = "description")
    private String description;
}
