package com.finance.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.backend.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // POST /api/transactions

    @Test
    void testCreateTransaction_AsAdmin_Returns201() throws Exception {
        String token = TestUtils.getAdminToken(mockMvc);

        String body = """
                {
                    "amount": 1000.00,
                    "type": "INCOME",
                    "category": "Salary",
                    "date": "2024-06-15",
                    "notes": "Admin income"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreateTransaction_AsAnalyst_Returns201() throws Exception {
        String token = TestUtils.getAnalystToken(mockMvc);

        String body = """
                {
                    "amount": 1500.00,
                    "type": "INCOME",
                    "category": "Freelance",
                    "date": "2024-06-15",
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
                    "date": "2024-06-15"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateTransaction_WithoutAuth_Returns4xx() throws Exception {
        String body = """
                {
                    "amount": 100.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateTransaction_MissingFields_Returns400() throws Exception {
        String token = TestUtils.getAnalystToken(mockMvc);

        String body = """
                {
                    "type": "INCOME",
                    "category": "Freelance",
                    "date": "2024-06-15"
                }
                """; // Missing amount

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_NegativeAmount_Returns400() throws Exception {
        String token = TestUtils.getAnalystToken(mockMvc);

        String body = """
                {
                    "amount": -50.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTransaction_InvalidType_Returns400() throws Exception {
        String token = TestUtils.getAnalystToken(mockMvc);

        String body = """
                {
                    "amount": 50.00,
                    "type": "INVALID",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // GET /api/transactions

    @Test
    void testGetTransactions_ReturnsPaginated() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetTransactions_WithTypeFilter_Income() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.type != 'INCOME')]").isEmpty());
    }

    @Test
    void testGetTransactions_WithTypeFilter_Expense() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.type != 'EXPENSE')]").isEmpty());
    }

    @Test
    void testGetTransactions_WithCategoryFilter() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("category", "Salary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.category != 'Salary')]").isEmpty());
    }

    @Test
    void testGetTransactions_WithDateRange() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("dateFrom", "2024-01-01")
                        .param("dateTo", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testGetTransactions_Pagination() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(5)));
    }

    @Test
    void testGetTransactions_WithoutAuth_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().is4xxClientError());
    }

    // GET /api/transactions/{id}

    @Test
    void testGetTransactionById_Returns200() throws Exception {
        String token = TestUtils.getAdminToken(mockMvc);

        // Get a transaction
        String listResponse = mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "1"))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode responseNode = objectMapper.readTree(listResponse);
        Long id = 1L;
        if (responseNode.has("content") && responseNode.get("content").isArray() && responseNode.get("content").size() > 0) {
            id = responseNode.get("content").get(0).get("id").asLong();
        } else if (responseNode.isArray() && responseNode.size() > 0) {
            id = responseNode.get(0).get("id").asLong();
        }

        mockMvc.perform(get("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void testGetTransactionById_InvalidId_Returns404() throws Exception {
        String token = TestUtils.getAdminToken(mockMvc);

        mockMvc.perform(get("/api/transactions/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // PUT /api/transactions/{id}

    @Test
    void testUpdateTransaction_AsAdmin_Returns200() throws Exception {
        String token = TestUtils.getAdminToken(mockMvc);

        // Get any transaction
        String listResponse = mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "1"))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode responseNode = objectMapper.readTree(listResponse);
        Long id = 1L;
        if (responseNode.has("content") && responseNode.get("content").isArray() && responseNode.get("content").size() > 0) {
            id = responseNode.get("content").get(0).get("id").asLong();
        } else if (responseNode.isArray() && responseNode.size() > 0) {
            id = responseNode.get(0).get("id").asLong();
        }

        String body = """
                {
                    "amount": 2000.00,
                    "type": "INCOME",
                    "category": "Salary",
                    "date": "2024-06-15",
                    "notes": "Updated admin"
                }
                """;

        mockMvc.perform(put("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(2000.00));
    }

    @Test
    void testUpdateTransaction_AsAnalyst_OwnTransaction_Returns200() throws Exception {
        String analystToken = TestUtils.getAnalystToken(mockMvc);

        // Create transaction as analyst
        String createBody = """
                {
                    "amount": 100.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Update it
        String updateBody = """
                {
                    "amount": 150.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        mockMvc.perform(put("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.00));
    }

    @Test
    void testUpdateTransaction_AsAnalyst_AnotherUsersTransaction_Returns403() throws Exception {
        String adminToken = TestUtils.getAdminToken(mockMvc);
        String analystToken = TestUtils.getAnalystToken(mockMvc);

        // Create transaction as admin
        String createBody = """
                {
                    "amount": 100.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Update it as analyst
        String updateBody = """
                {
                    "amount": 150.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        mockMvc.perform(put("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + analystToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateTransaction_AsViewer_Returns403() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        String body = """
                {
                    "amount": 150.00,
                    "type": "EXPENSE",
                    "category": "Food",
                    "date": "2024-06-15"
                }
                """;

        mockMvc.perform(put("/api/transactions/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // DELETE /api/transactions/{id}

    @Test
    void testDeleteTransaction_AsAdmin_Returns204() throws Exception {
        String token = TestUtils.getAdminToken(mockMvc);

        // Create
        String createBody = """
                {
                    "amount": 999.00,
                    "type": "EXPENSE",
                    "category": "Test",
                    "date": "2024-06-10"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Delete
        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Subsequent GET is 404
        mockMvc.perform(get("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteTransaction_AsAnalyst_OwnTransaction_Returns204() throws Exception {
        String token = TestUtils.getAnalystToken(mockMvc);

        // Create
        String createBody = """
                {
                    "amount": 999.00,
                    "type": "EXPENSE",
                    "category": "Test",
                    "date": "2024-06-10"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Delete
        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteTransaction_AsAnalyst_AnotherUsersTransaction_Returns403() throws Exception {
        String adminToken = TestUtils.getAdminToken(mockMvc);
        String analystToken = TestUtils.getAnalystToken(mockMvc);

        // Create as admin
        String createBody = """
                {
                    "amount": 999.00,
                    "type": "EXPENSE",
                    "category": "Test",
                    "date": "2024-06-10"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // Delete as analyst
        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteTransaction_AsViewer_Returns403() throws Exception {
        String token = TestUtils.getViewerToken(mockMvc);

        mockMvc.perform(delete("/api/transactions/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteTransaction_AlreadyDeleted_Returns404() throws Exception {
        String token = TestUtils.getAdminToken(mockMvc);

        // Create
        String createBody = """
                {
                    "amount": 999.00,
                    "type": "EXPENSE",
                    "category": "Test",
                    "date": "2024-06-10"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // First Delete
        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Second Delete
        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
