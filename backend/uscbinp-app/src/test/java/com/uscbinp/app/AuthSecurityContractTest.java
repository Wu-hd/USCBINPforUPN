package com.uscbinp.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityContractTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void loginRouteShouldNotBeBlockedByUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
            });
    }
}
