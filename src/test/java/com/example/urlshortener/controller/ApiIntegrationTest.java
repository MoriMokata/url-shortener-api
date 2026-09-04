package com.example.urlshortener.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String EMAIL_A = "owner-a@example.com";
    private static final String EMAIL_B = "owner-b@example.com";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String registerAndLogin(String email) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"P@ssw0rd\"}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"P@ssw0rd\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void shortenRejectsInvalidUrl() throws Exception {
        String token = registerAndLogin(EMAIL_A);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/shorten")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"original_url\":\"not-a-url\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void shortenSucceedsAndRedirectFollowsIt() throws Exception {
        String token = registerAndLogin(EMAIL_A);

        MvcResult shortenResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/shorten")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"original_url\":\"https://example.com/it-target\"}"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        JsonNode shortenJson = objectMapper.readTree(shortenResult.getResponse().getContentAsString());
        String code = shortenJson.get("shortCode").asText();

        mockMvc.perform(MockMvcRequestBuilders.get("/" + code))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.header()
                        .string(HttpHeaders.LOCATION, "https://example.com/it-target"));
    }

    @Test
    void redirectReturnsNotFoundForMissingCode() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/does-not-exist-999"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void listReturnsOnlyUrlsOwnedByCaller() throws Exception {
        String tokenA = registerAndLogin(EMAIL_A);
        String tokenB = registerAndLogin(EMAIL_B);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/shorten")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"original_url\":\"https://example.com/owner-a\"}"))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/urls")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result ->
                        Assertions.assertThat(objectMapper.readTree(result.getResponse().getContentAsString()))
                                .isEmpty());
    }

    @Test
    void deleteReturnsNotFoundForMissingId() throws Exception {
        String token = registerAndLogin(EMAIL_A);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/urls/999999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
