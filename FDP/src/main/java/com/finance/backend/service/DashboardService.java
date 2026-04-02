package com.finance.backend.service;

import com.finance.backend.dto.response.CategorySummaryResponse;
import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.MonthlyTrendResponse;
import com.finance.backend.dto.response.TransactionResponse;
import com.finance.backend.entity.Transaction;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardSummaryResponse getSummary(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null) dateFrom = LocalDate.of(2000, 1, 1);
        if (dateTo == null) dateTo = LocalDate.now();

        BigDecimal totalIncome = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.INCOME, dateFrom, dateTo);
        BigDecimal totalExpenses = transactionRepository.sumByTypeAndDateBetween(
                TransactionType.EXPENSE, dateFrom, dateTo);
        long count = transactionRepository.countByDateBetween(dateFrom, dateTo);

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .transactionCount(count)
                .dateRange(DashboardSummaryResponse.DateRange.builder()
                        .from(dateFrom.toString())
                        .to(dateTo.toString())
                        .build())
                .build();
    }

    public List<CategorySummaryResponse> getCategorySummary(
            TransactionType type, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null) dateFrom = LocalDate.of(2000, 1, 1);
        if (dateTo == null) dateTo = LocalDate.now();

        List<Object[]> results = transactionRepository.getCategorySummary(type, dateFrom, dateTo);

        BigDecimal grandTotal = results.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return results.stream()
                .map(r -> {
                    BigDecimal total = (BigDecimal) r[1];
                    double percentage = grandTotal.compareTo(BigDecimal.ZERO) == 0
                            ? 0.0
                            : total.multiply(BigDecimal.valueOf(100))
                            .divide(grandTotal, 2, RoundingMode.HALF_UP)
                            .doubleValue();

                    return CategorySummaryResponse.builder()
                            .category((String) r[0])
                            .total(total)
                            .percentage(percentage)
                            .count((Long) r[2])
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<MonthlyTrendResponse> getMonthlyTrend(int year) {
        List<Object[]> results = transactionRepository.getMonthlyTrend(year);

        Map<Integer, MonthlyTrendResponse> monthMap = new LinkedHashMap<>();

        // Initialize all 12 months with zero values
        for (int m = 1; m <= 12; m++) {
            monthMap.put(m, MonthlyTrendResponse.builder()
                    .month(m)
                    .monthName(Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .totalIncome(BigDecimal.ZERO)
                    .totalExpenses(BigDecimal.ZERO)
                    .netBalance(BigDecimal.ZERO)
                    .build());
        }

        // Fill in actual data
        for (Object[] row : results) {
            int month = ((Number) row[0]).intValue();
            BigDecimal income = row[1] instanceof BigDecimal
                    ? (BigDecimal) row[1]
                    : BigDecimal.valueOf(((Number) row[1]).doubleValue());
            BigDecimal expenses = row[2] instanceof BigDecimal
                    ? (BigDecimal) row[2]
                    : BigDecimal.valueOf(((Number) row[2]).doubleValue());

            MonthlyTrendResponse trend = monthMap.get(month);
            trend.setTotalIncome(income);
            trend.setTotalExpenses(expenses);
            trend.setNetBalance(income.subtract(expenses));
        }

        return new ArrayList<>(monthMap.values());
    }

    public List<TransactionResponse> getRecentTransactions(int limit) {
        if (limit <= 0) limit = 10;
        if (limit > 50) limit = 50;

        List<Transaction> transactions = transactionRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")));

        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .date(transaction.getDate())
                .notes(transaction.getNotes())
                .createdByName(transaction.getCreatedBy().getName())
                .createdByEmail(transaction.getCreatedBy().getEmail())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
