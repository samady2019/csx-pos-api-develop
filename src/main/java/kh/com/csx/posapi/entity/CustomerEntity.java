package kh.com.csx.posapi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;
import static kh.com.csx.posapi.constant.Constant.DateTime.DATE_FORMAT;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_group_id")
    private Long customerGroupId;

    @Column(name = "company_en")
    @Size(max = 255, message = "Company (EN) must be at most 255 characters")
    private String companyEn;

    @Column(name = "company_kh")
    @Size(max = 255, message = "Company (KH) must be at most 255 characters")
    private String companyKh;

    @Column(name = "name_en")
    @NotEmpty(message = "Name (EN) is required")
    @Size(max = 255, message = "Name (EN) must be at most 255 characters")
    private String nameEn;

    @Column(name = "name_kh")
    @Size(max = 255, message = "Name (KH) must be at most 255 characters")
    private String nameKh;

    @Column(name = "vat_no", length = 255)
    private String vatNo;

    @Column(name = "gender")
    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String gender;

    @Column(name = "contact_person")
    @Size(max = 50, message = "Contact must be at most 50 characters")
    private String contactPerson;

    @Column(name = "phone")
    @Size(max = 50, message = "Phone must be at most 50 characters")
    private String phone;

    @Column(name = "email")
    @Email(message = "Email must be a valid email address containing '@'")
    @Size(max = 50, message = "Email must be at most 50 characters")
    private String email;

    private String city;
    private String state;
    private String postalCode;
    private String addressEn;
    private String addressKh;
    private String country;

    @Lob
    @Column(name = "description")
    private String description;

    private Long createdBy;
    private Long updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "customer_group_id", referencedColumnName = "id", insertable = false, updatable = false)
    private CustomerGroupEntity customerGroup;
}
