package com.uscbinp.app;

import com.uscbinp.infra.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SystemUserRoleApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    void listUsersWithTokenShouldReturnUnifiedPagedResponse() throws Exception {
        String token = jwtTokenProvider.generateToken("1:admin");
        mockMvc.perform(get("/api/system/users")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.page.pageNum").value(1))
            .andExpect(jsonPath("$.data.page.pageSize").value(10))
            .andExpect(jsonPath("$.data.page.total").isNumber())
            .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void bindRolesWithTokenShouldReturnUnifiedResponse() throws Exception {
        String token = jwtTokenProvider.generateToken("1:admin");
        mockMvc.perform(put("/api/system/users/{id}/roles", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleIds": [1, 2]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.roleIds[0]").value(1))
            .andExpect(jsonPath("$.data.roleIds[1]").value(2));
    }
}
