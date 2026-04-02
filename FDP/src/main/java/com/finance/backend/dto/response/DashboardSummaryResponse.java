package com.finance.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private long transactionCount;
    private DateRange dateRange;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DateRange {
        private String from;
        private String to;
    }
}
