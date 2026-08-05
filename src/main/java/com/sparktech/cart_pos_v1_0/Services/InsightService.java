package com.sparktech.cart_pos_v1_0.Services;

import com.sparktech.cart_pos_v1_0.DTO.*;
import com.sparktech.cart_pos_v1_0.Repositories.ExpenseRepository;
import com.sparktech.cart_pos_v1_0.Repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InsightService {

    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;

    // ১.৫ standard deviation dure gele "unusual" hisebe dhora hobe
    private static final double ANOMALY_THRESHOLD = 1.5;

    // ekta din theke "unusual" bolar jonno onto kom 4 din er data lagbe (noyle statistic e vorosha kora jay na)
    private static final int MIN_DAYS_FOR_ANOMALY = 4;

    public InsightDto generateInsights(LocalDate from, LocalDate to) {

        List<DailyAmountDto> currentSales = saleRepository.sumSalesGroupByDate(from, to);
        List<DailyAmountDto> currentExpense = expenseRepository.sumExpenseGroupByDate(from, to);

        // ---------- agerbarer soman-doirgher period ber kora ----------
        long dayCount = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate prevTo = from.minusDays(1);
        LocalDate prevFrom = prevTo.minusDays(dayCount - 1);

        List<DailyAmountDto> prevSales = saleRepository.sumSalesGroupByDate(prevFrom, prevTo);
        List<DailyAmountDto> prevExpense = expenseRepository.sumExpenseGroupByDate(prevFrom, prevTo);

        double currentSalesTotal = sum(currentSales);
        double currentExpenseTotal = sum(currentExpense);
        double prevSalesTotal = sum(prevSales);
        double prevExpenseTotal = sum(prevExpense);

        TrendDto salesTrend = buildTrend("Sales", currentSalesTotal, prevSalesTotal);
        TrendDto expenseTrend = buildTrend("Expense", currentExpenseTotal, prevExpenseTotal);
        TrendDto profitTrend = buildTrend("Profit",
                currentSalesTotal - currentExpenseTotal,
                prevSalesTotal - prevExpenseTotal);

        List<AnomalyDto> anomalies = new ArrayList<>();
        anomalies.addAll(detectAnomalies(currentSales, "Sales"));
        anomalies.addAll(detectAnomalies(currentExpense, "Expense"));
        anomalies.sort(Comparator.comparing(AnomalyDto::getDate));

        HighlightDto highlight = buildHighlight(currentSales);

        return new InsightDto(salesTrend, expenseTrend, profitTrend, anomalies, highlight);
    }

    private double sum(List<DailyAmountDto> list) {
        return list.stream().mapToDouble(DailyAmountDto::getAmount).sum();
    }

    // ---------- Trend % hisheb kora ----------
    private TrendDto buildTrend(String label, double current, double previous) {
        double percent;
        String direction;
        String displayText;

        if (previous == 0) {
            if (current == 0) {
                // agerbar o ekhon o kono activity nei
                percent = 0;
                direction = "flat";
                displayText = "No data yet";
            } else {
                // agerbar kono baseline e chilo na, tai % change mane-hin — "New" dekhano thik
                percent = 0;
                direction = "new";
                displayText = "🆕 New activity";
            }
        } else {
            percent = ((current - previous) / previous) * 100;
            direction = percent > 1 ? "up" : percent < -1 ? "down" : "flat";
            String arrow = direction.equals("up") ? "▲" : direction.equals("down") ? "▼" : "–";
            displayText = arrow + " " + String.format("%.1f", Math.abs(percent)) + "%";
        }

        return new TrendDto(label, round(current), round(previous), round(percent), direction, displayText);
    }
    // ---------- Mean + StdDev diye Anomaly khuje bar kora ----------
    private List<AnomalyDto> detectAnomalies(List<DailyAmountDto> data, String type) {
        List<AnomalyDto> result = new ArrayList<>();
        if (data.size() < MIN_DAYS_FOR_ANOMALY) return result;

        double mean = data.stream().mapToDouble(DailyAmountDto::getAmount).average().orElse(0);
        double variance = data.stream()
                .mapToDouble(d -> Math.pow(d.getAmount() - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        if (stdDev == 0) return result; // shob din er value same, kono "unusual" nei

        for (DailyAmountDto d : data) {
            double diff = d.getAmount() - mean;
            if (Math.abs(diff) >= ANOMALY_THRESHOLD * stdDev) {
                String message = diff > 0
                        ? type + " was unusually high this day"
                        : type + " was unusually low this day";
                result.add(new AnomalyDto(d.getDate(), type, round(d.getAmount()), round(mean), message));
            }
        }
        return result;
    }

    // ---------- Best / Worst sales day ----------
    private HighlightDto buildHighlight(List<DailyAmountDto> currentSales) {
        if (currentSales.isEmpty()) return new HighlightDto(null, 0, null, 0);

        DailyAmountDto best = currentSales.stream()
                .max(Comparator.comparing(DailyAmountDto::getAmount)).orElseThrow();
        DailyAmountDto worst = currentSales.stream()
                .min(Comparator.comparing(DailyAmountDto::getAmount)).orElseThrow();

        return new HighlightDto(best.getDate(), round(best.getAmount()), worst.getDate(), round(worst.getAmount()));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}