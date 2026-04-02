package com.finance.backend.controller;

import com.finance.backend.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void testGetMonthlyTrend_Has12Months() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        int currentYear = java.time.LocalDate.now().getYear();

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
}
