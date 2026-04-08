package com.uscbinp.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin")
class DeviceApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createDeviceThenListShouldReturnUnifiedResponse() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/device/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "deviceName": "压力采集器",
                      "deviceType": "PRESSURE_SENSOR",
                      "regionCode": "3301"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.deviceCode").isString())
            .andReturn();

        JsonNode node = objectMapper.readTree(created.getResponse().getContentAsString());
        long deviceId = node.path("data").path("id").asLong();

        mockMvc.perform(get("/api/device/measure-points")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("deviceId", String.valueOf(deviceId))
                .param("regionCode", "3301"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"));
    }

    @Test
    void createPointWithMissingDeviceShouldReturnBusinessError() throws Exception {
        mockMvc.perform(post("/api/device/measure-points")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointName": "压力测点A",
                      "deviceId": 999,
                      "metricType": "PRESSURE",
                      "regionCode": "3301"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("BIZ_4001"));
    }

    @Test
    @WithMockUser(username = "viewer")
    void nonAdminListWithoutRegionShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/device/devices")
                .param("pageNum", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("AUTH_4030"));
    }
}
