package kh.com.csx.posapi.service.report;

import jakarta.persistence.Tuple;
import kh.com.csx.posapi.constant.Constant;
import kh.com.csx.posapi.repository.report.ChartReportRepository;
import kh.com.csx.posapi.dto.report.chartReport.*;
import kh.com.csx.posapi.entity.UserEntity;
import kh.com.csx.posapi.exception.ApiException;
import kh.com.csx.posapi.utility.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChartReportService {
    private final ChartReportRepository chartReportRepository;
    private final Utility utility;

    public List<OverviewResponse> overview(OverviewRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            List<Tuple> periods = chartReportRepository.findPeriodOverview(request);
            Collections.reverse(periods);
            return periods.stream().map(period -> {
                String month   = (String) period.get("month");
                String year    = (String) period.get("year");
                Tuple sale     = chartReportRepository.findSaleByPeriod(request, year, month);
                Tuple purchase = chartReportRepository.findPurchaseByPeriod(request, year, month);
                return new OverviewResponse(period, sale, purchase);
            }).toList();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public StockValueResponse stockValue(OverviewRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Tuple result = chartReportRepository.findStockValue(request);
            return new StockValueResponse(result);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public List<StockCategoryResponse> stockCategories(OverviewRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            List<Tuple> result = chartReportRepository.findStockCategories(request);
            return result.stream().map(StockCategoryResponse::new).toList();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public List<StockWarehouseResponse> stockWarehouses(OverviewRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            List<Tuple> result = chartReportRepository.findStockWarehouses(request);
            return result.stream().map(StockWarehouseResponse::new).toList();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public BestSellerResponse bestSellers(OverviewRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            if (user.getWarehouses() != null && !user.getWarehouses().trim().isEmpty()) {
                request.setWhIds(Arrays.stream(user.getWarehouses().split(",")).map(Long::valueOf).toList());
            }
            Date date = new Date();
            if (request.getMonth() == null || request.getMonth().trim().isEmpty()) {
                DateFormat dateFormat = new SimpleDateFormat("MM");
                request.setMonth(dateFormat.format(date));
            }
            if (request.getYear() == null || request.getYear().trim().isEmpty()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy");
                request.setYear(dateFormat.format(date));
            }
            List<Tuple> result = chartReportRepository.findBestSellers(request);
            return new BestSellerResponse(request.getMonth(), request.getYear(), result);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ExpenseCategoryResponse expenseCategories(OverviewRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            if (user.getViewRight().equals(Constant.User.ViewRight.OWN)) {
                request.setUser(user.getUserId());
            }
            if (user.getBillers() != null && !user.getBillers().trim().isEmpty()) {
                request.setBIds(Arrays.stream(user.getBillers().split(",")).map(Long::valueOf).toList());
            }
            Date date = new Date();
            if (request.getMonth() == null || request.getMonth().trim().isEmpty()) {
                DateFormat dateFormat = new SimpleDateFormat("MM");
                request.setMonth(dateFormat.format(date));
            }
            if (request.getYear() == null || request.getYear().trim().isEmpty()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy");
                request.setYear(dateFormat.format(date));
            }
            List<Tuple> result = chartReportRepository.findExpenseCategories(request);
            return new ExpenseCategoryResponse(request.getMonth(), request.getYear(), result);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
