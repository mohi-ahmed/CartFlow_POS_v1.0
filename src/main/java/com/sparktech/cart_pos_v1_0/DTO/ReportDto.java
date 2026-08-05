package com.sparktech.cart_pos_v1_0.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private List<String> dateLabels;
    private List<Double> salesSeries;
    private List<Double> expenseSeries;
    private List<Double> profitSeries;

    private List<String> categoryLabels;
    private List<Double> categoryValues;

    private List<String> productLabels;
    private List<Long> productQuantities;

    private double totalSales;
    private double totalExpense;
    private double totalProfit;
}