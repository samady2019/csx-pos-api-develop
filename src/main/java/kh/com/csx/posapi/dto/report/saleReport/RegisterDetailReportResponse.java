package kh.com.csx.posapi.dto.report.saleReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Tuple;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static kh.com.csx.posapi.constant.Constant.DateTime.DATETIME_FORMAT;

@Data
@Builder
@NoArgsConstructor
public class RegisterDetailReportResponse {
    private PosRegister    posRegister;
    private List<Payment>  payments;
    private Sale           sale;
    private List<Customer> customers;

    @Data
    @NoArgsConstructor
    public static class PosRegister {
        private Long id;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
        private LocalDateTime openedAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATETIME_FORMAT)
        private LocalDateTime closedAt;
        private Long userId;
        private String name;
        private String status;
        private Double cashInHand;
        private Double totalCash;
        private Integer totalCheques;
        private Integer totalCcSlips;
        private Double totalCashSubmitted;
        private Integer totalChequesSubmitted;
        private Integer totalCcSlipsSubmitted;
        private String transferOpenedBills;
        private String note;

        public PosRegister(Tuple register) {
            this.id                    = ((Number) register.get("id")).longValue();
            this.openedAt              = (LocalDateTime) register.get("openedAt");
            this.closedAt              = (LocalDateTime) register.get("closedAt");
            this.userId                = ((Number) register.get("userId")).longValue();
            this.name                  = (String) register.get("name");
            this.status                = (String) register.get("status");
            this.cashInHand            = ((Number) register.get("cashInHand")).doubleValue();
            this.totalCash             = ((Number) register.get("totalCash")).doubleValue();
            this.totalCheques          = ((Number) register.get("totalCheques")).intValue();
            this.totalCcSlips          = ((Number) register.get("totalCcSlips")).intValue();
            this.totalCashSubmitted    = ((Number) register.get("totalCashSubmitted")).doubleValue();
            this.totalChequesSubmitted = ((Number) register.get("totalChequesSubmitted")).intValue();
            this.totalCcSlipsSubmitted = ((Number) register.get("totalCcSlipsSubmitted")).intValue();
            this.transferOpenedBills   = (String) register.get("transferOpenedBills");
            this.note                  = (String) register.get("note");
        }
    }

    @Data
    @NoArgsConstructor
    public static class Payment {
        private Long id;
        private String name;
        private String type;
        private Double amount;

        public Payment(Tuple payment) {
            this.id     = ((Number) payment.get("id")).longValue();
            this.name   = (String) payment.get("name");
            this.type   = (String) payment.get("type");
            this.amount = ((Number) payment.get("amount")).doubleValue();
        }
    }

    @Data
    @NoArgsConstructor
    public static class Sale {
        private Double orderDiscount;
        private Double orderTax;
        private Double shipping;
        private Double grandTotal;
        private Double refund;
        private Double expense;
        private Long totalTransactions;
        private List<SaleItem> items = new ArrayList<>();

        @Data
        @NoArgsConstructor
        public static class SaleItem {
            private Long categoryId;
            private String code;
            private String name;
            private List<Product> products = new ArrayList<>();

            @Data
            @NoArgsConstructor
            public static class Product {
                private Long productId;
                private String productCode;
                private String barCode;
                private String productNameEn;
                private String productNameKh;
                private String brand;
                private String category;
                private String unitCode;
                private String unitNameEn;
                private String unitNameKh;
                private Double unitQuantity;
                private Double discount;
                private Double tax;
                private Double subtotal;

                public Product(Tuple product) {
                    this.productId     = ((Number) product.get("productId")).longValue();
                    this.productCode   = (String) product.get("productCode");
                    this.barCode       = (String) product.get("barCode");
                    this.productNameEn = (String) product.get("productNameEn");
                    this.productNameKh = (String) product.get("productNameKh");
                    this.brand         = (String) product.get("brand");
                    this.category      = (String) product.get("category");
                    this.unitQuantity  = ((Number) product.get("unitQuantity")).doubleValue();
                    this.discount      = ((Number) product.get("discount")).doubleValue();
                    this.tax           = ((Number) product.get("tax")).doubleValue();
                    this.subtotal      = ((Number) product.get("subtotal")).doubleValue();
                }
            }

            public SaleItem(Tuple category, List<Tuple> products) {
                this.categoryId = ((Number) category.get("categoryId")).longValue();
                this.code       = (String) category.get("code");
                this.name       = (String) category.get("name");
                this.products   = new ArrayList<>();
                products.forEach(item -> this.products.add(new Product(item)));
            }
        }

        public Sale(Tuple sale, List<SaleItem> items) {
            this.orderDiscount     = ((Number) sale.get("orderDiscount")).doubleValue();
            this.orderTax          = ((Number) sale.get("orderTax")).doubleValue();
            this.shipping          = ((Number) sale.get("shipping")).doubleValue();
            this.grandTotal        = ((Number) sale.get("grandTotal")).doubleValue();
            this.refund            = ((Number) sale.get("refund")).doubleValue();
            this.expense           = ((Number) sale.get("expense")).doubleValue();
            this.totalTransactions = ((Number) sale.get("totalTransactions")).longValue();
            this.items             = items;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Customer {
        private Long id;
        private String companyEn;
        private String companyKh;
        private String nameEn;
        private String nameKh;
        private String phone;
        private String email;
        private Long totalItems;
        private Double amount;

        public Customer(Tuple customer) {
            this.id         = ((Number) customer.get("id")).longValue();
            this.companyEn  = (String) customer.get("companyEn");
            this.companyKh  = (String) customer.get("companyKh");
            this.nameEn     = (String) customer.get("nameEn");
            this.nameKh     = (String) customer.get("nameKh");
            this.phone      = (String) customer.get("phone");
            this.email      = (String) customer.get("email");
            this.totalItems = ((Number) customer.get("totalItems")).longValue();
            this.amount     = ((Number) customer.get("amount")).doubleValue();
        }
    }

    public RegisterDetailReportResponse(PosRegister posRegister, List<Payment> payments, Sale sale, List<Customer> customers) {
        this.posRegister = posRegister;
        this.payments    = payments;
        this.sale        = sale;
        this.customers   = customers;
    }
}

