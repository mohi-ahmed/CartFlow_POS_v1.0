package com.sparktech.cart_pos_v1_0.Services;

import com.sparktech.cart_pos_v1_0.DTO.*;
import com.sparktech.cart_pos_v1_0.Repositories.ExpenseRepository;
import com.sparktech.cart_pos_v1_0.Repositories.OrderItemRepository;
import com.sparktech.cart_pos_v1_0.Repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final OrderItemRepository orderItemRepository;
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("dd MMM");

    public ReportDto generateReport(LocalDate from, LocalDate to) {

        List<DailyAmountDto> salesByDate = saleRepository.sumSalesGroupByDate(from, to);
        List<DailyAmountDto> expenseByDate = expenseRepository.sumExpenseGroupByDate(from, to);

        Map<LocalDate, Double> salesMap = salesByDate.stream()
                .collect(Collectors.toMap(DailyAmountDto::getDate, DailyAmountDto::getAmount));
        Map<LocalDate, Double> expenseMap = expenseByDate.stream()
                .collect(Collectors.toMap(DailyAmountDto::getDate, DailyAmountDto::getAmount));

        // range er প্রতিটা date fill করা হচ্ছে, jate line chart e gap na thake
        List<String> dateLabels = new ArrayList<>();
        List<Double> salesSeries = new ArrayList<>();
        List<Double> expenseSeries = new ArrayList<>();
        List<Double> profitSeries = new ArrayList<>();

        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            double sales = salesMap.getOrDefault(d, 0.0);
            double expense = expenseMap.getOrDefault(d, 0.0);

            dateLabels.add(d.format(LABEL_FORMAT));
            salesSeries.add(sales);
            expenseSeries.add(expense);
            profitSeries.add(sales - expense);
        }

        List<CategoryAmountDto> categoryData = expenseRepository.sumExpenseGroupByCategory(from, to);
        List<String> categoryLabels = categoryData.stream().map(CategoryAmountDto::getCategory).toList();
        List<Double> categoryValues = categoryData.stream().map(CategoryAmountDto::getAmount).toList();

//        List<ProductSalesDto> topProducts = saleRepository.sumQuantityGroupByProduct(from, to)
//                .stream().limit(5).toList();
//        List<String> productLabels = topProducts.stream().map(ProductSalesDto::getProductName).toList();
//        List<Long> productQuantities = topProducts.stream().map(ProductSalesDto::getTotalQuantity).toList();
        List<ProductVelocityDto> topProducts = orderItemRepository.sumQuantityGroupByProduct(from, to)
                .stream().limit(5).toList();
        List<String> productLabels = topProducts.stream().map(ProductVelocityDto::getProductName).toList();
        List<Long> productQuantities = topProducts.stream().map(ProductVelocityDto::getTotalQuantity).toList();

        double totalSales = salesSeries.stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = expenseSeries.stream().mapToDouble(Double::doubleValue).sum();
        double totalProfit = totalSales - totalExpense;

        return new ReportDto(dateLabels, salesSeries, expenseSeries, profitSeries,
                categoryLabels, categoryValues, productLabels, productQuantities,
                totalSales, totalExpense, totalProfit);
    }
}