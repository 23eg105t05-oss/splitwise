package com.splitwise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.splitwise.dto.request.LoginRequest;
import com.splitwise.dto.request.RegisterRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SplitwiseApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static Long groupId;

    // ── Auth Tests ────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void registerUser_ShouldReturn201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("testuser@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @Order(2)
    void registerUser_DuplicateEmail_ShouldReturn400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("testuser@example.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    void loginUser_ShouldReturn200AndToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("testuser@example.com");
        req.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(body).path("data").path("token").asText();
    }

    @Test
    @Order(4)
    void loginUser_WrongPassword_ShouldReturn401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("testuser@example.com");
        req.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    void getMe_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("testuser@example.com"));
    }

    // ── Group Tests ───────────────────────────────────────────────────────────

    @Test
    @Order(6)
    void createGroup_ShouldReturn201() throws Exception {
        String body = """
                { "name": "Goa Trip", "description": "Fun trip!", "currency": "INR" }
                """;

        MvcResult result = mockMvc.perform(post("/api/groups")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Goa Trip"))
                .andReturn();

        String resp = result.getResponse().getContentAsString();
        groupId = objectMapper.readTree(resp).path("data").path("id").asLong();
    }

    @Test
    @Order(7)
    void getMyGroups_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/groups")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(8)
    void getGroupById_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/groups/" + groupId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(groupId));
    }

    // ── Bill Tests ────────────────────────────────────────────────────────────

    @Test
    @Order(9)
    void createBill_ShouldReturn201() throws Exception {
        String body = String.format("""
                {
                  "title": "Dinner",
                  "amount": 1200.00,
                  "groupId": %d,
                  "splitType": "EQUAL",
                  "category": "FOOD"
                }
                """, groupId);

        mockMvc.perform(post("/api/bills")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Dinner"));
    }

    @Test
    @Order(10)
    void getBillsByGroup_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bills/group/" + groupId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── Health Check ──────────────────────────────────────────────────────────

    @Test
    @Order(11)
    void healthCheck_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
