package com.finance.backend.controller;

import com.finance.backend.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateTransaction_AsAnalyst_Returns201() throws Exception {
        String token = TestUtils.getAnalystToken(mockMvc);

        String body = """
                {
                    "amount": 1500.00,
                    "type": "INCOME",
                    "category": "Freelance",
                    "date": "2024-01-15",
                    "notes": "Contract work"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("Freelance"));
    }

    @Test
    void testCreateTransaction_AsViewer_Returns403() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        String body = """
                {
                    "amount": 100.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-01-15"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetTransactions_WithFilters() throws Exception {
        String token = TestUtils.getAnalystToken(mockMvc);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "INCOME")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testSoftDelete_RecordNotInSubsequentGet() throws Exception {
        String token = TestUtils.getAdminToken(mockMvc);

        // Create a transaction
        String body = """
                {
                    "amount": 999.00,
                    "type": "EXPENSE",
                    "category": "Test",
                    "date": "2024-01-10",
                    "notes": "To be deleted"
                }
                """;

        String createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract ID from response
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Long id = mapper.readTree(createResult).get("id").asLong();

        // Delete (soft)
        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verify it's gone from GET
        mockMvc.perform(get("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
