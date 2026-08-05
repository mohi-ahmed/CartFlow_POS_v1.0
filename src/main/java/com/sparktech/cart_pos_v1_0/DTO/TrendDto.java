package com.sparktech.cart_pos_v1_0.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDto {
    private String label;
    private double currentValue;
    private double previousValue;
    private double percentChange;
    private String direction; // "up" / "down" / "flat"
    private String displayText;
}