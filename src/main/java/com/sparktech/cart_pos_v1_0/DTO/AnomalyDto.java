package com.sparktech.cart_pos_v1_0.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDto {
    private LocalDate date;
    private String type;      // "Sales" / "Expense"
    private double value;
    private double average;
    private String message;
}