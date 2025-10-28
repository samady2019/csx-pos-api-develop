package kh.com.csx.posapi.dto.report.peopleReport;

import jakarta.persistence.Tuple;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private Long id;
    private String companyEn;
    private String companyKh;
    private String nameEn;
    private String nameKh;
    private String gender;
    private String contactPerson;
    private String phone;
    private String email;
    private Integer totalSales;
    private Double totalAmount;
    private Double totalPaid;
    private Double totalBalance;

    public CustomerResponse(Tuple data) {
        this.id             = ((Number) data.get("id")).longValue();
        this.companyEn      = (String) data.get("companyEn");
        this.companyKh      = (String) data.get("companyKh");
        this.nameEn         = (String) data.get("nameEn");
        this.nameKh         = (String) data.get("nameKh");
        this.gender         = (String) data.get("gender");
        this.contactPerson  = (String) data.get("contactPerson");
        this.phone          = (String) data.get("phone");
        this.email          = (String) data.get("email");
        this.totalSales     = ((Number) data.get("totalSales")).intValue();
        this.totalAmount    = ((Number) data.get("totalAmount")).doubleValue();
        this.totalPaid      = ((Number) data.get("totalPaid")).doubleValue();
        this.totalBalance   = ((Number) data.get("totalBalance")).doubleValue();
    }
}
