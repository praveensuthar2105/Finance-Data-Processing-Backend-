package com.finance.backend.controller;

import com.finance.backend.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // GET /api/dashboard/summary

    @Test
    void testGetSummary_ReturnsCorrectTotals() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpenses").exists())
                .andExpect(jsonPath("$.netBalance").exists())
                .andExpect(jsonPath("$.transactionCount").exists())
                .andExpect(jsonPath("$.dateRange.from").exists())
                .andExpect(jsonPath("$.dateRange.to").exists());
    }

    @Test
    void testGetSummary_WithDateRange() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token)
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpenses").exists())
                .andExpect(jsonPath("$.netBalance").exists())
                .andExpect(jsonPath("$.dateRange.from").value("2024-01-01"))
                .andExpect(jsonPath("$.dateRange.to").value("2024-12-31"));
    }

    @Test
    void testGetSummary_WithoutAuth_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().is4xxClientError());
    }

    // GET /api/dashboard/by-category

    @Test
    void testGetByCategory_Expense() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/dashboard/by-category")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").exists())
                .andExpect(jsonPath("$[0].total").exists())
                .andExpect(jsonPath("$[0].count").exists())
                .andExpect(jsonPath("$[0].percentage").exists());
    }

    @Test
    void testGetByCategory_Income() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/dashboard/by-category")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").exists())
                .andExpect(jsonPath("$[0].total").exists())
                .andExpect(jsonPath("$[0].count").exists())
                .andExpect(jsonPath("$[0].percentage").exists());
    }

    @Test
    void testGetByCategory_WithoutAuth_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/dashboard/by-category")
                        .param("type", "EXPENSE"))
                .andExpect(status().is4xxClientError());
    }

    // GET /api/dashboard/monthly-trend

    @Test
    void testGetMonthlyTrend_Has12Months() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        int currentYear = 2026;

        mockMvc.perform(get("/api/dashboard/monthly-trend")
                        .header("Authorization", "Bearer " + token)
                        .param("year", String.valueOf(currentYear)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)))
                .andExpect(jsonPath("$[0].month").value(1))
                .andExpect(jsonPath("$[0].monthName").value("January"))
                .andExpect(jsonPath("$[11].month").value(12))
                .andExpect(jsonPath("$[11].monthName").value("December"));
    }

    @Test
    void testGetMonthlyTrend_WithoutAuth_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/dashboard/monthly-trend")
                        .param("year", "2024"))
                .andExpect(status().is4xxClientError());
    }

    // GET /api/dashboard/recent

    @Test
    void testGetRecent_DefaultLimit10() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/dashboard/recent")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(10))));
    }

    @Test
    void testGetRecent_CustomLimit() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/dashboard/recent")
                        .header("Authorization", "Bearer " + token)
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(5))));
    }
}
