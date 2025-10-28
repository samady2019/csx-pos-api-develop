package kh.com.csx.posapi.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Tuple;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.constant.Constant.*;
import kh.com.csx.posapi.dto.ID;
import kh.com.csx.posapi.dto.currency.CurrencyPayment;
import kh.com.csx.posapi.dto.setting.FileInfoResponse;
import kh.com.csx.posapi.dto.stockCount.StockCountItem;
import kh.com.csx.posapi.dto.stockMovement.StockCost;
import kh.com.csx.posapi.dto.stockMovement.StockExpiry;
import kh.com.csx.posapi.dto.stockMovement.StockMovement;
import kh.com.csx.posapi.dto.stockMovement.StockResponse;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.repository.UnitRepository;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.config.SftpConfig;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.apache.poi.ss.util.CellReference;
import org.springframework.web.multipart.MultipartFile;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import net.jpountz.lz4.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

@Component
@RequiredArgsConstructor
public class Utility {
    @Value("${storage.system.documents}")
    public String sysDocPath;

    @Value("${storage.system.images}")
    public String sysImgPath;

    @Value("${storage.upload.documents}")
    public String uploadDocPath;

    @Value("${storage.upload.images}")
    public String uploadImgPath;

    @Autowired
    private ServletContext servletContext;

    private final SettingRepository settingRepository;
    private final PosSettingRepository posSettingRepository;
    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final CurrencyRepository currencyRepository;
    private final TaxRateRepository taxRateRepository;
    private final UnitRepository unitRepository;
    private final ProductUnitRepository productUnitRepository;
    private final BillerRepository billerRepository;
    private final OrderRefRepository orderRefRepository;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;
    private final SuspendedBillRepository suspendedBillRepository;
    private final StockCountRepository stockCountRepository;
    private final TransferRepository transferRepository;
    private final AdjustmentRepository adjustmentRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    private static final String[] DATE_TIME_FORMATS = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS"
    };

    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy"
    };

    private static final String[] TIME_FORMATS = {
            "HH:mm:ss",
            "HH:mm"
    };

    private String baseUrl(HttpServletRequest request) {
        String protocol   = request.isSecure() ? "https" : "http";
        String serverName = request.getServerName();
        int port          = request.getServerPort();
        return protocol + "://" + serverName + ":" + port;
    }

    public Pageable initPagination(Integer page, Integer size, String sortBy, String orderBy) {
        SettingEntity setting = getSettings();
        page   = (page != null ? page : 1) -1;
        size   = size != null ? size : setting.getRowsPerPage();
        sortBy = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy : "id";
        Sort.Direction sortDirection = (orderBy != null && orderBy.equalsIgnoreCase("ASC")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String[] sortFields = sortBy.split(",");
        Sort sort = Sort.unsorted();
        for (String field : sortFields) {
            sort = sort.and(Sort.by(sortDirection, field.trim()));
        }
        return PageRequest.of(page, size, sort);
    }

    public SettingEntity getSettings() {
        return settingRepository.findById(1L).orElseThrow(() -> new ApiException("Settings not found.", HttpStatus.BAD_REQUEST));
    }

    public PosSettingEntity getPosSettings() {
        return posSettingRepository.findById(1L).orElseThrow(() -> new ApiException("Pos settings not found.", HttpStatus.BAD_REQUEST));
    }

    public CurrencyEntity getDefaultCurrency() {
        SettingEntity settings = getSettings();
        return currencyRepository.findFirstByCode(settings.getDefaultCurrency()).orElseThrow(() -> new ApiException("Default currency not found.", HttpStatus.BAD_REQUEST));
    }

    public boolean isValidLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public double calculateDiscount(double amount, String discount) {
        if (discount == null || discount.trim().isEmpty()) return 0;

        double discount_amount = 0;
        discount = discount.trim();
        if (discount.endsWith("%")) {
            double percentage = Double.parseDouble(discount.replace("%", "").trim());
            if (percentage < 0 || percentage > 100) {
                throw new ApiException("Percentage discount should be between 0 and 100.", HttpStatus.BAD_REQUEST);
            }
            discount_amount = amount * (percentage / 100);
        } else {
            double fixedDiscount = Double.parseDouble(discount.trim());
            if (fixedDiscount < 0) {
                throw new ApiException("Fixed discount should not be negative.", HttpStatus.BAD_REQUEST);
            } else if (fixedDiscount > amount) {
                throw new ApiException("Fixed discount should not exceed the amount.", HttpStatus.BAD_REQUEST);
            }
            discount_amount = fixedDiscount;
        }
        return discount_amount;
    }

    public double calculateTax(double amount, Long taxRateId) {
        return calculateTax(amount, taxRateId, Constant.TaxMethod.DEFAULT);
    }

    public double calculateTax(double amount, Long taxRateId, Integer taxMethod) {
        TaxRateEntity taxRate = taxRateRepository.findById(taxRateId).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        double tax_amount = 0;
        if (taxMethod.equals(Constant.TaxMethod.INCLUSIVE)) {
            tax_amount = amount * (taxRate.getRate() / (100 + taxRate.getRate()));
        } else {
            tax_amount = amount * (taxRate.getRate() / 100);
        }
        return formatDecimal(tax_amount);
    }

    public double calculateTotal(double total, double discount, double tax, double shipping) {
        return calculateTotal(total, discount, tax, shipping, 0.0);
    }

    public double calculateTotal(double total, double discount, double tax, double shipping, double surcharge) {
        return formatDecimal(total - discount + tax + shipping - surcharge);
    }

    public double calculateNetAmount(double amount, double taxAmount, Integer taxMethod) {
        double net_amount = 0;
        if (taxMethod.equals(Constant.TaxMethod.INCLUSIVE)) {
            net_amount = amount - taxAmount;
        } else {
            net_amount = amount;
        }
        return formatDecimal(net_amount);
    }

    public double calculateTotalBaseCurrency(String currenciesJson) {
        if (currenciesJson == null || currenciesJson.isEmpty()) {
            throw new ApiException("Currencies JSON cannot be null or empty.", HttpStatus.BAD_REQUEST);
        }
        List<CurrencyPayment> currencies;
        try {
            currencies = new ObjectMapper().readValue(currenciesJson, new TypeReference<List<CurrencyPayment>>() {});
        } catch (JsonProcessingException e) {
            throw new ApiException("Failed to parse currencies JSON. Please ensure it is in the correct format.", HttpStatus.BAD_REQUEST);
        }
        List<CurrencyPayment> payments = new ArrayList<>();
        for (CurrencyPayment currency : currencies) {
            if (currency.getCode() == null || currency.getCode().isEmpty()) {
                throw new ApiException("Currency code is required.", HttpStatus.BAD_REQUEST);
            }
            if (currency.getRate() == null) {
                throw new ApiException("Currency rate is required.", HttpStatus.BAD_REQUEST);
            }
            if (currency.getAmount() == null) {
                throw new ApiException("Currency amount is required.", HttpStatus.BAD_REQUEST);
            }
            currencyRepository.findFirstByCode(currency.getCode()).orElseThrow(() -> new ApiException("Currency code '" + currency.getCode() + "' not found.", HttpStatus.BAD_REQUEST));
            payments.add(currency);
        }

        return payments.stream().mapToDouble(this::convertToBaseCurrency).sum();
    }

    public double convertToBaseCurrency(CurrencyPayment currency) {
        SettingEntity settings = getSettings();
        double amountInBaseCurrency = currency.getAmount();
        if (!currency.getCode().equalsIgnoreCase(settings.getDefaultCurrency())) {
            amountInBaseCurrency /= (currency.getRate() != null && currency.getRate() != 0) ? currency.getRate() : 1;
        }
        return formatDecimal(amountInBaseCurrency);
    }

    public double convertToBaseUnitQuantity(Long unitId, double value) {
        if (value == 0) return 0;
        UnitEntity unit = unitRepository.findByUnitId(unitId).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        double convertedValue = value;
        if (unit.getPunitId() != null) {
            UnitEntity baseUnit = unitRepository.findByUnitId(unit.getPunitId()).orElseThrow(() -> new ApiException("Base unit not found.", HttpStatus.BAD_REQUEST));
            convertedValue = value * unit.getValue();
        }
        return formatQuantity(convertedValue);
    }

    public double convertFromBaseUnitQuantity(Long unitId, double value) {
        if (value == 0) return 0;
        UnitEntity unit = unitRepository.findByUnitId(unitId).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        double convertedValue = value;
        if (unit.getPunitId() != null) {
            UnitEntity baseUnit = unitRepository.findByUnitId(unit.getPunitId()).orElseThrow(() -> new ApiException("Base unit not found.", HttpStatus.BAD_REQUEST));
            convertedValue = value / (unit.getValue() != 0 ? unit.getValue() : 1);
        }
        return formatQuantity(convertedValue);
    }

    public double convertToBaseUnitPrice(Long unitId, double value) {
        if (value == 0) return 0;
        UnitEntity unit = unitRepository.findByUnitId(unitId).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        double convertedValue = value;
        if (unit.getPunitId() != null) {
            UnitEntity baseUnit = unitRepository.findByUnitId(unit.getPunitId()).orElseThrow(() -> new ApiException("Base unit not found.", HttpStatus.BAD_REQUEST));
            convertedValue = value / (unit.getValue() != 0 ? unit.getValue() : 1);
        }
        return formatDecimal(convertedValue);
    }

    public double convertFromBaseUnitPrice(Long unitId, double value) {
        if (value == 0) return 0;
        UnitEntity unit = unitRepository.findByUnitId(unitId).orElseThrow(() -> new ApiException("Unit not found.", HttpStatus.BAD_REQUEST));
        double convertedValue = value;
        if (unit.getPunitId() != null) {
            UnitEntity baseUnit = unitRepository.findByUnitId(unit.getPunitId()).orElseThrow(() -> new ApiException("Base unit not found.", HttpStatus.BAD_REQUEST));
            convertedValue = value * unit.getValue();
        }
        return formatDecimal(convertedValue);
    }

    public LocalDateTime convertToLocalDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(Constant.DateTime.DATETIME_FORMAT));
    }

    public LocalDate convertToLocalDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(Constant.DateTime.DATE_FORMAT));
    }

    public String convertDateTimeFormat(String dateTimeString) {
        return convertDateTimeFormat(dateTimeString, Constant.DateTime.DATETIME_FORMAT);
    }

    public String convertDateTimeFormat(String dateTimeString, String toformat) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            throw new IllegalArgumentException("DateTime string cannot be null or empty.");
        }
        LocalDateTime dateTime = null;
        toformat = (toformat != null && !toformat.trim().isEmpty()) ? toformat : Constant.DateTime.DATETIME_FORMAT;
        for (String format : DATE_TIME_FORMATS) {
            try {
                dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(format));
                break;
            } catch (DateTimeParseException e) {
                // Continue trying the next format
            }
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("Invalid dateTime format: " + dateTimeString);
        }
        return dateTime.format(DateTimeFormatter.ofPattern(toformat));
    }

    public String convertDateFormat(String dateString) {
        return convertDateFormat(dateString, Constant.DateTime.DATE_FORMAT);
    }

    public String convertDateFormat(String dateString, String toformat) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty.");
        }
        LocalDate date = null;
        toformat = (toformat != null && !toformat.trim().isEmpty()) ? toformat : Constant.DateTime.DATE_FORMAT;
        for (String format : DATE_FORMATS) {
            try {
                date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));
                break;
            } catch (DateTimeParseException e) {
                // Continue trying the next format
            }
        }
        if (date == null) {
            throw new IllegalArgumentException("Invalid date format: " + dateString);
        }
        return date.format(DateTimeFormatter.ofPattern(toformat));
    }

    public Double formatDecimal(Number number) {
        SettingEntity settings = getSettings();
        return formatDecimal(number, settings.getDecimals());
    }

    public Double formatDecimal(Number number, Integer decimals) {
        SettingEntity settings = getSettings();
        if (number == null) {
            return null;
        }
        if (decimals == null) {
            decimals = settings.getDecimals();
        }
        BigDecimal bd = new BigDecimal(number.toString());
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String formatMoney(Number number, String currencyCode) {
        return formatMoney(number, currencyCode, getSettings().getDecimals());
    }

    public String formatMoney(Number number, String currencyCode, int decimalPlaces) {
        if (number == null) return "";
        BigDecimal bd = new BigDecimal(number.toString());
        Locale khmerLocale = new Locale("km", "KH");
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalPlaces > 0) {
            pattern.append(".");
            for (int i = 0; i < decimalPlaces; i++) {
                pattern.append("0");
            }
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat(pattern.toString(), symbols);
        String symbol;
        if ("KHR".equalsIgnoreCase(currencyCode)) {
            symbol = Currency.getInstance(currencyCode).getSymbol(khmerLocale);
            return formatter.format(bd) + " " + symbol;
        } else {
            if ("USD".equalsIgnoreCase(currencyCode)) {
                symbol = "$";
            } else {
                Locale locale = getLocaleFromCurrency(currencyCode);
                symbol = Currency.getInstance(currencyCode).getSymbol(locale);
            }
            return symbol + " " + formatter.format(bd);
        }
    }

    public Number noZeroDecimal(Number number) {
        if (number == null) return null;
        BigDecimal bd = new BigDecimal(number.toString());
        BigDecimal stripped = bd.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return stripped.longValue();
        } else {
            return stripped.doubleValue();
        }
    }

    private Locale getLocaleFromCurrency(String currencyCode) {
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                if (Currency.getInstance(locale).getCurrencyCode().equals(currencyCode)) {
                    return locale;
                }
            } catch (Exception ignored) {
                // Some locales may not have a currency
            }
        }
        return Locale.US;
    }

    public Double formatQuantity(Number number) {
        SettingEntity settings = getSettings();
        return formatQuantity(number, settings.getQuantityDecimals());
    }

    public Double formatQuantity(Number number, Integer decimals) {
        SettingEntity settings = getSettings();
        if (number == null) {
            return null;
        }
        if (decimals == null) {
            decimals = settings.getQuantityDecimals();
        }
        BigDecimal bd = new BigDecimal(number.toString());
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public UnitEntity getProductBaseUnit(Long product_id) {
        return unitRepository.findProductBaseUnit(product_id);
    }

    public boolean validProductUnit(Long productId, Long unitId) {
        return getProductUnit(productId, unitId) != null;
    }

    public List<ProductUnitEntity> getProductUnits(Long product_id) {
        return productUnitRepository.findProductUnits(product_id);
    }

    public ProductUnitEntity getProductUnit(Long product_id, Long unit_id) {
        return productUnitRepository.findProductUnit(product_id, unit_id);
    }

    public List<StockCost> getFifoCost(Long product_id, double quantity, String transaction, Long transaction_id) {
        List<StockMovementEntity> stkTnx = stockMovementRepository.findStkTnx(product_id, transaction, transaction_id, OrderBy.ASC);
        Map<String, StockCost> stockOut  = new HashMap<>();
        List<StockCost>        stockIns  = new ArrayList<>();
        List<StockCost>        stockList = new ArrayList<>();
        List<StockCost>        itemCost  = new ArrayList<>();
        for (StockMovementEntity row : stkTnx) {
            String costIdx = row.getCost().toString();
            if (row.getQuantity() < 0) {
                stockOut.merge(costIdx, new StockCost(row.getCost(), row.getQuantity(), 0.0), (existing, newStock) -> {
                    existing.setQuantity(existing.getQuantity() + newStock.getQuantity());
                    return existing;
                });
            } else {
                stockIns.add(new StockCost(row.getCost(), row.getQuantity(), 0.0));
            }
        }
        for (StockCost stockIn : stockIns) {
            String costIdx = stockIn.getCost().toString();
            if (stockOut.containsKey(costIdx) && Math.abs(stockOut.get(costIdx).getQuantity()) > 0) {
                stockIn.setOutQuantity(-1 * stockIn.getQuantity());
                stockOut.get(costIdx).setQuantity(stockIn.getQuantity() + stockOut.get(costIdx).getQuantity());
            } else {
                stockIn.setOutQuantity(stockOut.containsKey(costIdx) ? stockOut.get(costIdx).getQuantity() : 0.0);
                stockOut.get(costIdx).setQuantity(0.0);
            }
            if (stockIn.getQuantity() > Math.abs(stockIn.getOutQuantity())) {
                stockList.add(stockIn);
            }
        }
        for (StockCost stock : stockList) {
            double outQuantity = Math.abs(stock.getOutQuantity()) + quantity;
            if (stock.getQuantity() > outQuantity) {
                if (quantity > 0) {
                    itemCost.add(new StockCost(stock.getCost(), quantity, 0.0));
                }
                break;
            } else {
                double balanceQuantity = stock.getQuantity() + stock.getOutQuantity();
                itemCost.add(new StockCost(stock.getCost(), balanceQuantity, 0.0));
                quantity = quantity - balanceQuantity;
            }
        }
        return itemCost;
    }

    public List<StockCost> getLifoCost(Long product_id, double quantity, String transaction, Long transaction_id) {
        List<StockMovementEntity> stkTnx = stockMovementRepository.findStkTnx(product_id, transaction, transaction_id, OrderBy.DESC);
        Map<String, StockCost> stockOut  = new HashMap<>();
        List<StockCost>        stockIns  = new ArrayList<>();
        List<StockCost>        stockList = new ArrayList<>();
        List<StockCost>        itemCost  = new ArrayList<>();
        for (StockMovementEntity row : stkTnx) {
            String costIdx = row.getCost().toString();
            if (row.getQuantity() < 0) {
                stockOut.merge(costIdx, new StockCost(row.getCost(), row.getQuantity(), 0.0), (existing, newStock) -> {
                    existing.setQuantity(existing.getQuantity() + newStock.getQuantity());
                    return existing;
                });
            } else {
                stockIns.add(new StockCost(row.getCost(), row.getQuantity(), 0.0));
            }
        }
        for (StockCost stockIn : stockIns) {
            String costIdx = stockIn.getCost().toString();
            if (stockOut.containsKey(costIdx) && Math.abs(stockOut.get(costIdx).getQuantity()) > 0) {
                stockIn.setOutQuantity(-1 * stockIn.getQuantity());
                stockOut.get(costIdx).setQuantity(stockIn.getQuantity() + stockOut.get(costIdx).getQuantity());
            } else {
                stockIn.setOutQuantity(stockOut.containsKey(costIdx) ? stockOut.get(costIdx).getQuantity() : 0.0);
                stockOut.get(costIdx).setQuantity(0.0);
            }
            if (stockIn.getQuantity() > Math.abs(stockIn.getOutQuantity())) {
                stockList.add(stockIn);
            }
        }
        for (StockCost stock : stockList) {
            double outQuantity = Math.abs(stock.getOutQuantity()) + quantity;
            if (stock.getQuantity() > outQuantity) {
                if (quantity > 0) {
                    itemCost.add(new StockCost(stock.getCost(), quantity, 0.0));
                }
                break;
            } else {
                double balanceQuantity = stock.getQuantity() + stock.getOutQuantity();
                itemCost.add(new StockCost(stock.getCost(), balanceQuantity, 0.0));
                quantity = quantity - balanceQuantity;
            }
        }
        return itemCost;
    }

    public double getAvgCost(Long product_id) {
        Double avgCost = null;
        ProductEntity product = getProductDetails(product_id);
        if (product.getType().trim().equals(Constant.ProductType.SERVICE)) {
            UnitEntity        base_unit   = getProductBaseUnit(product_id);
            ProductUnitEntity productUnit = getProductUnit(product_id, base_unit.getUnitId());
            avgCost = productUnit.getCost();
        } else {
            avgCost = stockMovementRepository.findProdAvgCost(product_id);
        }
        return formatDecimal(avgCost != null ? avgCost : 0.0);
    }

    public List<StockMovement> checkPOSExpiry(List<StockMovementEntity> stocksEntity) {
        List<StockMovement> expiry_stockmoves = new ArrayList<>();
        List<StockMovement> stocks = stocksEntity.stream().map(StockMovement::new).toList();
        for (StockMovement stock : stocks) {
            Long product_id   = stock.getProductId();
            Long warehouse_id = stock.getWarehouseId();
            UnitEntity unit   = getProductBaseUnit(product_id);
            List<StockMovement> product_expiries = getStockGrpExpByPID(warehouse_id, product_id, null, null, null).stream().map(StockMovement::new).toList();
            if (!product_expiries.isEmpty()) {
                boolean con            = true;
                double expiry_quantity = 0;
                for (StockMovement product_expirie : product_expiries) {
                    if (expiry_quantity == 0) {
                        expiry_quantity = Math.abs(stock.getQuantity());
                    }
                    LocalDate expiry_date = product_expirie.getExpiry();
                    for (StockMovement expiry_stockmove : expiry_stockmoves) {
                        boolean expiryDatesMatch = (expiry_stockmove.getExpiry() == null && expiry_date == null) || (expiry_stockmove.getExpiry() != null && expiry_stockmove.getExpiry().equals(expiry_date));
                        if (expiry_stockmove.getProductId().equals(stock.getProductId()) && expiryDatesMatch) {
                            product_expirie.setQuantity(product_expirie.getQuantity() - Math.abs(expiry_stockmove.getQuantity()));
                        }
                    }
                    if (con && product_expirie.getQuantity() > 0) {
                        StockMovement newStock = new StockMovement(stock);
                        if (product_expirie.getQuantity() >= expiry_quantity) {
                            newStock.setUnitId(unit.getUnitId());
                            newStock.setUnitQuantity(-1 * expiry_quantity);
                            newStock.setQuantity(-1 * expiry_quantity);
                            newStock.setExpiry(expiry_date);
                            con = false;
                        } else {
                            expiry_quantity = expiry_quantity - product_expirie.getQuantity();
                            newStock.setUnitId(unit.getUnitId());
                            newStock.setUnitQuantity(-1 * product_expirie.getQuantity());
                            newStock.setQuantity(-1 * product_expirie.getQuantity());
                            newStock.setExpiry(expiry_date);
                        }
                        expiry_stockmoves.add(newStock);
                    }
                }
                if (con && expiry_quantity > 0) {
                    StockMovement newStock = new StockMovement(stock);
                    newStock.setUnitId(unit.getUnitId());
                    newStock.setUnitQuantity(-1 * expiry_quantity);
                    newStock.setQuantity(-1 * expiry_quantity);
                    newStock.setExpiry(null);
                    expiry_stockmoves.add(newStock);
                }
            } else {
                expiry_stockmoves.add(stock);
            }
        }
        return expiry_stockmoves;
    }

    public ProductEntity getProductDetails(Long product_id) {
        return productRepository.findByProductId(product_id).orElseThrow();
    }

    public StockMovementEntity getStockGrpExpByPIDExp(Long warehouse_id, Long product_id, LocalDate expiry, String transaction, Long transaction_id) {
        return stockMovementRepository.findProductExpiryByProductId(warehouse_id, product_id, expiry, transaction, transaction_id);
    }

    public List<StockMovementEntity> getStockGrpExpByPID(Long warehouse_id, Long product_id, LocalDate expiry, String transaction, Long transaction_id) {
        return stockMovementRepository.findProductExpiriesByProductId(warehouse_id, product_id, expiry, transaction, transaction_id);
    }

    public List<StockMovementEntity> getStockGrpExp(Long warehouse_id, Long product_id, String brands, String categories, LocalDate expiry, String transaction, Long transaction_id) {
        String[] brandArr    = brands != null ? brands.split(",") : null;
        String[] categoryArr = categories != null ? categories.split(",") : null;
        return stockMovementRepository.findProductsExpiries(warehouse_id, product_id, brandArr, categoryArr, expiry, transaction, transaction_id);
    }

    public List<StockCountItem> getStockCountGrpExp(Long warehouse_id, Long product_id, String brands, String categories, LocalDate expiry) {
        String[] brandArr    = brands != null ? brands.split(",") : null;
        String[] categoryArr = categories != null ? categories.split(",") : null;
        return stockMovementRepository.findProductsExpiriesCount(warehouse_id, product_id, brandArr, categoryArr, expiry);
    }

    public void checkOverstock(Long warehouse_id, List<StockMovementEntity> products) {
        SettingEntity settings = getSettings();
        WarehouseEntity warehouse = warehouseRepository.findById(warehouse_id).orElseThrow();
        if (settings.getOverselling().equals(Constant.NO) || (settings.getOverselling().equals(Constant.YES) && warehouse.getOverselling().equals(Constant.NO))) {
            for (StockMovementEntity product : products) {
                StockMovementEntity stock = getStockGrpExpByPIDExp(product.getWarehouseId(), product.getProductId(), product.getExpiry(), null, null);
                if (stock == null || stock.getQuantity() < 0) {
                    ProductEntity product_details = getProductDetails(product.getProductId());
                    throw new ApiException("The product name: '" + product_details.getProductNameEn() + "' (" + product_details.getProductCode() + ") is out of stock.", HttpStatus.BAD_REQUEST);
                }
            }
        }
    }

    private String capitalize(String field) {
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public String generateRandomFilename(String prefix, String extension) {
        return generateRandomFilename(prefix, extension, null);
    }

    public String generateRandomFilename(String prefix, String extension, String metadata) {
        String hash       = "";
        String randomUUID = UUID.randomUUID().toString();
        if (metadata != null && !metadata.trim().isEmpty()) {
            // byte[] compressedData = compressLZ4(metadata);
            // hash = base64Encode(compressedData);
            // hash = hashMetadata(metadata);
            // hash = bytesToHex(compressedData);
            hash = encrypt(metadata);
        }
        return prefix + randomUUID + (!hash.isEmpty() ? ("-" + hash) : "") + "." + extension;
    }

    public String generateRandomNumber(Integer length) {
        SecureRandom random = new SecureRandom();
        long max = (long) Math.pow(10, length) - 1;
        long min = (long) Math.pow(10, length - 1);
        long randomNumber = min + (long) (random.nextDouble() * (max - min));
        return String.format("%0" + length + "d", randomNumber);
    }

    public OrderRefEntity initOrderRef(Long billerId) {
        SettingEntity setting = getSettings();
        LocalDate date = !setting.getReferenceReset().equals(ReferenceReset.NONE) ? LocalDate.now() : null;
        OrderRefEntity orderRef = new OrderRefEntity();
        orderRef.setBillerId(billerId);
        orderRef.setDate(date);
        try {
            return orderRefRepository.save(orderRef);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public OrderRefEntity getOrderRef(Long billerId) {
        SettingEntity setting  = getSettings();
        LocalDate     date     = LocalDate.now();
        Integer       reset    = setting.getReferenceReset();
        String        filter   = null;
        if (reset.equals(ReferenceReset.YEAR)) {
            filter = date.format(DateTimeFormatter.ofPattern("yyyy"));
        } else if (reset.equals(ReferenceReset.MONTH)) {
            filter = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        return orderRefRepository.findByBillerIdAndDate(billerId, filter).orElseGet(() -> initOrderRef(billerId));
    }

    public String getReferenceNo(Long billerId, String field) {
        if (billerId == null) {
            throw new ApiException("biller ID is required.", HttpStatus.BAD_REQUEST);
        }
        if (field == null || field.trim().isEmpty()) {
            throw new ApiException("Field is required.", HttpStatus.BAD_REQUEST);
        }
        BillerEntity biller = billerRepository.findById(billerId).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        long totalBillers   = billerRepository.count();

        SettingEntity setting   = getSettings();
        LocalDate     date      = LocalDate.now();
        String        format    = setting.getReferenceFormat().trim();
        String        prefix    = "";
        String        bill      = (setting.getMultiBiller().equals(Constant.YES) || totalBillers > 1) ? biller.getCode() : "";
        String        year      = date.format(DateTimeFormatter.ofPattern("yy"));
        String        month     = date.format(DateTimeFormatter.ofPattern("MM"));
        String        sequence  = "";
        String        ref       = "";
        OrderRefEntity orderRef = getOrderRef(billerId);
        try {
            Field f = OrderRefEntity.class.getDeclaredField(field);
            f.setAccessible(true);
            Object v = f.get(orderRef);
            sequence = String.format("%06d", ((Number) v).longValue());
        } catch (Exception e) {
            throw new ApiException("Error accessing field: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        switch (field) {
            case ReferenceKey.PO   -> prefix = setting.getPurchaseOrderPrefix();
            case ReferenceKey.PR   -> prefix = setting.getPurchaseReturnPrefix();
            case ReferenceKey.P    -> prefix = setting.getPurchasePrefix();
            case ReferenceKey.SO   -> prefix = setting.getSaleOrderPrefix();
            case ReferenceKey.SR   -> prefix = setting.getSaleReturnPrefix();
            case ReferenceKey.S    -> prefix = setting.getSalePrefix();
            case ReferenceKey.POS  -> prefix = setting.getPosPrefix();
            case ReferenceKey.BILL -> prefix = setting.getBillPrefix();
            case ReferenceKey.SC   -> prefix = setting.getStockCountPrefix();
            case ReferenceKey.AJ   -> prefix = setting.getAdjustmentPrefix();
            case ReferenceKey.TR   -> prefix = setting.getTransferPrefix();
            case ReferenceKey.EX   -> prefix = setting.getExpensePrefix();
            case ReferenceKey.PAY  -> prefix = setting.getPaymentPrefix();
            default                -> prefix = "";
        }
        switch (format) {
            case ReferenceFormat.SEQUENCE                   -> ref = bill + sequence;
            case ReferenceFormat.PREFIX_SEQUENCE            -> ref = prefix + bill + sequence;
            case ReferenceFormat.PREFIX_YEAR_SEQUENCE       -> ref = prefix + bill + year + sequence;
            case ReferenceFormat.PREFIX_YEAR_MONTH_SEQUENCE -> ref = prefix + bill + year + month + sequence;
            default                                         -> ref = generateRandomNumber(12);
        }
        return ref;
    }

    public void updateReferenceNo(Long billerId, String field, String referenceNo) {
        if (billerId == null) {
            throw new ApiException("biller ID is required.", HttpStatus.BAD_REQUEST);
        }
        if (field == null || field.trim().isEmpty()) {
            throw new ApiException("Field is required.", HttpStatus.BAD_REQUEST);
        }
        if (referenceNo != null && referenceNo.equals(getReferenceNo(billerId, field))) {
            OrderRefEntity orderRef = getOrderRef(billerId);
            try {
                String capitalizedField = capitalize(field);
                Method getter = OrderRefEntity.class.getMethod("get" + capitalizedField);
                Method setter = OrderRefEntity.class.getMethod("set" + capitalizedField, getter.getReturnType());
                Integer currentValue = (Integer) getter.invoke(orderRef);
                setter.invoke(orderRef, currentValue + 1);
            } catch (Exception e) {
                throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
    }

    public String checkReferenceNo(Long billerId, String field, String referenceNo) {
        boolean isExists = false;
        switch (field) {
            case ReferenceKey.PO   -> isExists = purchaseOrderRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.PR   -> isExists = purchaseRepository.existsByReturnReferenceNo(referenceNo);
            case ReferenceKey.P    -> isExists = purchaseRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.SO   -> {}
            case ReferenceKey.SR   -> {}
            case ReferenceKey.S    -> isExists = saleRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.POS  -> isExists = saleRepository.existsByReferenceNoPOS(referenceNo);
            case ReferenceKey.BILL -> isExists = suspendedBillRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.SC   -> isExists = stockCountRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.AJ   -> isExists = adjustmentRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.TR   -> isExists = transferRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.EX   -> isExists = expenseRepository.existsByReferenceNo(referenceNo);
            case ReferenceKey.PAY  -> isExists = paymentRepository.existsByReferenceNo(referenceNo);
        }
        return (isExists ? getReferenceNo(billerId, field) : referenceNo);
    }

    public StockResponse getStockDetailsByPID(Long product_id) {
        return getStockDetailsByPID(product_id, null);
    }

    public StockResponse getStockDetailsByPID(Long product_id, Long warehouse_id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        String user_whs = (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) ? user.getWarehouses().trim() : null;
        if (product_id == null) {
            throw new ApiException("Product ID is required.", HttpStatus.BAD_REQUEST);
        }
        ProductEntity product = productRepository.findByProductId(product_id).orElseThrow(() -> new ApiException("Product not found.", HttpStatus.BAD_REQUEST));
        if (!product.getType().equals(ProductType.STANDARD)) {
            return null;
        }
        Double quantity = stockMovementRepository.findStockBalance(product_id, user_whs, warehouse_id);
        List<WarehouseProductEntity> warehousesProduct = warehouseProductRepository.findWarehousesProduct(product_id, user_whs, warehouse_id);
        List<StockExpiry> expiriesProduct = new ArrayList<>();
        List<StockMovementEntity> stockMovements = stockMovementRepository.findStockExpiries(product_id, user_whs, warehouse_id);
        for (StockMovementEntity stockMovement : stockMovements) {
            StockExpiry stockExpiry = new StockExpiry();
            stockExpiry.setProductId(stockMovement.getProductId());
            stockExpiry.setExpiry(stockMovement.getExpiry());
            stockExpiry.setQuantity(stockMovement.getQuantity());
            expiriesProduct.add(stockExpiry);
        }
        StockResponse stock = new StockResponse();
        stock.setQuantity(quantity);
        stock.setWarehousesProduct(warehousesProduct);
        stock.setExpiriesProduct(expiriesProduct);
        return stock;
    }

    public void verifyAccess(Object request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                try {
                    Method getBillerIdMethod = request.getClass().getMethod("getBillerId");
                    Long bilerId = (Long) getBillerIdMethod.invoke(request);
                    List<Long> accessibleBillers = Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList();
                    if (bilerId != null && !accessibleBillers.contains(bilerId)) {
                        throw new ApiException("Access denied: You cannot access this biller.", HttpStatus.BAD_REQUEST);
                    }
                } catch (NoSuchMethodException ignored) {

                }
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                try {
                    Method getWarehouseIdMethod = request.getClass().getMethod("getWarehouseId");
                    Long warehouseId = (Long) getWarehouseIdMethod.invoke(request);
                    List<Long> accessibleWarehouses = Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList();
                    if (warehouseId != null && !accessibleWarehouses.contains(warehouseId)) {
                        throw new ApiException("Access denied: You cannot access this warehouse.", HttpStatus.BAD_REQUEST);
                    }
                } catch (NoSuchMethodException ignored) {

                }
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public void checkModule(String moduleKey) {
        try {
            Field field = SettingEntity.class.getDeclaredField(moduleKey);
            field.setAccessible(true);
            Object value = field.get(getSettings());
            if (!(value instanceof Integer) || ((Integer) value) != 1) {
                throw new ApiException("Module '" + getKeyNameByValue(Features.class, moduleKey) + "' is disabled.", HttpStatus.BAD_REQUEST);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ApiException("Invalid module key: '" + moduleKey + "'.", HttpStatus.BAD_REQUEST);
        }
    }

    private String getKeyNameByValue(Class<?> obj, String fieldValue) {
        for (Field field : obj.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(null);
                if (value instanceof String && value.equals(fieldValue)) {
                    return field.getName();
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return fieldValue;
    }

    public Object getPropertyObjectValue(Object obj, String propertyName) {
        if (obj == null || propertyName == null) return null;
        try {
            Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    public <T> List<List<T>> arrayChunk(List<T> list, int chunkSize) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            int end = Math.min(list.size(), i + chunkSize);
            chunks.add(list.subList(i, end));
        }
        return chunks;
    }

    public int getTextWidth(String text, Font font) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        g2d.dispose();
        return width;
    }

    public Font loadFont(String path, float fontSize) {
        Font font = null;
        try {
            FileInputStream fontStream = new FileInputStream(new File(path));
            font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (IOException e) {

        } catch (Exception e) {

        }
        return font.deriveFont(fontSize);
    }

    public void printLog(Object... objects) {
        System.out.println("=========================================");
        for (Object obj : objects) {
            if (obj != null) {
                String className = obj.getClass().getSimpleName();
                System.out.printf("%s:%n", className);
                if (obj instanceof Page<?> page) {
                    for (Object element : page.getContent()) {
                        if (element instanceof Tuple tuple) {
                            tuple.getElements().forEach(field -> {
                                Object value = tuple.get(field);
                                System.out.printf("%s: %s\n", field.getAlias(), value);
                            });
                            System.out.println("-----------------------------------------");
                        } else {
                            System.out.println(stringify(obj));
                        }
                    }
                } else {
                    System.out.printf("%s:%n", stringify(obj));
                }
            } else {
                System.out.println("null");
            }
            System.out.println("-----------------------------------------");
        }
        System.out.println("=========================================");
    }

    public String stringify(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj.getClass().isArray()) {
            return Arrays.deepToString((Object[]) obj);
        } else {
            return obj.toString();
        }
    }

    public List<String> months(String param) {
        if (param == null || param.trim().isEmpty()) {
            return Arrays.stream(Month.values()).map(month -> LocalDate.of(2000, month.getValue(), 1).format(DateTimeFormatter.ofPattern("MM"))).toList();
        }
        if (param.toLowerCase().trim().equals(Constant.SHORT)) {
            return Arrays.stream(new DateFormatSymbols().getShortMonths()).filter(shortMonth -> !shortMonth.isEmpty()).toList();
        } else {
            return Arrays.stream(new DateFormatSymbols().getMonths()).filter(fullMonth -> !fullMonth.isEmpty()).toList();
        }
    }

    public String determineGender(String genderInput) {
        if (genderInput != null && !genderInput.trim().isEmpty()) {
            String gender = genderInput.trim().toLowerCase();
            if (gender.equals("m") || gender.equals("male")) {
                return Constant.Gender.MALE;
            } else if (gender.equals("f") || gender.equals("female")) {
                return Constant.Gender.FEMALE;
            } else {
                throw new ApiException("Invalid gender. " + Constant.Gender.NOTE, HttpStatus.BAD_REQUEST);
            }
        } else {
            return null;
        }
    }

    public <T> boolean validateRequest(T request) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            int currentIndex = 1;
            StringBuilder errorMessage = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                errorMessage.append(violation.getMessage());
                errorMessage.append(currentIndex < violations.size() ? ", " : "");
                currentIndex++;
            }
            throw new ApiException(errorMessage.toString(), HttpStatus.BAD_REQUEST);
        }

        return true;
    }

    //////////////////////////////////////// FILE ////////////////////////////////////////

    public MultipartFile convertBytesToMultipartFile(byte[] imageBytes, String fileName) {
        return new MockMultipartFile("file", fileName, "image/png", imageBytes);
    }

    public String fileChecksum(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02X", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("File checksum failed.", e);
        }
    }

    public String fileContentType(Path path) {
        try {
            return Files.probeContentType(path);
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    public static void setFileAttribute(Path filePath, String key, String value) throws IOException {
        UserDefinedFileAttributeView view = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
        if (view != null) {
            view.write(key, StandardCharsets.UTF_8.encode(value));
        } else {
            throw new IOException("File attribute view is not supported on this file system.");
        }
    }

    public static String getFileAttribute(Path filePath, String key) {
        try {
            UserDefinedFileAttributeView view = Files.getFileAttributeView(filePath, UserDefinedFileAttributeView.class);
            if (view != null && view.list().contains(key)) {
                int size = view.size(key);
                if (size > 0) {
                    ByteBuffer buffer = ByteBuffer.allocate(view.size(key));
                    view.read(key, buffer);
                    buffer.flip();
                    return StandardCharsets.UTF_8.decode(buffer).toString();
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading attribute '" + key + "' for file: " + filePath + " - " + e.getMessage());
        }
        return null;
    }

    public List<FileInfoResponse> uploadFile(String type, MultipartFile file, HttpServletRequest servletRequest) {
        type = type != null ? type.trim().toLowerCase() : type;
        if (type == null || type.trim().isEmpty()) {
            throw new ApiException("Type is required.", HttpStatus.BAD_REQUEST);
        } else if (!Constant.Directory.VALID.contains(type)) {
            throw new ApiException("Type is invalid.", HttpStatus.BAD_REQUEST);
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException("File is empty.", HttpStatus.BAD_REQUEST);
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user               = (UserEntity) authentication.getPrincipal();
            String originalFilename       = Objects.requireNonNull(file.getOriginalFilename());
            String extension              = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            if (Constant.FileExtension.DOC.contains(extension)) {
                return handleDocumentUpload(type, file, user, servletRequest);
            } else if (Constant.FileExtension.IMG.contains(extension)) {
                return handleImageUpload(type, file, user, servletRequest);
            } else {
                throw new ApiException("Unsupported file type: " + extension, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public List<FileInfoResponse> handleDocumentUpload(String type, MultipartFile file, UserEntity user, HttpServletRequest servletRequest) throws IOException {
        List<FileInfoResponse> fileInfoResponses = new ArrayList<>();
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String extension        = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        String randomFilename   = generateRandomFilename("", extension);
        Path path = Paths.get(uploadDocPath, type, randomFilename).toAbsolutePath().normalize();
        if (!Files.exists(path.getParent())) {
            throw new ApiException("Directory '" + path.getParent() + "' not found.", HttpStatus.BAD_REQUEST);
        }
        if (Files.exists(path)) {
            throw new ApiException("File already exists: " + path.getFileName(), HttpStatus.BAD_REQUEST);
        }
        Files.copy(file.getInputStream(), path);
        setFileAttribute(path, "originalFilename", originalFilename);
        setFileAttribute(path, "uploadedBy", user.getUserId().toString());
        setFileAttribute(path, "createdDate", LocalDateTime.now().toString());
        file.getInputStream().close();
        fileInfoResponses.add(getFileInfo(type, randomFilename, null, servletRequest));

        return fileInfoResponses;
    }

    public List<FileInfoResponse> handleImageUpload(String type, MultipartFile file, UserEntity user, HttpServletRequest servletRequest) throws IOException {
        List<FileInfoResponse> fileInfoResponses = new ArrayList<>();
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String extension        = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        String randomFilename   = generateRandomFilename("", extension);
        String[] qualityLevels = {"original", "low", "medium", "high"};
        double[] compressionQualities = {1.0, 0.2, 0.5, 0.7};
        if ("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension)) {
            for (int i = 0; i < qualityLevels.length; i++) {
                Path path = Paths.get(uploadImgPath, type, "thumbnail", qualityLevels[i], randomFilename).toAbsolutePath().normalize();
                if (!Files.exists(path.getParent())) {
                    throw new ApiException("Directory '" + path.getParent() + "' not found.", HttpStatus.BAD_REQUEST);
                }
                if (Files.exists(path)) {
                    throw new ApiException("File already exists: " + path.getFileName(), HttpStatus.BAD_REQUEST);
                }
                InputStream fileStream = (i == 0) ? file.getInputStream() : compressImage(file, compressionQualities[i]);
                Files.copy(fileStream, path);
                setFileAttribute(path, "originalFilename", originalFilename);
                setFileAttribute(path, "uploadedBy", user.getUserId().toString());
                setFileAttribute(path, "createdDate", LocalDateTime.now().toString());
                fileStream.close();
                fileInfoResponses.add(getFileInfo(type, randomFilename, qualityLevels[i], servletRequest));
            }
        } else {
            Path path = Paths.get(uploadImgPath, type, "thumbnail", "original", randomFilename).toAbsolutePath().normalize();
            if (!Files.exists(path.getParent())) {
                throw new ApiException("Directory '" + path.getParent() + "' not found.", HttpStatus.BAD_REQUEST);
            }
            if (Files.exists(path)) {
                throw new ApiException("File already exists: " + path.getFileName(), HttpStatus.BAD_REQUEST);
            }
            InputStream fileStream = file.getInputStream();
            Files.copy(fileStream, path);
            setFileAttribute(path, "originalFilename", originalFilename);
            setFileAttribute(path, "uploadedBy", user.getUserId().toString());
            setFileAttribute(path, "createdDate", LocalDateTime.now().toString());
            fileStream.close();
            fileInfoResponses.add(getFileInfo(type, randomFilename, "original", servletRequest));
        }

        return fileInfoResponses;
    }

    public FileInfoResponse getFileInfo(String type, String fileName, String quality, HttpServletRequest servletRequest) {
        type     = type     != null ? type.trim().toLowerCase() : type;
        fileName = fileName != null ? fileName.trim() : fileName;
        quality  = quality  != null ? quality.trim().toLowerCase() : quality;

        if (type == null || type.isEmpty()) {
            throw new ApiException("Type is required.", HttpStatus.BAD_REQUEST);
        } else if (!Constant.Directory.VALID.contains(type)) {
            throw new ApiException("Type is invalid.", HttpStatus.BAD_REQUEST);
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new ApiException("Filename is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            Path path = filePath(type, fileName, quality);
            if (!Files.exists(path)) {
                throw new ApiException("File '" + fileName + "' not found.", HttpStatus.BAD_REQUEST);
            }
            File file = path.toFile();
            String originalFilename        = getFileAttribute(path, "originalFilename");
            String contentType             = fileContentType(path);
            String fileExtension           = fileName.substring(fileName.lastIndexOf('.') + 1);
            Long   size                    = file.length();
            String fileSizeInKB            = file.length() / 1024 + " KB";
            String filePath                = path.toString().replace("\\", "/");
            String downloadUrl             = baseUrl(servletRequest) + "/api/v1/setting/download/" + fileName;
            String checksum                = fileChecksum(file);
            String uploadedBy              = getFileAttribute(path, "uploadedBy");
            String createdDateStr          = getFileAttribute(path, "createdDate");
            LocalDateTime createdDate      = createdDateStr == null ? null : LocalDateTime.parse(createdDateStr);
            LocalDateTime lastModifiedDate = Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            return FileInfoResponse.builder()
                    .directory(type)
                    .originalFilename(originalFilename)
                    .fileName(fileName)
                    .fileType(contentType)
                    .quality(quality)
                    .size(size)
                    .downloadUrl(downloadUrl)
                    .filePath(filePath)
                    .fileSizeInKB(fileSizeInKB)
                    .checksum(checksum)
                    .fileExtension(fileExtension)
                    .uploadedBy(uploadedBy)
                    .createdDate(createdDate)
                    .lastModifiedDate(lastModifiedDate)
                    .build();

        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public FileInfoResponse getSystemFileInfo(String type, String fileName, String quality, HttpServletRequest servletRequest) {
        type     = type     != null ? type.trim().toLowerCase() : type;
        fileName = fileName != null ? fileName.trim() : fileName;
        quality  = quality  != null ? quality.trim().toLowerCase() : quality;

        if (type == null || type.isEmpty()) {
            throw new ApiException("Type is required.", HttpStatus.BAD_REQUEST);
        } else if (!Constant.Directory.VALID.contains(type)) {
            throw new ApiException("Type is invalid.", HttpStatus.BAD_REQUEST);
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new ApiException("Filename is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            Path path = filePath(type, fileName, quality, true);
            if (!Files.exists(path)) {
                throw new ApiException("File '" + fileName + "' not found.", HttpStatus.BAD_REQUEST);
            }
            File file = path.toFile();
            String originalFilename        = getFileAttribute(path, "originalFilename");
            String contentType             = fileContentType(path);
            String fileExtension           = fileName.substring(fileName.lastIndexOf('.') + 1);
            Long   size                    = file.length();
            String fileSizeInKB            = file.length() / 1024 + " KB";
            String filePath                = path.toString().replace("\\", "/");
            String downloadUrl             = baseUrl(servletRequest) + "/api/v1/setting/download/system/" + fileName;
            String checksum                = fileChecksum(file);
            String uploadedBy              = getFileAttribute(path, "uploadedBy");
            String createdDateStr          = getFileAttribute(path, "createdDate");
            LocalDateTime createdDate      = createdDateStr == null ? null : LocalDateTime.parse(createdDateStr);
            LocalDateTime lastModifiedDate = Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            return FileInfoResponse.builder()
                    .directory(type)
                    .originalFilename(originalFilename)
                    .fileName(fileName)
                    .fileType(contentType)
                    .quality(quality)
                    .size(size)
                    .downloadUrl(downloadUrl)
                    .filePath(filePath)
                    .fileSizeInKB(fileSizeInKB)
                    .checksum(checksum)
                    .fileExtension(fileExtension)
                    .uploadedBy(uploadedBy)
                    .createdDate(createdDate)
                    .lastModifiedDate(lastModifiedDate)
                    .build();

        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Resource downloadFile(String type, String fileName, String quality) {
        return downloadFile(type, fileName, quality, false);
    }

    public Resource downloadFile(String type, String fileName, String quality, boolean system) {
        try {
            Path path = filePath(type, fileName, quality, system);
            if (!Files.exists(path)) {
                throw new ApiException("File not found: " + fileName, HttpStatus.BAD_REQUEST);
            }
            return new FileSystemResource(path.toFile());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public Path filePath(String type, String fileName, String quality) {
        return filePath(type, fileName, quality, false);
    }

    public Path filePath(String type, String fileName, String quality, boolean system) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();

        if (Constant.FileExtension.DOC.contains(extension)) {
            return Paths.get((system ? sysDocPath : uploadDocPath), type).toAbsolutePath().normalize().resolve(fileName);
        } else if (Constant.FileExtension.IMG.contains(extension)) {
            final Set<String> VALID_QUALITY = Set.of("original", "low", "medium", "high");
            if (quality == null || quality.isEmpty() || !VALID_QUALITY.contains(quality)) {
                return Paths.get((system ? sysImgPath : uploadImgPath), type, "thumbnail", "original").toAbsolutePath().normalize().resolve(fileName);
            } else {
                return Paths.get((system ? sysImgPath : uploadImgPath), type, "thumbnail", quality).toAbsolutePath().normalize().resolve(fileName);
            }
        } else {
            throw new ApiException("Unsupported file type: " + extension, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    public InputStream compressImage(MultipartFile file, double quality) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());

        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        int newWidth = (int) (originalWidth * quality);
        int newHeight = (int) (originalHeight * quality);
        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        BufferedImage scaledBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledBufferedImage.createGraphics();
        g.drawImage(scaledImage, 0, 0, null);
        g.dispose();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String originalFilename = file.getOriginalFilename();
        String format = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase() : "jpg";
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new IOException("Unsupported image format: " + format);
        }
        ImageWriter writer = writers.next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
        writer.setOutput(imageOutputStream);
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) quality);
        }
        writer.write(null, new IIOImage(scaledBufferedImage, null, null), param);
        imageOutputStream.close();
        writer.dispose();

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public InputStream compressImage_(MultipartFile file, double quality) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String originalFilename = file.getOriginalFilename();
        String format = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase() : "jpg";
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new IOException("Unsupported image format: " + format);
        }
        ImageWriter writer = writers.next();

        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
        writer.setOutput(imageOutputStream);
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality((float) quality);
        }
        writer.write(null, new IIOImage(image, null, null), param);
        imageOutputStream.close();
        writer.dispose();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public InputStream compressImage__(MultipartFile file, double quality) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase() : "jpg";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if ("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension)) {
            Thumbnails.of(file.getInputStream())
                    .scale(quality)
                    .outputQuality(quality)
                    .toOutputStream(byteArrayOutputStream);
        } else {
            throw new IOException("Unsupported image format: " + extension);
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    //////////////////////////////////////// FILE ////////////////////////////////////////

    //////////////////////////////////////// FTP ////////////////////////////////////////

    @Autowired
    private SftpConfig sftpConfig;

    @Autowired
    private DefaultSftpSessionFactory sftpSessionFactory;

    private static final String ENCRYPTION_ALGORITHM = "Blowfish";
    private static final String ENCODING   = StandardCharsets.UTF_8.name();
    private static final String SECRET_KEY = "§£<2ε7";

    private static String hashMetadata(String metadata) {
        try {
            // MessageDigest digest = MessageDigest.getInstance("SHA-256");
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] encodedHash = digest.digest(metadata.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static byte[] compressLZ4(String metadata) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (LZ4BlockOutputStream lz4OutputStream = new LZ4BlockOutputStream(byteArrayOutputStream)) {
                lz4OutputStream.write(metadata.getBytes(StandardCharsets.UTF_8));
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static byte[] decompressLZ4(byte[] compressedData) throws IOException {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
            LZ4BlockInputStream lz4InputStream = new LZ4BlockInputStream(byteArrayInputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = lz4InputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static String base64Encode(byte[] compressedData) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(compressedData);
    }

    public static String decodeMetadata(String filename) {
        try {
            String[] parts = filename.split("-");
            String hash = parts.length > 1 ? parts[parts.length - 1] : null;
            if (hash != null && !hash.isEmpty()) {
                int lastDotIndex = hash.lastIndexOf('.');
                String name = (lastDotIndex != -1) ? hash.substring(0, lastDotIndex) : hash;

//                byte[] compressedData = Base64.getUrlDecoder().decode(name);
//                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
//                     LZ4BlockInputStream lz4InputStream = new LZ4BlockInputStream(byteArrayInputStream)) {
//                    byte[] decompressedData = new byte[1024];
//                    int bytesRead = lz4InputStream.read(decompressedData);
//                    if (bytesRead == -1) {
//                        throw new IOException("No data was decompressed");
//                    }
//                    return new String(decompressedData, 0, bytesRead, StandardCharsets.UTF_8);
//                }
//
//                byte[] compressedData = hexToBytes(name);
//                byte[] decompressedData = decompressLZ4(compressedData);
//                return new String(decompressedData, StandardCharsets.UTF_8);

                return decrypt(name);
            }
            return null;
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static String encrypt(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(ENCODING), ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes(ENCODING));
            return bytesToHex(encryptedData);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static String decrypt(String data) {
        try {
            byte[] encryptedData = hexToBytes(data);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(ENCODING), ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, ENCODING);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private static String bytesToHex(byte[] data) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : data) {
            hexString.append(String.format("%02x", b));  // Converts byte to hex
        }
        return hexString.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static String xorOperation(String input) {
        StringBuilder result = new StringBuilder();
        int keyLength = SECRET_KEY.length();
        for (int i = 0; i < input.length(); i++) {
            char encryptedChar = (char) (input.charAt(i) ^ SECRET_KEY.charAt(i % keyLength));
            result.append(encryptedChar);
        }
        return result.toString();
    }

    public FileInfoResponse uploadFtp(MultipartFile file, HttpServletRequest servletRequest) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File is empty.", HttpStatus.BAD_REQUEST);
        }
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String extension        = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
            String metadata         = originalFilename + "|" + user.getUserId().toString() + "|" + LocalDateTime.now();
            String randomFilename   = generateRandomFilename("", extension, metadata);
            String sftpTargetPath   = sftpConfig.filePath(randomFilename);
            SftpSession session     = sftpSessionFactory.getSession();
            try (InputStream inputStream = file.getInputStream()) {
                String remoteDirectory = sftpTargetPath.substring(0, sftpTargetPath.lastIndexOf('/'));
                if (!session.exists(remoteDirectory)) {
                    // session.mkdir(remoteDirectory);
                    throw new ApiException("Directory '" + remoteDirectory + "' not found.", HttpStatus.BAD_REQUEST);
                }
                if (session.exists(sftpTargetPath)) {
                    throw new ApiException("File already exists on the server: " + sftpTargetPath, HttpStatus.BAD_REQUEST);
                }
                session.write(inputStream, sftpTargetPath);
            }

            return getFileFtpInfo(randomFilename, servletRequest);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public FileInfoResponse getFileFtpInfo(String fileName, HttpServletRequest servletRequest) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new ApiException("Filename is required.", HttpStatus.BAD_REQUEST);
        }
        try {
            String path = sftpConfig.filePath(fileName);
            SftpSession session = sftpSessionFactory.getSession();
            if (!session.exists(path)) {
                throw new ApiException("File not found on the server: " + fileName, HttpStatus.BAD_REQUEST);
            }
            SftpClient.Attributes fileAttributes = session.getClientInstance().stat(path);
            List<String> metadata          = List.of(Objects.requireNonNull(decodeMetadata(fileName)).split("\\|"));
            String originalFilename        = metadata.get(0);
            String contentType             = sftpConfig.fileContentType(fileName);
            String fileExtension           = fileName.substring(fileName.lastIndexOf('.') + 1);
            long size                      = fileAttributes.getSize();
            String fileSizeInKB            = (size / 1024) + " KB";
            String filePath                = path.replace("\\", "/");
            String downloadUrl             = baseUrl(servletRequest) + "/api/v1/setting/download/" + fileName;
            String checksum                = sftpConfig.fileChecksum(path, session);
            String uploadedBy              = metadata.get(1);
            LocalDateTime createdDate      = LocalDateTime.parse(metadata.get(2));
            LocalDateTime lastModifiedDate = LocalDateTime.ofInstant(fileAttributes.getModifyTime().toInstant(), ZoneId.systemDefault());

            return FileInfoResponse.builder()
                    .originalFilename(originalFilename)
                    .fileName(fileName)
                    .fileType(contentType)
                    .size(size)
                    .downloadUrl(downloadUrl)
                    .filePath(filePath)
                    .fileSizeInKB(fileSizeInKB)
                    .checksum(checksum)
                    .fileExtension(fileExtension)
                    .uploadedBy(uploadedBy)
                    .createdDate(createdDate)
                    .lastModifiedDate(lastModifiedDate)
                    .build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

//    public Resource downloadFileFtp(String fileName) {
//        try {
//            String path = sftpConfig.filePath(fileName);
//            SftpSession session = sftpSessionFactory.getSession();
//            if (!session.exists(path)) {
//                throw new ApiException("File not found on the server: " + fileName, HttpStatus.BAD_REQUEST);
//            }
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            session.read(path, byteArrayOutputStream);
//            InputStream fileInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//            Path tempFilePath = Files.createTempFile(fileName, ".tmp");
//            Files.copy(fileInputStream, tempFilePath);
//            return new FileSystemResource(tempFilePath.toFile());
//        } catch (Exception e) {
//            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }

    public Resource downloadFileFtp(String fileName) {
        try {
            String path = sftpConfig.filePath(fileName);
            SftpSession sftpSession = sftpSessionFactory.getSession();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            sftpSession.read(path, outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            return new InputStreamResource(inputStream);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //////////////////////////////////////// FTP ////////////////////////////////////////

    //////////////////////////////////////// EXCEL ////////////////////////////////////////

    public int colIdx(String column) {
        return CellReference.convertColStringToIndex(column);
    }

    public <T> T getCellValue(Cell cell, Class<T> type) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                if (type == Integer.class) yield (T) Integer.valueOf((int) numericValue);
                if (type == Long.class) yield (T) Long.valueOf((long) numericValue);
                if (type == Double.class) yield (T) Double.valueOf(numericValue);
                if (type == String.class) yield (T) String.valueOf(numericValue).trim();
                if (type == LocalDate.class && DateUtil.isCellDateFormatted(cell)) {
                    yield (T) cell.getLocalDateTimeCellValue().toLocalDate();
                }
                yield null;
            }
            case STRING -> {
                String value = cell.getStringCellValue().trim();
                if (type == String.class) yield (T) value;
                if (type == Integer.class) {
                    try {
                        yield (T) Integer.valueOf(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        throw new ApiException("Invalid number format: '" + value + "'", HttpStatus.BAD_REQUEST);
                    }
                }
                if (type == Long.class) {
                    try {
                        yield (T) Long.valueOf(Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        throw new ApiException("Invalid number format: '" + value + "'", HttpStatus.BAD_REQUEST);
                    }
                }
                if (type == Double.class) {
                    try {
                        yield (T) Double.valueOf(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        throw new ApiException("Invalid number format: '" + value + "'", HttpStatus.BAD_REQUEST);
                    }
                }
                if (type == LocalDate.class) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateTime.DATE_FORMAT);
                        value = convertDateFormat(value);
                        yield (T) LocalDate.parse(value, formatter);
                    } catch (DateTimeParseException e) {
                        throw new ApiException("Invalid date format: '" + value + "'. Expected format: " + DateTime.DATE_FORMAT, HttpStatus.BAD_REQUEST);
                    }
                }
                yield null;
            }
            case BOOLEAN -> {
                boolean boolValue = cell.getBooleanCellValue();
                if (type == String.class) yield (T) String.valueOf(boolValue).trim();
                yield (T) Boolean.valueOf(boolValue);
            }
            default -> null;
        };
    }

    public byte[] getExcelImageByRow(Sheet sheet, int rowNum, int colNum) {
        if (!(sheet instanceof XSSFSheet xssfSheet)) return null;
        XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();
        if (drawing == null) return null;
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture picture) {
                XSSFClientAnchor anchor = picture.getPreferredSize();
                if (anchor.getRow1() == rowNum && anchor.getCol1() == colNum) {
                    return picture.getPictureData().getData();
                }
            }
        }
        return null;
    }

    //////////////////////////////////////// EXCEL ////////////////////////////////////////

}
