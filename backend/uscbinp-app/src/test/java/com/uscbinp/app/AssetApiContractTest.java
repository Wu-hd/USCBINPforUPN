package com.uscbinp.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin")
class AssetApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createNetworkThenListShouldReturnUnifiedResponse() throws Exception {
        mockMvc.perform(post("/api/asset/networks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "networkName": "主干管网",
                      "networkType": "MAIN",
                      "regionCode": "3301",
                      "serviceStatus": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.networkCode").isString())
            .andExpect(jsonPath("$.data.networkName").value("主干管网"));

        mockMvc.perform(get("/api/asset/networks")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("regionCode", "3301"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.page.pageNum").value(1))
            .andExpect(jsonPath("$.data.list[0].networkName").value("主干管网"));
    }

    @Test
    void createPipeSectionWithMissingNetworkShouldReturnBusinessError() throws Exception {
        mockMvc.perform(post("/api/asset/pipe-sections")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sectionName": "管段A",
                      "networkId": 999,
                      "regionCode": "3301"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("BIZ_4001"));
    }
}
