package com.sparktech.cart_pos_v1_0.Controller;

import com.sparktech.cart_pos_v1_0.DTO.InsightDto;
import com.sparktech.cart_pos_v1_0.DTO.ReportDto;
import com.sparktech.cart_pos_v1_0.Services.InsightService;
import com.sparktech.cart_pos_v1_0.Services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reports")
public class AnalyticsController {

    private final ReportService reportService;
    private final InsightService insightService;

    @GetMapping
    public String showReports(
            @RequestParam(required = false, defaultValue = "30d") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        LocalDate today = LocalDate.now();
        LocalDate from;
        LocalDate to = today;

        if ("custom".equals(range) && dateFrom != null && dateTo != null) {
            from = dateFrom;
            to = dateTo;
        } else if ("7d".equals(range)) {
            from = today.minusDays(6);
        } else if ("month".equals(range)) {
            from = today.withDayOfMonth(1);
        } else {
            from = today.minusDays(29);
            range = "30d";
        }

        ReportDto report = reportService.generateReport(from, to);
        InsightDto insight = insightService.generateInsights(from, to);

        model.addAttribute("report", report);
        model.addAttribute("activeRange", range);
        model.addAttribute("dateFrom", from);
        model.addAttribute("insight", insight);
        model.addAttribute("dateTo", to);

        return "reports";
    }
}