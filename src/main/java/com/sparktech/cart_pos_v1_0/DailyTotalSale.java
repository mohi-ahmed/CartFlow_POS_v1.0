package com.sparktech.cart_pos_v1_0;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DailyTotalSale {
    @Id
    private LocalDate date;

    private int transactionCount;

    private int itemsSold;

    private double  revenue;


}
