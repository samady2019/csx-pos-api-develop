package kh.com.csx.posapi.service;

import kh.com.csx.posapi.dto.setting.*;
import kh.com.csx.posapi.entity.*;
import kh.com.csx.posapi.repository.*;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import kh.com.csx.posapi.exception.ApiException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettingService {
    private final SettingRepository settingRepository;
    private final PosSettingRepository posSettingRepository;
    private final TaxRateRepository taxRateRepository;
    private final BillerRepository billerRepository;
    private final WarehouseRepository warehouseRepository;
    private final CurrencyRepository currencyRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    private final Utility utility;

    public SettingResponse retrieveSystem() {
        SettingEntity settingEntity = utility.getSettings();
        return SettingResponse.builder().setting(settingEntity).build();
    }

    public SettingResponse updateSystem(SettingRequest request) {
        taxRateRepository.findById(request.getDefaultTaxRate()).orElseThrow(() -> new ApiException("Tax rate not found.", HttpStatus.BAD_REQUEST));
        billerRepository.findById(request.getDefaultBiller()).orElseThrow(() -> new ApiException("Biller not found.", HttpStatus.BAD_REQUEST));
        warehouseRepository.findById(request.getDefaultWarehouse()).orElseThrow(() -> new ApiException("Warehouse not found.", HttpStatus.BAD_REQUEST));
        currencyRepository.findFirstByCode(request.getDefaultCurrency().trim()).orElseThrow(() -> new ApiException("Currency not found.", HttpStatus.BAD_REQUEST));
        supplierRepository.findById(request.getDefaultSupplier()).orElseThrow(() -> new ApiException("Supplier not found.", HttpStatus.BAD_REQUEST));
        customerRepository.findById(request.getDefaultCustomer()).orElseThrow(() -> new ApiException("Customer not found.", HttpStatus.BAD_REQUEST));

        SettingEntity setting = utility.getSettings();
        setting.setLogo(request.getLogo());
        setting.setSmallLogo(request.getSmallLogo());
        setting.setBigLogo(request.getBigLogo());
        setting.setSiteName(request.getSiteName());
        setting.setDateFormat(request.getDateFormat());
        setting.setDateTimeFormat(request.getDateTimeFormat());
        setting.setReferenceFormat(request.getReferenceFormat());
        setting.setReferenceReset(request.getReferenceReset());
        setting.setAlertDay(request.getAlertDay());
        setting.setExpiryAlertDays(request.getExpiryAlertDays());
        setting.setProductExpiry(request.getProductExpiry());
        setting.setDecimals(request.getDecimals());
        setting.setQuantityDecimals(request.getQuantityDecimals());
        setting.setUpdateCost(request.getUpdateCost());
        setting.setMultiWarehouse(request.getMultiWarehouse());
        setting.setMultiBiller(request.getMultiBiller());
        setting.setDefaultCurrency(request.getDefaultCurrency().trim());
        setting.setDefaultTaxRate(request.getDefaultTaxRate());
        setting.setDefaultTaxRateDeclare(request.getDefaultTaxRateDeclare());
        setting.setDefaultBiller(request.getDefaultBiller());
        setting.setDefaultWarehouse(request.getDefaultWarehouse());
        setting.setDefaultSupplier(request.getDefaultSupplier());
        setting.setDefaultCustomer(request.getDefaultCustomer());
        setting.setPurchaseOrderPrefix(request.getPurchaseOrderPrefix());
        setting.setPurchasePrefix(request.getPurchasePrefix());
        setting.setPurchaseReturnPrefix(request.getPurchaseReturnPrefix());
        setting.setSaleOrderPrefix(request.getSaleOrderPrefix());
        setting.setSalePrefix(request.getSalePrefix());
        setting.setSaleReturnPrefix(request.getSaleReturnPrefix());
        setting.setPosPrefix(request.getPosPrefix());
        setting.setBillPrefix(request.getBillPrefix());
        setting.setTransferPrefix(request.getTransferPrefix());
        setting.setAdjustmentPrefix(request.getAdjustmentPrefix());
        setting.setStockCountPrefix(request.getStockCountPrefix());
        setting.setDeliveryPrefix(request.getDeliveryPrefix());
        setting.setExpensePrefix(request.getExpensePrefix());
        setting.setPaymentPrefix(request.getPaymentPrefix());
        setting.setAccountingMethod(request.getAccountingMethod());
        setting.setAccounting(request.getAccounting());
        setting.setOverselling(request.getOverselling());
        setting.setRowsPerPage(request.getRowsPerPage());
        setting.setLanguage(request.getLanguage());
        setting.setTimezone(request.getTimezone());
        setting.setTheme(request.getTheme());
        setting.setVersion(request.getVersion());
        setting.setDevelopedBy(request.getDevelopedBy());
        setting.setLicenseName(request.getLicenseName());
        setting.setLicenseKey(request.getLicenseKey());
        setting.setLastModified(LocalDateTime.now());
        try {
            settingRepository.save(setting);
            return retrieveSystem();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public PosSettingResponse retrievePos() {
        PosSettingEntity posSettingEntity = utility.getPosSettings();
        return PosSettingResponse.builder().posSetting(posSettingEntity).build();
    }

    public PosSettingResponse updatePos(PosSettingRequest request) {
        brandRepository.findByBrandId(request.getDefaultBrand()).orElseThrow(() -> new ApiException("Brand not found.", HttpStatus.BAD_REQUEST));
        categoryRepository.findByCategoryId(request.getDefaultCategory()).orElseThrow(() -> new ApiException("Category not found.", HttpStatus.BAD_REQUEST));

        PosSettingEntity posSetting = utility.getPosSettings();
        posSetting.setCategoryLimit(request.getCategoryLimit());
        posSetting.setBrandLimit(request.getBrandLimit());
        posSetting.setProductLimit(request.getProductLimit());
        posSetting.setDefaultCategory(request.getDefaultCategory());
        posSetting.setDefaultBrand(request.getDefaultBrand());
        posSetting.setShowCategory(request.getShowCategory());
        posSetting.setShowQuantity(request.getShowQuantity());
        posSetting.setDisplayTime(request.getDisplayTime());
        posSetting.setCouponCard(request.getCouponCard());
        posSetting.setSaleDue(request.getSaleDue());
        posSetting.setPinCode(request.getPinCode());
        posSetting.setPosType(request.getPosType());
        posSetting.setLastModified(LocalDateTime.now());
        try {
            posSettingRepository.save(posSetting);
            return retrievePos();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
