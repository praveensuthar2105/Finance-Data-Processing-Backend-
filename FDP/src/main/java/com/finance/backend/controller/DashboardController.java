package com.finance.backend.controller;

import com.finance.backend.dto.response.CategorySummaryResponse;
import com.finance.backend.dto.response.DashboardSummaryResponse;
import com.finance.backend.dto.response.MonthlyTrendResponse;
import com.finance.backend.dto.response.TransactionResponse;
import com.finance.backend.enums.TransactionType;
import com.finance.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Auth")
@Tag(name = "Dashboard", description = "Dashboard analytics and summaries")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get financial summary", description = "Total income, expenses, net balance")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved")
    })
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(dashboardService.getSummary(dateFrom, dateTo));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Get category breakdown", description = "Spending/income grouped by category")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category summary retrieved")
    })
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(dashboardService.getCategorySummary(type, dateFrom, dateTo));
    }

    @GetMapping("/monthly-trend")
    @Operation(summary = "Get monthly trend", description = "Monthly income/expenses for a given year, always 12 months")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly trend retrieved")
    })
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrend(
            @RequestParam int year) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrend(year));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recent transactions", description = "Most recent transactions, default 10, max 50")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recent transactions retrieved")
    })
    public ResponseEntity<List<TransactionResponse>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentTransactions(limit));
    }
}
