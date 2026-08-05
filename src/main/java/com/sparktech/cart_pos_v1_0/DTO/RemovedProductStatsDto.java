package com.sparktech.cart_pos_v1_0.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemovedProductStatsDto {
    private long totalRemoved;
    private long removedThisMonth;
    private String mostRecentName;

}
