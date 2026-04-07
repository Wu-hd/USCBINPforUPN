package com.uscbinp.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uscbinp.infra.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SystemUserRoleApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ObjectMapper objectMapper;

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
    void listUsersWithNonAdminTokenShouldBeRestrictedByRegion() throws Exception {
        String token = jwtTokenProvider.generateToken("2:demo");
        mockMvc.perform(get("/api/system/users")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.page.total").value(1))
            .andExpect(jsonPath("$.data.list[0].username").value("demo"))
            .andExpect(jsonPath("$.data.list[1]").doesNotExist());
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

    @Test
    void bindRolesShouldAllowClearingAllBindings() throws Exception {
        String token = jwtTokenProvider.generateToken("1:admin");
        mockMvc.perform(put("/api/system/users/{id}/roles", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleIds": []
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.userId").value(1))
            .andExpect(jsonPath("$.data.roleIds").isArray())
            .andExpect(jsonPath("$.data.roleIds[0]").doesNotExist());
    }

    @Test
    void bindRolesWithNonExistentRoleShouldReturnBusinessErrorCode() throws Exception {
        String token = jwtTokenProvider.generateToken("1:admin");
        mockMvc.perform(put("/api/system/users/{id}/roles", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleIds": [999]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("BIZ_4001"));
    }

    @Test
    void deletingBoundRoleShouldBeRejectedAndRoleShouldDisappearAfterUnbind() throws Exception {
        String token = jwtTokenProvider.generateToken("1:admin");
        String roleCode = "temp_role_" + System.nanoTime();
        MvcResult createdRole = mockMvc.perform(post("/api/system/roles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleCode": "%s",
                      "roleName": "临时角色",
                      "roleStatus": 1
                    }
                    """.formatted(roleCode)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andReturn();
        Long roleId = objectMapper.readTree(createdRole.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(put("/api/system/users/{id}/roles", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleIds": [1, %d]
                    }
                    """.formatted(roleId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(delete("/api/system/roles/{id}", roleId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("BIZ_4001"));

        mockMvc.perform(put("/api/system/users/{id}/roles", 1L)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleIds": [1]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(delete("/api/system/roles/{id}", roleId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(get("/api/system/users/{id}", 1L)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.roleIds[0]").value(1))
            .andExpect(jsonPath("$.data.roleIds[1]").doesNotExist());
    }
}
