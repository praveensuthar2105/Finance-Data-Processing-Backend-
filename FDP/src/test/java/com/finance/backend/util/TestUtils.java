package com.finance.backend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String registerAndGetToken(MockMvc mockMvc, String name, String email, String password) throws Exception {
        String body = String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}", name, email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    public static String loginAndGetToken(MockMvc mockMvc, String email, String password) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }

    public static String getAdminToken(MockMvc mockMvc) throws Exception {
        return loginAndGetToken(mockMvc, "admin@finance.com", "Admin@123");
    }

    public static String getAnalystToken(MockMvc mockMvc) throws Exception {
        return loginAndGetToken(mockMvc, "analyst@finance.com", "Analyst@123");
    }

    public static String getViewerToken(MockMvc mockMvc) throws Exception {
        return loginAndGetToken(mockMvc, "viewer@finance.com", "Viewer@123");
    }
}
