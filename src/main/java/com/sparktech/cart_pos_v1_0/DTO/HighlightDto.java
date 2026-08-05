package com.sparktech.cart_pos_v1_0.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HighlightDto {
    private LocalDate bestSalesDate;
    private double bestSalesAmount;
    private LocalDate worstSalesDate;
    private double worstSalesAmount;
}