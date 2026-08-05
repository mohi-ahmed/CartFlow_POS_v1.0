package com.sparktech.cart_pos_v1_0.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsightDto {
    private TrendDto salesTrend;
    private TrendDto expenseTrend;
    private TrendDto profitTrend;
    private List<AnomalyDto> anomalies;
    private HighlightDto highlight;
}