package kh.com.csx.posapi.constant;

import java.util.*;

public class Constant {

    public static final String TEST = "023-886-555";

    public static final Integer YES          = 1;
    public static final Integer NO           = 0;

    private static final Integer ACTIVE      = 1;
    private static final Integer INACTIVE    = 0;

    private static final String NONE         = "";
    private static final String PENDING      = "pending";
    public  static final String SHORT        = "short";
    public  static final String FULL         = "full";
    private static final String PARTIAL      = "partial";
    private static final String COMPLETED    = "completed";
    private static final String RETURNED     = "returned";
    private static final String SENT         = "sent";
    private static final String RECEIVED     = "received";

    public static final String PRODUCT_NO_IMAGE = "no_image.png";
    public static final String USER_NO_IMAGE    = "default_user_avatar.png";

    public static class User {
        public static class Type {
            public static final String SYSTEM   = "0";
            public static final String VENDER   = "1";
            public static final String SALESMAN = "2";
            public static final String DEFAULT  = VENDER;
            public static final Set<String> VALID_STATUSES = Set.of(SYSTEM, VENDER);
            public static final String NOTE = "Accepted values are 0 or 1. (Note: 0=super_user, 1=vendor)";
        }

        public static class Status {
            public static final String ACTIVE   = "1";
            public static final String INACTIVE = "0";
            public static final String DEFAULT  = ACTIVE;
            public static final Set<String> VALID_STATUSES = Set.of(ACTIVE, INACTIVE);
            public static final String NOTE = "Accepted values are 0 or 1. (Note: 0=inactive, 1=active)";
        }

        public static class ViewRight {
            public static final Integer OWN     = NO;
            public static final Integer ALL     = YES;
            public static final Integer DEFAULT = OWN;
            public static final Set<Integer> VALID = Set.of(OWN, ALL);
            public static final String NOTE = "Accepted values are 0 or 1. (Note: 0=Own Records, 1=All Records)";
        }
    }

    public static class Gender {
        public static final String MALE   = "Male";
        public static final String FEMALE = "Female";
        public static final String NOTE = "Accepted values are 'm', 'male', 'f', or 'female'";
    }

    public static class ActiveStatus {
        public static final Integer ACTIVE   = Constant.ACTIVE;
        public static final Integer INACTIVE = Constant.INACTIVE;
        public static final Integer DEFAULT  = ACTIVE;
        public static final Set<Integer> VALID_STATUSES    = Set.of(ACTIVE, INACTIVE);
        public static final Map<String, Integer> VALID_KEY = Map.of(
                "active", ACTIVE,
                "inactive", INACTIVE
        );
        public static final String NOTE = "Accepted values are 0 or 1. (Note: 0=inactive, 1=active)";
    }

    public static class Overselling {
        public static final Integer YES      = Constant.YES;
        public static final Integer NO       = Constant.NO;
        public static final Integer DEFAULT  = NO;
        public static final Set<Integer> VALID = Set.of(YES, NO);
        public static final String NOTE = "Accepted values are 0 or 1. (Note: 0=No, 1=Yes)";
    }

    public static class DateTime {
        public static final String TIME_FORMAT     = "HH:mm:ss";
        public static final String DATE_FORMAT     = "yyyy-MM-dd";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    }

    public static class ProductType {
        public static final String STANDARD         = "standard";
        public static final String SERVICE          = "service";
        public static final String DEFAULT          = STANDARD;
        public static final Set<String> VALID_TYPES = Set.of(STANDARD, SERVICE);
        public static final String regexp           = "^(" + STANDARD + "|" + SERVICE + ")$";
        public static final String NOTE             = "Accepted values are '" + STANDARD + "' or '" + SERVICE + "'";
    }

    public static class TaxMethod {
        public static final Integer INCLUSIVE       = NO;
        public static final Integer EXCLUSIVE       = YES;
        public static final Integer DEFAULT         = EXCLUSIVE;
        public static final Set<Integer> VALID      = Set.of(INCLUSIVE, EXCLUSIVE);
        public static final Map<String, Integer> VALID_KEY = Map.of(
                "inclusive", INCLUSIVE,
                "exclusive", EXCLUSIVE
        );
        public static final String NOTE             = "Accepted values are 0 (inclusive) or 1 (exclusive).";
    }

    public static class TaxDeclarationType {
        public static final String PURCHASE   = "purchase";
        public static final String SALE       = "sale";
        public static final String EXPENSE    = "expense";
        public static final Set<String> VALID = Set.of(PURCHASE, SALE);
        public static final String REGEXP     = "^(" + PURCHASE + "|" + SALE + ")$";
        public static final String NOTE       = "Accepted values are '" + PURCHASE + "' or '" + SALE + "'";
    }

    public static class PurchaseStatus {
        public static final String PENDING    = Constant.PENDING;
        public static final String PARTIAL    = Constant.PARTIAL;
        public static final String COMPLETED  = Constant.COMPLETED;
        public static final String RETURNED   = Constant.RETURNED;
        public static final String DEFAULT    = PENDING;
        public static final Set<String> VALID = Set.of(PENDING, PARTIAL, COMPLETED);
        public static final String NOTE       = "Accepted values are '" + PENDING + "' or '" + PARTIAL + "' or '" + COMPLETED + "'";
    }

    public static class SaleStatus {
        public static final String PENDING    = Constant.PENDING;
        public static final String PARTIAL    = Constant.PARTIAL;
        public static final String COMPLETED  = Constant.COMPLETED;
        public static final String DEFAULT    = COMPLETED;
        public static final Set<String> VALID = Set.of(PENDING, PARTIAL, COMPLETED);
        public static final String NOTE       = "Accepted values are '" + PENDING + "' or '" + PARTIAL + "' or '" + COMPLETED + "'";
    }

    public static class SuspendStatus {
        public static final Integer SUSPENDED  = Constant.ACTIVE;
        public static final Integer COMPLETED  = Constant.INACTIVE;
    }

    public static class DeliveryStatus {
        public static final String NONE       = Constant.NONE;
        public static final String PENDING    = Constant.PENDING;
        public static final String PARTIAL    = Constant.PARTIAL;
        public static final String COMPLETED  = Constant.COMPLETED;
        public static final String DEFAULT    = NONE;
        public static final Set<String> VALID = Set.of(NONE, PENDING, PARTIAL, COMPLETED);
        public static final String NOTE       = "Accepted values are '" + PENDING + "' or '" + PARTIAL + "' or '" + COMPLETED + "'";
    }

    public static class StockCountStatus {
        public static final String PENDING    = Constant.PENDING;
        public static final String COMPLETED  = Constant.COMPLETED;
        public static final Set<String> VALID = Set.of(PENDING, COMPLETED);
        public static final String NOTE       = "Accepted values are '" + PENDING + "' or '" + COMPLETED + "'";
    }

    public static class StockCountType {
        public static final String FULL       = Constant.FULL;
        public static final String PARTIAL    = Constant.PARTIAL;
        public static final Set<String> VALID = Set.of(FULL, PARTIAL);
        public static final String NOTE       = "Accepted values are '" + FULL + "' or '" + PARTIAL + "'";
    }

    public static class PaymentMethodType {
        public static final String CASH       = "cash";
        public static final String CARD       = "card";
        public static final String BANK       = "bank";
        public static final String GIFT       = "gift";
        public static final String CHEQUE     = "cheque";
        public static final String DEPOSIT    = "deposit";
        public static final Set<String> VALID = Set.of(CASH, CARD, BANK, GIFT, CHEQUE, DEPOSIT);
        public static final String NOTE       = "Accepted values are '" + CASH + "' or '" + CARD + "' or '" + BANK + "' or '" + GIFT + "' or '" + CHEQUE + "' or '" + DEPOSIT + "'";
    }

    public static class PaymentStatus {
        public static final String PENDING    = Constant.PENDING;
        public static final String PARTIAL    = Constant.PARTIAL;
        public static final String COMPLETED  = Constant.COMPLETED;
        public static final String DEFAULT    = PENDING;
        public static final Set<String> VALID = Set.of(PENDING, PARTIAL, COMPLETED);
        public static final String NOTE       = "Accepted values are '" + PENDING + "' or '" + PARTIAL + "' or '" + COMPLETED + "'";
    }

    public static class PaymentTransactionStatus {
        public static final String SENT       = Constant.SENT;
        public static final String RECEIVED   = Constant.RECEIVED;
        public static final String RETURNED   = Constant.RETURNED;
        public static final Set<String> VALID = Set.of(SENT, RECEIVED, RETURNED);
        public static final String NOTE       = "Accepted values are '" + SENT + "' or '" + RECEIVED + "' or '" + RETURNED + "'";
    }

    public static class PaymentTransactionType {
        public static final String PURCHASE_ORDER  = "purchase_order";
        public static final String PURCHASE        = "purchase";
        public static final String PURCHASE_RETURN = "purchase_return";
        public static final String SALE_ORDER      = "sale_order";
        public static final String SALE            = "sale";
        public static final String SALE_RETURN     = "sale_return";
        public static final String EXPENSE         = "expense";
    }

    public static class StockTransactionType {
        public static final String PURCHASE        = "purchase";
        public static final String PURCHASE_RETURN = "purchase_return";
        public static final String SALE            = "sale";
        public static final String SALE_RETURN     = "sale_return";
        public static final String POS             = "pos";
        public static final String ADJUSTMENT      = "adjustment";
        public static final String TRANSFER        = "transfer";
    }

    public static class AdjustmentType {
        public static final String ADDITION    = "addition";
        public static final String SUBTRACTION = "subtraction";
        public static final Set<String> VALID  = Set.of(ADDITION, SUBTRACTION);
        public static final String NOTE        = "Accepted values are '" + ADDITION + "' or '" + SUBTRACTION + "'";
    }

    public static class TransferStatus {
        public static final String PENDING     = "pending";
        public static final String COMPLETED   = "completed";
        public static final String DEFAULT     = PENDING;
        public static final Set<String> VALID  = Set.of(PENDING, COMPLETED);
        public static final String NOTE        = "Accepted values are '" + PENDING + "' or '" + COMPLETED + "'";
    }

    public static class PosRegister {
        public static final String OPEN   = "open";
        public static final String CLOSE  = "close";
    }

    public static class PosType {
        public static final String POS    = "pos";
        public static final String TABLE  = "table";
        public static final String regexp = "^(" + POS + "|" + TABLE + ")$";
        public static final String NOTE   = "'" + POS + "' or '" + TABLE + "'";
    }

    public static enum Modules {
        USER("EMPLOYEES", "USERS", "PERMISSIONS", "ROLES", "REPORTS", "OTHER"),
        INVENTORY("PRODUCTS", "UNITS", "BRANDS", "CATEGORIES", "ADJUSTMENTS", "TRANSFERS", "STOCK_COUNTS", "REPORTS", "OTHER"),
        SALE("POS", "SALES_ORDER", "SALES", "RETURNS", "PROMOTIONS", "MEMBERSHIPS", "CUSTOMERS", "DELIVERIES", "DRIVERS", "PAYMENTS", "REPORTS", "OTHER"),
        PURCHASE("PURCHASES_ORDER", "PURCHASES", "SUPPLIERS", "PAYMENTS", "REPORTS", "OTHER"),
        EXPENSE("CATEGORIES", "EXPENSES", "PAYMENTS", "REPORTS", "OTHER"),
        GENERAL("SETTINGS");

        // MODULE-SUBMODULE-ACTION, Ex: INVENTORY-PRODUCTS-CREATE

        private final List<String> submodules;

        Modules(String... submodules) {
            this.submodules = Arrays.asList(submodules);
        }

        public List<String> getSubmodules() {
            return submodules;
        }
    }

    public static class OrderBy {
        public static final String ASC  = "ASC";
        public static final String DESC = "DESC";
    }

    public static class AccountingMethod {
        public static final Integer FIFO    = 1;
        public static final Integer LIFO    = 2;
        public static final Integer AVG     = 3;
        public static final Integer DEFAULT = AVG;
    }

    public static class Directory {
        public static final String EXPENSE    = "expense";
        public static final String INVENTORY  = "inventory";
        public static final String PAYMENT    = "payment";
        public static final String PEOPLE     = "people";
        public static final String PURCHASE   = "purchase";
        public static final String SALE       = "sale";
        public static final String SETTING    = "setting";
        public static final Set<String> VALID = Set.of(EXPENSE, INVENTORY, PAYMENT, PEOPLE, PURCHASE, SALE, SETTING);
    }

    public static class FileExtension {
        public static final String CSV   = "csv";
        public static final String EXCEL = "xlsx";
        public static final Set<String> DOC = Set.of("pdf", "doc", "docx", "xls", "xlsx", "txt");
        public static final Set<String> IMG = Set.of("jpg", "jpeg", "png", "gif", "bmp");
    }

    public static class ReferenceReset {
        public static final Integer NONE  = 0;
        public static final Integer YEAR  = 1;
        public static final Integer MONTH = 2;
    }

    public static class ReferenceFormat {
        public static final String SEQUENCE                   = "{SEQUENCE}";
        public static final String PREFIX_SEQUENCE            = "{PREFIX}{SEQUENCE}";
        public static final String PREFIX_YEAR_SEQUENCE       = "{PREFIX}{YEAR}{SEQUENCE}";
        public static final String PREFIX_YEAR_MONTH_SEQUENCE = "{PREFIX}{YEAR}{MONTH}{SEQUENCE}";
        public static final String regexp = "^("
                + "\\{SEQUENCE\\}" + "|"
                + "\\{PREFIX\\}\\{SEQUENCE\\}" + "|"
                + "\\{PREFIX\\}\\{YEAR\\}\\{SEQUENCE\\}" + "|"
                + "\\{PREFIX\\}\\{YEAR\\}\\{MONTH\\}\\{SEQUENCE\\}"
                + ")$";
        public static final String NOTE = "formats: '" + SEQUENCE + "' or '" + PREFIX_SEQUENCE + "' or '" + PREFIX_YEAR_SEQUENCE + "' or '" + PREFIX_YEAR_MONTH_SEQUENCE + "'";
    }

    public static class ReferenceKey {
        public static final String PO    = "po";
        public static final String P     = "p";
        public static final String PR    = "pr";
        public static final String SO    = "so";
        public static final String S     = "s";
        public static final String SR    = "sr";
        public static final String POS   = "pos";
        public static final String BILL  = "bill";
        public static final String SC    = "sc";
        public static final String AJ    = "aj";
        public static final String TR    = "tr";
        public static final String EX    = "ex";
        public static final String PAY   = "pay";
        public static final Set<String> VALID = Set.of(PO, P, PR, SO, S, SR, POS, BILL, SC, AJ, TR, EX, PAY);
    }

    public static class Language {
        public static final String KH = "kh";
        public static final String EN = "en";
        public static final String CN = "cn";
        public static final String TH = "th";
        public static final String VN = "vn";

        public static final Map<String, String> KEY = Map.of(
                KH, "khmer",
                EN, "english",
                CN, "chinese",
                TH, "thai",
                VN, "vietnamese"
        );

        public static final Set<String> VALID = Set.of(KH, EN, CN, TH, VN);
        public static final String regexp = "^(" + KH + "|" + EN + "|" + CN + "|" + TH + "|" + VN + ")$";
        public static final String NOTE = "Supported languages '" + KH + "', '" + EN + "', '" + CN + "', '" + TH + "', '" + VN + "'";
    }

    public static class Features {
        public static final String TAX_DECLARE = "moduleTaxDeclare";
    }
}
