package com.uscbinp.app;

import com.uscbinp.domain.service.monitor.impl.InMemoryMonitorDataStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class MonitorIngestApiContractTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InMemoryMonitorDataStore store;

    @Test
    void ingestShouldReturnUnifiedAcceptedPayload() throws Exception {
        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 1001,
                      "metricType": "PRESSURE",
                      "metricValue": 1.23,
                      "qualityFlag": 1,
                      "edgeNodeCode": "edge-a",
                      "traceId": "api-1"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.accepted").value(true))
            .andExpect(jsonPath("$.data.alarmFlag").value(0));
    }

    @Test
    void ingestInvalidPointShouldReturnBusinessError() throws Exception {
        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 9999,
                      "metricType": "PRESSURE",
                      "metricValue": 1.23
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("BIZ_4001"))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void ingestAfterShouldBeQueryableFromCurrentApi() throws Exception {
        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 1001,
                      "metricType": "PRESSURE",
                      "metricValue": 1.66,
                      "qualityFlag": 1,
                      "traceId": "api-2"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"));

        mockMvc.perform(get("/api/monitor/current")
                .param("pointId", "1001")
                .param("pageNum", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.items[0].pointId").value(1001))
            .andExpect(jsonPath("$.data.items[0].currentValue").value(1.66));
    }

    @Test
    void ingestAlarmShouldCreateAlertEventRecord() throws Exception {
        mockMvc.perform(post("/api/monitor/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "pointId": 1001,
                      "metricType": "PRESSURE",
                      "metricValue": 2.88,
                      "qualityFlag": 1,
                      "traceId": "api-alert-1"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("00000"))
            .andExpect(jsonPath("$.data.alarmFlag").value(1))
            .andExpect(jsonPath("$.data.alertCode").isNotEmpty());
        assertTrue(store.alertEventSize() >= 1);
    }
}
