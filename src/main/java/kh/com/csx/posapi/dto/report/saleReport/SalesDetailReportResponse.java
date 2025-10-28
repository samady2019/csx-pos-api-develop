package kh.com.csx.posapi.dto.report.saleReport;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesDetailReportResponse {
    private Long customerId;
    private String companyEn;
    private String companyKh;
    private String nameEn;
    private String nameKh;
    private String phone;
    private String email;
    private List<Sale> sales;

    @Data
    @NoArgsConstructor
    public static class Sale {
        private Long id;
        private Long saleOrderId;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATETIME_FORMAT)
        private LocalDateTime date;
        private String referenceNo;
        private String soReferenceNo;
        private String biller;
        private String warehouse;
        private String customer;
        private Double total;
        private Double shipping;
        private Double orderDiscount;
        private String orderDiscountId;
        private Double orderTax;
        private String orderTaxId;
        private Double grandTotal;
        private Double paid;
        private Double balance;
        private Double changes;
        private Double cost;
        private String status;
        private String paymentStatus;
        private String deliveryStatus;
        private String note;
        private String attachment;
        private String createdBy;
        private List<SaleItem> items;

        @Data
        @NoArgsConstructor
        public static class SaleItem {
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
            private Double unitPrice;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTime.DATE_FORMAT)
            private LocalDate expiry;
            private String discount;
            private Double itemDiscount;
            private String taxRate;
            private Double itemTax;
            private Double subtotal;
            private String description;

            public SaleItem(Tuple item) {
                this.productId     = ((Number) item.get("productId")).longValue();
                this.productCode   = (String) item.get("productCode");
                this.barCode       = (String) item.get("barCode");
                this.productNameEn = (String) item.get("productNameEn");
                this.productNameKh = (String) item.get("productNameKh");
                this.brand         = (String) item.get("brand");
                this.category      = (String) item.get("category");
                this.unitCode      = (String) item.get("unitCode");
                this.unitNameEn    = (String) item.get("unitNameEn");
                this.unitNameKh    = (String) item.get("unitNameKh");
                this.unitQuantity  = ((Number) item.get("unitQuantity")).doubleValue();
                this.unitPrice     = ((Number) item.get("unitPrice")).doubleValue();
                this.expiry        = (LocalDate) item.get("expiry");
                this.discount      = (String) item.get("discount");
                this.itemDiscount  = ((Number) item.get("itemDiscount")).doubleValue();
                this.taxRate       = (String) item.get("taxRate");
                this.itemTax       = ((Number) item.get("itemTax")).doubleValue();
                this.subtotal      = ((Number) item.get("subtotal")).doubleValue();
                this.description   = (String) item.get("description");
            }
        }

        public Sale(Tuple sale, List<Tuple> items) {
            this.id              = ((Number) sale.get("id")).longValue();
            this.saleOrderId     = sale.get("saleOrderId") != null ? ((Number) sale.get("saleOrderId")).longValue() : null;
            this.date            = (LocalDateTime) sale.get("date");
            this.referenceNo     = (String) sale.get("referenceNo");
            this.soReferenceNo   = (String) sale.get("soReferenceNo");
            this.biller          = (String) sale.get("biller");
            this.warehouse       = (String) sale.get("warehouse");
            this.customer        = (String) sale.get("customer");
            this.total           = ((Number) sale.get("total")).doubleValue();
            this.shipping        = ((Number) sale.get("shipping")).doubleValue();
            this.orderDiscount   = ((Number) sale.get("orderDiscount")).doubleValue();
            this.orderDiscountId = (String) sale.get("orderDiscountId");
            this.orderTax        = ((Number) sale.get("orderTax")).doubleValue();
            this.orderTaxId      = (String) sale.get("orderTaxId");
            this.grandTotal      = ((Number) sale.get("grandTotal")).doubleValue();
            this.paid            = ((Number) sale.get("paid")).doubleValue();
            this.balance         = ((Number) sale.get("balance")).doubleValue();
            this.changes         = ((Number) sale.get("changes")).doubleValue();
            this.cost            = ((Number) sale.get("cost")).doubleValue();
            this.status          = (String) sale.get("status");
            this.paymentStatus   = (String) sale.get("paymentStatus");
            this.deliveryStatus  = (String) sale.get("deliveryStatus");
            this.note            = (String) sale.get("note");
            this.attachment      = (String) sale.get("attachment");
            this.createdBy       = (String) sale.get("createdBy");
            this.items           = new ArrayList<>();
            items.forEach(item -> this.items.add(new SaleItem(item)));
        }
    }

    public SalesDetailReportResponse(Tuple customer, List<Sale> sales) {
        this.customerId = ((Number) customer.get("customerId")).longValue();
        this.companyEn  = (String) customer.get("companyEn");
        this.companyKh  = (String) customer.get("companyKh");
        this.nameEn     = (String) customer.get("nameEn");
        this.nameKh     = (String) customer.get("nameKh");
        this.phone      = (String) customer.get("phone");
        this.email      = (String) customer.get("email");
        this.sales      = sales;
    }
}
